package jrsamples.datasource

import net.sf.jasperreports.engine.*
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
import net.sf.jasperreports.engine.export.JRXhtmlExporter
import net.sf.jasperreports.engine.util.JRLoader
import net.sf.jasperreports.export.SimpleExporterInput
import net.sf.jasperreports.export.SimpleHtmlExporterOutput
import org.grails.testing.GrailsUnitTest
import spock.lang.Specification

/**
 * Playground for various features.
 */
class NestPropsSpec extends Specification implements GrailsUnitTest {

    void "test this"() {
    	expect:
        runReport()
        //print()
        pdf()
        html()
        xml()
        xhtml()
        //xlsx()
    }


    def runReport() {
        //def resource = new FileSystemResource("src/test/resources/NestProps.jrxml")
        //InputStream is = resource.getInputStream()
        JasperReport jreport = JasperCompileManager.compileReport("src/test/resources/NestProps.jrxml")
//        is.withStream {
//            JasperDesign design = JRXmlLoader.load(is)
//            jreport = JasperCompileManager.compileReport(design)
//        }

        def ds =new JRBeanCollectionDataSource(CustomBeanFactory.listMap)

        long start = System.currentTimeMillis();
        //Preparing parameters
        Map parameters = ["ReportTitle":"NestProps Report", "DataFile": "CustomBeanFactory.listMap"]
        new File("target/jasper/").mkdirs()
        assert(new File("target/jasper/")).exists()
        JasperFillManager.fillReportToFile(
                jreport, "target/jasper/NestProps.jrprint",
                parameters,ds);
        println("Filling time : " + (System.currentTimeMillis() - start));
        //JasperUtil.exportReportToPdfFile()
        return true
    }

    public void pdf() throws JRException
    {
        long start = System.currentTimeMillis();
        JasperExportManager.exportReportToPdfFile("target/jasper/NestProps.jrprint");
        System.err.println("PDF creation time : " + (System.currentTimeMillis() - start));
    }

    /**
     *
     */
    public void html() throws JRException
    {
        long start = System.currentTimeMillis();
        JasperExportManager.exportReportToHtmlFile("target/jasper/NestProps.jrprint");
        System.err.println("HTML creation time : " + (System.currentTimeMillis() - start));
    }

    /**
     *
     */
    public void xml() throws JRException
    {
        long start = System.currentTimeMillis();
        JasperExportManager.exportReportToXmlFile("target/jasper/NestProps.jrprint",false);
        System.err.println("HTML creation time : " + (System.currentTimeMillis() - start));
    }


    public void xhtml() throws JRException
    {
        File sourceFile = new File("target/jasper/NestProps.jrprint");

        JasperPrint jasperPrint = (JasperPrint)JRLoader.loadObject(sourceFile);

        File destFile = new File(sourceFile.getParent(), jasperPrint.getName() + ".xhtml");

        JRXhtmlExporter exporter =new JRXhtmlExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleHtmlExporterOutput(destFile));

        exporter.exportReport();

    }

}
