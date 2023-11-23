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
 * Indicates that a method (or service, feature, component, etc.) has not been (fully) implemented yet. If it will never be implemented (e.g. because
 * not all methods of an interface shall be supported), one should use {@link NotSupportedException}.
 *
 * @author michael.lafite
 */
public class NotImplementedException extends AbstractUncheckedBtException {

	private static final long serialVersionUID = -8906729714740191286L;

	public NotImplementedException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public NotImplementedException(final String message) {
		super(message);
	}

	public NotImplementedException() {
		// no message
		this(null);
	}
}
