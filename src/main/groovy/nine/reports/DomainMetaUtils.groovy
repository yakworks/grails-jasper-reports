package nine.reports


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
class DomainMetaUtils {

    //def grailsApplication

    static Map<String,Map> buildColumnMap(GrailsDomainClass domainClass, List<String> fields, Map config = null) {
        Map columns = [:]
        fields.each { propertyName ->
            Map<String,Object> colProps = [propertyName:propertyName]

            GrailsDomainClassProperty property = domainClass.getPropertyByName(propertyName)

            Class dcPropType = property.type
            Class propertyType
            //assert Boolean.class.getName() == "foo"
            List<Class> ctypes = [Boolean,Integer,BigDecimal,Short,Float,Double,Byte,Character,Date,String,List]

            for(Class clazz : ctypes){
                if(isAssignableOrConvertibleFrom(clazz, dcPropType)){
                    //assert clazz.getName() == "foo"
                    colProps['clazz'] = clazz
                    colProps['className'] = clazz.getName()
                    break
                }
            }
            //set it up as a toString
//            if(!colProps['clazz']){
//                colProps['clazz'] = String
//                colProps['className'] = String.getName()
//            }

            colProps['title'] = config?.columnTitles?."${propertyName}" ?: property.naturalName

            columns[propertyName] = colProps
        }
        return columns
    }

}
