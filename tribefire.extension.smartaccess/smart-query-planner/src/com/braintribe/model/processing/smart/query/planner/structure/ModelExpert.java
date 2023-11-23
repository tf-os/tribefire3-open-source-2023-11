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
package com.braintribe.model.processing.smart.query.planner.structure;

import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.sort;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.smart.SmartAccess;
import com.braintribe.model.accessdeployment.smart.meta.AsIs;
import com.braintribe.model.accessdeployment.smart.meta.DefaultDelegate;
import com.braintribe.model.accessdeployment.smart.meta.EntityAssignment;
import com.braintribe.model.accessdeployment.smart.meta.EnumConstantAssignment;
import com.braintribe.model.accessdeployment.smart.meta.IdentityEntityAssignment;
import com.braintribe.model.accessdeployment.smart.meta.PropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.QualifiedEntityAssignment;
import com.braintribe.model.accessdeployment.smart.meta.SmartUnmapped;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.smart.SmartAccessException;
import com.braintribe.model.processing.smart.query.planner.graph.EntitySourceNode;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.ConstantPropertyMapping;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.DiscriminatedHierarchy;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EmUseCase;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityMapping;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityPropertyMapping;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EnumMapping;

/**
 * Utility class which provides a wide range of model-related methods. This class is meant to be instantiated per task
 * (query/applyManipulation), as it retrieves information that can only be cached under the scope of a given task
 * (meta-data related). The tasks that can be cached with a "wider" scope should be delegated to the
 * {@link StaticModelExpert} provided via constructor.
 * 
 * TODO PGA after reviewing this, I don't see why this couldn't be cached on SmartAccess directly? Some of the
 * functionality could be used outside of the planner - e.g. checking if property is mapped inside the SmartEagerLoader.
 * 
 * EXCEPT, THIS IMPLEMENTATION IS NOT THREAD-SAFE!!! <br/>
 * MAKE SURE IT'S THREAD SAFE FIRST, BEFORE USING A SINGLE INSTANCE ON THE SMART ACCESS DIRECTLY.
 * 
 * @see StaticModelExpert
 */
public class ModelExpert {

	private static final Logger log = Logger.getLogger(ModelExpert.class);

	public final CmdResolver cmdResolver;
	public final StaticModelExpert staticModelExpert;
	public final SmartAccess smartDenotation;
	public final Map<IncrementalAccess, com.braintribe.model.access.IncrementalAccess> accessMapping;

	protected final EntityTypeRuleExpert entityTypeRuleExpert;
	protected final EntityMappingExpert entityMappingExpert;
	protected final EnumMappingExpert enumMappingExpert;
	protected final MappedHierarchyExpert mappedHierarchyExpert;
	protected final QueryableHierarchyExpert queryableHierarchyExpert;
	protected final DiscriminatorHierarchyExpert discriminatorHierarchyExpert;
	protected final AsIsDelegationExpert asIsDelegationExpert;

	protected final EntityAssignment defaultEntityAssignment;
	protected final Map<com.braintribe.model.accessdeployment.IncrementalAccess, EntityAssignment> defaultEntityAssignments;
	protected final PropertyAssignment defaultPropertyAssignment;
	protected final Map<String, IncrementalAccess> partitionToAccess;
	protected final Collection<IncrementalAccess> accesses;

	private static final Comparator<IncrementalAccess> accessComparator = Comparator.comparing(IncrementalAccess::getExternalId);
	
