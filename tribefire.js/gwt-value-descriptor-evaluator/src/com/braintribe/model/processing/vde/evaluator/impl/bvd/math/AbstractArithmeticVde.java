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
package com.braintribe.model.processing.vde.evaluator.impl.bvd.math;

import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;

import java.util.Iterator;
import java.util.List;

import com.braintribe.model.bvd.math.ArithmeticOperation;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.arithmetic.ArithmeticOperator;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;
import com.braintribe.model.processing.vde.evaluator.impl.arithmetic.ArithmeticOperatorEval;

public abstract class AbstractArithmeticVde {

	protected VdeResult evaluate(VdeContext context, ArithmeticOperation valueDescriptor, ArithmeticOperator operator) throws VdeRuntimeException {

		List<Object> operandList = valueDescriptor.getOperands();

		if (isEmpty(operandList)) {
			throw new VdeRuntimeException("No operands provided for Arithmetic operation");
		}

		Object result = evaluateOperands(context, operandList, operator);
		return new VdeResultImpl(result, false);
	}

	private Object evaluateOperands(VdeContext context, List<Object> operandList, ArithmeticOperator operator) {
		if (operandList.size() == 1) {
			return context.evaluate(operandList.get(0));
		}

		Iterator<Object> it = operandList.iterator();
		Object left = context.evaluate(it.next());

		while (it.hasNext()) {
			Object right = context.evaluate(it.next());
			left = ArithmeticOperatorEval.evaluate(left, right, operator);
		}

		return left;
	}

}
