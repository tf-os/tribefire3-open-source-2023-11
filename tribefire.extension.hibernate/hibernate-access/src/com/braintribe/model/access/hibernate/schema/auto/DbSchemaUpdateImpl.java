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
package com.braintribe.model.access.hibernate.schema.auto;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.codec.string.HashCodec;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.lock.api.Locking;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;
import com.braintribe.utils.lcd.StopWatch;

//TODO: clob for accessId, instanceId, context
//TODO: add date for last error, last hash update
//TODO: service for resetting of values (e.g. tribefire.extension.hibernate) - only works for non required/system db
//TODO: store content of hbm files in blob (e.g. zip) for analysis without db - put into diagnostic package - hbm directly from file system?
//TODO: this need to remove the _TMP postfix

// COVERED CASES:
// - no entry in DB; everything up and running: run schema update (hash=actual; errorCount=0)
// - entry in DB; everything up and running: no schema update (hash=actual; errorCount=0)
// - no entry in DB; error during postConstruct before table init: no entry in db, no schema update
// - no/initial entry in DB; error during postConstruct after table init: no schema update (hash=initial; errorCount>=1)
// - no/initial entry in DB; error before schema update check: no schema update (hash=initial; errorCount>=1)
// - no/initial entry in DB; error during schema update check: no schema update (hash=initial; errorCount>=1)
// - no/initial entry in DB; error during schema update: no schema update (hash=initial; errorCount>=1)
// - no/initial entry in DB; error after schema update (outside this code): no schema update (hash=initial; errorCount>=1)
/**
 * Checks if a DB schema update needs to be done for a Hibernate access. It creates a hash over all hbm files which then
 * gets compared to the existing hash stored in a technical table {@link DbSchemaUpdateImpl#TABLE_NAME}. If the hashes
 * are equal then the DB schema update mechanism of Hibernate is skipped. Otherwise the update is run and noted in
 * {@link DbSchemaUpdateImpl#TABLE_NAME}.
 * 
 * The mechanism can be overridden by 'TF_HIBERNATE_SCHEMA_UPDATE' runtime property to be forced to be disabled or
 * enabled.
 * 
 */
public class DbSchemaUpdateImpl implements LifecycleAware {

	protected static Logger logger = Logger.getLogger(DbSchemaUpdateImpl.class);

	private static final String BEFORE_ENSURE = "BEFORE_ENSURE";
	private static final String AFTER_ENSURE = "AFTER_ENSURE";
	private static final String BEFORE_SCHEMA_UPDATE = "BEFORE_SCHEMA_UPDATE";
	private static final String ON_UNDEPLOY = "ON_UNDEPLOY";
	private static final String ON_NO_SCHEMA_UPDATE_REQUIRED = "ON_NO_SCHEMA_UPDATE_REQUIRED";
	private static final String AFTER_SCHEMA_UPDATE_FAILED = "AFTER_SCHEMA_UPDATE_FAILED";
	private static final String AFTER_SCHEMA_UPDATE_SUCCESSFUL = "AFTER_SCHEMA_UPDATE_SUCCESSFUL";

	private static final String HASH_ALGORITHM = "SHA-1";
	private static final String TABLE_NAME = "TF_SCHEMA_UPDATE_TMP";

	private static final String COLUMN_ACCESS_ID = "ACCESS_ID";
	private static final String COLUMN_HASH = "HASH";
	private static final String COLUMN_ERROR_COUNT = "ERROR_COUNT";
	private static final String COLUMN_INSTANCE_ID = "INSTANCE_ID";
	private static final String COLUMN_CONTEXT = "CONTEXT";

	// -----------------------
	// PROPERTIES
	// -----------------------

	private Locking locking;
	private File mappingDirectory;
	private DataSource dataSource;
	private String accessId;
	private String instanceId;
	private Supplier<String> dbSchemaUpdateContextProvider;
	private Boolean overrideDbSchemaUpdate;

