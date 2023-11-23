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
package com.braintribe.utils.lcd;

/**
 * This class provides utility methods related to {@link Throwable}s.
 *
 * @author michael.lafite
 */
public class ThrowableTools {

	protected ThrowableTools() {
		// nothing to do
	}

	/**
	 * Returns the root cause of the passed <code>throwable</code>.
	 */
	public static Throwable getRootCause(final Throwable throwable) {
		Throwable rootCause = throwable;
		while (rootCause.getCause() != null) {
			rootCause = rootCause.getCause();
		}
		return rootCause;
	}

	/**
	 * Sets the <code>cause</code> of the <code>throwable</code> and returns the <code>throwable</code>. This is a convenience method for throwables
	 * that don't have a constructor where one can set the cause.
	 */
	public static <T extends Throwable> T getWithCause(final T throwable, final Throwable cause) {
		throwable.initCause(cause);
		return throwable;
	}

}
