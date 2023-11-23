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
package com.braintribe.model.processing.securityservice.basic;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.securityservice.OpenUserSessionResponse;
import com.braintribe.model.securityservice.OpenUserSessionWithUserAndPassword;
import com.braintribe.model.securityservice.SimplifiedOpenUserSession;
import com.braintribe.model.securityservice.credentials.UserPasswordCredentials;
import com.braintribe.model.securityservice.credentials.identification.UserNameIdentification;

/**
 * The service processor that handles all the simplified open user session requests.
 *
 */
public class SimpleSecurityServiceProcessor extends AbstractDispatchingServiceProcessor<SimplifiedOpenUserSession, OpenUserSessionResponse> {

	@Override
	protected void configureDispatching(DispatchConfiguration<SimplifiedOpenUserSession, OpenUserSessionResponse> dispatching) {
		dispatching.registerReasoned(OpenUserSessionWithUserAndPassword.T, this::openUserSessionWithUserAndPassword);
	}

	private Maybe<? extends OpenUserSessionResponse> openUserSessionWithUserAndPassword(ServiceRequestContext requestContext,
			OpenUserSessionWithUserAndPassword request) {
		OpenUserSession openUserSession = OpenUserSession.T.create();

		openUserSession.setLocale(request.getLocale());
		openUserSession.setMetaData(request.getMetaData());

		UserPasswordCredentials credentials = UserPasswordCredentials.T.create();
		openUserSession.setCredentials(credentials);
		credentials.setPassword(request.getPassword());

		UserNameIdentification userIdentification = UserNameIdentification.T.create();
		credentials.setUserIdentification(userIdentification);
		userIdentification.setUserName(request.getUser());

		return openUserSession.eval(requestContext).getReasoned();
	}

}
