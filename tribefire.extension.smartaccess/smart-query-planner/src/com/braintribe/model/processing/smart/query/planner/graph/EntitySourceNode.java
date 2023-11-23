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

import static com.braintribe.model.processing.smart.query.planner.graph.SourceNodeType.subTypeJoin;
import static com.braintribe.model.processing.smart.query.planner.structure.adapter.ConversionWrapper.inverseOf;
import static com.braintribe.model.processing.smart.query.planner.tools.SmartQueryPlannerTools.collectionTypeOrNull;
import static com.braintribe.utils.lcd.CollectionTools2.acquireLinkedSet;
import static com.braintribe.utils.lcd.CollectionTools2.acquireMap;
import static com.braintribe.utils.lcd.CollectionTools2.newLinkedMap;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.smart.meta.CompositeInverseKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.CompositeKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.InverseKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.KeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.LinkPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.PropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmCollectionType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.smart.query.planner.SmartQueryPlannerException;
import com.braintribe.model.processing.smart.query.planner.context.SmartQueryPlannerContext;
import com.braintribe.model.processing.smart.query.planner.core.combination.DelegateQueryBuilder;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNodeGroup.SingleSourceGroup;
import com.braintribe.model.processing.smart.query.planner.structure.EntityHierarchyNode;
import com.braintribe.model.processing.smart.query.planner.structure.ModelExpert;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.ConstantPropertyMapping;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.ConversionWrapper;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.DiscriminatedHierarchy;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.DqjDescriptor;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EmUseCase;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityMapping;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityPropertyMapping;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityPropertyMapping.EpmType;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.InverseKeyPropertyAssignmentWrapper;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.KeyPropertyAssignmentWrapper;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.LinkPropertyAssignmentWrapper;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.LinkPropertyAssignmentWrapper.Level;
import com.braintribe.model.processing.smart.query.planner.tools.SmartQueryPlannerTools;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.Source;
import com.braintribe.utils.lcd.CollectionTools2;

/**
 * Represents a node for given smart-level entity.
 * 
 * There are three disjoint types of joins, see:
 * <ul>
 * <li>{@link #explicitJoins}</li>
 * <li>{@link #simpleCollectionJoins}</li>
 * <li>{@link #simpleInverseKeyCollectionJoins}</li>
 * </ul>
 * 
 * It keeps track of it's <tt>entity</tt> and <tt>simple collection</tt> joins.
 */
public final class EntitySourceNode extends SourceNode {

	private final ModelExpert modelExpert;
	private final EntityType<?> entityType;
	private final GmEntityType smartGmEntityType;
	private final GmEntityType delegateGmEntityType;
	private final EntityMapping entityMapping;
	private final EntitySourceNode superTypeNode;
	private final EntitySourceNode rootSuperTypeNode;
	private final IncrementalAccess access;
	private final EmUseCase emUseCase;

	/**
	 * Contains all property joins for entity properties.
	 * 
	 * @see #getExplicitJoins()
	 */
	private final Set<EntitySourceNode> explicitJoins = newSet();

	private DqjDescriptor dqjDescriptor; // describes from the keyPropertyJoinMaster
	private final Set<EntitySourceNode> keyPropertyJoins = newSet();
	/* The following variable is used in case this node has a keyPropertyJoinMaster, and represents a collection. This "counterpart" represents (more
	 * or less) the collection as it would look like, if it was simply mapped using standard mapping. For List/Set and Map with simple keys it means
	 * the collection is same as in delegate, for Map with entity keys, this would be a Map where key is a SmartEntity. This counterpart is also
	 * stored as a regular join of the keyPropertyJoinMaster, so that we can retrieve keys/values from the master */
	private SimpleValueNode keyPropertySimpleCounterpart;

	private final Set<SimpleValueNode> simpleCollectionJoins = newSet();
	private final Set<SimpleValueNode> simpleInverseKeyCollectionJoins = newSet();

