package yakworks.jasper

import net.sf.jasperreports.engine.JasperCompileManager
import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.JasperReport
import yakworks.reports.ReportFormat
import org.grails.testing.GrailsUnitTest
import spock.lang.Specification

/**
 * Playground for various features.
 */
class JasperUtilSpec extends Specification implements GrailsUnitTest {

    def setup() {
    }

    def cleanup() {
    }

//    void "test this"() {
//    	expect:
//        runReport()
//        //print()
//        pdf()
//        html()
//        //xlsx()
//    }
    static List<Map> dataList = [
            [city:"Berne", id:22, name:"Bill Ott", street:"250 - 20th Ave.", country:[name:"US"]],
            [city:"Chicago", id:1, name:"Joshua Burnett", street:"22 3rd", country:[name:"US"]]
    ]

    Map parameters = ["ReportTitle":"Test Report", "DataFile": "List of maps"]

    void "test createExporter PDF"() {

        when:
        def os = new ByteArrayOutputStream();
        def exporter = JasperUtils.createExporter(ReportFormat.PDF,getJasperPrint(), os)
        exporter.exportReport()

        then:
        new String(os.toByteArray(), "US-ASCII").startsWith("%PDF")

    }

    void "test createExporter Excel"() {

        when:
        def os = new ByteArrayOutputStream();
        def exporter = JasperUtils.createExporter(ReportFormat.XLSX,getJasperPrint(), os)
        exporter.exportReport()

        then:
        os.toByteArray().length >0

    }

    void "test createExporter HTML"() {
        when:
        StringWriter sw = new StringWriter();
        def exporter = JasperUtils.createExporter(ReportFormat.HTML,getJasperPrint(), sw)
        exporter.exportReport()

        then:
        sw.toString().length() > 0
    }

    void "test renderAsPdf"() {

        when:
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        JasperUtils.renderAsPdf(getReport(), parameters, dataList, os)
        byte[] output = os.toByteArray()

        then:
        output.length > 0
        new String(os.toByteArray(), "US-ASCII").startsWith("%PDF")
    }

    void "test renderAsHtml"() {

        when:
        StringWriter os = new StringWriter();
        JasperUtils.renderAsHtml(getReport(), parameters, dataList, os)

        then:
        os.toString().length() > 0
        os.toString().contains("Test Report")
    }

    boolean assertPdfOutputCorrect(byte[] output) throws Exception {
        assert output.length > 0

        String translated = new String(output, "US-ASCII")
        assert translated.startsWith("%PDF")
    }

    JasperReport getReport(){
        //ClassPathResource resource = new ClassPathResource("src/test/resources/NestProps.jrxml");
        //def res = grailsApplication.mainContext.getResource(".")
        //assert res.getURL().toString().contains("blah")
        return JasperCompileManager.compileReport("grails-app/views/test/testme.jrxml")
        //def res = grailsApplication.mainContext.getResource("src/test/resources/testme.jrxml")
        //return (JasperReport) JRLoader.loadObject(res.getInputStream())
    }

    JasperPrint getJasperPrint(){
        JasperFillManager.fillReport(report, parameters, JasperUtils.convertReportData(dataList));
    }


}
