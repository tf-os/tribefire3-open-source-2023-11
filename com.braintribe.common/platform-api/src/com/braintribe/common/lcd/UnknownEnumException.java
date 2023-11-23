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
 * This exception signals that an enum constant couldn't be handled because it's unknown. If your code is intended to handle all constants of an enum
 * type but it still finds an unknown constant, throw this exception (for example in the default case of a switch statement). In doing so one will get
 * a proper exception if someone adds a new enum constant but forgets to update your code.
 *
 * @author michael.lafite
 * @see UnsupportedEnumException
 */
public class UnknownEnumException extends AbstractUncheckedBtException {

	private static final long serialVersionUID = -7752127746828963233L;

	public UnknownEnumException(final Enum<?> enumConstant) {
		super("Enum constant " + enumConstant + " of enum type " + enumConstant.getClass().getName() + " is unknown!");
	}
}
