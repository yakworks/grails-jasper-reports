package nine.reports

import grails.util.GrailsNameUtils
import grails.util.Holders
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association

import static grails.util.GrailsClassUtils.isAssignableOrConvertibleFrom

/**
 * tooling methods to retrieve and build meta data for domains to use in reporting
 *
 * @author Joshua Burnett
 */
//FIXME this should be merged in with the BeanPathTools in angle-grinder and moved to the DAO plugin
//FIXME for Grails 3 see this commit to more to PersitentEntity instead of GrailsDomainClass
//https://github.com/grails/grails-core/commit/d3cbd999010666bfd956068e826676632ae3fa17
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
    @SuppressWarnings(['ThrowRuntimeException'])
    @CompileDynamic
    static Map<String, FieldMetadata> getFieldMetadata(PersistentEntity domainClass, List<String> fields, Map columnTitles = null) {
        Map<String, FieldMetadata> columns = [:]
        fields.each { propertyName ->
            //Map colProps = [fieldName:propertyName]
            FieldMetadata fld = new FieldMetadata(propertyName)

            PersistentProperty property = getPropertyByName(domainClass, propertyName)

            if (!property) {
                throw new RuntimeException("Invalid property name $propertyName for domain $domainClass.name")
            }

            Class dcPropType = property.type
            Class propertyType
            //assert Boolean.class.getName() == "foo"
            List<Class> ctypes = [Boolean, Integer, Long, BigDecimal, Short, Float, Double, Byte, Character, Date, String, List]

            for (Class clazz : ctypes) {
                if (isAssignableOrConvertibleFrom(clazz, dcPropType)) {
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
            switch (fld.typeClass) {
                case [Boolean, Character, Byte]:
                    fld.width = 15
                    break
                case [Date, Integer, Short]:
                    fld.width = 20
                    break
                case [String]:
                    fld.width = 30
            }
            String colTitle = columnTitles?.get(propertyName)
            if (colTitle) {
                fld.title = columnTitles?.get(propertyName)
            } else {
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
    static PersistentEntity findDomainClass(String name) {

        if (name.indexOf('.') == -1) {
            String propertyName = GrailsNameUtils.getPropertyName(name)
            Holders.grailsApplication.mappingContext.persistentEntities.find{
                it.decapitalizedName == propertyName
            }
        } else {
            return Holders.grailsApplication.mappingContext.getPersistentEntity(name)
        }

    }

    @SuppressWarnings(['ThrowRuntimeException'])
    //See https://github.com/grails/grails-core/issues/10978
    static PersistentProperty getPropertyByName(PersistentEntity entity, String name) {
        if (name?.contains(".")) {
            int indexOfDot = name.indexOf('.')
            String basePropertyName = name.substring(0, indexOfDot)
            PersistentProperty property = entity.getPropertyByName(basePropertyName)
            if (property instanceof Association) {
                PersistentEntity association = ((Association) property).getAssociatedEntity()
                String restOfPropertyName = name.substring(indexOfDot + 1)
                return getPropertyByName(association, restOfPropertyName)
            } else {
                throw new RuntimeException("Property $basePropertyName of class $entity.javaClass.name is not an association")
            }

        } else {
            return entity.getPropertyByName(name)
        }
    }

    /**
     * Converts a property name into its natural title equivalent eg ('firstName' becomes 'First Name')
     * customer.name = Customer (drops off the .name). product.code = Product Code ,
     * @return
     */
    @CompileDynamic
    static String getNaturalTitle(String text) {
        text = text.endsWith(".name") ? text[0..<text.lastIndexOf('.')] : text
        // make foo.bar into fooBar so we can pass it through the getNaturalName
        text = text.replaceAll("(\\.)([A-Za-z0-9])") { Object[] it -> it[2].toUpperCase() }
        //text
        return GrailsNameUtils.getNaturalName(text)

    }

    /**
     * Orders by the specified property name and direction
     * takes invoice.customer.name and builds a closure that looke like
     *
     * invoice {*    customer {*       order(name)
     *}*}* and then calls the that closure on this.
     *
     * @param propertyName The property name to order by
     * @param direction Either "asc" for ascending or "desc" for descending
     * @param forceSuper use original order(...) from HibernateCriteriaBuilder
     *
     * @return A Order instance
     */
    @CompileDynamic
    static Closure orderNested(String propertyName, String direction) {
        if (propertyName.contains('.')) {
            def props = propertyName.split(/\./) as List
            def last = props.pop()
            Closure toDo = { order(last, direction) }
            Closure newOrderBy = props.reverse().inject(toDo) { acc, prop ->
                { -> "$prop"(acc) }
            }
            return newOrderBy
        } else {
            return { order(propertyName, direction) }

        }
    }

    @CompileDynamic
    static Closure orderNested(List groupFields, Closure callingDelegate) {
        return {
            groupFields.each {
                def o = orderNested(it, 'asc')
                o.delegate = callingDelegate
                o()
            }
        }

    }

}
