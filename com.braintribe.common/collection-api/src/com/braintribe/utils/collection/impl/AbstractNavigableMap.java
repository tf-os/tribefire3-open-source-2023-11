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

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 
 */
public abstract class AbstractNavigableMap<K, V> implements NavigableMap<K, V> {

	protected SortedMap<K, V> map;
	protected final Comparator<? super K> keyComparator;
	protected final BoundAwareComparator boundComparator;

	/**
	 * @param keyComparator
	 *            comparator which also handles <tt>nulls</tt> properly
	 */
	public AbstractNavigableMap(Comparator<? super K> keyComparator) {
		this.keyComparator = keyComparator;
		this.map = new TreeMap<K, V>(boundComparator = new BoundAwareComparator());
	}

	/**
	 * Used in sub-class which initializes the map by itself.
	 */
	protected AbstractNavigableMap(Comparator<? super K> keyComparator, BoundAwareComparator boundComparator) {
		this.keyComparator = keyComparator;
		this.boundComparator = boundComparator;
	}

	@Override
	public Comparator<? super K> comparator() {
		return keyComparator;
	}

	@Override
	public K firstKey() {
		if (map.isEmpty()) {
			throw new NoSuchElementException();
		}

		return firstKey(map);
	}

	@Override
	public K lastKey() {
		if (map.isEmpty()) {
			throw new NoSuchElementException();
		}

		return lastKey(map);
	}

	@Override
	public Set<K> keySet() {
		return map.keySet();
	}

	@Override
	public Collection<V> values() {
		return map.values();
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return map.entrySet();
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
	public V get(Object key) {
		return map.get(key);
	}

	@Override
	public V put(K key, V value) {
		return map.put(key, value);
	}

	@Override
	public V remove(Object key) {
		return map.remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		map.putAll(m);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Map.Entry<K, V> lowerEntry(K key) {
		return lastEntry(headMap(key));
	}

	@Override
	public K lowerKey(K key) {
		return lastKey(map.headMap(key));
	}

	@Override
	public Map.Entry<K, V> floorEntry(K key) {
		return lastEntry(headMap(key, true));
	}

	@Override
	public K floorKey(K key) {
		return lastKey(headMap(key, true));
	}

	@Override
	public Map.Entry<K, V> ceilingEntry(K key) {
		return firstEntry(tailMap(key));
	}

	@Override
	public K ceilingKey(K key) {
		return firstKey(map.tailMap(key));
	}

	@Override
	public Map.Entry<K, V> higherEntry(K key) {
		return firstEntry(tailMap(key, false));
	}

	@Override
	public K higherKey(K key) {
		return firstKey(tailMap(key, false));
	}

	@Override
	public Map.Entry<K, V> firstEntry() {
		return firstEntry(map);
	}

	@Override
	public Map.Entry<K, V> lastEntry() {
		return lastEntry(this);
	}

	@Override
	public Map.Entry<K, V> pollFirstEntry() {
		return pollFirstEntry(map);
	}

	@Override
	public Map.Entry<K, V> pollLastEntry() {
		if (map.isEmpty()) {
			return null;
		}
		K lastKey = map.lastKey();

		return pollFirstEntry(subMap(lastKey, true, lastKey, true));
	}

	private static <K, V> Map.Entry<K, V> pollFirstEntry(SortedMap<K, V> map) {
		if (map.isEmpty()) {
			return null;
		}

		Iterator<java.util.Map.Entry<K, V>> it = map.entrySet().iterator();

		Map.Entry<K, V> result = exportEntry(it.next());
		it.remove();

		return result;
	}

	private static <K, V> java.util.Map.Entry<K, V> exportEntry(java.util.Map.Entry<K, V> e) {
		return new BtMapEntry<K, V>(e.getKey(), e.getValue());
	}

	public static class BtMapEntry<K, V> implements Map.Entry<K, V> {

		private final K key;
		private final V value;

		public BtMapEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			throw new UnsupportedOperationException();
		}

	}

	static <K, V> Map.Entry<K, V> firstEntry(SortedMap<K, V> map) {
		return map.isEmpty() ? null : map.entrySet().iterator().next();
	}

	static <K> K firstKey(SortedMap<K, ?> map) {
		return map.isEmpty() ? null : map.firstKey();
	}

	static <K, V> Map.Entry<K, V> lastEntry(NavigableMap<K, V> map) {
		if (map.isEmpty()) {
			return null;
		}
		K lastKey = map.lastKey();
		return map.subMap(lastKey, true, lastKey, true).entrySet().iterator().next();
	}

	static <K> K lastKey(SortedMap<K, ?> map) {
		return map.isEmpty() ? null : map.lastKey();
	}

	@Override
	public NavigableMap<K, V> descendingMap() {
		throw new UnsupportedOperationException();
	}

	@Override
	public NavigableSet<K> navigableKeySet() {
		/* This does not seem to be do-able in an easy way, as the SortedSet itself does not provide a "sorted" view of
		 * the key Set. We would have to do everything on our own, which right now is not needed, so I am skipping it. */
		throw new UnsupportedOperationException();
	}

	@Override
	public NavigableSet<K> descendingKeySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public NavigableMap<K, V> subMap(K fromKey, K toKey) {
		return subMap(fromKey, true, toKey, false);
	}

	@Override
	public NavigableMap<K, V> headMap(K toKey) {
		return headMap(toKey, false);
	}

	@Override
	public NavigableMap<K, V> tailMap(K fromKey) {
		return tailMap(fromKey, true);
	}

	protected class BoundAwareComparator implements Comparator<K> {

		@Override
		public int compare(K o1, K o2) {
			if (o1 instanceof KeyBound) {
				if (o2 instanceof KeyBound) {
					return compareBounds((KeyBound) o1, (KeyBound) o2);
				}
				return compareBoundWithKey((KeyBound) o1, o2);
			}

			if (o2 instanceof KeyBound) {
				return -compareBoundWithKey((KeyBound) o2, o1);
			}

			return keyComparator.compare(o1, o2);
		}

		protected int compareBoundWithKey(KeyBound o1, K o2) {
			return compare((K) o1.key, o1.level, o2, KeyBound.EVEN);
		}

		protected int compareBounds(KeyBound o1, KeyBound o2) {
			return compare((K) o1.key, o1.level, (K) o2.key, o2.level);
		}

		private int compare(K k1, int l1, K k2, int l2) {
			int result = keyComparator.compare(k1, k2);

			return result != 0 ? result : l1 - l2;
		}

	}

}
