package nine.reports

import grails.util.GrailsNameUtils
import grails.util.Holders
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

import static org.codehaus.groovy.grails.commons.GrailsClassUtils.isAssignableOrConvertibleFrom

/**
 * tooling methods to retrieve and build meta data for domains to use in reporting
 *
 * @author Joshua Burnett
 */
//FIXME this should be merged in with the BeanPathTools in angle-grinder and moved to the DAO plugin
@CompileStatic
class DomainMetaUtils {

    //def grailsApplication

    /**
     * Build property metadata from the list of fields for the domain class. Allows nested properties.
     * returns a Map keyed by the list of fields
     *
     * @param domainClass the base domain to use
     * @param fields the list of properties
     * @param columnTitles a list of overrides for titel labels, keyed off of the fields list
     * @return
     */
    @CompileDynamic
    static Map<String,FieldMetadata> getFieldMetadata(GrailsDomainClass domainClass, List<String> fields, Map columnTitles = null) {
        Map<String,FieldMetadata> columns = [:]
        fields.each { propertyName ->
            //Map colProps = [fieldName:propertyName]
            FieldMetadata fld = new FieldMetadata(propertyName)

            GrailsDomainClassProperty property = domainClass.getPropertyByName(propertyName)

            Class dcPropType = property.type
            Class propertyType
            //assert Boolean.class.getName() == "foo"
            List<Class> ctypes = [Boolean,Integer,Long,BigDecimal,Short,Float,Double,Byte,Character,Date,String,List]

            for(Class clazz : ctypes){
                if(isAssignableOrConvertibleFrom(clazz, dcPropType)){
                    //assert clazz.getName() == "foo"
                    //colProps['clazz'] = clazz.class
                    //TODO implement type short population
                    fld.typeClass = clazz
                    fld.typeClassName = clazz.getName()
                    break
                }
            }
            //set default width of 4 as a ratio
            fld.width = 20
            switch (fld.typeClass){
                case [Boolean,Character,Byte]:
                    fld.width = 15
                    break
                case [Date,Integer,Short]:
                    fld.width = 20
                    break
                case [String]:
                    fld.width = 30
            }
            String colTitle = columnTitles?.get(propertyName)
            if(colTitle){
                fld.title = columnTitles?.get(propertyName)
            }else{
                fld.title = getNaturalTitle(propertyName)
            }

            columns[propertyName] = fld
        }
        return columns
    }

    /**
     * finds domain using either a simple name like "Product" or fully qualified name "com.foo.Product"
     * @param name
     * @return
     */
    @CompileDynamic
    static GrailsDomainClass findDomainClass(String name){
        if(name.indexOf('.') == -1){
            return Holders.grailsApplication.domainClasses.find {
                it.clazz.simpleName.toLowerCase() == name.toLowerCase()
            }
        }else{
            return Holders.grailsApplication.getDomainClass(name)
        }

    }

    /**
     * Converts a property name into its natural title equivalent eg ('firstName' becomes 'First Name')
     * customer.name = Customer (drops off the .name). product.code = Product Code ,
     * @return
     */
    @CompileDynamic
    static String getNaturalTitle(String text){
        text = text.endsWith(".name") ? text[0..<text.lastIndexOf('.')] : text
        // make foo.bar into fooBar so we can pass it through the getNaturalName
        text = text.replaceAll( "(\\.)([A-Za-z0-9])", { Object[] it -> it[2].toUpperCase() } )
        //text
        return GrailsNameUtils.getNaturalName(text)

    }


    /**
     * Orders by the specified property name and direction
     * takes invoice.customer.name and builds a closure that looke like
     *
     * invoice {
     *    customer {
     *       order(name)
     *    }
     * }
     * and then calls the that closure on this.
     *
     * @param propertyName The property name to order by
     * @param direction Either "asc" for ascending or "desc" for descending
     * @param forceSuper use original order(...) from HibernateCriteriaBuilder
     *
     * @return A Order instance
     */
    @CompileDynamic
    static Closure orderNested(String propertyName, String direction) {
        if(!propertyName.contains('.')) {
            return { order(propertyName, direction) }
        }
        else {
            def props = propertyName.split(/\./) as List
            def last = props.pop()
            Closure toDo = { order(last, direction) }
            Closure newOrderBy = props.reverse().inject(toDo) { acc, prop ->
                { -> "$prop"(acc) }
            }
            return newOrderBy
        }
    }

    @CompileDynamic
    static Closure orderNested(List groupFields, callingDelegate) {
        return {
            groupFields.each {
                def o = orderNested(it,'asc')
                o.delegate = callingDelegate
                o()
            }
        }

    }

}
