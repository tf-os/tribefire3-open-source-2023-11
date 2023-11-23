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
package com.braintribe.model.processing.deployment.hibernate.mapping.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Set of collection related helper methods.
 * TODO: Move to proper auxiliary artifact.
 *
 */
public class CollectionsUtils {

	public static <E> Iterable<E> nullSafe(Iterable<E> it) {
		return it != null ? it : Collections.<E> emptySet();
	}
	
	public static <E> boolean isEmpty(Collection<E> col) {
		return (col == null || col.isEmpty());
	}
	
	public static <K, V> boolean isEmpty(Map<K, V> col) {
		return (col == null || col.isEmpty());
	}
	
	public static <E> int safeSize(Collection<E> col) {
		return (col == null || col.isEmpty()) ? 0 : col.size();
	}
	
	public static <K, V> int safeSize(Map<K, V> col) {
		return (col == null || col.isEmpty()) ? 0 : col.size();
	}
	
	public static<K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map, final boolean inverse) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
	    Collections.sort(list,
	        new Comparator<Map.Entry<K, V>>() {
	            @Override
				public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
	            	if (inverse) return (o2.getValue().compareTo(o1.getValue()));
	                return (o1.getValue().compareTo(o2.getValue()));
	            }
	        });

	    Map<K, V> result = new LinkedHashMap<K, V>();
	    for (Iterator<Map.Entry<K, V>> it = list.iterator(); it.hasNext();) {
	        Map.Entry<K, V> entry = it.next();
	        result.put(entry.getKey(), entry.getValue());
	    }
	    return result;
	}
	
	
	public static<K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map) {
		return sortMapByValue(map, false);
	}
	
}
