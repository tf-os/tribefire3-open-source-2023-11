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
package com.braintribe.model.access.rdbms.deploy;

import static com.braintribe.utils.SysPrint.spOut;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import com.braintribe.logging.Logger;
import com.braintribe.model.access.sql.dialect.SqlDialect;
import com.braintribe.model.access.sql.dialect.StandardSqls;
import com.braintribe.model.access.sql.tools.JdbcTools;
import com.braintribe.model.dbs.DbColumn;
import com.braintribe.model.dbs.DbSchema;
import com.braintribe.model.dbs.DbTable;

/**
 * @author peter.gazdik
 */
public class RdbmsDdlExpert {

	private static final Logger log = Logger.getLogger(RdbmsDdlExpert.class);

	protected final DataSource dataSource;
	protected final String databaseName;
	private final SqlDialect sqlDialect;

	protected boolean metaExisted;

	public RdbmsDdlExpert(DataSource dataSource, String databaseName, SqlDialect sqlDialect) {
		this.dataSource = dataSource;
		this.databaseName = databaseName;
		this.sqlDialect = sqlDialect;
	}

	// #############################################
	// ## . . . . . . Ensure Database . . . . . . ##
	// #############################################

	/**
	 * Creates a database with given name if such does not exist yet.
	 */
	public void ensureDatabase() throws Exception {
		JdbcTools.tryDoStatement(dataSource, this::ensureDatabase);
	}

	private void ensureDatabase(Statement statement) throws SQLException {
		if (!dbExists(statement))
			createDatabase(statement);
	}

	protected boolean dbExists(Statement statement) throws SQLException {
		String sql = sqlDialect.showDatabasesLike(databaseName);
		try (ResultSet rs = statement.executeQuery(sql)) {
			return rs.next();
		}
	}

	protected void createDatabase(Statement statement) throws SQLException {
		log.debug("Creating DB: " + databaseName);

		String sql = sqlDialect.createDatabase(databaseName);
		int updateResult = statement.executeUpdate(sql);

		log.debug("Creating DB '" + databaseName + "' finished with result: " + updateResult);
	}

	// #############################################
	// ## . . . . . . Ensure Meta Table . . . . . ##
	// #############################################

	public void ensureMetaTable() throws Exception {
		metaExisted = JdbcTools.tryComputeStatement(dataSource, this::ensureMetatable);
	}

	private boolean ensureMetatable(Statement statement) throws SQLException {
		boolean tableExists = tableExists(StandardSqls.metaTable, statement);
		if (!tableExists)
			createMetaTable(statement);

		return tableExists;
	}

	private boolean tableExists(String tableName, Statement statement) throws SQLException {
		String sql = sqlDialect.showTablesLike(tableName);
		try (ResultSet rs = statement.executeQuery(sql)) {
			return rs.next();
		}
	}

	protected void createMetaTable(Statement statement) throws SQLException {
		String sql = sqlDialect.createMetaTable();
		log.debug("Creating meta table with SQL: " + sql);

		int updateResult = statement.executeUpdate(sql);

		log.debug("Creating meta table '" + StandardSqls.metaTable + "' finished with result: " + updateResult);
	}

	// #############################################
	// ## . . . . . . Ensure DbSchema . . . . . . ##
	// #############################################

	public void ensureSchema(DbSchema dbSchema) throws Exception {
		JdbcTools.tryDoStatement(dataSource, s -> ensureDbSchema(s, dbSchema));
	}

	private void ensureDbSchema(Statement statement, DbSchema dbSchema) throws Exception {
		Map<String, String> existingTablesWithHash = getExistingTableInfo(statement);

		List<String> tablesToDelete = newList();
		List<DbTable> tablesToCreate = newList();
		Map<String, String> entriesToCreate = newMap();

		for (DbTable dbTable : dbSchema.getTables()) {
			String tableName = dbTable.getName();

			String oldHash = existingTablesWithHash.remove(tableName);
			String newHash = RdbmsBase.computeHash(dbTable);

			if (!newHash.equals(oldHash)) {
				tablesToCreate.add(dbTable);
				entriesToCreate.put(tableName, newHash);

				// if table existed before
				if (oldHash != null)
					tablesToDelete.add(tableName);
			}
		}

		// delete all tables that are not part of given dbSchema
		tablesToDelete.addAll(existingTablesWithHash.keySet());

		dropTables(statement, tablesToDelete);
		createTables(statement, tablesToCreate);

		deleteMetaEntries(statement, tablesToDelete);
		createMetaEntries(statement, entriesToCreate);
	}

