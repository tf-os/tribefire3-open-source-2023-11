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

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.smart.meta.InverseKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.KeyPropertyAssignment;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmTypeKind;
import com.braintribe.model.processing.smart.query.planner.context.SmartQueryPlannerContext;
import com.braintribe.model.processing.smart.query.planner.graph.EntitySourceNode;
import com.braintribe.model.processing.smart.query.planner.graph.QueryPlanStructure;
import com.braintribe.model.processing.smart.query.planner.graph.SimpleValueNode;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNode;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.CorrelationJoinInfo;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.DelegateJoinGroup;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.SingleAccessGroup;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.SingleSourceGroup;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.DqjDescriptor;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityPropertyMapping;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.InverseKeyPropertyAssignmentWrapper;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.KeyPropertyAssignmentWrapper;
import com.braintribe.model.processing.smart.query.planner.tools.SmartQueryPlannerTools;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;
import com.braintribe.utils.lcd.NullSafe;

/**
 * A component of {@link NodeRecombiner} which combines nodes using {@link DelegateQueryJoin}.
 * 
 * @see KeyPropertyAssignment
 * @see InverseKeyPropertyAssignment
 */
class DelegateQueryJoiner {

	private static final Logger log = Logger.getLogger(DelegateQueryJoiner.class);
	
	private final SmartQueryPlannerContext context;
	private final QueryPlanStructure planStructure;

	public static void doDqjs(SmartQueryPlannerContext context) {
		new DelegateQueryJoiner(context).doDqjs();
	}

	private DelegateQueryJoiner(SmartQueryPlannerContext context) {
		this.context = context;
		this.planStructure = context.planStructure();
	}

	private void doDqjs() {
		doKeyPropertyJoinsDqjs();
		doSubTypeDqjs();
	}

	// ###################################
	// ## . Key-Property Joins DQJs . . ##
	// ###################################

	private void doKeyPropertyJoinsDqjs() {
		if (!planStructure.hasMultipleGroups())
			return;

		while (true) {
			boolean didSomething = false;

			for (SourceNodeGroup group: new GroupJoinOrderResolver(context).sortFromSmallest()) {
				do {
					group = doDqjIfEligibleGroupExists(group);

					if (group != null)
						didSomething = true;

				} while (group != null);

				if (didSomething)
					break;

			}

			if (!didSomething)
				return;
		}
	}

	private SourceNodeGroup doDqjIfEligibleGroupExists(SourceNodeGroup group) {
		/* Only optimization. We keep track of nodes not needing DQJ so we can skip them in "doDqj(...)" method */
		Set<EntitySourceNode> nodesWithouthDqj = newSet();

		for (EntitySourceNode node: group.allNodes) {
			SingleAccessGroup otherGroup = findOtherGroupForDqj(node, group);

			if (otherGroup != null) {
				context.orderAndPaging().notifyJoinedGroup(otherGroup);

				return doDqj(group, otherGroup, nodesWithouthDqj);

			} else {
				nodesWithouthDqj.add(node);
			}
		}

		return null;
	}

	private SingleAccessGroup findOtherGroupForDqj(EntitySourceNode node, SourceNodeGroup group) {
		/* Node regarding the second check - for collections (node.getType() is CollectionType), we never want to do the DQJ in this order
		 * (for now at least). */
		if (node.getKeyPropertyJoinMaster() != null /* && node.getType() instanceof EntityType */) {
			SingleAccessGroup result = findOtherGroupCandidate(group, node.getKeyPropertyJoinMaster());
			if (result != null)
				return result;
		}

		for (EntitySourceNode otherNode: node.getKeyPropertyJoins()) {
			SingleAccessGroup result = findOtherGroupCandidate(group, otherNode);
			if (result != null)
				return result;
		}

		return null;
	}

	private SingleAccessGroup findOtherGroupCandidate(SourceNodeGroup group, EntitySourceNode otherNode) {
		SourceNodeGroup otherGroup = planStructure.getNodeGroup(otherNode);

		if ((otherGroup instanceof SingleAccessGroup) && otherGroup != group)
			return (SingleAccessGroup) otherGroup;
		else
			return null;
	}

	private DelegateJoinGroup doDqj(SourceNodeGroup mGroup, SingleAccessGroup qGroup, Set<EntitySourceNode> nodesWithouthDqj) {
		Set<CorrelationJoinInfo> correlationJoinInfos = newSet();

		for (EntitySourceNode mNode: mGroup.allNodes) {
			if (nodesWithouthDqj.contains(mNode))
				continue;

			Set<EntitySourceNode> qNodes = findQNodesForDqj(mNode, qGroup);

			if (qNodes.isEmpty())
				continue;

			for (EntitySourceNode qNode: qNodes) {
				Collection<CorrelationJoinInfo> cjis = resolveKeyPropertyCjis(mNode, qNode);
				correlationJoinInfos.addAll(cjis);
			}
		}

		JoinType joinType = resolveJoinType(correlationJoinInfos);
		joinType = tmpEnsureInnerOrLeft(joinType, correlationJoinInfos);
		
		return planStructure.doDqj(mGroup, qGroup, correlationJoinInfos, joinType == JoinType.left);
	}

