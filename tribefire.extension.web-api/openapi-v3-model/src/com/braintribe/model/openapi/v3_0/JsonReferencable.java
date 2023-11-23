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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Models a JSON reference (https://json-spec.readthedocs.io/reference.html). Entities that want to explicitly support
 * being referenced this way (thus supporting respective tooling) can implement this interface.
 *
 * @author Neidhart.Orlich
 *
 */
public interface JsonReferencable extends GenericEntity {

	EntityType<JsonReferencable> T = EntityTypes.T(JsonReferencable.class);

	String get$ref();
	void set$ref(String type);
}
