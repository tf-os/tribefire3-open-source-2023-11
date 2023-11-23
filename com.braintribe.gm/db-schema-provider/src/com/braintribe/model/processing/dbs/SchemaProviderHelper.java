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
package com.braintribe.model.processing.dbs;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.dbs.DbColumn;
import com.braintribe.model.dbs.DbSchema;
import com.braintribe.model.dbs.DbSequence;
import com.braintribe.model.dbs.DbTable;
import com.braintribe.model.dbs.TableBasedEntity;

/**
 * 
 */
class SchemaProviderHelper {

	private static final Logger log = Logger.getLogger(SchemaProviderHelper.class);

	private final DatabaseMetaData md;
	private final String catalog;
	private final String schema;

	private final Map<String, DbSchemaDescriptor> schemaRegistry = newMap();

	SchemaProviderHelper(DatabaseMetaData md, String catalog, String schema) {
		this.md = md;
		this.catalog = catalog;
		this.schema = schema;
	}

	Set<DbSchema> provide() throws SQLException {
		processTables();
		processSequences();

		return createResult();
	}

	private void processTables() throws SQLException {
		try (ResultSet rs = md.getTables(catalog, schema, "%", new String[] { "TABLE" })) {
			processTableOrView(rs, false);
		}

		try (ResultSet rs = md.getTables(catalog, schema, "%", new String[] { "VIEW" })) {
			processTableOrView(rs, true);
		}
	}

	private void processTableOrView(ResultSet rs, boolean isView) throws SQLException {
		while (rs.next()) {
			String tableSchema = rs.getString("TABLE_SCHEM");
			String tableName = rs.getString("TABLE_NAME");
			DbTable dbTable = acquireDbTable(tableName, tableSchema);
			setTableBasedEntityData(dbTable, rs);

			processColumns(dbTable, isView);
		}
	}

	private void processSequences() throws SQLException {
		try (ResultSet rs = md.getTables(catalog, schema, "%", new String[] { "SEQUENCE" })) {
			while (rs.next()) {
				String tableSchema = rs.getString("TABLE_SCHEM");
				DbSequence dbSequence = DbSequence.T.create();
				setTableBasedEntityData(dbSequence, rs);

				acquireSchemaDescriptor(tableSchema).sequences.add(dbSequence);
			}
		}
	}

	private void setTableBasedEntityData(TableBasedEntity e, ResultSet rs) throws SQLException {
		e.setName(rs.getString("TABLE_NAME"));
		e.setSchema(rs.getString("TABLE_SCHEM"));
		e.setCatalog(rs.getString("TABLE_CAT"));
		e.setRemarks(rs.getString("REMARKS"));
	}

	private Set<DbSchema> createResult() {
		Set<DbSchema> result = newSet();

		for (DbSchemaDescriptor schemaDescriptor : schemaRegistry.values())
			result.add(schemaDescriptor.completeSchema());

		return result;
	}

	private void processColumns(DbTable table, boolean isView) {
		try {
			tryProcessColumns(table, isView);
		} catch (SQLException e) {
			log.error("Error while processing columns - catalog:" + table.getCatalog() + ", schema: " + table.getSchema() + ", tableName: "
					+ table.getName(), e);
		}
	}

