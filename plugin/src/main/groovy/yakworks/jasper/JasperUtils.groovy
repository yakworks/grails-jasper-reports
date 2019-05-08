/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.jasper

import java.sql.Connection
import java.sql.SQLException
import javax.sql.DataSource

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Slf4j

import org.springframework.context.ApplicationContextException
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataRetrievalFailureException
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.util.CollectionUtils
import org.springframework.util.StringUtils

import net.sf.jasperreports.engine.*
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
import net.sf.jasperreports.engine.export.HtmlExporter
import net.sf.jasperreports.engine.export.JRPdfExporter
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter
import net.sf.jasperreports.engine.util.JRLoader
import net.sf.jasperreports.export.Exporter
import net.sf.jasperreports.export.SimpleExporterInput
import net.sf.jasperreports.export.SimpleHtmlExporterConfiguration
import net.sf.jasperreports.export.SimpleHtmlExporterOutput
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput
import yakworks.reports.ReportFormat

/**
 * Utility methods for working with JasperReports. Provides a set of convenience
 * methods for generating reports in a CSV, HTML, PDF and XLS formats.
 *
 * @author Joshua Burnett
 */
@Slf4j
@CompileStatic
@SuppressWarnings(['JdbcConnectionReference'])
class JasperUtils {

    /**
     * Render a report in HTML format using the supplied report data.
     * Writes the results to the supplied {@code Writer}.
     * @param report the {@code JasperReport} instance to render
     * @param parameters the parameters to use for rendering
     * @param writer the {@code Writer} to write the rendered report to
     * @param reportData a {@code JRDataSource}, {@code java.util.Collection} or object array
     * (converted accordingly), representing the report data to read fields from
     * @throws JRException if rendering failed
     * @see #convertReportData
     */
    public static Exporter createExporter(ReportFormat format, JasperPrint print, Object output) throws JRException {

        //JasperUtils."export${format.name()}"(report,output,model)
        Exporter exporter

        switch (format) {
            case ReportFormat.PDF:
                exporter = createExporterPDF(print, output)
                break
            case ReportFormat.XLSX:
                exporter = createExporterXLSX(print, output)
                break
            case ReportFormat.HTML:
                exporter = createExporterHTML(print, output)
                break
            default:
                throw new IllegalArgumentException("Export format [$format] not yet supported")
        }
        //standard for all
        exporter.setExporterInput(new SimpleExporterInput(print))

        return exporter

    }

