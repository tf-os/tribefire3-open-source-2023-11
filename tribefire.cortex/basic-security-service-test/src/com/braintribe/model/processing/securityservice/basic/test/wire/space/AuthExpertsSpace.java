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
package com.braintribe.model.processing.securityservice.basic.test.wire.space;

import static com.braintribe.wire.api.util.Sets.set;

import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.credential.authenticator.ExistingSessionCredentialsAuthenticationServiceProcessor;
import com.braintribe.model.processing.credential.authenticator.GrantedCredentialsAuthenticationServiceProcessor;
import com.braintribe.model.processing.credential.authenticator.TrustedCredentialsAuthenticationServiceProcessor;
import com.braintribe.model.processing.credential.authenticator.UserPasswordCredentialsAuthenticationServiceProcessor;
import com.braintribe.model.processing.securityservice.basic.test.wire.space.access.AuthAccessSpace;
import com.braintribe.model.processing.securityservice.basic.test.wire.space.access.ClientsAccessSpace;
import com.braintribe.model.securityservice.credentials.identification.UserNameIdentification;
import com.braintribe.model.user.User;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

@Managed
public class AuthExpertsSpace implements WireSpace {

	@Import
	private AuthAccessSpace authAccess;

	@Import
	private ClientsAccessSpace cortexAccess;

	@Import
	private UserSessionServiceSpace userSessionService;

	@Import
	private AuthContextSpace authContext;

	@Import
	private CryptoSpace crypto;

	@Managed
	public ExistingSessionCredentialsAuthenticationServiceProcessor existingSessionCredentials() {
		ExistingSessionCredentialsAuthenticationServiceProcessor bean = new ExistingSessionCredentialsAuthenticationServiceProcessor();
		bean.setAuthGmSessionProvider(authAccess::lowLevelSession);
		return bean;
	}

	@Managed
	public TrustedCredentialsAuthenticationServiceProcessor trustedCredentials() {

		TrustedCredentialsAuthenticationServiceProcessor bean = new TrustedCredentialsAuthenticationServiceProcessor();
		bean.setAuthGmSessionProvider(authAccess::lowLevelSession);

		User internalUser = authContext.internalUser().user();
		UserNameIdentification internalUserId = UserNameIdentification.T.create();
		internalUserId.setUserName(internalUser.getName());

		return bean;
	}

	@Managed
	public GrantedCredentialsAuthenticationServiceProcessor grantedCredentials() {

		GrantedCredentialsAuthenticationServiceProcessor bean = new GrantedCredentialsAuthenticationServiceProcessor();
		bean.setAuthGmSessionProvider(authAccess::lowLevelSession);
		bean.setGrantingRoles(set("tf-admin", "tf-locksmith", "tf-internal"));

		return bean;

	}

	@Managed
	public UserPasswordCredentialsAuthenticationServiceProcessor userPasswordCredentials() {
		UserPasswordCredentialsAuthenticationServiceProcessor bean = new UserPasswordCredentialsAuthenticationServiceProcessor();
		bean.setAuthGmSessionProvider(authAccess::lowLevelSession);
		bean.setDecryptSecret(TribefireRuntime.DEFAULT_TRIBEFIRE_DECRYPTION_SECRET);
		bean.setCryptorProvider(crypto.cryptorProvider());
		return bean;
	}

	// protected void configStandardExpert(AuthenticationExpert<?> expert) {
	// expert.setSessionMaxIdleTime(userSessionService.defaultMaxIdleTime());
	// expert.setAuthGmSessionProvider(authAccess::lowLevelSession);
	// expert.setClientGmSessionProvider(cortexAccess::lowLevelSession);
	// expert.setAuthenticationService(authenticationService());
	// expert.setUserSessionService(userSessionService.service());
	// }

}
