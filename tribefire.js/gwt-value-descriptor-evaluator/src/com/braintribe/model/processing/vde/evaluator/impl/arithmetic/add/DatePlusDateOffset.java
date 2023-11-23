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

import java.util.Date;

import com.braintribe.model.bvd.math.Add;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.arithmetic.ArithmeticEvalExpert;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.ArithmeticVdeUtil;
import com.braintribe.model.time.DateOffset;

/**
 * Expert for {@link Add} that operates on first operand of type Date and second
 * operand of type DateOffset
 * 
 */
public class DatePlusDateOffset implements ArithmeticEvalExpert<Date, DateOffset> {

	private static DatePlusDateOffset instance = null;

	protected DatePlusDateOffset() {
		// empty
	}

	public static DatePlusDateOffset getInstance() {
		if (instance == null) {
			instance = new DatePlusDateOffset();
		}
		return instance;
	}

	@Override
	public Object evaluate(Date firstOperand, DateOffset secondOperand) throws VdeRuntimeException {
		return ArithmeticVdeUtil.addDateOffsetToDate(firstOperand, secondOperand, 1.0);
	}

}
