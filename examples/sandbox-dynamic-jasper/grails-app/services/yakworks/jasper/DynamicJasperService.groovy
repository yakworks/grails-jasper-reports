package yakworks.jasper

import java.awt.*
import java.util.List

import groovy.util.logging.Slf4j

import org.apache.commons.lang.StringUtils
import org.grails.core.artefact.DomainClassArtefactHandler

import ar.com.fdvs.dj.domain.*
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder
import ar.com.fdvs.dj.domain.constants.*
import ar.com.fdvs.dj.domain.entities.DJGroup
import ar.com.fdvs.dj.domain.entities.DJGroupVariable
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn
import ar.com.fdvs.dj.domain.entities.columns.ExpressionColumn
import ar.com.fdvs.dj.domain.entities.columns.PropertyColumn
import ar.com.fdvs.dj.domain.entities.columns.SimpleColumn
import grails.core.GrailsDomainClass
import grails.core.GrailsDomainClassProperty
import grails.util.GrailsClassUtils
import grails.util.GrailsUtil

import static GrailsClassUtils.isAssignableOrConvertibleFrom

//@Transactional
@Slf4j
class DynamicJasperService {
    static transactional = false

    def grailsApplication

    //defaults. TODO externalize these into config
    Map<String,Object> styleDefaults = [
            font : Font.ARIAL_MEDIUM,
            textColor : Color.decode("#555555"),
            border : new Border(1f, Border.BORDER_STYLE_SOLID, Color.decode("#bbbbbb"))
    ]

    DynamicReport buildDynamicReport(def domainClass, def config, def params) {
        println "doReport with $params"

        Style groupTitleStyle = new Style();
        groupTitleStyle.setFont(Font.ARIAL_BIG);

        List fields = config.fields
        //?: getPropertyValue(domainClass.clazz, 'reportColumns') ?: domainClass.properties.name - ['id', 'version']

        Map columns = addColumns(config, domainClass, fields)

        DynamicReportBuilder drb = setupReportBuilder()

        List groupFields = config.groupFields
        Map groupVariables = config.groupVariables
        //config.groupFooterColumns

        List<DJGroup> djGroups = buildGroupBands(columns, groupFields, groupVariables)
        djGroups.each { drb.addGroup(it) }

        columns.each { String name, AbstractColumn column ->
            drb.addColumn(column)
        }
        drb.setUseFullPageWidth(true);
        drb.addAutoText(AutoText.AUTOTEXT_PAGE_X_SLASH_Y, AutoText.POSITION_FOOTER, AutoText.ALIGNMENT_RIGHT);
        DynamicReport dr = drb.build();
        return dr
    }

    Style initStyle(Style style){
        style.font = styleDefaults.font
        style.textColor = styleDefaults.textColor
    }
    /**
     * The style setup for the values that are printed for the group
     * @param index
     * @return
     */
    Style groupHeaderStyle(Integer index = 0) {
        Style style = new Style()
        style.font = (Font)styleDefaults.font
        style.font.bold = true
        //style.font = (index == 0 )? Font.ARIAL_BIG_BOLD : Font.ARIAL_BIG_BOLD
        style.textColor = styleDefaults.textColor
        style.font.fontSize = 13 - index
        style.paddingBottom = 7 - (index * 2)
        style.paddingTop = 7 - (index * 2)
        style.paddingLeft = index * 4
        //style.border = Border.NO_BORDER()
        style.borderTop = styleDefaults.border
        style.borderBottom = styleDefaults.border
        return style
    }

