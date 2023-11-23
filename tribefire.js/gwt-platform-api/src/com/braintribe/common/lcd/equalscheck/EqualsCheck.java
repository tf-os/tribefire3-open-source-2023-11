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

/**
 * Interface used to implement custom <code>equals</code> checks without overriding {@link Object#equals(Object)}.
 *
 * @author michael.lafite
 *
 * @param <T>
 *            the type of the objects to check for equality
 */
public interface EqualsCheck<T> {

	/**
	 * Checks whether the two objects are equal (from the standpoint of this check). Objects may be <code>null</code>.
	 */
	boolean equals(T object1, Object object2);

}
