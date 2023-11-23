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

import static com.braintribe.model.processing.query.planner.condition.QueryConditionBuilder.negation;
import static com.braintribe.model.processing.query.planner.condition.QueryConditionBuilder.valueComparison;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;
import java.util.function.Predicate;

import com.braintribe.model.processing.query.planner.RuntimeQueryPlannerException;
import com.braintribe.model.processing.query.planner.tools.EntitySignatureTools;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.conditions.AbstractJunction;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.query.conditions.FulltextComparison;
import com.braintribe.model.query.conditions.Negation;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.query.functions.EntitySignature;

/**
 *
 */
public class ConditionNormalizer {

	private final Predicate<Object> evalExcludedCheck;
	private final ConstantConditionEvaluator constantConditionEvaluator;

	public ConditionNormalizer(Predicate<Object> evalExcludedCheck) {
		this.evalExcludedCheck = evalExcludedCheck;
		this.constantConditionEvaluator = new ConstantConditionEvaluator();
	}

	/**
	 * Beware this might also return instance of {@link ConstantCondition}.
	 */
	public Condition normalize(Condition condition) {
		switch (condition.conditionType()) {
			case conjunction:
				return normalize((Conjunction) condition);
			case disjunction:
				return normalize((Disjunction) condition);
			case negation:
				return normalize((Negation) condition);
			case valueComparison:
				return normalize((ValueComparison) condition);
			case fulltextComparison:
				return normalize((FulltextComparison) condition);
			default:
				return condition;
		}
	}

	private Condition normalize(Conjunction condition) {
		return linearize(Conjunction.T.create(), condition.getOperands(), true);
	}

	private Condition normalize(Disjunction condition) {
		return linearize(Disjunction.T.create(), condition.getOperands(), false);
	}

	private Condition linearize(AbstractJunction junction, List<Condition> operands, boolean conjunction) {
		List<Condition> linearizedOperands = newList();

		for (Condition operand : operands) {
			operand = normalize(operand);
			if (operand == null)
				continue;

			if (operand instanceof ConstantCondition) {
				if (conjunction == (operand == ConstantCondition.FALSE))
					return operand;

				continue;
			}

			if ((conjunction && (operand instanceof Conjunction)) || (!conjunction && (operand instanceof Disjunction)))
				linearizedOperands.addAll(((AbstractJunction) operand).getOperands());
			else
				linearizedOperands.add(operand);
		}

		if (linearizedOperands.isEmpty())
			return null;

		if (linearizedOperands.size() == 1)
			return linearizedOperands.get(0);

		junction.setOperands(linearizedOperands);

		return junction;
	}

	private Condition normalize(Negation condition) {
		Condition operand = normalize(condition.getOperand());

		if (operand instanceof ConstantCondition)
			return ((ConstantCondition) operand).negate();
		else
			return operand == null ? null : negateCondition(operand);
	}

	/**
	 * This might be overridden by a sub-type if some special features are available there (e.g. we have special
	 * handling of VCs involving ConstantPropertyAssignemnt properties in SmartAccess.)
	 * 
	 * This method always returns an instance of {@link ValueComparison}, but overrides might also return something else
	 * (e.g. a {@link Disjunction}).
	 */
	protected Condition normalize(ValueComparison vc) {
		vc = newValueComparison(vc.getOperator(), resolve(vc.getLeftOperand()), resolve(vc.getRightOperand()));

		ConstantCondition constantCondition = constantConditionEvaluator.tryEvaluate(vc);

		if (constantCondition != null)
			return constantCondition;
		else
			return vc;
	}

	protected Condition normalize(FulltextComparison fc) {
		return "".equals(fc.getText()) ? ConstantCondition.TRUE : fc;
	}

	private Object resolve(Object value) {
		return isOperand(value) ? resolveOperand((Operand) value) : value;
	}

	private Object resolveOperand(Operand value) {
		if (value instanceof EntitySignature) {
			EntitySignature signatureFunction = (EntitySignature) value;
			String staticSignature = EntitySignatureTools.getStaticSignature(signatureFunction);

			if (staticSignature != null)
				return staticSignature;
		}

		return value;
	}

	/** Also ensures (if possible) that the {@link Operand} is on the left and the static value is on the right. */
	private ValueComparison newValueComparison(Operator operator, Object leftOperand, Object rightOperand) {
		if (shouldMirror(operator, leftOperand, rightOperand))
			return valueComparison(rightOperand, leftOperand, mirror(operator));
		else
			return valueComparison(leftOperand, rightOperand, operator);
	}

	private boolean shouldMirror(Operator operator, Object left, Object right) {
		return !isOperand(left) && isOperand(right) && isMirrorable(operator);
	}

	public static boolean isMirrorable(Operator operator) {
		switch (operator) {
			case equal:
			case greater:
			case greaterOrEqual:
			case less:
			case lessOrEqual:
			case notEqual:
				return true;
			default:
				return false;
		}
	}

	public static Operator mirror(Operator operator) {
		switch (operator) {
			case equal:
			case notEqual:
				return operator;
			case greater:
				return Operator.lessOrEqual;
			case greaterOrEqual:
				return Operator.less;
			case less:
				return Operator.greaterOrEqual;
			case lessOrEqual:
				return Operator.greater;
			default:
				return null;
		}
	}

	protected boolean isOperand(Object o) {
		return o instanceof Operand && !evalExcludedCheck.test(o);
	}

	// ##################################
	// ## . . . . . Negation . . . . . ##
	// ##################################

	private Condition negateCondition(Condition condition) {
		switch (condition.conditionType()) {
			case conjunction:
				return negate((Conjunction) condition);
			case disjunction:
				return negate((Disjunction) condition);
			case negation:
				return negate((Negation) condition);
			case fulltextComparison:
				return negation(condition);
			case valueComparison:
				return negate((ValueComparison) condition);
		}

		throw new RuntimeQueryPlannerException("Unsupported condition: " + condition + " of type: " + condition.conditionType());
	}

	private Condition negate(Conjunction condition) {
		return addNegatedOperands(Disjunction.T.create(), condition.getOperands());
	}

	private Condition negate(Disjunction condition) {
		return addNegatedOperands(Conjunction.T.create(), condition.getOperands());
	}

	private Condition addNegatedOperands(AbstractJunction junction, List<Condition> operands) {
		List<Condition> negatedOperands = newList();

		for (Condition operand : operands)
			negatedOperands.add(negateCondition(operand));

		junction.setOperands(negatedOperands);

		return junction;
	}

	private Condition negate(Negation condition) {
		return condition.getOperand();
	}

	private Condition negate(ValueComparison comparison) {
		switch (comparison.getOperator()) {
			case equal:
				return newComparison(comparison, Operator.notEqual);
			case notEqual:
				return newComparison(comparison, Operator.equal);
			case greater:
				return newComparison(comparison, Operator.lessOrEqual);
			case greaterOrEqual:
				return newComparison(comparison, Operator.less);
			case less:
				return newComparison(comparison, Operator.greaterOrEqual);
			case lessOrEqual:
				return newComparison(comparison, Operator.greater);
			default:
				return negation(comparison);
		}
	}

	private Condition newComparison(ValueComparison comparison, Operator operator) {
		return valueComparison(comparison.getLeftOperand(), comparison.getRightOperand(), operator);
	}

}
