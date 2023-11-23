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

/**
 * 
 */
public class ObjectComparator {

	@SuppressWarnings("unchecked")
	public static int compare(Object o1, Object o2) {
		Comparable<Object> value1 = (Comparable<Object>) o1;
		Comparable<Object> value2 = (Comparable<Object>) o2;

		if (value1 == value2) {
			return 0;
		}

		if (value1 == null) {
			return -1;
		}

		if (value2 == null) {
			return 1;
		}

		return value1.compareTo(value2);
	}
}
