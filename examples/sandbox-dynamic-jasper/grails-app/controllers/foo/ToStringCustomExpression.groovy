package foo

import ar.com.fdvs.dj.domain.CustomExpression

/**
 * @author Alejandro Gomez (alejandro.gomez@fdvsolutions.com)
 * Date: 30-dic-2008
 * Time: 16:47:19
 */

class ToStringCustomExpression implements CustomExpression {

    def fieldName

    def ToStringCustomExpression(fieldName) {
        this.fieldName = fieldName;
    }

    public Object evaluate(Map fields, Map variables, Map parameters) {
        fields[(fieldName)].toString()
    }

    public String getClassName() {
        String.name
    }

}