	// @formatter:off
	@Required public void setLocking(Locking locking) { this.locking = locking; }
	@Required public void setMappingDirectory(File mappingDirectory) { this.mappingDirectory = mappingDirectory; }
	@Required public void setDataSource(DataSource dataSource) { this.dataSource = dataSource; }
	@Required public void setAccessId(String accessId) { this.accessId = accessId; }
	@Required public void setInstanceId(String instanceId) { this.instanceId = instanceId; }
	@Required public void setDbSchemaUpdateContextProvider(Supplier<String> dbSchemaUpdateContextProvider) { this.dbSchemaUpdateContextProvider = dbSchemaUpdateContextProvider; }
	@Required public void setOverrideDbSchemaUpdate(Boolean overrideDbSchemaUpdate) { this.overrideDbSchemaUpdate = overrideDbSchemaUpdate; }
	// @formatter:on

	// -----------------------
	// LOCAL VARIABLES
	// -----------------------

	private Lock lock;
	private HashCodec hashCodec;
	private String context; // used for locking - restricts on a DB schema (potentially blocks multiple accesses)
	private String actualHash; // transported from check schema update to schema update
	private boolean schemaUpdateSuccessfullyFinished; // true iff schema was successfully update

	// -----------------------------------------------------------------------
	// LifecycleAware
	// -----------------------------------------------------------------------

	@Override
	public void postConstruct() {
		logger.debug(() -> "Starting initialize schema update mechanism for " + stringify());

		context = dbSchemaUpdateContextProvider.get();

		lock = locking.forIdentifier(context).writeLock();
		hashCodec = new HashCodec(HASH_ALGORITHM);

		schemaUpdateSuccessfullyFinished = false;

		lock(BEFORE_ENSURE);
		try {
			ensureSchemaUpdateTable();
			ensureSchemaUpdateEntry();
		} finally {
			unlock(AFTER_ENSURE);
		}

		logger.debug(() -> "Finished initialize schema update mechanism for " + stringify());

	}

	@Override
	public void preDestroy() {
		// in case of an error: increase the errorCount by 1; having the errorCount available
		// allows to stop in clustered environments to retry the schema update one every node
		if (!schemaUpdateSuccessfullyFinished) {

			PreparedStatement query = null;
			String updateString = null;
			PreparedStatement update = null;

			int actualErrorCount = 0;
			try (Connection connection = dataSource.getConnection()) {
				connection.setAutoCommit(false);

				query = connection.prepareStatement(resolveQueryStatement());

				int numberOfRows = 0;
				try (ResultSet rs = query.executeQuery()) {
					while (rs.next()) {
						String accessId = rs.getString(COLUMN_ACCESS_ID);
						String hash = rs.getString(COLUMN_HASH);
						actualErrorCount = rs.getInt(COLUMN_ERROR_COUNT);
						String instanceId = rs.getString(COLUMN_INSTANCE_ID);
						String context = rs.getString(COLUMN_CONTEXT);

						if (logger.isDebugEnabled()) {
							logger.debug("Fetched entry: '" + stringifyEntry(accessId, hash, actualErrorCount, instanceId, context));
						}
						numberOfRows++;
					}
				}

				if (numberOfRows != 0) {
					actualErrorCount++;

					updateString = resolveUpdateErrorCountStatement();
					update = connection.prepareStatement(updateString);
					update.setInt(1, actualErrorCount);
					update.setString(2, instanceId);
					update.setString(3, context);
					update.setString(4, accessId);
					update.execute();

					connection.commit();

					logger.info(() -> "Successfully updated errorCount by 1 for '" + stringify() + "'");
				}
			} catch (Exception e) {
				logger.error(
						() -> "Could not update errorCount by 1 after receiving an error during schema update procedure - this is not recoverable - '"
								+ stringify() + "'",
						e);
			} finally {
				IOTools.closeCloseable(query, logger);
				IOTools.closeCloseable(update, logger);
			}

		}

		if (!schemaUpdateSuccessfullyFinished) {

			// be paranoid: release the lock for sure (should only be necessary in case of an error)
			try {
				unlock(ON_UNDEPLOY);
			} catch (IllegalMonitorStateException imse) {
				// already unlocked - no issue
				logger.trace(() -> "Lock for " + stringify() + " could not be released - no issue, it was already unlocked before");
			}
		}

	}

	// -----------------------------------------------------------------------
	// METHODS
	// -----------------------------------------------------------------------

