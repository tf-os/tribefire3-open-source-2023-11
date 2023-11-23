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
 * Checks if two strings are equal ignoring the case.
 *
 * @author michael.lafite
 */
public class IgnoreCaseEqualsCheck implements EqualsCheck<String> {

	public IgnoreCaseEqualsCheck() {
		// nothing to do
	}

	/**
	 * Returns <code>true</code>, if both objects are <code>null</code>, or if <code>object1</code> {@link String#equalsIgnoreCase(String)
	 * equalsIgnoreCase} <code>object2</code>, otherwise <code>false</code>.
	 */
	@Override
	public boolean equals(final String object1, final Object object2) {
		if (object1 == null) {
			return (object2 == null);
		}
		if (!(object2 instanceof String)) {
			return false;
		}

		return object1.equalsIgnoreCase((String) object2);
	}
}
