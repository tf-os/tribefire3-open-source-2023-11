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

import static com.braintribe.utils.SysPrint.spOut;

import java.sql.Connection;
import java.sql.SQLException;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.reflection.GenericModelException;

/**
 * @author peter.gazdik
 */
public class NonCommittingConnection extends DelegatingConnection {

	private static final Logger log = Logger.getLogger(NonCommittingConnection.class);

	public NonCommittingConnection(Connection connection) {
		super(connection);

		this.setAutoCommit(false);
	}

	@Override
	public void setAutoCommit(boolean autoCommit) {
		if (autoCommit) {
			log.warn("This is a NonCommitting transaction, setting autoCommit=true will be ignored.");
			return;
		}

		try {
			delegate.setAutoCommit(false);
		} catch (SQLException e) {
			throw new GenericModelException("Failed to set autoCommit to false.", e);
		}
	}

	@Override
	public boolean getAutoCommit() {
		return false;
	}

	@Override
	public void commit() throws SQLException {
		log.info("Commit will be ignored.");
		spOut("Commit will be ignored.");
	}

	@Override
	public void close() throws SQLException {
		log.info("Close will be ignored.");
		spOut("Close will be ignored.");
	}

}
