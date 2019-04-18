/*
 * Copyright 2002-2015 the original author or authors.
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
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Slf4j
import net.sf.jasperreports.engine.JRParameter
import nine.reports.ReportFormat
import net.sf.jasperreports.engine.JRAbstractExporter
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.export.Exporter
import net.sf.jasperreports.export.WriterExporterOutput
import nine.jasper.JasperUtils
import org.springframework.web.util.WebUtils

import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
/**
 * JasperReports view class that allows for the actual rendering format
 * to be specified at runtime using a parameter contained in the model.
 */
@Slf4j
@CompileStatic
public class JasperView extends AbstractJasperReportsView {

    /**
     * The key of the model parameter that holds the format key.
     */
    String formatKey = "format"

    /**
     * the passed in report format from formatKey in model
     */
    //ReportFormat format

    /**
     * Stores the mappings of mapping keys to Content-Disposition header values.
     */
    Properties contentDispositionMappings

    @Override
    public String getContentType() {
        throw new MissingFieldException("Use model and ReportFormat, contentType no longer valid ", "contentType", this.class)
    }

    /**
     * Set the mappings of {@code Content-Disposition} header values to
     * mapping keys. If specified, Spring will look at these mappings to determine
     * the value of the {@code Content-Disposition} header for a given
     * format mapping.
     */
    public void setContentDispositionMappings(Properties mappings) {
        this.contentDispositionMappings = mappings
    }

    /**
     * Return the mappings of {@code Content-Disposition} header values to
     * mapping keys. Mainly available for configuration through property paths
     * that specify individual keys.
     */
    public Properties getContentDispositionMappings() {
        if (this.contentDispositionMappings == null) {
            this.contentDispositionMappings = new Properties()
        }
        return this.contentDispositionMappings
    }

    /**
     * renders outside of a request Web environment
     * @param model
     * @param output the writer or stream you want to render to.
     * @return null if output was passed , or the temp file if output was null
     */
    public void render(ReportFormat format, Map<String, Object> params, Collection data, Object output) {
        params << [data: data, format: format]
        render(params, output)
    }

    /**
     * renders outside of a request Web environment
     * @param model
     * @param output the writer or stream you want to render to.
     * @return null if output was passed , or the temp file if output was null
     */
    public void render(Map<String, Object> model, Object output) {
        setupFormat(model)
        //TODO should we still in case stuff was set that we want passed through?
        //Map<String, Object> mergedModel = createMergedOutputModel(model, request, response)
        //prepareResponse(request, response)

        // TODO how to get subReports
        //exposeSubReports(model)
        // Expose Spring-managed Locale and MessageSource.
        //TODO how to get localization into the report
        //exposeLocalizationContext(model, request)

        JasperPrint print = fillReport(model)
        JasperUtils.render(model.format as ReportFormat, print, output)
    }

    /**
     * This is the main method that Grails/Spring calls to render the view in the MVC context
     * @param model
     * @param request
     * @param response
     * @throws Exception
     */
    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        setupFormat(model, response)

        super.render(model, request, response)
        /**
         the super is in AbstractView and calls
         Map<String, Object> mergedModel = createMergedOutputModel(model, request, response)
         prepareResponse(request, response)
         renderMergedOutputModel(mergedModel, getRequestToExpose(request), response)
         ^ is in AbstractJasperReportsView and calls
         exposeSubReports(model)
         exposeLocalizationContext(model, request)
         JasperPrint filledReport = fillReport(model)
         postProcessReport(filledReport, model)
         populateHeaders(response)
         renderReport(filledReport, model, response)

         */
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    public void setupFormat(Map model, HttpServletResponse response = null) {

        //if the model has one then use that as the override
        if (model[formatKey]) {
            def fmat = model[formatKey]
            model[formatKey] = fmat instanceof ReportFormat ? fmat : ReportFormat.get(fmat)
        }

        if (response && !model[formatKey]) {
            //use the one that grails puts in the response
            model[formatKey] = ReportFormat.get(response.format)
        }
        //if format is null still then just use html
        model[formatKey] = model[formatKey] ?: ReportFormat.HTML

        if (model[formatKey] == ReportFormat.HTML
                && !model.containsKey(JRParameter.IS_IGNORE_PAGINATION)) {
            model.put(JRParameter.IS_IGNORE_PAGINATION, true)
        }
    }

