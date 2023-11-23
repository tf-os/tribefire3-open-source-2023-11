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

import javax.servlet.http.HttpServletRequest;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.web.servlet.auth.WebCredentialsProvider;

public interface CredentialFromAuthorizationHeaderProvider extends WebCredentialsProvider {

	Maybe<Credentials> HEADER_NOT_FOUND = Reasons.build(NotFound.T).text("HTTP Authorization header parameter not present").toMaybe();

	@Override
	default Maybe<Credentials> provideCredentials(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");

		if (authHeader == null || authHeader.isEmpty())
			return HEADER_NOT_FOUND;

		return provideCredentials(authHeader);
	}

	Maybe<Credentials> provideCredentials(String authHeader);

}
