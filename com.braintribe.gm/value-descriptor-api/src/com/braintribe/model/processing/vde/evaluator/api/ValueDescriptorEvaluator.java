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
package com.braintribe.model.processing.vde.evaluator.api;

import com.braintribe.model.generic.value.ValueDescriptor;

/**
 * This is the super type for all value descriptor evaluators
 * 
 * @param <C>
 *            Type of value descriptor
 * 
 */
public interface ValueDescriptorEvaluator<C extends ValueDescriptor> {

	/**
	 * Evaluate the value descriptor based on the context
	 * 
	 * @param context
	 *            Context that the value descriptor is evaluated against
	 * @param valueDescriptor
	 *            The value descriptor that needs evaluation
	 * @return A VdeResult instance that contains the result of the evaluation
	 * @throws VdeRuntimeException
	 */
	VdeResult evaluate(VdeContext context, C valueDescriptor) throws VdeRuntimeException;

}
