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
package com.braintribe.model.processing.resource.filesystem.common;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.DispatchableRequest;

public class ServiceIdDispatchingProcessor<T extends DispatchableRequest> implements ServiceProcessor<T, Object> {
	private Map<String, ServiceProcessor<? super T, ?>> delegates = new LinkedHashMap<String, ServiceProcessor<? super T,?>>();
	
	public void register(String serviceId, ServiceProcessor<? super T, ?> processor) {
		delegates.put(serviceId, processor);
	}
	
	@Override
	public Object process(ServiceRequestContext requestContext, T request) {
		ServiceProcessor<? super T, ?> serviceProcessor = delegates.get(request.getServiceId());
		
		if (serviceProcessor == null)
			throw new NoSuchElementException("No ServiceProcessor registered for [" + request.entityType().getTypeSignature() + "] with serviceId [" + request.getServiceId() + "]");
			
		return serviceProcessor.process(requestContext, request);
	}

}
