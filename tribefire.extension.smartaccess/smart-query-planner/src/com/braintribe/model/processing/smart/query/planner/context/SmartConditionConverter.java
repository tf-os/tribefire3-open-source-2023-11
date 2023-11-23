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
package com.braintribe.model.processing.smart.query.planner.context;

import static com.braintribe.model.processing.smart.query.planner.tools.SmartQueryPlannerTools.isString;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.query.planner.RuntimeQueryPlannerException;
import com.braintribe.model.processing.query.planner.builder.ConditionBuilder;
import com.braintribe.model.processing.smart.query.planner.graph.EntitySourceNode;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.query.conditions.FulltextComparison;
import com.braintribe.model.query.conditions.Negation;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.queryplan.filter.Junction;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.model.smartqueryplan.filter.SmartFullText;

/**
 * Converts conditions to be evaluated by SmartAccess, cause they cannot be delegated.
 * 
 * @see #convert(Condition)
 */
class SmartConditionConverter {

	private final SmartQueryPlannerContext context;

	public SmartConditionConverter(SmartQueryPlannerContext context) {
		this.context = context;
	}

	/**
	 * Functionality for {@link SmartQueryPlannerContext#convertCondition(Condition)}
	 */
	public com.braintribe.model.queryplan.filter.Condition convert(Condition condition) {
		switch (condition.conditionType()) {
			case conjunction:
				return convert((Conjunction) condition);

			case disjunction:
				return convert((Disjunction) condition);

			case fulltextComparison:
				return convert((FulltextComparison) condition);

			case negation:
				return convert((Negation) condition);

			case valueComparison:
				return convert((com.braintribe.model.query.conditions.ValueComparison) condition);
		}

		throw new RuntimeQueryPlannerException("Unsupported condition: " + condition + " of type: " + condition.conditionType());
	}

	private Junction convert(Conjunction condition) {
		return ConditionBuilder.newConjunction(convertOperands(condition.getOperands()));
	}

	private Junction convert(Disjunction condition) {
		return ConditionBuilder.newDisjunction(convertOperands(condition.getOperands()));
	}

	private List<com.braintribe.model.queryplan.filter.Condition> convertOperands(List<Condition> operands) {
		return operands.stream() //
				.map(this::convert) //
				.collect(Collectors.toList());
	}

	private SmartFullText convert(FulltextComparison condition) {
		EntitySourceNode sourceNode = context.planStructure().getSourceNode(condition.getSource());
		GmEntityType smartType = sourceNode.getSmartGmType();

		List<Integer> stringPropertyPositions = newList();

		for (GmProperty property: nullSafe(smartType.getProperties())) {
			String smartProperty = property.getName();

			if (isString(property.getType()) && sourceNode.isSmartPropertyMapped(smartProperty)) {
				int propertyPosition = sourceNode.getSimplePropertyPosition(smartProperty);
				stringPropertyPositions.add(propertyPosition);
			}
		}

		SmartFullText result = SmartFullText.T.createPlain();
		result.setStringPropertyPositions(stringPropertyPositions);
		result.setText(condition.getText());

		return result;
	}

	private com.braintribe.model.queryplan.filter.Negation convert(Negation condition) {
		return ConditionBuilder.newNegation(convert(condition.getOperand()));
	}

	private com.braintribe.model.queryplan.filter.ValueComparison convert(ValueComparison condition) {
		Value left = context.convertOperand(condition.getLeftOperand());
		Value right = context.convertOperand(condition.getRightOperand());

		switch (condition.getOperator()) {
			case contains:
				// TODO not optimal
				// return ConditionBuilder.newContains(left, right);
				return ConditionBuilder.newEquality(left, right);
			case equal:
				return ConditionBuilder.newEquality(left, right);
			case greater:
				return ConditionBuilder.newGreaterThan(left, right);
			case greaterOrEqual:
				return ConditionBuilder.newGreaterThanOrEqual(left, right);
			case ilike:
				return ConditionBuilder.newILike(left, right);
			case in:
				// TODO not optimal
				// return ConditionBuilder.newIn(left, right);
				return ConditionBuilder.newEquality(left, right);
			case less:
				return ConditionBuilder.newLessThan(left, right);
			case lessOrEqual:
				return ConditionBuilder.newLessThanOrEqual(left, right);
			case like:
				return ConditionBuilder.newLike(left, right);
			case notEqual:
				return ConditionBuilder.newUnequality(left, right);
		}

		throw new RuntimeQueryPlannerException("Unsupported comparison operator of type: " + condition.getOperator());
	}
}