	// TODO this should only be single entity joins, not collections
	private final Map<String, EntitySourceNode> allEntityJoins = newMap();

	private final Map<String, Map<ConversionWrapper, Integer>> propertyPositions = newMap();
	private int signaturePosition = -1;
	// sorted in prefix order when traversing from the root entity (i.e. every node precedes nodes of it's sub types)
	private final Map<String, EntitySourceNode> subTypeNodes = newLinkedMap();

	private final Map<String, Set<ConversionWrapper>> selectedDelegateProps = newMap();
	/** A virtual property is one that only exists on smart level, but isn't mapped to some delegate. */
	private final Set<String> selectedSmartVirtualProps = newSet();

	// TODO these maps should be in the model expert, qualified by an entity type
	private final Map<String, String> smartToDelegateProperty = newMap();
	private final Map<String, String> delegateToSmartProperty = newMap();

	private boolean selectSignature;

	/**
	 * Non-null properties: <tt>entityType</tt>,<tt>access</tt> and <tt>context</tt>
	 * 
	 * Nullable properties: <tt>source</tt>, <tt>smartGmType</tt>, and <tt>emUseCase</tt>
	 */
	public EntitySourceNode(Source source, EntityType<?> entityType, GmType smartGmType, IncrementalAccess access, EmUseCase emUseCase,
			SmartQueryPlannerContext context) {
		// the type for joins is set when these joins are being appended to the masters
		this(source, null, entityType, smartGmType, access, emUseCase, context, source instanceof From ? SourceNodeType.from : null);
	}

	/**
	 * This is directly used only when creating nodes for sub-types that have to be DQJ-ed for given node, or for map-key nodes.
	 */
	protected EntitySourceNode(Source source, EntitySourceNode superTypeNode, EntityType<?> entityType, GmType smartGmType, IncrementalAccess access,
			EmUseCase emUseCase, SmartQueryPlannerContext context, SourceNodeType nodeType) {

		super(source, smartGmType, context);

		this.superTypeNode = superTypeNode;
		this.rootSuperTypeNode = superTypeNode == null ? this : superTypeNode.rootSuperTypeNode;
		this.nodeType = nodeType;
		this.entityType = entityType;
		this.access = access;
		this.emUseCase = emUseCase;

		this.modelExpert = context.modelExpert();

		this.smartGmEntityType = modelExpert.resolveSmartEntityType(entityType.getTypeSignature());
		this.entityMapping = modelExpert.resolveEntityMapping(smartGmEntityType, access, emUseCase);
		this.delegateGmEntityType = entityMapping.getDelegateEntityType();
	}

	public IncrementalAccess getAccess() {
		return access;
	}

	/**
	 * This is currently only used for the "Link Entity" ({@link LinkPropertyAssignment} case), cause there the standard procedure fails (there is no
	 * smart type for the "Link Entity").
	 */
	private EntitySourceNode(Source source, EntitySourceNode superTypeNode, EntityType<?> entityType, GmEntityType smartGmEntityType,
			GmEntityType deleagateGmEntityType, IncrementalAccess access, SmartQueryPlannerContext context) {
		super(source, null, context);

		this.superTypeNode = superTypeNode;
		this.rootSuperTypeNode = superTypeNode == null ? this : superTypeNode.rootSuperTypeNode;

		this.modelExpert = context.modelExpert();
		this.entityType = entityType;

		this.smartGmEntityType = smartGmEntityType;
		this.delegateGmEntityType = deleagateGmEntityType;
		this.entityMapping = null;
		this.access = access;
		this.emUseCase = null;
	}

	// ######################################################
	// ## . . . . . . . . Simple Getters . . . . . . . . . ##
	// ######################################################

	@Override
	public GmEntityType getSmartGmType() {
		return smartGmEntityType;
	}

	public EntityType<?> getSmartEntityType() {
		return entityType;
	}

