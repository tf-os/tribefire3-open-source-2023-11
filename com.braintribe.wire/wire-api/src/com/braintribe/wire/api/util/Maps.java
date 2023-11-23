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
package com.braintribe.wire.api.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public interface Maps {

	static <K, V> HashMap<K, V> map() {
		return new HashMap<K, V>();
	}

	@SafeVarargs
	static <K, V> HashMap<K, V> map(Entry<K, V> ... entries) {
		HashMap<K, V> map = new HashMap<K, V>();
		put(map, entries);
		return map;
	}

	static <K, V> LinkedHashMap<K, V> linkedMap() {
		return new LinkedHashMap<K, V>();
	}

	@SafeVarargs
	static <K, V> LinkedHashMap<K, V> linkedMap(Entry<K, V> ... entries) {
		LinkedHashMap<K, V> map = new LinkedHashMap<K, V>();
		put(map, entries);
		return map;
	}

	@SafeVarargs
	static <K, V> void put(Map<K, V> map, Entry<K, V> ... entries) {
		if (map != null && entries != null) {
			for (Entry<K, V> entry : entries) {
				map.put(entry.key, entry.value);
			}
		}
	}
	
	static <K, V> Entry<K, V> entry(K key, V value) {
		Entry<K, V> entry = new Entry<K, V>();
		entry.key  = key;
		entry.value  = value;
		return entry;
	}
	
	class Entry<K, V> {
		K key;
		V value;
	}

}
