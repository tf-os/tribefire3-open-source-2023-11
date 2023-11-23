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
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.security.reason.InvalidCredentials;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.securityservice.AuthenticateCredentials;
import com.braintribe.model.securityservice.AuthenticateCredentialsResponse;
import com.braintribe.model.securityservice.credentials.TrustedCredentials;
import com.braintribe.model.securityservice.credentials.identification.UserIdentification;
import com.braintribe.model.user.User;

/**
 * <p>
 * Authentication is granted only for trusted requests.
 * 
 */
public class TrustedCredentialsAuthenticationServiceProcessor extends BasicAuthenticateCredentialsServiceProcessor<TrustedCredentials>
		implements UserIdentificationValidation {

	private static Logger log = Logger.getLogger(TrustedCredentialsAuthenticationServiceProcessor.class);

	@Override
	protected Maybe<AuthenticateCredentialsResponse> authenticateCredentials(ServiceRequestContext requestContext, AuthenticateCredentials request,
			TrustedCredentials credentials) {

		Reason reason = validateCredentials(requestContext, credentials);

		if (reason != null)
			return Maybe.empty(reason);

		UserIdentification userIdentification = credentials.getUserIdentification();

		if (userIdentification == null)
			return Reasons.build(InvalidCredentials.T).text("TrustedCredentials.userIdentification must not be null").toMaybe();

		Maybe<User> userMaybe = retrieveUser(userIdentification);

		if (userMaybe.isUnsatisfied()) {
			log.debug(() -> "Authentication failed: " + userMaybe.whyUnsatisfied().stringify());
			return Reasons.build(InvalidCredentials.T).text("Invalid credentials").toMaybe();
		}

		User user = userMaybe.get();

		log.debug(() -> "Valid user [ " + user.getName() + " ] found based on the given trusted identification [ "
				+ credentials.getUserIdentification() + " ].");

		return Maybe.complete(buildAuthenticatedUserFrom(user));
	}

	private Reason validateCredentials(ServiceRequestContext requestContext, TrustedCredentials credentials) {
		if (!requestContext.isTrusted()) {
			return Reasons.build(InvalidCredentials.T).text("Trusted credentials are not allowed in this context.").toReason();
		}
		return validateUserIdentification(credentials.getUserIdentification());
	}

}
