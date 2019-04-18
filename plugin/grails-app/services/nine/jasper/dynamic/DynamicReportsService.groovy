package nine.jasper.dynamic

import grails.core.GrailsApplication
import grails.util.GrailsUtil
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.HyperLinkBuilder
import net.sf.dynamicreports.report.builder.column.ColumnBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.column.ValueColumnBuilder
import net.sf.dynamicreports.report.builder.component.Components
import net.sf.dynamicreports.report.builder.datatype.DataTypes
import net.sf.dynamicreports.report.builder.expression.Expressions
import net.sf.dynamicreports.report.builder.expression.JasperExpression
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder
import net.sf.dynamicreports.report.builder.group.Groups
import net.sf.dynamicreports.report.builder.subtotal.AggregationSubtotalBuilder
import net.sf.dynamicreports.report.builder.subtotal.Subtotals
import net.sf.dynamicreports.report.constant.GroupHeaderLayout
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import nine.reports.DomainMetaUtils
import nine.reports.FieldMetadata
import org.grails.datastore.mapping.model.PersistentEntity

import static net.sf.dynamicreports.report.builder.DynamicReports.hyperLink

@Slf4j
@CompileDynamic
class DynamicReportsService {
    static transactional = false

    GrailsApplication grailsApplication

    JasperReportBuilder buildDynamicReport(Map reportCfg) {
        PersistentEntity domainClass = DomainMetaUtils.findDomainClass(reportCfg.domain)
        //TODO blow logical error here is domainClass can't be found

        buildDynamicReport(domainClass, reportCfg)
    }

    JasperReportBuilder buildDynamicReport(PersistentEntity domainClass, Map reportCfg, Map params = null) {
        log.debug "doReport with $params"

        //TODO do some basic validation on reportCfg. maybe even setup domains for them
        List fields = reportCfg["fields"] as List
        //StyleStatics.init()

        //?: getPropertyValue(domainClass.clazz, 'reportColumns') ?: domainClass.properties.name - ['id', 'version']
        JasperReportBuilder jrb = new JasperReportBuilder()
                .title(TemplateStyles.createTitleComponent("Group"))
                .setTemplate(TemplateStyles.reportTemplate)
                .templateStyles(TemplateStyles.loadStyles(grailsApplication.mainContext))

//        def res = grailsApplication.mainContext.getResource("classpath:nine/jasper/DefaultTemplate.jrxml")
//        jrb.setTemplateDesign(res.inputStream)

        if (reportCfg.highlightDetailOddRows) {
            jrb.highlightDetailOddRows().setDetailOddRowStyle(TemplateStyles.oddRowStyle)
        }
        if (reportCfg.showGridLines) {
            jrb.setColumnStyle(TemplateStyles.columnWithGridLines)//StyleTemplates.columnStyleWithGridLines)//
        }
        if (reportCfg.tableOfContents) {
            jrb.tableOfContents()
        }
        if (reportCfg.ignorePagination) {
            jrb.ignorePagination()
        } else {
            jrb.pageFooter(TemplateStyles.createFooter())
        }

        if (reportCfg.landscape) {
            jrb.setPageFormat(PageType.LETTER, PageOrientation.LANDSCAPE)
        }

        //TODO should we do this just in case?
        //.sortBy(dateColumn, invoiceColumn)

        //Column setups
        Map<String, FieldMetadata> fieldMetaMap = DomainMetaUtils.getFieldMetadata(domainClass, fields, reportCfg.columnTitles)
        populateColumnBuilders(fieldMetaMap, jrb)
        List<ColumnBuilder> columnBuilderList = fieldMetaMap.values()*.builder.toList()
        jrb.columns(*columnBuilderList)

        //Groups
        Map<String, Map> groupBuilders = buildGroupBands(fieldMetaMap, reportCfg)
        groupBuilders.eachWithIndex { k, v, i ->
            jrb.groupBy(v.builder.headerWithSubtotal()).subtotalsAtGroupFooter(v.builder, *v.subtotalBuilders)
        }

        if (reportCfg.columnHeaderInFirstGroup) jrb.setShowColumnTitle(false)
        if (reportCfg.showTableOfContents) jrb.tableOfContents()

        return jrb
    }

    Collection<?> createDataSource(PersistentEntity domainClass, List groupFields) {
        List results
        if (groupFields) {
            def c = domainClass.javaClass.createCriteria()
            results = c.list {
                orderNested(groupFields).call()
            }
            //recs = domainClass.clazz.findAll("from $domainClass.clazz.name as s order by ${groupFields.join(',')}")
        } else {
            results = domainClass.javaClass.list()
        }
        return results

    }

    /**
     * add a reference to the column builder into the FieldMetadata
     * @param fieldMap
     * @return the same map ref populated
     */
    Map<String, FieldMetadata> populateColumnBuilders(Map<String, FieldMetadata> fieldMap, JasperReportBuilder jrb) {
        //Map<String,Map> drCols = [:]

        fieldMap.each { /*key*/ String field, /*value*/ FieldMetadata fld ->
            ColumnBuilder colb
            colb = Columns.column(field, DataTypes.detectType(fld.typeClass)).setAnchorName(field)
            colb.setWidth(fld.width == null ? 4 : fld.width)
            //link symbols , 221e,260d, 2709 is email, 270e is pencil
            HyperLinkBuilder link = hyperLink(jrExp('"https://www.google.com/search?q=" + $F{' + field + '}'))
            colb.setHyperLink(link)

            if (fld.isBooleanType()) {
                jrb.addField(field, Boolean.class) //drb.field(field,Boolean.class)

                //? (char)0x2611 : (char)0x2610") //<- see http://dejavu.sourceforge.net/samples/DejaVuSans.pdf for more options
                JasperExpression bool = jrExp('$F{' + field + '} ? (char)0x2713 : ""')
                colb = Columns.column(bool).setDataType(DataTypes.booleanType())
                //do style
                //def sb = new StyleBuilder()
                //sb.object.parentStyle = jrb.object.columnStyle
                //colb.style = sb.bold()//.setFontSize(18)
                colb.setWidth(fld.width == null ? 1 : fld.width)
            }

            colb.setTitle(jrText(fld.title))
            fld.builder = colb
        }
        return fieldMap
    }

