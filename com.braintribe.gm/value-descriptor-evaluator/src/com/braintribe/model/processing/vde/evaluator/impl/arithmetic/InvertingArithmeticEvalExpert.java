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
package com.braintribe.model.processing.vde.evaluator.impl.arithmetic;

import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.arithmetic.ArithmeticEvalExpert;

/**
 * An {@link ArithmeticEvalExpert} that flips the order of the operands
 * 
 * @param <T1>
 *            Type of first operand
 * @param <T2>
 *            Type of second operand
 */
public class InvertingArithmeticEvalExpert<T1, T2> implements ArithmeticEvalExpert<T1, T2> {

	private ArithmeticEvalExpert<T1, T2> expert;

	public InvertingArithmeticEvalExpert(ArithmeticEvalExpert<T1, T2> expert) {
		this.expert = expert;

	}

	@Override
	@SuppressWarnings("unchecked")
	public Object evaluate(T1 firstOperand, T2 secondOperand) throws VdeRuntimeException {
		return expert.evaluate((T1) secondOperand, (T2) firstOperand);
	}

}
