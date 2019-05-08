/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.jasper

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.View

import grails.core.GrailsApplication
import yakworks.jasper.spring.JasperView
import yakworks.jasper.spring.JasperViewResolver

/**
 * Retrieves and processes view report for jasper tempalates.
 *
 * @author Joshua Burnett
 */
@CompileStatic
@Slf4j
class JasperViewService {

    static transactional = false

    GrailsApplication grailsApplication
    LocaleResolver localeResolver
    JasperViewResolver jasperViewResolver

    View getView(String viewName, Locale locale = null) {
        locale = locale ?: getLocale()
        //GrailsWebEnvironment.bindRequestIfNull(grailsApplication.mainContext)
        return jasperViewResolver.resolveViewName(viewName, locale)
    }

    /**
     * Calls getView to grab the jasper template and and then passes to render(view,model...)
     */
    Writer render(String viewName, Map model, Writer writer = new CharArrayWriter()) {
        //GrailsWebEnvironment.bindRequestIfNull(grailsApplication.mainContext, writer) -- xxx why do we need this ?
        JasperView view = (JasperView) jasperViewResolver.resolveViewName(viewName, getLocale())
        if (!view) {
            throw new IllegalArgumentException("The ftl view [${viewName}] could not be found")
        }
        render(view, model, writer)
    }

    /**
     * processes the jasper report template in the View.
     * sets the plugin thread local if passed in and bind a request if none exists before processing.
     *
     * @param view JasperView that holds the template
     * @param model the hash model the should be passed into the freemarker tempalate
     * @param writer (optional) a writer if you have one. a CharArrayWriter will be created by default.
     * @return the writer that was passed in.
     */
    Writer render(JasperView view, Map model, Writer writer = new CharArrayWriter()) {

        if (!view) {
            throw new IllegalArgumentException("The 'view' argument cannot be null")
        }
        log.debug("primary render called with view : $view ")
        // Consolidate static and dynamic model attributes.
        Map attributesMap = view.attributesMap
        int mapSize = attributesMap.size() + (model != null ? model.size() : 0)
        Map mergedModel = new HashMap(mapSize)
        mergedModel.putAll(attributesMap)
        if (model) mergedModel.putAll(model)
        ///GrailsWebEnvironment.bindRequestIfNull(grailsApplication.mainContext, writer) XXX why do we need this ?
        //view render
        return writer

    }

    /**
     * returns the local by using the localResolver and the webrequest from RequestContextHolder.getRequestAttributes()
     */

    Locale getLocale() {
        def locale
        def request = GrailsWebRequest.lookup()?.currentRequest
        locale = localeResolver?.resolveLocale(request)
        if (locale == null) {
            locale = Locale.default
        }
        return locale
    }
}
