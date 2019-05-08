package foo

import java.awt.*
import java.util.List

import org.apache.commons.lang.StringUtils
import org.grails.core.artefact.DomainClassArtefactHandler

import ar.com.fdvs.dj.core.DynamicJasperHelper
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager
import ar.com.fdvs.dj.domain.*
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder
import ar.com.fdvs.dj.domain.builders.GroupBuilder
import ar.com.fdvs.dj.domain.constants.Border
import ar.com.fdvs.dj.domain.constants.GroupLayout
import ar.com.fdvs.dj.domain.constants.HorizontalAlign
import ar.com.fdvs.dj.domain.constants.VerticalAlign
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn
import ar.com.fdvs.dj.domain.entities.columns.ExpressionColumn
import ar.com.fdvs.dj.domain.entities.columns.PropertyColumn
import ar.com.fdvs.dj.domain.entities.columns.SimpleColumn
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.plugins.GrailsPluginManager
import grails.util.GrailsUtil
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.JasperReport
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
import yakworks.jasper.spring.JasperView
import yakworks.jasper.spring.JasperViewResolver

class DjReportController {
    GrailsPluginManager pluginManager
    GrailsApplication grailsApplication
    JasperViewResolver jasperViewResolver
    //DynamicJasperService dynamicJasperService

    def image = {
        def pluginPath = pluginManager.getGrailsPlugin('dynamic-jasper').getPluginPath()
        redirect(uri: "${pluginPath}/images/${params.image}.gif")
    }

    def index = {
        println "Index with $params"
        if (params.report) {
            namedReport()
        } else if (params.entity) {
            entityReport()
        } else {
            handleError()
        }
    }

    def handleError() {
        redirect(uri: '/')
    }

    def namedReport() {
        def config = loadConfig()
        def reportConfig = config[params.report]
        if (reportConfig) {
            config.merge(reportConfig)
            GrailsClass domainClass = grailsApplication.getArtefactByLogicalPropertyName(DomainClassArtefactHandler.TYPE, config.entity)
            doReport(domainClass, config, params, request, response)
        } else {
            handleError()
        }
    }

    def rptConfig =[
//            group1:[
//                column:[]
//            ],
//            group1:[
//                    columns
//            ]
        columns: ['customer.name','product.group.name', 'product.name', 'qty', 'amount'],
        columnTitles:['customer.name':'Customer','product.group.name':'Group', 'product.name':'Product', 'qty':'Qty', 'amount':'Amount'],
//        groupBuilders:[
//            customer:[
//                amount:"sum",
//                id:"count"
//            ]
//        ]
        groupColumns: ['customer.name','product.group.name'],
        groupFooterColumns: [amount:"sum"]//,id:"count"]
        //groupOperations: ['SUM']
            //,
            //autoTexts: [new AutoText(AutoText.AUTOTEXT_PAGE_X_OF_Y, AutoText.POSITION_FOOTER, HorizontalBandAlignment.buildAligment(AutoText.ALIGMENT_CENTER), (byte)0, 200, 200)]

    ]

    def entityReport() {
        println "entityReport with $params"
        GrailsClass domainClass = grailsApplication.getArtefactByLogicalPropertyName(DomainClassArtefactHandler.TYPE, params.entity)
        assert domainClass
        //def reportable = getPropertyValue(domainClass.clazz, 'reportable')
        //because an empty map also means this class is reportable
        //if (reportable != null) {
            def config = loadAndMergeConfig(domainClass, rptConfig, params)
            doReport(domainClass, config, params, request, response)
        //} else {
        //    handleError()
        //}
    }

