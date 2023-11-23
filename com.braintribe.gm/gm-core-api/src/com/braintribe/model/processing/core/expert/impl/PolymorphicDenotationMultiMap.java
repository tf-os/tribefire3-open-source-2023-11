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
package com.braintribe.model.processing.core.expert.impl;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.core.expert.api.MutableDenotationMultiMap;

/**
 * Implementation of {@link MutableDenotationMultiMap} where values are inherited alongside the type hierarchy. See {@link #findAll(EntityType)}
 * 
 * @author peter.gazdik
 */
@SuppressWarnings("unusable-by-js")
public class PolymorphicDenotationMultiMap<B extends GenericEntity, V> implements MutableDenotationMultiMap<B, V> {

	private final Map<EntityType<? extends B>, Set<V>> configuration;
	private final Map<EntityType<? extends B>, Object> cache;

	private int size = 0;

	/** Equivalent to {@code new PolymorphicDenotationMultiMap(true)} */
	public PolymorphicDenotationMultiMap() {
		this(true);
	}

	/** Constructor with a parameter to make the map thread-safe. Note the thread-safety is only relevant for reading. */
	public PolymorphicDenotationMultiMap(boolean threadSafe) {
		this.configuration = new IdentityHashMap<>();
		this.cache = threadSafe ? new ConcurrentHashMap<>() : new IdentityHashMap<>();
	}

	@Override
	public void putAll(Map<EntityType<? extends B>, V> map) {
		for (Entry<EntityType<? extends B>, V> entry : map.entrySet())
			put(entry.getKey(), entry.getValue());
	}

	@Override
	public void put(EntityType<? extends B> denotationType, V value) {
		nonNull(denotationType, "denotationType");
		nonNull(value, "value");

		if (configuration.computeIfAbsent(denotationType, k -> new LinkedHashSet<>()).add(value))
			size++;

		// This seems like an overkill, but then intention of the map is to be configured initially and then only read.
		// Can be improved later.
		cache.clear();
	}

	/**
	 * Removes all the values previously "put" for given denotationType. Also, any association created for any sub-type of given type is removed.
	 */
	@Override
	public void remove(EntityType<? extends B> denotationType) {
		if (denotationType == null)
			return;

		Set<V> set = configuration.remove(denotationType);
		if (set != null) {
			size -= set.size();
			cache.clear();
		}
	}

	@Override
	public void removeEntry(EntityType<? extends B> denotationType, V value) {
		if (denotationType == null)
			return;

		Set<V> set = configuration.get(denotationType);
		if (set == null)
			return;

		if (set.remove(value)) {
			size--;
			cache.clear();
		}

		if (set.isEmpty())
			configuration.remove(denotationType);
	}

	// @formatter:off
	@Override public <T extends V> T get(B denotation) { return get(denotation.entityType()); }
	@Override public <T extends V> T find(B denotation) { return find(denotation.entityType()); }
	@Override public <T extends V> List<T> findAll(B denotation) { return findAll(denotation.entityType()); }
	// @formatter:on

	@Override
	public <T extends V> T get(EntityType<? extends B> denotationType) {
		T result = find(denotationType);
		if (result == null)
			throw new NoSuchElementException("No value found for denotation type: " + denotationType.getTypeSignature());

		return result;
	}

	@Override
	public <T extends V> T find(EntityType<? extends B> denotationType) {
		List<V> allValues = findAll(denotationType);
		return allValues.isEmpty() ? null : (T) allValues.get(0);
	}

	/**
	 * Returns all values associated with given {@link EntityType} or any of its super-types.
	 * <p>
	 * The resulting list does not contain duplicates, even if the same value was associated with multiple types in denotationType's hierarchy.
	 * <p>
	 * The order of the returned list is not specified, but it is stable (same result for multiple invocations), as long as the configuration doesn't
	 * change.
	 */
	@Override
	public <T extends V> List<T> findAll(EntityType<? extends B> denotationType) {
		return (List<T>) (List<?>) cache.computeIfAbsent(denotationType, this::computeAll);
	}

	private List<V> computeAll(EntityType<?> denotationType) {
		Set<V> result = new LinkedHashSet<>();

		for (EntityType<?> meOrSuperType : denotationType.getTransitiveSuperTypes(true, true)) {
			Set<V> values = configuration.get(meOrSuperType);
			if (values == null)
				continue;

			result.addAll(values);
		}

		return new ArrayList<>(result);
	}

	@Override
	public Stream<Entry<EntityType<? extends B>, V>> entryStream() {
		return configuration.entrySet().stream() //
				.flatMap(PolymorphicDenotationMultiMap.this::toEntryStream);
	}

	private Stream<Entry<EntityType<? extends B>, V>> toEntryStream(Entry<EntityType<? extends B>, Set<V>> entry) {
		EntityType<? extends B> key = entry.getKey();

		// @formatter:off
		class Entry implements Map.Entry<EntityType<? extends B>, V> {
			private final V value;
			public Entry(V value) { this.value = value; }
			@Override public EntityType<? extends B> getKey() { return key; }
			@Override public V getValue() { return value; }
			@Override public V setValue(V value) { throw new UnsupportedOperationException("Method 'setValue' is not supported!"); }
		}
		// @formatter:on

		return entry.getValue().stream()//
				.map(Entry::new);
	}

	@Override
	public Stream<V> expertStream() {
		return configuration.values().stream() //
				.flatMap(Set::stream);
	}

	@Override
	public boolean isEmpty() {
		return configuration.isEmpty();
	}

	@Override
	public int configurationSize() {
		return size;
	}

	private static <T> T nonNull(T obj, String name) {
		return Objects.requireNonNull(obj, () -> name + " cannot be null");
	}

}
