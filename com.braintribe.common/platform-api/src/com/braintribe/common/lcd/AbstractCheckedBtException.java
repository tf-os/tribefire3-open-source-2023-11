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
 * An {@link Exception} that can be used as super class, if one wants to distinguish own <code>Exception</code>s from other
 * <code>java.lang.Exception</code>s.
 *
 * @author michael.lafite
 */
public abstract class AbstractCheckedBtException extends Exception implements CheckedBtException {

	private static final long serialVersionUID = 4199072506849171063L;

	protected AbstractCheckedBtException(final String message) {
		super(message);
	}

	protected AbstractCheckedBtException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