    Map<String, Map> buildGroupBands(Map<String, FieldMetadata> fieldMetaMap, Map config) {

        Map<String, Map> groups = [:]
        int grpSize = (config.groups as Map).size()
        config.groups.eachWithIndex { String field, Integer index ->
            ColumnGroupBuilder group = Groups.group("Group_$field", fieldMetaMap[field].builder as ValueColumnBuilder)
            boolean isLastOrSingleGroup = (grpSize == index + 1)
            group.setPadding(3)

            if (index == 0) {
                group.setHeaderLayout(GroupHeaderLayout.VALUE)
                if (config?.columnHeaderInFirstGroup) group.showColumnHeaderAndFooter()
                group.setStyle(TemplateStyles.group)
                group.setFooterStyle(TemplateStyles.group)

            } else if (index == 1) {
                //group.setHeaderLayout(GroupHeaderLayout.VALUE)
                group.setStyle(TemplateStyles.groupL2)
                group.setHeaderStyle(TemplateStyles.groupHeaderL2)
                group.setFooterStyle(TemplateStyles.groupFooterL2)
                //group.setPadding(3)
                //group.showColumnHeaderAndFooter
            } else {
                group.setStyle(TemplateStyles.groupL3)
                group.setHeaderStyle(TemplateStyles.groupHeaderL3)
                group.setFooterStyle(TemplateStyles.groupFooterL3)
            }

            List<AggregationSubtotalBuilder> sbtList = []

            config.subtotals.each { grpField, calc ->
                AggregationSubtotalBuilder subtot = Subtotals."${calc}"(fieldMetaMap[grpField].builder as ValueColumnBuilder)

                sbtList.add subtot
            }
//            def comp = drb.cmp.horizontalList()
//                .setFixedDimension(557, 20)
//                //.setBackgroundComponent(...)
//                .add(drb.cmp.gap(557,2))
//                .newRow()
//                .add(
//                    //2. a gap of width 70
//                    drb.cmp.gap(70,13),
//                    //3. the text field
//                    drb.cmp.text("Hello World")//.setStyle(...)
//                );
//
//            group.addFooterComponent(comp)

            //don't add it to the last group by default or if there is only 1 group
            if (config.groupTotalLabels && sbtList && !isLastOrSingleGroup) {
                //just add it to the first one
                //sbtList[0].setLabel("${fieldMetaMap[field].title} Totals").setLabelPosition(Position.LEFT);

                JasperExpression<String> label = jrExp("\$F{" + field + "} + \" Total\"", String.class)
                //sbtList.add drb.sbt.first(label,fieldMetaMap[config.groupTotalLabels].builder)
                group.setFooterBackgroundComponent(
                        Components.text(label).setStyle(TemplateStyles.subtotal)
                )
            }

            //add the subtotals
            groups[field] = [:]
            groups[field].subtotalBuilders = sbtList
            groups[field].builder = group
        }
        return groups
    }

    @SuppressWarnings(['UnusedPrivateMethod'])
    private Map loadConfig() {
        def config = grailsApplication.config
        GroovyClassLoader classLoader = new GroovyClassLoader(getClass().classLoader)
        config.merge(new ConfigSlurper(GrailsUtil.environment).parse(classLoader.loadClass('DefaultDynamicJasperConfig')))
        try {
            config.merge(new ConfigSlurper(GrailsUtil.environment).parse(classLoader.loadClass('DynamicJasperConfig')))
        } catch (Exception ignored) {
            // ignore, just use the defaults
        }
        return new ConfigSlurper(GrailsUtil.environment).parse(new Properties()).merge(config.dynamicJasper)
    }

    def getPropertyValue(Class clazz, String propertyName) {
        clazz.metaClass.hasProperty(clazz, propertyName)?.getProperty(clazz)
    }

    def setPropertyIfNotNull(Object target, String propertyName, Object value) {
        if (value != null && (!(value instanceof ConfigObject) || !(value.isEmpty()))) {
            target[propertyName] = value
        }
    }

    //jasper
    /**
     * Creates a new jasper string expression, useful only for showing a static text.<br/>
     * This method escapes the characters in a {@code String} using Java String rules.
     *
     * @param text text to be shown
     * @return the expression
     */
    public JasperExpression<String> jrText(String text) {
        return Expressions.jasperSyntaxText(text)
    }

    /**
     * Creates a new jasper expression.<br/>
     * This expression allows declaring an expression in a Jasper native syntax. Knowledge of the jasper syntax is also required for proper use.
     *
     * @param expression the jasper expression
     * @param valueClass the expression class
     * @return the expression
     */
    public <T> JasperExpression<T> jrExp(String expression, Class<? super T> valueClass) {
        return Expressions.jasperSyntax(expression, valueClass)
    }

    /**
     * Creates a new jasper expression.<br/>
     * This expression allows declaring an expression in a Jasper native syntax. Knowledge of the jasper syntax is also required for proper use.
     *
     * @param expression the jasper expression
     * @return the expression
     */
    @SuppressWarnings("rawtypes")
    public JasperExpression jrExp(String expression) {
        return Expressions.jasperSyntax(expression)
    }

}
