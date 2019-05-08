/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package nine.jasper.gorm

import groovy.transform.CompileDynamic

import net.sf.jasperreports.engine.JRException
import net.sf.jasperreports.engine.JRField
import net.sf.jasperreports.engine.JRRewindableDataSource

/**
 * A data source implementation that wraps a collection of JavaBean objects.
 * <p>
 * It is common to access application data through object persistence layers like EJB,
 * Hibernate, or JDO. Such applications may need to generate reports using data they
 * already have available as arrays or collections of in-memory JavaBean objects.
 * </p><p>
 * This JavaBean-compliant data source can be used when data comes in a
 * <code>java.util.Collection</code> of JavaBean objects.
 *
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 */
//FIXME Finish this
@CompileDynamic
public class GormJasperDataSource implements JRRewindableDataSource {

    private Collection<?> data
    private Iterator<?> iterator
    private Object currentBean

    public GormJasperDataSource(Collection<?> beanCollection) {
        this(beanCollection, true)
    }

    /**
     *
     */
    public GormJasperDataSource(Collection<?> beanCollection, boolean isUseFieldDescription) {
        super(isUseFieldDescription)

        this.data = beanCollection

        if (this.data != null) {
            this.iterator = this.data.iterator()
        }
    }

    @Override
    public boolean next() {
        boolean hasNext = false

        if (this.iterator != null) {
            hasNext = this.iterator.hasNext()

            if (hasNext) {
                this.currentBean = this.iterator.next()
            }
        }

        return hasNext
    }

    @Override
    public Object getFieldValue(JRField field) throws JRException {
        return getFieldValue(currentBean, field)
    }

    @Override
    public void moveFirst() {
        if (this.data != null) {
            this.iterator = this.data.iterator()
        }
    }

    /**
     * Returns the underlying bean collection used by this data source.
     *
     * @return the underlying bean collection
     */
    public Collection<?> getData() {
        return data
    }

    /**
     * Returns the total number of records/beans that this data source
     * contains.
     *
     * @return the total number of records of this data source
     */
    public int getRecordCount() {
        return data == null ? 0 : data.size()
    }

//    /**
//     * Clones this data source by creating a new instance that reuses the same
//     * underlying bean collection.
//     *
//     * @return a clone of this data source
//     */
//    public JRBeanCollectionDataSource cloneDataSource()
//    {
//        return new JRBeanCollectionDataSource(data)
//    }
}