	private void tryProcessColumns(DbTable table, boolean isView) throws SQLException {
		Map<String, DbColumn> columnRegistry = newMap();

		// Columns data
		try (ResultSet rs = md.getColumns(table.getCatalog(), table.getSchema(), table.getName(), null)) {
			while (rs.next()) {
				DbColumn dbColumn = DbColumn.T.create();
				dbColumn.setOwner(table);
				dbColumn.setName(rs.getString("COLUMN_NAME"));
				dbColumn.setDataType(rs.getInt("DATA_TYPE"));
				dbColumn.setDataTypeName(getDataTypeName(rs));
				dbColumn.setTypeName(rs.getString("TYPE_NAME"));
				dbColumn.setNullable(isNullable(rs.getShort("NULLABLE")));
				dbColumn.setOrdinalPosition(rs.getInt("ORDINAL_POSITION"));
				dbColumn.setColumnSize(rs.getInt("COLUMN_SIZE"));
				dbColumn.setRemarks(rs.getString("REMARKS"));

				// VIEW PK if column is called ID 
				boolean isViewPrimaryKey = isView && dbColumn.getName().equals("id");
				
				if (isViewPrimaryKey) {
					dbColumn.setIsPrimaryKey(true);
					List<DbColumn> columns = new LinkedList<>();
					columns.add(dbColumn);
					table.setPrimaryKeyColumns(columns);
				}

				columnRegistry.put(dbColumn.getName(), dbColumn);
			}
		}

		addColumns(table, columnRegistry.values());

		// Foreign keys
		try (ResultSet keys = md.getImportedKeys(table.getCatalog(), table.getSchema(), table.getName())) {
			while (keys.next()) {
				String pkTableSchema = keys.getString("PKTABLE_SCHEM");
				String pkTableName = keys.getString("PKTABLE_NAME");
				String fkColumnName = keys.getString("FKCOLUMN_NAME");

				DbTable pkTable = acquireDbTable(pkTableName, pkTableSchema);
				DbColumn fkColumn = columnRegistry.get(fkColumnName);

				fkColumn.setReferencedTable(pkTable);
			}
		}

		// We could probably end before FKs as well, no FKs in a view, right?
		if (isView)
			return;

		// Primary keys
		try (ResultSet primKeys = md.getPrimaryKeys(table.getCatalog(), table.getSchema(), table.getName())) {
			List<DbColumn> primKeysColumns = newList();

			while (primKeys.next()) {
				String pkColumnName = primKeys.getString("COLUMN_NAME");
				DbColumn pkColumn = columnRegistry.get(pkColumnName);

				pkColumn.setIsPrimaryKey(true);
				primKeysColumns.add(pkColumn);
			}

			table.setPrimaryKeyColumns(primKeysColumns);
			
			// TODO
			if (table.getPrimaryKeyColumns().size() > 1) {			
				addDefaultPrimaryKey(table);
				// multiple primary keys = do nothing, create ID later
			} else if (table.getPrimaryKeyColumns().size() == 1) {
				// one primary key = set as the key
				table.setPrimaryKeyColumn(table.getPrimaryKeyColumns().get(0));
			} else { 
				// no primary keys? = all columns are keys...
				table.setPrimaryKeyColumns(table.getColumns());
				addDefaultPrimaryKey(table);
			}
		}
	}

	private void addDefaultPrimaryKey(DbTable table) {
		// TODO
		
//		DbColumn primaryKey = DbColumn.T.create(); 
//		
//		primaryKey.setName(prima);
//		
//		table.getColumns().add(primaryKey);
		
	}

	private String getDataTypeName(ResultSet rs) throws SQLException {
		String dataType = SqlTypeNameResolver.getSqlTypeName(rs.getInt("DATA_TYPE"));

		return dataType != null ? dataType : rs.getString("TYPE_NAME");
	}

	private void addColumns(DbTable table, Collection<DbColumn> values) {
		List<DbColumn> columns = newList(values);
		Collections.sort(columns, ColumnSorter.instance);

		table.setColumns(columns);
	}

	private Boolean isNullable(int nullable) {
		switch (nullable) {
			case DatabaseMetaData.columnNullable:
				return Boolean.TRUE;
			case DatabaseMetaData.columnNoNulls:
				return Boolean.FALSE;
			case DatabaseMetaData.columnNullableUnknown:
			default:
				return null;
		}
	}

	private DbTable acquireDbTable(String name, String schema) {
		return acquireSchemaDescriptor(schema).acquireDbTable(name);
	}

	private DbSchemaDescriptor acquireSchemaDescriptor(String schema) {

		if (schema == null)
			// Fix for databases like Mysql which treats the catalog as schema name. DEVCX-859
			schema = catalog;

		DbSchemaDescriptor result = schemaRegistry.get(schema);

		if (result == null) {
			DbSchema dbSchema = DbSchema.T.create();
			dbSchema.setName(schema);

			result = new DbSchemaDescriptor(dbSchema);
			schemaRegistry.put(schema, result);
		}

		return result;
	}

	private static class DbSchemaDescriptor {
		DbSchema dbSchema;
		Map<String, DbTable> tableRegistry = newMap();
		Set<DbSequence> sequences = newSet();

		DbSchemaDescriptor(DbSchema dbSchema) {
			this.dbSchema = dbSchema;
		}

		DbTable acquireDbTable(String name) {
			DbTable result = tableRegistry.get(name);

			if (result == null) {
				result = DbTable.T.create();
				result.setName(name);
				tableRegistry.put(name, result);
			}

			return result;
		}

		DbSchema completeSchema() {
			dbSchema.setTables(newSet(tableRegistry.values()));
			dbSchema.setSequences(sequences);

			return dbSchema;
		}
	}

	static class ColumnSorter implements Comparator<DbColumn> {
		static final ColumnSorter instance = new ColumnSorter();

		@Override
		public int compare(DbColumn c1, DbColumn c2) {
			return c1.getOrdinalPosition() - c2.getOrdinalPosition();
		}
	}

}
