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
package com.braintribe.model.processing.smart.query.planner.tools;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 */
public class ValueBasedEntryComparator<K, V extends Comparable<V>> implements Comparator<Map.Entry<K, V>> {

	@SuppressWarnings("rawtypes")
	public static final ValueBasedEntryComparator<?, ?> INSTANCE = new ValueBasedEntryComparator();

	public static <K, V extends Comparable<V>> ValueBasedEntryComparator<K, V> instance() {
		return (ValueBasedEntryComparator<K, V>) INSTANCE;
	}

	public static <K, V extends Comparable<V>> List<Map.Entry<K, V>> sortMapEntries(Map<K, V> map) {
		List<Map.Entry<K, V>> result = newList(map.entrySet());
		Collections.sort(result, ValueBasedEntryComparator.<K, V> instance());

		return result;
	}

	private ValueBasedEntryComparator() {
	}

	@Override
	public int compare(Entry<K, V> e1, Entry<K, V> e2) {
		return e1.getValue().compareTo(e2.getValue());
	}

}
