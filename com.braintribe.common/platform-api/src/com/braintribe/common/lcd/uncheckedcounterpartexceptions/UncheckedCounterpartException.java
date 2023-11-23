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
package com.braintribe.common.lcd.uncheckedcounterpartexceptions;

import com.braintribe.common.lcd.AbstractUncheckedBtException;
import com.braintribe.common.lcd.BtException;
import com.braintribe.common.lcd.GenericRuntimeException;

/**
 * Super class for <code>RuntimeException</code>s that are unchecked counterparts of checked <code>Exception</code>s.
 * This exception <b>requires</b> the {@link #getCause() cause} to be a checked exception! If you just want to throw an
 * unchecked {@link BtException} without having to implement your own, please use {@link GenericRuntimeException}
 * instead!
 *
 * @author michael.lafite
 */
public class UncheckedCounterpartException extends AbstractUncheckedBtException {

	private static final long serialVersionUID = -8282360971144294276L;

	protected UncheckedCounterpartException(final String message) {
		super(message);
	}

	/**
	 * Creates a new {@link UncheckedCounterpartException}.
	 *
	 * @param message
	 *            the exception message.
	 * @param cause
	 *            the cause which must be a checked exception (or <code>null</code>).
	 */
	protected UncheckedCounterpartException(final String message, final Exception cause) {
		super(message);
		// set cause via initCause method, because it checks the passed cause
		initCause(cause);
	}

	@Override
	public final synchronized UncheckedCounterpartException initCause(final Throwable cause) {
		if (cause instanceof RuntimeException) {
			throw new IllegalArgumentException(
					"The passed cause is not a checked exception! (for convenience it is added as cause of this exception)", cause);
		}
		if (!(cause instanceof Exception)) {
			throw new IllegalArgumentException("The passed cause is not an exception!", cause);
		}

		return (UncheckedCounterpartException) super.initCause(cause);
	}
}
