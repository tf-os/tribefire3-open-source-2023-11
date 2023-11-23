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

import static java.util.Collections.singletonList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.stream.Stream;

import com.braintribe.exception.Exceptions;
import com.braintribe.gm.jdbc.api.GmColumn;
import com.braintribe.gm.jdbc.api.GmSelectionContext;
import com.braintribe.gm.jdbc.api.GmTable;

/**
 * @author peter.gazdik
 */
public abstract class AbstractGmColumn<T> implements GmColumn<T> {

	protected final String name;
	private GmTable table;

	public AbstractGmColumn(String name) {
		this.name = name;
	}

	public void setTable(GmTable table) {
		if (this.table != null && this.table != table)
			throw new IllegalStateException("Cannot set table " + table.getName() + " for column " + name
					+ ", because this column is already associated with table " + this.table.getName());

		this.table = table;
	}

	protected abstract void setPrimaryKey(boolean primaryKey);
	protected abstract void setNotNull(boolean notNull);

	@Override
	public String getSingleSqlColumn() {
		throw new UnsupportedOperationException("Cannot retrieve single column for GmColumn type: " + getClass().getName());
	}

	@Override
	public String getGmName() {
		return name;
	}

	@Override
	public boolean storesLobs() {
		return false;
	}

	@Override
	public final boolean isStoredAsLob(ResultSet rs) {
		try {
			return tryIsStoredAsLob(rs);

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while resolving value for column: " + name);
		}
	}

	protected boolean tryIsStoredAsLob(@SuppressWarnings("unused") ResultSet rs) throws Exception {
		return false;
	}

	@Override
	public T getValue(ResultSet rs, GmSelectionContext context) {
		try {
			return tryGetValue(rs, context);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while resolving value for column: " + name);
		}
	}

	protected abstract T tryGetValue(ResultSet rs, GmSelectionContext context) throws Exception;

	@Override
	public final void bindParameter(PreparedStatement ps, int index, T value) {
		try {
			validateValueType(value);
			tryBind(ps, index, value);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while binding value : " + value + " to column: " + name);
		}
	}

	private void validateValueType(Object value) {
		if (value == null)
			return;

		Class<T> type = type();
		if (!type.isInstance(value))
			throw new IllegalArgumentException("Cannot bind value of type '" + value.getClass().getName() + "'  to column '" + name + "' of type '"
					+ type.getName() + ". Value: " + value);
	}

	protected abstract Class<T> type();

	protected abstract void tryBind(PreparedStatement ps, int index, T value) throws Exception;

	@Override
	public void afterStatementExecuted(PreparedStatement ps) {
		// NO OP
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + name + "]";
	}

	public static abstract class AbstractSingularGmColumn<T> extends AbstractGmColumn<T> {

		private boolean primaryKey;
		private boolean notNull;

		public AbstractSingularGmColumn(String name) {
			super(name);
		}

		// @formatter:off
		@Override protected void setPrimaryKey(boolean primaryKey) { this.primaryKey = primaryKey; }
		@Override public boolean isPrimaryKey() { return primaryKey; }		
		@Override protected void setNotNull(boolean notNull) { this.notNull = notNull; }
		@Override public boolean isNotNull() { return notNull; }		
		@Override public List<String> getSqlColumns() { return singletonList(name); }
		@Override public String getSingleSqlColumn() { return name; }
		// @formatter:on

		@Override
		public Stream<String> streamSqlColumnDeclarations() {
			return Stream.of(name + " " + sqlType() + primaryKeyFlag() + notNullFlag());
		}

		protected abstract String sqlType();

		private String primaryKeyFlag() {
			return primaryKey ? " primary key" : "";
		}

		private String notNullFlag() {
			return notNull ? " not null" : "";
		}

	}

	public static abstract class MultiGmColumn<T> extends AbstractGmColumn<T> {

		private boolean notNull;

		public MultiGmColumn(String name) {
			super(name);
		}

		@Override
		public boolean storesLobs() {
			return true;
		}

		@Override
		protected abstract boolean tryIsStoredAsLob(ResultSet rs) throws Exception;

		@Override
		protected void setPrimaryKey(boolean primaryKey) {
			throw new UnsupportedOperationException("Multi-column cannot be a primary key. Type: " + getClass().getName());
		}

		@Override
		public boolean isPrimaryKey() {
			return false;
		}

		@Override
		protected void setNotNull(boolean notNull) {
			this.notNull = notNull;
		}

		@Override
		public boolean isNotNull() {
			return notNull;
		}

	}

	public static abstract class AbstractDelegatingGmColumn<T, D> extends AbstractGmColumn<T> {

		protected final AbstractGmColumn<D> delegate;

		public AbstractDelegatingGmColumn(AbstractGmColumn<D> delegate) {
			super(delegate.getGmName());
			this.delegate = delegate;
		}

	// @formatter:off
		@Override protected void setPrimaryKey(boolean primaryKey) { delegate.setPrimaryKey(primaryKey); }
		@Override public boolean isPrimaryKey() { return delegate.isPrimaryKey(); }		
		@Override protected void setNotNull(boolean notNull) { delegate.setNotNull(notNull); }		
		@Override public boolean isNotNull() { return delegate.isNotNull(); }
		@Override public String getSingleSqlColumn() { return delegate.getSingleSqlColumn(); }
		@Override public Stream<String> streamSqlColumnDeclarations() { return delegate.streamSqlColumnDeclarations(); }
		@Override public List<String> getSqlColumns() { return delegate.getSqlColumns(); }
		@Override public String getGmName() { return name; }
	// @formatter:on
	}

}
