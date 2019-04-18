/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nine.jasper.spring

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import net.sf.jasperreports.engine.*
import nine.jasper.JRDataSourceJDBC
import nine.jasper.JasperUtils
import org.springframework.context.ApplicationContextException
import org.springframework.context.support.MessageSourceResourceBundle
import org.springframework.core.io.Resource
import org.springframework.ui.jasperreports.JasperReportsUtils
import org.springframework.util.CollectionUtils
import org.springframework.web.servlet.support.RequestContext
import org.springframework.web.servlet.view.AbstractUrlBasedView

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.sql.DataSource

/**
 * Base class for all JasperReports views. Applies on-the-fly compilation
 * of report designs as required and coordinates the rendering process.
 * The resource path of the main report needs to be specified as {@code url}.
 *
 * <p>This class is responsible for getting report data from the model that has
 * been provided to the view. The default implementation checks for a model object
 * under the specified {@code reportDataKey} first, then falls back to looking
 * for a value of type {@code JRDataSource}, {@code java.util.Collection},
 * object array (in that order).
 *
 * <p>If no {@code JRDataSource} can be found in the model, then reports will
 * be filled using the configured {@code javax.sql.DataSource} if any. If neither
 * a {@code JRDataSource} or {@code javax.sql.DataSource} is available then
 * an {@code IllegalArgumentException} is raised.
 *
 * <p>Provides support for sub-reports through the {@code subReportUrls} and
 * {@code subReportDataKeys} properties.
 *
 * <p>When using sub-reports, the master report should be configured using the
 * {@code url} property and the sub-reports files should be configured using
 * the {@code subReportUrls} property. Each entry in the {@code subReportUrls}
 * Map corresponds to an individual sub-report. The key of an entry must match up
 * to a sub-report parameter in your report file of type
 * {@code net.sf.jasperreports.engine.JasperReport},
 * and the value of an entry must be the URL for the sub-report file.
 *
 * <p>For sub-reports that require an instance of {@code JRDataSource}, that is,
 * they don't have a hard-coded query for data retrieval, you can include the
 * appropriate data in your model as would with the data source for the parent report.
 * However, you must provide a List of parameter names that need to be converted to
 * {@code JRDataSource} instances for the sub-report via the
 * {@code subReportDataKeys} property. When using {@code JRDataSource}
 * instances for sub-reports, you <i>must</i> specify a value for the
 * {@code reportDataKey} property, indicating the data to use for the main report.

 * <p>Response headers can be controlled via the {@code headers} property. Spring
 * will attempt to set the correct value for the {@code Content-Diposition} header
 * so that reports render correctly in Internet Explorer. However, you can override this
 * setting through the {@code headers} property.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * chnages for new Jasper 6 and remove old deperecated classes
 * @author Joshua Burnett
 * @since 1.1.3
 * @see #setUrl
 * @see #setReportDataKey
 * @see #setSubReportUrls
 * @see #setSubReportDataKeys
 * @see #setHeaders
 * @see #setJdbcDataSource
 */

@Slf4j
@CompileStatic
public abstract class AbstractJasperReportsView extends AbstractUrlBasedView {

    /**
     * Constant that defines "Content-Disposition" header.
     */
    static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition"

    /**
     * The default Content-Disposition header. Used to make IE play nice.
     */
    static final String CONTENT_DISPOSITION_INLINE = "inline"

    /**
     * A String key used to lookup the {@code JRDataSource} in the model.
     */
    String reportDataKey = "data"

    /**
     * Stores the paths to any sub-report files used by this top-level report,
     * along with the keys they are mapped to in the top-level report file.
     */
    Properties subReportUrls

    /**
     * Stores the names of any data source objects that need to be converted to
     * {@code JRDataSource} instances and included in the report parameters
     * to be passed on to a sub-report.
     */
    String[] subReportDataKeys

    /**
     * Stores the headers to written with each response
     */
    Properties headers

