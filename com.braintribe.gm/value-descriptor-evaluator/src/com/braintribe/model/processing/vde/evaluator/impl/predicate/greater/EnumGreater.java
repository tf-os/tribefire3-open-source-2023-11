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

import com.braintribe.model.bvd.predicate.Greater;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.predicate.PredicateEvalExpert;

/**
 * Expert for {@link Greater} that operates on left hand side operand of type
 * Enum and right hand side operand of type Enum
 * 
 */
public class EnumGreater implements PredicateEvalExpert<Enum<?>, Enum<?>> {

	private static EnumGreater instance = null;

	protected EnumGreater() {
		// empty
	}

	public static EnumGreater getInstance() {
		if (instance == null) {
			instance = new EnumGreater();
		}
		return instance;
	}

	@Override
	public Object evaluate(Enum<?> leftOperand, Enum<?> rightOperand) throws VdeRuntimeException {

		@SuppressWarnings("rawtypes")
		Class<? extends Enum> class1 = leftOperand.getClass();
		@SuppressWarnings("rawtypes")
		Class<? extends Enum> class2 = rightOperand.getClass();

		if (class1.equals(class2)) {
			return leftOperand.ordinal() > rightOperand.ordinal();

		} else {
			throw new VdeRuntimeException("Left and right Enum must be of same type for Enum Comparison");
		}
	}

}
