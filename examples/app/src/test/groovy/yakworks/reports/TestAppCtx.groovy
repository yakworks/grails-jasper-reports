package yakworks.reports

import yakworks.jasper.spring.JasperView
import yakworks.jasper.spring.JasperViewResolver
import org.grails.gsp.GroovyPageResourceLoader
import org.springframework.core.io.UrlResource

/**
 * Created by basejump on 10/14/16.
 */
class TestAppCtx {
    static Closure getDoWithSpring() {
        def baseRes =  new UrlResource("file:.")// as Resource
        GroovyPageResourceLoader srl = new GroovyPageResourceLoader(baseResource:baseRes)
        return {
            jasperViewResourceLocator(grails.plugin.viewtools.ViewResourceLocator) { bean ->
                searchBinaryPlugins = false //whether to look in binary plugins, does not work in grails2
                grailsViewPaths = ["/grails-app/views"]
                webInfPrefix = ""
                resourceLoader = srl
            }

            jasperViewResolver(JasperViewResolver) {
                //viewLocator = ref("viewLocator")
                viewResourceLoader = ref("jasperViewResourceLocator")
                //dataSource = ref("dataSource")
                reportDataKey = "data"
                viewNames = ["*.jasper", "*.jrxml"] as String[]
                viewClass = JasperView.class
                order = 10
                cache = false
            }
        }
    }
}
