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

import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A pool for {@link JsonReferencable} OpenAPI entities that can be reused by referencing them with a json reference
 * <p>
 * See https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#componentsObject
 */
public interface OpenapiComponents extends GenericEntity {

	EntityType<OpenapiComponents> T = EntityTypes.T(OpenapiComponents.class);

	Map<String, OpenapiSchema> getSchemas();
	void setSchemas(Map<String, OpenapiSchema> schemas);

	Map<String, OpenapiResponse> getResponses();
	void setResponses(Map<String, OpenapiResponse> responses);

	Map<String, OpenapiParameter> getParameters();
	void setParameters(Map<String, OpenapiParameter> parameters);

	// Map<String, OpenapiExample> getExamples();
	// void setExamples(Map<String, OpenapiExample> examples);
	//
	Map<String, OpenapiRequestBody> getRequestBodies();
	void setRequestBodies(Map<String, OpenapiRequestBody> requestBodies);

	Map<String, OpenapiHeader> getHeaders();
	void setHeaders(Map<String, OpenapiHeader> headers);

}
