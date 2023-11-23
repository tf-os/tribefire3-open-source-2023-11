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
package com.braintribe.model.processing.smart.query.planner.core.combination;

import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.processing.smart.query.planner.context.OrderAndPagingManager;
import com.braintribe.model.processing.smart.query.planner.context.SmartQueryPlannerContext;
import com.braintribe.model.processing.smart.query.planner.graph.EntitySourceNode;
import com.braintribe.model.processing.smart.query.planner.graph.QueryPlanStructure;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.SingleAccessGroup;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.smartqueryplan.set.DelegateQuerySet;

/**
 * 
 */
public class NodeRecombiner {

	private final SmartQueryPlannerContext context;
	private final List<Condition> conjunctionOperands;
	private final QueryPlanStructure planStructure;
	private final Map<Condition, ConditionExaminationDescription> conditionToNodes;

	public static void recombine(SmartQueryPlannerContext context) {
		new NodeRecombiner(context).recombine();
	}

	private NodeRecombiner(SmartQueryPlannerContext context) {
		this.context = context;
		this.conjunctionOperands = context.conjunctionOperands();
		this.planStructure = context.planStructure();

		this.conditionToNodes = ConditionExaminer.examine(context);
	}

	private void recombine() {
		groupNodesForExplicitJoins();
		applyDelegatableCondition();
		applyDelegatableOrdering();
		removeDelegatablePagingIfRemainingConditionsExist();
		applyDelegateSourceBatchingIfEligible();
		applyDelegateQueryJoining();
		applyRemainingConditions();
	}

	private void groupNodesForExplicitJoins() {
		for (From from: planStructure.getFroms())
			groupExplicitJoinNodesFor(planStructure.<EntitySourceNode> getSourceNode(from));
	}

	private void groupExplicitJoinNodesFor(EntitySourceNode sourceNode) {
		for (EntitySourceNode join: sourceNode.getExplicitJoins()) {
			joinGroupsFor(sourceNode, join);
			groupExplicitJoinNodesFor(join);
		}

		for (EntitySourceNode join: sourceNode.getKeyPropertyJoins()) {
			groupExplicitJoinNodesFor(join);
			groupIfSameAccessDqj(sourceNode, join);
		}
	}

	private void joinGroupsFor(EntitySourceNode sourceNode, EntitySourceNode join) {
		planStructure.combineSingleAccessGroups(Arrays.asList(sourceNode, join), null, null);
	}

	private void groupIfSameAccessDqj(EntitySourceNode sourceNode, EntitySourceNode join) {
		if (sourceNode.getAccess() != join.getAccess())
			return;
		
		if (join.getDqjDescriptor().getForceExternalJoin())
			return;
		
		
		planStructure.combineSingleAccessGroups(Arrays.asList(sourceNode, join), null, join);
	}

	private void applyDelegatableCondition() {
		Iterator<Condition> it = conjunctionOperands.iterator();

		while (it.hasNext())
			if (applyConditionIfDelegable(it.next()))
				it.remove();
	}

	private boolean applyConditionIfDelegable(Condition condition) {
		ConditionExaminationDescription ced = getExaminationDescriptionForCondition(condition);
		Set<EntitySourceNode> nodes = ced.affectedSourceNodes;

		if (nodes.isEmpty())
			throw new UnsupportedOperationException("This special case (conditions not related to any sources) is not expected here!");

		if (!ced.delegateable)
			return false;

		if (!allInOneAccess(nodes))
			return false;

		Set<SourceNodeGroup> nodeGroups = planStructure.getNodeGroups(nodes);

		if (nodeGroups.size() == 1) {
			SingleAccessGroup singleGroup = first(nodeGroups);
			singleGroup.conditions.add(condition);

		} else {
			/* TODO test with select from Person p, Company c, Address a where p.nameA = c.nameA and c.nameA = a.nameA
			 * (or something like that) */
			planStructure.combineSingleAccessGroups(nodes, condition, null);
		}

		return true;
	}

	private boolean allInOneAccess(Set<EntitySourceNode> nodes) {
		IncrementalAccess access = first(nodes).getAccess();

		for (EntitySourceNode node: nodes)
			if (node.getAccess() != access)
				return false;

		return true;
	}

	private void applyDelegatableOrdering() {
		OrderedGroupResolver.determineOrderedGroupIfExists(context);
	}

