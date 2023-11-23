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
package com.braintribe.model.processing.query.stringifier.experts;

import java.util.List;

import com.braintribe.model.processing.query.api.stringifier.QueryStringifierRuntimeException;
import com.braintribe.model.processing.query.api.stringifier.experts.Stringifier;
import com.braintribe.model.processing.query.stringifier.BasicQueryStringifierContext;
import com.braintribe.model.query.conditions.AbstractJunction;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.Disjunction;

public class JunctionStringifier<J extends AbstractJunction> implements Stringifier<J, BasicQueryStringifierContext> {
	@Override
	public String stringify(J operand, BasicQueryStringifierContext context) throws QueryStringifierRuntimeException {
		StringBuilder queryString = new StringBuilder();
		queryString.append("(");

		List<Condition> operands = operand.getOperands();
		if (operands != null) {
			// Work through all operands
			for (int i = 0, l = operands.size(); i < l; i++) {
				if (i > 0) {
					// Add junction delimiter
					queryString.append(" ");
					queryString.append((operand instanceof Disjunction) ? "or" : "and");
					queryString.append(" ");
				}

				// Add junctions operands to queryString
				context.stringifyAndAppend(operands.get(i), queryString);
			}
		}

		queryString.append(")");
		return queryString.toString();
	}
}