	public ModelExpert(CmdResolver cmdResolver, StaticModelExpert staticModelExpert, SmartAccess smartDenotation,
			Map<IncrementalAccess, com.braintribe.model.access.IncrementalAccess> accessMapping) {

		this.cmdResolver = cmdResolver;
		this.staticModelExpert = staticModelExpert;
		this.smartDenotation = smartDenotation;
		this.accessMapping = accessMapping;
		this.accesses = sort(accessMapping.keySet(), accessComparator);
		this.partitionToAccess = index(smartDenotation, accessMapping);
		this.entityTypeRuleExpert = new EntityTypeRuleExpert(this);
		this.entityMappingExpert = new EntityMappingExpert(this);
		this.enumMappingExpert = new EnumMappingExpert(this);
		this.mappedHierarchyExpert = new MappedHierarchyExpert(this);
		this.queryableHierarchyExpert = new QueryableHierarchyExpert(this);
		this.discriminatorHierarchyExpert = new DiscriminatorHierarchyExpert(this);
		this.asIsDelegationExpert = new AsIsDelegationExpert(this);

		this.defaultEntityAssignment = DefaultMappings.entity(smartDenotation);
		this.defaultEntityAssignments = asMap(smartDenotation, defaultEntityAssignment);
		this.defaultPropertyAssignment = DefaultMappings.property();
	}

	private Map<String, IncrementalAccess> index(SmartAccess smartDenotation,
			Map<IncrementalAccess, com.braintribe.model.access.IncrementalAccess> accessMapping) {

		Map<String, IncrementalAccess> result = newMap();

		for (Entry<IncrementalAccess, com.braintribe.model.access.IncrementalAccess> entry : accessMapping.entrySet()) {
			IncrementalAccess denotation = entry.getKey();
			if (denotation == smartDenotation)
				continue;

			for (String partition : entry.getValue().getPartitions())
				result.put(partition, denotation);
		}

		return result;
	}

	public Collection<IncrementalAccess> getSortedAccesses() {
		return accesses;
	}

	public IncrementalAccess getAccess(String partition) {
		return partitionToAccess.get(partition);
	}

	public Set<String> getPartitions(IncrementalAccess access) {
		return accessMapping.get(access).getPartitions();
	}

	// ##################################################
	// ## . . . Static Model-Information Methods . . . ##
	// ##################################################

	public ModelOracle getSmartModelOracle() {
		return staticModelExpert.getSmartModelOracle();
	}

	public GmEntityType resolveSmartEntityType(String signature) {
		return staticModelExpert.resolveSmartEntityType(signature);
	}

	public GmEnumType resolveSmartEnumType(String signature) {
		return cmdResolver.getMetaData().enumTypeSignature(signature).getEnumType();
	}

	public GmEntityType resolveEntityType(String delegateSignature, IncrementalAccess access) {
		return staticModelExpert.resolveEntityType(delegateSignature, access);
	}

	public GmProperty getGmProperty(GmEntityType gmEntityType, String propertyName) {
		GmProperty result = getAllProperties(gmEntityType).get(propertyName);

		if (result == null)
			throw new RuntimeException("Property not found: " + gmEntityType.getTypeSignature() + "." + propertyName);

		return result;
	}

	public Map<String, GmProperty> getAllProperties(GmEntityType gmEntityType) {
		return staticModelExpert.getAllProperties(gmEntityType);
	}

	public Set<GmEntityType> getDirectSmartSubTypes(GmEntityType smartEntityType) {
		return staticModelExpert.getDirectSmartSubTypes(smartEntityType);
	}

	public boolean isFirstAssignableFromSecond(GmEntityType et1, GmEntityType et2) {
		return staticModelExpert.isFirstAssignableFromSecond(et1, et2);
	}

	/** @see ModelHierarchyExpert#resolveHierarchyRootedAt(GmEntityType) */
	public EntityHierarchyNode resolveHierarchyRootedAt(GmEntityType smartType) {
		return staticModelExpert.resolveHierarchyRootedAt(smartType);
	}

	/**
	 * Similar to {@link #resolveHierarchyRootedAt(GmEntityType)}, but also only considers sub-types of given smartType
	 * which are mapped to the same hierarchy as this smartType.
	 */
	public EntityHierarchyNode resolveMappedHierarchyRootedAt(GmEntityType smartType, IncrementalAccess access, EmUseCase useCase) {
		EntityHierarchyNode staticHierarchy = staticModelExpert.resolveHierarchyRootedAt(smartType);
		return mappedHierarchyExpert.resolveMappedHierarchyRootedAt(staticHierarchy, access, useCase);
	}

