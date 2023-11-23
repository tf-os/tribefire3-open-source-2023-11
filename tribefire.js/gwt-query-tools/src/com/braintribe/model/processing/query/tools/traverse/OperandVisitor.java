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

import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.functions.JoinFunction;
import com.braintribe.model.query.functions.Localize;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.query.functions.aggregate.AggregateFunction;

/**
 * 
 */
@SuppressWarnings("unused")
public interface OperandVisitor {

	default void visitStaticValue(Object operand) {
		// NO OP
	}

	default void visit(PropertyOperand operand) {
		// NO OP
	}

	default void visit(JoinFunction operand) {
		// NO OP
	}

	default void visit(Localize operand) {
		// NO OP
	}

	default void visit(AggregateFunction operand) {
		// NO OP
	}

	default void visit(QueryFunction operand) {
		// NO OP
	}

	default void visit(Source operand) {
		// NO OP
	}

}
