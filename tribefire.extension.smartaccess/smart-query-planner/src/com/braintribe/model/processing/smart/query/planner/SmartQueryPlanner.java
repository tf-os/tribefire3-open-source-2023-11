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
package com.braintribe.model.processing.smart.query.planner;

import static com.braintribe.model.processing.smart.query.planner.tools.DelegateAsIsTools.buildDelegateAsIsPlan;
import static com.braintribe.model.processing.smart.query.planner.tools.DelegateAsIsTools.canDelegateAsIs;
import static com.braintribe.model.processing.smart.query.planner.tools.SmartQueryPlannerTools.computeResultComponentCount;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;
import java.util.Map;

import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.eval.api.function.QueryFunctionExpert;
import com.braintribe.model.processing.query.planner.builder.TupleSetBuilder;
import com.braintribe.model.processing.query.planner.builder.ValueBuilder;
import com.braintribe.model.processing.smart.query.planner.context.OrderAndPagingManager;
import com.braintribe.model.processing.smart.query.planner.context.SmartQueryPlannerContext;
import com.braintribe.model.processing.smart.query.planner.core.builder.ScalarPlanBuilder;
import com.braintribe.model.processing.smart.query.planner.core.builder.SelectionPlanBuilder;
import com.braintribe.model.processing.smart.query.planner.core.combination.NodeRecombiner;
import com.braintribe.model.processing.smart.query.planner.core.order.SmartOrderByProcessor;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.SingleAccessGroup;
import com.braintribe.model.processing.smart.query.planner.splitter.DisambiguatedQuery;
import com.braintribe.model.processing.smart.query.planner.splitter.SmartQuerySplitter;
import com.braintribe.model.processing.smart.query.planner.structure.ModelExpert;
import com.braintribe.model.processing.smart.query.planner.tools.SmartTupleSetBuilder;
import com.braintribe.model.processing.smartquery.eval.api.SmartConversionExpert;
import com.braintribe.model.query.CascadedOrdering;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SimpleOrdering;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.set.StaticSet;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.set.TupleSetType;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.model.smartqueryplan.SmartQueryPlan;

/**
 * @author peter.gazdik
 * @author dirk.scheffler
 */
public class SmartQueryPlanner {

	private Map<EntityType<? extends QueryFunction>, QueryFunctionExpert<?>> functionExperts;
	private Map<EntityType<? extends SmartConversion>, SmartConversionExpert<?>> conversionExperts;

	// ###################################################
	// ## . . . . . . . . Constructor . . . . . . . . . ##
	// ###################################################

	public SmartQueryPlanner() {
		// set modelExpert when it's thread-safe
	}

	// ###################################################
	// ## . . . . . . . Other Configuration . . . . . . ##
	// ###################################################

	public void setConversionExperts(Map<EntityType<? extends SmartConversion>, SmartConversionExpert<?>> conversionExperts) {
		this.conversionExperts = conversionExperts;
	}

	public void setFunctionExperts(Map<EntityType<? extends QueryFunction>, QueryFunctionExpert<?>> functionExperts) {
		this.functionExperts = functionExperts;
	}

	// ###################################################
	// ## . . . . . . . Query-Plan Building . . . . . . ##
	// ###################################################

	public SmartQueryPlan buildQueryPlan(SelectQuery query, ModelExpert modelExpert) {
		SmartQuerySplitter splitter = new SmartQuerySplitter(query, modelExpert);

		if (splitter.isEmpty())
			return emptyPlan();

		else if (!splitter.hasMultipleQueries())
			return buildPlanFor(splitter.singleQuery(), modelExpert);
		else
			return buildJointPlanForQueries(splitter, modelExpert);
	}

	private SmartQueryPlan emptyPlan() {
		return SmartTupleSetBuilder.queryPlan(TupleSetBuilder.staticSet(0), 0);
	}

	private SmartQueryPlan buildJointPlanForQueries(SmartQuerySplitter splitter, ModelExpert modelExpert) {
		List<SmartQueryPlan> plans = newList();

		for (DisambiguatedQuery query : splitter) {
			SmartQueryPlan queryPlan = buildPlanFor(query, modelExpert);
			plans.add(queryPlan);
		}

		return join(plans, splitter);
	}

	private SmartQueryPlan join(List<SmartQueryPlan> plans, SmartQuerySplitter splitter) {
		int totalComponents = 0;

		SmartQueryPlan nonEmptyPlan = null;
		List<TupleSet> sets = newList();
		for (SmartQueryPlan plan : plans) {
			TupleSet tupleSet = plan.getTupleSet();

			if (!isEmptySet(tupleSet)) {
				nonEmptyPlan = plan;
				totalComponents = Math.max(totalComponents, plan.getTotalComponentCount());
				sets.add(tupleSet);
			}
		}

		switch (sets.size()) {
			case 0:
				return first(plans); // all plans empty, just pick one of them
			case 1:
				return applyProjectionIfNeeded(nonEmptyPlan, splitter);
			default:
				return concatenationPlan(splitter, sets, totalComponents);
		}
	}

	private static boolean isEmptySet(TupleSet tupleSet) {
		return tupleSet.tupleSetType() == TupleSetType.staticSet && ((StaticSet) tupleSet).getValues().isEmpty();
	}

