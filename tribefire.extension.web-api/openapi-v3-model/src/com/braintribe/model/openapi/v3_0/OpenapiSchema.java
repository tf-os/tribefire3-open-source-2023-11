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
import java.util.Map;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * See https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#schemaObject
 */
public interface OpenapiSchema extends WithFormat, WithType, JsonReferencable {

	EntityType<OpenapiSchema> T = EntityTypes.T(OpenapiSchema.class);

	boolean getNullable();
	void setNullable(boolean nullable);

	boolean getDeprecated();
	void setDeprecated(boolean deprecated);

	OpenapiDiscriminator getDiscriminator();
	void setDiscriminator(OpenapiDiscriminator discriminator);

	Object getExample();
	void setExample(Object example);

	String getTitle();
	void setTitle(String title);

	Object getDefault();
	void setDefault(Object defaultValue);

	Object getMinimum();
	void setMinimum(Object minimum);

	Object getMaximum();
	void setMaximum(Object maximum);

	Boolean getExclusivMinimum();
	void setExclusivMinimum(Boolean minimum);

	Boolean getExclusivMaximum();
	void setExclusivMaximum(Boolean maximum);

	Long getMinItems();
	void setMinItems(Long minItems);

	Long getMaxItems();
	void setMaxItems(Long maxItems);

	Long getMinLength();
	void setMinLength(Long minLength);

	Long getMaxLength();
	void setMaxLength(Long maxLength);

	Boolean getUniqueItems();
	void setUniqueItems(Boolean uniqueItems);

	/**
	 * For "array" type, the type if the element in the list/array.
	 */
	OpenapiSchema getItems();
	void setItems(OpenapiSchema items);

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
	Map<String, OpenapiSchema> getProperties();
	void setProperties(Map<String, OpenapiSchema> properties);

	/**
	 * For "enum" type, the list of possible values.
	 */
	List<String> getEnum();
	void setEnum(List<String> enumValues);

	/**
	 * Schema for all properties that are not explicitly listed in {@link #getProperties()}
	 */
	OpenapiSchema getAdditionalProperties();
	void setAdditionalProperties(OpenapiSchema additionalProperties);

	List<OpenapiSchema> getAnyOf();
	void setAnyOf(List<OpenapiSchema> anyOf);

	List<OpenapiSchema> getOneOf();
	void setOneOf(List<OpenapiSchema> oneOf);

	List<OpenapiSchema> getAllOf();
	void setAllOf(List<OpenapiSchema> allOf);

}
