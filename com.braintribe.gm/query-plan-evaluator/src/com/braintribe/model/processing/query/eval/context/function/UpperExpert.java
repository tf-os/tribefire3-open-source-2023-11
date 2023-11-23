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

import static com.braintribe.model.processing.query.eval.context.function.QueryFunctionEvalTools.resolveOperandValue;

import java.util.Map;

import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.api.function.QueryFunctionExpert;
import com.braintribe.model.query.functions.value.Upper;
import com.braintribe.model.queryplan.value.Value;

public class UpperExpert implements QueryFunctionExpert<Upper> {

	public static final UpperExpert INSTANCE = new UpperExpert();

	private UpperExpert() {
	}

	@Override
	public Object evaluate(Tuple tuple, Upper queryFunction, Map<Object, Value> operandMappings, QueryEvaluationContext context) {
		Object s = resolveOperandValue(context, tuple, operandMappings, queryFunction.getOperand());

		return s != null ? String.valueOf(s).toUpperCase() : null;
	}

}
