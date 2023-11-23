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
package com.braintribe.model.processing.gcp.service;

import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.gcp.deployment.GcpConnector;
import com.braintribe.model.gcp.service.CheckConnection;
import com.braintribe.model.gcp.service.ConnectionStatus;
import com.braintribe.model.gcp.service.GcpRequest;
import com.braintribe.model.gcp.service.GcpResult;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessors;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.deployment.api.DeployedUnit;
import com.braintribe.model.processing.gcp.connect.GcpStorage;
import com.braintribe.model.processing.gcp.connect.GcpStorageConnector;

public class GcpServiceProcessor implements AccessRequestProcessor<GcpRequest, GcpResult>, LifecycleAware {

	private final static Logger logger = Logger.getLogger(GcpServiceProcessor.class);

	private DeployRegistry deployRegistry;
	

	private AccessRequestProcessor<GcpRequest, GcpResult> delegate = AccessRequestProcessors.dispatcher(dispatching -> {
		dispatching.register(CheckConnection.T, this::checkConnection);
	});

	
	public ConnectionStatus checkConnection(AccessRequestContext<CheckConnection> context) {
		
		ConnectionStatus result = ConnectionStatus.T.create();
		
		CheckConnection request = context.getRequest();
		GcpConnector connector = request.getConnector();
		
		DeployedUnit resolve = deployRegistry.resolve(connector);
		
		long start = System.currentTimeMillis();
		if (resolve != null) {
			GcpStorageConnector connectorImpl = (GcpStorageConnector) resolve.findComponent(GcpConnector.T);
			
			setConnectionStatus(result, connectorImpl);
		}
		
		result.setDurationInMs(System.currentTimeMillis()-start);
		
		return result;
	}
	
	protected static void setConnectionStatus(ConnectionStatus status, GcpStorageConnector connector) {
		
		GcpStorage storage = connector.getStorage();
		
		int totalCount = storage.getBucketCount(500);
		
		status.setBucketCount(totalCount);
	}

	
	@Override
	public GcpResult process(AccessRequestContext<GcpRequest> context) {
		return delegate.process(context);
	}
	@Override
	public void postConstruct() {
		logger.debug(() -> GcpServiceProcessor.class.getSimpleName() + " deployed.");
	}

	@Override
	public void preDestroy() {
		logger.debug(() -> GcpServiceProcessor.class.getSimpleName() + " undeployed.");
	}

	@Required
	public void setDeployRegistry(DeployRegistry deployRegistry) {
		this.deployRegistry = deployRegistry;
	}

}
