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
package com.braintribe.utils.collection.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.utils.collection.api.ListMap;

/**
 * A {@link HashMap} based implementation of {@link ListMap}. Per default {@link ArrayList}s are used to create new list
 * values. This behavior can be changed though by passing a supplier to the constructor {@link #HashListMap(Supplier)}
 * 
 * @author Neidhart.Orlich
 *
 */
public class HashListMap<K, V> implements ListMap<K, V> {
	private Map<K, List<V>> map = new HashMap<>();
	private Supplier<List<V>> listSupplier;

	/**
	 * Allows you to specify which kind of list is created by {@link #putSingleElement(Object, Object)} when a value is added for a new key. 
	 * The empty constructor {@link #HashListMap()} uses an {@link ArrayList} per default.
	 * 
	 * @param listSupplier Must return a new empty list
	 */
	public HashListMap(Supplier<List<V>> listSupplier) {
		this.listSupplier = listSupplier;
	}

	public HashListMap() {
		this(ArrayList<V>::new);
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public List<V> get(Object key) {
		return map.get(key);
	}

	@Override
	public List<V> put(K key, List<V> value) {
		return map.put(key, value);
	}

	@Override
	public List<V> remove(Object key) {
		return map.remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends List<V>> m) {
		map.putAll(m);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Set<K> keySet() {
		return map.keySet();
	}

	@Override
	public Collection<List<V>> values() {
		return map.values();
	}

	@Override
	public Set<Entry<K, List<V>>> entrySet() {
		return map.entrySet();
	}

	@Override
	public V getSingleElement(K key) {
		List<V> list = get(key);

		if (list == null || list.isEmpty()) {
			return null;
		} else if (list.size() > 1) {
			throw new IllegalStateException(
					"More than one element found for key " + key + ". If that's intended please use the normal get() method instead.");
		}

		return list.get(0);

	}

	@Override
	public boolean containsKeyValue(K key, V value) {
		List<V> list = get(key);

		if (list == null)
			return false;

		return list.contains(value);
	}

	@Override
	public boolean removeSingleElement(K key, V value) {
		List<V> list = get(key);

		if (list == null)
			return false;

		return list.remove(value);
	}

	@Override
	public void putSingleElement(K key, V value) {
		List<V> list = get(key);

		if (list == null) {
			list = listSupplier.get();
			put(key, list);
		}

		list.add(value);
	}

}
