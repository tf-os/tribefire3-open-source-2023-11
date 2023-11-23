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

import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.smart.meta.CompositeInverseKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.CompositeKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.KeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.LinkPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.PropertyAssignment;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.builder.meta.MetaModelBuilder;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.query.tools.SourceTypeResolver;
import com.braintribe.model.processing.smart.query.planner.SmartQueryPlannerException;
import com.braintribe.model.processing.smart.query.planner.context.SmartQueryPlannerContext;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.CombinationGroup;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.CorrelationJoinInfo;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.DelegateJoinGroup;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.SingleAccessCombinationGroup;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.SingleAccessGroup;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.SingleSourceGroup;
import com.braintribe.model.processing.smart.query.planner.structure.ModelExpert;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EmUseCase;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityMapping;
import com.braintribe.model.processing.smart.query.planner.tools.SmartMappingTools;
import com.braintribe.model.processing.smart.query.planner.tools.SmartQueryPlannerTools;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.queryplan.set.CartesianProduct;

/**
 * Manages the graph of {@link SourceNode}s. As the planner analyzes he selection and condition of the query, the nodes are being grouped together
 * using the API provided by this class. In the end, we convert this structure into a query plan as follows:
 * <p>
 * Our graph consists of one or more connected components. We create a plan for each component and combine them using a {@link CartesianProduct}.
 * <p>
 */
public class QueryPlanStructure {

	private final SmartQueryPlannerContext context;
	private final ConditionedPropertyMarker conditionedPropertyMarker;

	private final Set<From> froms = newSet();
	private final Map<Source, SourceNode> sourceNodes = newMap();
	private final Map<EntitySourceNode, SourceNodeGroup> nodeToGroup = newMap();

	public QueryPlanStructure(SmartQueryPlannerContext context) {
		this.context = context;
		this.conditionedPropertyMarker = new ConditionedPropertyMarker(this, context);
	}

	public boolean hasMultipleGroups() {
		return getAllGroups().size() > 1;
	}

	public Set<SourceNodeGroup> getAllGroups() {
		return newSet(nodeToGroup.values());
	}

	public SourceNodeGroup getNodeGroup(SourceNode node) {
		if (node instanceof EntitySourceNode)
			return getNodeGroup((EntitySourceNode) node);
		else
			return getNodeGroup(((SimpleValueNode) node).getJoinMaster());
	}

	public SourceNodeGroup getNodeGroup(EntitySourceNode node) {
		return nodeToGroup.get(node);
	}

	// ##########################################
	// ## . . . Building structure nodes . . . ##
	// ##########################################

	/**
	 * Only called by {@link com.braintribe.model.processing.smart.query.planner.context.PlanStructureInitializer} and by this class itself.
	 */
	public <T extends SourceNode> T acquireSourceNode(Source source) {
		return acquireSourceNode(source, false);
	}

	public <T extends SourceNode> T acquireSourceNodeLeniently(Source source) {
		return acquireSourceNode(source, true);
	}

	private <T extends SourceNode> T acquireSourceNode(Source source, boolean lenient) {
		T result = (T) sourceNodes.get(source);

		if (result == null) {
			GenericModelType type = SourceTypeResolver.resolveType(source, false);
			GmType gmType = resolveGmType(source);

			switch (type.getTypeCode()) {
				case entityType:
					return (T) newSourceNode(source, (EntityType<?>) type, gmType, lenient);
				case listType:
				case setType:
				case mapType: {
					GenericModelType elementType = SourceTypeResolver.resolveCollectionElementType(type);

					if (elementType.getTypeCode() == TypeCode.entityType)
						return (T) newSourceNode(source, (EntityType<?>) elementType, gmType, lenient);
					else
						return (T) newSimpleValueNode((Join) source, gmType);
				}
				default:
					return result;
			}
		}

		return result;
	}

	private GmType resolveGmType(Source source) {
		if (source instanceof From) {
			return context.modelExpert().resolveSmartEntityType(((From) source).getEntityTypeSignature());

		} else {
			Join join = (Join) source;
			EntitySourceNode masterNode = acquireSourceNode(join.getSource());

			return context.modelExpert().getGmProperty(masterNode.getSmartGmType(), join.getProperty()).getType();
		}
	}

	/**
	 * @param lenient
	 *            means that is given source is in fact a {@link Join} and the joined property is not mapped, we simply return <tt>null</tt> here (and
	 *            also remember that this source was handled this way - see
	 *            {@link SmartQueryPlannerContext#isUnmappedSourceRelatedOperand(com.braintribe.model.query.Operand)})
	 */
	private EntitySourceNode newSourceNode(Source source, EntityType<?> entityType, GmType gmType, boolean lenient) {
		MappingDetails md = resolveMappingDetails(source, entityType, gmType);

		if (source instanceof From) {
			froms.add((From) source);
			return newSourceNode(source, md);
		}

		if (source instanceof Join) {
			Join join = (Join) source;
			String smartJoinProperty = join.getProperty();

			EntitySourceNode superNode = acquireSourceNode(join.getSource(), false);
			PropertyAssignment pa = superNode.resolveSmartPropertyAssignment(smartJoinProperty);
			if (pa == null)
				return handleUnmappedPropertyNode(superNode, join, lenient);

			EntitySourceNode result = newSourceNode(source, md);
			superNode.appendJoinNode(smartJoinProperty, pa, result);
			return result;
		}

		throw new IllegalArgumentException("Unsupported source type: " + source.getClass().getName());
	}

