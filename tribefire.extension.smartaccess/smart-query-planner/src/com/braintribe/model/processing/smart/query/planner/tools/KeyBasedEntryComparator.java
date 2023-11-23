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
public class KeyBasedEntryComparator<K extends Comparable<K>, V> implements Comparator<Map.Entry<K, V>> {

	@SuppressWarnings("rawtypes")
	public static final KeyBasedEntryComparator<?, ?> INSTANCE = new KeyBasedEntryComparator();

	public static <K extends Comparable<K>, V> KeyBasedEntryComparator<K, V> instance() {
		return (KeyBasedEntryComparator<K, V>) INSTANCE;
	}

	public static <K extends Comparable<K>, V> List<Map.Entry<K, V>> sortMapEntries(Map<K, V> map) {
		List<Map.Entry<K, V>> result = newList(map.entrySet());
		Collections.sort(result, KeyBasedEntryComparator.<K, V> instance());

		return result;
	}

	private KeyBasedEntryComparator() {
	}

	@Override
	public int compare(Entry<K, V> e1, Entry<K, V> e2) {
		return e1.getKey().compareTo(e2.getKey());
	}

}
