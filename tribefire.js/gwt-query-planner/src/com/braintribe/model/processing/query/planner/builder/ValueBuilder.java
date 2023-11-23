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
package com.braintribe.model.processing.query.planner.builder;

import java.util.Collections;
import java.util.Map;

import com.braintribe.model.processing.query.planner.RuntimeQueryPlannerException;
import com.braintribe.model.processing.query.planner.core.index.IndexKeys;
import com.braintribe.model.processing.query.planner.core.index.ResolvedLookupIndexKeys;
import com.braintribe.model.processing.query.planner.core.index.StaticIndexKeys;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.queryplan.value.AggregateFunction;
import com.braintribe.model.queryplan.value.AggregationFunctionType;
import com.braintribe.model.queryplan.value.ConstantValue;
import com.braintribe.model.queryplan.value.IndexValue;
import com.braintribe.model.queryplan.value.QueryFunctionValue;
import com.braintribe.model.queryplan.value.StaticValue;
import com.braintribe.model.queryplan.value.TupleComponent;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.model.queryplan.value.ValueProperty;

/**
 * 
 */
public class ValueBuilder {

	public static AggregateFunction aggregateFunction(Value operand, AggregationFunctionType type) {
		AggregateFunction result = AggregateFunction.T.create();
		result.setOperand(operand);
		result.setAggregationFunctionType(type);

		return result;
	}

	public static QueryFunctionValue queryFunctionNoMappings(QueryFunction queryFunction) {
		return queryFunction(queryFunction, Collections.<Object, Value> emptyMap());
	}

	public static QueryFunctionValue queryFunction(QueryFunction queryFunction, Map<Object, Value> operandMappings) {
		QueryFunctionValue result = QueryFunctionValue.T.create();
		result.setQueryFunction(queryFunction);
		result.setOperandMappings(operandMappings);

		return result;
	}

	public static StaticValue staticValue(Object actualValue) {
		StaticValue result = StaticValue.T.create();
		result.setValue(actualValue);

		return result;
	}

	public static ValueProperty valueProperty(Value value, String propertyPath) {
		ValueProperty result = ValueProperty.T.create();

		result.setValue(value);
		result.setPropertyPath(propertyPath);

		return result;
	}

	public static TupleComponent tupleComponent(int index) {
		TupleComponent result = TupleComponent.T.create();
		result.setTupleComponentIndex(index);

		return result;
	}

	public static ConstantValue indexKeyValue(IndexKeys keys) {
		if (keys instanceof StaticIndexKeys) {
			return staticValue(((StaticIndexKeys) keys).keys);
		}

		if (keys instanceof ResolvedLookupIndexKeys) {
			ResolvedLookupIndexKeys rlik = (ResolvedLookupIndexKeys) keys;
			return indexValue(rlik.index.getIndexId(), indexKeyValue(rlik.keys));
		}

		// TODO CHAIN+RANGE add support for ResolvedMetricIndexKeys

		throw new RuntimeQueryPlannerException("Unsupported IndexKey " + keys + " of type: " + keys.getClass().getName());
	}

	private static IndexValue indexValue(String indexId, ConstantValue keys) {
		IndexValue result = IndexValue.T.create();
		result.setIndexId(indexId);
		result.setKeys(keys);

		return result;
	}

}
