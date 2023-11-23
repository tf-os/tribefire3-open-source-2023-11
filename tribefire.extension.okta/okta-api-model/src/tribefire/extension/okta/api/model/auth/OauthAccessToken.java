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
package tribefire.extension.okta.api.model.auth;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface OauthAccessToken extends GenericEntity {

	EntityType<OauthAccessToken> T = EntityTypes.T(OauthAccessToken.class);

	String token_type = "token_type";
	String expires_in = "expires_in";
	String access_token = "access_token";
	String scope = "scope";

	String getToken_type();
	void setToken_type(String token_type);

	Integer getExpires_in();
	void setExpires_in(Integer expires_in);

	String getAccess_token();
	void setAccess_token(String access_token);

	String getScope();
	void setScope(String scope);
}
