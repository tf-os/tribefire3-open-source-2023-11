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
package com.braintribe.model.processing.smood.population.index;

import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static java.util.Collections.emptyList;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.braintribe.model.generic.GenericEntity;

/**
 * Used only internally as a mixin to bring common implementation to {@link MultiMetricIndex} and {@link UniqueMetricIndex}.
 * 
 * @author peter.gazdik
 */
/* package */ interface SmoodMetricIndex extends MetricIndex {

	@Override
	default Collection<? extends GenericEntity> getRange(Object from, Boolean fromInclusive, Object to, Boolean toInclusive) {
		return mergeAllLevels(getKeyComparator(), smi -> smi.getThisLevelRange(from, fromInclusive, to, toInclusive));
	}

	default NavigableMap<Object, GenericEntity> getThisLevelRange(Object from, Boolean fromInclusive, Object to, Boolean toInclusive) {
		NavigableMap<Object, GenericEntity> navigableMap = getNavigableMap();

		if (fromInclusive == null) {
			if (toInclusive == null)
				return navigableMap;
			else
				return navigableMap.headMap(to, toInclusive);

		} else if (toInclusive == null) {
			return navigableMap.tailMap(from, fromInclusive);

		} else {
			return navigableMap.subMap(from, fromInclusive, to, toInclusive);
		}
	}

	@Override
	default Collection<? extends GenericEntity> getFullRange(boolean reverseOrder) {
		Comparator<Object> keyComparator = getKeyComparator();
		if (reverseOrder)
			keyComparator = keyComparator.reversed();

		return mergeAllLevels(keyComparator, smi -> smi.getThisLevelFullRange(reverseOrder));
	}

	default NavigableMap<Object, GenericEntity> getThisLevelFullRange(boolean reverseOrder) {
		NavigableMap<Object, GenericEntity> navigableMap = getNavigableMap();

		return reverseOrder ? navigableMap.descendingMap() : navigableMap;
	}

	default Collection<? extends GenericEntity> mergeAllLevels(Comparator<Object> keyComparator,
			Function<SmoodMetricIndex, NavigableMap<Object, GenericEntity>> rangeSupplier) {

		List<NavigableMap<Object, GenericEntity>> ranges = ((SmoodIndex) this).meAndSubIndices.stream() //
				.map(index -> (SmoodMetricIndex) index) //
				.map(rangeSupplier) //
				.filter(range -> !range.isEmpty()) //
				.collect(Collectors.toList());

		switch (ranges.size()) {
			case 0:
				return emptyList();
			case 1:
				return first(ranges).values();
			default:
				return new RangesMerger(keyComparator, ranges).merge();
		}
	}

	Comparator<Object> getKeyComparator();

	NavigableMap<Object, GenericEntity> getNavigableMap();

}

/**
 * Merges ranges from different level (entity types) to a single collection by always the entry with the smallest key. If the same key is used on
 * different levels, there is no guarantee about which is picked first, just that subsequent invocation return the indexed entities in the same order.
 */
class RangesMerger {

	private final Comparator<Object> keyComparator;
	private final List<NavigableMap<Object, GenericEntity>> ranges;
	private final List<GenericEntity> result = newList();

	private List<Deque<Entry<Object, GenericEntity>>> deques;

	public RangesMerger(Comparator<Object> keyComparator, List<NavigableMap<Object, GenericEntity>> ranges) {
		this.keyComparator = keyComparator;
		this.ranges = ranges;
		this.mergeRanges();
	}

	public List<GenericEntity> merge() {
		return result;
	}

	private void mergeRanges() {
		deques = ranges.stream() //
				.map(navMap -> new ArrayDeque<>(navMap.entrySet())) //
				.collect(Collectors.toList());

		while (!deques.isEmpty())
			mergeNextEntry();
	}

	private void mergeNextEntry() {
		Deque<Entry<Object, GenericEntity>> deque = null;
		Object key = null;

		for (Deque<Entry<Object, GenericEntity>> d : deques) {
			Entry<Object, GenericEntity> e = d.getFirst();
			Object k = e.getKey();
			if (deque == null || keyComparator.compare(key, k) > 0) {
				deque = d;
				key = k;
			}
		}

		GenericEntity entity = deque.removeFirst().getValue();
		result.add(entity);

		if (deque.isEmpty())
			deques.remove(deque);
	}

}
