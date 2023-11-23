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

import com.braintribe.utils.lcd.CommonTools;

/**
 * This exception is thrown (manually) to indicate that an assertion (i.e. a some check) has failed. Note that this is not the same as
 * {@link AssertionError}, which is thrown by <code>assert</code>.
 *
 * @author michael.lafite
 */
public class AssertionException extends AbstractUncheckedBtException {

	private static final long serialVersionUID = 3017226426669205792L;

	public AssertionException(final String message, final Object actualValue, final Object expectedValue) {
		super(message + Constants.LINE_SEPARATOR + "Expected Value: " + CommonTools.getStringRepresentation(expectedValue) + "Actual Value:   "
				+ CommonTools.getStringRepresentation(actualValue));
	}

	public AssertionException(final Object actualValue, final Object expectedValue) {
		this("Assertion failed! Actual value doesn't match expected one!", actualValue, expectedValue);
	}

	public AssertionException(final String message) {
		super(message);
	}
}
