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
package com.braintribe.common.lcd;

import java.util.Comparator;

/**
 * {@link Comparator} that compares objects based on their string representations.
 *
 * @author michael.lafite
 */
public class StringRepresentationBasedComparator implements Comparator<Object> {

	@Override
	public int compare(final Object object1, final Object object2) {
		final String stringRepresentation1 = String.valueOf(object1);
		final String stringRepresentation2 = String.valueOf(object2);
		return stringRepresentation1.compareTo(stringRepresentation2);
	}
}
