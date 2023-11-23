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
package com.braintribe.model.processing.manipulation.basic.normalization;

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.addManipulation;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.changeValue;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.removeManipulation;
import static com.braintribe.utils.lcd.CollectionTools.removeAllWhenEquivalentSets;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.utils.lcd.CollectionTools;

/**
 * Tracks manipulations for property of type {@link Set} and merges all the manipulations into either one {@link ChangeValueManipulation} or one or
 * both of {@link RemoveManipulation}, {@link AddManipulation} (if both, they will be in this order). It is very similar to what {@link MapTracker}
 * does (basically this is same as map tracker if for each entry the key was equal to the value).
 */
class SetTracker extends CollectionTracker {

	private Set<Object> adds = CodingSet.create(ElementHashingComparator.INSTANCE);
	private final Set<Object> removes = CodingSet.create(ElementHashingComparator.INSTANCE);

	private boolean startsWithClear = false;

	public SetTracker(Owner owner, String propertySignature) {
		super(owner, propertySignature);
	}

	@Override
	public void onClearCollection() {
		startsWithClear = true;
	}

	@Override
	public void onChangeValue(ChangeValueManipulation m) {
		startsWithClear = true;

		Set<?> newValue = getTypeSafeCvmValue(Set.class, m);
		if (newValue == null) {
			adds = CodingSet.create(ElementHashingComparator.INSTANCE);
		} else {
			adds = adds == null ? CodingSet.create(ElementHashingComparator.INSTANCE) : adds;
			insertAll(newValue);
		}
	}

	@Override
	public void onBulkInsert(AddManipulation m) {
		insertAll(m.getItemsToAdd().keySet());
	}

	@Override
	public void onBulkRemove(RemoveManipulation m) {
		removeAll(m.getItemsToRemove().keySet());
	}

	private void removeAll(Set<?> set) {
		set = ensureComparable(set);

		adds.removeAll(set);

		if (!startsWithClear) {
			removes.addAll(set);
		}
	}

	private void insertAll(Collection<?> set) {
		set = ensureComparable(set);

		if (!startsWithClear) {
			removes.removeAll(set);
		}

		adds.addAll(set);
	}

	@Override
	public void appendAggregateManipulations(List<AtomicManipulation> manipulations, Set<EntityReference> entitiesToDelete) {
		removeDeletedEntities(entitiesToDelete);

		if (startsWithClear) {
			ChangeValueManipulation cvm = changeValue(newValue(), owner);
			manipulations.add(cvm);

		} else {
			if (!removes.isEmpty()) {
				RemoveManipulation rm = removeManipulation(CollectionTools.getIdentityMap(removes), owner);
				manipulations.add(rm);
			}

			if (!adds.isEmpty()) {
				AddManipulation im = addManipulation(addsMap(), owner);
				manipulations.add(im);
			}
		}
	}

	private void removeDeletedEntities(Set<EntityReference> entitiesToDelete) {
		if (adds != null)
			removeAllWhenEquivalentSets(adds, entitiesToDelete);
		removeAllWhenEquivalentSets(removes, entitiesToDelete);
	}

	private Set<?> newValue() {
		return adds;
	}

	private Map<Object, Object> addsMap() {
		Map<Object, Object> result = newMap();

		for (Object o : adds)
			result.put(o, o);

		return result;
	}

}
