package foo

/**
 * Created by basejump on 10/15/16.
 */
class Product {
    ProductGroup group
    String num
    String name

    static constraints = {
        num nullable:true
    }
}