	// ##################################################
	// ## . . . . . . Task-scoped Methods . . . . . . .##
	// ##################################################

	public EntityAssignment resolveEntityAssignment(GmEntityType smartType, IncrementalAccess access, EmUseCase useCase) {
		EntityAssignment entityAssignment = resolveEntityAssignmentsIfPossible(smartType, useCase).get(access);
		if (entityAssignment == null)
			throw new SmartAccessException(
					"No mapping found for entity '" + smartType.getTypeSignature() + "', access: '" + access.getExternalId() + "'");

		return entityAssignment;
	}

	/**
	 * When having configuration using key property, we have two options on how to configure the actual keyProperty. In
	 * normal cases, it is the property of the corresponding delegate entity, e.g. if SmartPerson references
	 * SmartCompany via KPA, the keyProperty would be the DelegateCompany.companyId.
	 * 
	 * However, we might have a smart property whose type is an unmapped smart type and still want to do the keyProperty
	 * mappings. In such case (and none other) we can configure the smart property to be the keyProperty. Using previous
	 * example, if SmartCompany is not mapped, but has various mapped sub-types, we can say that our key property is
	 * SmartCompany.id.
	 * 
	 * Let's now assume for both cases that SmartCompany has sub-type LlcCompany, and that this is the actual type we
	 * are dealing with (depends on the context, in case of manipulations might be the type of our assigned value, in
	 * case of query might be the type of our source). However, in both cases our configuration references SmartCompany
	 * (as either Type or EntityType of given keyProperty, again depends on use-case which one is relevant).
	 * 
	 * Then, in the first case (usual one) our method would be called with params: (DelegateCompany, companyId,
	 * LlcCompany) and would return companyId, because DelegateCompany is not a smart type. In the second case it would
	 * be called with params: (SmartCompany, id, LlcCompany) and would see SmartCompany is an unmapped smart type,
	 * therefore it would assume "id" is smart property and find what is it mapped to for LlcCompany, which is let's say
	 * llcCompanyid.
	 */
	public String findDelegatePropertyForKeyProperty(GmEntityType mappingKeyPropertyOwner, IncrementalAccess access, String keyProperty,
			GmEntityType actualKeyPropertyOwner) {

		/* configured entity is not a smart type at all, or is mapped (so it is both smart-type and delegate type) */
		if (!isSmartType(mappingKeyPropertyOwner) || resolveEntityMappingIfPossible(mappingKeyPropertyOwner, access, null) != null)
			return keyProperty;

		/* Configured entity is not mapped, but the entity is smart, so we know keyProperty is smart too, thus we have
		 * to find the corresponding delegate property, using mappings of actualKeyPropertyOwner. */
		return resolveEntityPropertyMapping(actualKeyPropertyOwner, access, keyProperty).getDelegatePropertyName();
	}

	public boolean isSmartType(GmEntityType gmType) {
		return staticModelExpert.isSmartType(gmType);
	}

	public <T extends EntityPropertyMapping> T resolveEntityPropertyMapping(GmEntityType smartType, IncrementalAccess access, String smartProperty) {
		return resolveEntityPropertyMapping(smartType, access, smartProperty, null);
	}

	public <T extends EntityPropertyMapping> T resolveEntityPropertyMapping( //
			GmEntityType smartType, IncrementalAccess access, String smartProperty, EmUseCase useCase) {

		T result = resolveEntityPropertyMappingIfPossible(smartType, access, smartProperty, useCase);
		if (result == null)
			throw new SmartAccessException("No mapping found for property '" + smartProperty + "' of: " + smartType.getTypeSignature());

		return result;
	}

	public <T extends EntityPropertyMapping> T resolveEntityPropertyMappingIfPossible(GmEntityType smartType, IncrementalAccess access,
			String smartProperty, EmUseCase useCase) {

		return entityMappingExpert.resolveEntityPropertyMappingIfPossible(smartType, access, smartProperty, useCase);
	}

	public boolean isPropertyMapped(GmEntityType smartType, IncrementalAccess access, String smartProperty, EmUseCase useCase) {
		return resolvePropertyAssignmentIfPossible(smartType, access, smartProperty, useCase) != null;
	}

