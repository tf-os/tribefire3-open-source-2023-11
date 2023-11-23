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
package com.braintribe.model.processing.meta.cmd.index;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simple Least-Recently-Used map (although technically it is not exactly that, since not every access means an update
 * to the recently-used information - this behavior is randomized to improve concurrency). The size of the map never
 * grows more than {@code maxSize} value provided via constructor.
 * <p>
 * This implementation is thread-safe.
 */
public final class LRUMap<K, V> implements Map<K, V> {

	private final Map<K, LinkedEntry<K, V>> m = new ConcurrentHashMap<K, LinkedEntry<K, V>>();
	private int maxSize;
	private LinkedEntry<K, V> sentinel;

	private int mod;

	private static class LinkedEntry<K, V> {
		K key;
		V value;

		LinkedEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		void remove() {
			previous.next = next;
			next.previous = previous;
		}

		void insertBefore(LinkedEntry<K, V> entry) {
			next = entry;
			previous = entry.previous;

			previous.next = this;
			next.previous = this;
		}

		LinkedEntry<K, V> previous;
		LinkedEntry<K, V> next;
	}

	/** Equivalent of calling {@link #LRUMap(int, int)} with params: (maxSize, 10) */
	public LRUMap(int maxSize) {
		this(maxSize, 10);
	}

	/**
	 * @param maxSize
	 *            maximum size this map can have
	 * @param mod
	 *            specifies probability that the ordered list of entries will be updated when accessed (probability = 1/
	 *            {@code mod}). That means, if the value of the parameter is 1, we have a real least-recently used map.
	 */
	public LRUMap(int maxSize, int mod) {
		this.maxSize = maxSize;
		this.mod = mod;

		sentinel = new LinkedEntry<K, V>(null, null);
		sentinel.next = sentinel.previous = sentinel;
	}

	@Override
	public int size() {
		return m.size();
	}

	@Override
	public boolean isEmpty() {
		return m.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return m.containsKey(key);
	}

	@Override
	public V get(Object key) {
		LinkedEntry<K, V> entry = m.get(key);
		if (entry == null) {
			return null;
		}

		notifyAccess(entry);
		return entry.value;
	}

	private void notifyAccess(LinkedEntry<K, V> entry) {
		if (ThreadLocalRandom.current().nextInt() % mod != 0 || sentinel.next == entry) {
			/* If entry is first already, or God of Randomness is against, we do not update entry order in list */
			return;
		}

		setFirst(entry);
	}

	private synchronized void setFirst(LinkedEntry<K, V> entry) {
		LinkedEntry<K, V> first = sentinel.next;
		if (first == entry || entry.next == null) {
			/* entry.next may be null if some other thread has removed the entry after "m.get(key)" was invoked in the
			 * method #get(Object) by this thread. */
			return;
		}

		entry.remove();
		entry.insertBefore(first);
	}

	@Override
	public synchronized V put(K key, V value) {
		if (m.size() == maxSize) {
			removeLRU();
		}
		LinkedEntry<K, V> entry = new LinkedEntry<K, V>(key, value);
		LinkedEntry<K, V> val = m.put(key, entry);

		notifyNew(entry);
		if (val != null) {
			removeEntry(val);
		}

		return val == null ? null : val.value;
	}

	private void notifyNew(LinkedEntry<K, V> entry) {
		entry.insertBefore(sentinel.next);
	}

	private void removeLRU() {
		if (m.size() < maxSize) {
			return;
		}

		LinkedEntry<K, V> lru = sentinel.previous;
		m.remove(lru.key);
		removeEntry(lru);
	}

	private void removeEntry(LinkedEntry<K, V> entry) {
		entry.remove();
		entry.next = entry.previous = null;
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Method 'LRUMap.clear' is not supported!");
	}

	@Override
	public V remove(Object key) {
		throw new UnsupportedOperationException("Method 'LRUMap.remove' is not supported!");
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		throw new UnsupportedOperationException("Method 'LRUMap.putAll' is not supported!");
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException("Method 'LRUMap.containsValue' is not supported!");
	}

	@Override
	public Set<K> keySet() {
		throw new UnsupportedOperationException("Method 'LRUMap.keySet' is not supported!");
	}

	@Override
	public Collection<V> values() {
		throw new UnsupportedOperationException("Method 'LRUMap.values' is not supported!");
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException("Method 'LRUMap.entrySet' is not supported!");
	}

}
