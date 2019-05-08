/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.jasper

import groovy.util.logging.Slf4j

import grails.plugins.Plugin
import yakworks.jasper.spring.JasperView
import yakworks.jasper.spring.JasperViewResolver

@Slf4j
class JasperReportsGrailsPlugin extends Plugin {

    def profiles = ['web']

    def title = "Jasper Reports Plugin"
    def description = 'Jasper Reports Plugin'
    def documentation = "https://github.com/yakwroks/grails-jasper-reports"
    def observe = ["controllers", 'groovyPages', 'viewTools']
    def loadAfter = ['controllers', 'groovyPages', 'viewTools']

    def author = "Joshua Burnett"
    def authorEmail = "joshdev@9ci.com"

    def license = "APACHE"

    def pluginExcludes = [
            "grails-app/views/**/*",
            "grails-app/controllers/**/*",
            "grails-app/services/grails/plugin/freemarker/test/**/*",
            "src/main/groovy/grails/plugin/freemarker/test/**/*",
            "src/docs/**/*",
            "grails-app/i18n/*",
            'grails-app/taglib/**/test/**/*',
            'scripts/**/Eclipse.groovy',
            "test-plugins/**/*",
            "web-app/**/*"
    ]

    Closure doWithSpring() {
        { ->
            //def jconfig = application.mergedConfig.asMap().reporting.jasper

            log.info "initializing jasper reports plugin"
            jasperViewResourceLocator(grails.plugin.viewtools.ViewResourceLocator) { bean ->
                searchBinaryPlugins = true //whether to look in binary plugins, does not work in grails2

                //initial searchLocations
                searchPaths = []//jconfig.viewResourceLocator.searchPaths

                //resourceLoaders beans to use right after searchLocations above are scanned
                //searchLoaders = [ref('tenantViewResourceLoader')]

                // in dev mode there will be a groovyPageResourceLoader with base dir set to the running project
                //if(Environment.isDevelopmentEnvironmentAvailable()) <- better for Grails 3
                if (!application.warDeployed) { // <- grails2
                    grailsViewPaths = ["/grails-app/views"]
                    webInfPrefix = ""
                }

            }

            jasperViewResolver(JasperViewResolver) {
                viewResourceLoader = ref("jasperViewResourceLocator")
                dataSource = ref("dataSource")
                reportDataKey = "data"
                viewNames = ["*.jasper", "*.jrxml"] as String[]
                viewClass = JasperView
                order = 10
                //don't cache in dev mode
                if (!application.warDeployed) { // <- grails2
                    cache = false
                }
            }

        }
    }
}
