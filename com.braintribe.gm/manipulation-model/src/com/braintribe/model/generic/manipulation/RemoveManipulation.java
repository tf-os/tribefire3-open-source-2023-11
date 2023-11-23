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

import static java.util.stream.Stream.concat;

import java.util.Map;
import java.util.stream.Stream;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Represents a bulk manipulation for removing items from collection. The {@link #getItemsToRemove() itemsToRemove}
 * property represents elements to be removed, and for various collection we use different representations. See
 * {@link #getItemsToRemove()}.
 */
public interface RemoveManipulation extends CollectionManipulation {

	EntityType<RemoveManipulation> T = EntityTypes.T(RemoveManipulation.class);

	/**
	 * Describes elements to remove from a collection. The expected value depends on the actual collection type:
	 * <ul>
	 * <li>Map - keys are keys to be removed, values are corresponding values. Note that the remove is performed for a
	 * given key even if the value here does not match the actual removed value.</li>
	 * <li>Set - keys and values are the same - elements to remove from the Set</li>
	 * <li>List - keys are integers (which are however only used as hint) and values are the elements to be
	 * removed.</li>
	 * </ul>
	 * 
	 * In all cases, it might happen that we are trying to remove elements from a collection, that is not there, or (in
	 * case of a map), we might remove something else than we thought we would.
	 */
	Map<Object, Object> getItemsToRemove();
	void setItemsToRemove(Map<Object, Object> itemsToRemove);

	@Override
	@SuppressWarnings("unusable-by-js")
	default Stream<GenericEntity> touchedEntities() {
		Map<?, ?> items = getItemsToRemove();
		return concat(CollectionManipulation.super.touchedEntities(), concat( //
				PropertyManipulation.filterTouchedEntities(items.keySet().stream(), isRemote()),
				PropertyManipulation.filterTouchedEntities(items.values().stream(), isRemote())) //
		);
	}

	@Override
	default ManipulationType manipulationType() {
		return ManipulationType.REMOVE;
	}

}
