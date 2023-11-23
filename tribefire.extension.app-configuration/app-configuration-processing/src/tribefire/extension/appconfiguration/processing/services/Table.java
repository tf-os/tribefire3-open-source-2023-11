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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.braintribe.utils.lcd.StringTools;

/**
 * A simple representation of a table with just a list of {@link #rows} where each row is a list holding the cell values of the respective row. One
 * can access the table content via various methods such as {@link Table#row(int)}, {@link #column(int)} or {@link #cell(int, int)}. All methods which
 * return lists return unmodifiable lists, which means one can't modify the table content that way. The only way to modify it is by
 * {@link #addCellsToNewRow(List) adding new rows}. Methods expecting row and column indexes are all zero based.
 */
public class Table {

	/** The list of list of strings, representing the rows and thus the content of the table. */
	private List<List<String>> rows = new ArrayList<>();

	/**
	 * Creates a new table with no content initially.
	 */
	public Table() {
		// nothing to do
	}

	/**
	 * Adds a new row containing the specified cells (i.e. cell values).
	 */
	public void addCellsToNewRow(List<String> cells) {
		rows.add(new ArrayList<>(cells));
	}

	/**
	 * Returns the value of the specified cell.
	 */
	public String cell(int rowIndex, int columnIndex) {
		return row(rowIndex).get(columnIndex);
	}
	
	/**
	 * Returns the table header, i.e. the first row.
	 */
	public List<String> header() {
		return Collections.unmodifiableList(rows.get(0));
	}

	/**
	 * Returns the table {@link #header()} without the first column/cell. This can be useful for tables where the first column contains keys and the
	 * header (starting with the second column) contains the denominators.
	 */
	public List<String> headerWithoutFirstColumn() {
		List<String> result = new ArrayList<>(header());
		result.remove(0);
		return Collections.unmodifiableList(result);
	}

	/**
	 * Returns the number of rows (currently) stored in the table.
	 */
	public int rowCount() {
		return rows.size();
	}

	/**
	 * Returns the cell values of the specified row.
	 */
	public List<String> row(int rowIndex) {
		return Collections.unmodifiableList(rows.get(rowIndex));
	}

	/**
	 * Returns the number of columns stored in the table.
	 */
	public int columnCount() {
		return rows.get(0).size();
	}

	/**
	 * Returns the cell values of the specified column.
	 */
	public List<String> column(int columnIndex) {
		return Collections.unmodifiableList(rows.stream().map(row -> row.get(columnIndex)).toList());
	}

	/**
	 * Returns the cell values of the specified row, but without the header. This can be useful, if the header column contains the denominator and one
	 * wants to get only the values.
	 */
	public List<String> columnWithoutHeader(int columnIndex) {
		List<String> result = new ArrayList<>(column(columnIndex));
		result.remove(0);
		return Collections.unmodifiableList(result);
	}

	/**
	 * Returns all {@link #rowLine(List, int) row lines} of the passed row.
	 */
	private static List<List<String>> rowLines(List<String> row) {
		List<List<String>> rowLines = new ArrayList<>();
		for (int lineIndex = 0; lineIndex < rowHeight(row); lineIndex++) {
			rowLines.add(rowLine(row, lineIndex));
		}
		return rowLines;
	}

	/**
	 * Returns the specified row line, i.e. the respective {@link #cellLines(String) line} of each cell. This can be useful when there is at least one
	 * cell with a multiline string and one wants to get a single line only. For cells with have less lines, empty strings are returned.
	 */
	private static List<String> rowLine(List<String> row, int lineIndex) {
		if (lineIndex >= rowHeight(row)) {
			throw new IllegalArgumentException("Cannot return row line " + lineIndex + ". Height of row is only " + rowHeight(row) + ". Row: " + row);
		}

		return row.stream().map(cell -> {
			List<String> cellLines = cellLines(cell);
			return (lineIndex < cellLines.size()) ? cellLines.get(lineIndex) : "";
		}).toList();
	}

	/**
	 * Returns the width of the specified column. It's the maximum of the {@link #cellWidth(String) cell widths} of that column.
	 */
	private int columnWidth(int columnIndex) {
		return column(columnIndex).stream().mapToInt(Table::cellWidth).max().orElse(0);
	}

	/**
	 * Retruns the width of the passed cell. The width is just the length in characters. However, if the cell is a multiline string, it's the maximum
	 * length of the {@link #cellLines(String) cell lines}.
	 */
	private static int cellWidth(String cell) {
		return cellLines(cell).stream().mapToInt(String::length).max().orElse(0);
	}

	/**
	 * Returns the individual cell lines of the passed cell (value), i.e. it searches for the line separator and splits the string.
	 */
	private static List<String> cellLines(String cell) {
		return Arrays.asList(StringTools.splitString(cell, "\n"));
	}

	/**
	 * Returns the height of a cell, i.e. the count of {@link #cellLines(String) cell lines}.
	 */
	private static int cellHeight(String cell) {
		return cellLines(cell).size();
	}

	/**
	 * Returns the height of a row, i.e. the maximum of {@link #cellHeight(String) cell heights}.
	 */
	public static int rowHeight(List<String> row) {
		return row.stream().mapToInt(Table::cellHeight).max().orElse(1);
	}

	/**
	 * Returns a multiline string representation of this table. This is mainly used for logging / debugging.
	 */
	public String toMultilineString() {
		if (rows.isEmpty()) {
			return "<empty table>\n";
		}

		int columnCount = rows.get(0).size();
		Map<Integer, Integer> columnWidths = new HashMap<>();
		int columnWidthsSum = 0;

		for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
			int columnWidth = columnWidth(columnIndex);
			columnWidths.put(columnIndex, columnWidth);
			columnWidthsSum += columnWidth;
		}

		char rowDelimiter = '-';

		String leftBorder = "|";
		String rightBorder = leftBorder;
		String columnDelimiter = leftBorder;

		String space = " ";

		int tableWidth = leftBorder.length() + space.length() + columnWidthsSum
				+ ((columnWidths.size() - 1) * (space.length() + columnDelimiter.length() + space.length())) + space.length() + rightBorder.length();

		String rowDelimiterLine = StringTools.getFilledString(tableWidth, rowDelimiter) + "\n";

		StringBuilder builder = new StringBuilder();

		builder.append(rowDelimiterLine);

		for (List<String> row : rows) {
			List<List<String>> rowLines = rowLines(row);

			for (List<String> rowLine : rowLines) {

				List<String> formattedRowLine = new ArrayList<>();
				for (int columnIndex = 0; columnIndex < rowLine.size(); columnIndex++) {
					int columnWidth = columnWidths.get(columnIndex);
					String formatString = "%1$-" + columnWidth + "s";
					String formattedCellLine = String.format(formatString, rowLine.get(columnIndex));
					formattedRowLine.add(formattedCellLine);
				}

				builder.append(leftBorder + space + formattedRowLine.stream().collect(Collectors.joining(space + columnDelimiter + space)) + space
						+ rightBorder + "\n");
			}

			builder.append(rowDelimiterLine);
		}

		return builder.toString();
	}

	/**
	 * Checks whether this table is equal to the specified <code>other</code> object. That's the case, if it's another {@link Table} and the rows are
	 * equal.
	 */
	@Override
	public boolean equals(Object other) {
		return other instanceof Table table && rows.equals(table.rows);
	}

	/**
	 * Returns the hash code of this table, based on its rows.
	 */
	@Override
	public int hashCode() {
		return rows.hashCode();
	}
}