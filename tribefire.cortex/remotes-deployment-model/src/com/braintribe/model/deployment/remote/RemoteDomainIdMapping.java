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
package com.braintribe.model.deployment.remote;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.EntityTypeMetaData;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * {@link MetaData} which should be attached on a {@link ServiceRequest} (or it's sub-type) to specify information on how to delegate the request to a
 * remote server. Currently this means specifying the domainId of the remote server.
 * 
 * @author peter.gazdik
 */
public interface RemoteDomainIdMapping extends EntityTypeMetaData {

	EntityType<RemoteDomainIdMapping> T = EntityTypes.T(RemoteDomainIdMapping.class);

	String getDomainId();
	void setDomainId(String domainId);

}