    /**
     * Stores the {@code DataSource}, if any, used as the report data source.
     */
    DataSource jdbcDataSource

    /**
     * The {@code JasperReport} that is used to render the view.
     */
    JasperReport report

    /**
     * Holds mappings between sub-report keys and {@code JasperReport} objects.
     */
    private Map<String, JasperReport> subReports

    /**
     * Set the name of the model attribute that represents the report data.
     * If not specified, the model map will be searched for a matching value type.
     * <p>A {@code JRDataSource} will be taken as-is. For other types, conversion
     * will apply: By default, a {@code java.util.Collection} will be converted
     * to {@code JRBeanCollectionDataSource}, and an object array to
     * {@code JRBeanArrayDataSource}.
     * <p><b>Note:</b> If you pass in a Collection or object array in the model map
     * for use as plain report parameter, rather than as report data to extract fields
     * from, you need to specify the key for the actual report data to use, to avoid
     * mis-detection of report data by type.
     * @see net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
     * @see net.sf.jasperreports.engine.data.JRBeanArrayDataSource
     */
    public void setReportDataKey(String reportDataKey) {
        this.reportDataKey = reportDataKey
    }

    /**
     * Specify resource paths which must be loaded as instances of
     * {@code JasperReport} and passed to the JasperReports engine for
     * rendering as sub-reports, under the same keys as in this mapping.
     * @param subReports mapping between model keys and resource paths
     * (Spring resource locations)
     * @see #setUrl
     * @see org.springframework.context.ApplicationContext#getResource
     */
    public void setSubReportUrls(Properties subReports) {
        this.subReportUrls = subReports
    }

    /**
     * Set the list of names corresponding to the model parameters that will contain
     * data source objects for use in sub-reports. Spring will convert these objects
     * to instances of {@code JRDataSource} where applicable and will then
     * include the resulting {@code JRDataSource} in the parameters passed into
     * the JasperReports engine.
     * <p>The name specified in the list should correspond to an attribute in the
     * model Map, and to a sub-report data source parameter in your report file.
     * If you pass in {@code JRDataSource} objects as model attributes,
     * specifying this list of keys is not required.
     * <p>If you specify a list of sub-report data keys, it is required to also
     * specify a {@code reportDataKey} for the main report, to avoid confusion
     * between the data source objects for the various reports involved.
     * @param subReportDataKeys list of names for sub-report data source objects
     * @see #setReportDataKey
     * @see JRDataSource
     * @see net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
     * @see net.sf.jasperreports.engine.data.JRBeanArrayDataSource
     */
    public void setSubReportDataKeys(String... subReportDataKeys) {
        this.subReportDataKeys = subReportDataKeys
    }

    /**
     * JasperReports views do not strictly required a 'url' value.
     * Alternatively, the {@link #getReport()} template method may be overridden.
     */
    @Override
    protected boolean isUrlRequired() {
        return false
    }

    /**
     * Checks to see that a valid report file URL is supplied in the
     * configuration. Compiles the report file is necessary.
     * <p>Subclasses can add custom initialization logic by overriding
     * the {@link #onInit} method.
     */
    @Override
    protected final void initApplicationContext() throws ApplicationContextException {
        this.report = loadReport()

        // Load sub reports if required, and check data source parameters.
        if (subReportUrls) {
            if (this.subReportDataKeys != null && this.subReportDataKeys.length > 0 && this.reportDataKey == null) {
                throw new ApplicationContextException(
                        "'reportDataKey' for main report is required when specifying a value for 'subReportDataKeys'")
            }
            this.subReports = new HashMap<String, JasperReport>(this.subReportUrls.size())
            //FIXME replace this with viewResourceLocator
            for (Enumeration<?> urls = this.subReportUrls.propertyNames() ; urls.hasMoreElements();) {
                String key = (String) urls.nextElement()
                String path = this.subReportUrls.getProperty(key)
                Resource resource = getApplicationContext().getResource(path)

                this.subReports.put(key, JasperUtils.loadReport(resource))
            }
        }

        if (this.headers == null) {
            this.headers = new Properties()
        }
        if (!this.headers.containsKey(HEADER_CONTENT_DISPOSITION)) {
            this.headers.setProperty(HEADER_CONTENT_DISPOSITION, CONTENT_DISPOSITION_INLINE)
        }

        onInit()
    }

