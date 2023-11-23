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

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelTypes;
import com.braintribe.model.processing.session.GmSessionFactories;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;

/**
 * The Class TfConnection.
 *
 */
public class TfConnection implements Connection {

	private static Logger logger = Logger.getLogger(TfConnection.class);
	private static final Set<String> RESERVED_KEYWORDS = Stream
			.of("ALL", "ALTER", "AND", "ANY", "ARRAY", "ARROW", "AS", "ASC", "AT", "BEGIN", "BETWEEN", "BY", "CASE",
					"CHECK", "CLUSTERS", "CLUSTER", "COLAUTH", "COLUMNS", "COMPRESS", "CONNECT", "CRASH", "CREATE",
					"CURRENT", "DECIMAL", "DECLARE", "DEFAULT", "DELETE", "DESC", "DISTINCT", "DROP", "ELSE", "END",
					"EXCEPTION", "EXCLUSIVE", "EXISTS", "FETCH", "FORM", "FOR", "FROM", "GOTO", "GRANT", "GROUP",
					"HAVING", "IDENTIFIED", "IF", "IN", "INDEXES", "INDEX", "INSERT", "INTERSECT", "INTO", "IS", "LIKE",
					"LOCK", "MINUS", "MODE", "NOCOMPRESS", "NOT", "NOWAIT", "NULL", "OF", "ON", "OPTION", "OR",
					"ORDER,OVERLAPS", "PRIOR", "PROCEDURE", "PUBLIC", "RANGE", "RECORD", "RESOURCE", "REVOKE", "SELECT",
					"SHARE", "SIZE", "SQL", "START", "SUBTYPE", "TABAUTH", "TABLE", "THEN", "TO", "TYPE", "UNION",
					"UNIQUE", "UPDATE", "USE", "VALUES", "VIEW", "VIEWS", "WHEN", "WHERE", "WITH")
			.collect(Collectors.toSet());

	private String url;
	private String accessId;
	private String username;
	private String password;
	private Properties info;

	private boolean autoCommit = false;
	private String catalog;
	private int transactionIsolationLevel;
	private Map<String, Class<?>> typeMap;
	private int holdability;
	private String schema;
	private GmMetaModel metaModel = null;

	private boolean readOnly = false;

	private PersistenceGmSessionFactory sessionFactory = null;

	private PersistenceGmSession session = null;
	private PersistenceGmSession cortexSession = null;

	private Map<String, EntityType<?>> entityTypeMap = new HashMap<>();
	private Map<String, Set<TfAssociationTable>> associationTableEntityMap = new HashMap<>();
	private Map<String, TfAssociationTable> associationTableNameMap = new HashMap<>();

	private TfMetadata metaData;

	/**
	 * Instantiates a new tf connection.
	 *
	 * @param url
	 *            the url
	 * @param accessId
	 *            the access id
	 * @param username
	 *            the username
	 * @param password
	 *            the password
	 * @param info
	 *            the info
	 * @throws SQLException
	 *             the SQL exception
	 */
	public TfConnection(String url, String accessId, String username, String password, Properties info)
			throws SQLException {
		this.url = url;
		this.accessId = accessId;
		this.username = username;
		this.password = password;
		this.info = info;
		this.connectAndLoadInfo();
	}

	/**
	 * Connect and load info.
	 *
	 * @throws SQLException
	 *             the SQL exception
	 */
	protected void connectAndLoadInfo() throws SQLException {
		try {
			sessionFactory = GmSessionFactories.remote(url).authentication(username, password).done();
			session = sessionFactory.newSession(accessId);
			cortexSession = sessionFactory.newSession("cortex");

			metaModel = session.getModelAccessory().getModel();
			metaModel.deploy();
			getMetaData(); // intentional -> forces lazy init at this point!

			ModelOracle oracle = session.getModelAccessory().getOracle();
			ModelTypes types = oracle.getTypes();

			types.onlyEntities().<GmEntityType>asGmTypes().forEach(type -> {
				String typeSignature = type.getTypeSignature();
				registerType(typeSignature);
			});

			logger.debug("Session: " + session.getSessionAuthorization().getSessionId());
		} catch (Exception e) {
			throw new SQLException("Error during initial connection and data load from tribefire: " + e.getMessage(),
					e);
		}
	}

