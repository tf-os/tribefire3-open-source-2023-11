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
package com.braintribe.model.processing.session.wire.common.space;

import com.braintribe.model.processing.session.wire.common.contract.RemoteAuthenticationContract;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.securityservice.credentials.UserPasswordCredentials;
import com.braintribe.model.securityservice.credentials.identification.UserNameIdentification;
import com.braintribe.wire.api.annotation.Managed;

/**
 * Please always import {@link RemoteAuthenticationContract} instead of this space directly because credentials and url
 * are configured from the outside.
 *
 * @author Neidhart.Orlich
 *
 */
@Managed
public class RemoteAuthenticationSpace implements RemoteAuthenticationContract {
	private final Credentials credentials;
	private final String url;

	public static final RemoteAuthenticationSpace CORTEX_LOCALHOST = with("http://localhost:8080/tribefire-services", "cortex", "cortex");

	public RemoteAuthenticationSpace(Credentials credentials, String url) {
		this.credentials = credentials;
		this.url = url;
	}

	public static RemoteAuthenticationSpace with(String url, String username, String password) {
		UserNameIdentification cortexUserIdentification = UserNameIdentification.T.create();
		cortexUserIdentification.setUserName(username);

		UserPasswordCredentials cortexCredentials = UserPasswordCredentials.T.create();
		cortexCredentials.setPassword(password);
		cortexCredentials.setUserIdentification(cortexUserIdentification);

		return new RemoteAuthenticationSpace(cortexCredentials, url);
	}

	@Override
	public Credentials credentials() {
		return credentials;
	}

	@Override
	public String tfServicesUrl() {
		return url;
	}

}
