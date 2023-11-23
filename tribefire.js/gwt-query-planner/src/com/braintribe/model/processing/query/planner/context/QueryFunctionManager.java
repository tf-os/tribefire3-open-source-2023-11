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
package com.braintribe.model.processing.query.planner.context;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.braintribe.model.processing.query.tools.QueryFunctionAnalyzer;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.queryplan.value.StaticValue;
import com.braintribe.model.queryplan.value.Value;

/**
 * @author peter.gazdik
 */
public class QueryFunctionManager {

	private final QueryPlannerContext context;

	private final Map<QueryFunction, List<Operand>> functionOperands = newMap();
	private final Map<QueryFunction, Map<Object, Value>> queryModelOperandValues = newMap();

	public QueryFunctionManager(QueryPlannerContext context) {
		this.context = context;
	}

	public Map<Object, Value> noticeQueryFunction(QueryFunction function) {
		Map<Object, Value> operandMappings = queryModelOperandValues.get(function);

		if (operandMappings == null) {
			operandMappings = resolveOperandMappings(function);
			queryModelOperandValues.put(function, operandMappings);
		}

		return operandMappings;
	}

	private Map<Object, Value> resolveOperandMappings(QueryFunction function) {
		Map<Object, Value> result = newMap();

		for (Operand operand: listOperands(function)) {
			if (operand instanceof QueryFunction)
				noticeQueryFunction((QueryFunction) operand);

			Value operandValue = context.convertOperand(operand);

			if (!(operandValue instanceof StaticValue))
				result.put(operand, operandValue);
		}

		return result;
	}

	public List<Operand> listOperands(QueryFunction function) {
		List<Operand> result = functionOperands.get(function);

		if (result == null) {
			result = newList();
			functionOperands.put(function, result);

			for (Object operand: listAllOperands(function))
				if (operand instanceof Operand)
					result.add((Operand) operand);
		}

		return result;
	}

	private Collection<?> listAllOperands(QueryFunction function) {
		return QueryFunctionAnalyzer.findOperands(function);
	}

}
