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
package com.braintribe.model.processing.http.resolver;

import java.util.function.Function;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.deployment.http.meta.HttpProcessWith;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.processing.http.client.HttpClient;

public class DynamicContextResolver extends AbstractContextResolver {

	private ModelAccessoryFactory modelAccessoryFactory;
	private Function<com.braintribe.model.deployment.http.client.HttpClient, HttpClient> clientResolver;
	
	// ***************************************************************************************************
	// Setter
	// ***************************************************************************************************

	@Required
	@Configurable
	public void setModelAccessoryFactory(ModelAccessoryFactory modelAccessoryFactory) {
		this.modelAccessoryFactory = modelAccessoryFactory;
	}

	@Required
	@Configurable
	public void setClientResolver(Function<com.braintribe.model.deployment.http.client.HttpClient, HttpClient> clientResolver) {
		this.clientResolver = clientResolver;
	}

	// ***************************************************************************************************
	// Context Resolver
	// ***************************************************************************************************

	@Override
	protected HttpClient getHttpClient(RequestContextResolver contextResolver) {
		ServiceRequest serviceRequest = contextResolver.serviceRequest;
		HttpProcessWith processWith = contextResolver.modelResolver.entity(serviceRequest).meta(HttpProcessWith.T).exclusive();
		if (processWith == null) {
			throw new IllegalArgumentException("No HttpProcessWith configured for request: "+serviceRequest);
		}
		com.braintribe.model.deployment.http.client.HttpClient clientDennotation = processWith.getClient();
		if (clientDennotation == null) {
			throw new IllegalArgumentException("No HttpClient configured for request: "+serviceRequest);
		}
		return clientResolver.apply(clientDennotation);

	}
	
	@Override
	protected ModelMdResolver getModelResolver(ServiceRequestContext serviceContext, ServiceRequest serviceRequest) {
		String domainId = serviceContext.getDomainId();
		ModelAccessory modelAccessory = modelAccessoryFactory.getForServiceDomain(domainId);
		return modelAccessory.getMetaData().useCases(resolverUseCases);

	}
}