    /**
     * Subclasses can override this to add some custom initialization logic. Called
     * by {@link #initApplicationContext()} as soon as all standard initialization logic
     * has finished executing.
     * @see #initApplicationContext()
     */
    @SuppressWarnings(['EmptyMethodInAbstractClass'])
    protected void onInit() {
    }

    /**
     * Load the main {@code JasperReport} from the specified {@code Resource}.
     * If the {@code Resource} points to an uncompiled report design file then the
     * report file is compiled dynamically and loaded into memory.
     * @return a {@code JasperReport} instance, or {@code null} if no main
     * report has been statically defined
     */
    protected JasperReport loadReport() {
        String url = getUrl()
        if (url == null) {
            return null
        }
        //FIXME replace this with the viewResourceLoader
        //it should have the full aboslute URL at this point in the game
        Resource mainReport = getApplicationContext().getResource(url)
        return JasperUtils.loadReport(mainReport)
    }

    /**
     * Finds the report data to use for rendering the report and then invokes the
     * {@link #renderReport} method that should be implemented by the subclass.
     * @param model the model map, as passed in for view rendering. Must contain
     * a report data value that can be converted to a {@code JRDataSource},
     * according to the rules of the {@link #fillReport} method.
     */
    @Override
    protected void renderMergedOutputModel(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        exposeSubReports(model)

        // Expose Spring-managed Locale and MessageSource.
        exposeLocalizationContext(model, request)

        // Fill the report.
        JasperPrint filledReport = fillReport(model)
        postProcessReport(filledReport, model)

        // Prepare response and render report.
        populateHeaders(response)
        renderReport(filledReport, model, response)
    }

    public void exposeSubReports(Map<String, Object> model) {
        if (this.subReports != null) {
            // Expose sub-reports as model attributes.
            model.putAll(this.subReports)

            // Transform any collections etc into JRDataSources for sub reports.
            if (this.subReportDataKeys != null) {
                for (String key : this.subReportDataKeys) {
                    model.put(key, convertReportData(model.get(key)))
                }
            }
        }
    }

    /**
     * Expose current Spring-managed Locale and MessageSource to JasperReports i18n
     * ($R expressions etc). The MessageSource should only be exposed as JasperReports
     * resource bundle if no such bundle is defined in the report itself.
     * <p>The default implementation exposes the Spring RequestContext Locale and a
     * MessageSourceResourceBundle adapter for the Spring ApplicationContext,
     * analogous to the {@code JstlUtils.exposeLocalizationContext} method.
     * @see org.springframework.web.servlet.support.RequestContextUtils#getLocale
     * @see MessageSourceResourceBundle
     * @see #getApplicationContext()
     * @see JRParameter#REPORT_LOCALE
     * @see JRParameter#REPORT_RESOURCE_BUNDLE
     * @see org.springframework.web.servlet.support.JstlUtils#exposeLocalizationContext
     */
    protected void exposeLocalizationContext(Map<String, Object> model, HttpServletRequest request) {
        RequestContext rc = new RequestContext(request, getServletContext())
        Locale locale = rc.getLocale()
        if (!model.containsKey(JRParameter.REPORT_LOCALE)) {
            model.put(JRParameter.REPORT_LOCALE, locale)
        }
        TimeZone timeZone = rc.getTimeZone()
        if (timeZone != null && !model.containsKey(JRParameter.REPORT_TIME_ZONE)) {
            model.put(JRParameter.REPORT_TIME_ZONE, timeZone)
        }
        JasperReport report = getReport()
        if ((report == null || report.getResourceBundle() == null) && !model.containsKey(JRParameter.REPORT_RESOURCE_BUNDLE)) {
            model.put(JRParameter.REPORT_RESOURCE_BUNDLE, new MessageSourceResourceBundle(rc.getMessageSource(), locale))
        }
    }

