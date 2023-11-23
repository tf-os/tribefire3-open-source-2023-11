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
package tribefire.extension.elasticsearch.model.api.request.doc.conditions;

import com.braintribe.model.generic.annotation.meta.Priority;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.elasticsearch.model.api.request.doc.Comparison;
import tribefire.extension.elasticsearch.model.api.request.doc.Operator;

public interface ValueComparison extends Comparison {

	EntityType<ValueComparison> T = EntityTypes.T(ValueComparison.class);

	@Priority(0.82d)
	Operator getOperator();
	void setOperator(Operator operator);

	/**
	 * An operand is either a static value (String, Integer, Date or an instance of Condition.
	 */
	@Priority(0.81d)
	Object getLeftOperand();
	void setLeftOperand(Object leftOperand);

	@Priority(0.8d)
	Object getRightOperand();
	void setRightOperand(Object rightOperand);

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

}
