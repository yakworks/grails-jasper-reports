/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.jasper.dynamic

import java.awt.*

import groovy.transform.CompileStatic

import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter
import net.sf.dynamicreports.report.builder.HyperLinkBuilder
import net.sf.dynamicreports.report.builder.ReportTemplateBuilder
import net.sf.dynamicreports.report.builder.component.ComponentBuilder
import net.sf.dynamicreports.report.builder.datatype.BigDecimalType
import net.sf.dynamicreports.report.builder.style.PenBuilder
import net.sf.dynamicreports.report.builder.style.SimpleStyleBuilder
import net.sf.dynamicreports.report.builder.style.StyleBuilder
import net.sf.dynamicreports.report.builder.style.Styles
import net.sf.dynamicreports.report.builder.tableofcontents.TableOfContentsCustomizerBuilder
import net.sf.dynamicreports.report.constant.Evaluation
import net.sf.dynamicreports.report.constant.HorizontalAlignment
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment
import net.sf.dynamicreports.report.constant.VerticalTextAlignment
import net.sf.dynamicreports.report.definition.ReportParameters

import static net.sf.dynamicreports.report.builder.DynamicReports.*

/**
 * A bunch of helpers, mostly for examples and tests. should be using TemplateStyles so its configurable
 */
@CompileStatic
public class StyleStatics {
    public static StyleBuilder rootStyle
    public static StyleBuilder boldStyle
    public static StyleBuilder italicStyle
    public static StyleBuilder boldCentered
    public static StyleBuilder bold12Centered
    public static StyleBuilder bold18Centered
    public static StyleBuilder bold22Centered

    public static StyleBuilder columnStyle
    public static StyleBuilder columnTitleStyle
    public static StyleBuilder columnStyleWithGridLines
    public static StyleBuilder columnsBoolean

    public static StyleBuilder groupStyle
    public static StyleBuilder groupFooterStyle
    public static StyleBuilder groupHeaderStyle
    public static StyleBuilder groupStyleL2
    public static StyleBuilder groupStyleL3
    public static StyleBuilder groupFooterStyleL2
    public static StyleBuilder groupHeaderStyleL2
    public static StyleBuilder groupFooterStyleL3
    public static StyleBuilder groupHeaderStyleL3
    public static StyleBuilder subtotalStyle

    public static PenBuilder lineStyle

    public static ReportTemplateBuilder reportTemplate
    public static CurrencyType currencyType
    public static ComponentBuilder<?, ?> dynamicReportsComponent
    public static ComponentBuilder<?, ?> footerComponent

    public static PenBuilder lineStyleLight
    public static SimpleStyleBuilder oddRowStyle

    static init() {
        rootStyle = stl.style().setName("style_root").setFont(stl.fontArial())
        //(stl.font("Verdana",false,false,10)) //(stl.fontArial())
        boldStyle = stl.style(rootStyle).setName("style_bold").bold().setForegroundColor(Color.decode("#333333"))
        italicStyle = stl.style(rootStyle).setName("style_italic").italic()
        boldCentered = stl.style(boldStyle).setName("style_boldCentered")
                .setTextAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.MIDDLE)
        bold12Centered = stl.style(boldCentered).setName("style_bold12Centered").setFontSize(12)
        bold18Centered = stl.style(boldCentered).setName("style_bold18Centered").setFontSize(18)
        bold22Centered = stl.style(boldCentered).setName("style_bold22Centered").setFontSize(22)

        lineStyle = stl.penThin().setLineColor(Color.decode("#bbbbbb"))
        lineStyleLight = stl.penThin().setLineColor(Color.decode("#dddddd"))

