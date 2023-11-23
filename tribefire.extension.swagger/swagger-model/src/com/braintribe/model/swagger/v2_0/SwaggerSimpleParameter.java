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

import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * This is the parameter
 * 
 * See https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#parameterObject
 */
public interface SwaggerSimpleParameter extends SwaggerParameter, WithFormat, WithType {

	final EntityType<SwaggerSimpleParameter> T = EntityTypes.T(SwaggerSimpleParameter.class);

	/**
	 * Possible values: "string", "number", "integer", "boolean", "array" or "file"
	 */
	@Override
	@Mandatory
	String getType();

	boolean getAllowEmptyValue();
	void setAllowEmptyValue(boolean allowEmptyValue);

	String getCollectionFormat();
	void setCollectionFormat(String collectionFormat);

	SwaggerItems getItems();
	void setItems(SwaggerItems items);

	String getDefault();
	void setDefault(String defaultValue);
}
