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
package com.braintribe.model.processing.smart.query.planner.core.builder;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;

import com.braintribe.model.processing.query.planner.builder.TupleSetBuilder;
import com.braintribe.model.processing.smart.query.planner.context.SmartQueryPlannerContext;
import com.braintribe.model.queryplan.set.AggregatingProjection;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.model.queryplan.value.ValueType;

/**
 * @see TupleSetBuilder
 */
public class SelectionPlanBuilder {

	public static TupleSet projection(TupleSet tupleSet, List<Object> selections, SmartQueryPlannerContext context) {
		List<Value> values = newList();

		boolean aggregation = false;

		for (Object operand: selections) {
			Value convertOperand = context.convertOperand(operand);
			values.add(convertOperand);

			aggregation |= convertOperand.valueType() == ValueType.aggregateFunction;
		}

		Projection result = aggregation ? AggregatingProjection.T.createPlain() : Projection.T.createPlain();
		result.setOperand(tupleSet);
		result.setValues(values);

		return result;
	}

}
