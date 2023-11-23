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

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;

import com.braintribe.utils.collection.api.NavigableMultiMap;

/**
 * 
 */
public abstract class NavigableMultiMapBase<K, V> extends AbstractNavigableMultiMap<K, V> {

	// used for bounds of subMaps (LOW means a bound for "from", HIGH means a bound for "to")
	private static final boolean HIGH = true;
	private static final boolean LOW = !HIGH;

	@Override
	public boolean containsKey(Object key) {
		return getLowestEntry((K) key) != null;
	}

	@Override
	public boolean remove(Object key, Object value) {
		return set.remove(new NavigableEntry<K, V>((K) key, (V) value));
	}

	private transient EntrySet<K, V> entrySet = null;
	private transient KeySet<K> navigableKeySet = null;
	private transient NavigableMultiMap<K, V> descendingMultiMap = null;

	@Override
	public NavigableSet<K> navigableKeySet() {
		return keySet();
	}

	@Override
	public NavigableSet<K> keySet() {
		KeySet<K> nks = navigableKeySet;
		return (nks != null) ? nks : (navigableKeySet = new KeySet<K>(this));
	}

	@Override
	public NavigableSet<K> descendingKeySet() {
		return descendingMap().keySet();
	}

	@Override
	public Collection<V> values() {
		Collection<V> vs = values;
		return (vs != null) ? vs : (values = new Values());
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		EntrySet<K, V> es = entrySet;
		return (es != null) ? es : (entrySet = new EntrySet<K, V>(set));
	}

	class Values extends AbstractCollection<V> {
		@Override
		public Iterator<V> iterator() {
			return new ValueIterator(set.iterator());
		}

		@Override
		public int size() {
			return NavigableMultiMapBase.this.size();
		}

		@Override
		public boolean contains(Object o) {
			return NavigableMultiMapBase.this.containsValue(o);
		}

		@Override
		public boolean remove(Object o) {
			for (Iterator<V> it = iterator(); it.hasNext();) {
				if (valEquals(it.next(), o)) {
					it.remove();
					return true;
				}
			}
			return false;
		}

		@Override
		public void clear() {
			NavigableMultiMapBase.this.clear();
		}
	}

	/**
	 * This ugly code was copied from java.util.TreeMap and adjusted a bit.
	 */
	static class EntrySet<K, V> extends AbstractSet<Map.Entry<K, V>> {

		private final SortedSet<NavigableEntry<K, V>> set;

		public EntrySet(SortedSet<NavigableEntry<K, V>> set) {
			super();
			this.set = set;
		}

		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			// we have an Iterator<NavigableEntry<K, V>> - so it is actually compatible
			return (Iterator<Map.Entry<K, V>>) (Iterator<?>) set.iterator();
		}

		@Override
		public boolean contains(Object o) {
			return (o instanceof Map.Entry) ? set.contains(o) : false;
		}

		@Override
		public boolean remove(Object o) {
			return (o instanceof Map.Entry) ? set.remove(o) : false;
		}

		@Override
		public int size() {
			return set.size();
		}

