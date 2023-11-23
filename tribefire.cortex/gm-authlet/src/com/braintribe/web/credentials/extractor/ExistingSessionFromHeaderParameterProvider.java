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
import com.braintribe.web.servlet.auth.Constants;

public class ExistingSessionFromHeaderParameterProvider implements ExistingSessionWebCredentialProvider {

	private static final Maybe<String> NOT_FOUND = Reasons.build(NotFound.T)
			.text("HTTP header parameter " + Constants.HEADER_PARAM_SESSIONID + " not present").toMaybe();

	@Override
	public Maybe<String> findSessionId(HttpServletRequest request) {
		String sessionId = request.getHeader(Constants.HEADER_PARAM_SESSIONID);

		if (sessionId == null || sessionId.isEmpty())
			return NOT_FOUND;

		return Maybe.complete(sessionId);
	}

}
