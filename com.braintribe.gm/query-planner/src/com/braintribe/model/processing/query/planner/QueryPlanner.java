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
package com.braintribe.model.processing.query.planner;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.eval.api.repo.Repository;
import com.braintribe.model.processing.query.planner.context.QueryPlannerContext;
import com.braintribe.model.processing.query.planner.core.QueryPlannerCore;
import com.braintribe.model.processing.query.tools.SelectQueryNormalizer;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.QueryPlan;

/**
 * A {@link SelectQuery} planner - builds a {@link QueryPlan} for given select query.
 * 
 * This class is thread-safe and concurrent as long as the provided {@link Repository repository} is thread-safe.
 */
public class QueryPlanner {

	private final Repository repository;
	private boolean ignorePartitions;

	public QueryPlanner(Repository repository) {
		this.repository = repository;
		this.ignorePartitions = repository.defaultPartition() != null;
	}

	/**
	 * Builds a {@link QueryPlan} for given {@link SelectQuery}.
	 */
	public QueryPlan buildQueryPlan(SelectQuery query) {
		/* We need this for (at least) the purpose of selecting collections. The normalizer replaces a selection of a
		 * collection with a join, thus forcing the result to have one row for each member of the collection, not just
		 * one row, where one slot of the tuple is the entire collection. This makes it compatible with hibernate. */
		query = new SelectQueryNormalizer(query, false, false) //
				.defaultPartition(defaultPartition()) //
				.mappedPropertyIndicator(this::isPropertyMapped)
				.normalize();

		QueryPlannerContext context = new QueryPlannerContext(query, repository);

		return new QueryPlannerCore(context, query).buildQueryPlan();
	}

	/**
	 * Ignoring partitions means ignoring {@link GenericEntity#partition} property of each entity, so in that case we
	 * actually want to do the replace.
	 */
	private String defaultPartition() {
		return ignorePartitions ? repository.defaultPartition() : null;
	}

	
	/** If ignoring partitions, we treat them as if they were not mapped in the query normalizer, thus their references are replaced. */
	private boolean isPropertyMapped(@SuppressWarnings("unused") String typeSignature, String property) {
		return ignorePartitions ? !GenericEntity.partition.equals(property) : true;
	}
	
	public void ignorePartitions(boolean ignorePartitions) {
		this.ignorePartitions = ignorePartitions;
	}

}
