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
package com.braintribe.model.shiro.deployment.client;

import java.util.Map;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface ShiroGenericOAuth20Client extends ShiroOAuth20Client {

	final EntityType<ShiroGenericOAuth20Client> T = EntityTypes.T(ShiroGenericOAuth20Client.class);

	void setAuthUrl(String authUrl);
	String getAuthUrl();

	void setProfileAttrs(Map<String, String> profileParams);
	Map<String, String> getProfileAttrs();

	void setProfileNodePath(String profileNodePath);
	String getProfileNodePath();

	void setProfileUrl(String profileUrl);
	String getProfileUrl();

	void setProfileVerb(GenericOAuthClientVerb profileVerb);
	GenericOAuthClientVerb getProfileVerb();

	void setTokenUrl(String tokenUrl);
	String getTokenUrl();

	void setUsePathUrlResolver(Boolean usePathUrlResolver);
	@Initializer("false")
	Boolean getUsePathUrlResolver();

	void setClientAuthenticationMethod(OAuth20ClientAuthenticationMethod clientAuthenticationMethod);
	OAuth20ClientAuthenticationMethod getClientAuthenticationMethod();

}
