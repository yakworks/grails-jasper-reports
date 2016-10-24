package foo

class Bills {

    Customer customer
    Product product
    String color
    BigDecimal amount
    Long qty
    Date tranDate
    Boolean isPaid = false

    Map ext

    static transients = ['tranProp']

    String getTranProp(){
        "tp"
    }
}
