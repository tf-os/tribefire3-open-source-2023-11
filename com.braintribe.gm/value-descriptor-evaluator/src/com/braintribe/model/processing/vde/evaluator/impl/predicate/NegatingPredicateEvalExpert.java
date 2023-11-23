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
package com.braintribe.model.processing.vde.evaluator.impl.predicate;

import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.predicate.PredicateEvalExpert;

/**
 * An {@link PredicateEvalExpert} that negates a result prior to returning the value
 * 
 * @param <T1>
 *            Type of first operand
 * @param <T2>
 *            Type of second operand
 */
public class NegatingPredicateEvalExpert<T1, T2> implements PredicateEvalExpert<T1, T2> {

	private PredicateEvalExpert<T1, T2> expert;

	public NegatingPredicateEvalExpert(PredicateEvalExpert<T1,T2> expert) {
		this.expert = expert;
		
	}
	
	@Override
	public Object evaluate(T1 firstOperand, T2 secondOperand) throws VdeRuntimeException {
		Object result = expert.evaluate(firstOperand, secondOperand);
		return ! ((Boolean)result);
	}

}
