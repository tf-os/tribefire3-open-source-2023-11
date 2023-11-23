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

import static com.braintribe.utils.collection.impl.NavigableEntry.BIG;
import static com.braintribe.utils.collection.impl.NavigableEntry.EVEN;
import static com.braintribe.utils.collection.impl.NavigableEntry.SMALL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.SortedSet;

import com.braintribe.utils.collection.api.NavigableMultiMap;

/**
 * 
 */
public abstract class AbstractNavigableMultiMap<K, V> extends AbstractMultiMap<K, V> implements NavigableMultiMap<K, V> {

	// used for bounds of subMaps (LOW means a bound for "from", HIGH means a bound for "to")
	private static final boolean HIGHEST = true;
	private static final boolean LOWEST = !HIGHEST;

	protected NavigableSet<NavigableEntry<K, V>> set;
	protected boolean ascending;

	protected abstract int compareEntries(NavigableEntry<K, V> o1, NavigableEntry<K, V> o2);

	@Override
	public boolean put2(K key, V value) {
		return set.add(new NavigableEntry<K, V>(key, value));
	}

	@Override
	public V get(Object key) {
		return getLowest((K) key);
	}

	@Override
	public final V getLowest(K key) {
		return valueOrNull(getLowestEntry(key));
	}

	@Override
	public final V getHighest(K key) {
		return valueOrNull(getHighestEntry(key));
	}

	@Override
	public List<V> getAll(K key) {
		NavigableEntry<K, V> lowestEntry, highestEntry;
		if ((lowestEntry = getLowestEntry(key)) == null || (highestEntry = getHighestEntry(key)) == null) {
			return new ArrayList<V>();
		}
		// TODO PERFORMACE TESTS

		/* Not that this is equivalent to subMap(key, true, key,true).values(), but should be faster -> therefore the commented equivalent.
		 * Performance tests should be done to wisely chose better option. */
		// Collection<V> values = subMap(key, true, key, true).values();
		Collection<V> values = subMap(key, lowestEntry.value, true, key, highestEntry.value, true).values();
		return new ArrayList<V>(values);
	}

