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

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Extension of standard {@link Comparator} interface which also features a hash-code computing method.
 *
 * @see HashingComparatorWrapperCodec
 * @see CodingMap#create(HashingComparator)
 */
public interface HashingComparator<E> {

	boolean compare(E e1, E e2);

	int computeHash(E e);

	default boolean isHashImmutable() {
		return true;
	}

	default Set<E> newHashSet() {
		return CodingSet.create(this);
	}

	default Set<E> newHashSet(Supplier<Set<?>> backupFactory) {
		return CodingSet.create(this, backupFactory);
	}

	default Set<E> newLinkedHashSet() {
		return newHashSet(LinkedHashSet::new);
	}

	default <V> Map<E, V> newHashMap() {
		return CodingMap.create(this);
	}

	default <V> Map<E, V> newHashMap(Supplier<Map<?, ?>> backupFactory) {
		return CodingMap.create(this, backupFactory);
	}

	default <V> Map<E, V> newLinkedHashMap() {
		return newHashMap(LinkedHashMap::new);
	}

	default <V> Map<E, V> newConcurrentHashMap() {
		return newHashMap(ConcurrentHashMap::new);
	}

	default EqProxy<E> eqProxy(E subject) {
		return isHashImmutable() ? new HashComparatorImmutableEqProxy<>(this, subject) : //
				new HashComparatorEqProxy<>(this, subject);
	}
}