    List<DJGroup> buildGroupBands(Map columns, List groupFields, Map footerFields){
        List<DJGroup> groups = []
        groupFields.eachWithIndex {String groupColumn, Integer index ->
            DJGroup group = new DJGroup()
            AbstractColumn col = columns[groupColumn]
            group.columnToGroupBy = (PropertyColumn) columns[groupColumn]
            col.style = groupHeaderStyle(index)

            /** mess with the header so it not shown. needs to be here in DynamicJasper:5.0.10 or the layout is all messed up **/
            //col.setHeaderStyle(groupTitleStyle)
            col.setTitle("")
            col.setWidth(0)

            footerFields.each{ String footerColName, String calc ->
                AbstractColumn fcol = columns[footerColName]
                Style fvStyle = groupHeaderStyle(index)
                //fvStyle.borderTop = Border.THIN()
                fvStyle.horizontalAlign = fcol.style.horizontalAlign
                fvStyle.pattern = fcol.style.pattern
                group.addFooterVariable(new DJGroupVariable(fcol,DJCalculation."${calc.toUpperCase()}",fvStyle))

            }
            group.headerHeight = 20

            if(index == 0) {
                group.layout = GroupLayout.VALUE_IN_HEADER_WITH_HEADERS_AND_COLUMN_NAME
            }else{
                group.layout = GroupLayout.VALUE_IN_HEADER
            }
            //gb.setGroupLayout(GroupLayout.VALUE_IN_HEADER_WITH_HEADERS_AND_COLUMN_NAME)
            groups.add(group)
        }
        return groups
    }

    DynamicReportBuilder setupReportBuilder(){
        Style detailStyle = new Style();
        detailStyle.setVerticalAlign(VerticalAlign.TOP);
        detailStyle.paddingTop = 3
        detailStyle.paddingBottom = 3
        detailStyle.paddingLeft = 5
        detailStyle.paddingRight = 5

        Style groupTitleStyle = new Style();
        groupTitleStyle.setFont(Font.VERDANA_BIG_BOLD);

        //Style headerStyle = new Style();
        Style headerStyle =new Style().with {
            font = Font.VERDANA_MEDIUM_BOLD
            border = Border.PEN_1_POINT()
            borderColor = Color.decode("#bbbbbb")
            backgroundColor = Color.decode("#f9fafb")
            textColor = Color.black
            horizontalAlign = HorizontalAlign.CENTER
            verticalAlign = VerticalAlign.MIDDLE
            transparency = Transparency.OPAQUE
            return delegate
        }

        Style headerVariables = new Style();
        headerVariables.setFont(Font.ARIAL_MEDIUM_BOLD);
//		headerVariables.setBorderBottom(Border.THIN());
        headerVariables.setHorizontalAlign(HorizontalAlign.RIGHT);
        headerVariables.setVerticalAlign(VerticalAlign.MIDDLE);

        Style titleStyle = new Style();
        titleStyle.setFont(new Font(18, Font._FONT_VERDANA, true));

        Style importeStyle = new Style();
        importeStyle.setHorizontalAlign(HorizontalAlign.RIGHT);

        Style oddRowStyle = new Style();
        oddRowStyle.setBorder(Border.NO_BORDER());
        oddRowStyle.setBackgroundColor(Color.LIGHT_GRAY);
        oddRowStyle.setTransparency(Transparency.OPAQUE);

        DynamicReportBuilder drb = new DynamicReportBuilder();
        Integer margin = new Integer(20);
        drb.with {
            setTitleStyle(titleStyle)
            setTitle("Sales Report")                    //defines the title of the report
            setSubtitle("The items in this report correspond to the main products: DVDs, Books, Foods and Magazines")
            setDetailHeight(15)
            setLeftMargin(20)
            setRightMargin(20)
            setTopMargin(20)
            setBottomMargin(20)
            setPrintBackgroundOnOddRows(false)
            setGrandTotalLegend("Grand Total")
            setGrandTotalLegendStyle(headerVariables)
            setDefaultStyles(titleStyle, null, headerStyle, detailStyle)
            setPrintColumnNames(false)
            setOddRowBackgroundStyle(oddRowStyle)
            setPrintBackgroundOnOddRows(false)
            //.addImageBanner(System.getProperty("user.dir") +"/target/test-classes/images/logo_fdv_solutions_60.jpg", new Integer(100), new Integer(30), ImageBanner.ALIGN_RIGHT)
        }
        return drb
    }

