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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.record.ListRecord;
import com.braintribe.tribefire.jdbc.resultset.GenericResultSetMetaData;
import com.braintribe.tribefire.jdbc.statement.PropertyWrapper;
import com.braintribe.tribefire.jdbc.statement.SqlQueryParser;

/**
 * The Class TfResultSet.
 *
 */
public class TfResultSet implements ResultSet {

	private static final String JDBC_ERROR = "@JDBC_Error@";
	private SelectQuery query;
	private SqlQueryParser queryParser;
	private Paging paging;
	private int maxPageSize = 100;
	private TfConnection connection;
	private String sql;

	private int currentWindowStart = 0;
	private int currentIndexInWindow = -1;
	private List<Object> currentWindowList;
	private Object currentEntityInWindow;
	private Logger logger = Logger.getLogger(TfResultSet.class);

	private SelectQueryResult result;

	private boolean wasNull = false;

	/**
	 * Instantiates a new tf result set.
	 *
	 * @param connection
	 *            the connection
	 * @param queryParser
	 *            the query parser
	 * @param sql
	 *            the sql
	 * @throws SQLException
	 *             the SQL exception
	 */
	public TfResultSet(TfConnection connection, SqlQueryParser queryParser, String sql) throws SQLException {
		this.connection = connection;
		this.queryParser = queryParser;
		this.sql = sql;
		query = queryParser.getQuery();

		Restriction restriction = query.getRestriction();
		if (restriction == null) {
			restriction = Restriction.T.create();
			query.setRestriction(restriction);
		}
		paging = restriction.getPaging();
		if (paging == null) {
			paging = Paging.T.create();
			restriction.setPaging(paging);
		}

		currentWindowStart = queryParser.getFromIndex();

		getBatch();

	}

	/**
	 * Gets the batch.
	 *
	 * @return the batch
	 * @throws SQLException
	 *             the SQL exception
	 */
	protected boolean getBatch() throws SQLException {

		int windowStart = currentWindowStart;

		int windowEnd = windowStart + maxPageSize;
		if (windowEnd >= queryParser.getToIndex()) {
			windowEnd = queryParser.getToIndex();
		}
		if (windowStart >= windowEnd) {
			currentWindowList = null;
			return false;
		}

		paging.setStartIndex(windowStart);
		paging.setPageSize(windowEnd - windowStart);

		try {
			result = connection.getSession().query().select(query).result();
			currentWindowList = result.getResults();
			if (currentWindowList.size() == 0) {
				return false;
			}
			return true;
		} catch (Exception e) {
			throw new SQLException("Error while trying to execute query: " + sql, e);
		}
	}