	public EmUseCase getEmUseCase() {
		return emUseCase;
	}

	public boolean isPolymorphicHierarchy() {
		return entityMapping != null && entityMapping.isPolymorphicAssignment();
	}

	public DiscriminatedHierarchy getDiscriminatorHierarchy() {
		return entityMapping.getDiscriminatedHierarchy();
	}

	public GmEntityType getDelegateGmType() {
		return delegateGmEntityType;
	}

	public GmProperty getDelegateGmProperty(String delegateProperty) {
		return modelExpert.getGmProperty(delegateGmEntityType, delegateProperty);
	}

	/**
	 * This is used when grouping {@link SingleSourceGroup}s together for these joins and when building delegate queries by
	 * {@link DelegateQueryBuilder}.
	 */
	public Collection<EntitySourceNode> getExplicitJoins() {
		return explicitJoins;
	}

	public Collection<SimpleValueNode> getSimpleCollectionJoins() {
		return simpleCollectionJoins;
	}

	public Collection<SimpleValueNode> getSimpleUnmappedCollectionJoins() {
		return simpleInverseKeyCollectionJoins;
	}

	public EntitySourceNode getKeyPropertyJoinMaster() {
		return nodeType.isExplicitJoin() ? null : joinMaster;
	}

	public Collection<EntitySourceNode> getKeyPropertyJoins() {
		return keyPropertyJoins;
	}

	public DqjDescriptor getDqjDescriptor() {
		return dqjDescriptor;
	}

	public SimpleValueNode getKeyPropertySimpleCounterpart() {
		return keyPropertySimpleCounterpart;
	}

	/**
	 * Returns an entity (i.e. not a simple collection) join node. The join node may be both explicit, or keyProperty join. See this class'
	 * {@link EntitySourceNode javadoc}.
	 */
	public EntitySourceNode getEntityJoin(String smartProperty) {
		return allEntityJoins.get(smartProperty);
	}

	public String delegateForSmartProperty(String smartProperty) {
		String result = smartToDelegateProperty.get(smartProperty);

		if (result == null) {
			markSmartPropertyHelper(smartProperty, false);
			result = smartToDelegateProperty.get(smartProperty);
		}

		return result;
	}

	// ######################################################
	// ## . Initialization and recombination phase methods .#
	// ######################################################

	public void markSignatureForSelection() {
		if (isPolymorphicHierarchy())
			markDiscriminatorProperties();
		else
			selectSignature = true;
	}

	/** Only called by PlanStructureInitializer */
	@Override
	public void markNodeForSelection() {
		if (retrieveEntireNode)
			return;

		retrieveEntireNode = true;

		EntityHierarchyNode hierarchyNode = resolveHierarchyRootedAtThis();

		if (isPolymorphicHierarchy()) {
			markPolymorphicHierarchy(hierarchyNode);

		} else {
			markSignatureIfNeeded(hierarchyNode);
			markIsomorphicHierarchy(this, hierarchyNode);
		}
	}

	// Marking isomorphic hierarchy

	private void markSignatureIfNeeded(EntityHierarchyNode hierarchyNode) {
		if (!hierarchyNode.getSubNodes().isEmpty())
			selectSignature = true;
	}

	private void markIsomorphicHierarchy(EntitySourceNode sourceNode, EntityHierarchyNode hierarchyNode) {
		markAllSimpleProperties(sourceNode, hierarchyNode);

		for (EntityHierarchyNode subHierarchyNode : hierarchyNode.getSubNodes()) {
			EntitySourceNode subSourceNode = newSubNode(subHierarchyNode);

			markIsomorphicHierarchy(subSourceNode, subHierarchyNode);
		}
	}

	// Marking isomorphic hierarchy

	private void markPolymorphicHierarchy(EntityHierarchyNode hierarchyNode) {
		markAllSimpleProperties(this, hierarchyNode);
		markDiscriminatorProperties();
		markNonTargettedSubProperties(hierarchyNode);
	}

