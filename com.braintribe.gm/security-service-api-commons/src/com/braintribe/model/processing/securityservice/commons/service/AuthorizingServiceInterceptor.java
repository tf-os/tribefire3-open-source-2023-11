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
package com.braintribe.model.processing.securityservice.commons.service;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.security.reason.AuthenticationFailure;
import com.braintribe.model.processing.service.api.ProceedContext;
import com.braintribe.model.processing.service.api.ReasonedServiceAroundProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.AuthorizableRequest;

public class AuthorizingServiceInterceptor implements ReasonedServiceAroundProcessor<AuthorizableRequest, Object> {
	
	private static String getSessionId(AuthorizableRequest authorizedRequest) {
		String sessionId = authorizedRequest.getSessionId();
		
		if (sessionId == null) {
			// TODO: find out if this is still used some where
			sessionId = (String)authorizedRequest.getMetaData().get(AuthorizableRequest.sessionId);
			
			if ("".equals(sessionId))
				sessionId = null;
		}

		return sessionId;
	}

	
	@Override
	public Maybe<Object> processReasoned(ServiceRequestContext requestContext, AuthorizableRequest request, ProceedContext proceedContext) {
		Maybe<ServiceRequestContext> contextMaybe = new ContextualizedAuthorization<>(requestContext, requestContext, getSessionId(request), requestContext.summaryLogger())
				.withRequest(request)
				.authorizeReasoned(request.requiresAuthentication());
		
		if (contextMaybe.isUnsatisfiedBy(AuthenticationFailure.T))
			return contextMaybe.emptyCast();
		
		return proceedContext.proceedReasoned(contextMaybe.get(), request);
	}
}
