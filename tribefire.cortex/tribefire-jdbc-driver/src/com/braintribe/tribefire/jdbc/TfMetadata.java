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
package com.braintribe.tribefire.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.tribefire.jdbc.resultset.GenericResultSet;

/**
 * The Class TfDatabaseMetaData.
 *
 */
public class TfMetadata implements DatabaseMetaData {

	private final TfConnection connection;
	private final Logger logger = Logger.getLogger(TfMetadata.class);

	public static final int JDBC_MINOR_VERSION = 4;
	public static final int JDBC_MAJOR_VERSION = 1;
	public static final int TRIBEFIRE_MAJOR_VERSION = 2;
	public static final int TRIBEFIRE_MINOR_VERSION = 0;
	public static final String TRIBEFIRE_VERSION = TRIBEFIRE_MAJOR_VERSION + "." + TRIBEFIRE_MINOR_VERSION;
	public static final int DRIVER_MAJOR_VERSION = 0;
	public static final int DRIVER_MINOR_VERSION = 1;
	public static final String DRIVER_VERSION_SUFFIX = "alpha01";
	public static final String DRIVER_FULL_NAME = "tribefire JDBC link";
	public static final String DRIVER_NAME = "tribefire";
	public static final String DRIVER_VERSION = DRIVER_MAJOR_VERSION + "." + DRIVER_MINOR_VERSION
			+ DRIVER_VERSION_SUFFIX;

