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
package com.braintribe.model.processing.smart.query.planner.context;

import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.SingleAccessGroup;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SelectQuery;

/**
 * 
 * @author peter.gazdik
 */
public class OrderAndPagingManager {

	private final SelectQuery query;
	private final Paging pagination;

	private SingleAccessGroup orderedGroup;

	public OrderAndPagingManager(SelectQuery query) {
		this.query = query;
		this.pagination = extractPagination();
	}

	private Paging extractPagination() {
		Restriction r = query.getRestriction();
		return r != null ? r.getPaging() : null;
	}

	public Ordering getOrdering() {
		return query.getOrdering();
	}

	public Paging getQueryPagination() {
		return pagination;
	}

	public void removePaginationDelegation() {
		if (paginationDelegated())
			orderedGroup.orderAndPagination.limit = orderedGroup.orderAndPagination.offset = null;
	}

	public boolean paginationDelegated() {
		return orderedGroup != null && orderedGroup.orderAndPagination.limit != null;
	}

	/**
	 * If we have an order-by in our query, only a set of the most significant order-by operands can be delegated, those
	 * that all belong to the same {@link SingleAccessGroup}. That group is set here as the ordered-group candidate.
	 * This can, however, be later removed, if the planner decides that it will not start with this group but with some
	 * other group and this one will be DQJ-ed to it. In that case, of course, the smart-evaluator has to do the
	 * sorting. This removing is done using {@link #notifyJoinedGroup(SingleAccessGroup)} method.
	 */
	public void setOrderedGroupCandiate(SingleAccessGroup orderedGroup) {
		this.orderedGroup = orderedGroup;
	}

	/** @see #setOrderedGroupCandiate(SingleAccessGroup) */
	public void notifyJoinedGroup(SingleAccessGroup group) {
		group.disableBatching();

		if (orderedGroup == group) {
			orderedGroup.orderAndPagination = null;
			orderedGroup = null;
		}
	}

	public SingleAccessGroup getOrderedGroup() {
		return orderedGroup;
	}

}
