package dynamicjasper

import ar.com.fdvs.dj.core.DynamicJasperHelper
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager
import ar.com.fdvs.dj.domain.AutoText
import ar.com.fdvs.dj.domain.DJCalculation
import ar.com.fdvs.dj.domain.DynamicReport
import ar.com.fdvs.dj.domain.Style
import ar.com.fdvs.dj.domain.builders.ColumnBuilder
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder
import ar.com.fdvs.dj.domain.builders.GroupBuilder
import ar.com.fdvs.dj.domain.constants.*
import ar.com.fdvs.dj.domain.entities.DJGroup
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn
import ar.com.fdvs.dj.domain.entities.columns.PropertyColumn
import ar.com.fdvs.dj.util.SortUtils
import domain.Product
import domain.TestRepositoryProducts
import foo.ReportsController
import grails.converters.JSON
import grails.plugin.viewtools.ViewResourceLocator
import grails.testing.spring.AutowiredTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.util.logging.Slf4j
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.JasperReport
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
import yakworks.jasper.JasperUtils
import yakworks.jasper.spring.JasperViewResolver
import spock.lang.Specification

import java.awt.Color

/**
 * Playground for various features.
 */
@Slf4j
class GroupsReportsSpec extends Specification implements ControllerUnitTest<ReportsController>,AutowiredTest {

    Closure doWithSpring() {
        return TestAppCtx.doWithSpring
    }

    ViewResourceLocator jasperViewResourceLocator //= ViewResourceLocator.mockForTest()
    JasperViewResolver jasperViewResolver

    static List<Map> dataList = [
            [city:"Berne", id:22, name:"Bill Ott", street:"250 - 20th Ave.", country:[name:"US"]],
            [city:"Chicago", id:1, name:"Joshua Burnett", street:"22 3rd", country:[name:"US"]]
    ]
    Map model = [
        ReportTitle:"Test Report",
        data:dataList
    ]

    JasperPrint jp;
    JasperReport jr;
    Map rparams = new HashMap();
    DynamicReport dr;

   // Map parameters = ["ReportTitle":"Test Report", "DataFile": "Foo"]

    String viewName = "/test/testme.jrxml"

    void setup(){
        jasperViewResourceLocator = applicationContext.getBean('jasperViewResourceLocator')
        jasperViewResolver = applicationContext.getBean('jasperViewResolver')
    }


    void "format in the model"() {
        when:
        testReport()

        then:
        assert 1==1
    }

    public void testReport() throws Exception {
        dr = buildReport();

        /**
         * Get a JRDataSource implementation
         */
        JRDataSource ds = getDataSource();


        /**
         * Creates the JasperReport object, we pass as a Parameter
         * the DynamicReport, a new ClassicLayoutManager instance (this
         * one does the magic) and the JRDataSource
         */
        jr = DynamicJasperHelper.generateJasperReport(dr, new ClassicLayoutManager(), rparams);

        /**
         * Creates the JasperPrint object, we pass as a Parameter
         * the JasperReport object, and the JRDataSource
         */

        if (ds != null)
            jp = JasperFillManager.fillReport(jr, rparams, ds);
        else
            jp = JasperFillManager.fillReport(jr, rparams);


        exportReport();


    }

    protected void exportReport() throws Exception {
        ReportExporter.exportReport(jp, "test-reports/" + this.getClass().getSimpleName() + ".pdf");
        exportToJRXML();
        JasperUtils.createExporterHTML(jp,new File("test-reports/" + this.getClass().getSimpleName() + ".html ")).exportReport()
        ReportExporter.exportReport(jp, "test-reports/" + this.getClass().getSimpleName() + ".pdf");
    }

    protected void exportToJRXML() throws Exception {
        if (this.jr != null){
            DynamicJasperHelper.generateJRXML(this.jr, "UTF-8","test-reports/" + this.getClass().getSimpleName() + ".jrxml");

        } else {
            DynamicJasperHelper.generateJRXML(this.dr, new ClassicLayoutManager(), this.rparams, "UTF-8","test-reports/" + this.getClass().getSimpleName() + ".jrxml");
        }
    }

