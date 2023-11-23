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

import java.util.Collection;
import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.context.ConditionEvaluationTools;
import com.braintribe.model.processing.query.planner.RuntimeQueryPlannerException;
import com.braintribe.model.processing.query.planner.tools.EntitySignatureTools;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.query.functions.EntitySignature;

/**
 * Evaluates a given
 */
public class ConstantConditionEvaluator {

	/**
	 * If given {@link ValueComparison} can be evaluated without a {@link Tuple} (e.g. both operands are static value)
	 */
	public ConstantCondition tryEvaluate(ValueComparison vc) {
		Object left = vc.getLeftOperand();
		Object right = vc.getRightOperand();

		switch (vc.getOperator()) {
			case contains:
				return evaluateContains(left, right, vc.getOperator());
			case in:
				return evaluateContains(right, left, vc.getOperator());
			case equal:
			case notEqual:
				return evaluateEquality(left, right, vc.getOperator() == Operator.equal);
			case greater:
			case greaterOrEqual:
			case less:
			case lessOrEqual:
				return evaluateInequality(left, right, vc.getOperator());
			case ilike:
				return evaluateLike(left, right, false);
			case like:
				return evaluateLike(left, right, true);
		}

		return null;
	}

	// ######################################
	// ## . . . . . . contains . . . . . . ##
	// ######################################

	private ConstantCondition evaluateContains(Object collectionObject, Object elementObject, Operator operator) {
		Object collection = resolveCollection(collectionObject);
		Object element = resolveObject(elementObject);

		return (collection != null && element != null) ? contains(collection, element, operator) : null;
	}

	private ConstantCondition contains(Object collection, Object element, Operator operator) {
		return ConstantCondition.instance(ConditionEvaluationTools.contains(collection, element, operator));
	}

	// ######################################
	// ## . . . . . . equality . . . . . . ##
	// ######################################

	private ConstantCondition evaluateEquality(Object left, Object right, boolean equal) {
		Object o1 = resolveObject(left);
		Object o2 = resolveObject(right);

		return (o1 != null && o2 != null) ? ConstantCondition.instance(equal == o1.equals(o2)) : null;
	}

	// ######################################
	// ## . . . . . . inequality . . . . . ##
	// ######################################

	private ConstantCondition evaluateInequality(Object left, Object right, Operator operator) {
		Object o1 = resolveObject(left);
		Object o2 = resolveObject(right);

		return (o1 != null && o2 != null) ? ConstantCondition.instance(holds(o1, o2, operator)) : null;
	}

	private boolean holds(Object o1, Object o2, Operator operator) {
		int cmp = ConditionEvaluationTools.compare(o1, o2);

		switch (operator) {
			case greater:
				return cmp > 0;
			case greaterOrEqual:
				return cmp >= 0;
			case less:
				return cmp < 0;
			case lessOrEqual:
				return cmp <= 0;
			default:
				throw new RuntimeQueryPlannerException("Inequality operator expected, but provided: " + operator);
		}
	}

	// ######################################
	// ## . . . . . . . like . . . . . . . ##
	// ######################################

	private ConstantCondition evaluateLike(Object textObject, Object patternObject, boolean caseSensitive) {
		String pattern = resolveString(patternObject);
		if (pattern == null)
			return null;

		// TODO actually, this conditions effectively checks if the other operand is not null - implement later
		if (pattern.matches("\\*+"))
			// if we have a pattern that matches everything
			return ConstantCondition.TRUE;

		String text = resolveString(textObject);

		return (text != null) ? matches(text, pattern, caseSensitive) : null;
	}

	private ConstantCondition matches(String text, String pattern, boolean caseSensitive) {
		if (!caseSensitive) {
			text = text.toLowerCase();
			pattern = pattern.toLowerCase();
		}

		return ConstantCondition.instance(text.matches(ConditionEvaluationTools.convertToRegexPattern(pattern)));
	}

	private Object resolveObject(Object o) {
		return o instanceof GenericEntity ? null : o;
	}

	private Object resolveCollection(Object o) {
		return o instanceof Collection || o instanceof Map ? o : null;
	}

	private String resolveString(Object o) {
		if (o instanceof String)
			return (String) o;

		if (o instanceof Enum)
			return ((Enum<?>) o).name();

		if (o instanceof EntitySignature) {
			String staticSignature = EntitySignatureTools.getStaticSignature((EntitySignature) o);
			if (staticSignature != null)
				return staticSignature;
		}

		return null;
	}

}
