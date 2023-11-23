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
package com.braintribe.model.processing.vde.evaluator.impl.predicate.assignable;

import com.braintribe.model.bvd.predicate.In;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.predicate.PredicateEvalExpert;
import com.braintribe.model.processing.vde.evaluator.impl.predicate.PredicateVdeUtil;

/**
 * Expert for {@link In} that operates on left hand side operand of type
 * Object and right hand side operand of type Object
 * 
 */
public class ObjectAssignableToObject implements PredicateEvalExpert<Object, Object> {

	private static ObjectAssignableToObject instance = null;

	protected ObjectAssignableToObject() {
		// empty
	}

	public static ObjectAssignableToObject getInstance() {
		if (instance == null) {
			instance = new ObjectAssignableToObject();
		}
		return instance;
	}
	
	@Override
	public Object evaluate(Object leftOperand, Object rightOperand) throws VdeRuntimeException {
		return PredicateVdeUtil.instanceOf(leftOperand, rightOperand, true);
	}

}
