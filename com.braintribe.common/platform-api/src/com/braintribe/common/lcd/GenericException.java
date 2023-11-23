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

/**
 * This exception can be used, if one wants to throw a {@link CheckedBtException}, but if it's not worth it to create a new exception class.
 *
 * @author michael.lafite
 */
public class GenericException extends AbstractCheckedBtException {

	private static final long serialVersionUID = -8140685108520299492L;

	public GenericException(final String message) {
		super(message);
	}

	public GenericException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
