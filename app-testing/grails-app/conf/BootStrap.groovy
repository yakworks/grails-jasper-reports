import foo.*

class BootStrap {
    Random rand = new Random()

    def init = { servletContext ->

        def fooGrp = new ProductGroup(name:"Foo Group").save()
        def barGrp = new ProductGroup(name:"Bar Group").save()

        def custBob = new Customer(num:"BP-7767",name:"Bob's Plumbing").save()
        def custJoe = new Customer(num:"BP-7767",name:"Joe's Septic and Computer Repairp").save()

        Random rand = new Random()

        (1..7).each {
            def prod = new Product(name:"Fooinator-$it",group:fooGrp).save()
            new Bills(customer: custBob, product: prod, amount: 5.00, qty:it, fooGroup:randomFoo()).save()
        }
        (1..5).each {
            def prod = new Product(name:"Bargoober-$it",group:barGrp).save()
            new Bills(customer: custBob, product: prod, amount: 5.00, qty:it, fooGroup:randomFoo()).save()
        }
        (1..7).each {
            def prod = new Product(name:"Fooinator-$it",group:fooGrp).save()
            new Bills(customer: custJoe, product: prod, amount: 5.00, qty:it, fooGroup:randomFoo()).save()
        }
        (1..5).each {
            def prod = new Product(name:"Bargoober-$it",group:barGrp).save()
            new Bills(customer: custJoe, product: prod, amount: 5.00, qty:it, fooGroup:randomFoo()).save()
        }

    }
    def destroy = {
    }

    String randomFoo(){
        int num = rand.nextInt(3)
        switch (num) {
            case 0:return "Blue"
            case 1:return "Green"
            case 2:return "Red"
        }
    }
}
