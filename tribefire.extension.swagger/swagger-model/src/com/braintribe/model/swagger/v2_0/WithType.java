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
package com.braintribe.model.swagger.v2_0;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * See http://json-schema.org/latest/json-schema-core.html#rfc.section.4.2.1
 */
public interface WithType extends GenericEntity {

	final EntityType<WithType> T = EntityTypes.T(WithType.class);

	/**
	 * This should really be an enum, but we want it to be serialized as String so we have no choice.
	 * 
	 * Possible values: "null", "boolean", "string", "number", "integer", "object" or "array"
	 */
	String getType();
	void setType(String type);

}
