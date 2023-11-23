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
package com.braintribe.model.platformsetup.api.request;

import com.braintribe.model.accessapi.AccessDataRequest;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Basic request type for defining lightweight asset requests.
 * <ul>
 * <li>The {@code accessId} defines the current domain where the request is being triggered (e.g. <i>cortex, workbench, ...</i>)
 * <li>The {@code transferOperation} is optional. There the way of automatic publishing an asset can be configured (local install, deploy, ...)
 * </ul>
 */
@Abstract
public interface TrunkAssetRequestForAccess extends AccessDataRequest {

	EntityType<TrunkAssetRequestForAccess> T = EntityTypes.T(TrunkAssetRequestForAccess.class);

	String ACCESS_ID_SETUP = "access.setup";

	@Override
	default String domainId() {
		return ACCESS_ID_SETUP;
	}

	@Mandatory
	String getAccessId();
	void setAccessId(String accessId);

	String getTransferOperation();
	void setTransferOperation(String transferOperation);

}