	public PropertyAssignment resolvePropertyAssignment(GmEntityType smartType, IncrementalAccess access, String smartProperty, EmUseCase useCase) {
		PropertyAssignment result = resolvePropertyAssignmentIfPossible(smartType, access, smartProperty, useCase);
		if (result == null)
			throw new SmartAccessException("No mapping found for property '" + smartProperty + "' of: " + smartType.getTypeSignature());

		return result;
	}

	public PropertyAssignment resolvePropertyAssignmentIfPossible(GmEntityType smartType, IncrementalAccess access, String propertyName,
			EmUseCase useCase) {

		if (useCase != null)
			return defaultPropertyAssignment;

		PropertyAssignment result = cmdResolver.getMetaData() //
				.entityType(smartType) //
				.property(propertyName) //
				.useCase(access.getExternalId()) //
				.meta(PropertyAssignment.T) //
				.exclusive();

		if (result == null || result.type() == SmartUnmapped.T)
			return null;
		else
			return result;
	}

	public EntityMapping resolveEntityMapping(String smartSignature, String partition, EmUseCase useCase) {
		return entityMappingExpert.resolveEntityMapping(smartSignature, partition, useCase);
	}

	public EntityMapping resolveEntityMapping(String smartSignature, IncrementalAccess access, EmUseCase useCase) {
		return entityMappingExpert.resolveEntityMapping(smartSignature, access, useCase);
	}

	public EntityMapping resolveEntityMapping(GmEntityType smartType, IncrementalAccess access, EmUseCase useCase) {
		return entityMappingExpert.resolveEntityMapping(smartType, access, useCase);
	}

	public EntityMapping resolveEntityMappingIfPossible(GmEntityType smartType, IncrementalAccess access, EmUseCase useCase) {
		Map<IncrementalAccess, EntityMapping> map = resolveEntityMappingsIfPossible(smartType, useCase);
		return map != null ? map.get(access) : null;
	}

	public Map<IncrementalAccess, EntityMapping> resolveEntityMappingsIfPossible(GmEntityType smartType, EmUseCase useCase) {
		return entityMappingExpert.resolveEntityMappingsIfPossible(smartType, useCase);
	}

	public EntityMapping resolveEntityMappingForDelegateTypeIfPossible(String delegateSignature, IncrementalAccess access) {
		return entityMappingExpert.resolveEntityMappingForDelegateTypeIfPossible(delegateSignature, access);
	}

	public EnumMapping resolveEnumMapping(String smartSignature) {
		return enumMappingExpert.resolveEnumMapping(resolveSmartEnumType(smartSignature));
	}

	public EnumMapping resolveEnumMapping(GmEnumType smartEnumType) {
		return enumMappingExpert.resolveEnumMapping(smartEnumType);
	}

	public IncrementalAccess resolveDefaultDelegate(String smartSignature) {
		DefaultDelegate dp = cmdResolver.getMetaData().entityTypeSignature(smartSignature).meta(DefaultDelegate.T).exclusive();
		return dp != null ? dp.getAccess() : null;
	}

	/* This should only be used by SmartManipulationProcessor, when processing induced manipulations, and from
	 * EntitySourceNode when selecting discriminator properties. */
	public GmProperty findSmartProperty(GmEntityType smartType, IncrementalAccess access, String delegateProperty, EmUseCase useCase) {
		return entityMappingExpert.findSmartProperty(smartType, access, delegateProperty, useCase);
	}

	public ConstantPropertyMapping resolveConstantPropertyMapping(GmEntityType smartGmType, IncrementalAccess access, String smartProperty) {
		return entityMappingExpert.resolveConstantPropertyMapping(smartGmType, access, smartProperty);
	}

	public Map<String, String> acquireTypeRules(EntitySourceNode sourceNode) {
		return entityTypeRuleExpert.acquireTypeRules(sourceNode.getSmartGmType(), sourceNode.getAccess(), sourceNode.getEmUseCase());
	}

