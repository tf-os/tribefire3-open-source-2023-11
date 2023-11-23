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

import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * This is used as bounds for keys for {@link NavigableMap}and {@link NavigableSet} implementations, which are based on
 * {@link SortedMap} and {@link SortedSet} delegates. In map case, this serves the same purpose as
 * {@link NavigableEntry}, except this only considers they key of the map.
 */
public class KeyBound {

	public static final int BIGGER = 2;
	public static final int BIG = 1;
	public static final int EVEN = 0;
	public static final int SMALL = -1;

	public Object key;
	public int level;

	private KeyBound(Object key, int level) {
		this.key = key;
		this.level = level;
	}

	public static KeyBound upperBound(Object key, boolean inclusive) {
		return new KeyBound(key, inclusive ? BIGGER : SMALL);
	}

	public static KeyBound upBound(Object key, boolean inclusive) {
		return new KeyBound(key, inclusive ? BIG : SMALL);
	}

	public static KeyBound lowerBound(Object key, boolean inclusive) {
		return new KeyBound(key, inclusive ? SMALL : BIG);
	}

	public <T> T cast() {
		return (T) this;
	}

}
