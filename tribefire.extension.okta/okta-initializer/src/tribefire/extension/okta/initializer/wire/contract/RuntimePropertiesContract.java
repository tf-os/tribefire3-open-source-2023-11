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
package tribefire.extension.okta.initializer.wire.contract;

import java.util.Set;

import com.braintribe.wire.api.annotation.Decrypt;
import com.braintribe.wire.api.annotation.Default;

import tribefire.cortex.initializer.support.wire.contract.PropertyLookupContract;

public interface RuntimePropertiesContract extends PropertyLookupContract {

	String OKTA_SERVICE_BASE_URL();
	boolean ENABLE_OKTA_ACCESS();

	@Default("SSWS")
	String OKTA_HTTP_AUTHORIZATION_SCHEME();
	@Decrypt
	String OKTA_HTTP_AUTHORIZATION_TOKEN();

	String OKTA_HTTP_OAUTH_AUDIENCE();
	@Decrypt
	String OKTA_HTTP_OAUTH_KEY_MODULUS_N_ENCRYPTED();
	@Decrypt
	String OKTA_HTTP_OAUTH_PRIVATE_EXPONENT_D_ENCRYPTED();
	String OKTA_HTTP_OAUTH_CLIENT_ID();
	Integer OKTA_HTTP_OAUTH_EXPIRATION_S();
	Set<String> OKTA_HTTP_OAUTH_SCOPES();

	String OKTA_CLIENT_SECRET_TOKEN_URL();
	String OKTA_CLIENT_ID();
	String OKTA_CLIENT_SECRET();
	Set<String> OKTA_CLIENT_SCOPES();
}
