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
package com.braintribe.model.processing.vde.evaluator.impl.arithmetic.add;

import com.braintribe.model.bvd.math.Add;
import com.braintribe.model.processing.vde.evaluator.api.arithmetic.ArithmeticEvalExpert;

/**
 * Expert for {@link Add} that operates on first operand of type Number and
 * second operand of type Number, where internally they are converted to
 * Long
 * 
 */
public class LongNumericAdd implements ArithmeticEvalExpert<Number, Number> {

	private static LongNumericAdd instance = null;

	protected LongNumericAdd() {
		// empty
	}

	public static LongNumericAdd getInstance() {
		if (instance == null) {
			instance = new LongNumericAdd();
		}
		return instance;
	}
	
	@Override
	public Object evaluate(Number firstOperand, Number secondOperand) {
		Long firstValue = firstOperand.longValue();
		Long secondValue = secondOperand.longValue();
		return firstValue + secondValue;
	}

}
