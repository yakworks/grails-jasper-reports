/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.jasper.spring

import javax.sql.DataSource

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.web.servlet.View
import org.springframework.web.servlet.view.AbstractUrlBasedView

import grails.plugin.viewtools.LoaderUrlBasedViewResolver
import net.sf.jasperreports.engine.JasperReport

/**
 * Uses Springs ViewResolver design concepts. The primary lookup uses {@link grails.plugin.viewtools.ViewResourceLocator}
 * The main DispatcherServlet spins through and calls the ViewResolvers ViewResolver.resolveViewName(String viewName, Locale locale)
 * The inheritance chain here is FreeMarkerViewResolver -> AbstractTemplateViewResolver -> UrlBasedViewResolver -> AbstractCachingViewResolver
 * AbstractCachingViewResolver holds the resolveViewName() which calls createView() then loadView() and buildView()
 *
 * This uses the {@link grails.plugin.viewtools.ViewResourceLocator} to locate the resource
 *
 * This gets used simply by registering it as a spring bean
 *   jasperViewResolver(JasperViewResolver){*      viewResourceLocator = ref("viewResourceLocator")
 *      jdbcDataSource = ref("dataSource")
 *      reportDataKey = "datasource"
 *      viewNames = ["*.jasper","*.jrxml"] as String[]
 *      viewClass = JasperReportsMultiFormatView.class
 *      order = 20
 *}*
 * @author Joshua Burnett
 */
@Slf4j
@CompileStatic
public class JasperViewResolver extends LoaderUrlBasedViewResolver {
    //injected autowired
    DataSource dataSource

    //props
    String reportDataKey = "data"
    String formatKey = "format"

    Properties subReportUrls

    String[] subReportDataKeys

    Properties headers

    //Map<String, Object> exporterParameters = new HashMap<String, Object>()

    /**
     * Requires the view class to be a subclass of {@link AbstractJasperReportsView}.
     */
    @Override
    protected Class<?> requiredViewClass() {
        return AbstractJasperReportsView
    }

    @Override
    //Overriden for logging
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        log.debug("resolveViewName with $viewName")

        return super.resolveViewName(viewName, locale)
    }

    /**
     * get a View that is built and initializeBean ran. Has the report that is ready for fill.
     * Does not cache it as there is no key although we could at some point in future
     * @param jr
     * @return
     */
    public AbstractJasperReportsView getView(JasperReport jr) {
        AbstractJasperReportsView view = (AbstractJasperReportsView) buildView("JasperReport_Populated_JasperView")
        view.url = null
        view = (AbstractJasperReportsView) applicationContext.autowireCapableBeanFactory.initializeBean(view, "JasperReport_Populated_JasperView")
        //order is important here as the initializeBean calls initApplicationContext which nulls the report out
        view.report = jr
        view
    }

    @CompileDynamic
    @Override
    @SuppressWarnings(['UnnecessaryObjectReferences'])
    protected AbstractUrlBasedView buildView(String viewName) throws Exception {
        AbstractJasperReportsView view = (AbstractJasperReportsView) super.buildView(viewName)
        view.reportDataKey = reportDataKey
        view.subReportUrls = subReportUrls
        view.subReportDataKeys = subReportDataKeys
        view.headers = headers
        //view.setExporterParameters(this.exporterParameters)
        view.jdbcDataSource = dataSource
        view.formatKey = formatKey
        return view
    }

}
