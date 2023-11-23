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
package com.braintribe.gm.jdbc.impl.column;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.braintribe.gm.jdbc.api.GmSelectionContext;
import com.braintribe.gm.jdbc.impl.column.AbstractGmColumn.AbstractSingularGmColumn;

/**
 * @author peter.gazdik
 */
public abstract class SimpleSingularGmColumn<T> extends AbstractSingularGmColumn<T> {

	private final String sqlType;

	public SimpleSingularGmColumn(String name, String sqlType) {
		super(name);
		this.sqlType = sqlType;
	}

	@Override
	protected String sqlType() {
		return sqlType;
	}

	// @formatter:off
	@Override protected void tryBind(PreparedStatement ps, int index, T value) throws SQLException { ps.setObject(index, value); }
	@Override protected T tryGetValue(ResultSet rs, GmSelectionContext context) throws SQLException {
		// If you see an AbstractMethodError here, your JDBC driver is too old. Happens with com.oracle:ojdbc6 for example
		T result = rs.getObject(name, type());
		// MySql and Oracle tend to return default primitive values rather than null from rs.getObject(...)
		return result == null || rs.wasNull() ? null : result;
	}
	// @formatter:on
	
	
	// @formatter:off
	public static class BooleanColumn extends SimpleSingularGmColumn<Boolean> {
		public BooleanColumn(String name, String sqlType) { super(name, sqlType); }
		@Override protected Class<Boolean> type() { return Boolean.class; }
	}

	public static class IntegerColumn extends SimpleSingularGmColumn<Integer> {
		public IntegerColumn(String name, String sqlType) { super(name, sqlType); }
		@Override protected Class<Integer> type() { return Integer.class; }
	}

	public static class LongColumn extends SimpleSingularGmColumn<Long> {
		public LongColumn(String name, String sqlType) { super(name, sqlType); }
		@Override protected Class<Long> type() { return Long.class; }
	}

	public static class FloatColumn extends SimpleSingularGmColumn<Float> {
		public FloatColumn(String name, String sqlType) { super(name, sqlType); }
		@Override protected Class<Float> type() { return Float.class; }
	}

	public static class DoubleColumn extends SimpleSingularGmColumn<Double> {
		public DoubleColumn(String name, String sqlType) { super(name, sqlType); }
		@Override protected Class<Double> type() { return Double.class; }
	}

	public static class BigDecimalColumn extends SimpleSingularGmColumn<BigDecimal> {
		public BigDecimalColumn(String name, String sqlType) { super(name, sqlType); }
		@Override protected Class<BigDecimal> type() { return BigDecimal.class; }
	}

	public static class StringColumn extends SimpleSingularGmColumn<String> {
		public StringColumn(String name, String sqlType) { super(name, sqlType);  }
		@Override protected Class<String> type() { return String.class; }
	}
	// @formatter:on

}
