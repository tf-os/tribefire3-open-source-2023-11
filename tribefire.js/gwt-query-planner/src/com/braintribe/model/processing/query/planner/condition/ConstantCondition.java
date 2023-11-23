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

import com.braintribe.model.generic.pseudo.GenericEntity_pseudo;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.conditions.ConditionType;
import com.braintribe.model.query.conditions.ValueComparison;

/**
 * 
 */
public class ConstantCondition extends GenericEntity_pseudo implements ValueComparison {

	public static final ConstantCondition TRUE = instance(0, 0);
	public static final ConstantCondition FALSE = instance(1, 0);

	private static ConstantCondition instance(Integer left, Integer right) {
		ConstantCondition result = new ConstantCondition();
		result.setLeftOperand(left);
		result.setRightOperand(right);
		result.setOperator(Operator.equal);

		return result;
	}

	public static ConstantCondition instance(boolean isTrue) {
		return isTrue ? TRUE : FALSE;
	}

	public ConstantCondition negate() {
		return this == TRUE ? FALSE : TRUE;
	}

	// ##################################################
	// ## . . . . . . Actual Implementation. . . . . . ##
	// ##################################################

	private Operator operator;
	private Object leftOperand;
	private Object rightOperand;

	@Override
	public Operator getOperator() {
		return operator;
	}

	@Override
	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	@Override
	public Object getLeftOperand() {
		return leftOperand;
	}

	@Override
	public void setLeftOperand(Object leftOperand) {
		this.leftOperand = leftOperand;
	}

	@Override
	public Object getRightOperand() {
		return rightOperand;
	}

	@Override
	public void setRightOperand(Object rightOperand) {
		this.rightOperand = rightOperand;
	}

	@Override
	public ConditionType conditionType() {
		return ConditionType.valueComparison;
	}
}
