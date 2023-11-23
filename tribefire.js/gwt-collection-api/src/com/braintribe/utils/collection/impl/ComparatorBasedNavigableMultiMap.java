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

import java.util.Comparator;
import java.util.TreeSet;

/**
 * 
 */
public class ComparatorBasedNavigableMultiMap<K, V> extends NavigableMultiMapBase<K, V> {

	Comparator<K> keyComparator;
	EntryComparator<K, V> entryComparator;

	static final class EntryComparator<K, V> implements Comparator<NavigableEntry<K, V>> {
		private final Comparator<K> keyComparator;
		private final Comparator<V> valueComparator;

		public EntryComparator(Comparator<K> keyComparator, Comparator<V> valueComparator) {
			super();
			this.keyComparator = keyComparator;
			this.valueComparator = valueComparator;
		}

		@Override
		public int compare(NavigableEntry<K, V> o1, NavigableEntry<K, V> o2) {
			return o1.compare(o2, keyComparator, valueComparator);
		}

	}

	public ComparatorBasedNavigableMultiMap(Comparator<? super K> kcmp, Comparator<? super V> vcmp) {
		initialize(kcmp, vcmp);
		this.set = new TreeSet<NavigableEntry<K, V>>(entryComparator);
	}

	private void initialize(Comparator<? super K> kcmp, Comparator<? super V> vcmp) {
		this.keyComparator = new NullHandlingComparator<K>(kcmp);

		Comparator<V> valueComparator = new NullHandlingComparator<V>(vcmp);

		this.entryComparator = new EntryComparator<K, V>(keyComparator, valueComparator);
		this.ascending = true;
	}

	@Override
	protected int compareKeys(K key1, K key2) {
		return keyComparator.compare(key1, key2);
	}

	@Override
	public Comparator<? super K> keyComparator() {
		return keyComparator;
	}

	@Override
	protected int compareEntries(NavigableEntry<K, V> o1, NavigableEntry<K, V> o2) {
		return entryComparator.compare(o1, o2);
	}

}