    def doReport(def domainClass, def config, def params, def request, def response) {
        println "doReport with $params"

        Style groupTitleStyle = new Style();
        groupTitleStyle.setFont(Font.ARIAL_BIG);

        List columnNames = config.columns ?: getPropertyValue(domainClass.clazz, 'reportColumns') ?: domainClass.properties.name - ['id', 'version']

        Map columns = addColumns(config, domainClass, columnNames)

        DynamicReportBuilder drb = setupReportBuilder()

        List groupColumns = config.groupColumns

        //List<DJGroup> djGroups = dynamicJasperService.buildGroupBands(columns, config.groupColumns, config.groupFooterColumns)
        //djGroups.each{ drb.addGroup(it) }
        if (groupColumns) {
            groupColumns.each {String groupColumn ->
                GroupBuilder gb = new GroupBuilder();
                gb.setCriteriaColumn((PropertyColumn) columns[groupColumn])
                //gb.addFooterVariable(columns[groupColumn],DJCalculation.SUM)
                def col = columns[groupColumn] as AbstractColumn
                //col.setStyle(titleStyle)
                //col.setHeaderStyle(groupTitleStyle)
                col.style.setFont(Font.ARIAL_BIG_BOLD)
                col.style.setPaddingBottom(5)
                col.style.paddingLeft = 0
                col.setTitle("")
                col.setWidth(0)
                col.style.border = Border.NO_BORDER()
                config.groupFooterColumns.each{ colName, calc ->
                    gb.addFooterVariable(columns[colName] , DJCalculation."${calc.toUpperCase()}",groupTitleStyle)
                    //gb.addHeaderVariable(columns[colName] , DJCalculation."${calc.toUpperCase()}",groupTitleStyle)
                }
                //gb.addHeaderVariable(columns["customer.id"] , DJCalculation.NOTHING)
                gb.setHeaderHeight(20)
                gb.setGroupLayout(GroupLayout.VALUE_IN_HEADER_WITH_HEADERS_AND_COLUMN_NAME)
                drb.addGroup( gb.build())
            }
        }

        columns.each{String name, AbstractColumn column->
            drb.addColumn(column)
        }
        drb.setUseFullPageWidth(true);
        drb.addAutoText(AutoText.AUTOTEXT_PAGE_X_SLASH_Y, AutoText.POSITION_FOOTER, AutoText.ALIGNMENT_RIGHT);
        DynamicReport dr = drb.build();

        def items
        if (config.dataSource) {
            items = config.dataSource.call(session, params)
        } else if (groupColumns) {
            items = domainClass.clazz.findAll("from $domainClass.clazz.name as s order by ${groupColumns.join(',')}")
        } else {
            items = domainClass.clazz.list()
        }


        JasperReport jr = DynamicJasperHelper.generateJasperReport(dr, new ClassicLayoutManager(), params)
        JasperView view = (JasperView) jasperViewResolver.getView(jr)

        JRDataSource ds = new JRBeanCollectionDataSource(items)
        Map model = [data:ds]
        view.render(model,request,response)

//        def print = JasperFillManager.fillReport(jr, params, ds)
//        //JasperPrint print = DynamicJasperHelper.generateJasperPrint(dr, new ClassicLayoutManager(), dataSource)
//        def reportFileName = config.fileName
//        def reportFormat = params.reportFormat ?: 'HTML'
//
//
//        ReportWriter reportWriter = ReportWriterFactory.getInstance().getReportWriter(print, reportFormat, [(JRHtmlExporterParameter.IMAGES_URI): "${request.contextPath}/djReport/image?image=".toString(), (JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN): config.isUsingImagesToAlign])
//        if (reportFileName) {
//            response.addHeader('content-disposition', "attachment; filename=${reportFileName}.${reportFormat.toLowerCase()}")
//        }
//
//        //ReportExporter.exportReport
//        reportWriter.writeTo(response)

    }