	private SmartQueryPlan concatenationPlan(SmartQuerySplitter splitter, List<TupleSet> sets, int totalComponents) {
		TupleSet tupleSet = SmartTupleSetBuilder.concatenation(sets, splitter.getSortCriteria(), splitter.getSelectionsCount());
		tupleSet = applyProjectionIfNeeded(tupleSet, splitter);
		tupleSet = applyPagination(tupleSet, splitter);

		return SmartTupleSetBuilder.queryPlan(tupleSet, totalComponents);
	}

	private SmartQueryPlan applyProjectionIfNeeded(SmartQueryPlan queryPlan, SmartQuerySplitter splitter) {
		if (splitter.getSortCriteria().isEmpty())
			return queryPlan;

		TupleSet adjustedTupleSet = applyProjection(queryPlan.getTupleSet(), splitter);

		return SmartTupleSetBuilder.queryPlan(adjustedTupleSet, queryPlan.getTotalComponentCount());
	}

	private TupleSet applyProjectionIfNeeded(TupleSet tupleSet, SmartQuerySplitter splitter) {
		if (splitter.getSortCriteria().isEmpty())
			return tupleSet;

		return applyProjection(tupleSet, splitter);
	}

	private TupleSet applyProjection(TupleSet tupleSet, SmartQuerySplitter splitter) {
		List<Value> values = newList();
		for (int i = 0; i < splitter.getOriginalSelectionsCount(); i++)
			values.add(ValueBuilder.tupleComponent(i));

		Projection result = Projection.T.createPlain();
		result.setOperand(tupleSet);
		result.setValues(values);

		return result;
	}

	private TupleSet applyPagination(TupleSet tupleSet, SmartQuerySplitter splitter) {
		Paging p = splitter.getPaging();

		return p != null ? TupleSetBuilder.paginatedSet(tupleSet, p.getPageSize(), p.getStartIndex(), 0, true) : tupleSet;
	}

	// ###################################################
	// ## . Query-Plan Building - Queryable sources . . ##
	// ###################################################

	private SmartQueryPlan buildPlanFor(DisambiguatedQuery dq, ModelExpert modelExp) {
		SmartQueryPlannerContext context = new SmartQueryPlannerContext(dq.query, modelExp, dq.fromMapping, functionExperts, conversionExperts);
		SmartQueryPlannerCore planner = new SmartQueryPlannerCore(context, dq);

		return planner.buildQueryPlan();
	}

	static class SmartQueryPlannerCore {

		private final SmartQueryPlannerContext context;
		private final DisambiguatedQuery dq;

		public SmartQueryPlannerCore(SmartQueryPlannerContext context, DisambiguatedQuery dq) {
			this.context = context;
			this.dq = dq;
		}

		public SmartQueryPlan buildQueryPlan() {
			if (context.conditionIsFalse())
				return planFor(TupleSetBuilder.staticSet(0));

			if (canDelegateAsIs(dq, context.modelExpert()))
				return buildDelegateAsIsPlan(dq);

			TupleSet tupleSet = processCondition();
			tupleSet = applyOrdering(tupleSet);
			tupleSet = applySelection(tupleSet);
			tupleSet = applyDistinct(tupleSet);
			tupleSet = applyPagination(tupleSet);

			return planFor(tupleSet);
		}

		private TupleSet processCondition() {
			NodeRecombiner.recombine(context);

			return ScalarPlanBuilder.build(context);
		}

		private TupleSet applyOrdering(TupleSet tupleSet) {
			Ordering ordering = dq.query.getOrdering();

			return orderingNeeded(ordering) ? SmartOrderByProcessor.applyOrdering(context, ordering, tupleSet) : tupleSet;
		}

		private boolean orderingNeeded(Ordering ordering) {
			if (ordering == null)
				return false;

			SingleAccessGroup orderedGroup = context.orderAndPaging().getOrderedGroup();
			if (orderedGroup == null)
				// if we were not able to delegate any ordering, we have to do it on the smart level
				return true;

			if (ordering instanceof SimpleOrdering) {
				/* if we've delegated some ordering (previous check) and we only had 1 ordering criterion -> done */
				return false;

			} else {
				/* Here we have to check whether or not we have already delegated all the orderings */
				List<SimpleOrdering> doneOrderings = orderedGroup.orderAndPagination.delegatableOrderings;
				return ((CascadedOrdering) ordering).getOrderings().size() > doneOrderings.size();
			}
		}

		private TupleSet applySelection(TupleSet tupleSet) {
			List<Object> selections = dq.query.getSelections();

			return SelectionPlanBuilder.projection(tupleSet, selections, context);
		}

		private TupleSet applyDistinct(TupleSet tupleSet) {
			return context.needsDistinct() ? TupleSetBuilder.distinct(tupleSet, computeResultComponentCount(tupleSet)) : tupleSet;
		}

		private TupleSet applyPagination(TupleSet tupleSet) {
			OrderAndPagingManager oapManager = context.orderAndPaging();

			Paging p = oapManager.getQueryPagination();
			if (p == null || oapManager.paginationDelegated())
				return tupleSet;

			return TupleSetBuilder.paginatedSet(tupleSet, p.getPageSize(), p.getStartIndex(), computeResultComponentCount(tupleSet));
		}

		private SmartQueryPlan planFor(TupleSet tupleSet) {
			return SmartTupleSetBuilder.queryPlan(tupleSet, context.getNumberOfAllocatedTupleTuplePositions());
		}
	}
}
