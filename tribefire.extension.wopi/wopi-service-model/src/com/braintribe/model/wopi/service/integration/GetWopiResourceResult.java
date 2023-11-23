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
package com.braintribe.model.wopi.service.integration;

import static tribefire.extension.wopi.model.WopiMetaDataConstants.CURRENT_RESOURCE_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CURRENT_RESOURCE_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.POST_OPEN_RESOURCE_VERSIONS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.POST_OPEN_RESOURCE_VERSIONS_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.RESOURCE_VERSIONS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.RESOURCE_VERSIONS_NAME;

import java.util.List;

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;

/**
 * 
 * 
 *
 */
public interface GetWopiResourceResult extends WopiResult {

	EntityType<GetWopiResourceResult> T = EntityTypes.T(GetWopiResourceResult.class);

	String currentResource = "currentResource";
	String resourceVersions = "resourceVersions";
	String postOpenResourceVersions = "postOpenResourceVersions";

	@Name(CURRENT_RESOURCE_NAME)
	@Description(CURRENT_RESOURCE_DESCRIPTION)
	Resource getCurrentResource();
	void setCurrentResource(Resource currentResource);

	@Name(RESOURCE_VERSIONS_NAME)
	@Description(RESOURCE_VERSIONS_DESCRIPTION)
	List<Resource> getResourceVersions();
	void setResourceVersions(List<Resource> resourceVersions);

	@Name(POST_OPEN_RESOURCE_VERSIONS_NAME)
	@Description(POST_OPEN_RESOURCE_VERSIONS_DESCRIPTION)
	List<Resource> getPostOpenResourceVersions();
	void setPostOpenResourceVersions(List<Resource> postOpenResourceVersions);

}