	private void markDiscriminatorProperties() {
		DiscriminatedHierarchy dh = resolveDiscriminatedHierarchyRootedAtThis();
		for (GmProperty p : dh.getDiscriminatorProperties())
			if (SmartQueryPlannerTools.isScalarOrId(p))
				markDiscriminatorProperty(p.getName());
	}

	private void markAllSimpleProperties(EntitySourceNode sourceNode, EntityHierarchyNode hierarchyNode) {
		for (Property p : hierarchyNode.getAdditionalProperties())
			if (SmartQueryPlannerTools.isScalarOrId(p))
				sourceNode.markSimpleSmartPropertyForSelection(p.getName());
	}

	private void markNonTargettedSubProperties(EntityHierarchyNode hierarchyNode) {
		for (EntityHierarchyNode subHierarchyNode : CollectionTools2.nullSafe(hierarchyNode.getSubNodes())) {
			newSubNode(subHierarchyNode);

			for (Property p : subHierarchyNode.getAdditionalProperties()) {
				if (SmartQueryPlannerTools.isScalarOrId(p)) {
					EntityPropertyMapping epm = modelExpert.resolveEntityPropertyMappingIfPossible( //
							subHierarchyNode.getGmEntityType(), access, p.getName(), emUseCase);
					if (epm != null)
						markNonTargettedDelegatePropertyForSelection(epm.getDelegatePropertyName());
				}
			}

			markNonTargettedSubProperties(subHierarchyNode);
		}
	}

	private EntitySourceNode newSubNode(EntityHierarchyNode subHierarchyNode) {
		EntityType<?> subEntityType = subHierarchyNode.getEntityType();
		EntitySourceNode subSourceNode = new EntitySourceNode(source, this, subEntityType, null, access, emUseCase, context, subTypeJoin);
		subTypeNodes.put(subEntityType.getTypeSignature(), subSourceNode);

		return subSourceNode;
	}

	public void markEntityId() {
		markSimpleSmartPropertyForSelection(GenericEntity.id);
	}

	/**
	 * Given <tt>discriminatorProperty</tt> is a delegate property. If this property is mapped from some smart property, then that smartProperty is
	 * selected. If it is mapped
	 * 
	 */
	private void markDiscriminatorProperty(String discriminatorProperty) {
		GmProperty smartProperty = modelExpert.findSmartProperty(smartGmEntityType, access, discriminatorProperty, emUseCase);
		if (smartProperty != null)
			markSimpleSmartPropertyForSelection(smartProperty.getName());
		else
			markNonTargettedDelegatePropertyForSelection(discriminatorProperty);
	}

	/** Only called by PlanStructureInitializer and by this class itself. */
	public void markSimpleSmartPropertyForSelection(String smartProperty) {
		markSmartPropertyHelper(smartProperty, true);
	}

	private void markSmartPropertyHelper(String smartProperty, boolean select) {
		PropertyAssignment assignment = resolveSmartPropertyAssignment(smartProperty);

		if (assignment == null)
			return;

		if (assignment instanceof KeyPropertyAssignment || assignment instanceof LinkPropertyAssignment
				|| assignment instanceof CompositeKeyPropertyAssignment || assignment instanceof CompositeInverseKeyPropertyAssignment)
			throw new SmartQueryPlannerException("Invalid mapping. Simple property cannot have assignment: " + assignment + ". Property: "
					+ smartGmType.getTypeSignature() + "." + smartProperty);

		EntityPropertyMapping epm = resolveSmartPropertyMapping(smartProperty);

		markPropertyHelper(smartProperty, epm.getDelegatePropertyName(), select, epm.isVirtual());

		if (isConstantPropertyDependentOnType(smartProperty, epm))
			markSignatureForSelection();
	}

