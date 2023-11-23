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
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * See https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#operationObject
 */
public interface SwaggerOperation extends GenericEntity {

	final EntityType<SwaggerOperation> T = EntityTypes.T(SwaggerOperation.class);

	String getSummary();
	void setSummary(String summary);

	String getDescription();
	void setDescription(String description);

	/**
	 * The key here is either a code (200, 500, ...) or "default" (which is why it's a string and not an it).
	 */
	@Mandatory
	Map<String, SwaggerResponse> getResponses();
	void setResponses(Map<String, SwaggerResponse> responses);

	List<String> getTags();
	void setTags(List<String> tags);

	List<SwaggerParameter> getParameters();
	void setParameters(List<SwaggerParameter> parameters);

	/**
	 * @return the mimeTypes that this API consumes
	 */
	List<String> getConsumes();
	void setConsumes(List<String> consumes);

	// /**
	// * @return the mimeTypes that this API produces
	// */
	// List<String> getProduces();
	// void setProduces(List<String> produces);
}
