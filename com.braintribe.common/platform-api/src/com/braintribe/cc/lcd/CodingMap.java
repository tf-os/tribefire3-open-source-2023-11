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
package com.braintribe.cc.lcd;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;

/**
 * a coding map..<br/>
 * <br/>
 * for example:<br/>
 * {@literal Map<B, V> map = new CodingMap<BW, B, V>( new AnyMapYouLike<BW, V>(), new YourWrapperCodec());}<br/>
 * or<br/>
 * {@literal Map<B, V> map = CodingMap.createHashBased(new YourWrapperCodec());}<br/>
 * <br/>
 * <br/>
 * where<br/>
 * B : the thing you want to store, i.e. a Bean<br/>
 * BW : thing that wraps the bean and implements hashCode and equals(object), i.e. defines the identity behavior, a BeanWrapper<br/>
 * AnyMapYouLike : the map implementation you want to have as a delegate<br/>
 * YourWrapperCodec : a codec that translates B,BW. <br/>
 * <br/>
 * if you need any concrete examples, have a look that the test cases in com.braintribe:PlatformApiTest#1.0, package com.braintribe.coding<br/>
 *
 * @author pit
 *
 * @param <WK>
 *            - wrapper key class
 * @param <K>
 *            - key class
 * @param <V>
 *            - value class
 */
public class CodingMap<WK, K, V> implements Map<K, V> {

	private final Map<WK, V> delegate;
	private final Codec<K, WK> codec;

	/**
	 * Creates a {@code Map<K, V>} which is backed by a {@code HashMap<WK, V>}, where the key is transformed using given codec.
	 */
	public static <WK, K, V> CodingMap<WK, K, V> createHashMapBased(Codec<K, WK> codec) {
		return new CodingMap<>(new LinkedHashMap<>(), codec);
	}

	public static <WK, K, V> CodingMap<WK, K, V> createHashMapBased(Codec<K, WK> codec, Supplier<Map<?, ?>> mapFactory) {
		return new CodingMap<>((Map<WK, V>) mapFactory.get(), codec);
	}

	/**
	 * Equivalent to calling {@link #createHashMapBased(Codec)} with a {@link HashingComparatorWrapperCodec} backed by this comparator.
	 */
	public static <K, V> Map<K, V> create(HashingComparator<? super K> comparator) {
		return CodingMap.createHashMapBased(new HashingComparatorWrapperCodec<K>(comparator, comparator.isHashImmutable()));
	}

	public static <K, V> Map<K, V> create(HashingComparator<? super K> comparator, Supplier<Map<?, ?>> mapFactory) {
		return CodingMap.createHashMapBased(new HashingComparatorWrapperCodec<K>(comparator, comparator.isHashImmutable()), mapFactory);
	}

	/** Creates a {@code Map<K, V>} which is backed by given delegate and the key is transformed using given codec. */
	public static <WK, K, V> CodingMap<WK, K, V> create(Map<WK, V> delegate, Codec<K, WK> codec) {
		return new CodingMap<>(delegate, codec);
	}

	public static <K, V> Map<K, V> create(Map<Object, Object> delegate, HashingComparator<? super K> comparator) {
		return create(delegate, comparator, comparator.isHashImmutable());
	}

	/**
	 * Equivalent to calling {@link #createHashMapBased(Codec)} with a {@link HashingComparatorWrapperCodec} backed by this comparator.
	 */
	@SuppressWarnings("rawtypes")
	public static <K, V> Map<K, V> create(Map<Object, Object> delegate, HashingComparator<? super K> comparator, boolean entitiesAreImmutable) {
		return CodingMap.create((Map) delegate, new HashingComparatorWrapperCodec<K>(comparator, entitiesAreImmutable));
	}

	public CodingMap(Map<WK, V> delegate, Codec<K, WK> codec) {
		this.delegate = delegate;
		this.codec = codec;
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return delegate.containsKey(wrap((K) key));
	}

	@Override
	public boolean containsValue(Object value) {
		return delegate.containsValue(value);
	}

	@Override
	public V get(Object key) {
		return delegate.get(wrap((K) key));
	}

	@Override
	public V put(K key, V value) {
		return delegate.put(wrap(key), value);
	}

	@Override
	public V remove(Object key) {
		return delegate.remove(wrap((K) key));
	}

	@Override
	public V getOrDefault(Object key, V defaultValue) {
		return delegate.getOrDefault(wrap((K) key), defaultValue);
	}