	/**
	 * Returns <tt>true</tt> iff given property also needs the type signature for it's resolution. Currently this only involves constant property when
	 * the owner entity is in a hierarchy.
	 */
	private boolean isConstantPropertyDependentOnType(String smartProperty, EntityPropertyMapping epm) {
		return epm.type == EpmType.constant && !resolveConstantPropertyMapping(smartProperty).isStatic;
	}

	private void markNonTargettedDelegatePropertyForSelection(String discriminatorProperty) {
		markPropertyHelper("delegate:" + discriminatorProperty, discriminatorProperty, true, false);
	}

	/**  */
	private void markPropertyHelper(String smartProperty, String delegateProperty, boolean select, boolean virtual) {
		smartToDelegateProperty.put(smartProperty, delegateProperty);
		delegateToSmartProperty.put(delegateProperty, smartProperty);

		if (select)
			if (virtual)
				markVirtualPropertyForSelection(smartProperty);
			else
				markDelegatePropertyForSelection(delegateProperty);
	}

	private void markVirtualPropertyForSelection(String smartProperty) {
		selectedSmartVirtualProps.add(smartProperty);
	}

	public boolean isSelectedVirtualProperty(String smartProperty) {
		return selectedSmartVirtualProps.contains(smartProperty);
	}

	public void markDelegatePropertyForSelection(String delegateProperty) {
		markDelegatePropertyForSelection(delegateProperty, null);
	}

	/**
	 * Called by {@link com.braintribe.model.processing.smart.query.planner.core.combination.DelegateQueryJoiner} when ensuring properties for
	 * correlation.
	 */
	public void markDelegatePropertyForSelection(String delegateProperty, ConversionWrapper cw) {
		// we use LinkedSet just to keep the order stable, for testing
		acquireLinkedSet(selectedDelegateProps, delegateProperty).add(cw);
	}

	public SimpleValueNode markInverseKeyCollection(String delegateProperty, EntitySourceNode inverse, String inverseProperty) {
		GmType propertyType = getDelegateGmProperty(delegateProperty).getType();
		SimpleValueNode svn = new SimpleValueNode(null, null, context);
		svn.joinMaster = this;
		svn.delegateJoinProperty = delegateProperty;
		svn.delegateCollectionGmType = (GmCollectionType) propertyType;
		svn.nodeType = SourceNodeType.inverseKeyCollection;

		svn.inverseNode = inverse;
		svn.inverseProperty = inverseProperty;

		simpleInverseKeyCollectionJoins.add(svn);

		return svn;
	}

	@Override
	public void markJoinFunction() {
		if (nodeType.isExplicitJoin())
			super.markJoinFunction();
		else if (nodeType == SourceNodeType.linkedCollectionNode)
			joinMaster.markDelegatePropertyForSelection(linkIndexPropertyName());
		else
			keyPropertySimpleCounterpart.markJoinFunction();
	}

	@Override
	public int acquireJoinFunctionPosition() {
		if (nodeType.isExplicitJoin())
			return super.acquireJoinFunctionPosition();

		if (nodeType == SourceNodeType.linkedCollectionNode)
			return joinMaster.acquirePropertyPosition(linkIndexPropertyName(), null);
		else
			return keyPropertySimpleCounterpart.acquireJoinFunctionPosition();
	}

	@Override
	public int getJoinFunctionPosition() {
		if (nodeType.isExplicitJoin())
			return super.getJoinFunctionPosition();

		if (nodeType == SourceNodeType.linkedCollectionNode)
			return joinMaster.getSimpleDelegatePropertyPosition(linkIndexPropertyName());
		else
			return keyPropertySimpleCounterpart.getJoinFunctionPosition();
	}

	private String linkIndexPropertyName() {
		return ((LinkPropertyAssignmentWrapper) dqjDescriptor).getLinkIndexPropertyName();
	}

