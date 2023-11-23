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

import java.util.Map;

import com.braintribe.model.processing.query.planner.RuntimeQueryPlannerException;
import com.braintribe.model.processing.query.planner.builder.ValueBuilder;
import com.braintribe.model.processing.query.planner.tools.EntitySignatureTools;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.query.functions.JoinFunction;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.query.functions.aggregate.AggregateFunction;
import com.braintribe.model.query.functions.aggregate.Average;
import com.braintribe.model.query.functions.aggregate.Count;
import com.braintribe.model.query.functions.aggregate.Max;
import com.braintribe.model.query.functions.aggregate.Min;
import com.braintribe.model.query.functions.aggregate.Sum;
import com.braintribe.model.queryplan.value.AggregationFunctionType;
import com.braintribe.model.queryplan.value.TupleComponent;
import com.braintribe.model.queryplan.value.Value;

/**
 * @author peter.gazdik
 */
@SuppressWarnings("deprecation")
class OperandConverter {

	private final QueryPlannerContext context;

	private boolean postAggregation = false;

	public OperandConverter(QueryPlannerContext context) {
		this.context = context;
	}

	/** @see QueryPlannerContext#noticePostAggregation() */
	public void noticePostAggregation() {
		postAggregation = true;
	}

	public Value convert(Object operand) {
		if (context.isStaticValue(operand))
			return ValueBuilder.staticValue(operand);

		if (postAggregation)
			return converOperandAsTupleComponent((Operand)operand);
		
		if (operand instanceof PropertyOperand)
			return convertPropertyOperand(operand);

		if (operand instanceof JoinFunction)
			return convertJoinFunction(operand);

		if (operand instanceof AggregateFunction)
			return convertAggregateFunction((AggregateFunction) operand);

		if (operand instanceof com.braintribe.model.query.functions.Count)
			return convertCount(operand);

		if (operand instanceof QueryFunction)
			return convertQueryFunction(operand);

		if (operand instanceof Source)
			return convertSource(operand);

		throw new RuntimeQueryPlannerException("Unsupported operand: " + operand + " of type: " + operand.getClass().getName());
	}

	private Value converOperandAsTupleComponent(Operand operand) {
		int index = context.aggregationManager().getTupleComponentIndexOf(operand);
		return ValueBuilder.tupleComponent(index);
	}

	private Value convertPropertyOperand(Object operand) {
		PropertyOperand propertyOperand = (PropertyOperand) operand;
		int index = context.sourceManager().indexForSource(propertyOperand.getSource());
		TupleComponent component = ValueBuilder.tupleComponent(index);
		String pName = propertyOperand.getPropertyName();

		return pName == null ? component : ValueBuilder.valueProperty(component, pName);
	}

	private Value convertJoinFunction(Object operand) {
		JoinFunction joinFunction = (JoinFunction) operand;
		int index = context.sourceManager().indexForJoinKey(joinFunction.getJoin());

		return ValueBuilder.tupleComponent(index);
	}

	private Value convertCount(Object operand) {
		com.braintribe.model.query.functions.Count count = (com.braintribe.model.query.functions.Count) operand;
		Value convertedOperand = convert(count.getPropertyOperand());

		return ValueBuilder.aggregateFunction(convertedOperand, AggregationFunctionType.count);
	}

	private Value convertAggregateFunction(AggregateFunction operand) {
		Value convertedOperand = convert(operand.getOperand());
		AggregationFunctionType type = resolveAggregationType(operand);

		return ValueBuilder.aggregateFunction(convertedOperand, type);
	}

	private AggregationFunctionType resolveAggregationType(AggregateFunction operand) {
		if (operand instanceof Count)
			return ((Count) operand).getDistinct() ? AggregationFunctionType.countDistinct : AggregationFunctionType.count;

		if (operand instanceof Sum)
			return AggregationFunctionType.sum;

		if (operand instanceof Min)
			return AggregationFunctionType.min;

		if (operand instanceof Max)
			return AggregationFunctionType.max;

		if (operand instanceof Average)
			return AggregationFunctionType.avg;

		throw new RuntimeQueryPlannerException("Unsupported aggregateFunction: " + operand + " of type: " + operand.getClass().getName());
	}

	private Value convertQueryFunction(Object operand) {
		if (operand instanceof EntitySignature) {
			String staticSignature = EntitySignatureTools.getStaticSignature((EntitySignature) operand);

			if (staticSignature != null)
				return ValueBuilder.staticValue(staticSignature);
		}

		QueryFunction queryFunction = (QueryFunction) operand;

		Map<Object, Value> operandMappings = context.noticeQueryFunction(queryFunction);

		return ValueBuilder.queryFunction(queryFunction, operandMappings);
	}

	private Value convertSource(Object operand) {
		int index = context.sourceManager().indexForSource((Source) operand);
		return ValueBuilder.tupleComponent(index);
	}

}
