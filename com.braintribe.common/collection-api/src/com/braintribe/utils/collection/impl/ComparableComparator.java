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

public class ComparableComparator<T extends Comparable<T>> implements Comparator<T> {

	@SuppressWarnings({ "rawtypes" })
	private static ComparableComparator<?> instance = new ComparableComparator();

	public static <V extends Comparable<V>> ComparableComparator<V> instance() {
		return (ComparableComparator<V>) instance;
	}

	/** Same as {@link #instance()} but better for static imports due to unambiguous name of the method. */
	public static <V extends Comparable<V>> ComparableComparator<V> comparableComparator() {
		return (ComparableComparator<V>) instance;
	}
	
	public static <V> Comparator<V> unboundedInstance() {
		return (Comparator<V>) instance;
	}

	@Override
	public int compare(T o1, T o2) {
		return o1.compareTo(o2);
	}
}
