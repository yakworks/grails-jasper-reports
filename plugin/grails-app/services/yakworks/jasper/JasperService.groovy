/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.jasper

import groovy.transform.CompileDynamic

import net.sf.jasperreports.engine.JasperReport
import yakworks.jasper.spring.JasperReportDef
import yakworks.jasper.spring.JasperView
import yakworks.jasper.spring.JasperViewResolver

/**
 * @author Sudhir Nimavat
 */
@CompileDynamic
class JasperService {
    static transactional = false

    JasperViewResolver jasperViewResolver

    /**
     * Returns JasperView for given report file name
     *
     * @param name Report file name
     * @return JasperView
     */
    public JasperView getView(String name) {
        return (JasperView) jasperViewResolver.resolveViewName(name, null)
    }

    /**
     * Returns JasperReport instance for given report file name
     *
     * @param name Report file name
     * @return JasperReport
     */
    public JasperReport getJasperReport(String name) {
        return getView(name).report
    }

    /**
     * Renders the jasper report for given JasperReportDef and returns ByteArrayOutputStream
     *
     * @param opts JasperReportDef
     * @return ByteArrayOutputStream
     */
    public ByteArrayOutputStream generateReport(JasperReportDef opts) {
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        JasperView view = getView(opts.name)
        Map model = [:]
        model.putAll(opts.parameters)
        model.format = opts.fileFormat
        if (opts.reportData != null) model.data = opts.reportData
        view.render(model, out)
        out.flush()
        return out
    }

}
