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
package com.braintribe.model.processing;

import com.braintribe.model.processing.impl.ValidationContextImpl;

/**
 * A generic expert for the {@link Validator} to validate concrete values against a specific characteristic (e.g.
 * meeting certain type conditions).
 * 
 * @author Neidhart.Orlich
 *
 */
public interface ValidationExpert {

	/**
	 * Perform a validation action.
	 * 
	 * @see ValidationContextImpl
	 */
	void validate(ValidationContext context);

}
