package yakworks.jasper

import grails.plugin.viewtools.ViewResourceLocator
import jrsamples.datasource.CustomBeanFactory
import yakworks.jasper.spring.JasperView
import yakworks.jasper.spring.JasperViewResolver
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.grails.testing.GrailsUnitTest
import org.springframework.web.servlet.View
import spock.lang.Specification

/**
 * Playground for various features.
 */
class JasperViewNoRequestSpec extends Specification implements GrailsUnitTest {

    static String TEST_JASPER_DIR = "src/test/resources/jasper"
    Closure doWithSpring() { return TestAppCtx.doWithSpring }

    ViewResourceLocator jasperViewResourceLocator //= ViewResourceLocator.mockForTest()
    JasperViewResolver jasperViewResolver

    void setupSpec() {
        new File(TEST_JASPER_DIR).mkdirs()
    }

    static List<Map> dataList = [
            [city:"Berne", id:22, name:"Bill Ott", street:"250 - 20th Ave.", country:[name:"US"]],
            [city:"Chicago", id:1, name:"Joshua Burnett", street:"22 3rd", country:[name:"US"]]
    ]
    Map model = [
        ReportTitle:"Test Report",
        data:dataList
    ]

   // Map parameters = ["ReportTitle":"Test Report", "DataFile": "Foo"]

    String viewName = "/test/testme.jrxml"

    void setup(){
        jasperViewResourceLocator = applicationContext.getBean('jasperViewResourceLocator')
        jasperViewResolver = applicationContext.getBean('jasperViewResolver')
    }

    void "format in the model"() {
        when:
        JasperView view = jasperViewResolver.resolveViewName(viewName,null)
        model << [format:format]
        //def output = new ByteArrayOutputStream()

        view.render(model, output)

        then:
        assert output."$contentMethod"().toString().startsWith(startsWith)
        if(format == "xlsx") assertXlsOutputCorrect(output."$contentMethod"())

        where:
        format  | output                                | contentMethod | startsWith
        "pdf"   | new ByteArrayOutputStream()           | "toString"    | "%PDF"
        "pdf"   | new File("$TEST_JASPER_DIR/testme.pdf")  | "getText"     |"%PDF"
        "html"  | new StringWriter()                    | "toString"    |"<!DOCTYPE html"
        "html"  | new ByteArrayOutputStream()           | "toString"    |"<!DOCTYPE html"
        null    | new StringWriter()                    | "toString"    |"<!DOCTYPE html"
        "xlsx"  | new ByteArrayOutputStream()           | "toByteArray" | ""
        "xlsx"  | new File("$TEST_JASPER_DIR/testme.xlsx") | "getBytes"    | ""

    }

    void "put data in a different key"() {
        when:
        View view = jasperViewResolver.resolveViewName("test/contactReport.jrxml",null)
        Map model = [
            ReportTitle:"Bean Report",
            people: CustomBeanFactory.beanCollection,
            format:format
        ]
        File f = new File("$TEST_JASPER_DIR/contactReport.$format")
        if(!f.exists())f.createNewFile()
        view.render(model, f)

        then:
        f.exists()

        where:
        format  | output                                | contentMethod | startsWith
        "pdf"   | new ByteArrayOutputStream()           | "toString"    | "%PDF"
        "html"  | new StringWriter()                    | "toString"    |"<!DOCTYPE html"
        "xlsx"  | new ByteArrayOutputStream()           | "toByteArray" | ""

    }

    boolean assertXlsOutputCorrect(byte[] output) throws Exception {
        XSSFWorkbook workbook
        workbook = new XSSFWorkbook(new ByteArrayInputStream(output));
        XSSFSheet sheet = workbook.getSheetAt(0);
        assert sheet
        def row = sheet.getRow(3);
        def cell = row.getCell(1);
        assert cell;
        assert "Test Report" == cell.getRichStringCellValue().getString()

        assert sheet.getRow(9).getCell(1).getNumericCellValue()==22.0
        assert sheet.getRow(9).getCell(2).getStringCellValue()=='Bill Ott'
        assert sheet.getRow(15).getCell(1).getNumericCellValue()==1
        return true
    }

}
