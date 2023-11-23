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

import com.braintribe.model.processing.query.api.stringifier.QueryStringifierRuntimeException;
import com.braintribe.model.processing.query.stringifier.BasicQueryStringifierContext;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.SimpleOrdering;

public class SimpleOrderingStringifier extends AbstractOrderingStringifier<SimpleOrdering> {
	@Override
	public String stringify(SimpleOrdering simpleOrdering, BasicQueryStringifierContext context) throws QueryStringifierRuntimeException {
		StringBuilder queryString = new StringBuilder();

		// Stringify order by operand
		context.stringifyAndAppend(simpleOrdering.getOrderBy(), queryString);
		final OrderingDirection orderingDirection = simpleOrdering.getDirection();

		// Stringify ordering direction
		if (orderingDirection != null) {
			switch (orderingDirection) {
			case ascending: {
				queryString.append(" asc");
				break;
			}
			case descending: {
				queryString.append(" desc");
				break;
			}
			default: {
				break;
			}
			}
		}

		return queryString.toString();
	}
}