	/**
	 * If some condition was not delegated, we also do not delegate the pagination.
	 * 
	 * @see OrderedGroupResolver#canDelegatePaging
	 */
	private void removeDelegatablePagingIfRemainingConditionsExist() {
		if (!conjunctionOperands.isEmpty())
			context.orderAndPaging().removePaginationDelegation();
	}

	/**
	 * By SourceBatching we mean that sometimes it makes sense not to retrieve an entire {@link DelegateQuerySet} at
	 * once, but try only loading a bulk which might be enough. This might be the case iff there is a limit on the size
	 * of our result. Additionally, we either are not doing any sort of sorting, or the top-most materialized set is
	 * retrieved sorted, but cannot delegate the limit directly (because there might be other conditions which might
	 * cause some of the results not to be used, and imply the need to retrieve the next bulk).
	 * <p>
	 * In order for batching to work, we must of course have an absolute ordering on the query results, so that the next
	 * bulk can continue where we left off. Therefore, the {@link DelegateQueryBuilder} adds order by all the entity ids
	 * and primitive values for all the sources (see {@link DelegateQueryBuilder#computeTotalOrderingsIfNeeded}). If
	 * some entity has no id, no ordering is done and this batching information is removed from the group (using
	 * {@link SingleAccessGroup#disableBatching()}).
	 * <p>
	 * Here, we set the batchSize to either all {@link SingleAccessGroup}s or only the ordered-one, in case it does not
	 * delegate the pagination information (in case we have determined we will not need more than that number of
	 * results). Later, we remove this information for all {@link SingleAccessGroup}s which are not top-level
	 * materialized sets (i.e. those which are DQJ-ed to another group) - see
	 * {@link OrderAndPagingManager#notifyJoinedGroup(SingleAccessGroup)}, and also for those, where it is not possible
	 * to implement this correctly.
	 */
	private void applyDelegateSourceBatchingIfEligible() {
		OrderAndPagingManager orderAndPagingManager = context.orderAndPaging();
		Paging queryPaging = orderAndPagingManager.getQueryPagination();
		if (queryPaging == null)
			return;

		int batchSize = Math.max(SmartPlannerConstants.MIN_BATCH_SIZE, 2 * queryPaging.getPageSize());

		SingleAccessGroup orderedGroup = orderAndPagingManager.getOrderedGroup();
		if (orderedGroup != null) {
			if (!orderedGroup.orderAndPagination.isPaginationSet())
				orderedGroup.batchSize = batchSize;
			return;
		}

		for (SourceNodeGroup group: planStructure.getAllGroups())
			((SingleAccessGroup) group).batchSize = batchSize;
	}

	private void applyDelegateQueryJoining() {
		DelegateQueryJoiner.doDqjs(context);
	}

	private void applyRemainingConditions() {
		while (!conjunctionOperands.isEmpty()) {
			Condition c = first(conjunctionOperands);

			Set<EntitySourceNode> nodes = getNodesForCondition(c);
			nodes = findAllNodesConnectedTo(nodes);

			c = extractAllConditionsForNodes(nodes);

			/* Node that every condition here simply MUST bind together groups from different accesses, because
			 * otherwise such condition would be delegate-able and thus handled before. */
			planStructure.combineDifferentAccessGroups(nodes, c);
		}
	}

	private Set<EntitySourceNode> findAllNodesConnectedTo(Set<EntitySourceNode> nodes) {
		Set<EntitySourceNode> result = newSet();

		for (SourceNodeGroup sang: planStructure.getNodeGroups(nodes))
			result.addAll(sang.allNodes);

		return result;
	}

	private Condition extractAllConditionsForNodes(Set<EntitySourceNode> nodes) {
		List<Condition> matchingConditions = newList();

		for (Condition c: conjunctionOperands) {
			Set<EntitySourceNode> cNodes = getNodesForCondition(c);

			if (nodes.containsAll(cNodes))
				matchingConditions.add(c);
		}

		conjunctionOperands.removeAll(matchingConditions);

		if (matchingConditions.size() == 1) {
			return first(matchingConditions);

		} else {
			Conjunction result = Conjunction.T.createPlain();
			result.setOperands(matchingConditions);
			return result;
		}
	}

	private Set<EntitySourceNode> getNodesForCondition(Condition condition) {
		return conditionToNodes.get(condition).affectedSourceNodes;
	}

	private ConditionExaminationDescription getExaminationDescriptionForCondition(Condition condition) {
		return conditionToNodes.get(condition);
	}

}
