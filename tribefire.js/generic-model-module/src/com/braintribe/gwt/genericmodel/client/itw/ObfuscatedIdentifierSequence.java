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
package com.braintribe.gwt.genericmodel.client.itw;

// Currently not used. This is important,a s the specialChar is actually used outside of this generator.
public class ObfuscatedIdentifierSequence {

	public static final char specialChar = '\u02b9';

	public static final ObfuscatedIdentifierSequence runtimePropertySequence = new ObfuscatedIdentifierSequence(specialChar);

	private static final char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ$123456789".toCharArray();
	private static final int base = chars.length;

	private final int[] sequence = new int[6];
	private int digits = 1;
	public int count = 0;
	private final char poolName;
	private char extract[];

	public ObfuscatedIdentifierSequence(char poolName) {
		this.poolName = poolName;

		resetExtract();
	}

	public String next() {
		count++;
		extract[0] = poolName;
		boolean carry = true;
		for (int i = 0; i < digits; i++) {
			int d = sequence[i];

			extract[i + 1] = chars[d];

			if (carry) {
				d++;

				if (d == base) {
					sequence[i] = 0;
				} else {
					sequence[i] = d;
					carry = false;
				}
			}
		}

		String result = new String(extract);

		if (carry) {
			digits++;
			resetExtract();
		}

		return result;
	}

	private void resetExtract() {
		extract = new char[digits + 1];
	}

	public int getCount() {
		return count;
	}

}
