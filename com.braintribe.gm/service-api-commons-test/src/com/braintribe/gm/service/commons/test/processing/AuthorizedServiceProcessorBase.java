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
package com.braintribe.gm.service.commons.test.processing;

import org.junit.Assert;

import com.braintribe.exception.AuthorizationException;
import com.braintribe.gm.service.commons.test.model.AuthorizedServiceRequestBase;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.service.api.ServiceRequest;

public class AuthorizedServiceProcessorBase extends ServiceProcessorBase {
	
	protected static String extractSessionId(AuthorizedRequest serviceRequest) {

		Object sessionId = serviceRequest.getSessionId();

		if (sessionId == null && serviceRequest.getMetaData() != null) {
			sessionId = serviceRequest.getMetaData().get(AuthorizedRequest.sessionId);
		}

		if (sessionId == null) {
			String message = "No session id provided in call to authorization required request [ " + serviceRequest.entityType().getTypeSignature() + " ]";
			throw new AuthorizationException(message);
		}

		return sessionId.toString();

	}
	
	protected void validate(ServiceRequestContext context, ServiceRequest parameter) {
		if (parameter instanceof AuthorizedServiceRequestBase) {
			AuthorizedServiceRequestBase authorizedServiceRequestBase = (AuthorizedServiceRequestBase)parameter;
			if (authorizedServiceRequestBase.getValidateAuthorizationContext()) {
				Assert.assertTrue("A ServiceProcessor handling AuthorizedRequest should be called under an authorization context", context.isAuthorized());
				String sessionId = extractSessionId((AuthorizedRequest)parameter);
				Assert.assertEquals(sessionId, context.getRequestorSessionId());
			}
		}
	}

}