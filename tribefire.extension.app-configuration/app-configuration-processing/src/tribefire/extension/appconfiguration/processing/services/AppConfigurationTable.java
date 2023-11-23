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

import static tribefire.extension.appconfiguration.processing.services.GeneralTools.countAndName;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.gm.model.reason.Reason;

import tribefire.extension.appconfiguration.model.AppConfiguration;

/**
 * A {@link Table} extension which provides convenience methods that are helpful when a table represents a part of an {@link AppConfiguration} such as
 * {@link AppConfiguration#getLocalizations() localizations}.
 */
public class AppConfigurationTable extends Table {

	/**
	 * Creates a new table with no content initially.
	 */
	public AppConfigurationTable() {
		// nothing to do
	}

	/**
	 * Returns a clone of this table where empty cells are marked, i.e. <code>null</code> values are replaced with '[' + <code>key</code> + "]".
	 * Purpose is to make sure that there are no missing/empty entries in AppConfiguration when importing from a table. The method returns a clone
	 * because existing table content cannnot be modified.
	 * 
	 * @see #cloneAndUnmarkEmptyCells()
	 */
	public AppConfigurationTable cloneAndMarkEmptyCells() {
		AppConfigurationTable clone = new AppConfigurationTable(denominators());

		for (int rowIndex = 1; rowIndex < rowCount(); rowIndex++) {
			List<String> row = row(rowIndex);
			String key = row.get(0);

			List<String> rowClone = new ArrayList<>();
			rowClone.add(key);

			for (int columnIndex = 1; columnIndex < row.size(); columnIndex++) {
				String cellValue = cell(rowIndex, columnIndex);
				if (cellValue == null) {
					cellValue = emptyCellMarker(key);
				}
				rowClone.add(cellValue);
			}

			clone.addCellsToNewRow(rowClone);
		}
		return clone;
	}

	/**
	 * Returns a clone of this table where empty cells are not marked, i.e. searches for '[' + <code>key</code> + "]" values and replaces them with
	 * <code>null</code>. Purpose is to avoid exporting these markers. The method returns a clone because existing table content cannnot be modified.
	 * 
	 * @see #cloneAndMarkEmptyCells()
	 */
	public AppConfigurationTable cloneAndUnmarkEmptyCells() {
		AppConfigurationTable clone = new AppConfigurationTable(denominators());

		for (int rowIndex = 1; rowIndex < rowCount(); rowIndex++) {
			List<String> row = row(rowIndex);
			String key = row.get(0);

			List<String> rowClone = new ArrayList<>();
			rowClone.add(key);

			for (int columnIndex = 1; columnIndex < row.size(); columnIndex++) {
				String cellValue = cell(rowIndex, columnIndex);
				String markString = emptyCellMarker(key);
				if (markString.equals(cellValue)) {
					cellValue = null;
				}
				rowClone.add(cellValue);
			}

			clone.addCellsToNewRow(rowClone);
		}
		return clone;
	}

	/**
	 * Returns '[' + <code>key</code> + "]" which is used to signal the value for the <code>key</code> (and respective denominator) is missing, i.e.
	 * the cell is empty.
	 */
	private static final String emptyCellMarker(String key) {
		return "[" + key + "]";
	}

	/**
	 * Creates a new table with the specified <code>denominators</code>.
	 */
	public AppConfigurationTable(List<String> denominators) {
		List<String> header = new ArrayList<>();
		header.add("Key"); // first column
		header.addAll(denominators);
		addCellsToNewRow(header);
	}

	/**
	 * Returns the denominators, i.e. the header row without the first column.
	 */
	public List<String> denominators() {
		return headerWithoutFirstColumn();
	}

	/**
	 * Returns the keys, i.e. the first column without the header cell.
	 */
	public List<String> keys() {
		return columnWithoutHeader(0);
	}

	/**
	 * Returns the values for the specified <code>denominator</code>, i.e. the respective column without the header cell.
	 */
	public List<String> values(String denominator) {
		List<String> denominators = denominators();
		int denominatorIndex = denominators.indexOf(denominator);
		if (denominatorIndex < 0) {
			throw new IllegalArgumentException("Denominator '" + denominator + "' not found in table! Denominators are: " + denominators);
		}

		return columnWithoutHeader(denominatorIndex + 1);
	}

	/**
	 * Returns the value of the cell specified via <code>denominator</code> (-> column) and <code>key</code> (-> row).
	 */
	public String value(String denominator, String key) {
		List<String> keys = keys();
		int index = keys.indexOf(key);
		if (index < 0) {
			throw new IllegalArgumentException("Key '" + key + "' not found in table! Keys are: " + keys);
		}
		List<String> values = values(denominator);

		return values.get(index);
	}

	/**
	 * Performs various checks to assert that the table content is valid. Returns <code>null</code>, if no issues are found, otherwise a
	 * {@link Reason} with a proper message and sub reasons providing further details
	 */
	public Reason assertValid() {
		List<String> duplicateDenominators = GeneralTools.duplicates(denominators());
		List<String> duplicateKeys = GeneralTools.duplicates(keys());

		List<String> detailMessages = new ArrayList<>();
		if (!duplicateDenominators.isEmpty()) {
			detailMessages.add("Found " + countAndName(duplicateDenominators, "denominator") + ": " + duplicateDenominators);
		}
		if (!duplicateKeys.isEmpty()) {
			detailMessages.add("Found " + countAndName(duplicateKeys, "keys") + ": " + duplicateKeys);
		}

		Reason reason = null;
		if (!detailMessages.isEmpty()) {
			reason = GeneralTools.reason("App configuration table contains invalid data!", detailMessages);
		}
		return reason;
	}
}