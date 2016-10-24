package nine.reports

import foo.Bills
import foo.Customer
import foo.Product
import foo.ProductGroup
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import spock.lang.Specification


//@TestMixin(GrailsUnitTestMixin)
@TestMixin(ControllerUnitTestMixin)
@Mock([ProductGroup,Bills,Customer,Product])
class DomainMetaUtilsSpec extends Specification {

//    void setup(){
//
//    }
    void "buildColumnMap works for Bills"() {
        when:
        def fooGrp = new ProductGroup(name:"Foo Group").save()
        assert ProductGroup.get(1).name == "Foo Group"
//        assert fooGrp.getDomainClass()
        grailsApplication.domainClasses.each {
            println it.name
        }
        println Bills.name
        assert grailsApplication.getDomainClass(ProductGroup.name)
        def domainClass = grailsApplication.getDomainClass(ProductGroup.name)
        assert domainClass
        def colmap = DomainMetaUtils.getFieldMetadata(domainClass, ['name'])

        then:
        assert colmap['name']
    }

    void "buildColumnMap works with config on Bills and nested properties"() {
        given:
        GrailsDomainClass domainClass = grailsApplication.getDomainClass(Bills.name)
        def fields = ['customer.name','product.group.name', 'color', 'product.name', 'qty', 'amount']

        Map cfg = ['product.name':'Flubber']
        String foo = ""

        when:

        def colmap = DomainMetaUtils.getFieldMetadata(domainClass, fields,cfg)

        then:
        assert colmap.size() == 6
        assert colmap[field].title == title
        assert colmap[field].typeClassName == typeName

        where:
        field               | title           | typeName
        'customer.name'     | 'Customer'      | 'java.lang.String'
        'product.group.name'| 'Product Group' | 'java.lang.String'
        'product.name'      | 'Flubber'       | 'java.lang.String'
        'qty'               | 'Qty'           | 'java.lang.Long'
        'amount'            | 'Amount'        | 'java.math.BigDecimal'
        'color'            | 'Color'        | 'java.lang.String'


    }

    void "findDomainClass"() {
        expect:
        grailsApplication.getDomainClass(Bills.name)
        grailsApplication.getDomainClass("foo.Bills")
        DomainMetaUtils.findDomainClass("foo.Bills")
        DomainMetaUtils.findDomainClass("Bills")
        DomainMetaUtils.findDomainClass("bills")
        DomainMetaUtils.findDomainClass("asfasfasdf") == null

    }

    void "getNaturalTitle"() {
        expect:
        DomainMetaUtils.getNaturalTitle("Bills") == 'Bills'
        DomainMetaUtils.getNaturalTitle("bills") == 'Bills'
        DomainMetaUtils.getNaturalTitle("foo.Bills") == 'Foo Bills'
        DomainMetaUtils.getNaturalTitle("customer.name") == 'Customer'
        DomainMetaUtils.getNaturalTitle("customer.org.name") == 'Customer Org'
        DomainMetaUtils.getNaturalTitle("customer.org.id") == 'Customer Org Id'
        DomainMetaUtils.getNaturalTitle("customerOrgNum") == 'Customer Org Num'
        DomainMetaUtils.getNaturalTitle("customerOrgName") == 'Customer Org Name'
        DomainMetaUtils.getNaturalTitle("xx99yy1URLlocX90") == 'Xx99yy1 URL loc X90'
    }
}
