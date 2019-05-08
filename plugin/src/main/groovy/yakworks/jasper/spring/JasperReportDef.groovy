/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.jasper.spring

import groovy.transform.CompileStatic

import yakworks.reports.ReportFormat

/**
 * An abstract representation of a Jasper report.
 */
@CompileStatic
class JasperReportDef implements Serializable {

    /**
     * The name of the report file
     * The file can be in jrxml- or jasper-format.
     */
    String name

    /**
     * The data source used to fill the report.
     * <p>
     * This is a list of java beans.
     */
    Collection reportData

    /**
     * The target file format.
     */
    ReportFormat fileFormat = ReportFormat.PDF

    /**
     * The generated report as OutputStream.
     */
    ByteArrayOutputStream contentStream

    /**
     * Additional parameters.
     */
    Map parameters = [:]

    /**
     * Locale setting.
     */
    Locale locale

    /**
     * Add a parameter to the parameter map.
     * @param key , the key
     * @param value , the value
     */
    void addParameter(Object key, Object value) {
        parameters.put(key, value)
    }
}