	public void appendJoinNode(String smartJoinProperty, PropertyAssignment pa, EntitySourceNode joinNode) {
		if (pa instanceof InverseKeyPropertyAssignment)
			appendInverseWeakPropertyJoin(smartJoinProperty, joinNode, Arrays.asList((InverseKeyPropertyAssignment) pa));

		else if (pa instanceof KeyPropertyAssignment)
			appendWeakPropertyJoin(smartJoinProperty, joinNode, Arrays.asList((KeyPropertyAssignment) pa));

		else if (pa instanceof CompositeInverseKeyPropertyAssignment)
			appendInverseWeakPropertyJoin(smartJoinProperty, joinNode,
					((CompositeInverseKeyPropertyAssignment) pa).getInverseKeyPropertyAssignments());

		else if (pa instanceof CompositeKeyPropertyAssignment)
			appendWeakPropertyJoin(smartJoinProperty, joinNode, ((CompositeKeyPropertyAssignment) pa).getKeyPropertyAssignments());

		else if (pa instanceof LinkPropertyAssignment)
			appendLinkPropertyJoin(smartJoinProperty, joinNode, (LinkPropertyAssignment) pa);

		else
			appendStrongPropertyJoin(smartJoinProperty, joinNode);
	}

	/* The InverseKeyPropertyAssignment case */
	private void appendInverseWeakPropertyJoin(String smartJoinProperty, EntitySourceNode joinNode, Collection<InverseKeyPropertyAssignment> ikpas) {
		joinNode.joinMaster = this;
		joinNode.smartJoinProperty = smartJoinProperty;
		joinNode.dqjDescriptor = new InverseKeyPropertyAssignmentWrapper(smartGmEntityType, access, ikpas, modelExpert);
		joinNode.nodeType = SourceNodeType.inverseKpa;
		/* We do not need to set the joinNode.delegateJoinProperty, cause this is only needed for collections. For KPA, this is handled by the
		 * keyPropertySimpleCounterpart, for IKPA... */

		keyPropertyJoins.add(joinNode);
		allEntityJoins.put(smartJoinProperty, joinNode);
	}

	private void appendWeakPropertyJoin(String smartJoinProperty, EntitySourceNode joinNode, Collection<KeyPropertyAssignment> kpas) {
		joinNode.joinMaster = this;
		joinNode.smartJoinProperty = smartJoinProperty;
		joinNode.dqjDescriptor = new KeyPropertyAssignmentWrapper(kpas);
		joinNode.nodeType = SourceNodeType.kpa;
		/* We do not need to set the joinNode.delegateJoinProperty, cause this is only needed for collections. For KeyPropertyAssignment, this is
		 * handled by the keyPropertySimpleCounterpart, for InverseKeyPropertyAssignment.... */

		keyPropertyJoins.add(joinNode);
		allEntityJoins.put(smartJoinProperty, joinNode);

		if (joinNode.isCollection()) {
			Join join = (Join) joinNode.getSource();
			SimpleValueNode counterpartNode = new SimpleValueNode(join, joinNode.smartGmType, context);
			joinNode.keyPropertySimpleCounterpart = counterpartNode;
			appendSimpleNode(join.getProperty(), counterpartNode, SourceNodeType.simpleCounterpart);
		}
	}

	private void appendLinkPropertyJoin(String smartJoinProperty, EntitySourceNode joinNode, LinkPropertyAssignment lpa) {
		GmEntityType linkGmType = lpa.getLinkKey().getDeclaringType();
		EntitySourceNode linkEntityNode = new EntitySourceNode(null, null, null, linkGmType, linkGmType, lpa.getLinkAccess(), context);

		linkEntityNode.joinMaster = this;
		linkEntityNode.smartJoinProperty = smartJoinProperty;
		linkEntityNode.dqjDescriptor = new LinkPropertyAssignmentWrapper(lpa, Level.linkEntity);
		linkEntityNode.joinForJoinType = (Join) joinNode.getSource();
		linkEntityNode.nodeType = SourceNodeType.linkEntityNode;

		joinNode.joinMaster = linkEntityNode;
		joinNode.dqjDescriptor = new LinkPropertyAssignmentWrapper(lpa, Level.otherEntity);
		joinNode.nodeType = SourceNodeType.linkedCollectionNode;

		keyPropertyJoins.add(linkEntityNode);
		linkEntityNode.keyPropertyJoins.add(joinNode);

		context.planStructure().registerAuxEntitySourceNode(linkEntityNode);
	}

