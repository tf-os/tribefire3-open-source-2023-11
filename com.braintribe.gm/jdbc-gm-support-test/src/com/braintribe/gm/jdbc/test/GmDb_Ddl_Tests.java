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
package com.braintribe.gm.jdbc.test;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import com.braintribe.common.db.DbVendor;
import com.braintribe.gm.jdbc.api.GmColumn;
import com.braintribe.gm.jdbc.api.GmIndex;
import com.braintribe.gm.jdbc.api.GmTable;
import com.braintribe.util.jdbc.JdbcTools;

/**
 * @author peter.gazdik
 */
public class GmDb_Ddl_Tests extends AbstractGmDbTestBase {

	private final GmColumn<String> colIdStr;
	private final GmColumn<String> colVarchar255;

	public GmDb_Ddl_Tests(DbVendor vendor) {
		super(vendor);

		colIdStr = gmDb.shortString255("id").primaryKey().notNull().done();
		colVarchar255 = gmDb.shortString255("varchar255").done();
	}

	private String tableName;
	private Set<String> columnNames;
	private Set<String> indexNames;

	@Test
	public void testCreatesTable() {
		final String TABLE_NAME = "basic_table" + tmSfx;

		GmTable table = gmDb.newTable(TABLE_NAME) //
				.withColumns(colIdStr, colVarchar255) //
				.done();

		table.ensure();
		assertTableOk(table);
	}

	@Test
	public void testCreatesTable_WithIndex() {
		final String TABLE_NAME = "INDEXED_TABLE" + tmSfx;

		GmIndex varchar255Index = gmDb.index("V255_CRT_IDX" + tmSfx, colVarchar255);

		GmTable table = gmDb.newTable(TABLE_NAME) //
				.withColumns(colIdStr, colVarchar255) //
				.withIndices(varchar255Index) //
				.done();

		table.ensure();
		assertTableOk(table);
	}

	@Test
	public void testUpdatesTable() {
		final String TABLE_NAME = "Updated_Table" + tmSfx;

		// Create table

		GmColumn<String> origStr = gmDb.shortString255("origStr").done();
		GmIndex idxOrigStr = gmDb.index("ORIG_IDX" + tmSfx, origStr);

		GmTable originalTable = gmDb.newTable(TABLE_NAME) //
				.withColumns(colIdStr, origStr) //
				.withIndices(idxOrigStr) //
				.done();

		originalTable.ensure();
		assertTableOk(originalTable);

		// Update table

		GmColumn<String> origStr_2 = gmDb.shortString255("origStr").done();
		GmIndex idxOrigStr_2 = gmDb.index("ORIG_IDX" + tmSfx, origStr_2);

		GmColumn<String> colIdStr2 = gmDb.shortString255("id").primaryKey().notNull().done();
		GmColumn<String> colNewStr = gmDb.shortString255("newStr").done();
		GmIndex idxNewStr = gmDb.index("V255_UPD_IDX" + tmSfx, colNewStr);

		GmTable updatedTable = gmDb.newTable(TABLE_NAME) //
				.withColumns(colIdStr2, origStr_2, colNewStr) //
				.withIndices(idxOrigStr_2, idxNewStr) //
				.done();

		updatedTable.ensure();
		assertTableOk(updatedTable);
	}

	private void assertTableOk(GmTable table) {
		final Set<String> _columnNames = table.getColumns().stream().map(GmColumn::getGmName).collect(Collectors.toSet());
		final Set<String> _indexNames = table.getIndices().stream().map(GmIndex::getName).collect(Collectors.toSet());

		JdbcTools.withConnection(dataSource, false, () -> "Asserting creation of table: " + table.getName(), c -> {
			tableName = JdbcTools.tableExists(c, table.getName());
			if (tableName != null) {
				columnNames = JdbcTools.columnsExist(c, tableName, _columnNames);
				indexNames = JdbcTools.indicesExist(c, tableName, _indexNames);
			}
		});

		assertThat(tableName).isNotNull();
		assertThat(columnNames).containsExactlyElementsOf(_columnNames);
		assertThat(indexNames).containsExactlyElementsOf(_indexNames);
	}

}
