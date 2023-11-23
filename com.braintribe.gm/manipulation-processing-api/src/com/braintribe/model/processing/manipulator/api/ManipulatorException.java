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
package com.braintribe.model.processing.manipulator.api;

import com.braintribe.model.generic.reflection.GenericModelException;

/**
 * @deprecated Just don't use this, take {@link GenericModelException} or something specific to your problem.
 */
@Deprecated
public class ManipulatorException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ManipulatorException() {
		super();
	}

	public ManipulatorException(String message, Throwable cause) {
		super(message, cause);
	}

	public ManipulatorException(String message) {
		super(message);
	}

	public ManipulatorException(Throwable cause) {
		super(cause);
	}

}