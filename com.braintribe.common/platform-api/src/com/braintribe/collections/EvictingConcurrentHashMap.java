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
package com.braintribe.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Extension of the {@link ConcurrentHashMap} class that allows to specify a function that can decide whether an entry should be automatically removed
 * from the map.
 * <p>
 * Whenever either a size-theshold (see {@link #setEvictionThreshold(int)}) or a specific time interval (see {@link #setEvictionInterval(long)}) is
 * reached, the map will try to iterate over all entries in the map and consult the provided eviction policy function whether the entry should be
 * removed from the map.
 * <p>
 * With the <code>evictOnPut</code> set to true, the eviction run is triggered with any of the following method invocations:
 * {@link #put(Object, Object)}, {@link #putAll(Map)}, or {@link #putIfAbsent(Object, Object)}. When <code>evictOnPut</code> is false, the eviction is
 * triggered with any of the following calls: {@link #get(Object)} or {@link #getOrDefault(Object, Object)}. It is recommended to set
 * <code>evictOnPut</code> to true to prevent concurrent modifications of the map while the eviction process is running.
 * <p>
 * It is also possible to trigger the eviction run by calling {@link #evict()}. Please note that this only the triggers the eviction process. If none
 * of the two threshold limits is reached, nothing will happen.
 *
 * @author roman.kurmanowytsch
 *
 * @param <K>
 *            The type of keys maintained by this map
 * @param <V>
 *            The type of mapped values
 */
public class EvictingConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {

	private static final long serialVersionUID = 1L;

	private boolean evictOnPut = true;

	private Function<Map.Entry<K, V>, Boolean> evictionPolicy;
	private ReentrantLock evictionLock = new ReentrantLock();
	private long lastEvictionRun = -1L;
	private long evictionInterval = 10_000L;
	private int evictionThreshold = 10_000;

	public EvictingConcurrentHashMap(Function<Map.Entry<K, V>, Boolean> evictionPolicy) {
		super();
		if (evictionPolicy == null) {
			throw new IllegalArgumentException("The eviction policy must not be null.");
		}
		this.evictionPolicy = evictionPolicy;
	}
	public EvictingConcurrentHashMap(Function<Map.Entry<K, V>, Boolean> evictionPolicy, boolean evictOnPut) {
		super();
		if (evictionPolicy == null) {
			throw new IllegalArgumentException("The eviction policy must not be null.");
		}
		this.evictionPolicy = evictionPolicy;
		this.evictOnPut = evictOnPut;
	}

	@Override
	public V put(K key, V value) {
		if (evictOnPut) {
			evict();
		}
		return super.put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		if (evictOnPut) {
			evict();
		}
		super.putAll(m);
	}

	@Override
	public V putIfAbsent(K key, V value) {
		if (evictOnPut) {
			evict();
		}
		return super.putIfAbsent(key, value);
	}

	@Override
	public V get(Object key) {
		if (!evictOnPut) {
			evict();
		}
		return super.get(key);
	}

	@Override
	public V getOrDefault(Object key, V defaultValue) {
		if (!evictOnPut) {
			evict();
		}
		return super.getOrDefault(key, defaultValue);
	}

	/**
	 * Starts the eviction process. If {@link #doEviction()} returns false, this method does nothing. Otherwise, it will iterate over all entries and
	 * consult with the provided <code>eviction policy function</code> to determine whether the entry should be removed.
	 * <p>
	 * This process is synchronized so that two thread don't do the processing at the same time. It is, however, possible to have side-effects when
	 * multiple threads are accessing this map. Example: when the eviction is running and an entry is identified as to be evicted, it is not
	 * immediately removed but stored in a list of keys that should be deleted. It is possible that the entry with this key has changed in the
	 * meantime before it is removed by this method. For this reason, it is preferrable to set <code>evictOnPut</code> to true to prevent such
	 * effects.
	 */
	public void evict() {
		if (doEviction()) {
			evictionLock.lock();
			try {
				// Make sure that it is really necessary; it could have happened in the meantime
				if (doEviction()) {

					List<K> keysToEvict = new ArrayList<>();
					for (Map.Entry<K, V> entry : super.entrySet()) {
						if (evictionPolicy.apply(entry)) {
							keysToEvict.add(entry.getKey());
						}
					}
					keysToEvict.stream().forEach(key -> super.remove(key));

					lastEvictionRun = System.currentTimeMillis();
				}
			} finally {
				evictionLock.unlock();
			}
		}
	}

	/**
	 * Determines whether one of the thresholds is reached that would trigger an eviction run.
	 *
	 * @return True, if an eviction run should take place, false otherwise.
	 */
	private boolean doEviction() {
		long now = System.currentTimeMillis();
		long timeSinceLastEviction = now - lastEvictionRun;
		if (timeSinceLastEviction > evictionInterval) {
			return true;
		}
		if (evictionThreshold > 0 && super.size() >= evictionThreshold) {
			return true;
		}
		return false;
	}

	public void setEvictionThreshold(int evictionThreshold) {
		this.evictionThreshold = evictionThreshold;
	}
	public void setEvictionInterval(long evictionInterval) {
		this.evictionInterval = evictionInterval;
	}

	/**
	 * Convenience class that can be used to wrap a key with an object that also contains the timestamp of the creation of this object. This might
	 * come in handy when using {@link EvictingConcurrentHashMap} with an eviction policy based on the age of the entries.
	 *
	 *
	 * @author roman.kurmanowytsch
	 *
	 * @param <M>
	 *            The type of the key to be stored.
	 */
	public static class KeyWithTimestamp<M> {
		private M key;
		private long timestamp;

		public KeyWithTimestamp(M key) {
			this.key = key;
			this.timestamp = System.currentTimeMillis();
		}
		public long getTimestamp() {
			return this.timestamp;
		}
		@Override
		public int hashCode() {
			return key.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof KeyWithTimestamp) {
				KeyWithTimestamp<M> other = (KeyWithTimestamp<M>) obj;
				return key.equals(other.key);
			}
			return false;
		}
		public M getKey() {
			return key;
		}
		@Override
		public String toString() {
			return key.toString();
		}
	}
}