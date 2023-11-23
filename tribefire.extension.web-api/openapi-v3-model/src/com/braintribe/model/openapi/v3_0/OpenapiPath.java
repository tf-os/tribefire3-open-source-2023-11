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
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A collection of all {@link OpenapiOperation}s of different HTTP methods for a certain path
 * <p>
 * See https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#pathItemObject
 */
public interface OpenapiPath extends GenericEntity {

	EntityType<OpenapiPath> T = EntityTypes.T(OpenapiPath.class);

	String getSummary();
	void setSummary(String summary);

	String getDescription();
	void setDescription(String description);

	List<OpenapiServer> getServers();
	void setServers(List<OpenapiServer> servers);

	List<OpenapiParameter> getParameters();
	void setParameters(List<OpenapiParameter> parameters);

	OpenapiOperation getGet();
	void setGet(OpenapiOperation get);

	OpenapiOperation getPost();
	void setPost(OpenapiOperation post);

	OpenapiOperation getPut();
	void setPut(OpenapiOperation put);

	OpenapiOperation getDelete();
	void setDelete(OpenapiOperation delete);

	OpenapiOperation getOptions();
	void setOptions(OpenapiOperation options);

	OpenapiOperation getHead();
	void setHead(OpenapiOperation head);

	OpenapiOperation getPatch();
	void setPatch(OpenapiOperation patch);

	OpenapiOperation getTrace();
	void setTrace(OpenapiOperation trace);

}
