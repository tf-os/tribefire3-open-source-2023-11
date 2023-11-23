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
package com.braintribe.model.processing.smart.query.planner.tools;

import static com.braintribe.model.processing.smart.query.planner.tools.SmartQueryPlannerTools.newMultiMap;

import java.util.Collection;

import com.braintribe.model.processing.smart.query.planner.context.SmartQueryPlannerContext;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.AbstractJunction;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.Negation;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.utils.collection.api.MultiMap;

/**
 * 
 */
public class CollectionConditionExpert {

	private final SmartQueryPlannerContext context;

	public CollectionConditionExpert(SmartQueryPlannerContext context) {
		this.context = context;
	}

	public MultiMap<Source, ValueComparison> findCollectionConditions(Collection<Condition> conditions) {
		MultiMap<Source, ValueComparison> result = newMultiMap();
		findFor(conditions, result);

		return result;
	}

	private void findFor(Collection<Condition> conditions, MultiMap<Source, ValueComparison> map) {
		for (Condition c: conditions)
			findFor(c, map);
	}

	private void findFor(Condition condition, MultiMap<Source, ValueComparison> map) {
		switch (condition.conditionType()) {
			case conjunction:
			case disjunction:
				findFor(((AbstractJunction) condition).getOperands(), map);
				return;
			case negation:
				findFor(((Negation) condition).getOperand(), map);
				return;
			case valueComparison:
				findFor((ValueComparison) condition, map);
				return;
			default:
				return;
		}
	}

	private void findFor(ValueComparison comparison, MultiMap<Source, ValueComparison> map) {
		switch (comparison.getOperator()) {
			case in:
				addSourceIfPossible(comparison, comparison.getRightOperand(), map);
				return;
			case contains:
				addSourceIfPossible(comparison, comparison.getLeftOperand(), map);
				return;
			default:
				return;
		}
	}

	private void addSourceIfPossible(ValueComparison comparison, Object operand, MultiMap<Source, ValueComparison> map) {
		if (!isPropertyOperand(operand))
			return;

		PropertyOperand po = (PropertyOperand) operand;

		map.put(po.getSource(), comparison);
	}

	private boolean isPropertyOperand(Object operand) {
		return operand instanceof PropertyOperand && !context.isEvaluationExclude(operand);
	}

}
