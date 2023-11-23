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
package com.braintribe.util.jdbc;

import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.CollectionTools2.toStringWithElementTypes;
import static java.util.Collections.emptySet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import javax.sql.DataSource;

import com.braintribe.common.lcd.function.XConsumer;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;

/**
 * @author peter.gazdik
 */
/* package */ class JdbcToolsImplementation {

	private static final Logger log = Logger.getLogger(JdbcToolsImplementation.class);

	public static void withManualCommitConnection(DataSource dataSource, Supplier<String> details, XConsumer<Connection> task) {
		Connection connection = getConnection(dataSource, details);

		boolean wasAutoCommit = ensureManualCommit(connection, details);

		doWithConnection(connection, true, details, task, () -> {
			if (wasAutoCommit)
				try {
					connection.setAutoCommit(true);
				} catch (Exception e) {
					log.warn("Error wile trying to set connection back to autoCommit. Details: " + details.get(), e);
				}
		});
	}

	private static boolean ensureManualCommit(Connection c, Supplier<String> details) {
		try {
			boolean autoCommit = c.getAutoCommit();
			if (autoCommit)
				c.setAutoCommit(false);

			return autoCommit;

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while ensuring the connection is not in auto-commit mode. Details: " + details.get());
		}
	}

	public static void withConnection(DataSource dataSource, boolean commit, Supplier<String> details, XConsumer<Connection> task) {
		Connection connection = getConnection(dataSource, details);
		doWithConnection(connection, commit, details, task, null);
	}

	private static void doWithConnection(Connection c, boolean commit, Supplier<String> details, XConsumer<Connection> task, Runnable cleanup) {
		try {
			task.accept(c);

		} catch (Exception e) {
			commit = false; // transaction should be rolled back
			throw Exceptions.unchecked(e);

		} finally {
			if (cleanup != null)
				cleanup.run();

			thoroughlyCloseJdbcConnection(c, commit, details);
		}
	}

	private static Connection getConnection(DataSource dataSource, Supplier<String> details) {
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			throw Exceptions.unchecked(e, "Error while getting JDBC connection. Details: " + details.get());
		}
	}

	public static void withPreparedStatement(Connection c, String sql, List<?> params, Supplier<String> details, XConsumer<PreparedStatement> task) {
		Supplier<String> detailsWithSql = () -> details.get() + " Sql: " + sql + paramsInfo(params);

		PreparedStatement statement = createPreparedStatement(c, sql, detailsWithSql);

		withStatement(statement, detailsWithSql, task);
	}

	private static String paramsInfo(List<?> params) {
		return isEmpty(params) ? "" : ", Parameters: " + toStringWithElementTypes(params);
	}

	private static PreparedStatement createPreparedStatement(Connection c, String sql, Supplier<String> details) {
		try {
			return c.prepareStatement(sql);
		} catch (SQLException e) {
			throw Exceptions.unchecked(e, "Error while creating prepared statement. Details: " + details.get());
		}
	}

	public static void withStatement(Connection c, Supplier<String> details, XConsumer<Statement> task) {
		Statement statement = createStatement(c, details);

		withStatement(statement, details, task);
	}

	private static Statement createStatement(Connection c, Supplier<String> details) {
		try {
			return c.createStatement();
		} catch (SQLException e) {
			throw Exceptions.unchecked(e, "Error while creating statement. Details: " + details.get());
		}
	}

	private static <T extends Statement> void withStatement(T statement, Supplier<String> details, XConsumer<T> task) {
		Exception taskException = null;

		try {
			task.accept(statement);

		} catch (Exception e) {
			taskException = e;

		} finally {
			try {
				statement.close();
			} catch (Exception e) {
				if (taskException != null)
					e.addSuppressed(taskException);
				throw Exceptions.unchecked(e, "Error while closing statement. Details: " + details.get());
			}

			if (taskException != null)
				throw Exceptions.unchecked(taskException, "Error while executing statement. Details: " + details.get());
		}
	}

	public static void thoroughlyCloseJdbcConnection(Connection connection, boolean commit, Supplier<String> details) {
		if (connection == null)
			return;

		try {
			if (connection.isClosed())
				return;
		} catch (Exception e) {
			log.error("Failed to check if JDBC connection was closed. Attempt to close it will follow. Details: ." + details.get(), e);
		}

		try {
			if (!connection.getAutoCommit()) {
				if (commit)
					connection.commit();
				else
					connection.rollback();
			}

		} catch (Exception e) {
			String action = commit ? "commmit" : "rollback";
			log.error("Failed to " + action + " JDBC connection before closing it. Close attmpt will follow. Details: " + details.get(), e);
		}

		try {
			connection.close();

		} catch (Exception e) {
			log.error("Failed to close JDBC connection. Details: " + details.get(), e);
		}
	}

	/* IMPLEMENTATION NOTE:
	 * 
	 * Originally, this used MetaData.getTables(null, null, tableName, new String[] { "TABLE"}). This, however, caused problems for the PostreSQL DB,
	 * because calling "create table TABLE_NAME" there creates a table called "table_name" (i.e. all chars uncapitalized).
	 *
	 * The next attempt was to retrieve all tables, but in case of Oracle it took extremely long time - in a test with empty oracle DB running in
	 * Docker it was over 50 seconds.
	 * 
	 * Querying the table with a false condition seems to work fine so far. */
	public static String tableExists(Connection connection, String tableName) {
		// This resolves the value for PostgreSQL, MySql and even Derby
		String result = tableExistsQuick(connection, tableName);
		if (!"".equals(result))
			return result;

		/* Getting here means the table exists, as the Quick check which does a query returned a result, but table name was not available Let's now
		 * try to find the table using Connection MetaData */

		// Let's try the exact table name // SQL Server typically ends here
		result = tableExistsViaMd(connection, tableName, tableName);
		if (result != null)
			return result;

		// Let's try UPPERCASE // Oracle might end here, if given 'tableName' was not uppercase already)
		result = tableExistsViaMd(connection, tableName.toUpperCase(), tableName);
		if (result != null)
			return result;

		// Not sure if any other DB can get to the remaining cases, but just to be sure...

		// Let's try LOWERCASE
		result = tableExistsViaMd(connection, tableName.toLowerCase(), tableName);
		if (result != null)
			return result;

		result = tableExistsViaMd(connection, null, tableName);
		if (result != null)
			return result;

		return tableName;
	}

	private static String tableExistsQuick(Connection connection, String tableName) {
		// If the table does not exist, the SQLException may be thrown here or on executeQuery
		try (PreparedStatement ps = connection.prepareStatement("select * from " + tableName /* + " where 1=0" */)) {
			try (ResultSet rs = ps.executeQuery()) {
				String actualTableName = rs.getMetaData().getTableName(1 /* first column */);
				// for whatever reason MS SQL and Oracle return an empty string here
				// https://stackoverflow.com/questions/49924292/resultsetmetadata-gettablename-return-empty
				return actualTableName;
			}

		} catch (SQLException e) {
			return null;

		} finally {
			rollbackIfNeededAndPossible(connection, () -> "Error whie checking if table exits: " + tableName);
		}
	}

	private static String tableExistsViaMd(Connection connection, String tablePattern, String tableName) {
		try (ResultSet rs = connection.getMetaData().getTables(null, null, tablePattern, new String[] { "TABLE" })) {
			while (rs.next()) {
				String name = rs.getString("TABLE_NAME");
				if (tableName.equalsIgnoreCase(name))
					return name;
			}

			return null;

		} catch (SQLException e) {
			return null;

		} finally {
			rollbackIfNeededAndPossible(connection, () -> "Error while checking if table exits: " + tableName);
		}
	}

	public static Set<String> columnsExist(Connection connection, String table, Collection<String> columns) {
		if (columns.isEmpty())
			return emptySet();

		try {
			ResultSet rs = connection.getMetaData().getColumns(null, null, table, null);
			return findPresentValues(rs, columns, "COLUMN_NAME");

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while checking if columns exist: " + columns);
		}
	}

	public static Set<String> indicesExist(Connection connection, String table, Collection<String> indices) {
		if (indices.isEmpty())
			return emptySet();

		try {
			ResultSet rs = connection.getMetaData().getIndexInfo(null, null, table, false, false);
			return findPresentValues(rs, indices, "INDEX_NAME");

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while checking if indices exist: " + indices);
		}
	}

	private static Set<String> findPresentValues(ResultSet rs, Iterable<String> valuesToCheck, String columnName) throws SQLException {
		Set<String> result = newSet();
		Set<String> presentValuesLowerCase = newSet();

		try {
			while (rs.next()) {
				String value = rs.getString(columnName);
				if (value != null) // Some index in MS SQL has null name...
					presentValuesLowerCase.add(value.toLowerCase());
			}

			for (String valueToCheck : valuesToCheck) {
				if (presentValuesLowerCase.contains(valueToCheck.toLowerCase()))
					result.add(valueToCheck);
			}

			return result;

		} finally {
			rs.close();
		}
	}

	public static void rollbackIfNeededAndPossible(Connection connection, Supplier<String> errorMsgSupplier) {
		try {
			if (!connection.isClosed() && !connection.getAutoCommit())
				connection.rollback();

		} catch (Exception e) {
			throw Exceptions.unchecked(e, errorMsgSupplier.get());
		}
	}

}
