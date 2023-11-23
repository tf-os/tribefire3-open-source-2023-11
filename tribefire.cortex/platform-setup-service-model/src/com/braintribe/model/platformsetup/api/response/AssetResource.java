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
package com.braintribe.model.platformsetup.api.response;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.platformsetup.api.request.TransferAsset;
import com.braintribe.model.resource.Resource;

/**
 * Response for {@link TransferAsset}, which previously installed the asset locally or deployed it remotely, but now simply returns a {@link Resource}
 * containing all the artifact parts zipped.
 */
public interface AssetResource extends PlatformAssetResponse {

	EntityType<AssetResource> T = EntityTypes.T(AssetResource.class);

	Resource getResource();
	void setResource(Resource resource);

}
