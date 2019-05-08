package yakworks.reports.dynamic

import foo.Bills
import foo.Customer
import foo.Product
import foo.ProductGroup
import grails.test.hibernate.HibernateSpec
import grails.testing.gorm.DataTest
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import yakworks.jasper.dynamic.DynamicReportsService
import yakworks.reports.DomainMetaUtils
import yakworks.reports.SeedData
import spock.lang.Ignore
import spock.lang.Shared

import static net.sf.dynamicreports.report.builder.DynamicReports.export

//@Domain([ProductGroup,Bills,Customer,Product])
class DynamicReportsServiceSpec extends HibernateSpec implements DataTest {

    List<Class> getDomainClasses() { [ProductGroup, Customer, Bills, Product] }

    @Shared
    DynamicReportsService dynamicReportsService = new DynamicReportsService()
    /**
     * TODO
     * - links to pages
     * - subtotals on top
     * - incorporate patterns from ??
     * - better grouping for id and name concepts
     * - embed fonts into pdf
     * - how to use font awesome icons
     *
     */
    static folder = new File("build/jasper-tests/DynamicReportsServiceSpec/");

    void setupSpec() {
        mockDomains(ProductGroup, Customer, Bills, Product)

        dynamicReportsService = new DynamicReportsService()
        dynamicReportsService.grailsApplication = grailsApplication
        if (!folder.exists()) folder.mkdirs();

    }

    List getData(orderBy) {
        return Bills.createCriteria().list {
            def o = DomainMetaUtils.orderNested(orderBy, delegate)
            o()
        }
    }

    def saveToFiles(JasperReportBuilder dr, fname) {
        //dr.toJrXml(new FileOutputStream( new File(folder,"${fname}.jrxml")))
        long start = System.currentTimeMillis();
        dr.toPdf(new FileOutputStream(new File(folder, "${fname}.pdf")))
        System.err.println("PDF time : " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        dr.ignorePagination()//.ignorePageWidth() //.setPageFormat(PageType.LETTER, PageOrientation.LANDSCAPE)
                .rebuild()
        //.toHtml(new FileOutputStream( new File(folder,"basic.html")))
        def htmlRpt = new File(folder, "${fname}.html")
        dr.toHtml(
                export.htmlExporter(new FileOutputStream(htmlRpt))
                        .setHtmlHeader(getHTMLHeader())
                        .setHtmlFooter(HTMLFooter) //.setFramesAsNestedTables(true).setZoomRatio(200)
        )
        System.err.println("HTML time : " + (System.currentTimeMillis() - start));

        dr.toJrXml(new FileOutputStream(new File(folder, "${fname}.jrxml")))

        "open build/jasper-tests/DynamicReportsServiceSpec/${fname}.html".execute()
    }

    @Ignore("https://github.com/yakworks/grails-jasper-reports/issues/11")
    void "simple sanity check"() {
        when:
        Map cfg = [
                domain                  : 'Bills',
                fields                  : ['customer.name', 'product.group.name', 'color', 'product.name', 'isPaid', 'tranDate', 'qty', 'amount'],
                columns                 : ['tranProp': 'From Getter'],
                groups                  : ['customer.name', 'product.group.name', 'color'],
                subtotals               : [qty: "sum", amount: "sum"], //put these on all the group summaries
                subtotalsHeader         : [amount: "sum"], //put these on all the group summaries
                columnHeaderInFirstGroup: true, //for each new primary group value the column header will be reprinted, if false they occur once per page
                groupTotalLabels        : true, //puts a group total label on the subtotal footers
                //highlightDetailOddRows:true,
                showGridLines           : true,
//            tableOfContents:true,
//            landscape:true //short cut for pageFormat:[size:'letter', landscape:true]
//            pageFormat:[size:'letter', landscape:true] // [size:'letter',landscape:false] is the default. Size can be letter,legal, A0-C10, basically any static in net.sf.dynamicreports.report.constant.PageType
        ]
        def dr = dynamicReportsService.buildDynamicReport(cfg)
        SeedData.seed()
        def list = getData(cfg.groups)
        dr.setDataSource(list)

        then:
        assert dr

        //dr.setPageFormat(PageType.LETTER, PageOrientation.LANDSCAPE)
        saveToFiles(dr, 'basic')

        //dr.show()
        //sleep(5000)
    }

//    void "complex"() {
//        when:
//        Map rptCfg = [
//            domain: 'Bills',
//            fields: ['customer.name', 'color', 'product.group.name', 'product.name', 'isPaid', 'tranProp', 'tranDate', 'qty', 'amount'],
//            columns: [
//                'customer.name': [
//                    title:'Cust',
//                    width:10,
//                    pattern:
//                ],
////            groups: [
////                'customer.num':[
////                    show:''
////                ], 'color', 'product.group.name'],
//            subtotals: [qty: "sum", amount: "sum"], //put these on all the group summaries
//            subtotalsHeader         : [amount: "sum"], //put these on all the group summaries
//            columnHeaderInFirstGroup: true, //for each new primary group value the column header will be reprinted, if false they occur once per page
//            groupTotalLabels        : true, //puts a group name total label on the subtotal footers
//            highlightDetailOddRows:true,
//            showGridLines:true,
//            tableOfContents         : true,
//            landscape               : true //short cut for pageFormat:[size:'letter', landscape:true]
//            //pageFormat:[size:'letter', landscape:true] // [size:'letter',landscape:false] is the default. Size can be letter,legal, A0-C10, basically any static in net.sf.dynamicreports.report.constant.PageType
//        ]
//        def dr = dynamicReportsService.buildDynamicReport(rptCfg)
//        if(!Bills.count())
//            SeedData.seed()
//
//        def list = getData(rptCfg.groups)
//        dr.setDataSource(list)
//
//        then:
//        assert dr
//
//        //dr.setPageFormat(PageType.LETTER, PageOrientation.LANDSCAPE)
//        saveToFiles(dr,'complex')
//
//        //dr.show()
//        //sleep(5000)
//    }

    String HTMLHeader = ('''
<html>
    <head>
        <title></title>
        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>
        <style type=\"text/css\">
            a {text-decoration: none}
        </style>
    </head>
    <body text=\"#000000\" link=\"#000000\" alink=\"#000000\" vlink=\"#000000\">
        <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">
            <tr>
                <td width="10%">&nbsp;</td>
                <td align=\"center\">
 ''').toString()
    String HTMLFooter = '''
                </td>
                <td width="10%">&nbsp;</td>
            </tr>
        </table>
    </body>
</html>
'''
}
