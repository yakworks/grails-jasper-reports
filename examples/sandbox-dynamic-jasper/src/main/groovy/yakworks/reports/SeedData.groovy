package yakworks.reports

import foo.Bills
import foo.Customer
import foo.Product
import foo.ProductGroup

/**
 * Created by basejump on 10/18/16.
 */
class SeedData {

    static Random rand = new Random()

    static seed(){
        def fooGrp = new ProductGroup(name:"Foo Group").save()
        def barGrp = new ProductGroup(name:"Bar Group").save()

        def custBob = new Customer(num:"BP-777",name:"Bob's Plumbing").save()
        def custJoe = new Customer(num:"JS-888",name:"Joe's Septic Cleaning and Computer Repair").save()

        (1..7).each {
            def prod = new Product(name:"Fooinator-$it",group:fooGrp).save()
            new Bills(customer: custBob, product: prod, amount: 3.00, qty:it, color:randomFoo(),
                isPaid:randomBool(), tranDate:randomDate()).save()
        }
        (1..5).each {
            def prod = new Product(name:"Bargoober-$it",group:barGrp).save()
            new Bills(customer: custBob, product: prod, amount: 3.00, qty:it, color:randomFoo(),
                isPaid:randomBool(), tranDate:randomDate()).save()
        }
        (1..7).each {
            def prod = new Product(name:"Fooinator-$it",group:fooGrp).save()
            new Bills(customer: custJoe, product: prod, amount: 5.00, qty:it, color:randomFoo(),
                isPaid:randomBool(), tranDate:randomDate()).save()
        }
        (1..5).each {
            def prod = new Product(name:"Bargoober-$it",group:barGrp).save()
            new Bills(customer: custJoe, product: prod, amount: 5.00, qty:it, color:randomFoo(),
                isPaid:randomBool(), tranDate:randomDate()).save(flush:true)
        }
    }

    static Date randomDate() {
        Date dateFrom = Date.parse('yyyy-MM-dd', '2016-01-01')
        Range<Date> range = dateFrom..Date.parse('yyyy-MM-dd', '2016-12-31')
        def addPart = new Random().nextInt(range.to - range.from + 1)
        println addPart
        Date dt = dateFrom + addPart
        println dt
        return dt
    }

    static String randomFoo(){
        int num = rand.nextInt(3)
        switch (num) {
            case 0:return "Blue"
            case 1:return "Green"
            case 2:return "Red"
        }
    }

    static Boolean randomBool() {
        Boolean tf = rand.nextBoolean()
        println tf
        return tf
    }

}
