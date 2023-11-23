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

import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.util.function.Predicate;

import com.braintribe.model.query.CascadedOrdering;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.SimpleOrdering;

/**
 * 
 */
public class OrderingTraverser extends OperandTraverser {

	private final OrderingVisitor orderingVisitor;

	public static void traverse(Predicate<Object> evalExcludedCheck, OrderingVisitor orderingVisitor, OperandVisitor operandVisitor,
			Ordering ordering) {

		if (ordering != null)
			new OrderingTraverser(evalExcludedCheck, orderingVisitor, operandVisitor).traverse(ordering);
	}

	public OrderingTraverser(Predicate<Object> evalExcludedCheck, OrderingVisitor orderingVisitor, OperandVisitor operandVisitor) {
		super(evalExcludedCheck, operandVisitor);

		this.orderingVisitor = orderingVisitor;
	}

	public void traverse(Ordering ordering) {
		if (ordering instanceof SimpleOrdering)
			traverse((SimpleOrdering) ordering);

		else if (ordering instanceof CascadedOrdering)
			traverse((CascadedOrdering) ordering);

		else
			throw new IllegalArgumentException("Unsupported ordering type of: " + ordering);
	}

	private void traverse(CascadedOrdering ordering) {
		for (SimpleOrdering simpleOrdering : nullSafe(ordering.getOrderings()))
			traverse(simpleOrdering);
	}

	private void traverse(SimpleOrdering simpleOrdering) {
		if (orderingVisitor == null || orderingVisitor.visit(simpleOrdering))
			traverseOperand(simpleOrdering.getOrderBy());
	}

}
