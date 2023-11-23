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
package com.braintribe.model.access.sql.test.base.denotation;

import org.apache.derby.jdbc.EmbeddedDriver;

import com.braintribe.model.deployment.database.connector.GenericDatabaseConnectionDescriptor;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;

import tribefire.extension.sqlaccess.model.RdbmsDriver;

/**
 * @author peter.gazdik
 */
public class DerbyBasedSqlAccessProvider extends AbstractCustomizableSqlAccessDenotationProvider {

	static String databaseName = "res/junit/DerbyDb";
	static int derbyPort = 1527;
	static String user = null;
	static String password = null;

	public static RdbmsDriver newDriver() {
		return new DerbyBasedSqlAccessProvider().driver();
	}
	
	@Override
	protected String databaseName() {
		return databaseName;
	}

	@Override
	protected DatabaseConnectionPool connectionPool() {
		return singleThreadHikariCpConnectionPool(derbyConnectionDescriptor());
	}

	private static GenericDatabaseConnectionDescriptor derbyConnectionDescriptor() {
		GenericDatabaseConnectionDescriptor result = GenericDatabaseConnectionDescriptor.T.create();
		result.setDriver(EmbeddedDriver.class.getName());
		result.setUrl(String.format("jdbc:derby:%s;create=true", databaseName));
		result.setUser(user);
		result.setPassword(password);

		return result;
	}

}