	@Override
	public void forEach(BiConsumer<? super K, ? super V> action) {
		delegate.forEach((wk, v) -> action.accept(unwrap(wk), v));
	}

	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		delegate.replaceAll((wk, v) -> function.apply(unwrap(wk), v));
	}

	@Override
	public V putIfAbsent(K key, V value) {
		return delegate.putIfAbsent(wrap(key), value);
	}

	@Override
	public boolean remove(Object key, Object value) {
		return delegate.remove(wrap((K) key), value);
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		return delegate.replace(wrap(key), oldValue, newValue);
	}

	@Override
	public V replace(K key, V value) {
		return delegate.replace(wrap(key), value);
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		return delegate.computeIfAbsent(wrap(key), wk -> mappingFunction.apply(key));
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return delegate.computeIfPresent(wrap(key), (wk, v) -> remappingFunction.apply(key, v));
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return delegate.compute(wrap(key), (wk, v) -> remappingFunction.apply(key, v));
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		return delegate.merge(wrap(key), value, remappingFunction);
	}

	private WK wrap(K key) {
		try {
			return codec.encode(key);

		} catch (CodecException e) {
			throw new IllegalStateException("Cannot encode key:" + key, e);
		}
	}

	private K unwrap(WK wk) {
		try {
			return codec.decode(wk);

		} catch (CodecException e) {
			throw new IllegalStateException("Cannot decode encoded key:" + wk, e);
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void clear() {
		delegate.clear();

	}

	@Override
	public Set<K> keySet() {
		return new CodingSet<>(delegate.keySet(), codec);
	}

	@Override
	public Collection<V> values() {
		return delegate.values();
	}

	/**
	 * an internal helper class to enable the Map.Entry mapping
	 *
	 * @author pit
	 *
	 */
	private class CodingMapEntry implements Map.Entry<K, V> {

		@SuppressWarnings("hiding")
		private final Map.Entry<WK, V> delegate;

		public CodingMapEntry(final Map.Entry<WK, V> entry) {
			delegate = entry;
		}

		@Override
		public K getKey() {
			return CodingMap.this.unwrap(delegate.getKey());
		}

		@Override
		public V getValue() {
			return delegate.getValue();
		}

		@Override
		public V setValue(final V value) {
			return delegate.setValue(value);
		}

		@Override
		public final String toString() {
			return getKey() + "=" + getValue();
		}

	}

	/** Internal codec for the Map.Entry elements. */
	private class EntryCodec implements Codec<Map.Entry<K, V>, Map.Entry<WK, V>> {

		@Override
		public Map.Entry<WK, V> encode(Map.Entry<K, V> value) {
			return value != null ? ((CodingMapEntry) value).delegate : null;
		}

		@Override
		public Map.Entry<K, V> decode(Map.Entry<WK, V> encodedValue) {
			return encodedValue != null ? new CodingMapEntry(encodedValue) : null;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public Class<java.util.Map.Entry<K, V>> getValueClass() {
			return (Class) Map.Entry.class;
		}

	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return new CodingSet<>(delegate.entrySet(), new EntryCodec());
	}

	/** Copied from {@link AbstractMap} */
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (!(o instanceof Map)) {
			return false;
		}
		Map<K, V> m = (Map<K, V>) o;
		if (m.size() != size()) {
			return false;
		}

		try {
			Iterator<Entry<K, V>> i = entrySet().iterator();
			while (i.hasNext()) {
				Entry<K, V> e = i.next();
				K key = e.getKey();
				V value = e.getValue();
				if (value == null) {
					if (!(m.get(key) == null && m.containsKey(key))) {
						return false;
					}
				} else {
					if (!value.equals(m.get(key))) {
						return false;
					}
				}
			}
		} catch (ClassCastException unused) {
			return false;
		} catch (NullPointerException unused) {
			return false;
		}

		return true;
	}

	/** Copied from {@link AbstractMap} */
	@Override
	public int hashCode() {
		int h = 0;
		Iterator<Entry<K, V>> i = entrySet().iterator();
		while (i.hasNext()) {
			h += i.next().hashCode();
		}
		return h;
	}

	@Override
	public String toString() {
		if (isEmpty()) {
			return "{}";
		}

		Iterator<Entry<K, V>> i = entrySet().iterator();
		StringBuilder sb = new StringBuilder();

		sb.append('{');
		while (true) {
			Entry<K, V> e = i.next();

			sb.append(e.getKey());
			sb.append('=');
			sb.append(e.getValue());
			if (!i.hasNext()) {
				return sb.append('}').toString();
			}
			sb.append(',').append(' ');
		}
	}
}