	public boolean needsSchemaUpdate() {
		StopWatch stopWatch = new StopWatch();

		actualHash = mappingDirectoryHash();
		stopWatch.intermediate("Hash");

		lock(BEFORE_SCHEMA_UPDATE);
		stopWatch.intermediate("Lock Acquired");

		boolean schemaUpdateRequired = schemaUpdateRequired();
		stopWatch.intermediate("Check Update");

		if (!schemaUpdateRequired) {
			unlock(ON_NO_SCHEMA_UPDATE_REQUIRED);
			stopWatch.intermediate("Unlock");
			schemaUpdateSuccessfullyFinished = true;
			// no matter if the schema update is wrong or not - it won't get repeated
		}

		logger.debug(() -> "Checked for schema update required: " + schemaUpdateRequired + ": " + stopWatch);

		return schemaUpdateRequired;
	}

	public void storeDbSchemaConfiguration() {
		Objects.requireNonNull(actualHash, "hash must be set previously");

		PreparedStatement query = null;
		String insertString = null;
		PreparedStatement insert = null;
		String updateString = null;
		PreparedStatement update = null;

		try (Connection connection = dataSource.getConnection()) {
			// no transaction necessary - covered by lock
			query = connection.prepareStatement(resolveQueryStatement());

			try (ResultSet rs = query.executeQuery()) {
				updateString = resolveUpdateStatement();
				update = connection.prepareStatement(updateString);
				update.setString(1, actualHash);
				update.setInt(2, 0);
				update.setString(3, instanceId);
				update.setString(4, context);
				update.setString(5, accessId);
				update.execute();
				logger.debug(() -> "Successfully updated hash: '" + actualHash + "' from '" + resolveTableName() + "'");
			}
			unlock(AFTER_SCHEMA_UPDATE_SUCCESSFUL);
		} catch (Exception e) {
			unlock(AFTER_SCHEMA_UPDATE_FAILED);
			throw Exceptions.unchecked(e, "Could not store hash in dattabase using query: '" + resolveQueryStatement() + "' insert: '" + insertString
					+ "' update: '" + updateString + "'");
		} finally {
			IOTools.closeCloseable(query, logger);
			IOTools.closeCloseable(insert, logger);
			IOTools.closeCloseable(update, logger);

		}
		schemaUpdateSuccessfullyFinished = true;
		// no line allowed here any more
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	private boolean schemaUpdateRequired() {
		boolean dbOpinion = schemaUpdateRequiredBasedOnDb();
		if (overrideDbSchemaUpdate == null)
			return dbOpinion;

		logger.info("Force DB schema update via environment to '" + overrideDbSchemaUpdate + "'. Otherwise it would be: '" + dbOpinion + "'");
		return overrideDbSchemaUpdate;
	}

	private boolean schemaUpdateRequiredBasedOnDb() {

		PreparedStatement queryStatement = null;

		try (Connection connection = dataSource.getConnection(); Statement stmt = connection.createStatement()) {

			String queryStatementSql = resolveQueryStatement();
			queryStatement = connection.prepareStatement(queryStatementSql);

			// if this fails we have a real problem
			try (ResultSet rs = queryStatement.executeQuery()) {

				int numberOfRows = 0;

				String existingHash = null;
				while (rs.next()) {
					String accessId = rs.getString(COLUMN_ACCESS_ID);
					String hash = rs.getString(COLUMN_HASH);
					int errorCount = rs.getInt(COLUMN_ERROR_COUNT);
					String instanceId = rs.getString(COLUMN_INSTANCE_ID);
					String context = rs.getString(COLUMN_CONTEXT);
					numberOfRows++;

					existingHash = hash;

					logger.debug(() -> "Fetched: " + stringifyEntry(accessId, hash, errorCount, instanceId, context) + " schema update entry");
				}
				validateNumberOfFetchedRows(numberOfRows);

				boolean equals = existingHash.equals(actualHash);
				if (logger.isDebugEnabled()) {
					logger.debug("Successfully retrieved hash: '" + existingHash + "' actualHash: '" + actualHash + "' equals: '" + equals + "' for '"
							+ stringify() + "'");
				}
				if (!equals) {
					// if the hash has changed, update for sure
					return true;
				}
				return false;
			}
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not fetch hash from database using query: '" + resolveQueryStatement()
					+ "'. Before table was create using: '" + resolveCreateTableStatement() + "' for " + stringify());
		} finally {
			IOTools.closeCloseable(queryStatement, logger);
		}
	}

