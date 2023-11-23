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

import static com.braintribe.model.processing.smart.query.planner.tools.SmartQueryPlannerTools.isAssymetricJoinType;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.processing.smart.query.planner.SmartQueryPlannerException;
import com.braintribe.model.processing.smart.query.planner.context.SmartQueryPlannerContext;
import com.braintribe.model.processing.smart.query.planner.graph.EntitySourceNode;
import com.braintribe.model.processing.smart.query.planner.graph.QueryPlanStructure;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.DelegateJoinGroup;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.SingleAccessGroup;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.LinkPropertyAssignmentWrapper;
import com.braintribe.model.query.JoinType;

/**
 * 
 */
public class GroupJoinOrderResolver {

	private final SmartQueryPlannerContext context;
	private final QueryPlanStructure planStructure;
	private final GroupSizeEstimator groupSizeEstimator;
	private final Set<SourceNodeGroup> groups;

	private final Map<SourceNodeGroup, GroupPriorityDescriptor> priorityMapping = newMap();
	private final Set<SourceNodeGroup> groupsToSkip = newSet();

	private final Comparator<SourceNodeGroup> groupComparator = (g1, g2) -> priorityMapping.get(g1).compare(priorityMapping.get(g2));
	
	public GroupJoinOrderResolver(SmartQueryPlannerContext context) {
		this.context = context;
		this.planStructure = context.planStructure();
		this.groupSizeEstimator = new GroupSizeEstimator(context);
		this.groups = planStructure.getAllGroups();
	}

	/**
	 * TODO JAVADOC
	 */
	public List<SourceNodeGroup> sortFromSmallest() {
		createPriorityMapping();
		return sort(groups);
	}

	private List<SourceNodeGroup> sort(Collection<SourceNodeGroup> collection) {
		List<SourceNodeGroup> result = newList(collection);
		Collections.sort(result, groupComparator);

		return result;
	}

	private void createPriorityMapping() {
		for (SourceNodeGroup group: groups) {
			for (EntitySourceNode sourceNode: group.allNodes) {
				JoinType joinType = sourceNode.getJoinType();

				/* If our node is one of DQJed nodes, and the join type was 'left' or 'right', we want to make sure that the corresponding
				 * side of the join get's higher priority. See general description */

				/* TODO Add to general description: If we have a weak left or right join, this determines the order of the DQJ. If we have
				 * say A left join B, then we simply must take A as materialized, and DQJ B with isLeftJoin = true. (Full outer join will be
				 * implemented/discussed later.) We achieve this by noting the right side (in this case) as a special group with infinite
				 * cost. */

				if (sourceNode.getDqjDescriptor() != null && isAssymetricJoinType(joinType)) {
					boolean isLeftJoin = joinType == JoinType.left;
					
					if (sourceNode.getDqjDescriptor() instanceof LinkPropertyAssignmentWrapper) {
						EntitySourceNode joinMaster = sourceNode.getKeyPropertyJoinMaster();
						
						SourceNodeGroup masterGroup = planStructure.getNodeGroup(joinMaster);
						markRightGroupAsInfiniteCost(masterGroup, group, isLeftJoin);

						joinMaster = joinMaster.getJoinMaster();
						group = masterGroup;
						
						masterGroup = planStructure.getNodeGroup(joinMaster);
						markRightGroupAsInfiniteCost(masterGroup, group, isLeftJoin);
						
					} else {
						SourceNodeGroup masterGroup = planStructure.getNodeGroup(sourceNode.getKeyPropertyJoinMaster());
						markRightGroupAsInfiniteCost(masterGroup, group, isLeftJoin);
					}
				}
			}
		}

		for (SourceNodeGroup group: groups)
			priorityMapping.put(group, createPriorityMappingFor(group));
	}

	private void markRightGroupAsInfiniteCost(SourceNodeGroup masterGroup, SourceNodeGroup joinGroup, boolean isLeftJoin) {
		if (masterGroup == joinGroup)
			return;
		
		if (isLeftJoin)
			groupsToSkip.add(joinGroup);
		else
			groupsToSkip.add(masterGroup);
	}

	private SingleAccessGroup getSag(SourceNodeGroup group) {
		if (group instanceof DelegateJoinGroup)
			return getSag(((DelegateJoinGroup) group).materializedGroup);

		if (!(group instanceof SingleAccessGroup))
			throw new SmartQueryPlannerException("Planner error. Only SingleAccessGroups are expected at this point.");

		return (SingleAccessGroup) group;
	}

	private GroupPriorityDescriptor createPriorityMappingFor(SourceNodeGroup sourceNodeGroup) {
		SingleAccessGroup group = getSag(sourceNodeGroup);		

		long cost =  groupsToSkip.contains(sourceNodeGroup) ? Long.MAX_VALUE : estimateCost(group);
		
		return new GroupPriorityDescriptor(group, cost);
	}

	private long estimateCost(SourceNodeGroup group) {
		return groupSizeEstimator.estimateSize(group);
	}

	private class GroupPriorityDescriptor {
		final SingleAccessGroup group;
		final long cost;

		public GroupPriorityDescriptor(SingleAccessGroup group, long cost) {
			this.group = group;
			this.cost = cost;
		}

		public int compare(GroupPriorityDescriptor other) {
			long diff = cost - other.cost;

			if (diff == 0) {
				if (isSorted())
					return -1;
				else if (other.isSorted())
					return 1;

				return GroupSortingTools.compareLists(context.getCharStrings(group), context.getCharStrings(other.group));
			}

			return diff > 0 ? 1 : -1;
		}

		public boolean isSorted() {
			return group.orderAndPagination != null;
		}
	}
}
