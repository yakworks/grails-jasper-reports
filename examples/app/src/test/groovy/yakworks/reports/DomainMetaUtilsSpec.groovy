package yakworks.reports

import foo.Bills
import foo.Customer
import foo.Product
import foo.ProductGroup
import grails.testing.gorm.DataTest
import grails.testing.web.GrailsWebUnitTest
import org.grails.datastore.mapping.model.PersistentEntity
import spock.lang.Ignore
import spock.lang.Specification

class DomainMetaUtilsSpec extends Specification implements DataTest, GrailsWebUnitTest {

    void setupSpec(){
        mockDomains(ProductGroup,Bills,Customer,Product)
    }

    void "buildColumnMap works for Bills"() {
        when:
        def fooGrp = new ProductGroup(name:"Foo Group").save()
        assert ProductGroup.get(1).name == "Foo Group"
        assert grailsApplication.mappingContext.getPersistentEntity(ProductGroup.name)
        def domainClass = grailsApplication.mappingContext.getPersistentEntity(ProductGroup.name)
        assert domainClass
        def colmap = DomainMetaUtils.getFieldMetadata(domainClass, ['name'])

        then:
        assert colmap['name']
    }

    @Ignore('https://github.com/yakworks/grails-jasper-reports/issues/11')
    void "buildColumnMap works with config on Bills and nested properties"() {
        given:
        PersistentEntity domainClass = grailsApplication.mappingContext.getPersistentEntity(Bills.name)
        def fields = ['customer.name','product.group.name', 'color', 'product.name', 'qty', 'amount']

        Map cfg = ['product.name':'Flubber']
        String foo = ""

        expect:
        domainClass != null

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
        grailsApplication.mappingContext.getPersistentEntity(Bills.name)
        grailsApplication.mappingContext.getPersistentEntity("foo.Bills")
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
