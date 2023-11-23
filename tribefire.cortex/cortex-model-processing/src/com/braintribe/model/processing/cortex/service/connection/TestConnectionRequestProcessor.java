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



import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.check.service.jdbc.SelectedDatabaseConnectionCheck;
import com.braintribe.model.cortexapi.connection.TestConnectionRequest;
import com.braintribe.model.cortexapi.connection.TestConnectionResponse;
import com.braintribe.model.cortexapi.connection.TestDatabaseConnection;
import com.braintribe.model.cortexapi.connection.TestDatabaseConnections;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessors;
import com.braintribe.model.processing.check.api.ParameterizedAccessCheckProcessor;

public class TestConnectionRequestProcessor implements AccessRequestProcessor<TestConnectionRequest, TestConnectionResponse> {
	
	private ParameterizedAccessCheckProcessor<SelectedDatabaseConnectionCheck> databaseConnectionCheck;
	
	@Required
	@Configurable
	public void setDatabaseConnectionCheck(ParameterizedAccessCheckProcessor<SelectedDatabaseConnectionCheck> databaseConnectionCheck) {
		this.databaseConnectionCheck = databaseConnectionCheck;
	}
	
	
	private AccessRequestProcessor<TestConnectionRequest, TestConnectionResponse> dispatcher = AccessRequestProcessors.dispatcher(config->{
		config.register(TestDatabaseConnections.T, this::testDatabaseConnections);
		config.register(TestDatabaseConnection.T, this::testDatabaseConnection);
	});

	@Override
	public TestConnectionResponse process(AccessRequestContext<TestConnectionRequest> context) {
		return dispatcher.process(context);
	}

	public TestConnectionResponse testDatabaseConnections(AccessRequestContext<TestDatabaseConnections> context) {
		return new DatabaseConnectionsTester(context, databaseConnectionCheck).run(); 
	}

	public TestConnectionResponse testDatabaseConnection(AccessRequestContext<TestDatabaseConnection> context) {
		return new DatabaseConnectionTester(context, databaseConnectionCheck).run();
	}

	
	
	
	
}
