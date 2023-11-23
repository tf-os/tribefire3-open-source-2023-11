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

import com.braintribe.model.processing.query.api.stringifier.experts.Stringifier;
import com.braintribe.model.processing.query.api.stringifier.experts.StringifierContext;
import com.braintribe.model.processing.query.stringifier.BasicQueryStringifierContext;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.Restriction;
import com.braintribe.utils.lcd.StringTools;

public abstract class AbstractQueryStringifier<Q extends Query, C extends BasicQueryStringifierContext> implements Stringifier<Q, C> {
	protected static void replaceAll(final StringBuilder builder, final String from, final String to) {
		int index = builder.indexOf(from);

		while (index != -1) {
			// Replace and move to the end of the replacement
			builder.replace(index, index + from.length(), to);
			index += to.length();

			index = builder.indexOf(from, index);
		}
	}

	protected boolean hasCondition(Q query) {
		Restriction restriction = query.getRestriction();
		return (restriction != null && restriction.getCondition() != null);
	}

	protected boolean hasOrdering(Q query) {
		return query.getOrdering() != null;
	}

	protected boolean appendOrdering(Q query, StringifierContext context, StringBuilder queryString) {
		Ordering ordering = query.getOrdering();
		if (ordering != null) {
			String orderString = context.stringify(ordering);
			if (!StringTools.isEmpty(orderString)) {
				queryString.append(" order by ");
				queryString.append(orderString);
				return true;
			}
		}

		return false;
	}

	protected boolean appendCondition(Q query, StringifierContext context, final StringBuilder queryString) {
		if (hasCondition(query)) {
			// Stringify condition of Query
			queryString.append(" where ");
			context.stringifyAndAppend(query.getRestriction().getCondition(), queryString);
			return true;
		}

		return false;
	}

	protected boolean appendPaging(Q query, StringifierContext context, final StringBuilder queryString) {
		Restriction restriction = query.getRestriction();
		if (restriction != null) {
			Paging paging = restriction.getPaging();
			if (paging != null) {
				context.stringifyAndAppend(paging, queryString);
				return true;
			}
		}

		return false;
	}
}
