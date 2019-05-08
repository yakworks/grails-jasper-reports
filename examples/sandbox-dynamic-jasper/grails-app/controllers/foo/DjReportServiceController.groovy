package foo

import org.grails.core.artefact.DomainClassArtefactHandler

import ar.com.fdvs.dj.core.DynamicJasperHelper
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager
import ar.com.fdvs.dj.domain.DynamicReport
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.plugins.GrailsPluginManager
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.JasperReport
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
import yakworks.jasper.DynamicJasperService
import yakworks.jasper.spring.JasperView
import yakworks.jasper.spring.JasperViewResolver

class DjReportServiceController {
    GrailsPluginManager pluginManager
    GrailsApplication grailsApplication
    JasperViewResolver jasperViewResolver
    DynamicJasperService dynamicJasperService

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

    def sample(){
        def rptConfig =[
            fields: ['customer.name','product.group.name', 'product.name', 'qty', 'amount'],
            columnTitles:['customer.name':'Customer','product.group.name':'Group', 'product.name':'Product', 'qty':'Qty', 'amount':'Amount'],
            groupFields: ['customer.name','product.group.name'],
            groupVariables: [amount:"sum"]
        ]
    }

    def rptConfig =[
//            group1:[
//                column:[]
//            ],
//            group1:[
//                    columns
//            ]
        fields: ['customer.name','product.group.name', 'fooGroup', 'product.name', 'qty', 'amount'],
        columnTitles:['customer.name':'Customer','product.group.name':'Group', 'product.name':'Product', 'qty':'Qty', 'amount':'Amount'],
//        groupBuilders:[
//            customer:[
//                amount:"sum",
//                id:"count"
//            ]
//        ]
        groupFields: ['customer.name','product.group.name','fooGroup'],
        groupVariables: [amount:"sum",qty:"sum"]//,id:"count"]
        //groupOperations: ['SUM']
            //,
            //autoTexts: [new AutoText(AutoText.AUTOTEXT_PAGE_X_OF_Y, AutoText.POSITION_FOOTER, HorizontalBandAlignment.buildAligment(AutoText.ALIGMENT_CENTER), (byte)0, 200, 200)]

    ]

    def entityReport() {
        println "entityReport with $params"
        GrailsClass domainClass = grailsApplication.getArtefactByLogicalPropertyName(DomainClassArtefactHandler.TYPE, params.entity)
        assert domainClass
        def config = dynamicJasperService.loadConfig()
        config = config.merge(rptConfig as ConfigObject)
        doReport(domainClass, config, params, request, response)
    }

    def doReport(def domainClass, def config, def params, def request, def response) {
        DynamicReport dr = dynamicJasperService.buildDynamicReport( domainClass,  config,  params)

        List groupFields = config.groupFields

        def items
        if (config.dataSource) {
            items = config.dataSource.call(session, params)
        } else if (config.groupFields) {
            items = domainClass.clazz.findAll("from $domainClass.clazz.name as s order by ${config.groupFields.join(',')}")
        } else {
            items = domainClass.clazz.list()
        }

        JasperReport jr = DynamicJasperHelper.generateJasperReport(dr, new ClassicLayoutManager(), params)
        JasperView view = (JasperView) jasperViewResolver.getView(jr)

        JRDataSource ds = new JRBeanCollectionDataSource(items)
        Map model = [data:ds]
        view.render(model,request,response)
    }


//    def loadAndMergeConfig(def domainClass, def classConfig, def params) {
//        def config = loadConfig()
//        Properties props = new Properties()
//        setPropertyIfNotNull(props, 'entity', params.entity)
//        setPropertyIfNotNull(props, 'title', classConfig.title)
//        setPropertyIfNotNull(props, 'columns', params.reportColumns?.split(',') ?: classConfig.columns ?: domainClass.properties.name - ['id', 'version'])
//        setPropertyIfNotNull(props, 'patterns', classConfig.patterns)
//        setPropertyIfNotNull(props, 'columnTitles', classConfig.columnTitles)
//        setPropertyIfNotNull(props, 'groupColumns', params.groupColumns?.split(',') ?: classConfig.groupColumns)
//        setPropertyIfNotNull(props, 'groupFooterColumns', params.groupFooterColumns?.split(',') ?: classConfig.groupFooterColumns)
//        setPropertyIfNotNull(props, 'groupOperations', params.groupOperations?.split(',') ?: classConfig.groupOperations ?: ('SUM,' * (props.groupFooterColumns?.size() ?: 0)).split(','))
//        setPropertyIfNotNull(props, 'dataSource', classConfig.dataSource)
//        setPropertyIfNotNull(props, 'fileName', classConfig.fileName)
//        setPropertyIfNotNull(props, 'useFullPageWidth', classConfig.useFullPageWidth)
//        setPropertyIfNotNull(props, 'page', classConfig.page)
//        setPropertyIfNotNull(props, 'intPattern', classConfig.intPattern)
//        setPropertyIfNotNull(props, 'floatPattern', classConfig.floatPattern)
//        setPropertyIfNotNull(props, 'datePattern', classConfig.datePattern)
//        setPropertyIfNotNull(props, 'titleStyle', classConfig.titleStyle)
//        setPropertyIfNotNull(props, 'subtitleStyle', classConfig.subtitleStyle)
//        setPropertyIfNotNull(props, 'headerStyle', classConfig.headerStyle)
//        setPropertyIfNotNull(props, 'detailStyle', classConfig.detailStyle)
//        setPropertyIfNotNull(props, 'autoTexts', classConfig.autoTexts)
//        setPropertyIfNotNull(props, 'isUsingImagesToAlign', classConfig.isUsingImagesToAlign)
//        config.merge(new ConfigSlurper(GrailsUtil.environment).parse(props))
//        return config
//    }

//    def getStyle(def styleConfig) {
//        def style = new Style()
//        style.font = styleConfig.font
//        if (styleConfig.border) {
//            style.border = styleConfig.border
//        } else {
//            style.borderTop = styleConfig.borderTop
//            style.borderBottom = styleConfig.borderBottom
//            style.borderLeft = styleConfig.borderLeft
//            style.borderRight = styleConfig.borderRight
//        }
//        style.backgroundColor = styleConfig.backgroundColor
//        style.transparency = styleConfig.transparency
//        //style.transparent = styleConfig.transparent
//        style.textColor = styleConfig.textColor
//        style.horizontalAlign = styleConfig.horizontalAlign
//        style.verticalAlign = styleConfig.verticalAlign
//        style.blankWhenNull = styleConfig.blankWhenNull
//        style.borderColor = styleConfig.borderColor
//        if (style.padding) {
//            style.padding = styleConfig.padding
//        } else {
//            style.paddingTop = styleConfig.paddingTop
//            style.paddingBotton = styleConfig.paddingBotton
//            style.paddingLeft = styleConfig.paddingLeft
//            style.paddingRight = styleConfig.paddingRight
//        }
//        //style.pattern = styleConfig.pattern
//        style.radius = styleConfig.radius
//        style.rotation = styleConfig.rotation
//        //FIXME typo in DJ API
//        //style.streching = styleConfig.stretching
//        //style.stretchWithOverflow = styleConfig.stretchWithOverflow
//        style
//    }
}