    protected def setupReportBuilder(){
        Style detailStyle = new Style();
        detailStyle.setVerticalAlign(VerticalAlign.TOP);
        detailStyle.paddingTop = 3
        detailStyle.paddingBottom = 10
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
            delegate
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

    protected Map<String,AbstractColumn> addColumns(def config, def domainClass, def columnNames) {
        Map columns = [:]
        columnNames.each { propertyName ->
            Style style = getDetailStyle()
            def property = getProperty(domainClass, propertyName)
            def column = new SimpleColumn()
            def propertyType
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
                    column.expression = new ToStringCustomExpression(propertyName)
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

    def getProperty(def domainClass, def propertyName) {
        def property
        propertyName.tokenize('.').each { part ->
            property = domainClass.properties.find { prop ->
                prop.name == part
            }
            def name = property.type.simpleName
            domainClass = grailsApplication.getArtefactByLogicalPropertyName(DomainClassArtefactHandler.TYPE, name[0].toLowerCase() + name[1 .. - 1])
        }
        return property
    }

    def getPropertyValue(def clazz, def propertyName) {
        clazz.metaClass.hasProperty(clazz, propertyName)?.getProperty(clazz)
    }

    def setPropertyIfNotNull(def target, def propertyName, def value) {
        if (value != null && (!(value instanceof ConfigObject) || !(value.isEmpty()))) {
            target[propertyName] = value
        }
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
    def loadAndMergeConfig(def domainClass, def classConfig, def params) {
        def config = loadConfig()
        Properties props = new Properties()
        setPropertyIfNotNull(props, 'entity', params.entity)
        setPropertyIfNotNull(props, 'title', classConfig.title)
        setPropertyIfNotNull(props, 'columns', params.reportColumns?.split(',') ?: classConfig.columns ?: domainClass.properties.name - ['id', 'version'])
        setPropertyIfNotNull(props, 'patterns', classConfig.patterns)
        setPropertyIfNotNull(props, 'columnTitles', classConfig.columnTitles)
        setPropertyIfNotNull(props, 'groupColumns', params.groupColumns?.split(',') ?: classConfig.groupColumns)
        setPropertyIfNotNull(props, 'groupFooterColumns', params.groupFooterColumns?.split(',') ?: classConfig.groupFooterColumns)
        setPropertyIfNotNull(props, 'groupOperations', params.groupOperations?.split(',') ?: classConfig.groupOperations ?: ('SUM,' * (props.groupFooterColumns?.size() ?: 0)).split(','))
        setPropertyIfNotNull(props, 'dataSource', classConfig.dataSource)
        setPropertyIfNotNull(props, 'fileName', classConfig.fileName)
        setPropertyIfNotNull(props, 'useFullPageWidth', classConfig.useFullPageWidth)
        setPropertyIfNotNull(props, 'page', classConfig.page)
        setPropertyIfNotNull(props, 'intPattern', classConfig.intPattern)
        setPropertyIfNotNull(props, 'floatPattern', classConfig.floatPattern)
        setPropertyIfNotNull(props, 'datePattern', classConfig.datePattern)
        setPropertyIfNotNull(props, 'titleStyle', classConfig.titleStyle)
        setPropertyIfNotNull(props, 'subtitleStyle', classConfig.subtitleStyle)
        setPropertyIfNotNull(props, 'headerStyle', classConfig.headerStyle)
        setPropertyIfNotNull(props, 'detailStyle', classConfig.detailStyle)
        setPropertyIfNotNull(props, 'autoTexts', classConfig.autoTexts)
        setPropertyIfNotNull(props, 'isUsingImagesToAlign', classConfig.isUsingImagesToAlign)
        config.merge(new ConfigSlurper(GrailsUtil.environment).parse(props))
        return config
    }

    def getStyle(def styleConfig) {
        def style = new Style()
        style.font = styleConfig.font
        if (styleConfig.border) {
            style.border = styleConfig.border
        } else {
            style.borderTop = styleConfig.borderTop
            style.borderBottom = styleConfig.borderBottom
            style.borderLeft = styleConfig.borderLeft
            style.borderRight = styleConfig.borderRight
        }
        style.backgroundColor = styleConfig.backgroundColor
        style.transparency = styleConfig.transparency
        //style.transparent = styleConfig.transparent
        style.textColor = styleConfig.textColor
        style.horizontalAlign = styleConfig.horizontalAlign
        style.verticalAlign = styleConfig.verticalAlign
        style.blankWhenNull = styleConfig.blankWhenNull
        style.borderColor = styleConfig.borderColor
        if (style.padding) {
            style.padding = styleConfig.padding
        } else {
            style.paddingTop = styleConfig.paddingTop
            style.paddingBotton = styleConfig.paddingBotton
            style.paddingLeft = styleConfig.paddingLeft
            style.paddingRight = styleConfig.paddingRight
        }
        //style.pattern = styleConfig.pattern
        style.radius = styleConfig.radius
        style.rotation = styleConfig.rotation
        //FIXME typo in DJ API
        //style.streching = styleConfig.stretching
        //style.stretchWithOverflow = styleConfig.stretchWithOverflow
        style
    }
}
