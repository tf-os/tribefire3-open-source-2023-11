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
package com.braintribe.model.query;

import java.util.Set;

import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.query.conditions.Condition;

/**
 * A {@link Query} that is used to encapsulate a entity type request. Therefore one must define the entity type signature
 * {@link #setEntityTypeSignature(String)}.
 */

public interface EntityQuery extends Query {

	EntityType<EntityQuery> T = EntityTypes.T(EntityQuery.class);

	// @formatter:off
	/** The type signature of the entity that should be queried.	 */
	String getEntityTypeSignature();
	void setEntityTypeSignature(String entityTypeSignature);

	@Deprecated
	void setFroms(Set<From> froms);
	@Deprecated
	Set<From> getFroms();

	@Deprecated
	QueryContext getQueryContext();
	@Deprecated
	void setQueryContext(QueryContext queryContext);
	// @formatter:on

	
	static EntityQuery create(String entityTypeSignature) {
		EntityQuery query = T.create();
		query.setEntityTypeSignature(entityTypeSignature);
		return query;
	}
	
	static EntityQuery create(EntityType<?> entityType) {
		return create(entityType.getTypeSignature());
	}
	
	@Override
	default EntityQuery where(Condition condition) {
		Query.super.where(condition);
		return this;
	}
	
	@Override
	default EntityQuery distinct() {
		Query.super.distinct();
		return this;
	}
	
	@Override
	default EntityQuery orderBy(Ordering ordering) {
		Query.super.orderBy(ordering);
		return this;
	}
	
	@Override
	default EntityQuery orderBy(Object orderValue) {
		Query.super.orderBy(orderValue);
		return this;
	}
	
	@Override
	default EntityQuery orderBy(OrderingDirection direction, Object orderValue) {
		Query.super.orderBy(direction, orderValue);
		return this;
	}
	
	@Override
	default EntityQuery paging(Paging paging) {
		Query.super.paging(paging);
		return this;
	}
	
	@Override
	default EntityQuery paging(int startIndex, int pageSize) {
		Query.super.paging(Paging.create(startIndex, pageSize));
		return this;
	}
	
	@Override
	default EntityQuery limit(int limit) {
		Query.super.paging(Paging.create(0, limit));
		return this;
	}
	
	@Override
	default EntityQuery tc(TraversingCriterion tc) {
		Query.super.tc(tc);
		return this;
	}
	
	@Override
	default EntityQuery restriction(Restriction restriction) {
		Query.super.restriction(restriction);
		return this;
	}

}
