// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.Format;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.ExcelNumberFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.monitorjbl.xlsx.StreamingReader;
import com.monitorjbl.xlsx.impl.StreamingCell;

public class SheetLab {

	static Field rawContentsField;
	static Method createDateFormatMethod;

	static {
		// rawContentsField= createDateFormat(String pFormatStr, double cellValue)
		try {

			rawContentsField = StreamingCell.class.getDeclaredField("rawContents");
			rawContentsField.setAccessible(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {

			Field field = StreamingCell.class.getDeclaredField("rawContents");
			field.setAccessible(true);

			// Locale.setDefault(Locale.ENGLISH);
			// System.out.println("EN");
			// outputSheet(field);
			//
			// Locale.setDefault(Locale.GERMAN);
			// System.out.println("DE");
			// outputSheet(field);

			outputSheet();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void outputSheet() throws Exception {
		Workbook workbook = StreamingReader.builder().rowCacheSize(100).bufferSize(65536).open(new FileInputStream("res/date-test2.xlsx"));

		for (Sheet sheet : workbook) {
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();

				if (row.getRowNum() == 0)
					continue;

				if (row.getRowNum() > 5)
					break;

				for (Cell cell : row) {
					System.out.print("row: " + row.getRowNum() + ", cell: " + cell.getColumnIndex() + ", ");
					System.out.print("cell type: " + cell.getCellType() + ", ");
					System.out.print("format: " + cell.getCellStyle().getDataFormat() + ", ");
					System.out.print("raw value: " + rawContentsField.get(cell) + ", ");
					System.out.println("string value: " + cell.getStringCellValue());

					CellStyle style = cell.getCellStyle();
					ExcelNumberFormat excelNumberFormat = ExcelNumberFormat.from(style);

					if (DateUtil.isADateFormat(excelNumberFormat)) {
						StreamingCell streamingCell = new StreamingCell(cell.getColumnIndex(), cell.getRowIndex(), false);
						streamingCell.setRawContents("0");
						streamingCell.setCellStyle(cell.getCellStyle());
						Format format = new DataFormatter().createFormat(streamingCell);

						Object parseObject = format.parseObject(cell.getStringCellValue());
						System.out.println(parseObject);
					}

				}
			}
		}
	}
}
