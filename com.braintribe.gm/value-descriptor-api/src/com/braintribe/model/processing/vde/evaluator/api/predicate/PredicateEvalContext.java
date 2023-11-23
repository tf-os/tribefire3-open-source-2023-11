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
package com.braintribe.model.processing.vde.evaluator.api.predicate;

import com.braintribe.model.bvd.predicate.BinaryPredicate;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;

/**
 * Context that is responsible for evaluation of all possible
 * {@link BinaryPredicate} operations. It identifies the correct implementation of the
 * {@link BinaryPredicate} that adheres to the types of the provided
 * operands and returns the result of that implementation
 * 
 */
public interface PredicateEvalContext {

	/**
	 * Evaluates the operands according to the {@link PredicateOperator}. It
	 * selects the correct {@link PredicateEvalExpert} implementation that
	 * satisfies the types of the operands and the given operation
	 * 
	 * @param firstOperand
	 *            The left hand side of the operation
	 * @param secondOperand
	 *            The right hand side of the operation
	 * @param operation
	 *            The type of binary operation
	 * @return result of binary operation with the two operands
	 * @throws VdeRuntimeException
	 */
	<T> T evaluate(Object leftOperand, Object rightOperand, PredicateOperator operation) throws VdeRuntimeException;
	
	
}
