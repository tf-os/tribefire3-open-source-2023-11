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
package com.braintribe.model.generic;

/**
 * This should be the super type of all custom GM {@link Exception}s.
 */
public class GmfException extends RuntimeException {

	private static final long serialVersionUID = -7605916119918787461L;

	public GmfException() {
		super();
	}

	public GmfException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public GmfException(String message, Throwable cause) {
		super(message, cause);
	}

	public GmfException(String message) {
		super(message);
	}

	public GmfException(Throwable cause) {
		super(cause);
	}

}