	private void appendStrongPropertyJoin(String smartJoinProperty, EntitySourceNode joinNode) {
		EntityPropertyMapping epm = resolveSmartPropertyMapping(smartJoinProperty);

		joinNode.joinMaster = this;
		joinNode.smartJoinProperty = smartJoinProperty;
		joinNode.delegateJoinProperty = epm.getDelegatePropertyName();
		joinNode.delegateCollectionGmType = collectionTypeOrNull(epm.getDelegatePropertyType());
		joinNode.nodeType = SourceNodeType.explicitJoin;

		explicitJoins.add(joinNode);
		allEntityJoins.put(smartJoinProperty, joinNode);
	}

	public void appendSimpleNode(String smartJoinProperty, SimpleValueNode simpleNode) {
		appendSimpleNode(smartJoinProperty, simpleNode, SourceNodeType.explicitSimpleJoin);
	}

	private void appendSimpleNode(String smartJoinProperty, SimpleValueNode simpleNode, SourceNodeType nodeType) {
		EntityPropertyMapping epm = resolveSmartPropertyMapping(smartJoinProperty);

		simpleNode.joinMaster = this;
		simpleNode.smartJoinProperty = smartJoinProperty;
		simpleNode.delegateJoinProperty = epm.getDelegatePropertyName();
		simpleNode.delegateCollectionGmType = (GmCollectionType) epm.getDelegatePropertyType();
		simpleNode.nodeType = nodeType;

		simpleCollectionJoins.add(simpleNode);
	}

	// ######################################################
	// ## . . Plan structured building phase methods . . . ##
	// ######################################################

	public boolean isSignatureSelected() {
		// return (retrieveEntireNode || selectSignature) && !isPolymorphicHierarchy();
		return selectSignature && !isPolymorphicHierarchy();
	}

	public int acquireDelegateSignaturePosition() {
		return signaturePosition >= 0 ? signaturePosition : (signaturePosition = context.allocateTuplePosition());
	}

	public int getDelegateSignaturePosition() {
		if (signaturePosition < 0) {
			throw new SmartQueryPlannerException("Signature of '" + this + "' was not assigned a position.");
		}

		return signaturePosition;
	}

	public int acquirePropertyPosition(String delegateProperty, ConversionWrapper cw) {
		Map<ConversionWrapper, Integer> positionMap = acquireMap(propertyPositions, delegateProperty);

		Integer result = positionMap.get(cw);

		if (result == null) {
			result = newPropertyPosition(delegateProperty, cw);
			positionMap.put(cw, result);
		}

		return result;
	}

	private Integer newPropertyPosition(String delegateProperty, ConversionWrapper cw) {
		/* We must make sure that these "correlation" properties are mapped to exact same position, cause the DQJ algorithm relies on that. */
		if (dqjDescriptor != null && dqjDescriptor.getJoinedEntityDelegatePropertyNames().contains(delegateProperty)) {
			if (keyPropertySimpleCounterpart != null) {
				/* We have a query like this: "select p.keyCompanies from SmartPerson p", and now are marking property for Company.name (the node for
				 * the collection element has keyPropertyAssignment set). We have checked that the property we are retrieving is the correlation
				 * property on the delegate, so we take it's position from the node representing the simple collection (Person.companyNames). */
				return keyPropertySimpleCounterpart.acquireValuePosition(cw);
			}

			// inverse case?
			return joinMaster.acquirePropertyPosition(dqjDescriptor.getRelationOwnerDelegatePropertyName(delegateProperty), inverseOf(cw));

		} else if (superTypeNode != null && isIdProperty(delegateProperty)) {
			return superTypeNode.acquirePropertyPosition(delegateProperty, cw);

		} else {
			return context.allocateTuplePosition();
		}
	}

