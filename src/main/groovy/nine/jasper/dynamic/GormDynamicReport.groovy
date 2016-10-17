package nine.jasper.dynamic

import ar.com.fdvs.dj.domain.Style
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder
import ar.com.fdvs.dj.domain.constants.Border
import ar.com.fdvs.dj.domain.constants.Font
import ar.com.fdvs.dj.domain.constants.HorizontalAlign
import ar.com.fdvs.dj.domain.constants.Transparency
import ar.com.fdvs.dj.domain.constants.VerticalAlign

import java.awt.Color

/**
 * Created by basejump on 10/14/16.
 */
class GormDynamicReport {

    GormDynamicReport(){

    }

    protected def setupReportBuilder(){
        Style detailStyle = new Style();
        detailStyle.setVerticalAlign(VerticalAlign.TOP);

        Style groupTitleStyle = new Style();
        groupTitleStyle.setFont(Font.VERDANA_BIG_BOLD);

        Style headerStyle = new Style();
        headerStyle.setFont(Font.VERDANA_MEDIUM_BOLD);
        headerStyle.setBorderBottom(Border.PEN_1_POINT());
        headerStyle.setBackgroundColor(Color.gray);
        headerStyle.setTextColor(Color.white);
        headerStyle.setHorizontalAlign(HorizontalAlign.CENTER);
        headerStyle.setVerticalAlign(VerticalAlign.MIDDLE);
        headerStyle.setTransparency(Transparency.OPAQUE);

        Style headerVariables = new Style();
        headerVariables.setFont(Font.ARIAL_MEDIUM_BOLD);
//		headerVariables.setBorderBottom(Border.THIN());
        headerVariables.setHorizontalAlign(HorizontalAlign.RIGHT);
        headerVariables.setVerticalAlign(VerticalAlign.MIDDLE);

        Style titleStyle = new Style();
        titleStyle.setFont(new Font(18, Font._FONT_VERDANA, true));
        Style importeStyle = new Style();
        importeStyle.setHorizontalAlign(HorizontalAlign.RIGHT);
        Style oddRowStyle = new Style();
        oddRowStyle.setBorder(Border.NO_BORDER());
        oddRowStyle.setBackgroundColor(Color.LIGHT_GRAY);
        oddRowStyle.setTransparency(Transparency.OPAQUE);

        DynamicReportBuilder drb = new DynamicReportBuilder();
        Integer margin = new Integer(20);
        drb.with {
            setTitleStyle(titleStyle)
            setTitle("Sales Report")                    //defines the title of the report
            setSubtitle("The items in this report correspond to the main products: DVDs, Books, Foods and Magazines")
            setDetailHeight(new Integer(15)).setLeftMargin(margin)
            setRightMargin(margin).setTopMargin(margin).setBottomMargin(margin)
            setPrintBackgroundOnOddRows(false)
            setGrandTotalLegend("Grand Total")
            setGrandTotalLegendStyle(headerVariables)
            setDefaultStyles(titleStyle, null, headerStyle, detailStyle)
            setPrintColumnNames(false)
            setOddRowBackgroundStyle(oddRowStyle);
            //.addImageBanner(System.getProperty("user.dir") +"/target/test-classes/images/logo_fdv_solutions_60.jpg", new Integer(100), new Integer(30), ImageBanner.ALIGN_RIGHT)
        }
        return drb
    }

    Style getDetailStyle(){
        Style detailStyle = new Style();
        detailStyle.setVerticalAlign(VerticalAlign.TOP);
        return detailStyle
    }

    Style getHeaderStyle(){
        Style headerStyle = new Style();
        headerStyle.setFont(Font.VERDANA_MEDIUM_BOLD);
        headerStyle.setBorderBottom(Border.PEN_1_POINT());
        headerStyle.setBackgroundColor(Color.gray);
        headerStyle.setTextColor(Color.white);
        headerStyle.setHorizontalAlign(HorizontalAlign.CENTER);
        headerStyle.setVerticalAlign(VerticalAlign.MIDDLE);
        headerStyle.setTransparency(Transparency.OPAQUE);
    }
}
