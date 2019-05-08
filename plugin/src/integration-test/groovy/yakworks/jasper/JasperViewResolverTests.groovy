package yakworks.jasper

import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import yakworks.jasper.spring.JasperViewResolver
import yakworks.reports.ReportFormat
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Specification

@Integration
@Rollback
class JasperViewResolverTests extends Specification {

    JasperViewResolver jasperViewResolver
    def grailsApplication

    static List<Map> dataList = [
            [city:"Berne", id:22, name:"Bill Ott", street:"250 - 20th Ave.", country:[name:"US"]],
            [city:"Chicago", id:1, name:"Joshua Burnett", street:"22 3rd", country:[name:"US"]]
    ]

    Map model = ["ReportTitle":"Test Report", "DataFile": "List of maps"]

    MockHttpServletRequest request
    MockHttpServletResponse response

    void env(){
        //already setup in grails2, needs this in grails 3
        //GrailsWebEnvironment.bindRequestIfNull(grailsApplication.mainContext)
        request = GrailsWebRequest.lookup()?.currentRequest
        response = GrailsWebRequest.lookup()?.currentResponse
        //assert request instanceof JasperViewResolver
    }

    void testGetView() {
        def view = jasperViewResolver.resolveViewName("/test/testme.jrxml",Locale.US)
        assert view
    }

    /**
     * Calls getView to grab the freemarker tempalte and and then passes to render(view,model...)
     */
    void testRender_PDF(){
        env()
        def view = jasperViewResolver.resolveViewName("/test/testme.jrxml",Locale.US)
        model << [data:dataList,format:"PDF"]
        view.render(model, request, response)
        def res = response.getContentAsString()
        assert res.length() > 0
        assert res.startsWith("%PDF")
    }

    void testRender_HTML(){
        env()
        def view = jasperViewResolver.resolveViewName("/test/testme.jrxml",Locale.US)
        model << [data:dataList] //,format:"HTML"] <-default should be HTML
        view.render(model, request, response)
        def res = response.getContentAsString()
        assert res.length() > 0
        assert res.contains("Test Report</span>")
    }

    void testRender_XLSX(){
        env()
        def view = jasperViewResolver.resolveViewName("/test/testme.jrxml",Locale.US)
        model << [data:dataList,format:ReportFormat.XLSX]
        view.render(model, request, response)
        def res = response.getContentAsString()
        assert res.length() > 0
    }

}
