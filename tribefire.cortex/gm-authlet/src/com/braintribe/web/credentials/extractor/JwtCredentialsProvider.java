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
package com.braintribe.web.credentials.extractor;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.logging.Logger;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.securityservice.credentials.JwtTokenCredentials;

public class JwtCredentialsProvider implements CredentialFromAuthorizationHeaderProvider {

	private static Logger logger = Logger.getLogger(JwtCredentialsProvider.class);
	private static final Maybe<Credentials> JWT_TOKEN_NOT_FOUND = Reasons.build(NotFound.T)
			.text("HTTP Authorization header parameter did not contain a JWT token").toMaybe();

	@Override
	public Maybe<Credentials> provideCredentials(String authHeader) {
		authHeader = authHeader.trim();

		int typeSeparatorIndex = authHeader.indexOf(' ');

		if (typeSeparatorIndex == -1)
			return JWT_TOKEN_NOT_FOUND;

		String tokenType = authHeader.substring(0, typeSeparatorIndex).toLowerCase();

		if (tokenType.equals("bearer")) {
			String encodedToken = authHeader.substring(typeSeparatorIndex + 1).trim();
			logger.trace(() -> "Identified JWT token " + encodedToken + " in the Authorization header of the request.");

			JwtTokenCredentials credentials = JwtTokenCredentials.of(encodedToken);
			credentials.setAcquire(true);
			return Maybe.complete(credentials);
		}

		return JWT_TOKEN_NOT_FOUND;
	}

}