	private EntitySourceNode newSourceNode(Source source, MappingDetails md) {
		EntitySourceNode result = new EntitySourceNode(source, md.entityType, md.gmType, md.access, md.useCase, context);

		sourceNodes.put(source, result);
		nodeToGroup.put(result, new SingleSourceGroup(result));

		return result;
	}

	private EntitySourceNode handleUnmappedPropertyNode(EntitySourceNode superNode, Join join, boolean lenient) {
		if (lenient) {
			context.notifyUnmappedJoin(join);
			return null;
		} else {
			throw new SmartQueryPlannerException(
					"Property not mapped: '" + join.getProperty() + "' of '" + superNode.getSmartGmType().getTypeSignature());
		}
	}

	private MappingDetails resolveMappingDetails(Source source, EntityType<?> entityType, GmType gmType) {
		if (source instanceof From) {
			return new MappingDetails(context.getMapedAccess((From) source), null, entityType, gmType);

		} else {
			Join join = (Join) source;
			EntitySourceNode joinMasterNode = context.planStructure().acquireSourceNode(join.getSource());

			PropertyAssignment pa = joinMasterNode.resolveSmartPropertyAssignment(join.getProperty());

			if (pa instanceof KeyPropertyAssignment || pa instanceof CompositeInverseKeyPropertyAssignment
					|| pa instanceof CompositeKeyPropertyAssignment || pa instanceof LinkPropertyAssignment) {

				return mappingDetailsForWeaklyCoupledProperty(entityType, gmType, pa);

			} else /* strong join case */ {
				return new MappingDetails(joinMasterNode.getAccess(), joinMasterNode.getEmUseCase(), entityType, gmType);
			}
		}
	}

	/**
	 * If property type is not mapped, i assume the mapping contains the smart type to take; otherwise i take the original type, as mapping then
	 * contains the delegate type
	 */
	private MappingDetails mappingDetailsForWeaklyCoupledProperty(EntityType<?> smartPropertyEntityType, GmType smartPropertyGmType,
			PropertyAssignment pa) {

		ModelExpert modelExpert = context.modelExpert();
		GmEntityType smartGmEntityType = modelExpert.resolveSmartEntityType(smartPropertyEntityType.getTypeSignature());

		Map<IncrementalAccess, EntityMapping> mappings = modelExpert.resolveEntityMappingsIfPossible(smartGmEntityType, null);

		if (mappings == null)
			// standard smart reference use-case
			return mappingDetailsForUnmappedSmartEntity(smartPropertyEntityType, smartPropertyGmType, pa);

		if (mappings.size() > 1)
			/* we are DQJ-ing with a property which is of a shared type, so we want to make a recursive query (i.e. query SmartAccess itself, thus
			 * having all the mapped sources being concatenated.) */
			return new MappingDetails(context.getSmartDenotation(), EmUseCase.smartReference, smartPropertyEntityType, smartPropertyGmType);

		return new MappingDetails(first(mappings.values()).getAccess(), null, smartPropertyEntityType, smartPropertyGmType);
	}

	/**
	 * This method handles a special case when we have a property of some type (A), but our mapping references some more concrete type (SubA). In that
	 * case we set the mapping details data according to SubA, which is then later propagated to the {@link EntitySourceNode}, thus acting as if the
	 * type of the property was SubA. In case of a collection we also create the corresponding GmCollectionType.
	 */
	private MappingDetails mappingDetailsForUnmappedSmartEntity(EntityType<?> smartPropertyEntityType, GmType smartPropertyGmType,
			PropertyAssignment pa) {

		GmEntityType paSmartPropertyGmType = findSmartPropertyTypeFromMapping(pa);
		if (!smartPropertyEntityType.getTypeSignature().equals(paSmartPropertyGmType.getTypeSignature())) {
			smartPropertyEntityType = GMF.getTypeReflection().getEntityType(paSmartPropertyGmType.getTypeSignature());
			smartPropertyGmType = createSmartPropertyGmTypeIfNeeded(smartPropertyGmType, paSmartPropertyGmType);
		}

		return new MappingDetails(context.getSmartDenotation(), EmUseCase.smartReference, smartPropertyEntityType, smartPropertyGmType);
	}

	public static GmEntityType findSmartPropertyTypeFromMapping(PropertyAssignment pa) {
		return SmartMappingTools.getJoinedProperty(pa).propertyOwner();
	}

