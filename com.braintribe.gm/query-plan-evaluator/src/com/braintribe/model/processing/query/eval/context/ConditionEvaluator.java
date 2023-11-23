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
package com.braintribe.model.processing.query.eval.context;

//import static com.braintribe.model.processing.query.eval.tools.QueryEvaluationTools.newMap;

//import java.util.Map;
//import java.util.regex.Pattern;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.tools.SelectQueryNormalizer;
import com.braintribe.model.query.Operator;
import com.braintribe.model.queryplan.filter.Condition;
import com.braintribe.model.queryplan.filter.Conjunction;
import com.braintribe.model.queryplan.filter.Contains;
import com.braintribe.model.queryplan.filter.Disjunction;
import com.braintribe.model.queryplan.filter.Equality;
import com.braintribe.model.queryplan.filter.FullText;
import com.braintribe.model.queryplan.filter.GreaterThan;
import com.braintribe.model.queryplan.filter.GreaterThanOrEqual;
import com.braintribe.model.queryplan.filter.ILike;
import com.braintribe.model.queryplan.filter.In;
import com.braintribe.model.queryplan.filter.InstanceOf;
import com.braintribe.model.queryplan.filter.LessThan;
import com.braintribe.model.queryplan.filter.LessThanOrEqual;
import com.braintribe.model.queryplan.filter.Like;
import com.braintribe.model.queryplan.filter.Negation;
import com.braintribe.model.queryplan.filter.Unequality;
import com.braintribe.model.queryplan.filter.ValueComparison;
import com.braintribe.model.queryplan.value.StaticValue;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.model.queryplan.value.ValueType;

/**
 * 
 */
public class ConditionEvaluator {

	private static final ConditionEvaluator instance = new ConditionEvaluator();

	protected ConditionEvaluator() {
	}

	public static ConditionEvaluator getInstance() {
		return instance;
	}

	public boolean evaluate(Tuple tuple, Condition condition, QueryEvaluationContext context) {
		switch (condition.conditionType()) {
			case conjunction:
				return evaluate(tuple, (Conjunction) condition, context);
			case contains:
				return evaluate(tuple, (Contains) condition, context);
			case disjunction:
				return evaluate(tuple, (Disjunction) condition, context);
			case equality:
				return evaluate(tuple, (Equality) condition, context);
			case fullText:
				return evaluate(tuple, (FullText) condition, context);
			case greater:
				return evaluate(tuple, (GreaterThan) condition, context);
			case greaterOrEqual:
				return evaluate(tuple, (GreaterThanOrEqual) condition, context);
			case ilike:
				return evaluate(tuple, (ILike) condition, context);
			case in:
				return evaluate(tuple, (In) condition, context);
			case instanceOf:
				return evaluate(tuple, (InstanceOf) condition, context);
			case less:
				return evaluate(tuple, (LessThan) condition, context);
			case lessOrEqual:
				return evaluate(tuple, (LessThanOrEqual) condition, context);
			case like:
				return evaluate(tuple, (Like) condition, context);
			case negation:
				return evaluate(tuple, (Negation) condition, context);
			case unequality:
				return evaluate(tuple, (Unequality) condition, context);
		}

		throw new RuntimeQueryEvaluationException("Unsupported condition " + condition + " of type: " + condition.conditionType());
	}

	// ########################################
	// ## . . . . . Value Ordering . . . . . ##
	// ########################################

	private boolean evaluate(Tuple tuple, Equality condition, QueryEvaluationContext context) {
		return equal(tuple, condition, context);
	}

	private boolean evaluate(Tuple tuple, Unequality condition, QueryEvaluationContext context) {
		return !equal(tuple, condition, context);
	}

	private boolean evaluate(Tuple tuple, GreaterThan condition, QueryEvaluationContext context) {
		return compare(tuple, condition, context) > 0;
	}

	private boolean evaluate(Tuple tuple, GreaterThanOrEqual condition, QueryEvaluationContext context) {
		return compare(tuple, condition, context) >= 0;
	}

	private boolean evaluate(Tuple tuple, LessThan condition, QueryEvaluationContext context) {
		return compare(tuple, condition, context) < 0;
	}

	private boolean evaluate(Tuple tuple, LessThanOrEqual condition, QueryEvaluationContext context) {
		return compare(tuple, condition, context) <= 0;
	}

	private boolean equal(Tuple tuple, ValueComparison comparison, QueryEvaluationContext context) {
		Value leftOperand = comparison.getLeftOperand();
		Value rightOperand = comparison.getRightOperand();

		Object left = context.resolveValue(tuple, leftOperand);
		Object right = context.resolveValue(tuple, rightOperand);

		if (left == null && right == null)
			return isSameNullValue(leftOperand, rightOperand);
		else
			return ConditionEvaluationTools.equal(left, right);
	}

