package yakworks.jasper

import grails.plugin.viewtools.ViewResourceLocator
import grails.testing.web.GrailsWebUnitTest
import grails.util.BuildSettings
import yakworks.jasper.spring.JasperViewResolver
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.grails.plugins.web.GroovyPagesGrailsPlugin
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.UrlResource
import org.springframework.web.servlet.View
import spock.lang.Specification

/**
 * Playground for various features.
 */
class JasperViewSpec extends Specification implements GrailsWebUnitTest {
    static String TEST_JASPER_DIR = "src/test/resources/jasper"

    Closure doWithSpring() {
      return TestAppCtx.doWithSpring
    }

    ViewResourceLocator jasperViewResourceLocator //= ViewResourceLocator.mockForTest()
    JasperViewResolver jasperViewResolver

    static List<Map> dataList = [
            [city:"Berne", id:22, name:"Bill Ott", street:"250 - 20th Ave.", country:[name:"US"]],
            [city:"Chicago", id:1, name:"Joshua Burnett", street:"22 3rd", country:[name:"US"]]
    ]
    Map model = [
        ReportTitle:"Test Report",
        DataFile: "Foo",
        data:dataList
    ]

    String viewName = "/test/testme.jrxml"

    void setup(){
        jasperViewResourceLocator = applicationContext.getBean('jasperViewResourceLocator')
        jasperViewResolver = applicationContext.getBean('jasperViewResolver')
    }


    void "sanity playground"() {
        expect:
        def abs = GroovyPagesGrailsPlugin.transformToValidLocation(BuildSettings.BASE_DIR.absolutePath)
        def abs2 = new File("").absolutePath
        //assert abs==abs2
        def baseRes =  new UrlResource("file:.")
        assert baseRes.exists()
        def rl = [getResource:{url -> baseRes.createRelative(url)  as Resource}] as ResourceLoader

        assert rl.getResource("/grails-app/views/test/testme.jrxml").exists()
        //jasperViewResourceLocator.addResourceLoader(rl)
        jasperViewResourceLocator.getResource(viewName)
        jasperViewResolver.resolveViewName(viewName,null)
    }

    void "format in the model"() {
        when:
        View view = jasperViewResolver.resolveViewName(viewName,null)
        model << [format:format]
        view.render(model, request, response)

        then:
        def res = response.getContentAsString()
        res.length() > 0
        res.startsWith(startsWith)
        if(format == "xlsx") assertXlsOutputCorrect(response.contentAsByteArray)

        where:
        format  | startsWith
        "pdf"   | "%PDF"
        "html"  | "<!DOCTYPE html"
        null    | "<!DOCTYPE html"
        "xlsx"  | ""
    }

    void "format with reponse from Accept header or params"() {
        when:
        View view = jasperViewResolver.resolveViewName(viewName,null)
        response.format = format
        view.render(model, request, response)

        then:
        def res = response.getContentAsString()
        res.length() > 0
        res.startsWith(startsWith)
        if(format == "xlsx") assertXlsOutputCorrect(response.contentAsByteArray)

        where:
        format  | startsWith
        "pdf"   | "%PDF"
        "html"  | "<!DOCTYPE html"
        null    | "<!DOCTYPE html"
        "xlsx"  | ""
    }

    void "view XLSX"() {
        when:
        View view = jasperViewResolver.resolveViewName(viewName,null)
        model << [format:"XLSX"]
        new File("$TEST_JASPER_DIR/").mkdir()

        then:
        view.render(model, request, response)
        def res = response.getContentAsString()
        assert res.length() > 0

        //write to file

        def fos = new FileOutputStream("$TEST_JASPER_DIR/testme.xlsx")
        // Output the ByteArrayOutputStream buffer to it
        fos << response.contentAsByteArray
        // Close the stream und underlying file
        fos.close()
        assertXlsOutputCorrect(response.contentAsByteArray)

        when:"format not specified it should still be html"
        response.reset()
        model.remove("format")
        view.render(model, request, response)

        then: "It should be html"
        view.render(model, request, response)
        def res2 = response.getContentAsString()
        assert res2.length() > 0
        assert res2.startsWith("<!DOCTYPE html")
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
        return true
    }

}
