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
package com.braintribe.model.processing.query.planner.condition;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.List;

import com.braintribe.model.query.Operator;
import com.braintribe.model.query.conditions.AbstractJunction;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.query.conditions.Negation;
import com.braintribe.model.query.conditions.ValueComparison;

/**
 * @author peter.gazdik
 */
public class QueryConditionBuilder {

	public static ValueComparison valueComparison(Object leftOperand, Object rightOperand, Operator operator) {
		ValueComparison result = ValueComparison.T.create();

		result.setLeftOperand(leftOperand);
		result.setRightOperand(rightOperand);
		result.setOperator(operator);

		return result;
	}

	public static Negation negation(Condition operand) {
		Negation result = Negation.T.create();
		result.setOperand(operand);

		return result;
	}

	public static Conjunction conjunction(Condition... operands) {
		return junction(Conjunction.T.createPlain(), asList(operands));
	}

	public static Conjunction conjunction(List<Condition> operands) {
		return junction(Conjunction.T.createPlain(), operands);
	}

	public static Disjunction disjunction(Condition... operands) {
		return junction(Disjunction.T.createPlain(), asList(operands));
	}

	public static Disjunction disjunction(List<Condition> operands) {
		return junction(Disjunction.T.createPlain(), operands);
	}

	private static <T extends AbstractJunction> T junction(T junction, List<Condition> operands) {
		junction.setOperands(operands);
		return junction;
	}
}
