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
package com.braintribe.utils;

import com.braintribe.common.lcd.AssertionException;

/**
 * This class may contain Java-only (i.e. GWT incompatible) code. For further information please see {@link com.braintribe.utils.lcd.Not}.
 *
 */
public class Not extends com.braintribe.utils.lcd.Not {

	/**
	 * Asserts the passed <code>object</code> is not <code>null</code>.
	 */
	public static <T> T Null(T object, String errorMessage, Object... args) {
		if (object == null) {
			throw new AssertionException(String.format(errorMessage, args)); // String.format() is not supported in GWT.
		}
		return object;
	}

}