    protected Map<String,AbstractColumn> addColumns( Map config, GrailsDomainClass domainClass, List<String> fields) {
        Map columns = [:]
        fields.each { propertyName ->
            Map<String,Object> colProps = [:]
            Style style = getDetailStyle()
            GrailsDomainClassProperty property = domainClass.getPropertyByName(propertyName)

            SimpleColumn column = new SimpleColumn()
            Class dcPropType = property.type
            Class propertyType
            //assert Boolean.class.getName() == "foo"
            List<Class> ctypes = [Boolean,Integer,BigDecimal,Short,Float,Double,Byte,Character,Date,String,List]

            for(Class clazz : ctypes){
                if(isAssignableOrConvertibleFrom(clazz, dcPropType)){
                    //assert clazz.getName() == "foo"
                    colProps['clazz'] = clazz
                    colProps['className'] = clazz.getName()
                }
            }

            //GrailsClassUtils.isAssignableOrConvertibleFrom(Integer, it):
//            case Number:
//            case Boolean:
//            case Character:
//            case Date:
//            case String:
            //Class propertyType
            switch (property.type) {
                case int:
                    propertyType = Integer
                    break
                case char:
                    propertyType = Character
                    break
                case byte:
                case short:
                case long:
                case float:
                case double:
                case boolean:
                    propertyType = Class.forName('java.lang.' + StringUtils.capitalize(property.type.name))
                    break
                case Long.class:
                    assert GrailsClassUtils.isAssignableOrConvertibleFrom(Long, property.type)
                    assert GrailsClassUtils.isAssignableOrConvertibleFrom(Long.class, property.type)
                    //assert 1==0
                    propertyType = Long
                    break
                case Number:
                case Boolean:
                case Character:
                case Date:
                case String:
                    propertyType = property.type
                    break
                default:
                    //report.fields << new ColumnProperty(propertyName, property.type.name)
                    column = new ExpressionColumn()
                    column.expression = getToStringExpression(propertyName)
                    propertyType = String
            }
            column.columnProperty = new ColumnProperty(propertyName, propertyType.name)
            column.title = config.columnTitles?."${propertyName}" ?: property.naturalName
            //def style = getStyle(config.detailStyle)
            if (Number.isAssignableFrom(propertyType) || Date.isAssignableFrom(propertyType)) {
                style.horizontalAlign = HorizontalAlign.RIGHT
            } else {
                //style = getStyle(config.detailStyle)
            }
            def propertyPattern
            if (config.patterns?."${propertyName}") {
                propertyPattern = config.patterns?."${propertyName}"
            } else {
                switch (propertyType) {
                    case Byte:
                    case Short:
                    case Integer:
                    case Long:
                        propertyPattern = config.intPattern
                        break;
                    case Float:
                    case Double:
                        propertyPattern = config.floatPattern
                        break;
                    case Date:
                        propertyPattern = config.datePattern
                        break;
                    default:
                        propertyPattern = null
                }
            }

            style.pattern = propertyPattern
            column.style = style
            columns[(propertyName)] = column
        }
        return columns
    }

    protected Style getDetailStyle(){
        Style detailStyle = new Style();
        detailStyle.setVerticalAlign(VerticalAlign.TOP);
        detailStyle.paddingTop = 3
        detailStyle.paddingBottom = 3
        detailStyle.paddingLeft = 5
        detailStyle.paddingRight = 5
        return detailStyle
    }

    GrailsDomainClassProperty getProperty(GrailsDomainClass domainClass, String propertyName) {
        GrailsDomainClassProperty property
        propertyName.tokenize('.').each { part ->
            property = domainClass.properties.find { prop ->
                prop.name == part
            }
            def name = property.type.simpleName
            domainClass = grailsApplication.getArtefactByLogicalPropertyName(DomainClassArtefactHandler.TYPE, name[0].toLowerCase() + name[1 .. - 1])
        }
        return property
    }


    private ConfigObject loadConfig() {
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

    def getPropertyValue(def clazz, def propertyName) {
        clazz.metaClass.hasProperty(clazz, propertyName)?.getProperty(clazz)
    }

    def setPropertyIfNotNull(def target, def propertyName, def value) {
        if (value != null && (!(value instanceof ConfigObject) || !(value.isEmpty()))) {
            target[propertyName] = value
        }
    }

    CustomExpression getToStringExpression(fieldName){
       return new CustomExpression() {
            public Object evaluate(Map fields, Map variables, Map parameters) {
                fields[(fieldName)].toString()
            }
            public String getClassName() {
                String.name
            }
        }
    }
}
