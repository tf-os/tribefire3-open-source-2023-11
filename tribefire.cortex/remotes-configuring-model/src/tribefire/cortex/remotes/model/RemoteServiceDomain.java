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
package tribefire.cortex.remotes.model;

import java.util.List;

import com.braintribe.model.descriptive.HasExternalId;
import com.braintribe.model.descriptive.HasName;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.domain.ServiceDomain;

/**
 * @author peter.gazdik
 */
public interface RemoteServiceDomain extends HasExternalId, HasName {

	EntityType<RemoteServiceDomain> T = EntityTypes.T(RemoteServiceDomain.class);

	/** externalId of the remote {@link ServiceDomain} */
	@Mandatory
	String getRemoteDomainId();
	void setRemoteDomainId(String remoteDomainId);

	/** Names of models to include in our domain. */
	@Mandatory
	List<String> getModels();
	void setModels(List<String> models);
}
