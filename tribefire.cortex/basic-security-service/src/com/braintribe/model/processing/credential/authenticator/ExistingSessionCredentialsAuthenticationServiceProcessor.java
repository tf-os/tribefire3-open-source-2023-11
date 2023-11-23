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
package com.braintribe.model.processing.credential.authenticator;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.security.reason.InvalidCredentials;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.securityservice.AuthenticateCredentials;
import com.braintribe.model.securityservice.AuthenticateCredentialsResponse;
import com.braintribe.model.securityservice.ValidateUserSession;
import com.braintribe.model.securityservice.credentials.ExistingSessionCredentials;
import com.braintribe.model.securityservice.credentials.identification.UserNameIdentification;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.utils.lcd.StringTools;

public class ExistingSessionCredentialsAuthenticationServiceProcessor
		extends BasicAuthenticateCredentialsServiceProcessor<ExistingSessionCredentials> {

	private static Logger log = Logger.getLogger(ExistingSessionCredentialsAuthenticationServiceProcessor.class);

	@Override
	protected Maybe<AuthenticateCredentialsResponse> authenticateCredentials(ServiceRequestContext context, AuthenticateCredentials request,
			ExistingSessionCredentials credentials) {

		if (StringTools.isBlank(credentials.getExistingSessionId())) {
			log.debug(() -> "Session id is empty in the given credentials: [ " + credentials + " ]");
			return Reasons.build(InvalidCredentials.T).text("ExistingSessionCredentials.existingSessionId must not be null or empty").toMaybe();
		}

		ValidateUserSession validateUserSession = ValidateUserSession.T.create();
		validateUserSession.setSessionId(credentials.getExistingSessionId());

		Maybe<? extends UserSession> userSessionMaybe = validateUserSession.eval(context).getReasoned();

		if (userSessionMaybe.isUnsatisfied()) {
			return userSessionMaybe.whyUnsatisfied().asMaybe();
		}

		UserSession userSession = userSessionMaybe.get();

		if (credentials.getReuseSession())
			return Maybe.complete(buildAuthenticatedUserSessionFrom(userSession));
		else {
			Maybe<User> userMaybe = retrieveUser(UserNameIdentification.of(userSession.getUser().getId()));

			if (userMaybe.isUnsatisfied()) {
				String msg = "User from existing session was not found in persistence for authentication";
				log.debug(() -> msg + ": " + userMaybe.whyUnsatisfied().stringify());
				return Reasons.build(InvalidCredentials.T).text(msg).toMaybe();
			}

			return Maybe.complete(buildAuthenticatedUserFrom(userMaybe.get()));
		}
	}

}
