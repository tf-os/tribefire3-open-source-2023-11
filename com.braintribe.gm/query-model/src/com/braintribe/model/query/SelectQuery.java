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

import java.util.Arrays;
import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.query.conditions.Condition;

/**
 * A {@link Query} that encapsulates a request for entities based on a list of selections {@link #setSelections(List)}
 * defining items that are to be returned in a query result.
 */
public interface SelectQuery extends Query {

	EntityType<SelectQuery> T = EntityTypes.T(SelectQuery.class);

	void setFroms(List<From> froms);
	List<From> getFroms();

	/**
	 * Sets a list of selections. A selection is either a static value (String, Integer, Date or even
	 * {@link GenericEntity}) or an instance of {@link Operand}.
	 */
	void setSelections(List<Object> selections);
	List<Object> getSelections();

	@Deprecated
	QueryContext getQueryContext();
	@Deprecated
	void setQueryContext(QueryContext queryContext);

	GroupBy getGroupBy();
	void setGroupBy(GroupBy groupBy);

	Condition getHaving();
	void setHaving(Condition having);
	
	static SelectQuery create(From... froms) {
		SelectQuery query = SelectQuery.T.create();
		query.getFroms().addAll(Arrays.asList(froms));
		return query;
	}
	
	default SelectQuery select(Object... selections) {
		getSelections().addAll(Arrays.asList(selections));
		return this;
	}
	
	@Override
	default SelectQuery where(Condition condition) {
		Query.super.where(condition);
		return this;
	}
	
	@Override
	default SelectQuery distinct() {
		Query.super.distinct();
		return this;
	}
	
	@Override
	default SelectQuery orderBy(Ordering ordering) {
		Query.super.orderBy(ordering);
		return this;
	}

	@Override
	default SelectQuery orderBy(Object orderValue) {
		Query.super.orderBy(orderValue);
		return this;
	}
	
	@Override
	default SelectQuery orderBy(OrderingDirection direction, Object orderValue) {
		Query.super.orderBy(direction, orderValue);
		return this;
	}
	
	@Override
	default SelectQuery paging(Paging paging) {
		Query.super.paging(paging);
		return this;
	}
	
	@Override
	default SelectQuery paging(int startIndex, int pageSize) {
		Query.super.paging(Paging.create(startIndex, pageSize));
		return this;
	}
	
	@Override
	default SelectQuery limit(int limit) {
		Query.super.paging(Paging.create(0, limit));
		return this;
	}
	
	@Override
	default SelectQuery tc(TraversingCriterion tc) {
		Query.super.tc(tc);
		return this;
	}
	
	@Override
	default SelectQuery restriction(Restriction restriction) {
		Query.super.restriction(restriction);
		return this;
	}


}
