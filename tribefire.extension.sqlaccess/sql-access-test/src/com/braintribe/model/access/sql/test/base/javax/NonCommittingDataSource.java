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
package com.braintribe.model.access.sql.test.base.javax;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.braintribe.model.generic.reflection.GenericModelException;

/**
 * @author peter.gazdik
 */
public class NonCommittingDataSource extends DelegatingDataSource {

	private final NonCommittingConnection connection;
	
	public NonCommittingDataSource(DataSource delegate) {
		super(delegate);
		
		this.connection = newNonCommittingConnection();
	}

	private NonCommittingConnection newNonCommittingConnection()  {
		try {
			return new NonCommittingConnection(delegate.getConnection());
		} catch (SQLException e) {
			throw new GenericModelException("Failed to get connection.", e);
		}
	}
	
	@Override
	public NonCommittingConnection getConnection() {
		return connection;
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		throw new UnsupportedOperationException("Method 'NonCommittingDataSource.getConnection' is not supported!");
	}

}
