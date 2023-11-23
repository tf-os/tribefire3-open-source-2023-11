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
 * An unchecked exception that signals a Generic Model related problem.
 *
 * @author michael.lafite
 */
public class GmException extends AbstractUncheckedBtException {

	private static final long serialVersionUID = -2860713088264876392L;

	public GmException(final String message) {
		super(message);
	}

	/**
	 * Creates a new {@link GmException} instance.
	 *
	 * @param message
	 *            the exception message
	 * @param cause
	 *            the cause of the exception.
	 */
	public GmException(final String message, final Exception cause) {
		super(message, cause);
	}
}