	/**
	 * Gets the next window.
	 *
	 * @return the next window
	 * @throws SQLException
	 *             the SQL exception
	 */
	protected boolean getNextWindow() throws SQLException {
		currentWindowStart += maxPageSize;
		return getBatch();
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
	 * @see java.sql.ResultSet#next()
	 */
	@Override
	public boolean next() throws SQLException {
		if (currentWindowList == null) {
			return false;
		}
		if (currentIndexInWindow < (currentWindowList.size() - 1)) {
			currentIndexInWindow++;
		} else {
			if (!getNextWindow()) {
				return false;
			}
			currentIndexInWindow = 0;
		}
		currentEntityInWindow = currentWindowList.get(currentIndexInWindow);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#close()
	 */
	@Override
	public void close() throws SQLException {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#wasNull()
	 */
	@Override
	public boolean wasNull() throws SQLException {
		return wasNull;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getString(int)
	 */
	@Override
	public String getString(int columnIndex) throws SQLException {

		PropertyWrapper pw = queryParser.getSelectedProperties().get(columnIndex - 1);
		if (pw.getFixedValue() != null) {
			return pw.getFixedValue().toString();
		}

		Object property = null;
		if (currentEntityInWindow instanceof ListRecord) {
			return getStringCompoundResult(columnIndex, pw);
		}

		if (currentEntityInWindow instanceof GenericEntity) {
			property = pw.getProperty().get((GenericEntity) currentEntityInWindow);

			if (property == null) {
				return null;
			} else if (property instanceof StandardIdentifiable) {
				return ((StandardIdentifiable) property).getId().toString();
			}
		} else {
			return currentEntityInWindow.toString();
		}

		logger.debug("Returning fallback String for " + currentEntityInWindow);
		return property.toString();
	}

	/**
	 * Gets the string compound result.
	 *
	 * @param columnIndex
	 *            the column index
	 * @param pw
	 *            the pw
	 * @return the string compound result
	 */
	private String getStringCompoundResult(int columnIndex, PropertyWrapper pw) {
		ListRecord record = (ListRecord) currentEntityInWindow;
		columnIndex--; // it's an array...

		// loop over all the compound data type members
		for (Object value : record.getValues()) {
			try {

				Object property = null;

				if (value instanceof GenericEntity) {
					GenericEntity currentEntity = (GenericEntity) value;
					EntityType<GenericEntity> entityType = currentEntity.entityType();

					// if an entity type is specified and does not correspond to the current
					// compound subtype, skip the evaluation
					if (pw.getEntityType() != null && entityType != null
							&& !pw.getEntityType().equals(entityType.getTypeSignature())) {
						continue;
					}

					property = pw.getProperty().get(currentEntity);

					if (property == null) {
						return null;
					} else if (property instanceof StandardIdentifiable) {
						return ((StandardIdentifiable) property).getId().toString();
					}

					return property.toString();
				} else if (value instanceof String) {
					return (String) value;
				} else {
					// unknown type, we assume the order is correct

					value = record.getValues().get(columnIndex);
					return value.toString();
				}
			} catch (Exception ex) {
				logger.trace("Unable to lookup property on the compond subtype: ", ex);
				// continue with other subtypes
			}
		}

		logger.error("Compound type was unable to retrieve the property: " + pw.getProperty().getName());
		return JDBC_ERROR;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getBoolean(int)
	 */
	@Override
	public boolean getBoolean(int columnIndex) throws SQLException {
		Object property = getObject(columnIndex);
		if (property == null) {
			return false;
		}
		Boolean b = (Boolean) property;
		return b.booleanValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getByte(int)
	 */
	@Override
	public byte getByte(int columnIndex) throws SQLException {
		Object property = getObject(columnIndex);
		if (property == null) {
			return 0;
		}
		Byte b = (Byte) property;
		return b;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getShort(int)
	 */
	@Override
	public short getShort(int columnIndex) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getInt(int)
	 */
	@Override
	public int getInt(int columnIndex) throws SQLException {
		Object property = getObject(columnIndex);
		if (property == null) {
			return 0;
		}

		Integer i = (Integer) property;
		return i.intValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getLong(int)
	 */
	@Override
	public long getLong(int columnIndex) throws SQLException {
		Object property = getObject(columnIndex);
		if (property == null) {
			return 0L;
		}

		if (property instanceof StandardIdentifiable) {
			return ((StandardIdentifiable) property).getId();
		}

		if (property instanceof String) {
			return Long.parseLong((String) property);
		}

		Long l = (Long) property;
		return l.longValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getFloat(int)
	 */
	@Override
	public float getFloat(int columnIndex) throws SQLException {
		Object property = getObject(columnIndex);
		if (property == null) {
			return 0;
		}
		Float f = (Float) property;
		return f.floatValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getDouble(int)
	 */
	@Override
	public double getDouble(int columnIndex) throws SQLException {
		Object property = getObject(columnIndex);
		if (property == null) {
			return 0;
		}
		Double doubleValue = (Double) property;
		return doubleValue.doubleValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getBigDecimal(int, int)
	 */
	@Deprecated // needed for java.sql.ResultSet
	@Override
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
		return getBigDecimal(columnIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getBytes(int)
	 */
	@Override
	public byte[] getBytes(int columnIndex) throws SQLException {
		Object property = getObject(columnIndex);
		if (property == null) {
			return new byte[0];
		}
		return (byte[]) property;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getDate(int)
	 */
	@Override
	public Date getDate(int columnIndex) throws SQLException {
		Object property = getObject(columnIndex);
		if (property == null) {
			return null;
		}
		java.util.Date utilDate = (java.util.Date) property;
		Date sqlDate = new Date(utilDate.getTime());
		return sqlDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getTime(int)
	 */
	@Override
	public Time getTime(int columnIndex) throws SQLException {
		Object property = getObject(columnIndex);
		if (property == null) {
			return null;
		}
		java.util.Date utilDate = (java.util.Date) property;
		Time time = new Time(utilDate.getTime());
		return time;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getTimestamp(int)
	 */
	@Override
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		Object property = getObject(columnIndex);
		if (property == null) {
			return null;
		}
		java.util.Date utilDate = (java.util.Date) property;
		Timestamp time = new Timestamp(utilDate.getTime());
		return time;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getAsciiStream(int)
	 */
	@Override
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getUnicodeStream(int)
	 */
	@Deprecated // needed for java.sql.ResultSet
	@Override
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getBinaryStream(int)
	 */
	@Override
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getString(java.lang.String)
	 */
	@Override
	public String getString(String columnLabel) throws SQLException {

		PropertyWrapper pw = queryParser.getSelectedPropertiesMap().get(columnLabel);

		if (currentEntityInWindow instanceof GenericEntity) {
			Object property = pw.getProperty().get((GenericEntity) currentEntityInWindow);
			if (property == null) {
				return null;
			}

			if (property instanceof StandardIdentifiable) {
				return ((StandardIdentifiable) property).getId().toString();
			}
		}
		logger.debug("Returning fallback String for " + currentEntityInWindow);

		return pw.getProperty().toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getBoolean(java.lang.String)
	 */
	@Override
	public boolean getBoolean(String columnLabel) throws SQLException {
		Object property = getObject(columnLabel);
		if (property == null) {
			return false;
		}
		Boolean b = (Boolean) property;
		return b.booleanValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getByte(java.lang.String)
	 */
	@Override
	public byte getByte(String columnLabel) throws SQLException {
		Object property = getObject(columnLabel);
		if (property == null) {
			return 0;
		}
		Byte b = (Byte) property;
		return b;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getShort(java.lang.String)
	 */
	@Override
	public short getShort(String columnLabel) throws SQLException {
		Object property = getObject(columnLabel);
		if (property == null) {
			return 0;
		}
		Short s = (Short) property;
		return s.shortValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getInt(java.lang.String)
	 */
	@Override
	public int getInt(String columnLabel) throws SQLException {
		Object property = getObject(columnLabel);
		if (property == null) {
			return 0;
		}
		Integer i = (Integer) property;
		return i.intValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getLong(java.lang.String)
	 */
	@Override
	public long getLong(String columnLabel) throws SQLException {
		Object property = getObject(columnLabel);

		if (property == null) {
			return 0L;
		}

		if (property instanceof StandardIdentifiable) {
			return ((StandardIdentifiable) property).getId();
		}

		Long l = (Long) property;
		return l.longValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getFloat(java.lang.String)
	 */
	@Override
	public float getFloat(String columnLabel) throws SQLException {
		Object property = getObject(columnLabel);
		if (property == null) {
			return 0L;
		}
		Float f = (Float) property;
		return f.floatValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getDouble(java.lang.String)
	 */
	@Override
	public double getDouble(String columnLabel) throws SQLException {
		Object property = getObject(columnLabel);
		if (property == null) {
			return 0L;
		}
		Double d = (Double) property;
		return d.doubleValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getBigDecimal(java.lang.String, int)
	 */
	@Deprecated // needed for java.sql.ResultSet
	@Override
	public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
		return getBigDecimal(columnLabel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getBytes(java.lang.String)
	 */
	@Override
	public byte[] getBytes(String columnLabel) throws SQLException {
		Object property = getObject(columnLabel);
		if (property == null) {
			return new byte[0];
		}
		return (byte[]) property;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getDate(java.lang.String)
	 */
	@Override
	public Date getDate(String columnLabel) throws SQLException {
		Object property = getObject(columnLabel);
		if (property == null) {
			return null;
		}
		java.util.Date utilDate = (java.util.Date) property;
		Date sqlDate = new Date(utilDate.getTime());
		return sqlDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getTime(java.lang.String)
	 */
	@Override
	public Time getTime(String columnLabel) throws SQLException {
		Object property = getObject(columnLabel);
		if (property == null) {
			return null;
		}
		java.util.Date utilDate = (java.util.Date) property;
		Time sqlTime = new Time(utilDate.getTime());
		return sqlTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String)
	 */
	@Override
	public Timestamp getTimestamp(String columnLabel) throws SQLException {
		Object property = getObject(columnLabel);
		if (property == null) {
			return null;
		}
		java.util.Date utilDate = (java.util.Date) property;
		Timestamp sqlTime = new Timestamp(utilDate.getTime());
		return sqlTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getAsciiStream(java.lang.String)
	 */
	@Override
	public InputStream getAsciiStream(String columnLabel) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getUnicodeStream(java.lang.String)
	 */
	@Deprecated // needed for java.sql.ResultSet
	@Override
	public InputStream getUnicodeStream(String columnLabel) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getBinaryStream(java.lang.String)
	 */
	@Override
	public InputStream getBinaryStream(String columnLabel) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#clearWarnings()
	 */
	@Override
	public void clearWarnings() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	@Override
	public String getCursorName() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		List<String> propNames = new ArrayList<String>();
		List<Integer> types = new ArrayList<Integer>();
		for (PropertyWrapper pw : queryParser.getSelectedProperties()) {
			propNames.add(pw.getAlias());
			if (pw.getProperty() != null) {
				TypeCode typeCode = pw.getProperty().getType().getTypeCode();
				types.add(TfMetadata.getType(typeCode));
			} else {
				types.add(Types.VARCHAR);
			}
		}
		return new GenericResultSetMetaData(propNames, types);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getObject(int)
	 */
	@Override
	public Object getObject(int columnIndex) throws SQLException {
		PropertyWrapper pw = queryParser.getSelectedProperties().get(columnIndex - 1);
		if (pw.getFixedValue() != null) {
			wasNull = false;
			return pw.getFixedValue();
		}

		Object property = null;
		if (currentEntityInWindow instanceof ListRecord) {
			property = getObjectCompoundResult(columnIndex, pw);
		} else if (currentEntityInWindow instanceof GenericEntity) {
			property = pw.getProperty().get((GenericEntity) currentEntityInWindow);
		} else {
			property = pw.getProperty();
		}
		wasNull = (property == null);
		return property;
	}

	/**
	 * Gets the object compound result.
	 *
	 * @param columnIndex
	 *            the column index
	 * @param pw
	 *            the pw
	 * @return the object compound result
	 */
	private Object getObjectCompoundResult(int columnIndex, PropertyWrapper pw) {
		ListRecord record = (ListRecord) currentEntityInWindow;
		columnIndex--; // it's an array...

		if (record.getValues() != null && record.getValues().size() >= columnIndex
				&& record.getValues().get(columnIndex) instanceof String) {
			return record.getValues().get(columnIndex);
		}

		// loop over the compound subtypes
		for (Object value : record.getValues()) {
			try {

				Object property = null;

				if (value instanceof GenericEntity) {
					GenericEntity currentEntity = (GenericEntity) value;
					EntityType<GenericEntity> entityType = currentEntity.entityType();

					// if an entity type is specified and does not correspond to the current
					// compound subtype, skip the evaluation
					if (pw.getEntityType() != null && entityType != null
							&& !pw.getEntityType().equals(entityType.getTypeSignature())) {
						continue;
					}

					property = pw.getProperty().get(currentEntity);

					return property;
				} else {
					// fallback in case the property itself is the object
					return record.getValues().get(columnIndex).toString();
				}

			} catch (Exception ex) {
				logger.trace("Unable to lookup property on the compond subtype: ", ex);
				// continue with other subtypes
			}
		}

		return JDBC_ERROR;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getObject(java.lang.String)
	 */
	@Override
	public Object getObject(String columnLabel) throws SQLException {
		PropertyWrapper pw = queryParser.getSelectedPropertiesMap().get(columnLabel);
		if (pw.getFixedValue() != null) {
			wasNull = false;
			return pw.getFixedValue();
		}

		Object property = null;
		if (currentEntityInWindow instanceof ListRecord) {
			property = getObjectCompoundResult(columnLabel, pw);
		} else if (currentEntityInWindow instanceof GenericEntity) {
			property = pw.getProperty().get((GenericEntity) currentEntityInWindow);
		} else {
			property = pw.getProperty();
		}
		wasNull = (property == null);
		return property;
	}

	/**
	 * Gets the object compound result.
	 *
	 * Retrieval is not supported by column label.
	 */
	@SuppressWarnings("unused")
	private Object getObjectCompoundResult(String columnLabel, PropertyWrapper pw)
			throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#findColumn(java.lang.String)
	 */
	@Override
	public int findColumn(String columnLabel) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getCharacterStream(int)
	 */
	@Override
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getCharacterStream(java.lang.String)
	 */
	@Override
	public Reader getCharacterStream(String columnLabel) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getBigDecimal(int)
	 */
	@Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {

		Object property = getObject(columnIndex);

		if (property == null) {
			return new BigDecimal(0);
		}

		BigDecimal bd = (BigDecimal) property;
		return bd;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getBigDecimal(java.lang.String)
	 */
	@Override
	public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
		Object property = getObject(columnLabel);
		if (property == null) {
			return new BigDecimal(0);
		}
		BigDecimal bd = (BigDecimal) property;
		return bd;
	}

	@Override
	public boolean isBeforeFirst() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	@Override
	public boolean isAfterLast() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	@Override
	public boolean isFirst() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	@Override
	public boolean isLast() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#beforeFirst()
	 */
	@Override
	public void beforeFirst() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#afterLast()
	 */
	@Override
	public void afterLast() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#first()
	 */
	@Override
	public boolean first() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#last()
	 */
	@Override
	public boolean last() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	@Override
	public int getRow() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#absolute(int)
	 */
	@Override
	public boolean absolute(int row) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#relative(int)
	 */
	@Override
	public boolean relative(int rows) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#previous()
	 */
	@Override
	public boolean previous() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	@Override
	public int getFetchDirection() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	@Override
	public int getFetchSize() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	@Override
	public int getType() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	@Override
	public int getConcurrency() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#rowUpdated()
	 */
	@Override
	public boolean rowUpdated() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#rowInserted()
	 */
	@Override
	public boolean rowInserted() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#rowDeleted()
	 */
	@Override
	public boolean rowDeleted() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateNull(int)
	 */
	@Override
	public void updateNull(int columnIndex) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateBoolean(int, boolean)
	 */
	@Override
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateByte(int, byte)
	 */
	@Override
	public void updateByte(int columnIndex, byte x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateShort(int, short)
	 */
	@Override
	public void updateShort(int columnIndex, short x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateInt(int, int)
	 */
	@Override
	public void updateInt(int columnIndex, int x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateLong(int, long)
	 */
	@Override
	public void updateLong(int columnIndex, long x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateFloat(int, float)
	 */
	@Override
	public void updateFloat(int columnIndex, float x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateDouble(int, double)
	 */
	@Override
	public void updateDouble(int columnIndex, double x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateBigDecimal(int, java.math.BigDecimal)
	 */
	@Override
	public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateString(int, java.lang.String)
	 */
	@Override
	public void updateString(int columnIndex, String x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateBytes(int, byte[])
	 */
	@Override
	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateDate(int, java.sql.Date)
	 */
	@Override
	public void updateDate(int columnIndex, Date x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateTime(int, java.sql.Time)
	 */
	@Override
	public void updateTime(int columnIndex, Time x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateTimestamp(int, java.sql.Timestamp)
	 */
	@Override
	public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream, int)
	 */
	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream, int)
	 */
	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader, int)
	 */
	@Override
	public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateObject(int, java.lang.Object, int)
	 */
	@Override
	public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateObject(int, java.lang.Object)
	 */
	@Override
	public void updateObject(int columnIndex, Object x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateNull(java.lang.String)
	 */
	@Override
	public void updateNull(String columnLabel) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateBoolean(java.lang.String, boolean)
	 */
	@Override
	public void updateBoolean(String columnLabel, boolean x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateByte(java.lang.String, byte)
	 */
	@Override
	public void updateByte(String columnLabel, byte x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateShort(java.lang.String, short)
	 */
	@Override
	public void updateShort(String columnLabel, short x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateInt(java.lang.String, int)
	 */
	@Override
	public void updateInt(String columnLabel, int x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateLong(java.lang.String, long)
	 */
	@Override
	public void updateLong(String columnLabel, long x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateFloat(java.lang.String, float)
	 */
	@Override
	public void updateFloat(String columnLabel, float x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateDouble(java.lang.String, double)
	 */
	@Override
	public void updateDouble(String columnLabel, double x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateBigDecimal(java.lang.String,
	 * java.math.BigDecimal)
	 */
	@Override
	public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateString(java.lang.String, java.lang.String)
	 */
	@Override
	public void updateString(String columnLabel, String x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateBytes(java.lang.String, byte[])
	 */
	@Override
	public void updateBytes(String columnLabel, byte[] x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateDate(java.lang.String, java.sql.Date)
	 */
	@Override
	public void updateDate(String columnLabel, Date x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateTime(java.lang.String, java.sql.Time)
	 */
	@Override
	public void updateTime(String columnLabel, Time x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateTimestamp(java.lang.String, java.sql.Timestamp)
	 */
	@Override
	public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateAsciiStream(java.lang.String,
	 * java.io.InputStream, int)
	 */
	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateBinaryStream(java.lang.String,
	 * java.io.InputStream, int)
	 */
	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateCharacterStream(java.lang.String,
	 * java.io.Reader, int)
	 */
	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateObject(java.lang.String, java.lang.Object, int)
	 */
	@Override
	public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateObject(java.lang.String, java.lang.Object)
	 */
	@Override
	public void updateObject(String columnLabel, Object x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#insertRow()
	 */
	@Override
	public void insertRow() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateRow()
	 */
	@Override
	public void updateRow() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#deleteRow()
	 */
	@Override
	public void deleteRow() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#refreshRow()
	 */
	@Override
	public void refreshRow() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#cancelRowUpdates()
	 */
	@Override
	public void cancelRowUpdates() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#moveToInsertRow()
	 */
	@Override
	public void moveToInsertRow() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#moveToCurrentRow()
	 */
	@Override
	public void moveToCurrentRow() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	@Override
	public Statement getStatement() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getObject(int, java.util.Map)
	 */
	@Override
	public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getRef(int)
	 */
	@Override
	public Ref getRef(int columnIndex) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getBlob(int)
	 */
	@Override
	public Blob getBlob(int columnIndex) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getClob(int)
	 */
	@Override
	public Clob getClob(int columnIndex) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getArray(int)
	 */
	@Override
	public Array getArray(int columnIndex) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getObject(java.lang.String, java.util.Map)
	 */
	@Override
	public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getRef(java.lang.String)
	 */
	@Override
	public Ref getRef(String columnLabel) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getBlob(java.lang.String)
	 */
	@Override
	public Blob getBlob(String columnLabel) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getClob(java.lang.String)
	 */
	@Override
	public Clob getClob(String columnLabel) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getArray(java.lang.String)
	 */
	@Override
	public Array getArray(String columnLabel) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getDate(int, java.util.Calendar)
	 */
	@Override
	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		return getDate(columnIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getDate(java.lang.String, java.util.Calendar)
	 */
	@Override
	public Date getDate(String columnLabel, Calendar cal) throws SQLException {
		return getDate(columnLabel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getTime(int, java.util.Calendar)
	 */
	@Override
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getTime(java.lang.String, java.util.Calendar)
	 */
	@Override
	public Time getTime(String columnLabel, Calendar cal) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getTimestamp(int, java.util.Calendar)
	 */
	@Override
	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String, java.util.Calendar)
	 */
	@Override
	public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getURL(int)
	 */
	@Override
	public URL getURL(int columnIndex) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getURL(java.lang.String)
	 */
	@Override
	public URL getURL(String columnLabel) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateRef(int, java.sql.Ref)
	 */
	@Override
	public void updateRef(int columnIndex, Ref x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateRef(java.lang.String, java.sql.Ref)
	 */
	@Override
	public void updateRef(String columnLabel, Ref x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateBlob(int, java.sql.Blob)
	 */
	@Override
	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateBlob(java.lang.String, java.sql.Blob)
	 */
	@Override
	public void updateBlob(String columnLabel, Blob x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateClob(int, java.sql.Clob)
	 */
	@Override
	public void updateClob(int columnIndex, Clob x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateClob(java.lang.String, java.sql.Clob)
	 */
	@Override
	public void updateClob(String columnLabel, Clob x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateArray(int, java.sql.Array)
	 */
	@Override
	public void updateArray(int columnIndex, Array x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateArray(java.lang.String, java.sql.Array)
	 */
	@Override
	public void updateArray(String columnLabel, Array x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getRowId(int)
	 */
	@Override
	public RowId getRowId(int columnIndex) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getRowId(java.lang.String)
	 */
	@Override
	public RowId getRowId(String columnLabel) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateRowId(int, java.sql.RowId)
	 */
	@Override
	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateRowId(java.lang.String, java.sql.RowId)
	 */
	@Override
	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	@Override
	public int getHoldability() throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	@Override
	public boolean isClosed() throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateNString(int, java.lang.String)
	 */
	@Override
	public void updateNString(int columnIndex, String nString) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateNString(java.lang.String, java.lang.String)
	 */
	@Override
	public void updateNString(String columnLabel, String nString) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateNClob(int, java.sql.NClob)
	 */
	@Override
	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateNClob(java.lang.String, java.sql.NClob)
	 */
	@Override
	public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getNClob(int)
	 */
	@Override
	public NClob getNClob(int columnIndex) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getNClob(java.lang.String)
	 */
	@Override
	public NClob getNClob(String columnLabel) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getSQLXML(int)
	 */
	@Override
	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getSQLXML(java.lang.String)
	 */
	@Override
	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateSQLXML(int, java.sql.SQLXML)
	 */
	@Override
	public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateSQLXML(java.lang.String, java.sql.SQLXML)
	 */
	@Override
	public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getNString(int)
	 */
	@Override
	public String getNString(int columnIndex) throws SQLException {
		return getString(columnIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getNString(java.lang.String)
	 */
	@Override
	public String getNString(String columnLabel) throws SQLException {
		return getNString(columnLabel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getNCharacterStream(int)
	 */
	@Override
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getNCharacterStream(java.lang.String)
	 */
	@Override
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateNCharacterStream(int, java.io.Reader, long)
	 */
	@Override
	public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateNCharacterStream(java.lang.String,
	 * java.io.Reader, long)
	 */
	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream, long)
	 */
	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream, long)
	 */
	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader, long)
	 */
	@Override
	public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateAsciiStream(java.lang.String,
	 * java.io.InputStream, long)
	 */
	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateBinaryStream(java.lang.String,
	 * java.io.InputStream, long)
	 */
	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateCharacterStream(java.lang.String,
	 * java.io.Reader, long)
	 */
	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateBlob(int, java.io.InputStream, long)
	 */
	@Override
	public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateBlob(java.lang.String, java.io.InputStream,
	 * long)
	 */
	@Override
	public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateClob(int, java.io.Reader, long)
	 */
	@Override
	public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateClob(java.lang.String, java.io.Reader, long)
	 */
	@Override
	public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateNClob(int, java.io.Reader, long)
	 */
	@Override
	public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateNClob(java.lang.String, java.io.Reader, long)
	 */
	@Override
	public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateNCharacterStream(int, java.io.Reader)
	 */
	@Override
	public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateNCharacterStream(java.lang.String,
	 * java.io.Reader)
	 */
	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream)
	 */
	@Override
	public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream)
	 */
	@Override
	public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader)
	 */
	@Override
	public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateAsciiStream(java.lang.String,
	 * java.io.InputStream)
	 */
	@Override
	public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateBinaryStream(java.lang.String,
	 * java.io.InputStream)
	 */
	@Override
	public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateCharacterStream(java.lang.String,
	 * java.io.Reader)
	 */
	@Override
	public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateBlob(int, java.io.InputStream)
	 */
	@Override
	public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateBlob(java.lang.String, java.io.InputStream)
	 */
	@Override
	public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateClob(int, java.io.Reader)
	 */
	@Override
	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateClob(java.lang.String, java.io.Reader)
	 */
	@Override
	public void updateClob(String columnLabel, Reader reader) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateNClob(int, java.io.Reader)
	 */
	@Override
	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#updateNClob(java.lang.String, java.io.Reader)
	 */
	@Override
	public void updateNClob(String columnLabel, Reader reader) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getObject(int, java.lang.Class)
	 */
	@SuppressWarnings("unchecked") // known limitation
	@Override
	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
		return (T) getObject(columnIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSet#getObject(java.lang.String, java.lang.Class)
	 */
	@SuppressWarnings("unchecked") // known limitation
	@Override
	public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
		return (T) getObject(columnLabel);
	}

}
