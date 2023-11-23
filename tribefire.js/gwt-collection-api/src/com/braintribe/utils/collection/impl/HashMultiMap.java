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

import static java.util.Collections.emptySet;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * 
 */
public class HashMultiMap<K, V> extends AbstractMultiMap<K, V> {

	private final Map<K, Object> map; // value is either V or Set<V>;
	private int size = 0;
	private final int modCount = 0;
	private boolean linked;

	public HashMultiMap() {
		this(false);
	}
	
	public HashMultiMap(boolean linked) {
		this.linked = linked;
		this.map = linked? new LinkedHashMap<>(): new HashMap<>();
	}
	
	/**
	 * A special type of {@link HashSet} which we can only be instantiated inside this class, which used when storing
	 * multiple values for one key. We cannot use just any {@link Set}, because the user might want to have something
	 * like {@code MultiMap<?, Set<?>}, so we need a special implementation which unambiguously tells us, that the set
	 * is not a value itself, but it is used to contain multiple values.
	 */
	
	private interface DistinguishableSet {
		// empty
	}
	
	private static class DistinguishableHashSet<E> extends HashSet<E> implements DistinguishableSet {
		private static final long serialVersionUID = 5446832431866191953L;
	}
	
	private static class DistinguishableLinkedHashSet<E> extends LinkedHashSet<E> implements DistinguishableSet {
		private static final long serialVersionUID = 1173595890562683956L;
	} 

	/** {@inheritDoc} */
	@Override
	public boolean put2(K key, V value) {
		if (putHelper(key, value)) {
			size++;
			return true;
		}

		return false;
	}

	private boolean putHelper(K key, V value) {
		Object mapped = map.get(key);

		if (representsMultipleValues(mapped)) {
			return ((Set<Object>) mapped).add(value);

		} else {
			if (containsMappedValue(key, mapped, value)) {
				return false;
			}

			if (mapped == null && !map.containsKey(key)) {
				map.put(key, value);

			} else {
				setTwoValues(key, mapped, value);
			}

			return true;
		}
	}

	private void setTwoValues(K key, Object mapped, V value) {
		Set<Object> values = linked? new DistinguishableLinkedHashSet<>(): new DistinguishableHashSet<>();
		values.add(mapped);
		values.add(value);

		map.put(key, values);
	}

