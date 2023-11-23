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
package com.braintribe.model.processing.rpc.test.wire.space;

import com.braintribe.model.crypto.configuration.encryption.SymmetricEncryptionConfiguration;
import com.braintribe.model.processing.rpc.commons.api.config.GmRpcClientConfig;
import com.braintribe.model.processing.rpc.test.commons.TestAuthenticatingUserSessionProvider;
import com.braintribe.model.processing.rpc.test.commons.TestClientMetaDataProvider;
import com.braintribe.model.processing.rpc.test.commons.TestRpcClientAuthorizationContext;
import com.braintribe.model.processing.securityservice.commons.provider.SessionIdFromUserSessionProvider;
import com.braintribe.model.securityservice.credentials.TrustedCredentials;
import com.braintribe.model.securityservice.credentials.identification.UserNameIdentification;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

@Managed
public class ClientCommonsSpace implements WireSpace {

	@Import
	private CommonsSpace commons;

	@Import
	private CryptoSpace crypto;

	@Import
	private MarshallingSpace marshalling;

	@Import
	private ServerCommonsSpace serverCommons;

	public void config(GmRpcClientConfig bean, boolean reauthorization) {
		bean.setMarshaller(marshalling.binMarshaller()); // TODO: change in tests.
		bean.setMetaDataProvider(metaDataProvider());
		if (reauthorization) {
			bean.setAuthorizationContext(authContext());
		}
	}

	@Managed
	public TestRpcClientAuthorizationContext authContext() {
		TestRpcClientAuthorizationContext bean = new TestRpcClientAuthorizationContext();
		bean.setMaxRetries(5);
		bean.setAuthorizationFailureListener(userSessionProvider());
		return bean;
	}

	@Managed
	public TestClientMetaDataProvider metaDataProvider() {

		SessionIdFromUserSessionProvider sessionIdProvider = new SessionIdFromUserSessionProvider();
		sessionIdProvider.setUserSessionProvider(userSessionProvider());

		TestClientMetaDataProvider bean = new TestClientMetaDataProvider();
		bean.setSessionIdProvider(sessionIdProvider);
		return bean;

	}

	@Managed
	public TestAuthenticatingUserSessionProvider userSessionProvider() {
		TestAuthenticatingUserSessionProvider bean = new TestAuthenticatingUserSessionProvider();
		bean.setEvaluator(serverCommons.serviceRequestEvaluator());

		TrustedCredentials credentials = TrustedCredentials.T.create();
		UserNameIdentification userNameIdentification = UserNameIdentification.T.create();
		userNameIdentification.setUserName("testuser");
		credentials.setUserIdentification(userNameIdentification);

		bean.setCredentials(credentials);
		return bean;
	}

	@Managed
	private SymmetricEncryptionConfiguration requestEncryptionConfiguration() {
		SymmetricEncryptionConfiguration bean = SymmetricEncryptionConfiguration.T.create();
		bean.setAlgorithm("DESede");
		return bean;
	}

}
