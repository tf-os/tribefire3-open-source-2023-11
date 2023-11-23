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

import java.util.List;
import java.util.stream.Stream;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface CompoundManipulation extends Manipulation {

	EntityType<CompoundManipulation> T = EntityTypes.T(CompoundManipulation.class);

	List<Manipulation> getCompoundManipulationList();
	void setCompoundManipulationList(List<Manipulation> compoundManipulationList);

	@Override
	default boolean isRemote() {
		List<Manipulation> list = getCompoundManipulationList();
		return list != null && !list.isEmpty() && list.get(0).isRemote();
	}

	@Override
	@SuppressWarnings("unusable-by-js")
	default Stream<AtomicManipulation> stream() {
		List<Manipulation> list = getCompoundManipulationList();
		return list == null ? Stream.empty() : list.stream().flatMap(Manipulation::stream);
	}

	@Override
	@SuppressWarnings("unusable-by-js")
	default Stream<GenericEntity> touchedEntities() {
		List<Manipulation> list = getCompoundManipulationList();
		return list == null ? Stream.empty() : list.stream().flatMap(Manipulation::touchedEntities);
	}

	@Override
	default ManipulationType manipulationType() {
		return ManipulationType.COMPOUND;
	}

	static CompoundManipulation create(List<? extends Manipulation> list) {
		CompoundManipulation result = CompoundManipulation.T.create();
		result.setCompoundManipulationList((List<Manipulation>) (List<?>) list);

		return result;
	}

}
