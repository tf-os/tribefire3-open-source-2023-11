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
package com.braintribe.model.access.sql.main.assembly;

import java.util.Arrays;

import org.apache.derby.jdbc.EmbeddedDriver;

import com.braintribe.model.access.sql.main.model.Garment;
import com.braintribe.model.access.sql.main.model.Item;
import com.braintribe.model.deployment.database.connector.GenericDatabaseConnectionDescriptor;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.deployment.database.pool.HikariCpConnectionPool;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.util.meta.NewMetaModelGeneration;

import tribefire.extension.sqlaccess.model.RdbmsDriver;

/**
 * @author peter.gazdik
 */
public class RdbmsAssemblyProvider {

	public static RdbmsDriver provide() {
		RdbmsDriver result = RdbmsDriver.T.create();
		result.setDatabaseName(databaseName);
		result.setMetaModel(metaModel);
		result.setConnectionPool(derbyConnectionPool());
		result.setEnsureDatabase(false);

		return result;
	}

	// @formatter:off
	static GmMetaModel metaModel = new NewMetaModelGeneration().buildMetaModel("test:BigDataModel", Arrays.asList(
			Garment.T, 
			Item.T
		));
	// @formatter:on

	static String databaseName = "res/main/RdbmsMain";
	static String derbyUrl = "localhost";
	static int derbyPort = 1527;
	static String user = null;
	static String password = null;

	private static DatabaseConnectionPool derbyConnectionPool() {
		return hikariCpConnectionPool(derbyConnectionDescriptor());
	}

	private static GenericDatabaseConnectionDescriptor derbyConnectionDescriptor() {
		GenericDatabaseConnectionDescriptor result = GenericDatabaseConnectionDescriptor.T.create();
		result.setDriver(EmbeddedDriver.class.getName());
//		result.setUrl(String.format("jdbc:derby://%s:%d/%s;create=true", derbyUrl, derbyPort, databaseName));
		result.setUrl(String.format("jdbc:derby:%s;create=true", databaseName));
		result.setUser(user);
		result.setPassword(password);

		return result;
	}

	private static DatabaseConnectionPool hikariCpConnectionPool(GenericDatabaseConnectionDescriptor connectionDescriptor) {
		HikariCpConnectionPool result = HikariCpConnectionPool.T.create();
		result.setConnectionDescriptor(connectionDescriptor);
		result.setCheckoutTimeout(10*1000);
		result.setMinPoolSize(1); // for derby we don't want multiple connections
		// result.setInitialPoolSize(1); why ?
		result.setMaxPoolSize(1);

		return result;
	}

}
