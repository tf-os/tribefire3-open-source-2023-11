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
package com.braintribe.model.processing.vde.evaluator.impl.predicate.equal;

import com.braintribe.model.bvd.predicate.Equal;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.predicate.PredicateEvalExpert;

/**
 * Expert for {@link Equal} that operates on left hand side operand of type
 * Number and right hand side operand of type Number, internally they are
 * treated as Double
 * 
 */
public class NumberEqual implements PredicateEvalExpert<Number, Number> {

	private static NumberEqual instance = null;

	protected NumberEqual() {
		// empty
	}

	public static NumberEqual getInstance() {
		if (instance == null) {
			instance = new NumberEqual();
		}
		return instance;
	}
	
	@Override
	public Object evaluate(Number leftOperand, Number rightOperand) throws VdeRuntimeException {
		double left = leftOperand.doubleValue();
		double right = rightOperand.doubleValue();
		return left == right;
	}

}
