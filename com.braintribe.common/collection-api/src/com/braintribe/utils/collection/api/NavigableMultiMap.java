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
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;

/**
 * 
 */
public interface NavigableMultiMap<K, V> extends MultiMap<K, V>, NavigableMap<K, V> {

	/**
	 * {@inheritDoc}
	 * 
	 * Note we are overriding this method, because we specify more strictly the return type.
	 */
	@Override
	NavigableSet<K> keySet();

	/**
	 * Returns the comparator used to order the keys in this multi-map, or <tt>null</tt> if this multi-map uses the {@linkplain Comparable natural
	 * ordering} of its keys.
	 */
	Comparator<? super K> keyComparator();

	/** @return all values for given key in ascending order */
	@Override
	List<V> getAll(K key);

	/** Returns the smallest value for given <tt>key</tt>. */
	@Override
	V get(Object key);

	/** Removes all the values associated with given <tt>key</tt> and returns a list of the removed values. */
	@Override
	List<V> removeAll2(Object key);

	/* we cannot implement this now, because the GWT TreeMap does not provide the descending functionality. */
	// SortedSet<K> descendingKeySet();

	/**
	 * Returns the lowest(smallest) value to which the specified key is mapped, or {@code null} if this map contains no mapping for the key.
	 * 
	 * @param key
	 *            the key whose associated value is to be returned
	 * @return the value to which the specified key is mapped, or <tt>null</tt> if this map contains no mapping for the key
	 */
	V getLowest(K key);

	/** Similar to {@link #getLowest(Object)}, but returns the highest value. */
	V getHighest(K key);

	Map.Entry<K, V> lowerEntry(K key, V value);

	/** @return highest entry with <code>key</code> lower than the one provided. */
	@Override
	Map.Entry<K, V> lowerEntry(K key);

	@Override
	K lowerKey(K key);

	Map.Entry<K, V> floorEntry(K key, V value);

	/** @return highest entry with <code>key</code> not higher than the one provided. */
	@Override
	Map.Entry<K, V> floorEntry(K key);

	@Override
	K floorKey(K key);

	Map.Entry<K, V> ceilingEntry(K key, V value);

	/** @return lowest entry with <code>key</code> not lower than the one provided. */
	@Override
	Map.Entry<K, V> ceilingEntry(K key);

	@Override
	K ceilingKey(K key);

	Map.Entry<K, V> higherEntry(K key, V value);

	/** @return lowest entry with <code>key</code> higher than the one provided. */
	@Override
	Map.Entry<K, V> higherEntry(K key);

	@Override
	K higherKey(K key);

	@Override
	Map.Entry<K, V> firstEntry();

	@Override
	K firstKey();

	@Override
	Map.Entry<K, V> lastEntry();

	@Override
	K lastKey();

	/** Removes and returns the least key-value mapping key-value pair from this multi-map, or {@code null} if the map is empty. */
	@Override
	Map.Entry<K, V> pollFirstEntry();

	/** Removes and returns the greatest key-value mapping key-value pair from this multi-map, or {@code null} if the map is empty. */
	@Override
	Map.Entry<K, V> pollLastEntry();

	@Override
	NavigableMultiMap<K, V> descendingMap();

	/**
	 * Equivalent to <code> subMap(key, true, key, true)</code>.
	 * 
	 * @return subMap containing all the entries for given <code>key</code> and nothing else.
	 */
	NavigableMultiMap<K, V> subMap(K key);

	/** Equivalent to {@code subMap(fromKey, true, toKey, false)} */
	@Override
	NavigableMultiMap<K, V> subMap(K fromKey, K toKey);

	@Override
	NavigableMultiMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive);

	NavigableMultiMap<K, V> subMap(K fromKey, V fromValue, boolean fromInclusive, K toKey, V toValue, boolean toInclusive);

	/** Equivalent to {@code headMap(toKey, false)} */
	@Override
	NavigableMultiMap<K, V> headMap(K toKey);

	@Override
	NavigableMultiMap<K, V> headMap(K toKey, boolean inclusive);

	NavigableMultiMap<K, V> headMap(K toKey, V toValue, boolean inclusive);

	/** Equivalent to {@code tailMap(fromKey, true)} */
	@Override
	NavigableMultiMap<K, V> tailMap(K fromKey);

	@Override
	NavigableMultiMap<K, V> tailMap(K fromKey, boolean inclusive);

	NavigableMultiMap<K, V> tailMap(K fromKey, V fromValue, boolean inclusive);

}
