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

import java.util.List;
import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * See https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#schemaObject See http://json-schema.org/
 */
public interface SwaggerSchema extends GenericEntity, WithFormat, WithType, WithRef {

	final EntityType<SwaggerSchema> T = EntityTypes.T(SwaggerSchema.class);

	String getTitle();
	void setTitle(String title);

	String getDescription();
	void setDescription(String description);

	/**
	 * For "object" type, the list of required properties.
	 */
	List<String> getRequired();
	void setRequired(List<String> required);

	/**
	 * For "object" type, the map of properties names -> types.
	 */
	Map<String, SwaggerSchema> getProperties();
	void setProperties(Map<String, SwaggerSchema> properties);

	/**
	 * For "array" type, the type if the element in the list/array.
	 */
	SwaggerSchema getItems();
	void setItems(SwaggerSchema items);

	/**
	 * For "enum" type, the list of possible values.
	 */
	List<String> getEnum();
	void setEnum(List<String> enumValues);

	SwaggerSchema getAdditionalProperties();
	void setAdditionalProperties(SwaggerSchema additionalProperties);
}
