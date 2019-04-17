/**
 * @author Alejandro Gomez (alejandro.gomez@fdvsolutions.com)
 * Date: 21-dic-2008
 * Time: 19:42:00
 */
package foo
class NotNullPutAt {

    static void putAt(Object self, String prop, Object value) {
        if (value != null && (!(value instanceof ConfigObject) || (value.size() > 0))) {
            self.setProperty(prop, value)
        }
    }
}
