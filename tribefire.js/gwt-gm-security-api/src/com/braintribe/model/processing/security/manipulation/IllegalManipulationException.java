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
package com.braintribe.model.processing.security.manipulation;

import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processing.security.SecurityAspectException;

/**
 * Exception to use when {@link Manipulation} validation failed.
 */
public class IllegalManipulationException extends SecurityAspectException {

	private static final long serialVersionUID = 3745629615149151883L;

	private ValidationResult validationResult;

	public IllegalManipulationException(ValidationResult validationResult) {
		super(validationResult.toString());
		this.validationResult = validationResult;
	}

	public ValidationResult getValidationResult() {
		return validationResult;
	}

}
