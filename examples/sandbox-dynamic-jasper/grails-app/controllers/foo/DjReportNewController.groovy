package foo
import org.apache.commons.lang.StringUtils
import org.grails.core.artefact.DomainClassArtefactHandler

import ar.com.fdvs.dj.core.DynamicJasperHelper
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager
import ar.com.fdvs.dj.domain.ColumnProperty
import ar.com.fdvs.dj.domain.DynamicReport
import ar.com.fdvs.dj.domain.DynamicReportOptions
import ar.com.fdvs.dj.domain.Style
import ar.com.fdvs.dj.domain.builders.GroupBuilder
import ar.com.fdvs.dj.domain.constants.HorizontalAlign
import ar.com.fdvs.dj.domain.entities.DJGroup
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn
import ar.com.fdvs.dj.domain.entities.columns.ExpressionColumn
import ar.com.fdvs.dj.domain.entities.columns.PropertyColumn
import ar.com.fdvs.dj.domain.entities.columns.SimpleColumn
import ar.com.fdvs.dj.output.ReportWriter
import ar.com.fdvs.dj.output.ReportWriterFactory
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.plugins.GrailsPluginManager
import grails.util.GrailsUtil
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter

class DjReportNewController {
    GrailsPluginManager pluginManager
    GrailsApplication grailsApplication

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
        columns: ['customer', 'product', 'amount'],
//        groupBuilders:[
//            []
//        ]
        groupColumns: ['customer'],
        groupFooterColumns: ['amount']
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
        DynamicReport report = new DynamicReport()
        report.options = new DynamicReportOptions()
        report.options.useFullPageWidth = config.useFullPageWidth
        report.options.page = config.page
        report.title = config.title ?: "${domainClass?.naturalName} Report"
        setPropertyIfNotNull(report, 'titleStyle', getStyle(config.titleStyle))
        setPropertyIfNotNull(report, 'subtitleStyle', getStyle(config.subtitleStyle))
        setPropertyIfNotNull(report.options, 'titleHeight', config.titleHeight)
        setPropertyIfNotNull(report, 'subtitle', config.subtitle)
        setPropertyIfNotNull(report, 'autoTexts', config.autoTexts)
        setPropertyIfNotNull(report.options, 'subtitleHeight', config.subtitleHeight)
        setPropertyIfNotNull(report.options, 'detailHeight', config.detailHeight)
        setPropertyIfNotNull(report.options, 'useFullPageWidth', config.useFullPageWidth)
        report.options.defaultDetailStyle = getStyle(config.detailStyle)
        report.options.defaultHeaderStyle = getStyle(config.headerStyle)

        List columnNames = config.columns ?: getPropertyValue(domainClass.clazz, 'reportColumns') ?: domainClass.properties.name - ['id', 'version']

        Map columns = addColumns(config, domainClass, report, columnNames)

        List groupColumns = config.groupColumns
        if (groupColumns) {
            groupColumns.each {String groupColumn ->
                GroupBuilder gb1 = new GroupBuilder();
                DJGroup group = gb1.setCriteriaColumn((PropertyColumn) columns[(groupColumn)]).build()
                //    .addFooterVariable(columnAmount,DJCalculation.SUM)
//                group.columnToGroupBy = columns[(groupColumn)]
//                println "groupbyColum ${columns[(groupColumn)]}"
//                config.groupFooterColumns.eachWithIndex { groupFooterColumn, index ->
//                    new DJGroupVariable(columns[(groupFooterColumn)],
//
//                    group.addFooterVariable( , getPropertyValue(DJCalculation, config.groupOperations[index]) )
//                }
//                report.columnsGroups << group
                report.columnsGroups << group
            }
        }

        def items
        if (config.dataSource) {
            items = config.dataSource.call(session, params)
        } else if (groupColumns) {
            items = domainClass.clazz.findAll("from $domainClass.clazz.name as s order by ${groupColumns.join(',')}")
        } else {
            items = domainClass.clazz.list()
        }

        JRDataSource dataSource = new JRBeanCollectionDataSource(items)
        JasperPrint print = DynamicJasperHelper.generateJasperPrint(report, new ClassicLayoutManager(), dataSource)
        def reportFileName = config.fileName
        def reportFormat = params.reportFormat ?: 'HTML'
        ReportWriter reportWriter = ReportWriterFactory.getInstance().getReportWriter(print, reportFormat, [(JRHtmlExporterParameter.IMAGES_URI): "${request.contextPath}/djReport/image?image=".toString(), (JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN): config.isUsingImagesToAlign])
        if (reportFileName) {
            response.addHeader('content-disposition', "attachment; filename=${reportFileName}.${reportFormat.toLowerCase()}")
        }
        reportWriter.writeTo(response)

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

    Map<String,AbstractColumn> addColumns(def config, def domainClass, def report, def columnNames) {
        Map columns = [:]
        columnNames.each { propertyName ->
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
                    report.fields << new ColumnProperty(propertyName, property.type.name)
                    column = new ExpressionColumn()
                    column.expression = new ToStringCustomExpression(propertyName)
                    propertyType = String
            }
            column.columnProperty = new ColumnProperty(propertyName, propertyType.name)
            column.title = config.columnTitles?."${propertyName}" ?: property.naturalName
            def style = getStyle(config.detailStyle)
            if (Number.isAssignableFrom(propertyType) || Date.isAssignableFrom(propertyType)) {
                style.horizontalAlign = HorizontalAlign.RIGHT
            } else {
                style = getStyle(config.detailStyle)
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
            report.columns << column
            columns[(propertyName)] = column
        }
        return columns
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
