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

public class CollectionTestTools {
	private static final int BIG_PRIME = 65537;

	public static String orderToIteratorName(boolean ascending) {
		return ascending ? "AscendingIterator" : "DescendingIterator";
	}

	public static Long[] permutation(Long[] values, int seed) {
		int count = values.length;
		while (count-- > 0) {
			seed = Math.abs((BIG_PRIME * (seed + count)) % values.length);
			swap(values, count, seed);
		}

		return values;
	}

	private static void swap(Long[] values, int a, int b) {
		Long tmp = values[a];
		values[a] = values[b];
		values[b] = tmp;
	}

	// for map/multiMap
	public static Long getStandardValueForKey(Long key) {
		return key == null ? null : 11 * key;
	}

	public static Long getStandardKeyForValue(Long value) {
		return value == null ? null : value / 11;
	}

	public static Long[] longsForRangeRnd(long start, long end, int seed) {
		return permutation(longsForRange(start, end), seed);
	}

	public static Long[] longsForRange(long start, long end) {
		int count = (int) (end - start + 1);
		Long[] result = new Long[count];

		for (int i = 0; i < count; i++) {
			result[i] = start + i;
		}

		return result;
	}

	public static Long[] longsSeries(long start, int count, int diff) {
		Long[] result = new Long[count];

		for (int i = 0; i < count; i++) {
			result[i] = start + i * diff;
		}

		return result;
	}

}
