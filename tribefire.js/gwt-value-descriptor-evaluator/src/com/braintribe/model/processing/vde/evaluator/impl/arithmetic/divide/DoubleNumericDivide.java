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
package com.braintribe.model.processing.vde.evaluator.impl.arithmetic.divide;

import com.braintribe.model.bvd.math.Divide;
import com.braintribe.model.processing.vde.evaluator.api.arithmetic.ArithmeticEvalExpert;

/**
 * Expert for {@link Divide} that operates on first operand of type Number and
 * second operand of type Number, where internally they are converted to
 * Double
 * 
 */
public class DoubleNumericDivide implements ArithmeticEvalExpert<Number, Number> {

	private static DoubleNumericDivide instance = null;

	protected DoubleNumericDivide() {
		// empty
	}

	public static DoubleNumericDivide getInstance() {
		if (instance == null) {
			instance = new DoubleNumericDivide();
		}
		return instance;
	}
	
	@Override
	public Object evaluate(Number firstOperand, Number secondOperand) {
		Double firstValue = firstOperand.doubleValue();
		Double secondValue = secondOperand.doubleValue();
		return firstValue / secondValue;
	}

}
