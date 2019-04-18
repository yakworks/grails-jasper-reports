package nine.jasper

import groovy.transform.CompileStatic
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.JRException
import net.sf.jasperreports.engine.JRField

import javax.sql.DataSource

/**
 * Simple container class so we can pass the jdbc connection around as a JRDataSource
 */
@CompileStatic
class JRDataSourceJDBC implements JRDataSource {

    DataSource dataSource

    public JRDataSourceJDBC(DataSource dataSource) {
        this.dataSource = dataSource
    }

    @Override
    boolean next() throws JRException {
        throw new JRException("Not implemented")
    }

    @Override
    Object getFieldValue(JRField jrField) throws JRException {
        throw new JRException("Not implemented")
    }

}
