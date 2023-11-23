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

import com.braintribe.model.access.sql.test.model.SqlAccessTestModel;
import com.braintribe.model.deployment.database.connector.GenericDatabaseConnectionDescriptor;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.deployment.database.pool.HikariCpConnectionPool;
import com.braintribe.model.meta.GmMetaModel;

import tribefire.extension.sqlaccess.model.CustomizableSqlAccess;
import tribefire.extension.sqlaccess.model.RdbmsDriver;

/**
 * @author peter.gazdik
 */
public abstract class AbstractCustomizableSqlAccessDenotationProvider {

	public CustomizableSqlAccess customizableSqlAccess() {
		CustomizableSqlAccess result = CustomizableSqlAccess.T.create();
		result.setMetaModel(model());
		result.setDriver(driver());

		return result;
	}

	public RdbmsDriver driver() {
		RdbmsDriver result = RdbmsDriver.T.create();
		result.setDatabaseName(databaseName());
		result.setMetaModel(model());
		result.setConnectionPool(connectionPool());
		result.setEnsureDatabase(false);

		return result;
	}

	protected abstract String databaseName();

	protected abstract DatabaseConnectionPool connectionPool();

	protected GmMetaModel model() {
		return SqlAccessTestModel.raw();
	}

	protected DatabaseConnectionPool singleThreadHikariCpConnectionPool(
			GenericDatabaseConnectionDescriptor connectionDescriptor) {
		HikariCpConnectionPool result = HikariCpConnectionPool.T.create();
		result.setConnectionDescriptor(connectionDescriptor);
		result.setCheckoutTimeout(10 * 1000);
		result.setMinPoolSize(1);
		// result.setInitialPoolSize(1); why not?
		result.setMaxPoolSize(1);

		return result;
	}

}
