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
package com.braintribe.model.processing.smart.query.planner.core.builder;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;

import com.braintribe.model.processing.query.planner.builder.TupleSetBuilder;
import com.braintribe.model.processing.smart.query.planner.SmartQueryPlannerException;
import com.braintribe.model.processing.smart.query.planner.context.SmartQueryPlannerContext;
import com.braintribe.model.processing.smart.query.planner.core.combination.DelegateQueryBuilder;
import com.braintribe.model.processing.smart.query.planner.core.combination.GroupSortingTools;
import com.braintribe.model.processing.smart.query.planner.core.combination.StaticTupleValuesBuilder;
import com.braintribe.model.processing.smart.query.planner.graph.QueryPlanStructure;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.CombinationGroup;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.DelegateJoinGroup;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.SingleAccessCombinationGroup;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.SingleAccessGroup;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.SingleSourceGroup;
import com.braintribe.model.queryplan.set.CartesianProduct;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;
import com.braintribe.model.smartqueryplan.set.DelegateQuerySet;

/**
 * Builds {@link TupleSet} according to given {@link QueryPlanStructure}. This {@linkplain TupleSet} is the result of the first phase of
 * evaluation, where all the data is being retrieved as a list of vectors, each vector consisting of simple values only (scalars).
 */
public class ScalarPlanBuilder {

	private final SmartQueryPlannerContext context;
	private final QueryPlanStructure planStructure;

	public static TupleSet build(SmartQueryPlannerContext context) {
		return new ScalarPlanBuilder(context).build();
	}

	private ScalarPlanBuilder(SmartQueryPlannerContext context) {
		this.context = context;
		this.planStructure = context.planStructure();
	}

	private TupleSet build() {
		List<TupleSet> tupleSets = newList();

		for (SourceNodeGroup group: GroupSortingTools.sortAlphabeticallyOrderedFirst(planStructure.getAllGroups(), context))
			tupleSets.add(buildPlanFor(group));

		if (tupleSets.size() == 1)
			return tupleSets.get(0);
		else
			return TupleSetBuilder.cartesianProduct(tupleSets);
	}

	private TupleSet buildPlanFor(SourceNodeGroup group) {
		switch (group.nodeGroupType()) {
			case delegateQueryJoin:
				return buildPlanFor((DelegateJoinGroup) group);

			case combination:
				return buildPlanFor((CombinationGroup) group);

			case singleAccessCombination:
				return buildPlanFor((SingleAccessCombinationGroup) group);

			case singleSource:
				return buildPlanFor((SingleSourceGroup) group);
		}

		throw new SmartQueryPlannerException("Unknown NodeGroupType: " + group.nodeGroupType());
	}

	private TupleSet buildPlanFor(DelegateJoinGroup group) {
		DelegateQueryBuilder queryBuilder = startBuildingQuery(group.queryGroup).correlationWhere(group.correlationInfos);

		DelegateQueryJoin result = DelegateQueryJoin.T.createPlain();
		result.setMaterializedSet(buildPlanFor(group.materializedGroup));
		result.setQuerySet(delegateQuerySet(group.queryGroup, queryBuilder));
		result.setJoinDisjunction(queryBuilder.getCorrelationDisjunction());
		result.setJoinRestrictions(queryBuilder.getCorrelationRestrictions());
		result.setIsLeftJoin(group.isLeftJoin);

		return result;
	}

	private TupleSet buildPlanFor(CombinationGroup group) {
		List<TupleSet> operands = newList();

		for (SourceNodeGroup sang: GroupSortingTools.sortAlphabeticallyOrderedFirst(group.operands, context))
			operands.add(buildPlanFor(sang));

		return TupleSetBuilder.filteredSet(asTupleSet(operands), context.convertCondition(group.condition));
	}

	private TupleSet asTupleSet(List<TupleSet> operands) {
		if (operands.size() == 1) {
			/* We might have just 1 operand if it is a DelegateQueryJoin (i.e. in such case it is possible to have just
			 * 1 operand without being able to delegate the condition) */
			return operands.get(0);

		} else {
			CartesianProduct cp = CartesianProduct.T.createPlain();
			cp.setOperands(operands);
			return cp;
		}
	}

	private TupleSet buildPlanFor(SingleAccessCombinationGroup group) {
		return delegateQuerySet(group, buildBasicQuery(group));
	}

	private TupleSet buildPlanFor(SingleSourceGroup group) {
		TupleSet result = buildStaticTupleIfPossible(group);
		if (result == null)
			result = delegateQuerySet(group, buildBasicQuery(group));

		return result;
	}

	private TupleSet buildStaticTupleIfPossible(SingleSourceGroup group) {
		return new StaticTupleValuesBuilder(context, group).build();
	}

	private DelegateQuerySet delegateQuerySet(SingleAccessGroup sag, DelegateQueryBuilder queryBuilder) {
		queryBuilder.finish();

		DelegateQuerySet result = DelegateQuerySet.T.createPlain();

		result.setDelegateAccess(sag.access);
		result.setDelegateQuery(queryBuilder.getQuery());
		result.setScalarMappings(queryBuilder.getScalarMappings());
		result.setBatchSize(sag.batchSize);

		return result;
	}

	private DelegateQueryBuilder startBuildingQuery(SingleAccessGroup group) {
		switch (group.nodeGroupType()) {
			case singleSource:
				return buildBasicQuery((SingleSourceGroup) group);
			case singleAccessCombination:
				return buildBasicQuery((SingleAccessCombinationGroup) group);
			default:
				throw new SmartQueryPlannerException("Unsupported SingleSourceGroup " + group + " of type: " + group.nodeGroupType());
		}
	}

	private DelegateQueryBuilder buildBasicQuery(SingleSourceGroup group) {
		return new DelegateQueryBuilder(context, group).from(group.sourceNode);
	}

	private DelegateQueryBuilder buildBasicQuery(SingleAccessCombinationGroup group) {
		return new DelegateQueryBuilder(context, group).from(group.allNodes);
	}

}
