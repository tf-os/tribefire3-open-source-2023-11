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
package tribefire.extension.okta.processing.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.okta.api.model.auth.GetClientSecretAccessToken;
import tribefire.extension.okta.api.model.auth.HasAuthorization;
import tribefire.extension.okta.api.model.auth.OauthAccessToken;
import tribefire.extension.okta.model.OktaError;
import tribrefire.extension.okta.common.OktaCommons;

public class ClientSecretTokenAuthenticationSupplier implements AuthenticationSupplier, OktaCommons {

	private String clientId;
	private String clientSecret;
	private Set<String> scopes = Set.of("okta.users.read", "okta.groups.read", "okta.apps.read");
	private Evaluator<ServiceRequest> evaluator;

	private String jwtToken;
	private String tokenType;
	private Instant validUntil;
	private String oktaDomainId = "default.access.okta";

	private ReentrantLock updateLock = new ReentrantLock();

	@Override
	public void authorizeRequest(HasAuthorization request) {
		updateToken(request);
		request.setAuthorization(tokenType + " " + jwtToken);
	}

	private void updateToken(HasAuthorization originalRequest) {
		Instant now = Instant.now();

		if (jwtToken != null && now.isBefore(validUntil)) {
			return;
		}

		updateLock.lock();
		try {
			if (jwtToken != null && now.isBefore(validUntil)) {
				return;
			}

			GetClientSecretAccessToken request = GetClientSecretAccessToken.T.create();
			request.setClientId(clientId);
			request.setClientSecret(clientSecret);
			request.setScope(scopes.stream().collect(Collectors.joining(" ")));
			request.setGrantType("client_credentials");
			request.setDomainId(oktaDomainId);
			Object result = request.eval(evaluator).get();
			if (result instanceof OauthAccessToken oauthAccessToken) {
				jwtToken = oauthAccessToken.getAccess_token();
				Integer expires_in = oauthAccessToken.getExpires_in();
				if (expires_in != null) {
					validUntil = now.plus((long) (expires_in.doubleValue() * 0.9), ChronoUnit.SECONDS);
				}
				tokenType = oauthAccessToken.getToken_type();
			} else if (result instanceof OktaError error) {
				throw new IllegalStateException("Could not create an Client Secret JWT token: " + error.errorMessage());
			} else {
				throw new IllegalStateException("Could not create an Client Secret JWT token: " + result);
			}
		} finally {
			updateLock.unlock();
		}
	}

	// ***************************************************************************************************
	// Setters
	// ***************************************************************************************************

	@Configurable
	@Required
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Configurable
	@Required
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	@Configurable
	public void setScopes(Set<String> scopes) {
		this.scopes = scopes;
	}

	@Configurable
	@Required
	public void setEvaluator(Evaluator<ServiceRequest> evaluator) {
		this.evaluator = evaluator;
	}

	@Configurable
	@Required
	public void setOktaDomainId(String oktaDomainId) {
		this.oktaDomainId = oktaDomainId;
	}

}
