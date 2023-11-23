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
package com.braintribe.model.processing.ddra.endpoints.api.v1.handlers;

import com.braintribe.ddra.endpoints.api.api.v1.ApiV1EndpointContext;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resourceapi.request.ResourceDeleteRequest;
import com.braintribe.model.service.api.ServiceRequest;

public class ResourceDeleteHandler extends ResourceHandler {

	private static final Logger log = Logger.getLogger(ResourceDeleteHandler.class);

	public static boolean handleRequest(ApiV1EndpointContext context, ServiceRequest service, PersistenceGmSessionFactory gmSession,
			ModelAccessoryFactory modelAccessoryFactory) {
		if (!ResourceDeleteRequest.T.isInstance(service))
			return false;

		return new ResourceDeleteHandler(context, (ResourceDeleteRequest) service, gmSession, modelAccessoryFactory).handle();
	}

	private final ResourceDeleteRequest resourceDeleteRequest;

	public ResourceDeleteHandler(ApiV1EndpointContext context, ResourceDeleteRequest resourceDeleteRequest, PersistenceGmSessionFactory gmSession,
			ModelAccessoryFactory modelAccessoryFactory) {
		this.resourceDeleteRequest = resourceDeleteRequest;
		if (this.resourceDeleteRequest.getAccessId() == null)
			this.resourceDeleteRequest.setAccessId(context.getServiceDomain());
		this.sessionFactory = gmSession;
		this.modelAccessoryFactory = modelAccessoryFactory;
	}

	private boolean handle() {
		log.info("Handling resources remove for " + resourceDeleteRequest.getResourceId());
		PersistenceGmSession gmSession = openSession(resourceDeleteRequest.getAccessId());
		Resource resource = retrieveResource(gmSession, resourceDeleteRequest.getResourceId(), resourceDeleteRequest.getAccessId());

		// @formatter:off
		gmSession.resources()
				.delete(resource)
				.delete();
		// @formatter:on

		return true;
	}

}
