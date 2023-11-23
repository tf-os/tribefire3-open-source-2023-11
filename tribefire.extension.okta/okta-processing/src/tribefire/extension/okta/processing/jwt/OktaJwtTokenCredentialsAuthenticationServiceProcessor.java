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
package tribefire.extension.okta.processing.jwt;

import java.time.Duration;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.security.reason.InvalidCredentials;
import com.braintribe.logging.Logger;
import com.okta.jwt.AccessTokenVerifier;
import com.okta.jwt.Jwt;
import com.okta.jwt.JwtVerifiers;

public class OktaJwtTokenCredentialsAuthenticationServiceProcessor extends AbstractOktaJwtTokenCredentialsAuthenticationServiceProcessor
		implements InitializationAware {

	private static Logger logger = Logger.getLogger(OktaJwtTokenCredentialsAuthenticationServiceProcessor.class);

	private String issuer;
	private String audience;
	private long connectionTimeoutMs;

	private AccessTokenVerifier accessTokenVerifier;

	@Required
	@Configurable
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	@Required
	@Configurable
	public void setAudience(String audience) {
		this.audience = audience;
	}

	@Required
	@Configurable
	public void setConnectionTimeoutMs(long connectionTimeoutMs) {
		this.connectionTimeoutMs = connectionTimeoutMs;
	}

	@Override
	public void postConstruct() {
		//@formatter:off
		accessTokenVerifier = JwtVerifiers.accessTokenVerifierBuilder()
			.setIssuer(issuer)
			.setAudience(audience)                // defaults to 'api://default'
			.setConnectionTimeout(Duration.ofMillis(connectionTimeoutMs)) // defaults to 1s
			.build();
		//@formatter:on
	}

	@Override
	protected Maybe<Jwt> decodeJwt(String token) {

		try {
			return Maybe.complete(accessTokenVerifier.decode(token));
		} catch (Exception e) {
			String message = "Token could not be verified via " + issuer + ". Token was: " + token;
			logger.debug(() -> message, e);

			return Reasons.build(InvalidCredentials.T).text("JWT Token was not a valid Okta token").toMaybe();
		}
	}
}