	/**
	 * Usually we would say that null is equal to null, but in one special case - that the null was the result of an
	 * {@link EntityReference} resolution, such a null is not equal to other nulls. So we say that the nulls are equal
	 * as long as none of them is a static value, or that the static value is null directly (and not a reference which
	 * was resolved as null). <br/>
	 * 
	 * NOTE1: We assume the only non-null value that could be resolved as null is an entity reference.<br/>
	 * NOTE2: We do not handle the case when comparing two static values, because such case should be handled by
	 * {@link SelectQueryNormalizer}.
	 */
	private boolean isSameNullValue(Value leftOperand, Value rightOperand) {
		boolean leftIsConstant = leftOperand.valueType() == ValueType.staticValue;
		boolean rightIsConstant = rightOperand.valueType() == ValueType.staticValue;

		if (leftIsConstant)
			return isConstantNull((StaticValue) leftOperand);
		else if (rightIsConstant)
			return isConstantNull((StaticValue) rightOperand);
		else
			return true;
	}

	private boolean isConstantNull(StaticValue staticValue) {
		return staticValue.getValue() == null;
	}

	private int compare(Tuple tuple, ValueComparison comparison, QueryEvaluationContext context) {
		Object left = context.resolveValue(tuple, comparison.getLeftOperand());
		Object right = context.resolveValue(tuple, comparison.getRightOperand());

		return ConditionEvaluationTools.compare(left, right);
	}

	// ########################################
	// ## . . . . . . Strings . . . . . . . .##
	// ########################################

	// private static final int PATTERNS_CACHE_SIZE = 30;
	// private Map<String, Pattern> patterns = newMap(); // concurrency anybody?

	private boolean evaluate(Tuple tuple, Like condition, QueryEvaluationContext context) {
		String left = context.resolveValue(tuple, condition.getLeftOperand());
		String right = context.resolveValue(tuple, condition.getRightOperand());

		return ConditionEvaluationTools.like(left, right);
	}

	private boolean evaluate(Tuple tuple, ILike condition, QueryEvaluationContext context) {
		String left = context.resolveValue(tuple, condition.getLeftOperand());
		String right = context.resolveValue(tuple, condition.getRightOperand());

		return ConditionEvaluationTools.ilike(left, right);
	}

	// // TODO the cached version with Pattern (if wanted) cannot be used in GWT
	// private boolean like(String left, String right) {
	// // return convertToPattern(right).matcher(left).matches();
	// }
	// private Pattern convertToPattern(String pattern) {
	// Pattern result = patterns.get(pattern);
	//
	// if (result == null) {
	// String regex = ConditionEvaluationTools.convertToRegexPattern(pattern);
	// result = Pattern.compile(regex);
	//
	// if (patterns.size() > PATTERNS_CACHE_SIZE)
	// patterns.clear();
	//
	// patterns.put(pattern, result);
	// }
	//
	// return result;
	// }

	private boolean evaluate(Tuple tuple, FullText condition, QueryEvaluationContext context) {
		if (context.ignoreFulltextComparisons())
			return true;

		GenericEntity entity = context.resolveValue(tuple, condition.getLeftOperand());
		String text = context.resolveValue(tuple, condition.getRightOperand());

		return entity != null && FulltextComparator.matches(entity, text, context);
	}

	// ########################################
	// ## . . . Collection Membership . . . .##
	// ########################################

	private boolean evaluate(Tuple tuple, In condition, QueryEvaluationContext context) {
		Object left = context.resolveValue(tuple, condition.getLeftOperand());
		Object right = context.resolveValue(tuple, condition.getRightOperand());

		return ConditionEvaluationTools.contains(right, left, Operator.in);
	}

	private boolean evaluate(Tuple tuple, Contains condition, QueryEvaluationContext context) {
		Object left = context.resolveValue(tuple, condition.getLeftOperand());
		Object right = context.resolveValue(tuple, condition.getRightOperand());

		return ConditionEvaluationTools.contains(left, right, Operator.contains);
	}

	// ########################################
	// ## . . . . . . Reflection . . . . . . ##
	// ########################################

	private boolean evaluate(Tuple tuple, InstanceOf condition, QueryEvaluationContext context) {
		Object left = context.resolveValue(tuple, condition.getLeftOperand());
		String right = context.resolveValue(tuple, condition.getRightOperand());

		return left != null && ConditionEvaluationTools.instanceOf(left, right);
	}

	// #########################################
	// ## . . . . . Logical operators . . . . ##
	// #########################################

	private boolean evaluate(Tuple tuple, Conjunction condition, QueryEvaluationContext context) {
		for (Condition operand : condition.getOperands())
			if (!evaluate(tuple, operand, context))
				return false;

		return true;
	}

	private boolean evaluate(Tuple tuple, Disjunction condition, QueryEvaluationContext context) {
		for (Condition operand : condition.getOperands())
			if (evaluate(tuple, operand, context))
				return true;

		return false;
	}

	private boolean evaluate(Tuple tuple, Negation condition, QueryEvaluationContext context) {
		return !evaluate(tuple, condition.getOperand(), context);
	}

}
