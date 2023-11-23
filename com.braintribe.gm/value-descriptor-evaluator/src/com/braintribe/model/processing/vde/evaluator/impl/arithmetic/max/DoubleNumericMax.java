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
package com.braintribe.model.processing.vde.evaluator.impl.arithmetic.max;

import com.braintribe.model.bvd.math.Max;
import com.braintribe.model.processing.vde.evaluator.api.arithmetic.ArithmeticEvalExpert;

/**
 * Expert for {@link Max} that operates on first operand of type Number and
 * second operand of type Number, where internally they are converted to
 * Double
 * 
 */
public class DoubleNumericMax implements ArithmeticEvalExpert<Number, Number> {

	private static DoubleNumericMax instance = null;

	protected DoubleNumericMax() {
		// empty
	}

	public static DoubleNumericMax getInstance() {
		if (instance == null) {
			instance = new DoubleNumericMax();
		}
		return instance;
	}
	
	@Override
	public Object evaluate(Number firstOperand, Number secondOperand) {
		Double firstValue = firstOperand.doubleValue();
		Double secondValue = secondOperand.doubleValue();
		if(firstValue >= secondValue){
			return firstOperand;
		}
		else{
			return secondOperand;
		}
	}

}
