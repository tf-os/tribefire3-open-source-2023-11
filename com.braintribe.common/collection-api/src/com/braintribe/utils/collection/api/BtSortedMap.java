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

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * @deprecated simply use NavigableMap, which is emulated in this artifact.
 */
@Deprecated
public interface BtSortedMap<K, V> extends SortedMap<K, V> {

	/**
	 * {@inheritDoc}
	 * 
	 * Note we are overriding this method, because we specify more strictly the return type.
	 */
	@Override
	SortedSet<K> keySet();

	/**
	 * Returns the comparator used to order the keys in this map, or <tt>null</tt> if this multi-map uses the
	 * {@linkplain Comparable natural ordering} of its keys.
	 * 
	 */
	Comparator<? super K> keyComparator();

	/* we cannot implement this now, because the GWT TreeMap does not provide the descending functionality. */
	// SortedSet<K> descendingKeySet();

	Map.Entry<K, V> lowerEntry(K key, V value);

	K lowerKey(K key);

	/**
	 * @return highest entry with <code>key</code> not higher than the one provided.
	 */
	Map.Entry<K, V> floorEntry(K key);

	K floorKey(K key);

	/**
	 * @return lowest entry with <code>key</code> not lower than the one provided.
	 */
	Map.Entry<K, V> ceilingEntry(K key);

	K ceilingKey(K key);

	/**
	 * @return lowest entry with <code>key</code> higher than the one provided.
	 */
	Map.Entry<K, V> higherEntry(K key);

	K higherKey(K key);

	Map.Entry<K, V> firstEntry();

	@Override
	K firstKey();

	Map.Entry<K, V> lastEntry();

	@Override
	K lastKey();

	/**
	 * Removes and returns the least key-value mapping key-value pair from this multi-map, or {@code null} if the map is
	 * empty.
	 */
	Map.Entry<K, V> pollFirstEntry();

	/**
	 * Removes and returns the greatest key-value mapping key-value pair from this multi-map, or {@code null} if the map
	 * is empty.
	 */
	Map.Entry<K, V> pollLastEntry();

	// NavigableMultiMap<K, V> descendingMap();

	/**
	 * Equivalent to <code> subMap(key, true, key, true)</code>.
	 * 
	 * @return subMap containing all the entries for given <code>key</code> and nothing else.
	 */
	NavigableMultiMap<K, V> subMap(K key);

	/** Equivalent to {@code subMap(fromKey, true, toKey, false)} */
	@Override
	NavigableMultiMap<K, V> subMap(K fromKey, K toKey);

	NavigableMultiMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive);

	/** Equivalent to {@code headMap(toKey, false)} */
	@Override
	NavigableMultiMap<K, V> headMap(K toKey);

	NavigableMultiMap<K, V> headMap(K toKey, boolean inclusive);

	/** Equivalent to {@code tailMap(fromKey, true)} */
	@Override
	NavigableMultiMap<K, V> tailMap(K fromKey);

	NavigableMultiMap<K, V> tailMap(K fromKey, boolean inclusive);

}
