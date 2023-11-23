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
import static com.braintribe.utils.lcd.CollectionTools.removeAllFromFirstWhichAreInSecond;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.value.EntityReference;

/**
 * Tracks manipulations for property of type {@link Map} and merges all the manipulations into either one {@link ChangeValueManipulation} or
 * one or both of {@link RemoveManipulation}, {@link AddManipulation} (if both, they will to be in this order).
 * <p>
 * The {@linkplain ChangeValueManipulation} is used iff some of the manipulations for given property is a
 * {@linkplain ChangeValueManipulation} itself, or if a clear on the map is performed. From that point the tracker can basically perform the
 * manipulations by itself and in the end replace them with just one "aggregate" manipulation, that sets the value directly.
 * <p>
 * In the other case the tracker keeps track of entries added to/removed from the map and creates an equivalent manipulation, which first
 * performs all the necessary removes, and the inserts the new entries. It also performs all the natural optimization for manipulations,
 * like in case <code>put(x, y)</code> <code>put(x, z)</code> the first insert may be ignored, or if value is inserted and then removed,
 * then both such manipulations may be ignored.
 */
class MapTracker extends CollectionTracker {

	private Map<Object, Object> adds = CodingMap.create(ElementHashingComparator.INSTANCE);
	private final Map<Object, Object> removes = CodingMap.create(ElementHashingComparator.INSTANCE);

	private boolean startsWithClear = false;

	public MapTracker(Owner owner, String propertySignature) {
		super(owner, propertySignature);
	}

	@Override
	public void onClearCollection() {
		startsWithClear = true;
	}

	@Override
	public void onChangeValue(ChangeValueManipulation m) {
		startsWithClear = true;

		Map<?, ?> newValue = (Map<?, ?>) m.getNewValue();
		if (newValue == null) {
			adds = CodingMap.create(ElementHashingComparator.INSTANCE);
		} else {
			adds = adds == null ? CodingMap.create(ElementHashingComparator.INSTANCE) : adds;
			insertAll(newValue);
		}
	}

	@Override
	public void onBulkInsert(AddManipulation m) {
		insertAll(m.getItemsToAdd());
	}

	@Override
	public void onBulkRemove(RemoveManipulation m) {
		Map<?, ?> items = m.getItemsToRemove();

		for (Map.Entry<?, ?> e : items.entrySet()) {
			remove(e.getKey(), e.getValue());
		}
	}

	private void remove(Object key, Object value) {
		adds.remove(key);

		if (!startsWithClear) {
			removes.put(key, value);
		}
	}

	private void insertAll(Map<?, ?> map) {
		map = ensureComparable(map);

		if (!startsWithClear) {
			removes.keySet().removeAll(map.keySet());
		}

		adds.putAll(map);
	}

	private Map<?, ?> ensureComparable(Map<?, ?> map) {
		Map<Object, Object> m = CodingMap.create(ElementHashingComparator.INSTANCE);
		m.putAll(map);

		return m;
	}

	@Override
	public void appendAggregateManipulations(List<AtomicManipulation> manipulations, Set<EntityReference> entitiesToDelete) {
		removeDeletedEntities(entitiesToDelete);

		if (startsWithClear) {
			ChangeValueManipulation cvm = changeValue(newValue(), owner);
			manipulations.add(cvm);

		} else {
			if (!removes.isEmpty()) {
				RemoveManipulation rm = removeManipulation(removes, owner);
				manipulations.add(rm);
			}

			if (!adds.isEmpty()) {
				AddManipulation im = addManipulation(adds, owner);
				manipulations.add(im);
			}
		}
	}

	private void removeDeletedEntities(Set<EntityReference> entitiesToDelete) {
		if (entitiesToDelete.isEmpty())
			return;

		removeDeletedEntities(adds, entitiesToDelete);
		if (!startsWithClear)
			removeDeletedEntities(removes, entitiesToDelete);
	}

	private void removeDeletedEntities(Map<?, ?> map, Set<EntityReference> entitiesToDelete) {
		removeAllFromFirstWhichAreInSecond(map.keySet(), entitiesToDelete);
		removeAllFromFirstWhichAreInSecond(map.values(), entitiesToDelete);
	}

	private Map<?, ?> newValue() {
		return adds;
	}

}
