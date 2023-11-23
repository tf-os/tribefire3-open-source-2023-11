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
package com.braintribe.model.swagger.v2_0.meta;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * See https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#securitySchemeObject
 */
public interface SwaggerSecurityScheme extends GenericEntity {

	EntityType<SwaggerSecurityScheme> T = EntityTypes.T(SwaggerSecurityScheme.class);

	@Mandatory
	String getType();
	void setType(String type);

	String getDescription();
	void setDescription(String description);

	String getName();
	void setName(String name);

	String getIn();
	void setIn(String in);

	String getFlow();
	void setFlow(String flow);

	String getAuthorizationUrl();
	void setAuthorizationUrl(String authorizationUrl);

	String getTokenUrl();
	void setTokenUrl(String tokenUrl);

	SwaggerScopesObject getScopes();
	void setScopes(SwaggerScopesObject scopes);
}
