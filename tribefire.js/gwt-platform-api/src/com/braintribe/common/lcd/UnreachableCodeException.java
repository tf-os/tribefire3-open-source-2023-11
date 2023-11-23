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
 * Indicates an unanticipated execution path.
 *
 * @author michael.lafite
 */
public class UnreachableCodeException extends AbstractUncheckedBtException {

	private static final long serialVersionUID = 5969949996442696699L;

	public UnreachableCodeException() {
		this(null);
	}

	public UnreachableCodeException(final String furtherInfo) {
		this(null, furtherInfo);
	}

	public UnreachableCodeException(final String furtherInfo, final Throwable cause) {
		this(null, furtherInfo, cause);
	}

	public UnreachableCodeException(final String mainMessage, final String furtherInfo) {
		this(mainMessage, furtherInfo, null);
	}

	public UnreachableCodeException(final String mainMessage, final String furtherInfo, final Throwable cause) {
		super((mainMessage == null ? "Unanticipated execution path! This code should not be reachable!" : mainMessage)
				+ (furtherInfo == null ? "" : " Further info: " + furtherInfo), cause);
	}
}