    @CompileDynamic
    public static JRPdfExporter createExporterPDF(JasperPrint print, Object stream) throws JRException {
        JRPdfExporter exporter = new JRPdfExporter()
        exporter.setExporterInput(new SimpleExporterInput(print))
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(stream))
        return exporter
    }

    @CompileDynamic
    public static JRXlsxExporter createExporterXLSX(JasperPrint print, Object stream) throws JRException {
        JRXlsxExporter exporter = new JRXlsxExporter()
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(stream))

        print.setProperty("net.sf.jasperreports.export.xls.detect.cell.type", "true")
        print.setProperty("net.sf.jasperreports.export.xls.force.page.breaks", "true")
        //print.setProperty("net.sf.jasperreports.export.ignore.page.margins", "true")
        return exporter
    }

    @CompileDynamic
    public static HtmlExporter createExporterHTML(JasperPrint print, Object writer) throws JRException {
        HtmlExporter exporter = new HtmlExporter()
        exporter.setExporterInput(new SimpleExporterInput(print))
        exporter.setExporterOutput(new SimpleHtmlExporterOutput(writer))
        SimpleHtmlExporterConfiguration exporterConfig = new SimpleHtmlExporterConfiguration()
        exporterConfig.setBetweenPagesHtml("")
        exporter.setConfiguration(exporterConfig)
//        SimpleHtmlReportConfiguration reportConfig = new SimpleHtmlReportConfiguration()
//        reportConfig.setRemoveEmptySpaceBetweenRows(true)
//        exporter.setConfiguration(reportConfig)

        print.setProperty("net.sf.jasperreports.export.ignore.page.margins", "true")

        return exporter
    }

    /**
     * Convert the given report data value to a {@code JRDataSource}.
     * <p>In the default implementation, a {@code JRDataSource},
     * {@code java.util.Collection} or object array is detected.
     * The latter are converted to {@code JRBeanCollectionDataSource}
     * or {@code JRBeanArrayDataSource}, respectively.
     * @param value the report data value to convert
     * @return the JRDataSource (never {@code null})
     * @throws IllegalArgumentException if the value could not be converted
     * @see net.sf.jasperreports.engine.JRDataSource
     * @see net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
     * @see net.sf.jasperreports.engine.data.JRBeanArrayDataSource
     */
    public static JRDataSource convertReportData(Object value) throws IllegalArgumentException {
        if (value instanceof JRDataSource) {
            return value as JRDataSource
        } else if (value instanceof Collection) {
            return new JRBeanCollectionDataSource((Collection<?>) value)
        } else if (value instanceof Object[]) {
            return new JRBeanArrayDataSource(value)
        } else {
            throw new IllegalArgumentException("Value [" + value + "] cannot be converted to a JRDataSource")
        }
    }

    /**
     * Render a report in HTML format using the supplied report data.
     * Writes the results to the supplied {@code Writer}.
     * @param report the {@code JasperReport} instance to render
     * @param parameters the parameters to use for rendering
     * @param writer the {@code Writer} to write the rendered report to
     * @param reportData a {@code JRDataSource}, {@code java.util.Collection} or object array
     * (converted accordingly), representing the report data to read fields from
     * @throws JRException if rendering failed
     * @see #convertReportData
     */
    public static void renderAsHtml(JasperReport report, Map<String, Object> parameters, Object reportData,
                                    Writer writer) throws JRException {

        JasperPrint print = JasperFillManager.fillReport(report, parameters, convertReportData(reportData))

        HtmlExporter exporter = createExporterHTML(print, writer)
        exporter.exportReport()

    }

    /**
     * Render a report in PDF format using the supplied report data.
     * Writes the results to the supplied {@code OutputStream}
     *
     * @param report the {@code JasperReport} instance to render
     * @param parameters the parameters to use for rendering
     * @param stream the {@code OutputStream} to write the rendered report to
     * @param reportData a {@code JRDataSource}, {@code java.util.Collection} or object array
     * @throws JRException
     */
    public static void renderAsPdf(JasperReport report, Map<String, Object> parameters, Object reportData,
                                   OutputStream stream) throws JRException {

        JasperPrint print = JasperFillManager.fillReport(report, parameters, convertReportData(reportData))
        JasperExportManager.exportReportToPdfStream(print, stream)
    }

    /**
     * renders a source file to a temp file and returns it as a resrouce
     *
     * @param format {@code ReportFormat} enum instance to use
     * @param print the  {@code JasperPrint} that is filled and ready for export
     * @param output the {@code Writer} or {@code OutputStream} you want to render to.
     * @return null if output was passed , or the temp file if output was null
     * @throws JRException
     */
    public static Resource render(ReportFormat format, JasperPrint print, Object output = null) throws JRException {
        FileSystemResource fsr
        Exporter exporter

        if (output == null) {
            File tmp = File.createTempFile("jasper-${print.name}", format.extension)
            fsr = new FileSystemResource(tmp)
            exporter = createExporter(format, print, fsr.outputStream)
        } else {
            exporter = createExporter(format, print, output)
        }
        exporter.exportReport()

        return fsr
    }

    /**
     * renders a source file to a destination file
     *
     * @param format {@code ReportFormat} enum instance to use
     * @param sourceFileName the source file name. either a .jrxml or a .jasper
     * @param destFileName the destination file name.
     * @param parameters the parameters to pass in
     * @param reportData a {@code JRDataSource}, {@code java.util.Collection} or object array to populate the report
     * @return
     * @throws JRException
     */
    public static Resource render(ReportFormat format, String sourceFileName, String destFileName,
                                  Map<String, Object> parameters, Object reportData) throws JRException {

        JasperReport rpt = loadReport(sourceFileName)

        JasperPrint print = JasperFillManager.fillReport(rpt, parameters, convertReportData(reportData))

        def fsr = new FileSystemResource(destFileName)

        Exporter exporter = createExporter(format, print, fsr.outputStream)
        exporter.exportReport()

        return fsr
    }

    /**
     * renders a source file to a temp file and returns it as a resrouce
     *
     * @param format {@code ReportFormat} enum instance to use
     * @param rptResource the source {@code Resource}. either a .jrxml or a .jasper
     * @param parameters the parameters to pass in
     * @param reportData a {@code JRDataSource}, {@code java.util.Collection} or object array
     * @return the Resource with the generated doc as a temp file
     * @throws JRException
     */
    public static Resource render(ReportFormat format, Resource rptResource,
                                  Map<String, Object> parameters, Object reportData) throws JRException {

        JasperReport rpt = loadReport(rptResource)

        JasperPrint print = JasperFillManager.fillReport(rpt, parameters, convertReportData(reportData))

        File tmp = File.createTempFile("jasper-${rpt.name}", format.extension)
        def fsr = new FileSystemResource(tmp)

        Exporter exporter = createExporter(format, print, fsr.outputStream)
        exporter.exportReport()

        return fsr
    }

    /**
     * just calls loadReport after converting string to a Resource
     */
    public static JasperReport loadReport(String reportFilePath) {
        loadReport(new UrlResource(reportFilePath))
    }
    /**
     * Loads a {@code JasperReport} from the specified {@code Resource}.
     * If the {@code Resource} points to an uncompiled report design file then
     * the report file is compiled dynamically and loaded into memory.
     * @param resource the {@code Resource} containing the report definition or design
     * @return a {@code JasperReport} instance
     */
    public static JasperReport loadReport(Resource resource) {
        try {
            String filename = resource.getFilename()
            if (filename != null) {
                if (filename.endsWith(".jasper")) {
                    // Load pre-compiled report.
                    log.debug("Loading pre-compiled Jasper Report from $resource")
                    return resource.getInputStream().withStream {
                        return (JasperReport) JRLoader.loadObject(it)
                    }
                } else if (filename.endsWith(".jrxml")) {
                    // Compile report on-the-fly.
                    log.debug("Compiling Jasper Report loaded from $resource")
                    return resource.getInputStream().withStream {
                        return JasperCompileManager.compileReport(it)
                    }

                }
                throw new IllegalArgumentException("Report filename [" + filename + "] must end in either .jasper or .jrxml")
            }
            throw new IllegalArgumentException(
                    "Report [$resource} getFilename can't be null")

        }
        catch (IOException ex) {
            throw new ApplicationContextException(
                    "Could not load JasperReports report from " + resource, ex)
        }
        catch (JRException ex) {
            throw new ApplicationContextException(
                    "Could not parse JasperReports report from " + resource, ex)
        }
    }

    /**
     * Converts the parameters array in a report to a list of maps so they are easier to work with
     * and pass up to browser
     *
     * @param jasperReport
     * @return Map with keys [name, description, value( default value), type (the valueClass)]
     */
    @CompileStatic(TypeCheckingMode.SKIP)
    public static List<Map<String, Object>> getPromptingParams(JasperReport jasperReport) {
        JRParameter[] params = jasperReport.getParameters()
        return params.findAll { param ->
            !param.systemDefined && param.forPrompting
        }.collect { param ->
            [
                    name          : param.name,
                    description   : param.description ?: StringUtils.capitalize(param.name),
                    value         : param.defaultValueExpression?.text,
                    type          : param.valueClass,
                    valueClass    : param.valueClass,
                    valueClassName: param.valueClassName
            ]
        }
    }

    /**
     * By default, this method will use any {@code JRDataSource} instance
     * (or wrappable {@code Object}) that can be located using {@code data} as a key,
     * a lookup for type {@code JRDataSource} or DataSrouce, regardless of key will take place if not found with report Key
     * @param model the model for this request
     * @param reportDataKey the dataKey to use to find the datasource
     * @return the JRDataSource instance to use
     */
    public
    static JRDataSource extractJasperDataSrouce(Map<String, Object> model, String reportDataKey = null) throws Exception {
        // Determine main report.

        JRDataSource jrDataSource = null

        // Try with the specified key
        if (reportDataKey && model[reportDataKey]) {
            Object reportDataValue = model.get(reportDataKey)
            if (reportDataValue instanceof DataSource) {
                return new JRDataSourceJDBC((DataSource) reportDataValue)
            } else {
                return convertReportData(reportDataValue)
            }
        }

        //find the first item in map thats a collection
        Collection<?> values = model.values()
        jrDataSource = CollectionUtils.findValueOfType(values, JRDataSource)
        if (jrDataSource) {
            return jrDataSource
        } else {
            // see if the map has a DataSource in it and grab it
            def ds = CollectionUtils.findValueOfType(values, DataSource)
            if (ds) {
                return new JRDataSourceJDBC(ds)
            }
        }

    }

    public
    static JasperPrint fillReport(JasperReport report, Map<String, Object> model, DataSource dataSource) throws DataAccessException {

        Connection con = DataSourceUtils.getConnection(dataSource)
        try {

            return JasperFillManager.fillReport(report, model, con)
        }
        catch (SQLException ex) {
            // Release Connection early, to avoid potential connection pool deadlock
            // in the case when the exception translator hasn't been initialized yet.
            DataSourceUtils.releaseConnection(con, dataSource)
            con = null
            throw new DataRetrievalFailureException(ex.message, ex)
        }
        finally {
            DataSourceUtils.releaseConnection(con, dataSource)
        }
    }
}
