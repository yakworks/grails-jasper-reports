package jrsamples.datasource

import net.sf.jasperreports.engine.*
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
import net.sf.jasperreports.engine.design.JasperDesign
import net.sf.jasperreports.engine.export.HtmlExporter
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter
import net.sf.jasperreports.engine.util.JRLoader
import net.sf.jasperreports.engine.xml.JRXmlLoader
import net.sf.jasperreports.export.*
import org.grails.testing.GrailsUnitTest
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import spock.lang.Specification

/**
 * more or less gets the sample going as is from jaspers /datasource example
 */
class RunCustomSpec extends Specification implements GrailsUnitTest{

    static String TEST_JASPER_DIR = "src/test/resources/jasper"

    void "test something"() {
        expect:
        runReport()
        //print()
        pdf()
        html()
        xlsx()
    }

    //@CompileStatic
    def runReport() {
        Resource resource = new FileSystemResource("src/test/resources/DataSourceReport.jrxml")
        InputStream is = resource.getInputStream()
        JasperReport jreport
        is.withStream {
            JasperDesign design = JRXmlLoader.load(is)
            jreport = JasperCompileManager.compileReport(design)
        }

        def ds =new JRBeanCollectionDataSource(CustomBeanFactory.getBeanCollection())

        long start = System.currentTimeMillis();
        //Preparing parameters
        Map parameters = ["ReportTitle":"Address Report", "DataFile": "CustomBeanFactory.java - Bean Collection"]
        new File("$TEST_JASPER_DIR/").mkdirs()
        //parameters.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
        JasperFillManager.fillReportToFile(
                jreport, "$TEST_JASPER_DIR/DataSourceReport.jrprint",
                parameters,ds);
        println("Filling time : " + (System.currentTimeMillis() - start));

        return true
    }

    public void print() throws JRException {
        long start = System.currentTimeMillis();
        JasperPrintManager.printReport("$TEST_JASPER_DIR/DataSourceReport.jrprint", false);
        System.err.println("Printing time : " + (System.currentTimeMillis() - start));
    }


    public void pdf() throws JRException
    {
        long start = System.currentTimeMillis();
        JasperExportManager.exportReportToPdfFile("$TEST_JASPER_DIR/DataSourceReport.jrprint");
        System.err.println("PDF creation time : " + (System.currentTimeMillis() - start));
    }

    public void xlsx() throws JRException
    {
        long start = System.currentTimeMillis();
        File sourceFile = new File("$TEST_JASPER_DIR/DataSourceReport.jrprint");

        JasperPrint jasperPrint = (JasperPrint)JRLoader.loadObject(sourceFile);
        jasperPrint.setProperty("net.sf.jasperreports.export.xls.detect.cell.type", "true");
        jasperPrint.getPropertyNames().each{
            println it
        }

        File destFile = new File(sourceFile.getParent(), jasperPrint.getName() + ".xlsx");

        JRXlsxExporter exporter = new JRXlsxExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(destFile));

        exporter.exportReport();

        System.err.println("XLSX creation time : " + (System.currentTimeMillis() - start));
    }

    /**
     *
     */
    public void html() throws JRException
    {
        long start = System.currentTimeMillis();
        File sourceFile = new File("$TEST_JASPER_DIR/DataSourceReport.jrprint");
        JasperPrint jasperPrint = (JasperPrint)JRLoader.loadObject(sourceFile);

        File destFile = new File(sourceFile.getParent(), jasperPrint.getName() + ".html");

        HtmlExporter exporter = new HtmlExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleHtmlExporterOutput(destFile));

        SimpleHtmlExporterConfiguration exporterConfig = new SimpleHtmlExporterConfiguration();
        exporterConfig.setBetweenPagesHtml("");
        exporter.setConfiguration(exporterConfig);

        SimpleHtmlReportConfiguration reportConfig = new SimpleHtmlReportConfiguration();
        reportConfig.setRemoveEmptySpaceBetweenRows(true);
        exporter.setConfiguration(reportConfig);

        exporter.exportReport();

        //JasperExportManager.exportReportToHtmlFile("target/jasper/DataSourceReport.jrprint");
        System.err.println("HTML creation time : " + (System.currentTimeMillis() - start));
    }

//    public void testRenderAsHtmlWithCollection() throws Exception {
//        StringWriter writer = new StringWriter();
//        JasperReportsUtils.renderAsHtml(getReport(), getParameters(), getData(), writer);
//        String output = writer.getBuffer().toString();
//        assertHtmlOutputCorrect(output);
//    }
//
//    private void assertPdfOutputCorrect(byte[] output) throws Exception {
//        assertTrue("Output length should be greater than 0", (output.length > 0));
//
//        String translated = new String(output, "US-ASCII");
//        assertTrue("Output should start with %PDF", translated.startsWith("%PDF"));
//    }
//
//    private void assertHtmlOutputCorrect(String output) {
//        assertTrue("Output length should be greater than 0", (output.length() > 0));
//        assertTrue("Output should contain <html>", output.contains("<html>"));
//        assertTrue("Output should contain 'MeineSeite'", output.contains("MeineSeite"));
//    }
//
//    private void assertXlsOutputCorrect(byte[] output) throws Exception {
//        HSSFWorkbook workbook = new HSSFWorkbook(new ByteArrayInputStream(output));
//        HSSFSheet sheet = workbook.getSheetAt(0);
//        assertNotNull("Sheet should not be null", sheet);
//        HSSFRow row = sheet.getRow(3);
//        HSSFCell cell = row.getCell((short) 1);
//        assertNotNull("Cell should not be null", cell);
//        assertEquals("Cell content should be Dear Lord!", "Dear Lord!", cell.getRichStringCellValue().getString());
//    }
}