    /**
     * Locates the format key in the model using the configured discriminator key and uses this
     * key to lookup the appropriate view class from the mappings. The rendering of the
     * report is then delegated to an instance of that view class.
     */
    @Override
    @CompileStatic(TypeCheckingMode.SKIP)
    protected void renderReport(JasperPrint populatedReport, Map<String, Object> model, HttpServletResponse response)
            throws Exception {

        log.debug("Rendering report using format mapping key [$model.format]")
        //contentType = format.mimeType

        populateContentDispositionIfNecessary(response, model[formatKey].extension)

        if (model[formatKey].downloadContent) {
            renderReportUsingOutputStream(populatedReport, response, model.format)
        } else {
            renderReportUsingWriter(populatedReport, response, model.format)
        }

    }

    /**
     * Adds/overwrites the {@code Content-Disposition} header value with the format-specific
     * value if the mappings have been specified and a valid one exists for the given format.
     * @param response the {@code HttpServletResponse} to set the header in
     * @param format the format key of the mapping
     * @see #setContentDispositionMappings
     */
    private void populateContentDispositionIfNecessary(HttpServletResponse response, String format) {
        if (this.contentDispositionMappings != null) {
            String header = this.contentDispositionMappings.getProperty(format)
            if (header != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Setting Content-Disposition header to: [" + header + "]")
                }
                response.setHeader(AbstractJasperReportsView.HEADER_CONTENT_DISPOSITION, header)
            }
        }
    }

    /**
     * We need to write text to the response Writer.
     * @param print the populated {@code JasperPrint} to render
     * @param response the HTTP response the report should be rendered to
     * @throws Exception if rendering failed
     */
    @CompileStatic(TypeCheckingMode.SKIP)
    protected void renderReportUsingWriter(JasperPrint print, HttpServletResponse response, ReportFormat format) throws Exception {

        JRAbstractExporter exporter = (JRAbstractExporter) createExporter(format, print, response.getWriter())
        // Copy the encoding configured for the report into the response.
        String contentType = format.mimeType
        String encoding = (exporter.exporterOutput as WriterExporterOutput).getEncoding()
        if (encoding) {
            // Only apply encoding if content type is specified but does not contain charset clause already.
            if (contentType != null && !contentType.toLowerCase().contains(WebUtils.CONTENT_TYPE_CHARSET_PREFIX)) {
                contentType = contentType + WebUtils.CONTENT_TYPE_CHARSET_PREFIX + encoding
            }
        }
        response.setContentType(contentType)

        exporter.exportReport()
    }

    /**
     * We need to write binary output to the response OutputStream.
     * @param exporter the JasperReports exporter to use
     * @param populatedReport the populated {@code JasperPrint} to render
     * @param response the HTTP response the report should be rendered to
     * @throws Exception if rendering failed
     */
    protected void renderReportUsingOutputStream(JasperPrint print, HttpServletResponse response, ReportFormat format) throws Exception {
        response.setContentType(format.mimeType)

        //workaround for an IE bug when sending download content via HTTPS.
        //Copied from AbstractView.prepareResponse
        response.setHeader("Pragma", "private")
        response.setHeader("Cache-Control", "private, must-revalidate")

        // IE workaround: write into byte array first. not sure if eiher of these IE hack are needed or just angry monkey routines
        ByteArrayOutputStream baos = createTemporaryOutputStream()
        Exporter exporter = createExporter(format, print, baos)
        exporter.exportReport()

        // Write length (determined via byte array).
        response.setContentLength(baos.size())

        // Flush byte array to servlet output stream.
        ServletOutputStream out = response.getOutputStream()
        baos.writeTo(out)
        out.flush()
    }

    /**
     * Create a JasperReports exporter for a specific output format
     */
    Exporter createExporter(ReportFormat format, JasperPrint print, Object output) {
        JasperUtils.createExporter(format, print, output)
    }

}
