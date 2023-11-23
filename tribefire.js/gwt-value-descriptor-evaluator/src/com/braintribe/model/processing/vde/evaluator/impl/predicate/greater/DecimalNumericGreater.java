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
package com.braintribe.model.processing.vde.evaluator.impl.predicate.greater;

import java.math.BigDecimal;

import com.braintribe.model.bvd.predicate.Greater;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.predicate.PredicateEvalExpert;

/**
 * Expert for {@link Greater} that operates on left hand side operand of type
 * Number and right hand side operand of type Number, internally they are
 * converted to BigDecimal
 * 
 */
public class DecimalNumericGreater implements PredicateEvalExpert<Number, Number> {

	private static DecimalNumericGreater instance = null;

	protected DecimalNumericGreater() {
		// empty
	}

	public static DecimalNumericGreater getInstance() {
		if (instance == null) {
			instance = new DecimalNumericGreater();
		}
		return instance;
	}

	@Override
	public Object evaluate(Number leftOperand, Number rightOperand) throws VdeRuntimeException {
		BigDecimal left = new BigDecimal(leftOperand.toString());
		BigDecimal right = new BigDecimal(rightOperand.toString());
		return left.compareTo(right) == 1.0 ? Boolean.TRUE : Boolean.FALSE;
	}

}
