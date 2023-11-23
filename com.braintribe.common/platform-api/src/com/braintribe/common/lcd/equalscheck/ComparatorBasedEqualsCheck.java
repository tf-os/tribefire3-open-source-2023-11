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
package com.braintribe.common.lcd.equalscheck;

import java.util.Comparator;

/**
 * {@link EqualsCheck} whose implementation of {@link #equals(Object, Object)} is based on the result of {@link #compare(Object, Object)}.
 *
 * @author michael.lafite
 *
 * @param <T>
 *            the type of the objects to check for equality (and also the type of the objects that may be compared by the comparator this check is
 *            based on)
 */
public interface ComparatorBasedEqualsCheck<T> extends EqualsCheck<T>, Comparator<T> {
	// no additional methods
}
