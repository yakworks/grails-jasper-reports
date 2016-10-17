// configuration for plugin testing - will not be included in the plugin zip

log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}
    root { info() }

    appenders {
        console name:'stdout', layout:pattern(conversionPattern: '%d{HH:mm:ss,SSS} %-5p %c{3} %x - %m%n')
        //console name:'stdout', layout:pattern(conversionPattern: '%d{HH:mm:ss,SSS} [%t] %-5p %c %x - %m%n')
    }
    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'
    debug   'foo.start',
            'grails.app.services.grails.jasper',
//            'grails.jasper',
//            'grails.plugin.viewtools',
//            'org.codehaus.groovy.grails.web',
//            'org.springframework.ui.freemarker.SpringTemplateLoader',
            'foo.end'

}
