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
package com.braintribe.model.processing.query.eval.tools;

import java.util.Comparator;

/**
 * 
 * @author peter.gazdik
 */
public class StringAlphabeticalComparator implements Comparator<String> {

	public static final StringAlphabeticalComparator INSTANCE = new StringAlphabeticalComparator();

	private StringAlphabeticalComparator() {
	}

	@Override
	public int compare(String str1, String str2) {
		int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
		if (res == 0) {
			// we want lowerCase to be smaller than upperCase, like SQL DBs usually do (I think)
			res = str2.compareTo(str1);
		}
		return res;
	}

}
