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
package com.braintribe.gm.service.impl;

import com.braintribe.model.processing.service.api.ProceedContext;
import com.braintribe.model.processing.service.api.ServiceAroundProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.api.aspect.DomainIdAspect;
import com.braintribe.model.service.api.ServiceRequest;

public class DomainIdServiceAroundProcessor implements ServiceAroundProcessor<ServiceRequest, Object> {

	public final static DomainIdServiceAroundProcessor INSTANCE = new DomainIdServiceAroundProcessor();

	private DomainIdServiceAroundProcessor() {
		// singleton
	}

	@Override
	public Object process(ServiceRequestContext requestContext, ServiceRequest request, ProceedContext proceedContext) {
		ServiceRequestContext contextWithDomainId = requestContext.derive().set(DomainIdAspect.class, request.domainId()).build();
		return proceedContext.proceed(contextWithDomainId, request);
	}

}
