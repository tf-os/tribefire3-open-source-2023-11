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

import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.gm.model.security.reason.InvalidCredentials;
import com.braintribe.logging.Logger;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.securityservice.credentials.UserPasswordCredentials;
import com.braintribe.utils.StringTools;

public class BasicAuthCredentialsProvider implements CredentialFromAuthorizationHeaderProvider {

	private static Logger logger = Logger.getLogger(BasicAuthCredentialsProvider.class);
	private static final Maybe<Credentials> BASIC_TOKEN_NOT_FOUND = Reasons.build(NotFound.T)
			.text("HTTP Authorization header parameter did not contain a Basic token").toMaybe();

	@Override
	public Maybe<Credentials> provideCredentials(String authHeader) {
		String[] parts = authHeader.split("[ \\\r\\\n\\\t]");
		if (parts.length == 2) {
			String basic = parts[0].trim();
			String base64Encoded = parts[1].trim();

			if (basic.equalsIgnoreCase("Basic")) {
				byte[] decodedBytes;
				try {
					decodedBytes = Base64.getDecoder().decode(base64Encoded);
				} catch (Exception e) {
					logger.debug(() -> "Error while BASE64 decoding: " + base64Encoded);
					return Reasons.build(InvalidCredentials.T).text("Could not decode BASE64 encoding of Basic authorization token").toMaybe();
				}

				try {
					String credentials = new String(decodedBytes, "UTF-8");
					logger.debug(() -> "Credentials: " + StringTools.simpleObfuscatePassword(credentials));
					int p = credentials.indexOf(":");
					if (p != -1) {
						String login = credentials.substring(0, p).trim();
						String password = credentials.substring(p + 1).trim();

						UserPasswordCredentials creds = UserPasswordCredentials.forUserName(login, password);
						creds.setAcquire(true);
						return Maybe.complete(creds);
					} else {
						return Reasons.build(InvalidCredentials.T).text("Invalid Basic authorization token. Missing ':' separator.").toMaybe();
					}
				} catch (UnsupportedEncodingException e) {
					throw new UncheckedIOException(e);
				}
			}
		}

		return BASIC_TOKEN_NOT_FOUND;
	}

}
