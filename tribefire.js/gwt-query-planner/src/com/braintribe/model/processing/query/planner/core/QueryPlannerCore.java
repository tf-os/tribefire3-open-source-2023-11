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
package com.braintribe.model.processing.query.planner.core;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.sameSize;

import java.util.List;

import com.braintribe.model.processing.query.planner.builder.TupleSetBuilder;
import com.braintribe.model.processing.query.planner.condition.ConditionNormalizer;
import com.braintribe.model.processing.query.planner.condition.ConstantCondition;
import com.braintribe.model.processing.query.planner.context.QueryAggregationManager;
import com.braintribe.model.processing.query.planner.context.QueryOrderingManager;
import com.braintribe.model.processing.query.planner.context.QueryPlannerContext;
import com.braintribe.model.processing.query.planner.core.cross.simple.CrossJoinOrderResolver;
import com.braintribe.model.processing.query.planner.core.order.OrderByProcessor;
import com.braintribe.model.processing.query.planner.core.property.PropertyJoinAdder;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SimpleOrdering;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.ConditionType;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.queryplan.QueryPlan;
import com.braintribe.model.queryplan.set.AggregatingProjection;
import com.braintribe.model.queryplan.set.TupleSet;

/**
 * @author peter.gazdik
 */
public class QueryPlannerCore {

	private final QueryPlannerContext context;
	private final SelectQuery query;

	private AggregatingProjection aggregatingProjection;

	public QueryPlannerCore(QueryPlannerContext context, SelectQuery query) {
		this.context = context;
		this.query = query;
	}

	public QueryPlan buildQueryPlan() {
		Condition condition = extractCondition();

		TupleSet tupleSet = processCondition(condition);
		tupleSet = addJoins(tupleSet);
		tupleSet = applyAggregation(tupleSet);
		tupleSet = applyOrdering(tupleSet);
		tupleSet = applySelection(tupleSet);
		tupleSet = applyDistinct(tupleSet);
		tupleSet = applyPagination(tupleSet);

		QueryPlan queryPlan = QueryPlan.T.create();
		queryPlan.setTupleSet(tupleSet);

		return queryPlan;
	}

	private Condition extractCondition() {
		Restriction r = query.getRestriction();
		Condition c = r != null ? r.getCondition() : null;

		return c != null ? new ConditionNormalizer(context.evalExclusionCheck()).normalize(c) : null;
	}

	private TupleSet processCondition(Condition condition) {
		if (condition instanceof ConstantCondition) {
			if (condition == ConstantCondition.FALSE)
				return TupleSetBuilder.staticSet(0);

			condition = null;
		}

		CrossJoinOrderResolver orderResolver = new CrossJoinOrderResolver(context);
		return orderResolver.resolveCrossJoinOrder(condition);
	}

	private TupleSet addJoins(TupleSet tupleSet) {
		PropertyJoinAdder propertyJoinAdder = new PropertyJoinAdder(context, tupleSet);
		return propertyJoinAdder.addJoins();
	}

	private TupleSet applyOrdering(TupleSet tupleSet) {
		QueryOrderingManager om = context.orderingManager();
		List<SimpleOrdering> orderings = om.getRemainingOrderings();

		return orderings.isEmpty() ? tupleSet : OrderByProcessor.applyOrdering(context, orderings, om.getGroupValues(), tupleSet);
	}

	private TupleSet applyAggregation(TupleSet tupleSet) {
		if (!context.aggregationManager().hasAggregation())
			return tupleSet;

		tupleSet = applyAggregatedProjection(tupleSet);
		context.noticePostAggregation();
		tupleSet = applyHaving(tupleSet);

		return tupleSet;
	}

	private TupleSet applyAggregatedProjection(TupleSet tupleSet) {
		return aggregatingProjection = TupleSetBuilder.aggregatedProjection(tupleSet, context);
	}

	private TupleSet applyHaving(TupleSet tupleSet) {
		Condition having = query.getHaving();
		if (having == null)
			return tupleSet;

		List<Condition> conditions = having.conditionType() == ConditionType.conjunction ? ((Conjunction) having).getOperands() : asList(having);

		return TupleSetBuilder.filteredSet(tupleSet, conditions, context);
	}

	private TupleSet applySelection(TupleSet tupleSet) {
		QueryAggregationManager aggregationManager = context.aggregationManager();

		if (!aggregationManager.hasAggregation())
			// No aggregation happened yet, so we didn't do any projection yet, let's do it now
			return TupleSetBuilder.projection(tupleSet, query.getSelections(), context);

		if (sameSize(aggregatingProjection.getValues(), query.getSelections()))
			// Aggregated projection selected the right tuple components already, no need to do here
			return tupleSet;
		else
			// Aggregated projection selected extra tuple components beyond what we need for query selection
			return TupleSetBuilder.projectFirstNComponents(tupleSet, query.getSelections().size());
	}

	private TupleSet applyDistinct(TupleSet tupleSet) {
		return context.needsDistinct() ? TupleSetBuilder.distinct(tupleSet, 0) : tupleSet;
	}

	private TupleSet applyPagination(TupleSet tupleSet) {
		Restriction r = query.getRestriction();
		Paging p = r != null ? r.getPaging() : null;

		return p == null ? tupleSet : TupleSetBuilder.paginatedSet(tupleSet, p.getPageSize(), p.getStartIndex(), 0);
	}

}
