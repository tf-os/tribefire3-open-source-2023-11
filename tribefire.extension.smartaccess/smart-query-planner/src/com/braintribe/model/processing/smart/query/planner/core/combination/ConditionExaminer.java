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
package com.braintribe.model.processing.smart.query.planner.core.combination;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.List;
import java.util.Map;

import com.braintribe.model.processing.query.planner.RuntimeQueryPlannerException;
import com.braintribe.model.processing.smart.query.planner.context.SmartQueryPlannerContext;
import com.braintribe.model.query.conditions.AbstractJunction;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.Negation;

/**
 * 
 */
class ConditionExaminer {

	private final ConditionNodeResolver conditionNodeResolver;
	private final Map<Condition, ConditionExaminationDescription> map = newMap();

	public static Map<Condition, ConditionExaminationDescription> examine(SmartQueryPlannerContext context) {
		return new ConditionExaminer(context).examine(context.conjunctionOperands());
	}

	// ###################################
	// ## . . . . Constructor . . . . . ##
	// ###################################

	private ConditionExaminer(SmartQueryPlannerContext context) {
		this.conditionNodeResolver = new ConditionNodeResolver(context);
	}

	// ###################################
	// ## . . . . Implementation . . . .##
	// ###################################

	private Map<Condition, ConditionExaminationDescription> examine(List<Condition> conditions) {
		for (Condition c: conditions)
			examine(c);

		return map;
	}

	private ConditionExaminationDescription examine(Condition c) {
		switch (c.conditionType()) {
			case conjunction:
			case disjunction:
				return examineJunction((AbstractJunction) c);

			case negation:
				return examineNegation((Negation) c);

			case fulltextComparison:
			case valueComparison:
				ConditionExaminationDescription set = conditionNodeResolver.resolveNodesForCondition(c);
				map.put(c, set);

				return set;
		}

		throw new RuntimeQueryPlannerException("Unsupported condition: " + c + " of type: " + c.conditionType());
	}

	private ConditionExaminationDescription examineNegation(Negation c) {
		ConditionExaminationDescription result = examine(c.getOperand());
		map.put(c, result);

		return result;
	}

	private ConditionExaminationDescription examineJunction(AbstractJunction junction) {
		ConditionExaminationDescription result = new ConditionExaminationDescription();

		for (Condition c: junction.getOperands()) {
			ConditionExaminationDescription operandCed = examine(c);
			result.affectedSourceNodes.addAll(operandCed.affectedSourceNodes);
			result.delegateable = result.delegateable && operandCed.delegateable;
		}

		map.put(junction, result);

		return result;
	}

}
