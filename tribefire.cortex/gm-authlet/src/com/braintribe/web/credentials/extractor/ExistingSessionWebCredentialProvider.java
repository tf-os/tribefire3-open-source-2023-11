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
import com.braintribe.gm.model.security.reason.MissingCredentials;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.securityservice.credentials.ExistingSessionCredentials;
import com.braintribe.web.servlet.auth.WebCredentialsProvider;

public interface ExistingSessionWebCredentialProvider extends WebCredentialsProvider {

	Maybe<String> findSessionId(HttpServletRequest request);

	@Override
	default Maybe<Credentials> provideCredentials(HttpServletRequest request) {
		Maybe<String> sessionIdMaybe = findSessionId(request);

		if (sessionIdMaybe.isUnsatisfied())
			return Reasons.build(MissingCredentials.T).text("No session id found in http request").cause(sessionIdMaybe.whyUnsatisfied()).toMaybe();

		ExistingSessionCredentials c = ExistingSessionCredentials.T.create();
		c.setExistingSessionId(sessionIdMaybe.get());
		c.setReuseSession(true);
		c.setIgnoreReferenceCounterIncrement(false);

		return Maybe.complete(c);
	}

}
