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
package com.braintribe.model.access.hibernate.hql;

import org.hibernate.query.Query;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.From;
import com.braintribe.model.query.functions.Localize;

/**
 * This is only used when looking for referenced entities on delete.
 * 
 * @author peter.gazdik
 */
public class EntityHqlBuilder extends HqlBuilder<EntityQuery> {

	public EntityHqlBuilder(EntityQuery query) {
		super(query);
	}

	@Override
	public Query<GenericEntity> encode() {
		context.setReturnedType(typeReflection.<EntityType<?>> getType(query.getEntityTypeSignature()));

		// build select clause
		builder.append("select ");
		builder.append(context.aquireAlias(null));
		builder.append(' ');

		// build from and join clause
		encodeFroms();

		encodeCondition();

		encodeOrdering();

		return finishQuery();
	}

	private void encodeFroms() {
		From mainFrom = From.T.create();
		mainFrom.setEntityTypeSignature(query.getEntityTypeSignature());

		builder.append("from ");

		String alias = context.aquireAlias(null);
		encodeFrom(mainFrom, alias);
	}

	@Override
	protected void encodeLocalize(Localize localize) {
		String locale = localize.getLocale();
		if (locale == null)
			locale = context.getLocale();

		String escapedLocale = escapeStringLiteralBody(locale);

		// Useless. If localiedValues[locale] doesn't exist then the default won't work either, as we have already done a cross-join due to locale
		// We don't care though, this method should not be reached as EntityQuery is only used when deleting, so no localized strings there
		builder.append("case when '");
		builder.append(escapedLocale);
		builder.append("' in indices(");
		encodeOperand(localize.getLocalizedStringOperand(), false, false);
		builder.append(".localizedValues) then ");
		encodeOperand(localize.getLocalizedStringOperand(), false, false);
		builder.append(".localizedValues['");
		builder.append(escapedLocale);
		builder.append("'] else ");
		encodeOperand(localize.getLocalizedStringOperand(), false, false);
		builder.append(".localizedValues['default'] end");
	}

}