	/**
	 * Register type.
	 *
	 * @param typeSignature
	 *            the entity type signature
	 */
	private void registerType(String typeSignature) {

		EntityType<?> et = GMF.getTypeReflection().getEntityType(typeSignature);
		String entityTypeShortName = et.getShortName();

		boolean isReserved = RESERVED_KEYWORDS.stream().anyMatch(x -> x.equalsIgnoreCase(entityTypeShortName));
		if (entityTypeMap.containsKey(entityTypeShortName) || isReserved) {
			entityTypeMap.put(et.getTypeSignature().replaceAll("\\.", "_"), et);
		} else {
			entityTypeMap.put(entityTypeShortName, et);
		}
		generateVirtualAssociationTables(et);
	}

	/**
	 * Generate virtual association tables.
	 *
	 * @param et
	 *            the entity type for which to generate tables
	 */
	private void generateVirtualAssociationTables(EntityType<?> et) {
		for (Property property : et.getProperties()) {
			switch (property.getType().getTypeCode()) {
			case listType:
			case setType:
			case mapType:
				TfAssociationTable assTab = new TfAssociationTable(et, property, cortexSession);

				if (!associationTableEntityMap.containsKey(et.getShortName())) {
					associationTableEntityMap.put(et.getShortName(), new HashSet<>());
				}

				associationTableEntityMap.get(et.getShortName()).add(assTab);
				associationTableNameMap.put(assTab.getName(), assTab);
				break;
			default:
				break;
			}
		}

	}

	/**
	 * Gets the association table by name.
	 *
	 * @param tableNamePattern
	 *            the table name pattern
	 * @return the association table by name
	 */
	public TfAssociationTable getAssociationTableByName(String tableNamePattern) {
		if (associationTableNameMap.containsKey(tableNamePattern)) {
			return associationTableNameMap.get(tableNamePattern);
		} else {
			logger.trace("Unable to retrieve association table for name " + tableNamePattern);
			return null;
		}
	}