	@Override
	public boolean containsKeyValue(Object key, Object value) {
		Object mapped = map.get(key);

		if (representsMultipleValues(mapped)) {
			return ((Set<?>) mapped).contains(value);

		} else {
			K castedKey = (K) key;
			return containsMappedValue(castedKey, mapped, value);
		}
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean remove(Object key, Object value) {
		if (removeHelper(key, value)) {
			size--;
			return true;
		}

		return false;
	}

	private boolean removeHelper(Object key, Object value) {
		Object mapped = map.get(key);

		if (representsMultipleValues(mapped)) {
			Set<?> set = (Set<?>) mapped;
			if (set.remove(value)) {
				if (set.isEmpty()) {
					map.remove(key);
				}
				return true;
			}
			return false;

		} else {
			if (containsMappedValue((K) key, mapped, value)) {
				map.remove(key);
				return true;
			}

			return false;
		}
	}

	private boolean containsMappedValue(K key, Object mapped, Object value) {
		if (mapped == null) {
			return value == null && map.containsKey(key);

		} else {
			return mapped.equals(value);
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean removeAll(Object key) {
		int count = countValuesFor(key);
		if (count > 0) {
			map.remove(key);
			size -= count;
			return true;
		}

		return false;
	}

	private int countValuesFor(Object key) {
		Object mapped = map.get(key);

		if (representsMultipleValues(mapped))
			return ((Set<?>) mapped).size();

		else if (mapped != null || map.containsKey(key))
			return 1;
		else

			return 0;
	}

	@Override
	public Collection<V> removeAll2(Object key) {
		Object mapped = map.remove(key);

		if (representsMultipleValues(mapped)) {
			Set<V> vals = (Set<V>) mapped;
			size -= vals.size();
			return vals;

		} else if (mapped != null || map.containsKey(key)) {
			size--;
			return Collections.singleton((V) mapped);
		}

		else
			return emptySet();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public V get(Object key) {
		Object mapped = map.get(key);

		if (mapped == null) {
			return null;
		} else if (representsMultipleValues(mapped)) {
			return ((Set<V>) mapped).iterator().next();

		} else {
			// mapped is the actual mapped value
			return (V) mapped;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Collection<V> getAll(K key) {
		Object mapped = map.get(key);

		if (representsMultipleValues(mapped)) {
			return Collections.unmodifiableSet(((Set<V>) mapped));

		} else if (mapped == null) {
			return map.containsKey(key) ? Arrays.asList((V) null) : Collections.<V> emptySet();

		} else {
			return Arrays.asList((V) mapped);
		}
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void clear() {
		size = 0;
		map.clear();
	}

	private transient KeySet keySet = null;
	private transient EntrySet entrySet = null;

	@Override
	public Set<K> keySet() {
		return keySet != null ? keySet : (keySet = new KeySet());
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return entrySet != null ? entrySet : (entrySet = new EntrySet());
	}

	class KeySet extends AbstractSet<K> {
		@Override
		public Iterator<K> iterator() {
			return new KeyIterator();
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public boolean contains(Object o) {
			return containsKey(o);
		}

		@Override
		public boolean remove(Object o) {
			return HashMultiMap.this.removeAll(o);
		}

		@Override
		public void clear() {
			HashMultiMap.this.clear();
		}
	}

	class KeyIterator implements Iterator<K> {
		K currentKey;
		Iterator<K> it = map.keySet().iterator();

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public K next() {
			return currentKey = it.next();
		}

		@Override
		public void remove() {
			if (currentKey == null)
				throw new IllegalStateException();

			int count = countValuesFor(currentKey);

			it.remove();
			currentKey = null;
			size -= count;
		}
	}

	/**
	 * This ugly code was copied from java.util.TreeMap and adjusted a bit.
	 */
	class EntrySet extends AbstractSet<Map.Entry<K, V>> {

		public EntrySet() {
			super();
		}

		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new EntryIterator();
		}

		@Override
		public boolean contains(Object o) {
			return (o instanceof Map.Entry) ? HashMultiMap.this.containsEntry((Map.Entry<?, ?>) o) : false;
		}

		@Override
		public boolean remove(Object o) {
			return (o instanceof Map.Entry) ? HashMultiMap.this.removeEntry((Map.Entry<?, ?>) o) : false;
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public void clear() {
			HashMultiMap.this.clear();
		}

	}

	protected boolean removeEntry(Map.Entry<?, ?> entry) {
		return remove(entry.getKey(), entry.getValue());
	}

	protected boolean containsEntry(Map.Entry<?, ?> entry) {
		return containsKeyValue(entry.getKey(), entry.getValue());
	}

	class EntryIterator implements Iterator<Map.Entry<K, V>> {

		private int expectedModCount = modCount;
		/* it is important to take the iterator from the map, so that "remove" has no side effects (changing the size,
		 * which the iterator does by itself) */
		private final Iterator<K> keyIterator = map.keySet().iterator();

		private int passedValues = 0;
		private K currentKey;
		private V currentValue;
		private Set<V> valuesForCurrentKey;
		private Iterator<V> valuesIterator;
		private Map.Entry<K, V> currentEntry;

		@Override
		public boolean hasNext() {
			checkForComodification();
			return passedValues < size;
		}

		@Override
		public Map.Entry<K, V> next() {
			checkForComodification();

			currentEntry = prepareNext();
			passedValues++;

			return currentEntry;
		}

		private Map.Entry<K, V> prepareNext() {
			if (valuesIterator != null && valuesIterator.hasNext()) {
				currentValue = valuesIterator.next();
				return toEntry(currentKey, currentValue);
			}

			if (!keyIterator.hasNext()) {
				throw new NoSuchElementException();
			}

			currentKey = keyIterator.next();
			Object value = map.get(currentKey);

			if (representsMultipleValues(value)) {
				valuesForCurrentKey = (Set<V>) value;
				valuesIterator = valuesForCurrentKey.iterator();
				currentValue = valuesIterator.next();

			} else {
				valuesIterator = null;
				currentValue = (V) value;
			}

			return toEntry(currentKey, currentValue);
		}

		private java.util.Map.Entry<K, V> toEntry(K key, V value) {
			return new Entry<K, V>(key, value);
		}

		@Override
		public void remove() {
			if (currentEntry == null)
				throw new IllegalStateException();

			checkForComodification();

			if (valuesIterator != null) {
				valuesIterator.remove();
				if (valuesForCurrentKey.isEmpty()) {
					keyIterator.remove();
				}

			} else {
				keyIterator.remove();
			}

			expectedModCount = modCount;
			currentEntry = null;

			HashMultiMap.this.size--;
			passedValues--;
		}

		final void checkForComodification() {
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
		}

	}

	static class Entry<K, V> implements Map.Entry<K, V> {
		K key;
		V value;

		public Entry(K key, V value) {
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
			throw new UnsupportedOperationException("Method 'HashMultiMap.Entry.setValue' is not supported!");
		}

		@Override
		public String toString() {
			return "[" + key + ", " + value + "]";
		}
	}

	protected static boolean representsMultipleValues(Object mapped) {
		return mapped instanceof DistinguishableSet;
	}

}
