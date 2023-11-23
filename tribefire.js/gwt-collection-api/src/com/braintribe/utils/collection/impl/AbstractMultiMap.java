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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.braintribe.utils.collection.api.MultiMap;

/**
 * 
 */
public abstract class AbstractMultiMap<K, V> implements MultiMap<K, V> {

	/** {@inheritDoc} */
	@Override
	public V put(K key, V value) {
		return put2(key, value) ? null : value;
	}

	/**
	 * copied from {@link java.util.AbstractMap}
	 */
	@Override
	public boolean containsValue(Object value) {
		Iterator<Entry<K, V>> i = entrySet().iterator();
		if (value == null) {
			while (i.hasNext()) {
				Entry<K, V> e = i.next();
				if (e.getValue() == null)
					return true;
			}
		} else {
			while (i.hasNext()) {
				Entry<K, V> e = i.next();
				if (value.equals(e.getValue()))
					return true;
			}
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public V remove(Object key) {
		throw new UnsupportedOperationException(
				"This is in fact a multi-map. For removing items use either remove(key, value) or removeAll(key).");
	}

	transient volatile Collection<V> values = null;

	/** {@inheritDoc} */
	@Override
	public Collection<V> values() {
		if (values == null) {
			values = new AbstractCollection<V>() {
				@Override
				public Iterator<V> iterator() {
					return new Iterator<V>() {
						private final Iterator<Entry<K, V>> i = entrySet().iterator();

						@Override
						public boolean hasNext() {
							return i.hasNext();
						}

						@Override
						public V next() {
							return i.next().getValue();
						}

						@Override
						public void remove() {
							i.remove();
						}
					};
				}

				@Override
				public int size() {
					return AbstractMultiMap.this.size();
				}
				
				@Override
				public void clear() {
					AbstractMultiMap.this.clear();
				}

				@Override
				public boolean contains(Object v) {
					return AbstractMultiMap.this.containsValue(v);
				}
			};
		}
		return values;
	}

	/** {@inheritDoc} */
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		putAll(m.entrySet());
	}

	/** {@inheritDoc} */
	@Override
	public void putAll(K key, Collection<? extends V> values) {
		for (V value: values) {
			put2(key, value);
		}
	}

	private void putAll(Set<? extends Map.Entry<? extends K, ? extends V>> entrySet) {
		for (Map.Entry<? extends K, ? extends V> e: entrySet)
			put2(e.getKey(), e.getValue());
	}

}
