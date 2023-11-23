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

import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.processing.http.client.HttpClient;

public class StaticContextResolver extends AbstractContextResolver {

	private HttpClient httpClient;
	private ModelAccessory modelAccessory;

	// ***************************************************************************************************
	// Setter
	// ***************************************************************************************************

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}
	
	public void setModelAccessory(ModelAccessory modelAccessory) {
		this.modelAccessory = modelAccessory;
	}

	// ***************************************************************************************************
	// ContextResolver
	// ***************************************************************************************************

	@Override
	protected HttpClient getHttpClient(RequestContextResolver contextResolver) {
		return httpClient;
	}
	
	@Override
	protected ModelMdResolver getModelResolver(ServiceRequestContext serviceContext, ServiceRequest serviceRequest) {
		return this.modelAccessory.getMetaData().useCases(resolverUseCases);
	}
}
