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
package com.braintribe.model.generic.manipulation;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.value.PreliminaryEntityReference;

public interface AcquireManipulation extends LifecycleManipulation {

	EntityType<AcquireManipulation> T = EntityTypes.T(AcquireManipulation.class);

	/**
	 * Entity itself or {@link PreliminaryEntityReference} <br>
	 * NOTE that in case of a remotified stack, all acquired entities must be referenced with preliminary references
	 * only.
	 */
	@Override
	GenericEntity getEntity();

	String getEntityGlobalId();
	void setEntityGlobalId(String entityGlobalId);

	@Override
	default ManipulationType manipulationType() {
		return ManipulationType.ACQUIRE;
	}

}
