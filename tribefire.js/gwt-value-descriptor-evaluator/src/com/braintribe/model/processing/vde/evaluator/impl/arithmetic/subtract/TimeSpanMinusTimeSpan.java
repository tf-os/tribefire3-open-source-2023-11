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
package com.braintribe.model.processing.vde.evaluator.impl.arithmetic.subtract;

import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.arithmetic.ArithmeticEvalExpert;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.ArithmeticVdeUtil;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;

/**
 * Expert for Minus that operates on first operand of type TimeSpan and second
 * operand of type TimeSpan
 * 
 */
public class TimeSpanMinusTimeSpan implements ArithmeticEvalExpert<TimeSpan, TimeSpan> {

	private static TimeSpanMinusTimeSpan instance = null;

	protected TimeSpanMinusTimeSpan() {
		// empty
	}

	public static TimeSpanMinusTimeSpan getInstance() {
		if (instance == null) {
			instance = new TimeSpanMinusTimeSpan();
		}
		return instance;
	}
	
	@Override	
	public Object evaluate(TimeSpan firstOperand, TimeSpan secondOperand) throws VdeRuntimeException {
		double computationResult  = ArithmeticVdeUtil.getNanoSeconds(firstOperand) - ArithmeticVdeUtil.getNanoSeconds(secondOperand);
		TimeUnit unit = ArithmeticVdeUtil.getTargetTimeUnit(firstOperand, secondOperand);
		TimeSpan span = TimeSpan.T.create();
		span.setUnit(unit);
		span.setValue(ArithmeticVdeUtil.convertNanoSeconds(unit, computationResult));
		return span;
	}

}
