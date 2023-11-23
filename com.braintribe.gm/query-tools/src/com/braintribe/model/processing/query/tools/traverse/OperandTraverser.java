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
package com.braintribe.model.processing.query.tools.traverse;

import java.util.function.Predicate;

import com.braintribe.model.query.Operand;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.functions.JoinFunction;
import com.braintribe.model.query.functions.Localize;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.query.functions.aggregate.AggregateFunction;

/**
 * 
 * @author peter.gazdik
 */
public class OperandTraverser {

	protected Predicate<Object> evalExcludedCheck;
	protected OperandVisitor operandVisitor;

	public static void traverse(Predicate<Object> evalExcludedCheck, OperandVisitor operandVisitor, Object operand) {
		if (operand instanceof Operand && !evalExcludedCheck.test(operand))
			new OperandTraverser(evalExcludedCheck, operandVisitor).traverseOperand(operand);
	}

	public OperandTraverser(Predicate<Object> evalExcludedCheck, OperandVisitor operandVisitor) {
		this.evalExcludedCheck = evalExcludedCheck;
		this.operandVisitor = operandVisitor;
	}

	public void traverseOperand(Object operand) {
		if (!(operand instanceof Operand) || evalExcludedCheck.test(operand)) {
			operandVisitor.visitStaticValue(operand);
			return;
		}

		if (operand instanceof PropertyOperand) {
			PropertyOperand po = (PropertyOperand) operand;
			if (po.getPropertyName() != null) {
				operandVisitor.visit(po);
			} else {
				operandVisitor.visit(po.getSource());
			}

		} else if (operand instanceof JoinFunction) {
			operandVisitor.visit((JoinFunction) operand);

		} else if (operand instanceof Localize) {
			operandVisitor.visit((Localize) operand);

		} else if (operand instanceof AggregateFunction) {
			operandVisitor.visit((AggregateFunction) operand);

		} else if (operand instanceof QueryFunction) {
			operandVisitor.visit((QueryFunction) operand);

		} else if (operand instanceof Source) {
			operandVisitor.visit((Source) operand);

		} else {
			throw new IllegalArgumentException("Unsupported operand: " + operand + " of type: " + operand.getClass().getName());
		}
	}
}
