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
package tribefire.extension.okta.processing.impl;

import java.io.StringReader;
import java.util.Map;

import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.okta.jwt.impl.DefaultJwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.io.Decoders;
import tribefire.extension.okta.processing.jwt.AbstractOktaJwtTokenCredentialsAuthenticationServiceProcessor;

public class MockOktaJwtTokenCredentialsAuthenticationServiceProcessor extends AbstractOktaJwtTokenCredentialsAuthenticationServiceProcessor {

	private String base64ToString(String value) {
		try {
			return new String(Decoders.BASE64URL.decode(value), "UTF-8");
		} catch (Exception e) {
			throw Exceptions.unchecked(e);
		}
	}

	@Override
	protected Maybe<com.okta.jwt.Jwt> decodeJwt(String token) {

		String parts[] = token.split("\\.");

		String bodyAsStr = parts[1];

		String json = base64ToString(bodyAsStr);

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		Map<String, Object> map = (Map<String, Object>) marshaller.unmarshall(new StringReader(json));

		Claims body = new DefaultClaims(map);

		return Maybe.complete(new DefaultJwt(token, body.getIssuedAt().toInstant(), body.getExpiration().toInstant(), body));
	}
}
