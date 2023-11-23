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
 * Default {@link EqualsCheck} which just delegates to {@link Object#equals(Object)} .
 *
 * @author michael.lafite
 *
 * @param <T>
 *            the type of the objects to check for equality
 */
public class DefaultEqualsCheck<T> implements EqualsCheck<T> {

	public DefaultEqualsCheck() {
		// nothing to do
	}

	/**
	 * Checks if the two objects are equal using {@link Object#equals(Object)}. Note that this method also returns <code>true</code>, if both objects
	 * are <code>null</code>.
	 */
	@Override
	public boolean equals(final T object1, final Object object2) {
		if (object1 == null) {
			return (object2 == null);
		}

		return object1.equals(object2);
	}

}
