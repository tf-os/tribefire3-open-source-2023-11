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
package com.braintribe.codec.jseval.genericmodel;

public class LongLib {
	protected static final int BITS = 22;
	protected static final int BITS01 = 2 * BITS;
	protected static final int BITS2 = 64 - BITS01;
	protected static final int MASK = (1 << BITS) - 1;
	protected static final int MASK_2 = (1 << BITS2) - 1;

	/**
	 * Return a triple of ints { low, middle, high } that concatenate
	 * bitwise to the given number.
	 */
	public static int[] getAsIntArray(long l) {
		int[] a = new int[3];
		a[0] = (int) (l & MASK);
		a[1] = (int) ((l >> BITS) & MASK);
		a[2] = (int) ((l >> BITS01) & MASK_2);
		return a;
	}
}
