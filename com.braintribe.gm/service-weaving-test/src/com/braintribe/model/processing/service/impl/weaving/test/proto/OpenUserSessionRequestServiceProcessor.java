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
package com.braintribe.model.processing.service.impl.weaving.test.proto;


import com.braintribe.model.processing.service.api.ServiceProcessorException;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.weaving.test.SecurityServiceHandler;
import com.braintribe.model.processing.service.weaving.impl.dispatch.ServiceRequestHandler;
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.service.api.ServiceRequest;

public class OpenUserSessionRequestServiceProcessor implements ServiceRequestHandler {

	@Override
	public Object process(Object staticThis, ServiceRequestContext requestContext, ServiceRequest request) throws ServiceProcessorException {
		return ((SecurityServiceHandler)staticThis).openUserSession(requestContext, (OpenUserSession)request);
	}
}
