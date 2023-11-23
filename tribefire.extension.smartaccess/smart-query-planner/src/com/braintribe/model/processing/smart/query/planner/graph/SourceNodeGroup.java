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
package com.braintribe.model.processing.smart.query.planner.graph;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.List;
import java.util.Set;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.processing.smart.query.planner.context.SmartQueryPlannerContext;
import com.braintribe.model.processing.smart.query.planner.core.combination.GroupSortingTools;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.ConversionWrapper;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.query.SimpleOrdering;
import com.braintribe.model.query.conditions.Condition;

/**
 * 
 */
public abstract class SourceNodeGroup {
	public final Set<EntitySourceNode> allNodes = newSet();

	public Set<EntitySourceNode> allSourceNodes() {
		return allNodes;
	}

	public abstract NodeGroupType nodeGroupType();

	public static abstract class SingleAccessGroup extends SourceNodeGroup {
		public final Set<Condition> conditions = newSet();
		public final Set<EntitySourceNode> polymorphicAssignmentNodes = newSet();
		public final IncrementalAccess access;
		// this value is set (not-null) for at most one SingleAccessGroup, and only iff that one is sorted
		public OrderAndPagination orderAndPagination;
		/* this value is set (not-null) for only such SingleAccessGroups which are not DQJ-ed to other groups. Also, it is only used iff
		 * there is no sort criterion which */
		public Integer batchSize;

		public SingleAccessGroup(IncrementalAccess access) {
			this.access = access;
		}

		public void disableBatching() {
			this.batchSize = null;
		}
	}

	public static class OrderAndPagination {
		public List<SimpleOrdering> delegatableOrderings = newList();
		// These orderings ensure that data has total order and therefore bulks can be used
		public List<SimpleOrdering> totalOrderings = newList();
		public Integer limit;
		public Integer offset;

		public boolean isPaginationSet() {
			return limit != null;
		}
	}

	public static class SingleSourceGroup extends SingleAccessGroup {
		public final EntitySourceNode sourceNode;

		public SingleSourceGroup(EntitySourceNode sourceNode) {
			super(sourceNode.getAccess());
			this.sourceNode = sourceNode;
			this.allNodes.add(sourceNode);

			if (sourceNode.isPolymorphicHierarchy()) {
				this.polymorphicAssignmentNodes.add(sourceNode);
			}
		}

		@Override
		public NodeGroupType nodeGroupType() {
			return NodeGroupType.singleSource;
		}
	}

	public static class SingleAccessCombinationGroup extends SingleAccessGroup {
		public final List<SingleSourceGroup> operands;
		public final Set<EntitySourceNode> dqjNodes = newSet();

		public SingleAccessCombinationGroup(List<SingleSourceGroup> operands) {
			super(operands.get(0).access);

			this.operands = operands;

			for (SingleSourceGroup operand: operands) {
				allNodes.addAll(operand.allNodes);
				polymorphicAssignmentNodes.addAll(operand.polymorphicAssignmentNodes);
			}
		}

		@Override
		public NodeGroupType nodeGroupType() {
			return NodeGroupType.singleAccessCombination;
		}
	}

	public static class DelegateJoinGroup extends SourceNodeGroup {
		public final SourceNodeGroup materializedGroup;
		public final SingleAccessGroup queryGroup;
		public final boolean isLeftJoin;

		/** Maps nodes from materializedGroup to the corresponding {@link CorrelationJoinInfo}. */
		public final Set<CorrelationJoinInfo> correlationInfos;

		public DelegateJoinGroup(SourceNodeGroup materializedGroup, SingleAccessGroup queryGroup, Set<CorrelationJoinInfo> correlationInfos,
				boolean isLeftJoin) {

			this.materializedGroup = materializedGroup;
			this.queryGroup = queryGroup;
			this.isLeftJoin = isLeftJoin;
			this.correlationInfos = correlationInfos;

			allNodes.addAll(materializedGroup.allNodes);
			allNodes.addAll(queryGroup.allNodes);
		}

		@Override
		public NodeGroupType nodeGroupType() {
			return NodeGroupType.delegateQueryJoin;
		}
	}

	public static class CombinationGroup extends SourceNodeGroup {
		public final Condition condition;
		public final List<SourceNodeGroup> operands;

		public CombinationGroup(Condition condition, Set<SourceNodeGroup> operands, SmartQueryPlannerContext context) {
			this.condition = condition;
			this.operands = GroupSortingTools.sortAlphabeticallyOrderedFirst(operands, context);

			for (SourceNodeGroup operand: operands) {
				allNodes.addAll(operand.allNodes);
			}
		}

		@Override
		public NodeGroupType nodeGroupType() {
			return NodeGroupType.combination;
		}
	}

	public static class CorrelationJoinInfo {
		public EntitySourceNode mNode;
		public String mDelegateProperty;
		public ConversionWrapper mConversion;

		public EntitySourceNode qNode;
		public String qDelegateProperty;
		public ConversionWrapper qConversion;

		public EntitySourceNode collectionNode;
		public SimpleValueNode inverseKeyCollectionNode;

		public JoinType joinType; // never null, default is inner if not specified

		@Override
		public String toString() {
			return "CorrelationJoinInfo[mNode: " + mNode + ", mProp: " + mDelegateProperty + "qNode: " + qNode + ", qProp: " +
					qDelegateProperty + ", JT: " + joinType + "]";
		}
	}

	public static enum NodeGroupType {
		singleSource,
		singleAccessCombination,
		delegateQueryJoin,
		combination,
	}
}