    /**
     * Create a populated {@code JasperPrint} instance from the configured
     * {@code JasperReport} instance.
     * <p>By default, this method will use any {@code JRDataSource} instance
     * (or wrappable {@code Object}) that can be located using {@link #setReportDataKey},
     * a lookup for type {@code JRDataSource} in the model Map, or a special value
     * retrieved via {@link #getReportData}.
     * <p>If no {@code JRDataSource} can be found, this method will use a JDBC
     * {@code Connection} obtained from the configured {@code javax.sql.DataSource}
     * (or a DataSource attribute in the model). If no JDBC DataSource can be found
     * either, the JasperReports engine will be invoked with plain model Map,
     * assuming that the model contains parameters that identify the source
     * for report data (e.g. Hibernate or JPA queries).
     * @param model the model for this request
     * @throws IllegalArgumentException if no {@code JRDataSource} can be found
     * and no {@code javax.sql.DataSource} is supplied
     * @throws JRException if there is an error when populating the report using
     * a {@code JRDataSource}
     * @return the populated {@code JasperPrint} instance
     * @see #getReportData
     * @see #setJdbcDataSource
     */
    protected JasperPrint fillReport(Map<String, Object> model) throws Exception {
        // Determine main report.
        JasperReport report = getReport()
        if (report == null) {
            throw new IllegalStateException("No main report defined for 'fillReport' - " +
                    "specify a 'url' on this view or override 'getReport()' or 'fillReport(Map)'")
        }

        JRDataSource jrDataSource = JasperUtils.extractJasperDataSrouce(model, this.reportDataKey)
        DataSource jdbcDataSourceToUse = null

        if (!jrDataSource && this.jdbcDataSource) {
            jrDataSource = new JRDataSourceJDBC(jdbcDataSource)
        }

        if (jrDataSource && jrDataSource instanceof JRDataSourceJDBC) {
            return JasperUtils.fillReport(report, model, ((JRDataSourceJDBC) jrDataSource).dataSource)
        } else {
            // Determine JRDataSource for main report.
            if (jrDataSource == null) {
                //try grabbing it form the first collection in the model
                jrDataSource = getReportData(model)
            }
            if (jrDataSource) {
                // Use the JasperReports JRDataSource.
                if (logger.isDebugEnabled()) {
                    logger.debug("Filling report with JRDataSource [" + jrDataSource + "]")
                }
                return JasperFillManager.fillReport(report, model, jrDataSource)
            } else {
                // Assume that the model contains parameters that identify
                // the source for report data (e.g. Hibernate or JPA queries).
                logger.debug("Filling report with plain model")
                return JasperFillManager.fillReport(report, model)
            }
        }
    }

    /**
     * Populates the headers in the {@code HttpServletResponse} with the
     * headers supplied by the user.
     */
    private void populateHeaders(HttpServletResponse response) {
        // Apply the headers to the response.
        for (Enumeration<?> en = this.headers.propertyNames() ; en.hasMoreElements();) {
            String key = (String) en.nextElement()
            response.addHeader(key, this.headers.getProperty(key))
        }
    }

    /**
     * Determine the {@code JasperReport} to fill.
     * Called by {@link #fillReport}.
     * <p>The default implementation returns the report as statically configured
     * through the 'url' property (and loaded by {@link #loadReport()}).
     * Can be overridden in subclasses in order to dynamically obtain a
     * {@code JasperReport} instance. As an alternative, consider
     * overriding the {@link #fillReport} template method itself.
     * @return an instance of {@code JasperReport}
     */
    protected JasperReport getReport() {
        return this.report
    }

