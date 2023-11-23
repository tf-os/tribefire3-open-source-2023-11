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
package com.braintribe.model.processing.vde.evaluator.impl.predicate.greaterorequal;

import java.util.Date;

import com.braintribe.model.bvd.predicate.GreaterOrEqual;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.predicate.PredicateEvalExpert;

/**
 * Expert for {@link GreaterOrEqual} that operates on left hand side operand of type
 * Date and right hand side operand of type Date
 * 
 */
public class DateGreaterOrEqual implements PredicateEvalExpert<Date, Date> {

	private static DateGreaterOrEqual instance = null;

	protected DateGreaterOrEqual() {
		// empty
	}

	public static DateGreaterOrEqual getInstance() {
		if (instance == null) {
			instance = new DateGreaterOrEqual();
		}
		return instance;
	}
	
	@Override
	public Object evaluate(Date leftOperand, Date rightOperand) throws VdeRuntimeException {
		long left = leftOperand.getTime();
		long right = rightOperand.getTime();
		return left >= right;
	}

}
