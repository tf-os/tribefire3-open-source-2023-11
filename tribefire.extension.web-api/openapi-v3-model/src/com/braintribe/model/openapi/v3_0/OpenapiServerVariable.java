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
package com.braintribe.model.openapi.v3_0;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#serverVariableObject
 *
 * @see OpenapiServer
 */
public interface OpenapiServerVariable extends GenericEntity {
	EntityType<OpenapiServerVariable> T = EntityTypes.T(OpenapiServerVariable.class);

	@Mandatory
	String getDefault();
	void setDefault(String defaultValue);

	@Mandatory
	List<String> getEnum();
	void setEnum(List<String> enumeration);

	String getDescription();
	void setDescription(String description);
}