		@Override
		public void clear() {
			set.clear();
		}
	}

	static final class KeySet<E> extends AbstractSet<E> implements NavigableSet<E> {
		private final AbstractNavigableMultiMap<E, ?> m;

		KeySet(AbstractNavigableMultiMap<E, ?> map) {
			m = map;
		}

		@Override
		public Iterator<E> iterator() {
			return m.keyIterator();
		}

		@Override
		public Iterator<E> descendingIterator() {
			return m.descendingKeyIterator();
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
		public boolean contains(Object o) {
			return m.containsKey(o);
		}

		@Override
		public void clear() {
			m.clear();
		}

		@Override
		public E lower(E e) {
			return m.lowerKey(e);
		}

		@Override
		public E floor(E e) {
			return m.floorKey(e);
		}

		@Override
		public E ceiling(E e) {
			return m.ceilingKey(e);
		}

		@Override
		public E higher(E e) {
			return m.higherKey(e);
		}

		@Override
		public E first() {
			return m.firstKey();
		}

		@Override
		public E last() {
			return m.lastKey();
		}

		@Override
		public Comparator<? super E> comparator() {
			return m.keyComparator();
		}

		@Override
		public E pollFirst() {
			Map.Entry<E, ?> e = m.pollFirstEntry();
			return e == null ? null : e.getKey();
		}

		@Override
		public E pollLast() {
			Map.Entry<E, ?> e = m.pollLastEntry();
			return e == null ? null : e.getKey();
		}

		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException("Method 'remove' is not supported for KeySet!");
		}

		@Override
		public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
			return new MultiMapBackedSet<E>(m.subMap(fromElement, fromInclusive, toElement, toInclusive));
		}

		@Override
		public NavigableSet<E> headSet(E toElement, boolean inclusive) {
			return new MultiMapBackedSet<E>(m.headMap(toElement, inclusive));
		}

		@Override
		public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
			return new MultiMapBackedSet<E>(m.tailMap(fromElement, inclusive));
		}

		@Override
		public NavigableSet<E> subSet(E fromElement, E toElement) {
			return subSet(fromElement, true, toElement, false);
		}

		@Override
		public NavigableSet<E> headSet(E toElement) {
			return headSet(toElement, false);
		}

		@Override
		public NavigableSet<E> tailSet(E fromElement) {
			return tailSet(fromElement, true);
		}

		@Override
		public NavigableSet<E> descendingSet() {
			return new MultiMapBackedSet<E>(m.descendingMap());
		}

	}

	final class ValueIterator extends PrivateEntryIterator<V> {
		ValueIterator(Iterator<NavigableEntry<K, V>> setIterator) {
			super(setIterator);
		}

		@Override
		public V next() {
			return nextEntry().value;
		}
	}

	@Override
	public NavigableMultiMap<K, V> descendingMap() {
		NavigableMultiMap<K, V> mv = descendingMultiMap;
		return (mv != null) ? mv : (descendingMultiMap = new NavigableSubMultiMap(this, true, true, null, true, true, null, true));
	}

	@Override
	public NavigableMultiMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
		NavigableEntry<K, V> fromEntry = toBorderEntryByInclusion(fromKey, LOW, fromInclusive);
		NavigableEntry<K, V> toEntry = toBorderEntryByInclusion(toKey, HIGH, toInclusive);
		return new NavigableSubMultiMap(this, false, false, fromEntry, fromInclusive, false, toEntry, toInclusive);
	}

	@Override
	public NavigableMultiMap<K, V> subMap(K fromKey, V fromValue, boolean fromInclusive, K toKey, V toValue, boolean toInclusive) {
		NavigableEntry<K, V> fromEntry = toBorderEntry(fromKey, fromValue, LOW, fromInclusive);
		NavigableEntry<K, V> toEntry = toBorderEntry(toKey, toValue, HIGH, toInclusive);
		return new NavigableSubMultiMap(this, false, false, fromEntry, fromInclusive, false, toEntry, toInclusive);
	}

	@Override
	public NavigableMultiMap<K, V> headMap(K toKey, boolean inclusive) {
		NavigableEntry<K, V> toEntry = toBorderEntryByInclusion(toKey, HIGH, inclusive);
		return new NavigableSubMultiMap(this, false, true, null, true, false, toEntry, inclusive);
	}

	@Override
	public NavigableMultiMap<K, V> headMap(K toKey, V toValue, boolean inclusive) {
		NavigableEntry<K, V> toEntry = toBorderEntry(toKey, toValue, HIGH, inclusive);
		return new NavigableSubMultiMap(this, false, true, null, true, false, toEntry, inclusive);
	}

	@Override
	public NavigableMultiMap<K, V> tailMap(K fromKey) {
		return tailMap(fromKey, true);
	}

	@Override
	public NavigableMultiMap<K, V> tailMap(K fromKey, boolean inclusive) {
		NavigableEntry<K, V> fromEntry = toBorderEntryByInclusion(fromKey, LOW, inclusive);
		return new NavigableSubMultiMap(this, false, false, fromEntry, inclusive, true, null, true);
	}

	@Override
	public NavigableMultiMap<K, V> tailMap(K fromKey, V fromValue, boolean inclusive) {
		NavigableEntry<K, V> fromEntry = toBorderEntry(fromKey, fromValue, LOW, inclusive);
		return new NavigableSubMultiMap(this, false, false, fromEntry, inclusive, true, null, true);
	}

	final static boolean valEquals(Object o1, Object o2) {
		return (o1 == null ? o2 == null : o1.equals(o2));
	}

	@Override
	protected AbstractNavigableMultiMap<K, V> originalMultiMap() {
		return this;
	}

	class NavigableSubMultiMap extends AbstractNavigableMultiMap<K, V> {

		private final NavigableMultiMapBase<K, V> origMap;
		private final Comparator<? super K> keyComparator;

		final NavigableEntry<K, V> loEntry, hiEntry;
		final boolean fromStart, toEnd;
		final boolean loInclusive, hiInclusive;

		public NavigableSubMultiMap(AbstractNavigableMultiMap<K, V> superMap, boolean reverse, boolean fromStart, NavigableEntry<K, V> loEntry,
				boolean loIncl, boolean toEnd, NavigableEntry<K, V> hiEntry, boolean hiIncl) {

			if (reverse)
				this.set = superMap.set.descendingSet();

			else if (!fromStart && !toEnd)
				this.set = superMap.set.subSet(loEntry, loIncl, hiEntry.higherBorder(), hiIncl);

			else if (fromStart)
				// TODO do we need the higherBorder here?
				this.set = superMap.set.headSet(hiEntry.higherBorder(), hiIncl);

			else
				this.set = superMap.set.tailSet(loEntry, loIncl);

			this.loEntry = loEntry;
			this.hiEntry = hiEntry;
			this.fromStart = fromStart;
			this.toEnd = toEnd;
			this.loInclusive = loIncl;
			this.hiInclusive = hiIncl;

			this.ascending = reverse != superMap.ascending;
			this.origMap = (NavigableMultiMapBase<K, V>) superMap.originalMultiMap();
			this.keyComparator = ascending ? origMap.keyComparator() : origMap.keyComparator().reversed();
		}

		@Override
		protected int compareKeys(K key1, K key2) {
			return keyComparator.compare(key1, key2);
		}

		@Override
		protected int compareEntries(NavigableEntry<K, V> o1, NavigableEntry<K, V> o2) {
			int absComparison = NavigableMultiMapBase.this.compareEntries(o1, o2);
			return ascending ? absComparison : -absComparison;
		}

		@Override
		public boolean containsKey(Object key) {
			return mayContainKey((K) key) ? origMap.containsKey(key) : false;
		}

		@Override
		public boolean remove(Object key, Object value) {
			return containsKeyValue(key, value) ? origMap.remove(key, value) : false;
		}

		// TODO must be overridden?
		@Override
		protected NavigableEntry<K, V> getLowestEntry(K key) {
			if (set.isEmpty())
				return null;

			NavigableEntry<K, V> first = set.first();
			int cmp = keyComparator.compare(key, first.key);
			if (cmp < 0)
				return null;
			if (cmp == 0)
				return first;

			NavigableEntry<K, V> last = set.last();
			if (keyComparator.compare(key, last.key) > 0)
				return null;

			return ascending ? origMap.getLowestEntry(key) : origMap.getHighestEntry(key);
		}

		// TODO must be overridden?
		@Override
		protected NavigableEntry<K, V> getHighestEntry(K key) {
			if (set.isEmpty())
				return null;

			NavigableEntry<K, V> last = set.last();
			int cmp = keyComparator.compare(key, last.key);
			if (cmp > 0)
				return null;
			if (cmp == 0)
				return last;

			NavigableEntry<K, V> first = set.first();
			if (keyComparator.compare(key, first.key) < 0)
				return null;

			return ascending ? origMap.getHighestEntry(key) : origMap.getLowestEntry(key);
		}

		private final boolean mayContainKey(K key) {
			NavigableEntry<K, V> first = set.first();
			if (keyComparator.compare(key, first.key) < 0)
				return false;

			NavigableEntry<K, V> last = set.last();
			return keyComparator.compare(key, last.key) <= 0;
		}

		transient NavigableMultiMap<K, V> descendingMultiMapView = null;
		transient EntrySet<K, V> entrySetView = null;
		transient KeySet<K> navigableKeySetView = null;

		@Override
		public Comparator<? super K> keyComparator() {
			return keyComparator;
		}

		@Override
		public NavigableSet<K> navigableKeySet() {
			return keySet();
		}

		@Override
		public NavigableSet<K> keySet() {
			KeySet<K> nks = navigableKeySetView;
			return (nks != null) ? nks : (navigableKeySetView = new KeySet<K>(this));
		}

		@Override
		public NavigableSet<K> descendingKeySet() {
			return descendingMap().navigableKeySet();
		}

		@Override
		public Set<Entry<K, V>> entrySet() {
			EntrySet<K, V> es = entrySetView;
			return (es != null) ? es : (entrySetView = new EntrySet<K, V>(set));
		}

		// @Override
		@Override
		public NavigableMultiMap<K, V> descendingMap() {
			NavigableMultiMap<K, V> mv = descendingMultiMapView;
			return (mv != null) ? mv
					: (descendingMultiMapView = new NavigableSubMultiMap(this, true, toEnd, hiEntry, hiInclusive, fromStart, loEntry, loInclusive));
		}

		@Override
		public NavigableMultiMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
			BorderEntry<K, V> fbe = getRightBorderEntry(fromKey, LOW, fromInclusive);
			BorderEntry<K, V> tbe = getRightBorderEntry(toKey, HIGH, toInclusive);
			fromInclusive = fbe.usingNew ? fromInclusive : this.loInclusive;
			toInclusive = tbe.usingNew ? toInclusive : this.hiInclusive;
			// return new NavigableSubMultiMap<K, V>(origMap, set.subSet(fromEntry, fromInclusive, toEntry,
			// toInclusive), ascending);
			return new NavigableSubMultiMap(this, false, false, fbe.entry, fromInclusive, false, tbe.entry, toInclusive);
		}

		@Override
		public NavigableMultiMap<K, V> subMap(K fromKey, V fromValue, boolean fromInclusive, K toKey, V toValue, boolean toInclusive) {
			NavigableEntry<K, V> fromEntry = toBorderEntry(fromKey, fromValue, LOW, fromInclusive);
			NavigableEntry<K, V> toEntry = toBorderEntry(toKey, toValue, HIGH, toInclusive);
			return new NavigableSubMultiMap(this, false, false, fromEntry, fromInclusive, false, toEntry, toInclusive);
		}

		@Override
		public NavigableMultiMap<K, V> headMap(K toKey, boolean inclusive) {
			BorderEntry<K, V> tbe = getRightBorderEntry(toKey, HIGH, inclusive);
			inclusive = tbe.usingNew ? inclusive : this.hiInclusive;
			return new NavigableSubMultiMap(this, false, fromStart, loEntry, loInclusive, false, tbe.entry, inclusive);
		}

		@Override
		public NavigableMultiMap<K, V> headMap(K toKey, V toValue, boolean inclusive) {
			NavigableEntry<K, V> toEntry = toBorderEntry(toKey, toValue, HIGH, inclusive);
			return new NavigableSubMultiMap(this, false, true, null, false, false, toEntry, inclusive);
		}

		@Override
		public NavigableMultiMap<K, V> tailMap(K fromKey, boolean inclusive) {
			BorderEntry<K, V> fbe = getRightBorderEntry(fromKey, LOW, inclusive);
			inclusive = fbe.usingNew ? inclusive : this.loInclusive;
			return new NavigableSubMultiMap(this, false, false, fbe.entry, inclusive, true, null, true);
		}

		@Override
		public NavigableMultiMap<K, V> tailMap(K fromKey, V fromValue, boolean inclusive) {
			NavigableEntry<K, V> fromEntry = toBorderEntry(fromKey, fromValue, LOW, inclusive);
			return new NavigableSubMultiMap(this, false, false, fromEntry, inclusive, true, null, true);
		}

		/**
		 * Very important code that makes sure subMaps work properly when creating subMap with border having same key as current border. In this case
		 * we must specify the border entry carefully.
		 */
		private BorderEntry<K, V> getRightBorderEntry(K key, boolean upperBound, boolean inclusive) {
			NavigableEntry<K, V> borderEntry = toBorderEntryByInclusion(key, upperBound, inclusive);

			NavigableEntry<K, V> currentBorder = upperBound ? hiEntry : loEntry;

			if (currentBorder == null || keyComparator.compare(borderEntry.key, currentBorder.key) != 0)
				return new BorderEntry<K, V>(borderEntry, true);

			return ascending == upperBound ? absMin(borderEntry, currentBorder) : absMax(borderEntry, currentBorder);
		}

		private BorderEntry<K, V> absMin(NavigableEntry<K, V> newE, NavigableEntry<K, V> oldE) {
			boolean shouldUseNew = origMap.compareEntries(newE, oldE) < 0;
			return shouldUseNew ? new BorderEntry<K, V>(newE, true) : new BorderEntry<K, V>(oldE, false);
		}

		private BorderEntry<K, V> absMax(NavigableEntry<K, V> newE, NavigableEntry<K, V> oldE) {
			boolean shouldUseNew = origMap.compareEntries(newE, oldE) > 0;
			return shouldUseNew ? new BorderEntry<K, V>(newE, true) : new BorderEntry<K, V>(oldE, false);
		}

		@Override
		protected NavigableEntry<K, V> toBorderEntry(K key, boolean highest) {
			NavigableEntry<K, V> result = super.toBorderEntry(key, highest);

			if (!toEnd && (compareEntries(result, hiEntry) > 0))
				return hiEntry;

			if (!fromStart && (compareEntries(result, loEntry) < 0))
				return loEntry;

			return result;
		}

		@Override
		protected AbstractNavigableMultiMap<K, V> originalMultiMap() {
			return origMap;
		}

	}

	private static class BorderEntry<K, V> {
		NavigableEntry<K, V> entry;
		boolean usingNew;

		public BorderEntry(NavigableEntry<K, V> entry, boolean usingNew) {
			super();
			this.entry = entry;
			this.usingNew = usingNew;
		}
	}

}
