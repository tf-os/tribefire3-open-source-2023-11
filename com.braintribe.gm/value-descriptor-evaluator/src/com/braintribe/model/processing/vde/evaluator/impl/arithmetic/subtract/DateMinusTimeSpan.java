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

import java.util.Date;

import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.arithmetic.ArithmeticEvalExpert;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.ArithmeticVdeUtil;
import com.braintribe.model.time.TimeSpan;

/**
 * Expert for Minus that operates on first operand of type Date and second
 * operand of type TimeSpan
 * 
 */
public class DateMinusTimeSpan implements ArithmeticEvalExpert<Date, TimeSpan> {

	private static DateMinusTimeSpan instance = null;

	protected DateMinusTimeSpan() {
		// empty
	}

	public static DateMinusTimeSpan getInstance() {
		if (instance == null) {
			instance = new DateMinusTimeSpan();
		}
		return instance;
	}
	
	@Override
	public Object evaluate(Date firstOperand, TimeSpan secondOperand) throws VdeRuntimeException {
		return ArithmeticVdeUtil.addTimeSpanToDate(firstOperand, secondOperand, -1.0);
	}

}