	private String mappingDirectoryHash() {
		// get String content of all files in all directories in a sorted order and concatenate them (stable ordering)
		String value = FileTools.listFiles(mappingDirectory, File::isFile).stream() //
				.sorted(Comparator.comparing(File::getName)) //
				.map(file -> FileTools.readStringFromFile(file)) //
				.collect(Collectors.joining("||"));

		return hashCodec.encode(value);
	}

	// -----------------------
	// LOCK
	// -----------------------

	private void lock(String loggingContext) {
		logger.debug(() -> "Trying to get lock for: '" + stringify() + "' [" + loggingContext + "]");
		lock.lock();
		logger.debug(() -> "Received lock for: '" + stringify() + "' [" + loggingContext + "]");
	}

	private void unlock(String loggingContext) {
		logger.debug(() -> "Trying to unlock for: " + stringify() + "' [" + loggingContext + "]");
		lock.unlock();
		logger.debug(() -> "Successfully unlocked for: " + stringify() + "' [" + loggingContext + "]");
	}

	// -----------------------
	// STRING
	// -----------------------

	private String stringify() {
		return "'" + DbSchemaUpdateImpl.class.getSimpleName() + "': directory: '" + mappingDirectory.getAbsolutePath() + "' accessId: '" + accessId
				+ "' instanceId: '" + instanceId + "' table: '" + resolveTableName() + "' schemaUpdateFinished: '" + schemaUpdateSuccessfullyFinished
				+ "'";
	}

	private String stringifyEntry(String accessId, String hash, int actualErrorCount, String instanceId, String context) {
		return "accessId: '" + accessId + "' hash: '" + hash + "' actualErrorCount: '" + actualErrorCount + "' instanceId: '" + instanceId
				+ "' context: '" + context + "'";
	}

	// -----------------------
	// ENSURE DB ENTRIES
	// -----------------------

	private void ensureSchemaUpdateEntry() {

		PreparedStatement query = null;
		String insertString = null;
		PreparedStatement insert = null;

		try (Connection connection = dataSource.getConnection()) {
			// no transaction necessary - covered by lock

			query = connection.prepareStatement(resolveQueryStatement());

			try (ResultSet rs = query.executeQuery()) {
				int numberOfFetchedRows = 0;
				while (rs.next()) {
					numberOfFetchedRows++;
				}

				validateNumberOfFetchedRows(numberOfFetchedRows);

				if (numberOfFetchedRows == 0) {
					String hash = "notInitialized";
					int actualErrorCount = 0;

					// insert for the first time
					insertString = resolveInsertStatement();
					insert = connection.prepareStatement(insertString);
					insert.setString(1, accessId);
					insert.setString(2, hash);
					insert.setInt(3, actualErrorCount);
					insert.setString(4, instanceId);
					insert.setString(5, context);

					insert.execute();

					stringifyEntry(accessId, hash, actualErrorCount, instanceId, context);
					logger.debug(() -> "Successfully added inserted: '");
				}
			}
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not ensure schema update table entry for " + stringify());
		} finally {
			IOTools.closeCloseable(query, logger);
			IOTools.closeCloseable(insert, logger);
		}
	}

	private void validateNumberOfFetchedRows(int numberOfRows) {
		if (numberOfRows > 1 || numberOfRows < 0) {
			throw new IllegalStateException("Must fetch either 0 or 1 schema update entry from db for " + stringify()
					+ ". There must be 0 or 1 entry but there are '" + numberOfRows + "'!");
		}
	}

	// -----------------------
	// ENSURE DB TABLE
	// -----------------------

	private void ensureSchemaUpdateTable() {
		String tableName = TABLE_NAME;
		String sqlStatement = resolveCreateTableStatement();

		try {
			Connection connection = dataSource.getConnection();
			try {
				if (tableExists(connection, tableName) == null) {
					logger.debug(() -> "Table '" + tableName + "' does not exist.");
					Statement statement = connection.createStatement();
					try {
						logger.debug(() -> "Creating table with statement: " + sqlStatement);
						statement.executeUpdate(sqlStatement);
						logger.debug(() -> "Successfully created table '" + tableName + "'");
					} catch (SQLException e) {
						if (tableExists(connection, tableName) != null)
							return;
						else
							throw e;
					} finally {
						statement.close();
					}
				}
			} finally {
				connection.close();
			}
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not ensure schema update table for " + stringify());
		}
	}

