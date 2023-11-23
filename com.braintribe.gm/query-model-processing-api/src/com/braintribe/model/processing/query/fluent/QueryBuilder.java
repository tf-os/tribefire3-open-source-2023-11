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
package com.braintribe.model.processing.query.fluent;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

/**
 * @deprecated Use {@link EntityQueryBuilder} for building {@link com.braintribe.model.query.EntityQuery}s or
 *             {@link PropertyQueryBuilder} for building {@link com.braintribe.model.query.PropertyQuery}
 */
@Deprecated
public class QueryBuilder extends EntityQueryBuilder {
	public static EntityQueryBuilder from(Class<? extends GenericEntity> clazz) {
		return QueryBuilder.from(clazz.getName());
	}

	public static EntityQueryBuilder from(EntityType<?> type) {
		return QueryBuilder.from(type.getTypeSignature());
	}

	public static EntityQueryBuilder from(String typeSignature) {
		return EntityQueryBuilder.from(typeSignature);
	}
}
