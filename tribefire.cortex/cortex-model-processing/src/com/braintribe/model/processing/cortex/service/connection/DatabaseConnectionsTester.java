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
package com.braintribe.model.processing.cortex.service.connection;

import java.util.List;

import com.braintribe.model.check.service.jdbc.SelectedDatabaseConnectionCheck;
import com.braintribe.model.cortexapi.connection.TestDatabaseConnections;
import com.braintribe.model.deployment.database.connector.DatabaseConnectionDescriptor;
import com.braintribe.model.deployment.database.pool.ConfiguredDatabaseConnectionPool;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.check.api.ParameterizedAccessCheckProcessor;

public class DatabaseConnectionsTester extends AbstractConnectionTester<SelectedDatabaseConnectionCheck> {

	private List<DatabaseConnectionPool> connectionPools;
	
	
	public DatabaseConnectionsTester(AccessRequestContext<TestDatabaseConnections> context, ParameterizedAccessCheckProcessor<SelectedDatabaseConnectionCheck> checkProcessor) {
		super(context,checkProcessor);
		this.connectionPools = context.getSystemRequest().getConnectionPools();
		
	}
	
	@Override
	protected SelectedDatabaseConnectionCheck createCheckRequest() {
		
		for (DatabaseConnectionPool cp : connectionPools) {
			if (!validateConnectionPool(cp)) {
				return null;
			}
		}
		
		
		SelectedDatabaseConnectionCheck check = SelectedDatabaseConnectionCheck.T.create();
		check.setDatabaseConnectionPoolList(connectionPools);
		return check;
	}
	
	private boolean validateConnectionPool(DatabaseConnectionPool connectionPool) {
		if (connectionPool instanceof ConfiguredDatabaseConnectionPool) {
			DatabaseConnectionDescriptor connectionDescriptor = ((ConfiguredDatabaseConnectionPool) connectionPool).getConnectionDescriptor();
			if (connectionDescriptor == null) {
				notifyError("No connection descriptor provided for connectionPool: "+connectionPool.getExternalId());
				return false;
			}
		}
		return true;
	}

}