    /**
     * Create an appropriate {@code JRDataSource} for passed-in report data.
     * Called by {@link #fillReport} when its own lookup steps were not successful.
     * <p>The default implementation looks for a value of type {@code java.util.Collection}
     * or object array (in that order). Can be overridden in subclasses.
     * @param model the model map, as passed in for view rendering
     * @return the {@code JRDataSource} or {@code null} if the data source is not found
     * @see #getReportDataTypes
     * @see #convertReportData
     */
    protected JRDataSource getReportData(Map<String, Object> model) {
        // Try to find matching attribute, of given prioritized types.
        Object value = CollectionUtils.findValueOfType(model.values(), getReportDataTypes())
        return (value != null ? convertReportData(value) : null)
    }

    /**
     * Return the value types that can be converted to a {@code JRDataSource},
     * in prioritized order. Should only return types that the
     * {@link #convertReportData} method is actually able to convert.
     * <p>Default value types are: {@code java.util.Collection} and {@code Object} array.
     * @return the value types in prioritized order
     */
    protected Class<?>[] getReportDataTypes() {
        return [Collection.class, ([] as Object[]).class] as Class<?>[]
    }

    /**
     * Convert the given report data value to a {@code JRDataSource}.
     * <p>The default implementation delegates to {@code JasperReportUtils} unless
     * the report data value is an instance of {@code JRDataSourceProvider}.
     * A {@code JRDataSource}, {@code JRDataSourceProvider},
     * {@code java.util.Collection} or object array is detected.
     * {@code JRDataSource}s are returned as is, whilst {@code JRDataSourceProvider}s
     * are used to create an instance of {@code JRDataSource} which is then returned.
     * The latter two are converted to {@code JRBeanCollectionDataSource} or
     * {@code JRBeanArrayDataSource}, respectively.
     * @param value the report data value to convert
     * @return the JRDataSource
     * @throws IllegalArgumentException if the value could not be converted
     * @see JasperReportsUtils#convertReportData
     * @see JRDataSource
     * @see net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
     * @see net.sf.jasperreports.engine.data.JRBeanArrayDataSource
     */
    protected JRDataSource convertReportData(Object value) throws IllegalArgumentException {
        return JasperUtils.convertReportData(value)
    }

    /**
     * Template method to be overridden for custom post-processing of the
     * populated report. Invoked after filling but before rendering.
     * <p>The default implementation is empty.
     * @param populatedReport the populated {@code JasperPrint}
     * @param model the map containing report parameters
     * @throws Exception if post-processing failed
     */
    @SuppressWarnings(['EmptyMethodInAbstractClass'])
    protected void postProcessReport(JasperPrint populatedReport, Map<String, Object> model) throws Exception {
    }

    /**
     * Subclasses should implement this method to perform the actual rendering process.
     * <p>Note that the content type has not been set yet: Implementers should build
     * a content type String and set it via {@code response.setContentType}.
     * If necessary, this can include a charset clause for a specific encoding.
     * The latter will only be necessary for textual output onto a Writer, and only
     * in case of the encoding being specified in the JasperReports exporter parameters.
     * <p><b>WARNING:</b> Implementers should not use {@code response.setCharacterEncoding}
     * unless they are willing to depend on Servlet API 2.4 or higher. Prefer a
     * concatenated content type String with a charset clause instead.
     * @param populatedReport the populated {@code JasperPrint} to render
     * @param model the map containing report parameters
     * @param response the HTTP response the report should be rendered to
     * @throws Exception if rendering failed
     * @see javax.servlet.ServletResponse#setContentType
     * @see javax.servlet.ServletResponse#setCharacterEncoding
     */
    protected abstract void renderReport(
            JasperPrint populatedReport, Map<String, Object> model, HttpServletResponse response)
            throws Exception

}
