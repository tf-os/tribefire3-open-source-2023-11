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
package tribefire.extension.appconfiguration.processing.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.ParseError;

/**
 * Provides methods to {@link #readTableFromSheet(Workbook, String, Class) import} /
 * {@link #addTableToNewSheet(Workbook, String, Table) export} a {@link Table} from / to a spreadsheet.
 */
public class TableSpreadsheetIO {

	/** The default name of the sheet in the workbook which contains the localizations. */
	static final String DEFAULT_SHEET_NAME = "Localizations";

	/** The string which represents an empty string, i.e. which signals to remove the respective localized value. */
	static final String EMPTY = "EMPTY";

	private TableSpreadsheetIO() {
		// no need to instantiate
	}

	/**
	 * Adds the passed <code>table</code> to the specified sheet (which must already exist).
	 */
	public static void addTableToNewSheet(Workbook workbook, String sheetName, Table table) {
		if (workbook.getSheet(sheetName) != null) {
			throw new IllegalStateException("Sheet '" + sheetName + "' unexpectedly already exists!");
		}

		int rowCount = table.rowCount();
		int columnCount = table.columnCount();

		short heightPerLine = 300;
		short defaultRowHeight = heightPerLine;
		int defaultColumnWidth = 55;

		CellStyle normalCellStyle = workbook.createCellStyle();
		Font normalCellFont = workbook.createFont();
		normalCellStyle.setFont(normalCellFont);

		CellStyle headerRowCellStyle = workbook.createCellStyle();
		headerRowCellStyle.cloneStyleFrom(normalCellStyle);

		Font headerRowCellFont = workbook.createFont();
		headerRowCellFont.setBold(true);
		headerRowCellStyle.setFont(headerRowCellFont);

		Sheet sheet = workbook.createSheet(sheetName);

		sheet.setDefaultColumnWidth(defaultColumnWidth);
		sheet.setDefaultRowHeight(defaultRowHeight);

		for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
			Row row = sheet.createRow(rowIndex);

			int maxLinesInRow = Table.rowHeight(table.row(rowIndex));
			short rowHeight = (short) (heightPerLine * maxLinesInRow);
			row.setHeight(rowHeight);

			for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
				Cell cell = row.createCell(columnIndex);

				cell.setCellStyle(rowIndex == 0 ? headerRowCellStyle : normalCellStyle);

				String cellValue = table.cell(rowIndex, columnIndex);
				if (cellValue != null) {
					cell.setCellValue(cellValue);
				}
			}
		}
	}

	/**
	 * Reads the content of the specified sheet and returns that content as a table of the requested type.
	 */
	public static <T extends Table> Maybe<T> readTableFromSheet(Workbook workbook, String sheetName, Class<T> tableType) {
		Sheet sheet = workbook.getSheet(sheetName);

		if (sheet == null) {
			throw new IllegalStateException("Sheet '" + sheetName + "' not found in workbook!");
		}

		T table;
		try {
			table = tableType.getConstructor().newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException("Couldn't instantiate " + tableType.getName() + ".", e);
		}

		List<String> errorMessages = new ArrayList<>();

		for (Row row : sheet) {
			List<String> cells = new ArrayList<>();
			for (Cell cell : row) {
				String cellValue = readCell(errorMessages, cell);
				cells.add(cellValue);
			}

			if (table.rowCount() > 0) {
				int columnCount = table.columnCount();
				int missingTrailingCellsCount = columnCount - cells.size();
				for (int i = 0; i < missingTrailingCellsCount; i++) {
					// there may be missing cells in a row. it just means the respective cells are empty, which is valid. -> add nulls
					cells.add(null);
				}
			}

			table.addCellsToNewRow(cells);
		}

		Maybe<T> result;

		if (errorMessages.isEmpty()) {
			result = Maybe.complete(table);
		} else {
			result = Reasons.build(ParseError.T).text("Found errors while reading table from sheet.").enrich(reason -> {
				reason.getReasons().addAll(errorMessages.stream().map(message -> Reasons.build(ParseError.T).text(message).toReason()).toList());
			}).toMaybe(table);
		}
		return result;
	}

	/**
	 * Reads a single cell and returns its value. Only default / text cells are supported. Otherwise a proper error message
	 * is added to the passed <code>errorMessages</code>.
	 */
	private static String readCell(List<String> errorMessages, Cell cell) {
		String cellValue = null;
		switch (cell.getCellType()) {
			case STRING:
				cellValue = cell.getStringCellValue();
				break;
			case NUMERIC:
				double numericCellValue = cell.getNumericCellValue();
				if ((numericCellValue % 1) == 0) {
					cellValue = "" + (int) numericCellValue;
				} else {
					cellValue = "" + numericCellValue;
				}
				break;
			case BLANK:
				// nothing to do
				break;
			default:
				// collect error message and continue, so that we get all error messages
				errorMessages.add("Cell " + cell.getAddress().formatAsString() + " has unsupported type '" + cell.getCellType() + "'.");
				break;
		}

		if (cellValue != null) {
			cellValue = cellValue.trim();
			if (cellValue.isEmpty()) {
				// interpret empty/blank string as missing and set value to null.
				// purpose is to avoid accidental removals. instead one can explicitly remove via 'EMPTY', see below.
				cellValue = null;
			} else if (cellValue.equals(EMPTY)) {
				// set value to empty string. this will overwrite the old value (if any).
				cellValue = "";
			}
		}
		return cellValue;
	}
}