	/* IMPLEMENTATION NOTE: Originally, the third parameter for the MetaData.getTables(...) invocation (i.e.
	 * tableNamePattern) was not null, but the actual name of the table (i.e. "TF_DSTLCK"). This, however, caused
	 * problems for the PostreSQL DB, because calling "create table TF_DSTLCK" there creates a table called "tf_dstlck"
	 * (i.e. all chars uncapitalized). To avoid this problem, and possible future problems with different conventions,
	 * we simply retrieve all the tables and then perform a case-insensitive check for each table name. */
	private String tableExists(Connection connection, String tablename) throws SQLException {
		Instant start = NanoClock.INSTANCE.instant();
		ResultSet rs = connection.getMetaData().getTables(null, null, null, new String[] { "TABLE" });
		try {
			while (rs.next()) {
				String tableName = rs.getString("TABLE_NAME");
				if (tableName.equalsIgnoreCase(tablename)) {
					return tableName;
				}
			}
			return null;
		} finally {
			rs.close();
			Duration duration = Duration.between(start, NanoClock.INSTANCE.instant());
			if (duration.toMillis() > (Numbers.MILLISECONDS_PER_SECOND * 5)) {
				logger.info(() -> "The check for the existence of " + tablename + " took "
						+ StringTools.prettyPrintDuration(duration, true, ChronoUnit.MILLIS));
			} else {
				logger.debug(() -> "The check for the existence of " + tablename + " took "
						+ StringTools.prettyPrintDuration(duration, true, ChronoUnit.MILLIS));
			}
		}
	}

	// -----------------------------------------------------------------------
	// SQL STATEMENTS
	// -----------------------------------------------------------------------

	private String resolveQueryStatement() {
		//@formatter:off
		return "SELECT " + 
				COLUMN_ACCESS_ID + ", " + 
				COLUMN_HASH + ", " + 
				COLUMN_ERROR_COUNT + ", " + 
				COLUMN_INSTANCE_ID + ", " + 
				COLUMN_CONTEXT + 
			" FROM " + resolveTableName() + " WHERE " + COLUMN_ACCESS_ID + "='" + accessId + "'";
		//@formatter:on
	}
	private String resolveInsertStatement() {
		//@formatter:off
		return "INSERT INTO " + resolveTableName() + " (" + 
				COLUMN_ACCESS_ID + ", " + 
				COLUMN_HASH + ", " + 
				COLUMN_ERROR_COUNT + ", " + 
				COLUMN_INSTANCE_ID + ", " + 
				COLUMN_CONTEXT + 
			") values (?, ?, ?, ?, ?)";
		//@formatter:on
	}
	private String resolveCreateTableStatement() {
		// using 'primary key' instead of 'not null' and 'unique' because the syntax for different DBs is different
		//@formatter:off
		return "CREATE TABLE " + resolveTableName() + " (" +
				COLUMN_ACCESS_ID + " varchar(255) primary key," +
				COLUMN_HASH + " varchar(255) not null," +
				COLUMN_ERROR_COUNT + " INTEGER not null," +
				COLUMN_INSTANCE_ID + " varchar(255) not null," +
				COLUMN_CONTEXT + " varchar(255) not null" +
			")";
		//@formatter:on
	}
	private String resolveUpdateStatement() {
		//@formatter:off
		return "UPDATE " + resolveTableName() + " " +
				"SET " + 
					COLUMN_HASH + "=?, " +
					COLUMN_ERROR_COUNT + "=?, " +
					COLUMN_INSTANCE_ID + "=?, " +
					COLUMN_CONTEXT + "=? " +
		       	"WHERE " + COLUMN_ACCESS_ID + "=?";
		//@formatter:on
	}
	private String resolveUpdateErrorCountStatement() {
		//@formatter:off
		return "UPDATE " + resolveTableName() + " " +
				"SET " + 
					COLUMN_ERROR_COUNT + "=?, " +
					COLUMN_INSTANCE_ID + "=?, " +
					COLUMN_CONTEXT + "=? " +
		       	"WHERE " + COLUMN_ACCESS_ID + "=?";
		//@formatter:on
	}
	private String resolveTableName() {
		return TABLE_NAME;
	}

}
