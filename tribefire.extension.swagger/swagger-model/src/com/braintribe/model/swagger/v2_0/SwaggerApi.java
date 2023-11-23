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
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * The outer-most entity that corresponds to the Swagger representation of a complete API. This entity may be marshalled
 * to produce a swagger JSON/YAML compatible with the swagger 2.0 spec.
 * 
 * See https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#swagger-object for the exact specs.
 */
public interface SwaggerApi extends GenericEntity {

	EntityType<SwaggerApi> T = EntityTypes.T(SwaggerApi.class);

	@Mandatory
	@Initializer("'2.0'")
	String getSwagger();
	void setSwagger(String swagger);

	@Mandatory
	SwaggerInfo getInfo();
	void setInfo(SwaggerInfo info);

	@Mandatory
	Map<String, SwaggerPath> getPaths();
	void setPaths(Map<String, SwaggerPath> paths);

	List<SwaggerPath> getPathList();
	void setPathList(List<SwaggerPath> paths);

	String getHost();
	void setHost(String basePath);

	String getBasePath();
	void setBasePath(String basePath);

	Map<String, SwaggerSchema> getDefinitions();
	void setDefinitions(Map<String, SwaggerSchema> definitions);

	List<SwaggerTag> getTags();
	void setTags(List<SwaggerTag> tags);

	boolean getUseFullyQualifiedDefinitionName();
	void setUseFullyQualifiedDefinitionName(boolean useFullyQualifiedDefinitionName);

}
