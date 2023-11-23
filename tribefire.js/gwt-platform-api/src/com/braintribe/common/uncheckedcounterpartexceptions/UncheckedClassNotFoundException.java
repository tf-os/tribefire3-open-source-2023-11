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
package com.braintribe.common.uncheckedcounterpartexceptions;

import com.braintribe.common.lcd.uncheckedcounterpartexceptions.UncheckedCounterpartException;

/**
 * Unchecked counterpart of {@link ClassNotFoundException}.
 *
 * @author michael.lafite
 */
public class UncheckedClassNotFoundException extends UncheckedCounterpartException {

	private static final long serialVersionUID = 7187769926059260954L;

	public UncheckedClassNotFoundException(final String message) {
		super(message);
	}

	public UncheckedClassNotFoundException(final String message, final ClassNotFoundException cause) {
		super(message, cause);
	}
}
