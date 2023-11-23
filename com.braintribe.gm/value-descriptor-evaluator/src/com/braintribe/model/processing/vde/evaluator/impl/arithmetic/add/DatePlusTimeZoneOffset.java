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
import com.braintribe.model.time.TimeZoneOffset;

/**
 * Expert for {@link Add} that operates on first operand of type Date and second
 * operand of type TimeZoneOffset
 * 
 */
public class DatePlusTimeZoneOffset implements ArithmeticEvalExpert<Date, TimeZoneOffset> {

	private static DatePlusTimeZoneOffset instance = null;

	protected DatePlusTimeZoneOffset() {
		// empty
	}

	public static DatePlusTimeZoneOffset getInstance() {
		if (instance == null) {
			instance = new DatePlusTimeZoneOffset();
		}
		return instance;
	}
	
	@Override
	public Object evaluate(Date firstOperand, TimeZoneOffset secondOperand) throws VdeRuntimeException {
		return ArithmeticVdeUtil.addTimeZoneOffsetToDate(firstOperand, secondOperand, 1.0);
	}

}
