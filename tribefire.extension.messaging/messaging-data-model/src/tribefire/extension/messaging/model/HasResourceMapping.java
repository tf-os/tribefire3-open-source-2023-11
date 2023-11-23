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
package tribefire.extension.messaging.model;

import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;

/**
 * The {{@link #getResourceMapping()}} will be enriched automatically when producing the {@link Message} (writing to the
 * underlying messaging system) and read out when consuming the {@link Message} (from the underlying messaging system)
 */
@Abstract
public interface HasResourceMapping extends GenericEntity {

	EntityType<HasResourceMapping> T = EntityTypes.T(HasResourceMapping.class);

	String resourceMapping = "resourceMapping";

	@Name("Persisted Resource Pairing Map")
	@Description("Resource mapping storage, a pair of Original:Persisted resource versions. Must not be filled by the caller")
	Map<Object, Resource> getResourceMapping();
	void setResourceMapping(Map<Object, Resource> resourceMapping);
}