	/**
	 * Instantiates a new tf database meta data.
	 *
	 * @param connection
	 *            the connection
	 */
	public TfMetadata(TfConnection connection) {
		this.connection = connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#allProceduresAreCallable()
	 */
	@Override
	public boolean allProceduresAreCallable() throws SQLException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#allTablesAreSelectable()
	 */
	@Override
	public boolean allTablesAreSelectable() throws SQLException {
		return true;
	}

	@Override
	public String getURL() throws SQLException {
		return "http://www.tribefire.com";
	}

	@Override
	public String getUserName() throws SQLException {
		return connection.getUsername();
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#nullsAreSortedHigh()
	 */
	@Override
	public boolean nullsAreSortedHigh() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#nullsAreSortedLow()
	 */
	@Override
	public boolean nullsAreSortedLow() throws SQLException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#nullsAreSortedAtStart()
	 */
	@Override
	public boolean nullsAreSortedAtStart() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#nullsAreSortedAtEnd()
	 */
	@Override
	public boolean nullsAreSortedAtEnd() throws SQLException {
		return true;
	}

	/* (non-Javadoc)
	 * @see java.sql.DatabaseMetaData#getDatabaseProductName()
	 */
	@Override
	public String getDatabaseProductName() {
		return DRIVER_NAME;
	}

	/* (non-Javadoc)
	 * @see java.sql.DatabaseMetaData#getDatabaseProductVersion()
	 */
	@Override
	public String getDatabaseProductVersion() {
		return TRIBEFIRE_VERSION;
	}

	/* (non-Javadoc)
	 * @see java.sql.DatabaseMetaData#getDriverName()
	 */
	@Override
	public String getDriverName() {
		return DRIVER_FULL_NAME;
	}

	/* (non-Javadoc)
	 * @see java.sql.DatabaseMetaData#getDriverVersion()
	 */
	@Override
	public String getDriverVersion() {
		return DRIVER_VERSION;
	}

	/* (non-Javadoc)
	 * @see java.sql.DatabaseMetaData#getDriverMajorVersion()
	 */
	@Override
	public int getDriverMajorVersion() {
		return DRIVER_MAJOR_VERSION;
	}

	/* (non-Javadoc)
	 * @see java.sql.DatabaseMetaData#getDriverMinorVersion()
	 */
	@Override
	public int getDriverMinorVersion() {
		return DRIVER_MINOR_VERSION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#usesLocalFiles()
	 */
	@Override
	public boolean usesLocalFiles() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#usesLocalFilePerTable()
	 */
	@Override
	public boolean usesLocalFilePerTable() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsMixedCaseIdentifiers()
	 */
	@Override
	public boolean supportsMixedCaseIdentifiers() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#storesUpperCaseIdentifiers()
	 */
	@Override
	public boolean storesUpperCaseIdentifiers() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#storesLowerCaseIdentifiers()
	 */
	@Override
	public boolean storesLowerCaseIdentifiers() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#storesMixedCaseIdentifiers()
	 */
	@Override
	public boolean storesMixedCaseIdentifiers() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsMixedCaseQuotedIdentifiers()
	 */
	@Override
	public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#storesUpperCaseQuotedIdentifiers()
	 */
	@Override
	public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#storesLowerCaseQuotedIdentifiers()
	 */
	@Override
	public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#storesMixedCaseQuotedIdentifiers()
	 */
	@Override
	public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
		return false;
	}

	@Override
	public String getIdentifierQuoteString() throws SQLException {
		return " ";
	}

	@Override
	public String getSQLKeywords() throws SQLException {
		return "";
	}

	@Override
	public String getNumericFunctions() throws SQLException {
		return "";
	}

	@Override
	public String getStringFunctions() throws SQLException {
		return "";
	}

	@Override
	public String getSystemFunctions() throws SQLException {
		return "";
	}

	@Override
	public String getTimeDateFunctions() throws SQLException {
		return "";
	}

	@Override
	public String getSearchStringEscape() throws SQLException {
		return ""; // TODO
	}

	@Override
	public String getExtraNameCharacters() throws SQLException {
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsAlterTableWithAddColumn()
	 */
	@Override
	public boolean supportsAlterTableWithAddColumn() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsAlterTableWithDropColumn()
	 */
	@Override
	public boolean supportsAlterTableWithDropColumn() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsColumnAliasing()
	 */
	@Override
	public boolean supportsColumnAliasing() throws SQLException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#nullPlusNonNullIsNull()
	 */
	@Override
	public boolean nullPlusNonNullIsNull() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsConvert()
	 */
	@Override
	public boolean supportsConvert() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsConvert(int, int)
	 */
	@Override
	public boolean supportsConvert(int fromType, int toType) throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsTableCorrelationNames()
	 */
	@Override
	public boolean supportsTableCorrelationNames() throws SQLException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsDifferentTableCorrelationNames()
	 */
	@Override
	public boolean supportsDifferentTableCorrelationNames() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsExpressionsInOrderBy()
	 */
	@Override
	public boolean supportsExpressionsInOrderBy() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsOrderByUnrelated()
	 */
	@Override
	public boolean supportsOrderByUnrelated() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsGroupBy()
	 */
	@Override
	public boolean supportsGroupBy() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsGroupByUnrelated()
	 */
	@Override
	public boolean supportsGroupByUnrelated() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsGroupByBeyondSelect()
	 */
	@Override
	public boolean supportsGroupByBeyondSelect() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsLikeEscapeClause()
	 */
	@Override
	public boolean supportsLikeEscapeClause() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsMultipleResultSets()
	 */
	@Override
	public boolean supportsMultipleResultSets() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsMultipleTransactions()
	 */
	@Override
	public boolean supportsMultipleTransactions() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsNonNullableColumns()
	 */
	@Override
	public boolean supportsNonNullableColumns() throws SQLException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsMinimumSQLGrammar()
	 */
	@Override
	public boolean supportsMinimumSQLGrammar() throws SQLException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsCoreSQLGrammar()
	 */
	@Override
	public boolean supportsCoreSQLGrammar() throws SQLException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsExtendedSQLGrammar()
	 */
	@Override
	public boolean supportsExtendedSQLGrammar() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsANSI92EntryLevelSQL()
	 */
	@Override
	public boolean supportsANSI92EntryLevelSQL() throws SQLException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsANSI92IntermediateSQL()
	 */
	@Override
	public boolean supportsANSI92IntermediateSQL() throws SQLException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsANSI92FullSQL()
	 */
	@Override
	public boolean supportsANSI92FullSQL() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsIntegrityEnhancementFacility()
	 */
	@Override
	public boolean supportsIntegrityEnhancementFacility() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsOuterJoins()
	 */
	@Override
	public boolean supportsOuterJoins() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsFullOuterJoins()
	 */
	@Override
	public boolean supportsFullOuterJoins() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsLimitedOuterJoins()
	 */
	@Override
	public boolean supportsLimitedOuterJoins() throws SQLException {
		return false;
	}

	@Override
	public String getSchemaTerm() throws SQLException {
		return "Access";
	}

	@Override
	public String getProcedureTerm() throws SQLException {
		return null;
	}

	@Override
	public String getCatalogTerm() throws SQLException {
		return null;
	}

	@Override
	public boolean isCatalogAtStart() throws SQLException {
		return false;
	}

	@Override
	public String getCatalogSeparator() throws SQLException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsSchemasInDataManipulation()
	 */
	@Override
	public boolean supportsSchemasInDataManipulation() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsSchemasInProcedureCalls()
	 */
	@Override
	public boolean supportsSchemasInProcedureCalls() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsSchemasInTableDefinitions()
	 */
	@Override
	public boolean supportsSchemasInTableDefinitions() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsSchemasInIndexDefinitions()
	 */
	@Override
	public boolean supportsSchemasInIndexDefinitions() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsSchemasInPrivilegeDefinitions()
	 */
	@Override
	public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsCatalogsInDataManipulation()
	 */
	@Override
	public boolean supportsCatalogsInDataManipulation() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsCatalogsInProcedureCalls()
	 */
	@Override
	public boolean supportsCatalogsInProcedureCalls() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsCatalogsInTableDefinitions()
	 */
	@Override
	public boolean supportsCatalogsInTableDefinitions() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsCatalogsInIndexDefinitions()
	 */
	@Override
	public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsCatalogsInPrivilegeDefinitions()
	 */
	@Override
	public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsPositionedDelete()
	 */
	@Override
	public boolean supportsPositionedDelete() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsPositionedUpdate()
	 */
	@Override
	public boolean supportsPositionedUpdate() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsSelectForUpdate()
	 */
	@Override
	public boolean supportsSelectForUpdate() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsStoredProcedures()
	 */
	@Override
	public boolean supportsStoredProcedures() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsSubqueriesInComparisons()
	 */
	@Override
	public boolean supportsSubqueriesInComparisons() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsSubqueriesInExists()
	 */
	@Override
	public boolean supportsSubqueriesInExists() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsSubqueriesInIns()
	 */
	@Override
	public boolean supportsSubqueriesInIns() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsSubqueriesInQuantifieds()
	 */
	@Override
	public boolean supportsSubqueriesInQuantifieds() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsCorrelatedSubqueries()
	 */
	@Override
	public boolean supportsCorrelatedSubqueries() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsUnion()
	 */
	@Override
	public boolean supportsUnion() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsUnionAll()
	 */
	@Override
	public boolean supportsUnionAll() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsOpenCursorsAcrossCommit()
	 */
	@Override
	public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsOpenCursorsAcrossRollback()
	 */
	@Override
	public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsOpenStatementsAcrossCommit()
	 */
	@Override
	public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsOpenStatementsAcrossRollback()
	 */
	@Override
	public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
		return false;
	}

	@Override
	public int getMaxBinaryLiteralLength() throws SQLException {
		return 10000000;
	}

	@Override
	public int getMaxCharLiteralLength() throws SQLException {
		return 10000000;
	}

	@Override
	public int getMaxColumnNameLength() throws SQLException {
		return 256;
	}

	@Override
	public int getMaxColumnsInGroupBy() throws SQLException {
		return 256;
	}

	@Override
	public int getMaxColumnsInIndex() throws SQLException {
		return 256;
	}

	@Override
	public int getMaxColumnsInOrderBy() throws SQLException {
		return 256;
	}

	@Override
	public int getMaxColumnsInSelect() throws SQLException {
		return 256;
	}

	@Override
	public int getMaxColumnsInTable() throws SQLException {
		return 2048;
	}

	@Override
	public int getMaxConnections() throws SQLException {
		return 100;
	}

	@Override
	public int getMaxCursorNameLength() throws SQLException {
		return 256;
	}

	@Override
	public int getMaxIndexLength() throws SQLException {
		return 256;
	}

	@Override
	public int getMaxSchemaNameLength() throws SQLException {
		return 256;
	}

	@Override
	public int getMaxProcedureNameLength() throws SQLException {
		return 256;
	}

	@Override
	public int getMaxCatalogNameLength() throws SQLException {
		return 256;
	}

	@Override
	public int getMaxRowSize() throws SQLException {
		return 2048;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#doesMaxRowSizeIncludeBlobs()
	 */
	@Override
	public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
		return false;
	}

	@Override
	public int getMaxStatementLength() throws SQLException {
		return 64000;
	}

	@Override
	public int getMaxStatements() throws SQLException {
		return 64000;
	}

	@Override
	public int getMaxTableNameLength() throws SQLException {
		return 256;
	}

	@Override
	public int getMaxTablesInSelect() throws SQLException {
		return 256;
	}

	@Override
	public int getMaxUserNameLength() throws SQLException {
		return 256;
	}

	@Override
	public int getDefaultTransactionIsolation() throws SQLException {
		return Connection.TRANSACTION_NONE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsTransactions()
	 */
	@Override
	public boolean supportsTransactions() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsTransactionIsolationLevel(int)
	 */
	@Override
	public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#
	 * supportsDataDefinitionAndDataManipulationTransactions()
	 */
	@Override
	public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsDataManipulationTransactionsOnly()
	 */
	@Override
	public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#dataDefinitionCausesTransactionCommit()
	 */
	@Override
	public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#dataDefinitionIgnoredInTransactions()
	 */
	@Override
	public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#getProcedures(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern)
			throws SQLException {
		GenericResultSet grs = new GenericResultSet("PROCEDURE_CAT", "PROCEDURE_SCHEM", "PROCEDURE_NAME", "REMARKS",
				"PROCEDURE_TYPE", "SPECIFIC_NAME");
		grs.setTypes(Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.VARCHAR);
		return grs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#getProcedureColumns(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern,
			String columnNamePattern) throws SQLException {
		throw new SQLFeatureNotSupportedException(getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#getTables(java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String[])
	 */
	@Override
	public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types)
			throws SQLException {
		GenericResultSet grs = new GenericResultSet("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "TABLE_TYPE", "REMARKS",
				"TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME", "SELF_REFERENCING_COL_NAME", "REF_GENERATION");

		for (Map.Entry<String, EntityType<?>> entry : connection.getEntityTypeMap().entrySet()) {
			EntityType<?> et = entry.getValue();
			String shortName = entry.getKey();
			String idColumn = GenericEntity.id;
			grs.addData(null, connection.getAccessId(), shortName, "TABLE", et.getTypeSignature(), null,
					connection.getAccessId(), "TABLE", idColumn, "USER");
		}

		for (Entry<String, Set<TfAssociationTable>> assTabEntry : connection.getAssociationTableMap().entrySet()) {
			for (TfAssociationTable assTab : assTabEntry.getValue()) {

				grs.addData(null, connection.getAccessId(), assTab.getName(), "TABLE", "long", null,
						connection.getAccessId(), "TABLE", GenericEntity.id, "USER");
			}
		}

		return grs;
	}

	@Override
	public ResultSet getSchemas() throws SQLException {
		GenericResultSet grs = new GenericResultSet("TABLE_SCHEM", "TABLE_CATALOG");
		grs.addData(connection.getAccessId(), null);
		return grs;
	}

	@Override
	public ResultSet getCatalogs() throws SQLException {
		GenericResultSet grs = new GenericResultSet("TABLE_CAT");
		return grs;
	}

	@Override
	public ResultSet getTableTypes() throws SQLException {
		GenericResultSet grs = new GenericResultSet("TABLE_TYPE");
		grs.addData("TABLE");
		return grs;
	}

	/**
	 * Gets the type.
	 *
	 * @param p
	 *            the property
	 * @return the type
	 */
	protected int getType(Property p) {
		TypeCode typeCode = p.getType().getTypeCode();
		return getType(typeCode);
	}

	/**
	 * Gets the type.
	 *
	 * @param typeCode
	 *            the type code
	 * @return the type
	 */
	public static int getType(TypeCode typeCode) {
		switch (typeCode) {
		case booleanType:
			return Types.BOOLEAN;
		case dateType:
			return Types.TIMESTAMP;
		case decimalType:
			return Types.DECIMAL;
		case doubleType:
			return Types.DOUBLE;
		case listType:
		case mapType:
		case setType:
		case objectType:
			return Types.JAVA_OBJECT; // needs specific treatment
		case entityType:
			return Types.BIGINT; // only the ID will be returned
		case enumType:
			return Types.VARCHAR;
		case floatType:
			return Types.FLOAT;
		case integerType:
			return Types.INTEGER;
		case longType:
			return Types.INTEGER;
		case stringType:
			return Types.VARCHAR;
		default:
			// fail-fast, this should not happen
			throw new TfJdbcException("Attempted to resolve an unknown type: " + typeCode);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#getColumns(java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern)
			throws SQLException {
		GenericResultSet grs = new GenericResultSet("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME",
				"DATA_TYPE", "TYPE_NAME", "COLUMN_SIZE", "BUFFER_LENGTH", "DECIMAL_DIGITS", "NUM_PREC_RADIX",
				"NULLABLE", "REMARKS", "COLUMN_DEF", "SQL_DATA_TYPE", "SQL_DATETIME_SUB", "CHAR_OCTET_LENGTH",
				"ORDINAL_POSITION", "IS_NULLABLE", "SCOPE_CATALOG", "SCOPE_SCHEMA", "SCOPE_TABLE", "SOURCE_DATA_TYPE",
				"IS_AUTOINCREMENT", "IS_GENERATEDCOLUMN");
		grs.setTypes(Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.VARCHAR,
				Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.VARCHAR, Types.VARCHAR,
				Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,
				Types.VARCHAR, Types.INTEGER, Types.VARCHAR, Types.VARCHAR);

		EntityType<?> entityType = connection.getEntityTypeMap().get(tableNamePattern);

		int index = 1;
		if (entityType != null) { // dealing with an Entity
			String entityTypeShortName = entityType.getShortName();
			for (Property p : entityType.getProperties()) {
				grs.addData(null, connection.getAccessId(), entityTypeShortName, getPropertyName(p), getType(p),
						p.getType().getTypeName(), 64000, 0, 10, 10, columnNullable, getPropertyDescription(p), null, 0, 0, 64000,
						index++, "YES", null, null, null, null, "", "");
			}
		} else {
			TfAssociationTable assTab = connection.getAssociationTableByName(tableNamePattern);
			if (assTab != null) {
				grs.addData(null, connection.getAccessId(), assTab.getName(), assTab.getLeftPropertyDisplayName(),
						getType(assTab.getLeftProperty()), assTab.getLeftProperty().getType().getTypeName(), 64000, 0,
						10, 10, columnNullable, "Virtual mapped property", null, 0, 0, 64000, index++, "YES", null,
						null, null, null, "", "");
				grs.addData(null, connection.getAccessId(), assTab.getName(), assTab.getRightPropertyDisplayName(),
						getType(assTab.getTypeCode()), assTab.getLeftEntityType(), 64000, 0, 10, 10, columnNullable, "",
						"Virtual mapped property", 0, 0, 64000, index++, "YES", null, null, null, null, "", "");
			}
		}
		return grs;
	}

	/**
	 * Gets the property description.
	 *
	 * @param p
	 *            the p
	 * @return the property description
	 */
	private Object getPropertyDescription(Property p) {		
		return "The property " + getPropertyName(p) + " of type " + p.getType().getTypeName();
	}

	/**
	 * Gets the property name.
	 *
	 * @param p
	 *            the p
	 * @return the property name
	 */
	public static String getPropertyName(Property p) {
		GenericModelType type = p.getType();

		switch (type.getTypeCode()) {
		case objectType:
		case entityType:
			if (type instanceof EntityType) {
				return p.getName() + "." + ((EntityType<?>) type).getIdProperty().getName();
			}
			break;
		default:
		}

		return p.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#getColumnPrivileges(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern)
			throws SQLException {
		throw new SQLFeatureNotSupportedException("getColumnPrivileges not supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#getTablePrivileges(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern)
			throws SQLException {
		GenericResultSet grs = new GenericResultSet("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "GRANTOR", "GRANTEE",
				"PRIVILEGE", "IS_GRANTABLE");

		for (Map.Entry<String, EntityType<?>> entry : connection.getEntityTypeMap().entrySet()) {
			String shortName = entry.getKey();
			grs.addData(null, connection.getAccessId(), shortName, "cortex", "cortex", "SELECT", "NO");
		}
		return grs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#getBestRowIdentifier(java.lang.String,
	 * java.lang.String, java.lang.String, int, boolean)
	 */
	@Override
	public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable)
			throws SQLException {
		throw new SQLFeatureNotSupportedException(getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#getVersionColumns(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
		throw new SQLFeatureNotSupportedException(getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#getPrimaryKeys(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
		GenericResultSet grs = new GenericResultSet("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME", "KEY_SEQ",
				"PK_NAME");

		EntityType<?> entityType = connection.getEntityTypeMap().get(table);
		grs.addData(null, connection.getAccessId(), table, entityType.getIdProperty().getName(), null,
				entityType.getIdProperty().getName());

		return grs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#getImportedKeys(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {

		GenericResultSet grs = new GenericResultSet("PKTABLE_CAT", "PKTABLE_SCHEM", "PKTABLE_NAME", "PKCOLUMN_NAME",
				"FKTABLE_CAT", "FKTABLE_SCHEM", "FKTABLE_NAME", "FKCOLUMN_NAME", "KEY_SEQ", "UPDATE_RULE",
				"DELETE_RULE", "FK_NAME", "PK_NAME", "DEFERRABILITY");

		EntityType<?> entityType = connection.getEntityTypeMap().get(table);

		for (Property p : entityType.getProperties()) {
			GenericModelType primaryType = p.getType();
			if (primaryType.getTypeCode() == TypeCode.entityType) {

				EntityType<?> primaryEntityType = (EntityType<?>) primaryType;
				String keyName = p.getName() + "." + primaryEntityType.getIdProperty().getName();
				grs.addData(null, connection.getAccessId(), primaryEntityType.getShortName(),
						primaryEntityType.getIdProperty().getName(), null, connection.getAccessId(), table, keyName, 1,
						TfMetadata.importedKeyNoAction, TfMetadata.importedKeyNoAction, keyName, "id",
						TfMetadata.importedKeyNotDeferrable);
			}
		}

		return grs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#getExportedKeys(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
		GenericResultSet grs = new GenericResultSet("PKTABLE_CAT", "PKTABLE_SCHEM", "PKTABLE_NAME", "PKCOLUMN_NAME",
				"FKTABLE_CAT", "FKTABLE_SCHEM", "FKTABLE_NAME", "FKCOLUMN_NAME", "1", "UPDATE_RULE", "DELETE_RULE",
				"FK_NAME", "PK_NAME", "DEFERRABILITY");

		logger.warn("Client attempted to retrieve the exported keys" + getUnsupportedVersionSuffix());

		return grs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#getCrossReference(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable,
			String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException {
		GenericResultSet grs = new GenericResultSet("PKTABLE_CAT", "PKTABLE_SCHEM", "PKTABLE_NAME", "PKCOLUMN_NAME",
				"FKTABLE_CAT", "FKTABLE_SCHEM", "FKTABLE_NAME", "FKCOLUMN_NAME", "KEY_SEQ");
		// TODO Cross reference
		logger.warn("Client attempted to retrieve cross-references" + getUnsupportedVersionSuffix());

		return grs;
	}

	@Override
	public ResultSet getTypeInfo() throws SQLException {
		GenericResultSet grs = new GenericResultSet("TYPE_NAME", "DATA_TYPE", "PRECISION", "LITERAL_PREFIX",
				"LITERAL_SUFFIX", "CREATE_PARAMS", "NULLABLE", "CASE_SENSITIVE", "SEARCHABLE", "UNSIGNED_ATTRIBUTE",
				"FIXED_PREC_SCALE", "AUTO_INCREMENT", "LOCAL_TYPE_NAME", "MINIMUM_SCALE", "MAXIMUM_SCALE",
				"SQL_DATA_TYPE", "SQL_DATETIME_SUB", "NUM_PREC_RADIX");

		for (TypeCode tc : TypeCode.values()) {
			int sqlType = getType(tc);
			grs.addData(tc.name(), sqlType, 10, null, null, null, typeNullableUnknown, Boolean.TRUE, typeSearchable,
					Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, null, 10, 10, getType(TypeCode.dateType), 0, 10);
		}

		return grs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#getIndexInfo(java.lang.String,
	 * java.lang.String, java.lang.String, boolean, boolean)
	 */
	@Override
	public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate)
			throws SQLException {
		GenericResultSet grs = new GenericResultSet("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "NON_UNIQUE",
				"INDEX_QUALIFIER", "INDEX_NAME", "TYPE", "ORDINAL_POSITION", "COLUMN_NAME", "ASC_OR_DESC",
				"CARDINALITY", "PAGES", "FILTER_CONDITION");

		// TODO indices
		// grs.addData(null, connection.getAccessId(), table, unique, "INDEX_QUALIFIER",
		// "INDEX_NAME",
		// "TYPE", "ORDINAL_POSITION", "COLUMN_NAME", "ASC_OR_DESC", "CARDINALITY",
		// "PAGES", "FILTER_CONDITION");

		logger.warn("Client attempted to retrieve index info" + getUnsupportedVersionSuffix());
		
		return grs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsResultSetType(int)
	 */
	@Override
	public boolean supportsResultSetType(int type) throws SQLException {
		throw new SQLFeatureNotSupportedException(getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsResultSetConcurrency(int, int)
	 */
	@Override
	public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#ownUpdatesAreVisible(int)
	 */
	@Override
	public boolean ownUpdatesAreVisible(int type) throws SQLException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#ownDeletesAreVisible(int)
	 */
	@Override
	public boolean ownDeletesAreVisible(int type) throws SQLException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#ownInsertsAreVisible(int)
	 */
	@Override
	public boolean ownInsertsAreVisible(int type) throws SQLException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#othersUpdatesAreVisible(int)
	 */
	@Override
	public boolean othersUpdatesAreVisible(int type) throws SQLException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#othersDeletesAreVisible(int)
	 */
	@Override
	public boolean othersDeletesAreVisible(int type) throws SQLException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#othersInsertsAreVisible(int)
	 */
	@Override
	public boolean othersInsertsAreVisible(int type) throws SQLException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#updatesAreDetected(int)
	 */
	@Override
	public boolean updatesAreDetected(int type) throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#deletesAreDetected(int)
	 */
	@Override
	public boolean deletesAreDetected(int type) throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#insertsAreDetected(int)
	 */
	@Override
	public boolean insertsAreDetected(int type) throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsBatchUpdates()
	 */
	@Override
	public boolean supportsBatchUpdates() throws SQLException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#getUDTs(java.lang.String, java.lang.String,
	 * java.lang.String, int[])
	 */
	@Override
	public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types)
			throws SQLException {
		GenericResultSet grs = new GenericResultSet("TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME", "CLASS_NAME", "DATA_TYPE",
				"REMARKS", "BASE_TYPE");
		grs.setTypes(Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.VARCHAR,
				Types.INTEGER);
		// TODO UDTs
		
		logger.warn("Client attempted to retrieve UDTs" + getUnsupportedVersionSuffix());
		return grs;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsSavepoints()
	 */
	@Override
	public boolean supportsSavepoints() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsNamedParameters()
	 */
	@Override
	public boolean supportsNamedParameters() throws SQLException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsMultipleOpenResults()
	 */
	@Override
	public boolean supportsMultipleOpenResults() throws SQLException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsGetGeneratedKeys()
	 */
	@Override
	public boolean supportsGetGeneratedKeys() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#getSuperTypes(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException {
		throw new SQLFeatureNotSupportedException(getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#getSuperTables(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
		GenericResultSet grs = new GenericResultSet("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "SUPERTABLE_NAME");
		// throw new SQLFeatureNotSupportedException(getUnsupportedMessage());
		// TODO supertables
		logger.warn("Client attempted to retrieve supertables" + getUnsupportedVersionSuffix());
		return grs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#getAttributes(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern,
			String attributeNamePattern) throws SQLException {
		throw new SQLFeatureNotSupportedException(getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsResultSetHoldability(int)
	 */
	@Override
	public boolean supportsResultSetHoldability(int holdability) throws SQLException {
		return false;
	}

	@Override
	public int getResultSetHoldability() throws SQLException {
		return ResultSet.CLOSE_CURSORS_AT_COMMIT;
	}

	@Override
	public int getDatabaseMajorVersion() throws SQLException {
		return TRIBEFIRE_MAJOR_VERSION;
	}

	@Override
	public int getDatabaseMinorVersion() throws SQLException {
		return TRIBEFIRE_MINOR_VERSION;
	}

	@Override
	public int getJDBCMajorVersion() throws SQLException {
		return JDBC_MAJOR_VERSION;
	}

	@Override
	public int getJDBCMinorVersion() throws SQLException {
		return JDBC_MINOR_VERSION;
	}

	@Override
	public int getSQLStateType() throws SQLException {
		return sqlStateXOpen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#locatorsUpdateCopy()
	 */
	@Override
	public boolean locatorsUpdateCopy() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsStatementPooling()
	 */
	@Override
	public boolean supportsStatementPooling() throws SQLException {
		return false;
	}

	@Override
	public RowIdLifetime getRowIdLifetime() throws SQLException {
		return RowIdLifetime.ROWID_UNSUPPORTED;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#getSchemas(java.lang.String, java.lang.String)
	 */
	@Override
	public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
		return getSchemas();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#supportsStoredFunctionsUsingCallSyntax()
	 */
	@Override
	public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#autoCommitFailureClosesAllResultSets()
	 */
	@Override
	public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
		return false;
	}

	@Override
	public ResultSet getClientInfoProperties() throws SQLException {
		GenericResultSet grs = new GenericResultSet("NAME", "MAX_LEN", "DEFAULT_VALUE", "DESCRIPTION");
		grs.setTypes(Types.VARCHAR, Types.INTEGER, Types.VARCHAR, Types.VARCHAR);
		return grs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#getFunctions(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern)
			throws SQLException {
		throw new SQLFeatureNotSupportedException(getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#getFunctionColumns(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern,
			String columnNamePattern) throws SQLException {
		throw new SQLFeatureNotSupportedException(getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#getPseudoColumns(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern,
			String columnNamePattern) throws SQLException {
		throw new SQLFeatureNotSupportedException(getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.DatabaseMetaData#generatedKeyAlwaysReturned()
	 */
	@Override
	public boolean generatedKeyAlwaysReturned() throws SQLException {
		return false;
	}
	
	public static String getUnsupportedMessage() {
		return "This feature is not supported by the " + DRIVER_FULL_NAME + " version " + DRIVER_VERSION + ".";
	}

	public String getUnsupportedVersionSuffix() {
		return ", however that is not supported by the " + DRIVER_FULL_NAME + " version " + DRIVER_VERSION + ".";
	}

	
}
