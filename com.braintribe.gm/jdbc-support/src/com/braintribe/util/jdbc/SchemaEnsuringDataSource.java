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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.braintribe.utils.lcd.LazyInitialization;

/**
 * DataSource which ensures the DB schema before it acquires a JDBC {@link Connection} from given delegate.
 * 
 * @author peter.gazdik
 */
public abstract class SchemaEnsuringDataSource implements DataSource {

	protected DataSource delegate;

	private final LazyInitialization schemaUpdate = new LazyInitialization(this::updateSchema);

	protected abstract void updateSchema();

	public void setDelegate(DataSource delegate) {
		this.delegate = delegate;
	}

	// DB access - Schema Update Required

	@Override
	public Connection getConnection() throws SQLException {
		schemaUpdate.run();
		return delegate.getConnection();
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		schemaUpdate.run();
		return delegate.getConnection(username, password);
	}

	// Beyond Java 8

	// @Override
	// public ConnectionBuilder createConnectionBuilder() throws SQLException {
	// schemaUpdate.run();
	// return delegate.createConnectionBuilder();
	// }
	//
	// @Override
	// public ShardingKeyBuilder createShardingKeyBuilder() throws SQLException {
	// schemaUpdate.run();
	// return delegate.createShardingKeyBuilder();
	// }

	// NO DB access - Schema Update NOT Required

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return delegate.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return delegate.isWrapperFor(iface);
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return delegate.getParentLogger();
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return delegate.getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		delegate.setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		delegate.setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return delegate.getLoginTimeout();
	}

}