	protected Map<String, String> getExistingTableInfo(Statement statement) throws SQLException {
		if (!metaExisted)
			return Collections.emptyMap();

		Map<String, String> result = newMap();

		String sql = sqlDialect.selectFromMetaTable();
		log.debug("Getting info on existing tables with query: " + sql);

		try (ResultSet rs = statement.executeQuery(sql)) {
			while (rs.next()) {
				// Retrieve by column name
				String name = rs.getString("name");
				String hash = rs.getString("hash");

				result.put(name, hash);
			}
		}

		log.debug("Existing db tables info: " + result);

		return result;
	}

	//
	// Update tables
	//

	private void dropTables(Statement statement, Iterable<String> tableNames) throws Exception {
		for (String tableName : tableNames)
			dropTable(statement, tableName);
	}

	protected void dropTable(Statement statement, String tableName) throws SQLException {
		String fullTableName = RdbmsBase.qualifiedTableName(databaseName, StandardSqls.metaTable);
		log.debug(() -> "Dropping SQL table: " + fullTableName);

		String sql = sqlDialect.dropTable(tableName);
		log.debug(() -> "Dropping SQL table: " + fullTableName + ". SQL: " + sql);
		int updateResult = statement.executeUpdate(sql);

		log.debug(() -> "Dropping SQL table '" + fullTableName + "' finished with result: " + updateResult);
	}

	private void createTables(Statement statement, Iterable<DbTable> tables) throws SQLException {
		for (DbTable dbTable : tables)
			createTable(statement, dbTable);
	}

	protected void createTable(Statement statement, DbTable dbTable) throws SQLException {
		String fullTableName = RdbmsBase.qualifiedTableName(databaseName, dbTable.getName());
		log.debug("Creating SQL table: " + fullTableName);

		String columns = getColumns(dbTable);

		String sql = sqlDialect.createTable(dbTable.getName(), columns);
		log.debug("Creating SQL table with SQL: " + sql);

		spOut("Creating SQL table with SQL: " + sql);

		int updateResult = statement.executeUpdate(sql);

		log.debug(() -> "Creating SQL table '" + fullTableName + "' finished with result: " + updateResult);
	}

	private String getColumns(DbTable dbTable) {
		StringBuilder sb = new StringBuilder();

		String comma = "";

		for (DbColumn dbColumn : dbTable.getColumns()) {
			sb.append(comma);
			sb.append(dbColumn.getName());
			sb.append(' ');
			sb.append(dbColumn.getDataTypeName());

			comma = ",";
		}

		return sb.toString();
	}

	//
	// Update meta-entries
	//

	protected void deleteMetaEntries(Statement statement, List<String> tableNames) throws Exception {
		log.debug(() -> "Deleting entries from 'meta-table' for tables: " + tableNames);

		if (tableNames.isEmpty())
			return;

		String questionMarks = String.join(",", Collections.nCopies(tableNames.size(), "?"));
		String sql = String.format("delete from %s where %s in (%s)", StandardSqls.metaTable, StandardSqls.metaTable_Name, questionMarks);

		int updateResult = JdbcTools.tryComputePreparedStatement(statement.getConnection(), sql, ps -> runPreparedStatement(ps, tableNames));
		log.debug(() -> "Deleting entries from 'meta-table' finished with result: " + updateResult);
	}

	protected int runPreparedStatement(PreparedStatement ps, List<String> valuesToBind) throws Exception {
		for (int i = 0; i < valuesToBind.size(); i++) {
			ps.setString(i, valuesToBind.get(i));
		}

		return ps.executeUpdate();
	}

	protected void createMetaEntries(Statement statement, Map<String, String> entriesToCreate) throws Exception {
		log.debug(() -> "Creating 'meta-table' entries: " + entriesToCreate);

		String sql = insertMetaEntriesSql(entriesToCreate);
		int updateResult = statement.executeUpdate(sql);

		log.debug(() -> "Creating 'meta-table' entries finished with result: " + updateResult);
	}

	/** Building a query: {@code insert into meta (name, hash) values ('name1', 'hash1'), ('name2', 'hash2'), ... } */
	private String insertMetaEntriesSql(Map<String, String> entriesToCreate) {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ");
		sb.append(StandardSqls.metaTable);
		sb.append(" (");
		sb.append(StandardSqls.metaTable_Name);
		sb.append(", ");
		sb.append(StandardSqls.metaTable_Hash);
		sb.append(") values ");

		for (Iterator<Entry<String, String>> it = entriesToCreate.entrySet().iterator(); it.hasNext();) {
			Entry<String, String> entry = it.next();
			String name = entry.getKey();
			String hash = entry.getValue();

			sb.append("('");
			sb.append(name);
			sb.append("', '");
			sb.append(hash);
			sb.append("')");

			if (it.hasNext())
				sb.append(",");
		}

		return sb.toString();
	}

}
