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
package com.braintribe.model.processing.query.eval.context.function;

import java.util.Map;

import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.query.Operand;
import com.braintribe.model.queryplan.value.Value;

/**
 * @author peter.gazdik
 */
/* package */ class QueryFunctionEvalTools {

	public static Object resolveOperandValue(QueryEvaluationContext context, Tuple tuple, Map<Object, Value> operandMappings, Object operand) {
		if (operand instanceof Operand)
			return context.resolveValue(tuple, operandMappings.get(operand));
		else
			return operand;

	}

}
