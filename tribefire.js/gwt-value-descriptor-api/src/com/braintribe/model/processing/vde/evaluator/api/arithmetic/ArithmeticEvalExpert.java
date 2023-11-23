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
package com.braintribe.model.processing.vde.evaluator.api.arithmetic;

import com.braintribe.model.bvd.math.ArithmeticOperation;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;

/**
 * An expert that will evaluate an {@link ArithmeticOperation} between two
 * operands of type E and T
 * 
 * @param <E>
 *            Type of first operand
 * @param <T>
 *            Type of second operand
 */
public interface ArithmeticEvalExpert<E, T> {

	Object evaluate(E firstOperand, T secondOperand) throws VdeRuntimeException;

}
