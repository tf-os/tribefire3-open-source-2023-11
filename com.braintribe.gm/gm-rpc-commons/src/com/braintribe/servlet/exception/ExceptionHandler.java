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
package com.braintribe.servlet.exception;

import java.util.function.Function;

/**
 * Interface for code that deals with general exceptions in the {@link ExceptionFilter}.
 * <p><p>
 * The interface extends a function that takes a {@link ExceptionHandlingContext} object and return
 * a boolean value. True means, that the exception has been dealt with, false if nothing happened.
 * If none of the configured handlers is able to return true, a default mechnanism takes place.
 * Returning true does not prevent other handlers from processing the exception.
 * 
 * @author roman.kurmanowytsch
 */
public interface ExceptionHandler extends Function<ExceptionHandlingContext,Boolean> {

	//Intentionally left empty
	
}