	/**
	 * Gets the association table by table property.
	 *
	 * @param tableName
	 *            the table name
	 * @param propertyName
	 *            the property name
	 * @return the association table by table property
	 */
	public TfAssociationTable getAssociationTableByTableProperty(String tableName, String propertyName) {
		for (TfAssociationTable assTab : associationTableEntityMap.get(tableName)) {
			if ((assTab.getLeftProperty().getName().equals(propertyName)
					&& assTab.getLeftEntity().getShortName().equals(tableName))
					|| (assTab.getRightProperty().getName().equals(propertyName)
							&& assTab.getLeftEntity().getShortName().equals(tableName))) {
				return assTab;
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@SuppressWarnings("unchecked") // TODO type safety possible?
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (!isWrapperFor(iface))
			throw new SQLException("Connection is not a wrapper for " + iface.getName());

		return (T) this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return iface != null && iface == Connection.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createStatement()
	 */
	@Override
	public Statement createStatement() throws SQLException {
		return new TfStatement(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String)
	 */
	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return new TfPreparedStatement(this, sql);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareCall(java.lang.String)
	 */
	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#nativeSQL(java.lang.String)
	 */
	@Override
	public String nativeSQL(String sql) throws SQLException {
		return sql;
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		this.autoCommit = autoCommit;
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return this.autoCommit;
	}

	/**
	 * Check session validity.
	 *
	 * @throws SQLException
	 *             the SQL exception
	 */
	protected void checkSessionValidity() throws SQLException {
		if (session == null) {
			throw new SQLException("The connection has not been established.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#commit()
	 */
	@Override
	public void commit() throws SQLException {
		checkSessionValidity();
		try {
			session.commit();
		} catch (Exception e) {
			throw new SQLException("Commit error.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#rollback()
	 */
	@Override
	public void rollback() throws SQLException {
		checkSessionValidity();
		try {
			session = sessionFactory.newSession(accessId);
		} catch (Exception e) {
			throw new SQLException("Rollback error.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#close()
	 */
	@Override
	public void close() throws SQLException {
		try {
			if (session != null) {
				if (session instanceof BasicPersistenceGmSession) {
					((BasicPersistenceGmSession) session).cleanup();
				}
			}
		} catch (Exception e) {
			throw new SQLException("Error while closing connection.", e);
		} finally {
			session = null;
		}
	}

	@Override
	public boolean isClosed() throws SQLException {
		return (session == null);
	}

	@Override
	public DatabaseMetaData getMetaData() {
		if (metaData == null) {
			metaData = new TfMetadata(this);
		}
		return metaData;
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		this.readOnly = readOnly;
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return readOnly;
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		this.catalog = catalog;
	}

	@Override
	public String getCatalog() throws SQLException {
		return this.catalog;
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		this.transactionIsolationLevel = level;
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return this.transactionIsolationLevel;
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#clearWarnings()
	 */
	@Override
	public void clearWarnings() throws SQLException {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createStatement(int, int)
	 */
	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		return new TfStatement(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		return new TfPreparedStatement(this, sql);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
	 */
	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		throw new SQLFeatureNotSupportedException("CallableStatement not supported.");
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return this.typeMap;
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		this.typeMap = map;
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		this.holdability = holdability;
	}

	@Override
	public int getHoldability() throws SQLException {
		return this.holdability;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setSavepoint()
	 */
	@Override
	public Savepoint setSavepoint() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setSavepoint(java.lang.String)
	 */
	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
	 */
	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createStatement(int, int, int)
	 */
	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return new TfStatement(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int, int)
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		return new TfPreparedStatement(this, sql);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
	 */
	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int)
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		return new TfPreparedStatement(this, sql);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String,
	 * java.lang.String[])
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createClob()
	 */
	@Override
	public Clob createClob() throws SQLException {
		return new TfClob();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createBlob()
	 */
	@Override
	public Blob createBlob() throws SQLException {
		return new TfBlob();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createNClob()
	 */
	@Override
	public NClob createNClob() throws SQLException {
		return new TfNClob();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createSQLXML()
	 */
	@Override
	public SQLXML createSQLXML() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#isValid(int)
	 */
	@Override
	public boolean isValid(int timeout) throws SQLException {
		try {
			checkSessionValidity();
			// TODO: check isSessionValid
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setClientInfo(java.lang.String, java.lang.String)
	 */
	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		if (info == null) {
			info = new Properties();
		}
		info.setProperty(name, value);
	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		this.info = properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#getClientInfo(java.lang.String)
	 */
	@Override
	public String getClientInfo(String name) throws SQLException {
		if (info != null) {
			return info.getProperty(name);
		}
		return null;
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return this.info;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createArrayOf(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createStruct(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	@Override
	public void setSchema(String schema) throws SQLException {
		this.schema = schema;
	}

	@Override
	public String getSchema() throws SQLException {
		return this.schema;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#abort(java.util.concurrent.Executor)
	 */
	@Override
	public void abort(Executor executor) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setNetworkTimeout(java.util.concurrent.Executor,
	 * int)
	 */
	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	public Map<String, EntityType<?>> getEntityTypeMap() {
		return entityTypeMap;
	}

	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		TfConnection.logger = logger;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAccessId() {
		return accessId;
	}

	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Properties getInfo() {
		return info;
	}

	public void setInfo(Properties info) {
		this.info = info;
	}

	public int getTransactionIsolationLevel() {
		return transactionIsolationLevel;
	}

	public void setTransactionIsolationLevel(int transactionIsolationLevel) {
		this.transactionIsolationLevel = transactionIsolationLevel;
	}

	public GmMetaModel getMetaModel() {
		return metaModel;
	}

	public void setMetaModel(GmMetaModel metaModel) {
		this.metaModel = metaModel;
	}

	public PersistenceGmSessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public PersistenceGmSession getSession() {
		return session;
	}

	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}

	public PersistenceGmSession getCortexSession() {
		return cortexSession;
	}

	public void setCortexSession(PersistenceGmSession cortexSession) {
		this.cortexSession = cortexSession;
	}

	public void setEntityTypeMap(Map<String, EntityType<?>> entityTypeMap) {
		this.entityTypeMap = entityTypeMap;
	}

	public void setMetaData(TfMetadata metaData) {
		this.metaData = metaData;
	}

	public Map<String, Set<TfAssociationTable>> getAssociationTableMap() {
		return associationTableEntityMap;
	}

	public void setAssociationTableMap(Map<String, Set<TfAssociationTable>> associationTableMap) {
		this.associationTableEntityMap = associationTableMap;
	}

}
