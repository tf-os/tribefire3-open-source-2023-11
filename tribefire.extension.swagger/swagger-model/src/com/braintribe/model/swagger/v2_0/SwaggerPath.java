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
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * See https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#pathItemObject
 */
public interface SwaggerPath extends GenericEntity, WithRef {

	final EntityType<SwaggerPath> T = EntityTypes.T(SwaggerPath.class);

	@Mandatory
	String getPath();
	void setPath(String path);

	SwaggerOperation getGet();
	void setGet(SwaggerOperation get);

	SwaggerOperation getPost();
	void setPost(SwaggerOperation post);

	SwaggerOperation getPut();
	void setPut(SwaggerOperation put);

	SwaggerOperation getDelete();
	void setDelete(SwaggerOperation delete);

	SwaggerOperation getOptions();
	void setOptions(SwaggerOperation options);

	SwaggerOperation getHead();
	void setHead(SwaggerOperation head);

	SwaggerOperation getPatch();
	void setPatch(SwaggerOperation patch);

}
