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

import java.util.stream.Stream;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface ChangeValueManipulation extends PropertyManipulation {

	EntityType<ChangeValueManipulation> T = EntityTypes.T(ChangeValueManipulation.class);

	Object getNewValue();
	void setNewValue(Object newValue);

	@Override
	@SuppressWarnings("unusable-by-js")
	default Stream<GenericEntity> touchedEntities() {
		Object newValue = getNewValue();
		return Stream.concat(PropertyManipulation.super.touchedEntities(),
				PropertyManipulation.filterTouchedEntities(Stream.of(newValue), isRemote()));
	}

	@Override
	default ManipulationType manipulationType() {
		return ManipulationType.CHANGE_VALUE;
	}

}
