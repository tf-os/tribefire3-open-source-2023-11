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
package com.braintribe.model.processing.query.stringifier.experts.query;

import com.braintribe.model.processing.query.api.stringifier.QueryStringifierRuntimeException;
import com.braintribe.model.processing.query.stringifier.BasicQueryStringifierContext;
import com.braintribe.model.processing.query.stringifier.experts.AbstractQueryStringifier;
import com.braintribe.model.query.PropertyQuery;

public class PropertyQueryStringifier extends AbstractQueryStringifier<PropertyQuery, BasicQueryStringifierContext> {
	@Override
	public String stringify(PropertyQuery query, BasicQueryStringifierContext context) throws QueryStringifierRuntimeException {
		StringBuilder queryString = new StringBuilder();

		if (query.getDistinct()) {
			queryString.append("distinct ");
		}
		queryString.append("property ");

		// Get type-signature (Default-Alias needed for null sources)
		final String propertyName = query.getPropertyName();
		context.setDefaultAliasName(propertyName);

		// Stringify type-signature of EntityReference of PropertyQuery
		queryString.append(context.escapeKeywords(propertyName));
		if (hasCondition(query) || hasOrdering(query)) {
			// Appending propertyAlias replaceTag
			queryString.append(" ").append(context.getReplaceAliasTag());
		}

		queryString.append(" of ");
		context.stringifyAndAppend(query.getEntityReference(), queryString);

		appendCondition(query, context, queryString);
		appendOrdering(query, context, queryString);
		appendPaging(query, context, queryString);

		// Return result
		context.ReplaceAliasTags(queryString);
		return queryString.toString();
	}
}
