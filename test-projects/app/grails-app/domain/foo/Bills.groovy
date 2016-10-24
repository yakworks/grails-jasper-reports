package foo

import ar.com.fdvs.dj.core.layout.HorizontalBandAlignment
import ar.com.fdvs.dj.domain.AutoText

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
