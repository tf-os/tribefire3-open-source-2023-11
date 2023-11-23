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
 * Indicates that there is definitely a problem in the implementation of the code respective code itself (i.e. in the method that throws the exception
 * or in related methods/classes) and that the problem was not just caused by invalid input or an invalid state.
 *
 * @author michael.lafite
 */
public class ImplementationIssueException extends AbstractUncheckedBtException {

	private static final long serialVersionUID = -5616964027237856213L;

	public ImplementationIssueException(final String message) {
		super(message);
	}

	/**
	 * Creates a new {@link ImplementationIssueException} instance.
	 *
	 * @param message
	 *            the exception message
	 * @param cause
	 *            the cause of the exception.
	 */
	public ImplementationIssueException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