	private GmType createSmartPropertyGmTypeIfNeeded(GmType originalPropertyType, GmEntityType propertyEntityType) {
		switch (originalPropertyType.typeKind()) {
			case ENTITY:
				return propertyEntityType;
			case LIST:
				return MetaModelBuilder.listType(propertyEntityType);
			case SET:
				return MetaModelBuilder.setType(propertyEntityType);
			case MAP:
				return MetaModelBuilder.mapType(((GmMapType) originalPropertyType).getKeyType(), propertyEntityType);
			default:
				throw new SmartQueryPlannerException("Unexpected property type:" + originalPropertyType.getTypeSignature()
						+ ". Only entities and collection of entities are expected here.");
		}
	}

	static class MappingDetails {
		public IncrementalAccess access;
		public EmUseCase useCase;
		public EntityType<?> entityType;
		public GmType gmType;

		public MappingDetails(IncrementalAccess access, EmUseCase useCase, EntityType<?> entityType, GmType gmType) {
			this.access = access;
			this.useCase = useCase;
			this.entityType = entityType;
			this.gmType = gmType;
		}
	}

	/**
	 * Used for special nodes which do not correspond to a query {@link Source}. Right now the only use-case is the "Link" entity in case of
	 * {@link LinkPropertyAssignment}.
	 */
	public void registerAuxEntitySourceNode(EntitySourceNode node) {
		nodeToGroup.put(node, new SingleSourceGroup(node));
	}

	private SimpleValueNode newSimpleValueNode(Join join, GmType gmType) {
		SimpleValueNode result = new SimpleValueNode(join, gmType, context);

		sourceNodes.put(join, result);

		EntitySourceNode masterNode = acquireSourceNode(join.getSource());
		masterNode.appendSimpleNode(join.getProperty(), result);

		return result;
	}

	// ##########################################
	// ## . . . . . Node Recombination . . . . ##
	// ##########################################

	// Recombination Helpers

	public Set<From> getFroms() {
		return froms;
	}

	public <T extends SourceNode> T getSourceNode(Source source) {
		T result = (T) sourceNodes.get(source);
		if (result == null)
			throw new SmartQueryPlannerException("Unexpected source: " + source);

		return result;
	}

	public Set<SourceNodeGroup> getNodeGroups(Iterable<EntitySourceNode> nodes) {
		return SmartQueryPlannerTools.getAllNotNull(nodeToGroup, nodes);
	}

	// Group recombinations

	public void combineSingleAccessGroups(Collection<EntitySourceNode> nodes, Condition condition, EntitySourceNode dqjNode) {
		List<SingleSourceGroup> operands = newList();
		Set<Condition> conditions = newSet();
		Set<EntitySourceNode> dqjNodes = newSet();

		for (EntitySourceNode node : nodes) {
			SourceNodeGroup group = nodeToGroup.get(node);

			if (!(group instanceof SingleAccessGroup))
				throw new SmartQueryPlannerException("Cannot combine nodes, they are already part of groups from multiple accesses!");

			conditions.addAll(((SingleAccessGroup) group).conditions);

			if (group instanceof SingleSourceGroup) {
				operands.add((SingleSourceGroup) group);

			} else {
				dqjNodes.addAll(((SingleAccessCombinationGroup) group).dqjNodes);
				operands.addAll(((SingleAccessCombinationGroup) group).operands);
			}
		}

		SingleAccessCombinationGroup newGroup = new SingleAccessCombinationGroup(operands);
		newGroup.conditions.addAll(conditions);
		newGroup.dqjNodes.addAll(dqjNodes);
		if (condition != null)
			newGroup.conditions.add(condition);
		if (dqjNode != null)
			newGroup.dqjNodes.add(dqjNode);

		updateGroupMapping(newGroup);
	}

	// Right now, the only use-case for left join is the DQJ for a sub-type
	public DelegateJoinGroup doDqj(SourceNodeGroup materializedGroup, SingleAccessGroup queryGroup, Set<CorrelationJoinInfo> correlationJoinInfos,
			boolean isLeftJoin) {

		DelegateJoinGroup newGroup = new DelegateJoinGroup(materializedGroup, queryGroup, correlationJoinInfos, isLeftJoin);
		updateGroupMapping(newGroup);

		return newGroup;
	}

	public void combineDifferentAccessGroups(Collection<EntitySourceNode> nodes, Condition condition) {
		Set<SourceNodeGroup> operands = newSet();

		for (EntitySourceNode node : nodes)
			operands.add(nodeToGroup.get(node));

		CombinationGroup newGroup = new CombinationGroup(condition, operands, context);
		updateGroupMapping(newGroup);

		conditionedPropertyMarker.markProperties(condition);
	}

	private void updateGroupMapping(SourceNodeGroup newGroup) {
		for (EntitySourceNode node : newGroup.allNodes)
			nodeToGroup.put(node, newGroup);
	}

}