	@Override
	public boolean containsKeyValue(Object key, Object value) {
		NavigableEntry<K, V> tmpEntry = new NavigableEntry<K, V>((K) key, (V) value);
		return set.contains(tmpEntry);
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public int size() {
		return set.size();
	}

	@Override
	public void clear() {
		set.clear();
	}

	@Override
	public boolean removeAll(Object key) {
		NavigableMultiMap<K, V> subMap = subMap((K) key);
		boolean result = !subMap.isEmpty();
		subMap.clear();
		return result;
	}

	@Override
	public List<V> removeAll2(Object key) {
		NavigableMultiMap<K, V> subMap = subMap((K) key);
		List<V> result = new ArrayList<>(subMap.values());
		subMap.clear();
		return result;
	}

	protected NavigableEntry<K, V> getLowestEntry(K key) {
		NavigableEntry<K, V> entry = getFirst(set.tailSet(toBorderEntry(key, LOWEST)));
		return entry != null && compareKeys(key, entry.key) == 0 ? entry : null;
	}

	protected NavigableEntry<K, V> getHighestEntry(K key) {
		NavigableEntry<K, V> entry = getLast(set.headSet(toBorderEntry(key, HIGHEST)));
		return entry != null && compareKeys(key, entry.key) == 0 ? entry : null;
	}

	@Override
	public NavigableMultiMap<K, V> subMap(K key) {
		return subMap(key, true, key, true);
	}

	@Override
	public Entry<K, V> lowerEntry(K key, V value) {
		return exportEntry(getLast(set.headSet(toEntry(key, EVEN, value, SMALL))));
	}

	@Override
	public Entry<K, V> lowerEntry(K key) {
		return exportEntry(getLast(set.headSet(toBorderEntry(key, LOWEST))));
	}

	@Override
	public K lowerKey(K key) {
		return lastKey(set.headSet(toBorderEntry(key, LOWEST)));
	}

	@Override
	public Entry<K, V> floorEntry(K key, V value) {
		return exportEntry(getLast(set.headSet(toEntry(key, EVEN, value, EVEN))));

	}

	@Override
	public Entry<K, V> floorEntry(K key) {
		return exportEntry(getLast(set.headSet(toBorderEntry(key, HIGHEST))));
	}

	@Override
	public K floorKey(K key) {
		return lastKey(set.headSet(toBorderEntry(key, HIGHEST)));
	}

	@Override
	public Entry<K, V> ceilingEntry(K key, V value) {
		return exportEntry(getFirst(set.tailSet(toEntry(key, EVEN, value, SMALL))));
	}

	@Override
	public Entry<K, V> ceilingEntry(K key) {
		return exportEntry(getFirst(set.tailSet(toBorderEntry(key, LOWEST), false)));
	}

	@Override
	public K ceilingKey(K key) {
		return firstKey(set.tailSet(toBorderEntry(key, LOWEST), false));
	}

	@Override
	public Entry<K, V> higherEntry(K key, V value) {
		return exportEntry(getFirst(set.tailSet(toEntry(key, EVEN, value, EVEN))));
	}

	@Override
	public Entry<K, V> higherEntry(K key) {
		return exportEntry(getFirst(set.tailSet(toBorderEntry(key, HIGHEST), false)));
	}

	@Override
	public K higherKey(K key) {
		return firstKey(set.tailSet(toBorderEntry(key, HIGHEST), false));
	}

	@Override
	public Entry<K, V> firstEntry() {
		return exportEntry(set.first());
	}

	@Override
	public K firstKey() {
		return key(set.first());
	}

	@Override
	public Entry<K, V> lastEntry() {
		return exportEntry(set.last());
	}

	@Override
	public K lastKey() {
		return key(set.last());
	}

	@Override
	public Entry<K, V> pollFirstEntry() {
		if (set.isEmpty())
			return null;

		NavigableEntry<K, V> first = set.first();
		set.remove(first);
		return first;
	}

	@Override
	public Entry<K, V> pollLastEntry() {
		if (set.isEmpty())
			return null;

		NavigableEntry<K, V> last = set.last();
		set.remove(last);
		return last;
	}

	static <K, V> NavigableEntry<K, V> getFirst(SortedSet<? extends NavigableEntry<K, V>> set) {
		return set.isEmpty() ? null : set.first();
	}

	static <K, V> NavigableEntry<K, V> getLast(SortedSet<? extends NavigableEntry<K, V>> set) {
		return set.isEmpty() ? null : set.last();
	}

	static <K, V> Map.Entry<K, V> exportEntry(NavigableEntry<K, V> e) {
		return e == null ? null : new SimpleImmutableEntry<K, V>(e);
	}

	static <K> K firstKey(SortedSet<? extends NavigableEntry<K, ?>> set) {
		return set.isEmpty() ? null : set.first().key;
	}

	static <K> K lastKey(SortedSet<? extends NavigableEntry<K, ?>> set) {
		return set.isEmpty() ? null : set.last().key;
	}

	static <K> K keyOrNull(NavigableEntry<K, ?> e) {
		return e == null ? null : e.key;
	}

	static <K> K key(NavigableEntry<K, ?> e) {
		if (e == null)
			throw new NoSuchElementException();
		return e.key;
	}

	static <K, V> V valueOrNull(NavigableEntry<K, V> e) {
		return e == null ? null : e.value;
	}

	protected Iterator<K> keyIterator() {
		return new KeyIterator(set.iterator());
	}

	protected Iterator<K> descendingKeyIterator() {
		// return new KeyIterator(set.descendingIterator());
		throw new UnsupportedOperationException("Descending key iterator is not supported!");
	}

	/**
	 * Base class for TreeMap Iterators
	 */
	abstract class PrivateEntryIterator<T> implements Iterator<T> {
		protected Iterator<NavigableEntry<K, V>> setIterator;

		PrivateEntryIterator(Iterator<NavigableEntry<K, V>> setIterator) {
			this.setIterator = setIterator;
		}

		@Override
		public boolean hasNext() {
			return setIterator.hasNext();
		}

		public NavigableEntry<K, V> nextEntry() {
			return setIterator.next();
		}

		@Override
		public void remove() {
			setIterator.remove();
		}

	}

	final class KeyIterator implements Iterator<K> {
		protected Iterator<NavigableEntry<K, V>> setIterator;
		NavigableEntry<K, V> nextEntry;

		KeyIterator(Iterator<NavigableEntry<K, V>> setIterator) {
			this.setIterator = setIterator;
			nextEntry = setIterator.hasNext() ? setIterator.next() : null;
		}

		@Override
		public final boolean hasNext() {
			return nextEntry != null;
		}

		@Override
		public K next() {
			K result = nextEntry.key;
			nextEntry = null;
			while (setIterator.hasNext()) {
				NavigableEntry<K, V> ne = setIterator.next();
				if (compareKeys(result, ne.key) != 0) {
					nextEntry = ne;
					break;
				}
			}
			return result;
		}

		@Override
		public void remove() {
			// maybe implement, if desired
			throw new UnsupportedOperationException("Operation 'delete' is not supported for key iterator of MultiMap.");
		}
	}

	protected abstract int compareKeys(K key1, K key2);

	@Override
	public NavigableMultiMap<K, V> subMap(K fromKey, K toKey) {
		return subMap(fromKey, true, toKey, false);
	}

	@Override
	public NavigableMultiMap<K, V> headMap(K toKey) {
		return headMap(toKey, false);
	}

	@Override
	public NavigableMultiMap<K, V> tailMap(K fromKey) {
		return tailMap(fromKey, true);
	}

	@Override
	public Comparator<? super K> comparator() {
		return keyComparator();
	}

	protected NavigableEntry<K, V> toBorderEntryByInclusion(K key, boolean upperBound, boolean inclusive) {
		return toBorderEntry(key, upperBound == inclusive);
	}

	protected NavigableEntry<K, V> toBorderEntry(K key, V value, boolean upperBound, boolean inclusive) {
		return toEntry(key, EVEN, value, valueLevel(upperBound, inclusive));
	}

	protected NavigableEntry<K, V> toBorderEntry(K key, boolean highest) {
		return highest == ascending ? toEntry(key, BIG, null, EVEN) : toEntry(key, SMALL, null, EVEN);
	}

	private byte valueLevel(boolean upperBound, boolean inclusive) {
		if (upperBound) {
			return inclusive ? BIG : EVEN;
		} else {
			return inclusive ? SMALL : EVEN;
		}
	}

	protected NavigableEntry<K, V> toEntry(K key, byte keyLevel, V value, byte valueLevel) {
		return new NavigableEntry<K, V>(key, keyLevel, value, valueLevel);
	}

	protected abstract AbstractNavigableMultiMap<K, V> originalMultiMap();
}