	/* This will not be needed once we support full outer joins. Right should be unreachable, as we make sure to always put the outer part
	 * of join as materialized (thus we have a left join out of it). */
	private JoinType tmpEnsureInnerOrLeft(JoinType joinType, Set<CorrelationJoinInfo> cjis) {
		if (joinType == JoinType.right || joinType == JoinType.full) {
			log.warn("Unsupported join type: " + joinType + ". CorrelationJoinInfos: " + cjis);
			return JoinType.inner;
		}
		
		return joinType;
	}

	private JoinType resolveJoinType(Set<CorrelationJoinInfo> correlationJoinInfos) {
		JoinType result = null;
		
		for (CorrelationJoinInfo cji: correlationJoinInfos) {
			if (result == null) {
				result  = cji.joinType;
			} else {
				if (result != cji.joinType) {
					log.warn("Unsupproted join type for CorrelationJoinInfo: " + cji);
					return JoinType.inner;
				}
			}
		}
		
		return result;
	}

	/**
	 * Finds all the nodes from <code>otherGroup</code> which can be DQJ-ed to <code>node</code>, because they are related using the
	 * {@link KeyPropertyAssignment} or {@link InverseKeyPropertyAssignment}.
	 */
	private Set<EntitySourceNode> findQNodesForDqj(EntitySourceNode mNode, SingleAccessGroup qGroup) {
		Set<EntitySourceNode> candidates = newSet(mNode.getKeyPropertyJoins());
		candidates.add(mNode.getKeyPropertyJoinMaster()); // may be null, but we do not bother

		Set<EntitySourceNode> result = newSet(qGroup.allNodes);
		result.retainAll(candidates);

		return result;
	}

	/**
	 * We know that given nodes are related using {@link KeyPropertyAssignment} or {@link InverseKeyPropertyAssignment}. That means, one of
	 * the two sides is the "join master" and the other is joined to this. This method returns the corresponding {@link CorrelationJoinInfo}
	 * instance.
	 */
	private Collection<CorrelationJoinInfo> resolveKeyPropertyCjis(EntitySourceNode mNode, EntitySourceNode qNode) {
		EntitySourceNode ownerNode;
		EntitySourceNode otherNode;
		JoinType joinType;

		boolean qIsOwner = mNode.getJoinMaster() == qNode;

		if (qIsOwner) {
			ownerNode = qNode;
			otherNode = mNode;
			joinType = SmartQueryPlannerTools.reverseJoinType(mNode.getJoinType());
			
		} else if (qNode.getJoinMaster() == mNode /* Just for sure while testing */) {
			ownerNode = mNode;
			otherNode = qNode;
			joinType = qNode.getJoinType();

		} else {
			throw new RuntimeException("TODO remove when tested. This should not happen!");
		}

		joinType = NullSafe.get(joinType, JoinType.inner);
		
		CollectionNodeData cnData = handleCollectionNode(ownerNode, otherNode);

		DqjDescriptor dqjDescriptor = otherNode.getDqjDescriptor();
		List<CorrelationJoinInfo> result = newList();

		for (String joinedProperty: dqjDescriptor.getJoinedEntityDelegatePropertyNames()) {
			CorrelationJoinInfo cji = new CorrelationJoinInfo();
			cji.mNode = mNode;
			cji.qNode = qNode;
			cji.joinType = joinType;

			cji.collectionNode = cnData.collectionNode;
			cji.inverseKeyCollectionNode = cnData.inverseKeyCollectionNode;

			if (qIsOwner) {
				cji.mDelegateProperty = joinedProperty;
				cji.qDelegateProperty = dqjDescriptor.getRelationOwnerDelegatePropertyName(joinedProperty);

				cji.mConversion = dqjDescriptor.getJoinedEntityPropertyConversion(joinedProperty);
				cji.qConversion = dqjDescriptor.getRelationOwnerPropertyConversion(joinedProperty);
			} else {
				cji.qDelegateProperty = joinedProperty;
				cji.mDelegateProperty = dqjDescriptor.getRelationOwnerDelegatePropertyName(joinedProperty);

				cji.qConversion = dqjDescriptor.getJoinedEntityPropertyConversion(joinedProperty);
				cji.mConversion = dqjDescriptor.getRelationOwnerPropertyConversion(joinedProperty);
			}

			if (cji.mNode != cnData.ignoredNode)
				cji.mNode.markDelegatePropertyForSelection(cji.mDelegateProperty, cji.mConversion);
			if (cji.qNode != cnData.ignoredNode)
				cji.qNode.markDelegatePropertyForSelection(cji.qDelegateProperty, cji.qConversion);

			result.add(cji);
		}

		return result;
	}

