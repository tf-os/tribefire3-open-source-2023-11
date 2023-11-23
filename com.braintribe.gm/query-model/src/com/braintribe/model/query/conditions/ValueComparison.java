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
package com.braintribe.model.query.conditions;

import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.Operator;

/**
 * A {@link Comparison} that is used for comparing the operands leftOperand and rightOperand using the defined operator.
 */
public interface ValueComparison extends Comparison {

	EntityType<ValueComparison> T = EntityTypes.T(ValueComparison.class);

	Operator getOperator();
	void setOperator(Operator operator);

	/**
	 * An operand is either a static value (String, Integer, Date or even {@link GenericEntity}) or an instance of
	 * {@link Operand}.
	 */
	Object getLeftOperand();
	void setLeftOperand(Object leftOperand);

	/** @see #getLeftOperand() */
	Object getRightOperand();
	void setRightOperand(Object rightOperand);

	@Override
	default ConditionType conditionType() {
		return ConditionType.valueComparison;
	}

	static ValueComparison compare(Object op1, Operator operator, Object op2) {
		ValueComparison comparision = ValueComparison.T.create();
		comparision.setLeftOperand(op1);
		comparision.setRightOperand(op2);
		comparision.setOperator(operator);
		return comparision;
	}
	
	static ValueComparison eq(Object op1, Object op2) {
		return compare(op1, Operator.equal, op2);
	}
	
	static ValueComparison ne(Object op1, Object op2) {
		return compare(op1, Operator.notEqual, op2);
	}
	
	static ValueComparison gt(Object op1, Object op2) {
		return compare(op1, Operator.greater, op2);
	}
	
	static ValueComparison ge(Object op1, Object op2) {
		return compare(op1, Operator.greaterOrEqual, op2);
	}

	static ValueComparison lt(Object op1, Object op2) {
		return compare(op1, Operator.less, op2);
	}
	
	static ValueComparison le(Object op1, Object op2) {
		return compare(op1, Operator.lessOrEqual, op2);
	}
	
	static ValueComparison in(Object element, Set<?> set) {
		return compare(element, Operator.in, set);
	}
	
	static ValueComparison like(Object value, String pattern) {
		return compare(value, Operator.like, pattern);
	}
	
	static ValueComparison ilike(Object value, String pattern) {
		return compare(value, Operator.ilike, pattern);
	}
}
