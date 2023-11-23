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
package com.braintribe.utils.collection.api;

import java.util.List;
import java.util.Map;

/**
 * As opposed to {@link MultiMap} implementations of this interface just offer a few utility methods for handling maps
 * with list values. Where a {@link MultiMap} can't have repeated values for the same key, a {@link ListMap} of course
 * can.
 * <p>
 * Be aware that <code>ListMap&lt;K, V></code> implements <code>Map&lt;K, List&lt;V>></code>
 * 
 * @author Neidhart.Orlich
 *
 * @param <K> Key type of the Map
 * @param <V> The value type of the Map will be <code>List&lt;V></code>
 */
public interface ListMap<K, V> extends Map<K, List<V>> {
	/**
	 * If the list that is accessed by the key contains exactly one element, it is returned. No list or an empty list returns null;
	 * 
	 * @throws IllegalStateException if the list contains more than one element. 
	 */
	V getSingleElement(K key);

	/**
	 * Returns <tt>true</tt> iff this map contains given key-value pair.
	 */
	boolean containsKeyValue(K key, V value);

	/**
	 * Removes the first occurrence a single element from the list which is accessed by the key. The result is the same as calling {@link List#remove(Object)} on the resulting list.
	 * 
	 * @return true when the element was contained by the list, false otherwise.
	 */
	boolean removeSingleElement(K key, V value);

	/**
	 * Adds a single element to the list which is accessed by the key. If the key is not yet mapped or is mapped to null, a new List is created first.
	 */
	void putSingleElement(K key, V value);
}
