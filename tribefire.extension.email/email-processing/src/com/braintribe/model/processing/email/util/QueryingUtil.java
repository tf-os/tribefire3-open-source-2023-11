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
package com.braintribe.model.processing.email.util;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;

/**
 * Utility class related to querying
 * 
 *
 */
public class QueryingUtil {

	private QueryingUtil() {
		// This just pffers static convenience methods
	}

	public static final <T extends GenericEntity> T queryEntityByProperty(EntityType<T> entityType, String property, String propertyValue,
			PersistenceGmSession session) {

		// @formatter:off
		EntityQuery query = EntityQueryBuilder.from(entityType)
				.where()
				.property(property).eq(propertyValue)
				.done();
		// @formatter:on
		T entity = session.query().entities(query).unique();

		return entity;
	}

	public static final <T extends GenericEntity> T queryEntityByPropertyStrict(EntityType<T> entityType, String property, String propertyValue,
			PersistenceGmSession session) {

		// @formatter:off
		EntityQuery query = EntityQueryBuilder.from(entityType)
				.where()
				.property(property).eq(propertyValue)
				.done();
		// @formatter:on
		T entity = session.query().entities(query).unique();
		if (entity == null) {
			throw new GenericModelException("Could not find entity: '" + entityType.getTypeName() + "' with property: '" + property
					+ "' propertyValue: '" + propertyValue + "'");
		}

		return entity;
	}
}
