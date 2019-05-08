/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.reports

import groovy.transform.CompileStatic

/**
 * Stores basic information about a Field (property or column)
 */

@CompileStatic
class FieldMetadata implements Serializable {

    public FieldMetadata() {}

    public FieldMetadata(String property) {
        this.property = property
    }
    /**
     * the property or nested path for object such as "amount" or "customer.name"
     */
    String property

    /**
     * the short name, lower case, for the data type
     * object, array, string, number, boolean, or null as normal JSON defaults
     * we also add
     * date, datetime, percent, currency
     */
    String type

    /**
     * the java long qualified class name
     * ex: java.lang.String, java.math.BigDecimal etc...
     */
    Class typeClass

    /**
     * the java long qualified class name
     * ex: java.lang.String, java.math.BigDecimal etc...
     */
    String typeClassName

    /**
     * the display label for this property
     */
    String title

    /**
     * override the format to use special
     */
    String format

    /**
     * override on how to align this. left, right, middle
     */
    String align

    /**
     * hide this by default
     */
    Boolean hide

    /**
     * the width to override
     */
    Integer width

    /**
     * a builder reference that can be used, depending on the implementation
     */
    Object builder

    Boolean isBooleanType() {
        if (typeClass == Boolean || typeClassName == 'java.lang.Boolean') {
            return true
        }
    }

}