	/** @return non-empty map with all the relevant entity assignments or <tt>null</tt> */
	protected Map<IncrementalAccess, EntityAssignment> resolveEntityAssignmentsIfPossible(GmEntityType smartType, EmUseCase useCase) {
		if (useCase != null)
			return defaultEntityAssignments;

		Map<IncrementalAccess, EntityAssignment> result = newMap();

		for (IncrementalAccess delegate : getSortedAccesses()) {
			EntityAssignment ea = cmdResolver.getMetaData() //
					.entityType(smartType) //
					.useCase(delegate.getExternalId()) //
					.meta(EntityAssignment.T) //
					.exclusive();

			if (ea == null || ea.entityType() == (Object) SmartUnmapped.T)
				continue;

			/* The second check provides backwards compatibility - before we would not use the use-case selector, but
			 * have access on the mapping to tell us which access we map to. */
			if ((ea.getAccess() == null && checkNotIllegalAsIsAssignment(ea, smartType, delegate)) || ea.getAccess() == delegate)
				result.put(delegate, ea);
		}

		return result.isEmpty() ? null : result;
	}

	private boolean checkNotIllegalAsIsAssignment(EntityAssignment ea, GmEntityType smartType, IncrementalAccess delegate) {
		if (ea instanceof IdentityEntityAssignment || ea instanceof AsIs) {
			if (delegate == smartDenotation)
				return false;

			// This might happen with a default IdentityEntityAssignment
			return staticModelExpert.containsEntityType(smartType, delegate);
		}

		if (ea instanceof QualifiedEntityAssignment) {
			GmEntityType entityType = ((QualifiedEntityAssignment) ea).getEntityType();

			if (!staticModelExpert.containsEntityType(entityType, delegate)) {
				log.warn("Illegal smart mapping. The mapping states that smart type '" + smartType.getTypeSignature() + "' is mapped to '"
						+ entityType.getTypeSignature() + "'  in access '" + delegate.getExternalId()
						+ "', but this access does not contain that type. This mapping will be ignored.");
				return false;
			}

		}

		return true;
	}

	/** Only used by manipulation processor. */
	public Map<IncrementalAccess, PropertyAssignment> resolvePropertyAssignmentsIfPossible(//
			GmEntityType smartType, String propertyName, EmUseCase useCase) {

		if (useCase != null)
			throw new UnsupportedOperationException("Method 'ModelExpert.resolvePropertyAssignmentsIfPossible' is not implemented yet!");

		Map<IncrementalAccess, PropertyAssignment> result = newMap();

		Map<IncrementalAccess, EntityAssignment> resolveEntityAssignmentsIfPossible = resolveEntityAssignmentsIfPossible(smartType, useCase);
		for (IncrementalAccess access : resolveEntityAssignmentsIfPossible.keySet()) {
			PropertyAssignment pa = resolvePropertyAssignmentIfPossible(smartType, access, propertyName, useCase);

			result.put(access, pa);
		}

		return result.isEmpty() ? null : result;
	}

	protected EnumConstantAssignment resolveEnumConstantAssignmentIfPossible(GmEnumConstant gmEnumConstant) {
		return cmdResolver.getMetaData().lenient(true).enumConstant(gmEnumConstant).meta(EnumConstantAssignment.T).exclusive();
	}

	/** @see QueryableHierarchyExpert#resolveRelevantQueryableTypesFor(GmEntityType) */
	public List<EntityMapping> resolveRelevantQueryableTypesFor(GmEntityType smartType) {
		return queryableHierarchyExpert.resolveRelevantQueryableTypesFor(smartType);
	}

	public DiscriminatedHierarchy resolveDiscriminatedHierarchyRootedAt(GmEntityType smartType, IncrementalAccess access) {
		return discriminatorHierarchyExpert.resolveDiscriminatedHierarchyRootedAt(smartType, access);
	}

	public boolean isMappedAsIs(String smartSignature, IncrementalAccess access) {
		return asIsDelegationExpert.isMappedAsIs(smartSignature, access);
	}

}
