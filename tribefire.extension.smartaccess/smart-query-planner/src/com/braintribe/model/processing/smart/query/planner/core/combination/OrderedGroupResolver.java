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

import java.util.Arrays;
import java.util.List;

import com.braintribe.model.accessdeployment.smart.SmartAccess;
import com.braintribe.model.processing.smart.query.planner.SmartQueryPlannerException;
import com.braintribe.model.processing.smart.query.planner.context.OrderAndPagingManager;
import com.braintribe.model.processing.smart.query.planner.context.SmartQueryPlannerContext;
import com.braintribe.model.processing.smart.query.planner.graph.QueryPlanStructure;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNode;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.OrderAndPagination;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.SingleAccessGroup;
import com.braintribe.model.query.CascadedOrdering;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.SimpleOrdering;
import com.braintribe.model.query.Source;

/**
 * Component of {@link GroupJoinOrderResolver} which determines which group should be retrieved ordered. There is only
 * one group where we would also delegate the order-by criteria. What we do is we take the most significant order
 * criteria as long as they belong to one {@link SingleAccessGroup} and set the {@link OrderAndPagination} of that
 * group, which includes these criteria. In case our smart-query also contains pagination information, this might also
 * be set to be delegated. This depends on other conditions, see {@link #canDelegatePaging()} method.
 */
class OrderedGroupResolver {

	public static void determineOrderedGroupIfExists(SmartQueryPlannerContext context) {
		if (context.orderAndPaging().getOrdering() != null) {
			new OrderedGroupResolver(context).execute();
		}
	}

	private final SmartQueryPlannerContext context;
	private final QueryPlanStructure planStructure;
	private final OrderAndPagingManager orderAndPagingManager;
	private final Paging queryPaging;

	private OrderedGroupResolver(SmartQueryPlannerContext context) {
		this.context = context;
		this.planStructure = context.planStructure();
		this.orderAndPagingManager = context.orderAndPaging();
		this.queryPaging = orderAndPagingManager.getQueryPagination();
	}

	private void execute() {
		Ordering ordering = orderAndPagingManager.getOrdering();

		if (ordering instanceof SimpleOrdering) {
			determineOrderedGroup(Arrays.asList((SimpleOrdering) ordering));

		} else {
			determineOrderedGroup(((CascadedOrdering) ordering).getOrderings());
		}
	}

	private void determineOrderedGroup(List<SimpleOrdering> orderings) {
		SingleAccessGroup orderedGroup = null;

		for (SimpleOrdering ordering: orderings) {
			SingleAccessGroup group = findGroup(ordering);
			if (group == null)
				return;

			if (orderedGroup == null) {
				orderedGroup = group;
				orderedGroup.orderAndPagination = newOrderAndPagination();

				orderAndPagingManager.setOrderedGroupCandiate(orderedGroup);
			}

			if (orderedGroup == group)
				orderedGroup.orderAndPagination.delegatableOrderings.add(ordering);
			else
				return;
		}
	}

	private OrderAndPagination newOrderAndPagination() {
		OrderAndPagination paginationData = new OrderAndPagination();

		if (queryPaging != null && canDelegatePaging()) {
			paginationData.offset = queryPaging.getStartIndex();
			paginationData.limit = queryPaging.getPageSize();
		}

		return paginationData;
	}

	/**
	 * This method determines whether our delegate-query can also contain the paging information. Obviously, the result
	 * of a delegate query is then further processed by a {@link SmartAccess}, which might remove some tuples or
	 * multiply them by doing joins with other data, in which case the pagination cannot be deleted.
	 * <p>
	 * So, when can we delegate pagination? Only iff all the joins we are using (no matter what kind, if DQJ or joining
	 * using explicit condition) preserves the original number of tuples. For DQJ this happens if both correlation
	 * properties are simple (non-collections), the qProperty is unique and we are either doing a left-join, or both the
	 * mProperty and qProperty are mandatory. Similar holds for any possible join-condition.
	 * 
	 * Also, we have to consider "distinct" queries, in those case we also have to be careful, cause this keyword might
	 * cause the number of tuples to be reduced. E.g.
	 * <tt>select distinct p.smartPersonB.salary from SmartPersonA p limit 10</tt> cannot delegate the limit to the
	 * first query (for SmartPersonA), because the corresponding smartPersonB.salary might have multiple times the same
	 * value.
	 * <p>
	 * For now, to keep it simple, we only delegate pagination iff there is a single {@link SingleAccessGroup} and we
	 * can delegate all the conditions. In this case, we have no problem with distinct, as that is also taken care of in
	 * the delegate, thus we do not reduce number of tuples on smart level.
	 * 
	 * IMPORTANT As said, we can only delegate pagination if we also delegate all the conditions. We do not check that
	 * here (as it is not available), but rather remove the delegated pagination later - see
	 * {@link NodeRecombiner#removeDelegatablePagingIfRemainingConditionsExist}.
	 * 
	 * Note that in case we cannot delegate the query pagination, we might still use some optimization which uses
	 * pagination - see {@link NodeRecombiner#applyDelegateSourceBatchingIfEligible}.
	 */
	private boolean canDelegatePaging() {
		return planStructure.getAllGroups().size() == 1;
	}

	public SingleAccessGroup findGroup(SimpleOrdering ordering) {
		SingleAccessGroup result = null;

		for (Source s: context.getSourcesForOperand(ordering.getOrderBy())) {
			SingleAccessGroup group = findGroupFor(s);

			if (result != group) {
				if (result != null)
					/* might happen e.g. if someone orders by a concatenation of values coming from different delegates */
					return null;

				result = group;
			}
		}

		return result;
	}

	private SingleAccessGroup findGroupFor(Source s) {
		SourceNode node = planStructure.getSourceNode(s);
		SourceNodeGroup result = planStructure.getNodeGroup(node);

		if (!(result instanceof SingleAccessGroup))
			throw new SmartQueryPlannerException("Planner error. Only SingleAccessGroups are expected at this point.");

		return (SingleAccessGroup) result;
	}

}
