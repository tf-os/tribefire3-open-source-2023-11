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

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.query.api.stringifier.QueryStringifierRuntimeException;
import com.braintribe.model.processing.query.stringifier.BasicQueryStringifierContext;
import com.braintribe.model.processing.query.stringifier.experts.AbstractQueryStringifier;
import com.braintribe.model.query.PropertyQuery;

public class PropertyQueryStringifier extends AbstractQueryStringifier<PropertyQuery, BasicQueryStringifierContext> {
	@Override
	public String stringify(PropertyQuery query, BasicQueryStringifierContext context) throws QueryStringifierRuntimeException {
		context.pushDefaultSourceType(propertyTypeSignature(query));
		try {
			return stringifyIt(query, context);
		} finally {
			context.popDefaultSourceType();
		}
	}

	private String propertyTypeSignature(PropertyQuery query) {
		PersistentEntityReference ref = query.getEntityReference();
		if (ref == null)
			return null;

		EntityType<?> ownerType = GMF.getTypeReflection().findEntityType(ref.getTypeSignature());
		if (ownerType == null)
			return null;

		Property property = ownerType.findProperty(query.getPropertyName());
		if (property == null)
			return null;

		GenericModelType pt = property.getType();
		if (pt.getTypeCode() == TypeCode.listType || pt.getTypeCode() == TypeCode.setType)
			pt = ((LinearCollectionType) pt).getCollectionElementType();

		return pt.getTypeSignature();
	}

	private String stringifyIt(PropertyQuery query, BasicQueryStringifierContext context) throws QueryStringifierRuntimeException {
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
