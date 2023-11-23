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
package com.braintribe.model.shiro.service;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@SelectiveInformation("Authentication: ${name}")
public interface AuthenticationSpecification extends GenericEntity {

	EntityType<AuthenticationSpecification> T = EntityTypes.T(AuthenticationSpecification.class);

	@Name("Name")
	@Description("Name of the authentication provider.")
	String getName();
	void setName(String name);

	@Name("Authentication URL")
	@Description("URL to initiate authentication with this provider.")
	String getAuthenticationUrl();
	void setAuthenticationUrl(String authenticationUrl);

	@Name("Image URL")
	@Description("URL to a small banner image representing the authentication provider.")
	String getImageUrl();
	void setImageUrl(String imageUrl);
}