	private boolean isIdProperty(String delegateProperty) {
		return resolveSmartPropertyMapping(GenericEntity.id).getDelegatePropertyName().equals(delegateProperty);
	}

	public int getEntityIdPosition() {
		return getSimplePropertyPosition(GenericEntity.id);
	}

	/**
	 * @return assigned position of given smart property. Note this only works for simple-type properties.
	 */
	public int getSimplePropertyPosition(String smartProperty) {
		if (isPolymorphicHierarchy() && superTypeNode != null)
			return rootSuperTypeNode.getSimplePropertyPositionDirect("delegate:" + smartProperty);
		else
			return getSimplePropertyPositionDirect(smartProperty);
	}

	private int getSimplePropertyPositionDirect(String smartProperty) {
		Integer result = propertyPositions.get(smartToDelegateProperty.get(smartProperty)).get(null);
		if (result == null) {
			throw new SmartQueryPlannerException("Property '" + smartProperty + "' was not assigned a position.");
		}

		return result;
	}

	public int getSimpleDelegatePropertyPosition(String delegateProperty) {
		return getSimpleDelegatePropertyPosition(delegateProperty, null);
	}

	public int getSimpleDelegatePropertyPosition(String delegateProperty, ConversionWrapper cw) {
		Integer result = propertyPositions.get(delegateProperty).get(cw);

		if (result == null)
			throw new SmartQueryPlannerException("Delegate property '" + delegateProperty + "' was not assigned a position.");

		return result;
	}

	public boolean isSmartPropertyMapped(String smartProperty) {
		return modelExpert.isPropertyMapped(smartGmEntityType, access, smartProperty, emUseCase);
	}

	public EntityPropertyMapping resolveSmartPropertyMapping(String smartProperty) {
		return modelExpert.resolveEntityPropertyMapping(smartGmEntityType, access, smartProperty, emUseCase);
	}

	protected PropertyAssignment resolveSmartPropertyAssignment(String smartProperty) {
		return modelExpert.resolvePropertyAssignmentIfPossible(smartGmEntityType, access, smartProperty, emUseCase);
	}

	public EntityHierarchyNode resolveHierarchyRootedAtThis() {
		return modelExpert.resolveMappedHierarchyRootedAt(smartGmEntityType, access, emUseCase);
	}

	public DiscriminatedHierarchy resolveDiscriminatedHierarchyRootedAtThis() {
		return modelExpert.resolveDiscriminatedHierarchyRootedAt(smartGmEntityType, access);
	}

	public Map<String, String> acquireTypeRules() {
		return modelExpert.acquireTypeRules(this);
	}

	public ConstantPropertyMapping resolveConstantPropertyMapping(String smartProperty) {
		return modelExpert.resolveConstantPropertyMapping(smartGmEntityType, access, smartProperty);
	}

	public Collection<String> selectedDelegateProperties() {
		return selectedDelegateProps.keySet();
	}

	public Collection<ConversionWrapper> getConversionsForDelegateProperty(String delegateP) {
		return selectedDelegateProps.get(delegateP);
	}

	public SmartConversion findSmartPropertyConversion(String smartProperty) {
		return resolveSmartPropertyMapping(smartProperty).getConversion();
	}

	public Collection<EntitySourceNode> getSubTypeNodes() {
		return subTypeNodes.values();
	}

	/**
	 * Returns {@link EntitySourceNode} for a sub-type.
	 */
	public EntitySourceNode getSubTypeNode(String signature) {
		return subTypeNodes.get(signature);
	}

	@Override
	public String toString() {
		return "EntitySourceNode [" + smartGmEntityType.getTypeSignature() + "][" + nodeType + "]";
	}

}