        def lightBackground =
                columnStyle = stl.style(rootStyle).setName("style_column").setPadding(3).setLeftPadding(5).setRightPadding(5)
                        .setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)

        columnStyleWithGridLines = stl.style(columnStyle).setName("style_columnWithGridLines")
                .setBottomBorder(lineStyleLight).setTopBorder(lineStyleLight)

        columnTitleStyle = stl.style(boldStyle).setName("style_columnTitle").setPadding(5)
                .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                .setBorder(lineStyle)
                .setBackgroundColor(Color.decode("#f9fafb"))
                .setForegroundColor(Color.decode("#4d545a"))

        columnsBoolean = Styles.style(bold18Centered).setName("style_columnBoolean").setForegroundColor(Color.decode("#3e8c41"))
        //this is one the columns
        subtotalStyle = stl.style(boldStyle).setName("style_subtotalDefault").setPadding(3).setLeftPadding(5).setRightPadding(5)
                .setTopBorder(lineStyleLight)
                .setFontSize(10)

        /** Group Styles **/
        groupStyle = stl.style(boldStyle).setName("style_group").setForegroundColor(Color.decode("#2e4e6f"))
                .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
                .setFontSize(13).setTopPadding(5).setBottomPadding(5)

        //header and footer pertain to the bands
        groupHeaderStyle = stl.style(groupStyle).setName("style_groupHeader")//.setBackgroundColor(Color.decode("#eeeeee"))
        //.setBottomBorder(lineStyle)
                .setTopPadding(0).setBottomPadding(0)

        groupFooterStyle = stl.style(groupStyle).setName("style_groupFooter").setTopPadding(0).setBottomPadding(0)
        //.setTopBorder(lineStyle)

        groupStyleL2 = stl.style(groupStyle).setName("style_groupL2")
                .setForegroundColor(Color.decode("#2e4e6f"))
                .setTopPadding(0).setBottomPadding(0)
                .setFontSize(12)

        groupHeaderStyleL2 = stl.style(groupStyle).setName("style_groupHeaderL2")//.setBackgroundColor(Color.decode("#eeeeee"))
                .setBottomBorder(lineStyleLight)
                .setTopPadding(5).setBottomPadding(3)

        groupFooterStyleL2 = stl.style(groupFooterStyle).setName("style_groupFooterL2")
        //.setTopBorder(lineStyle)

        groupStyleL3 = stl.style(groupStyle).setName("style_groupL3").setForegroundColor(Color.decode("#333333"))
                .setTopPadding(0).setBottomPadding(0)
                .setFontSize(10)

        groupHeaderStyleL3 = stl.style(groupStyle).setName("style_groupHeaderL3").setTopPadding(0).setBottomPadding(0)
        //.setBackgroundColor(Color.decode("#eeeeee"))
        //.setBottomBorder(lineStyle)
        //.setTopPadding(3).setBottomPadding(3)

        groupFooterStyleL3 = stl.style(groupFooterStyleL2).setName("style_groupFooterL3")
        //.setTopBorder(lineStyle)

        StyleBuilder crosstabGroupStyle = stl.style(columnTitleStyle)
        StyleBuilder crosstabGroupTotalStyle = stl.style(columnTitleStyle)
                .setBackgroundColor(new Color(170, 170, 170))
        StyleBuilder crosstabGrandTotalStyle = stl.style(columnTitleStyle)
                .setBackgroundColor(new Color(140, 140, 140))
        StyleBuilder crosstabCellStyle = stl.style(columnStyle)
                .setBorder(stl.pen1Point())

        TableOfContentsCustomizerBuilder tableOfContentsCustomizer = tableOfContentsCustomizer()
                .setHeadingStyle(0, stl.style(rootStyle).bold())

        def rowstyle = stl.style().setName("style_oddRowStyle").setBackgroundColor(Color.decode("#f9fafb"))
                .setBottomBorder(stl.penThin().setLineColor(Color.decode("#dddddd")))
                .setTopBorder(stl.penThin().setLineColor(Color.decode("#dddddd")))

        oddRowStyle = stl.simpleStyle().setBackgroundColor(Color.decode("#f9fafb"))
                .setBottomBorder(stl.penThin().setLineColor(Color.decode("#dddddd")))
                .setTopBorder(stl.penThin().setLineColor(Color.decode("#dddddd")))

        reportTemplate = template()
        //.setLanguage(Language.GROOVY)
                .setLocale(Locale.ENGLISH)
                .setColumnStyle(Styles.templateStyle("columng"))
                .setColumnTitleStyle(Styles.templateStyle("columnTitle"))
                .setGroupStyle(groupStyle)
        //.setGroupFooterStyle(groupFooterStyleL2)
                .setGroupTitleStyle(groupStyle)
                .setSubtotalStyle(subtotalStyle)
        //.highlightDetailOddRows().setDetailOddRowStyle(oddRowStyle)
                .crosstabHighlightEvenRows()
                .setCrosstabGroupStyle(crosstabGroupStyle)
                .setCrosstabGroupTotalStyle(crosstabGroupTotalStyle)
                .setCrosstabGrandTotalStyle(crosstabGrandTotalStyle)
                .setCrosstabCellStyle(crosstabCellStyle)
                .setTableOfContentsCustomizer(tableOfContentsCustomizer)

        currencyType = new CurrencyType()

        HyperLinkBuilder link = hyperLink(exp.jasperSyntaxText("http://www.dynamicreports.org"))
        dynamicReportsComponent =
                cmp.horizontalList(
                        cmp.image(new URL("http://9ci.github.io/www/assets/images/9ci-logo-orange.png")).setFixedDimension(60, 60),
                        cmp.verticalList(
                                cmp.text(exp.jasperSyntaxText("DynamicReports Examples"))
                                        .setStyle(bold22Centered)
                                        .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT),
                                cmp.text(exp.jasperSyntaxText("http://www.dynamicreports.org"))
                                        .setStyle(italicStyle).setHyperLink(link)
                        )
                ).setFixedWidth(300)

        footerComponent = cmp.pageXofY()
                .setStyle(
                stl.style(boldCentered)
                        .setTopBorder(stl.pen1Point()))
    }

    /**
     * Creates custom component which is possible to add to any report band component
     */
    public static ComponentBuilder<?, ?> createTitleComponent(String label) {
        return cmp.horizontalList()
                .add(
                dynamicReportsComponent,
                cmp.text(exp.jasperSyntaxText(label))
                        .setStyle(bold18Centered)
                        .setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT)
        )
                .newRow()
                .add(cmp.line())
                .newRow()
                .add(cmp.verticalGap(3))
    }

    public static ComponentBuilder<?, ?> createFooter() {
        def pgOf = cmp.text(exp.jasperSyntax(' "Page "+$V{PAGE_NUMBER}+" of" '))
                .setHorizontalAlignment(HorizontalAlignment.RIGHT)

        def pgTotal = cmp.text(exp.jasperSyntax(' " " + $V{PAGE_NUMBER}'))
                .setEvaluationTime(Evaluation.REPORT)
                .setHorizontalAlignment(HorizontalAlignment.LEFT)
                .setWidth(10)

        //def page = cmp.horizontalList().add(pgOf,pgTotal)
        def date = cmp.text(exp.jasperSyntax('new java.util.Date()')).setPattern('EEEEE dd MMMMM yyyy')

        return cmp.horizontalList()
                .add(cmp.verticalGap(3))
                .newRow()
                .add(date, pgOf, pgTotal)

    }

    public static CurrencyValueFormatter createCurrencyValueFormatter(String label) {
        return new CurrencyValueFormatter(label)
    }

    @CompileStatic
    public static class CurrencyType extends BigDecimalType {
        private static final long serialVersionUID = 1L

        @Override
        public String getPattern() {
            return "\$ #,###.00"
        }
    }

    @CompileStatic
    private static class CurrencyValueFormatter extends AbstractValueFormatter<String, Number> {
        private static final long serialVersionUID = 1L

        private String label

        public CurrencyValueFormatter(String label) {
            this.label = label
        }

        @Override
        public String format(Number value, ReportParameters reportParameters) {
            return label + currencyType.valueToString(value, reportParameters.getLocale())
        }
    }
}
