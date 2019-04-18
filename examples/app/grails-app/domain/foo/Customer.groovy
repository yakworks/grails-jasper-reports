package foo

import grails.compiler.GrailsCompileStatic

/**
 * Created by basejump on 10/15/16.
 */
@GrailsCompileStatic
class Customer {
    String num
    String name

    static constraints = {
        num nullable:true
    }
}
