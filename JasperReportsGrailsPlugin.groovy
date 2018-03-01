import nine.jasper.spring.JasperView
import nine.jasper.spring.JasperViewResolver

class JasperReportsGrailsPlugin {
    // the plugin version
    def version        = "0.4-SNAPSHOT"
    def grailsVersion  = "2.3 > *"
    def pluginExcludes = [
            "grails-app/views/**/*",
            "grails-app/controllers/**/*",
            "grails-app/services/grails/plugin/freemarker/test/**/*",
            "src/groovy/grails/plugin/freemarker/test/**/*",
            "src/docs/**/*",
            "grails-app/i18n/*",
            'grails-app/taglib/**/test/**/*',
            'scripts/**/Eclipse.groovy',
            "test-plugins/**/*",
            "web-app/**/*"
    ]

    def title           = "Jasper Reports Plugin"
    def description     = 'The Grails FreeMarker plugin provides support for rendering FreeMarker templates as views.'
    def documentation   = "https://github.com/9ci/grails-jasper-reports"
    def license         = "APACHE"

    def scm             = [ url: "https://github.com/9ci/grails-jasper-reports" ]
    def issueManagement = [ system: "GITHUB", url: "https://github.com/9ci/grails-jasper-reports" ]

    def observe = ["controllers", 'groovyPages','viewTools']
    def loadAfter = ['controllers', 'groovyPages','pluginConfig','viewTools']


    def author = "Joshua Burnett"
    def authorEmail = "joshdev@9ci.com"


    def doWithSpring = {
        //def jconfig = application.mergedConfig.asMap().reporting.jasper

        println "initializing jasper reports plugin"
        jasperViewResourceLocator(grails.plugin.viewtools.ViewResourceLocator) { bean ->
            searchBinaryPlugins = false //whether to look in binary plugins, does not work in grails2

            //initial searchLocations
            searchPaths = []//jconfig.viewResourceLocator.searchPaths

            //resourceLoaders beans to use right after searchLocations above are scanned
            //searchLoaders = [ref('tenantViewResourceLoader')]

            // in dev mode there will be a groovyPageResourceLoader with base dir set to the running project
            //if(Environment.isDevelopmentEnvironmentAvailable()) <- better for Grails 3
            if(!application.warDeployed){ // <- grails2
                grailsViewPaths = ["/grails-app/views"]
                webInfPrefix = ""
            }

        }

        jasperViewResolver(JasperViewResolver) {
            viewResourceLoader = ref("jasperViewResourceLocator")
            dataSource = ref("dataSource")
            reportDataKey = "data"
            viewNames = ["*.jasper","*.jrxml"] as String[]
            viewClass = JasperView.class
            order = 10
            //don't cache in dev mode
            if(!application.warDeployed){ // <- grails2
                cache = false
            }
        }


    }
}