	/**
	 * In case of a node which has a collection joined (i.e. there is a collection in the delegate), we do not want to select this property
	 * (because for that collection we have a joined node, which is marked for selection). So we mark such node as ignored here.
	 * 
	 * @param ownerNode
	 *            - owner of the relationship on smart level
	 * @param otherNode
	 *            - other node, the one that is referenced to by the <tt>ownerNode</tt> on smart level
	 */
	private CollectionNodeData handleCollectionNode(EntitySourceNode ownerNode, EntitySourceNode otherNode) {
		DqjDescriptor dqjDescriptor = otherNode.getDqjDescriptor();
		CollectionNodeData cnData = new CollectionNodeData();

		if (dqjDescriptor instanceof InverseKeyPropertyAssignmentWrapper) {
			String otherDlgtProp = dqjDescriptor.getJoinedEntityDelegatePropertyNames().get(0);
			if (otherNode.getDelegateGmProperty(otherDlgtProp).getType().typeKind() == GmTypeKind.SET) {
				String ownerDlgtProp = dqjDescriptor.getRelationOwnerDelegatePropertyName(otherDlgtProp);
				cnData.inverseKeyCollectionNode = otherNode.markInverseKeyCollection(otherDlgtProp, ownerNode, ownerDlgtProp);
				cnData.ignoredNode = otherNode;
			}

			/* TODO FIX later (the check is correct, but we want to check in other way, possibly have the right nodeType for otherNode
			 * indicating this is a KPA case and not LPA) */
		} else if (dqjDescriptor instanceof KeyPropertyAssignmentWrapper) {
			if (otherNode.isCollection()) {
				/* This means if we have situation like: SmartPerson.smartCompanies, we return ownerNode (SmartPerson) so that we do not try
				 * to mark "companies" as delegate property to select for SmartPerson. */
				cnData.collectionNode = otherNode;
				cnData.ignoredNode = ownerNode;
			}
		}

		return cnData;
	}

	private static class CollectionNodeData {
		public EntitySourceNode collectionNode;
		public SimpleValueNode inverseKeyCollectionNode;

		public EntitySourceNode ignoredNode;
	}

	// ###################################
	// ## . . . . Sub-Type DQJs . . . . ##
	// ###################################

	/**
	 * Note this is not used right now. We would only do this, if we wanted to load the sub-type properties using the DQJ. For now, we leave
	 * it not-loaded, and it can be later done by the lazy-loader.
	 */
	protected void doSubTypeDqjs() {
		for (SourceNodeGroup group: planStructure.getAllGroups()) {
			for (EntitySourceNode sn: group.allSourceNodes()) {
				group = doSubTypeDqjs(sn, group);

				/* if our EntitySOurceNode is a map with entity-key, we apply the DQJs for this key as well (map-key nodes are not
				 * registered within the planStructure - it would require bigger changes, because we would also need groups for simple-value
				 * nodes, because those also can have map-keys) */
				group = doSubTypeDqjForMapKeyIfPossible(sn, group);

				/* also, we also have to check all the simpleCollectionJoins, if some of them are maps with entity-key */
				for (SimpleValueNode cj: sn.getSimpleCollectionJoins())
					group = doSubTypeDqjForMapKeyIfPossible(cj, group);
			}
		}
	}

	private SourceNodeGroup doSubTypeDqjForMapKeyIfPossible(SourceNode sn, SourceNodeGroup group) {
		EntitySourceNode mapKeyNode = sn.getMapKeyNode();
		if (mapKeyNode != null)
			group = doSubTypeDqjs(mapKeyNode, group);

		return group;
	}

	private SourceNodeGroup doSubTypeDqjs(EntitySourceNode sourceNode, SourceNodeGroup group) {
		if (sourceNode.isPolymorphicHierarchy() || sourceNode.getSubTypeNodes().isEmpty())
			// if the node gmType has no sub-types, we don't do anything
			return group;

		String keyProperty = findDelegateKeyProperty(sourceNode);

		for (EntitySourceNode subNode: sourceNode.getSubTypeNodes()) {
			Set<CorrelationJoinInfo> correlationJoinInfos = newSet();
			correlationJoinInfos.add(newSubTypeCji(sourceNode, subNode, keyProperty));

			group = planStructure.doDqj(group, new SingleSourceGroup(subNode), correlationJoinInfos, true);
		}

		return group;
	}

	private String findDelegateKeyProperty(EntitySourceNode sourceNode) {
		EntityPropertyMapping epm = sourceNode.resolveSmartPropertyMapping(GenericEntity.id);
		return epm.getDelegatePropertyName();
	}

	@SuppressWarnings("unused")
	private boolean isKeyProperty(EntityPropertyMapping epm) {
		// TODO check MD here, if we find some unique mandatory property
		return false;
	}

	private CorrelationJoinInfo newSubTypeCji(EntitySourceNode superNode, EntitySourceNode subNode, String keyProperty) {
		CorrelationJoinInfo cji = new CorrelationJoinInfo();
		cji.mNode = superNode;
		cji.qNode = subNode;
		cji.mDelegateProperty = keyProperty;
		cji.qDelegateProperty = keyProperty;

		cji.mNode.markDelegatePropertyForSelection(cji.mDelegateProperty);
		cji.qNode.markDelegatePropertyForSelection(cji.qDelegateProperty);

		return cji;
	}

}