    public DynamicReport buildReport() throws Exception {

        Style detailStyle = new Style();
        detailStyle.font = Font.MONOSPACED_MEDIUM
        detailStyle.setVerticalAlign(VerticalAlign.TOP);

        Style groupTitleStyle = new Style();
        groupTitleStyle.setFont(Font.ARIAL_BIG);

        Style col2Style = new Style();
        col2Style.setFont(Font.ARIAL_MEDIUM_BOLD);
        col2Style.setBorderBottom(Border.THIN());
        col2Style.setVerticalAlign(VerticalAlign.TOP);
        //col2Style.stretchWithOverflow = false

        Style headerStyle = new Style();
        headerStyle.with{
            setFont(Font.ARIAL_MEDIUM_BOLD)
            setBackgroundColor(Color.gray)
            setTextColor(Color.white)
            setHorizontalAlign(HorizontalAlign.CENTER)
            setVerticalAlign(VerticalAlign.MIDDLE)
            setTransparency(Transparency.OPAQUE)
            paddingTop = 0
            paddingBottom = 0
        }

        Style g1VariablesStyle = new Style();
        g1VariablesStyle.setFont(Font.ARIAL_MEDIUM_BOLD);
        g1VariablesStyle.setBorderTop(Border.THIN());
        g1VariablesStyle.setHorizontalAlign(HorizontalAlign.RIGHT);
        g1VariablesStyle.setVerticalAlign(VerticalAlign.MIDDLE);
        g1VariablesStyle.setTextColor(new Color(50,50,150));

        Style g2VariablesStyle = new Style();
        g2VariablesStyle.setFont(Font.ARIAL_MEDIUM_BOLD);
        g2VariablesStyle.setTextColor(new Color(150,150,150));
        g2VariablesStyle.setHorizontalAlign(HorizontalAlign.RIGHT);
        g2VariablesStyle.setVerticalAlign(VerticalAlign.MIDDLE);


        Style titleStyle = new Style();
        titleStyle.setFont(new Font(14, Font._FONT_ARIAL, true));
        titleStyle.setPaddingBottom(0)
        titleStyle.setVerticalAlign(VerticalAlign.TOP);
        //titleStyle.
        //titleStyle.setTransparency(Transparency.OPAQUE);

        Style importeStyle = new Style();
        importeStyle.setHorizontalAlign(HorizontalAlign.RIGHT);
        Style oddRowStyle = new Style();
        oddRowStyle.setBorder(Border.NO_BORDER());
        oddRowStyle.setBackgroundColor(Color.LIGHT_GRAY);
        oddRowStyle.setTransparency(Transparency.OPAQUE);

        DynamicReportBuilder drb = new DynamicReportBuilder();
        Integer margin = new Integer(20);
        drb
                .setTitleStyle(titleStyle)
                .setTitle("November 2016 sales report")					//defines the title of the report
                .setSubtitle("The items in this report correspond "
                +"to the main products: DVDs, Books, Foods and Magazines")
                //.setDetailHeight(new Integer(15))
                //.setHeaderHeight(new Integer(15))
                //.setHeaderVariablesHeight(new Integer(15))
                //.setHeaderVariablesHeight(15)
                .setRightMargin(margin)
                .setLeftMargin(margin)
                .setTopMargin(margin)
                .setBottomMargin(margin)
                .setGrandTotalLegend("Grand Total")
                .setGrandTotalLegendStyle(g2VariablesStyle)
                //.setDefaultStyles(titleStyle, null, headerStyle, detailStyle)
                .setPrintColumnNames(false)
                .setAllowDetailSplit(false)
                //.addImageBanner("src/test/resources/images/logo_fdv_solutions_60.jpg", new Integer(100), new Integer(30), ImageBanner.ALIGN_RIGHT)
                .setOddRowBackgroundStyle(oddRowStyle)
                .setPrintBackgroundOnOddRows(false)



        AbstractColumn columnState = ColumnBuilder.getNew()
                .setColumnProperty("state", String.class.getName())
                .setTitle("")
                .setWidth(0)
                .setStyle(titleStyle)//.setHeaderStyle(groupTitleStyle)
                .build();

        AbstractColumn columnBranch = ColumnBuilder.getNew()
                .setColumnProperty("branch", String.class.getName())
                .setTitle("").setWidth(50)
                .setStyle(col2Style).setHeaderStyle(col2Style)
                .build();

        AbstractColumn columnaProductLine = ColumnBuilder.getNew()
                .setColumnProperty("productLine", String.class.getName())
                .setTitle("Product Line").setWidth(new Integer(85))
                .setStyle(detailStyle).setHeaderStyle(headerStyle)
                .build();

        AbstractColumn columnaItem = ColumnBuilder.getNew()
                .setColumnProperty("item", String.class.getName())
                .setTitle("Item").setWidth(new Integer(85))
                .setStyle(detailStyle).setHeaderStyle(headerStyle)
                .build();

        AbstractColumn columnCode = ColumnBuilder.getNew()
                .setColumnProperty("id", Long.class.getName())
                .setTitle("ID").setWidth(new Integer(40))
                .setStyle(importeStyle).setHeaderStyle(headerStyle)
                .build();

        AbstractColumn columnaQuantity = ColumnBuilder.getNew()
                .setColumnProperty("quantity", Long.class.getName())
                .setTitle("Quantity").setWidth(new Integer(80))
                .setStyle(importeStyle).setHeaderStyle(headerStyle)
                .build();

        AbstractColumn columnAmount = ColumnBuilder.getNew()
                .setColumnProperty("amount", Float.class.getName())
                .setTitle("Amount").setWidth(new Integer(90)).setPattern("\$ 0.00")
                .setStyle(importeStyle).setHeaderStyle(headerStyle)
                .build();

        GroupBuilder gb1 = new GroupBuilder();

//		 define the criteria column to group by (columnState)
        DJGroup g1 = gb1.setCriteriaColumn((PropertyColumn) columnState)
                .addFooterVariable(columnAmount,DJCalculation.SUM,g1VariablesStyle)
                .addFooterVariable(columnaQuantity,DJCalculation.SUM,g1VariablesStyle)
                .addHeaderVariable(columnAmount,DJCalculation.SUM,titleStyle)
                .setGroupLayout(GroupLayout.VALUE_IN_HEADER_WITH_HEADERS_AND_COLUMN_NAME)
                .setHeaderHeight(15)
                .build();

        GroupBuilder gb2 = new GroupBuilder(); // Create another group (using another column as criteria)
        DJGroup g2 = gb2.setCriteriaColumn((PropertyColumn) columnBranch) // and we add the same operations for the columnAmount and
                .addFooterVariable(columnAmount,DJCalculation.SUM,g2VariablesStyle) // columnaQuantity columns
                .addFooterVariable(columnaQuantity,	DJCalculation.SUM,g2VariablesStyle)
                .setGroupLayout(GroupLayout.VALUE_IN_HEADER)
                //.setAllowSplitting(false,false)
                .build();

        drb.addColumn(columnState);
        drb.addColumn(columnBranch);
        drb.addColumn(columnaProductLine);
        drb.addColumn(columnaItem);
        drb.addColumn(columnCode);
        drb.addColumn(columnaQuantity);
        drb.addColumn(columnAmount);

        drb.addGroup(g1); // add group g1
        drb.addGroup(g2); // add group g2

        drb.setUseFullPageWidth(true);
        drb.addAutoText(AutoText.AUTOTEXT_PAGE_X_SLASH_Y, AutoText.POSITION_FOOTER, AutoText.ALIGNMENT_RIGHT);

        DynamicReport dr = drb.build();
        dr.setProperty('net.sf.jasperreports.awt.ignore.missing.font', 'true')

        return dr;
    }

    protected JRDataSource getDataSource() {
        Collection dummyCollection = TestRepositoryProducts.getDummyCollection();
        //println new JsonBuilder( dummyCollection ).toPrettyString()

        grails.converters.JSON.registerObjectMarshaller(Product) {
            return it.properties.findAll {k,v ->
                !(k in ['emptyStatistics','statistics',"image"])
            }
        }
        //json.setExcludes(domain.Product,["emptyStatistics", "statistics", "image","amount"])
        JSON json = new JSON(dummyCollection)
        json.prettyPrint = true
        def sw = new StringWriter()


        json.render(sw)
        println sw.toString()

        dummyCollection = SortUtils.sortCollection(dummyCollection,dr.getColumns());

        JRDataSource ds = new JRBeanCollectionDataSource(dummyCollection);		//Create a JRDataSource, the Collection used
        //here contains dummy hardcoded objects...
        return ds;
    }
}
