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
package com.braintribe.exception;

/**
 * This exception only exists to turn {@link ReflectiveOperationException}s into unchecked exceptions that can be thrown from methods that do not allow to throw this checked exception.
 * @author Dirk Scheffler
 *
 */
public class UncheckedReflectiveOperationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UncheckedReflectiveOperationException(String message, ReflectiveOperationException cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UncheckedReflectiveOperationException(String message, ReflectiveOperationException cause) {
		super(message, cause);
	}

	public UncheckedReflectiveOperationException(ReflectiveOperationException cause) {
		super(cause);
	}

}
