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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * An operation basically describes the api for a certain path/method combination
 * <p>
 * For details see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#operationObject
 */
public interface OpenapiOperation extends GenericEntity {

	EntityType<OpenapiOperation> T = EntityTypes.T(OpenapiOperation.class);

	String getSummary();
	void setSummary(String summary);

	String getDescription();
	void setDescription(String description);

	/**
	 * The key here is either a code (200, 500, ...) or "default" (which is why it's a string and not an int).
	 */
	@Mandatory
	Map<String, OpenapiResponse> getResponses();
	void setResponses(Map<String, OpenapiResponse> responses);

	List<String> getTags();
	void setTags(List<String> tags);

	List<OpenapiParameter> getParameters();
	void setParameters(List<OpenapiParameter> parameters);

	OpenapiRequestBody getRequestBody();
	void setRequestBody(OpenapiRequestBody reqestBody);

	boolean getDeprecated();
	void setDeprecated(boolean deprecated);

	List<OpenapiServer> getServers();
	void setServers(List<OpenapiServer> servers);

}
