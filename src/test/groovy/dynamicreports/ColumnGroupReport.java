/**
 * DynamicReports - Free Java reporting library for creating reports dynamically
 *
 * Copyright (C) 2010 - 2016 Ricardo Mariaca
 * http://www.dynamicreports.org
 *
 * This file is part of DynamicReports.
 *
 * DynamicReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DynamicReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with DynamicReports. If not, see <http://www.gnu.org/licenses/>.
 */

package dynamicreports;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder;
import net.sf.dynamicreports.report.constant.GroupHeaderLayout;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.definition.datatype.DRIDataType;
import net.sf.jasperreports.engine.JRDataSource;
import nine.jasper.dynamicreports.StyleTemplates;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;

/**
 * @author Ricardo Mariaca (r.mariaca@dynamicreports.org)
 */
public class ColumnGroupReport {

	public ColumnGroupReport() {
		build();
	}

	private void build() {


		try {
			TextColumnBuilder<String> itemColumn = col.column("item", (DRIDataType)type.detectType(String.class.getName()));
			itemColumn.setTitle(exp.jasperSyntaxText("Item"));
			TextColumnBuilder<BigDecimal> unitpriceColumn = col.column("Unit price", "unitprice", type.bigDecimalType());
			unitpriceColumn.setTitle(exp.jasperSyntaxText("Unit price"));
			TextColumnBuilder<Integer> quantityColumn = col.column("Quantity", "quantity", type.integerType());
			quantityColumn.setTitle(exp.jasperSyntaxText("Quantity"));
			TextColumnBuilder<Date> orderDateColumn = col.column("Order date", "orderdate", type.dateType());
			orderDateColumn.setTitle(exp.jasperSyntaxText("Order date"));

			ColumnGroupBuilder itemGroup = grp.group(itemColumn)
					.setTitleWidth(30)
					.setHeaderLayout(GroupHeaderLayout.VALUE)
					//.setHideColumn(true)
					.setPadding(0)
					.showColumnHeaderAndFooter()
					.headerWithSubtotal();
			JasperReportBuilder jrb = report();
				jrb.setTemplate(StyleTemplates.reportTemplate)
				.setShowColumnTitle(false)
				.columns(
					itemColumn,
					orderDateColumn,
					quantityColumn,
					unitpriceColumn)
				.groupBy(itemGroup)
				.subtotalsAtGroupFooter(itemGroup, sbt.sum(unitpriceColumn))
				.subtotalsAtGroupFooter(itemGroup, sbt.sum(quantityColumn))
						.subtotalsAtGroupHeader(itemGroup, sbt.sum(unitpriceColumn))
				.title(StyleTemplates.createTitleComponent("Group"))
				.pageFooter(StyleTemplates.footerComponent)
				.setDataSource(createDataSource())
					.toJrXml(new FileOutputStream( new File("target/ColumnGroupReport.jrxml")))
					.toPdf(new FileOutputStream( new File("target/ColumnGroupReport.pdf")))
					.toHtml(new FileOutputStream( new File("target/ColumnGroupReport.html")))
				.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JRDataSource createDataSource() {
		DRDataSource dataSource = new DRDataSource("item", "orderdate", "quantity", "unitprice");
		dataSource.add("Tablet", toDate(2010, 1, 1), 5, new BigDecimal(300));
		dataSource.add("Tablet", toDate(2010, 1, 3), 1, new BigDecimal(280));
		dataSource.add("Tablet", toDate(2010, 1, 19), 5, new BigDecimal(320));
		dataSource.add("Laptop", toDate(2010, 1, 5), 3, new BigDecimal(580));
		dataSource.add("Laptop", toDate(2010, 1, 8), 1, new BigDecimal(620));
		dataSource.add("Laptop", toDate(2010, 1, 15), 5, new BigDecimal(600));
		dataSource.add("Smartphone", toDate(2010, 1, 18), 8, new BigDecimal(150));
		dataSource.add("Smartphone", toDate(2010, 1, 20), 8, new BigDecimal(210));
		return dataSource;
	}

	private Date toDate(int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month - 1);
		c.set(Calendar.DAY_OF_MONTH, day);
		return c.getTime();
	}

	public static void main(String[] args) {
		new ColumnGroupReport();
	}
}