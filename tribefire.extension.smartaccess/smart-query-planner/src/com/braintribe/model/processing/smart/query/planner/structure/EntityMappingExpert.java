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

import static com.braintribe.utils.lcd.CollectionTools2.acquireMap;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.smart.meta.AsIs;
import com.braintribe.model.accessdeployment.smart.meta.CompositeInverseKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.CompositeKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.ConstantPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.DirectPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.EntityAssignment;
import com.braintribe.model.accessdeployment.smart.meta.IdentityEntityAssignment;
import com.braintribe.model.accessdeployment.smart.meta.InverseKeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.KeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.PolymorphicEntityAssignment;
import com.braintribe.model.accessdeployment.smart.meta.PropertyAsIs;
import com.braintribe.model.accessdeployment.smart.meta.PropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.QualifiedEntityAssignment;
import com.braintribe.model.accessdeployment.smart.meta.QualifiedPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.VirtualPropertyAssignment;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.smart.query.planner.SmartQueryPlannerException;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.ConstantPropertyMapping;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.DiscriminatedHierarchy;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EmUseCase;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityMapping;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityPropertyMapping;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityPropertyMapping.CompositeKpaPropertyWrapper;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityPropertyMapping.ConstantPropertyWrapper;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityPropertyMapping.PropertyAsIsWrapper;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityPropertyMapping.QualifiedPropertyWrapper;

/**
 * Component of {@link ModelExpert} which is able to resolve the {@link EntityMapping} for given smart entity type.
 * 
 * @see #resolveEntityMapping(GmEntityType, IncrementalAccess, EmUseCase)
 * 
 *      NOT THREAD-SAFE
 */
class EntityMappingExpert {

	private final ModelExpert modelExpert;
	// not easy to make this thread-safe, as the maps contain null
	private final Map<GmEntityType, Map<IncrementalAccess, EntityMapping>> smartEntityMappingsCache = newMap();
	private final Map<IncrementalAccess, Map<GmEntityType, Map<String, GmProperty>>> delegateToSmartPropertyCache = newMap();
	private final Map<IncrementalAccess, Map<String /* EntitySignature */, EntityMapping>> delegateEntityMappingCache = newMap();
	private final Map<IncrementalAccess, Map<EmUseCase, Map<GmEntityType, Map<String, EntityPropertyMapping>>>> entityPropertyMappingCache = newMap();
	private final Map<IncrementalAccess, Map<GmEntityType, Map<String /* propName */, ConstantPropertyMapping>>> constantPropertyValueCache = newMap();

	private boolean fullyIndexed = false;

	public EntityMappingExpert(ModelExpert modelExpert) {
		this.modelExpert = modelExpert;
	}

	// ###############################################################
	// ## . Resolving entity mapping for given smart-entity type . .##
	// ###############################################################

	/**
	 * @see #resolveEntityMapping(GmEntityType, IncrementalAccess, EmUseCase)
	 */
	public EntityMapping resolveEntityMapping(String smartSignature, String partition, EmUseCase useCase) {
		GmEntityType smartGmType = modelExpert.resolveSmartEntityType(smartSignature);
		IncrementalAccess access = resolveAccess(smartGmType, partition, useCase);

		return resolveEntityMapping(smartGmType, access, useCase);
	}

	private IncrementalAccess resolveAccess(GmEntityType smartGmType, String partition, EmUseCase useCase) {
		if (!EntityReference.ANY_PARTITION.equals(partition))
			return modelExpert.getAccess(partition);

		Map<IncrementalAccess, EntityMapping> mappings = resolveEntityMappingsIfPossible(smartGmType, useCase);
		if (mappings == null)
			throw new SmartQueryPlannerException("No entity mapping found for '" + smartGmType.getTypeSignature() + "' for useCase: " + useCase);

		if (mappings.size() == 1)
			return first(mappings.keySet());

		throw new IllegalArgumentException("Partition must be specified to resolve access for '" + smartGmType.getTypeSignature() + "', useCase: "
				+ useCase + ", because the type is mapped to multiple delegate accesses: " + mappings.keySet());
	}

	public EntityMapping resolveEntityMapping(String smartSignature, IncrementalAccess access, EmUseCase useCase) {
		return resolveEntityMapping(modelExpert.resolveSmartEntityType(smartSignature), access, useCase);
	}

	/**
	 * Resolves {@link EntityMapping} for given smart entity type, if entity is actually mapped. information may either be set explicitly using
	 * {@link EntityAssignment} meta data, or can be inferred using information from {@link PropertyAssignment}s meta data. <tt>null</tt> is returned
	 * if entity is not mapped, which means none of it's properties are mapped as well as none of it's super-types are mapped.
	 * 
	 * The inference follows the following rules.
	 * <ol>
	 * <li>If given type has super-types, their mapping is resolved first. This is necessary for checking that our resolved mapped type is a proper
	 * sub-type of all the mapped super-types.</li>
	 * <li>All the properties not mapped by any of the super-type are collected.</li>
	 * <li>The properties from previous step are examined, for each we try to find the corresponding {@link PropertyAssignment}, thus giving us the
	 * delegate type to which this property is mapped (the delegate type is the owner of the property to which our smart property is mapped). If
	 * multiple mapped types are found, we obviously take the most specific one (one which is a sub-type of all of these). If there is no unique
	 * common sub-type, the mapping is inconsistent and an exception is thrown (we obviously cannot map one entity to two incompatible entities).</li>
	 * <li>We have to resolve an access for this resolved delegate entity type. If some super-types are mapped, we check that all of them are mapped
	 * to same access and that our entity type is also in that delegate access. If no super-type is mapped, we select a unique access containing our
	 * resolved entity type. If no such access exists, we throw an exception.</li>
	 * <li>Finally, we make one extra check - whether the resolved type is a proper sub-type of all the mapped types for all the super-types of our
	 * smart type. If it is, we return information with this entity type, if not, we throw an exception.</li>
	 * </ol>
	 */
	public EntityMapping resolveEntityMapping(GmEntityType smartType, IncrementalAccess access, EmUseCase useCase) {
		EntityMapping em = resolveEntityMappingIfPossible(smartType, access, useCase);
		if (em == null)
			throw new SmartQueryPlannerException(
					"No entity mapping found for '" + smartType.getTypeSignature() + "' in access: " + access.getExternalId());

		return em;
	}

	public EntityMapping resolveEntityMappingIfPossible(GmEntityType smartType, IncrementalAccess access, EmUseCase useCase) {
		if (access == null)
			// temporary fail-fast solution (to identify possible bug asap)
			throw new SmartQueryPlannerException("Cannot resolve EntityMapping. No access given.");

		Map<IncrementalAccess, EntityMapping> map = resolveEntityMappingsIfPossible(smartType, useCase);
		return map != null ? map.get(access) : null;
	}

	/** Can return <tt>null</tt>. */
	public Map<IncrementalAccess, EntityMapping> resolveEntityMappingsIfPossible(GmEntityType smartType, EmUseCase useCase) {
		if (useCase != null)
			return resolveEntityMappingsHelper(smartType, useCase);

		if (smartEntityMappingsCache.containsKey(smartType))
			return smartEntityMappingsCache.get(smartType);

		Map<IncrementalAccess, EntityMapping> result = resolveEntityMappingsHelper(smartType, null);
		smartEntityMappingsCache.put(smartType, result);

		if (result != null)
			for (EntityMapping em : result.values())
				acquireMap(delegateEntityMappingCache, em.getAccess()).put(em.getDelegateEntityType().getTypeSignature(), em);

		return result;
	}

	public EntityMapping resolveEntityMappingForDelegateTypeIfPossible(String delegateSignature, IncrementalAccess access) {
		Map<String, EntityMapping> accessEmCache = acquireMap(delegateEntityMappingCache, access);
		EntityMapping result = accessEmCache.get(delegateSignature);
		if (result == null && !fullyIndexed) {
			indexMappingsForAllSmartTypes();
			result = accessEmCache.get(delegateSignature);
		}

		return result;
	}

	private void indexMappingsForAllSmartTypes() {
		Stream<GmEntityType> smartEntityType = modelExpert.getSmartModelOracle().getTypes().onlyEntities().asGmTypes();

		for (GmEntityType smartType : (Iterable<GmEntityType>) smartEntityType::iterator)
			for (IncrementalAccess access : modelExpert.getSortedAccesses())
				resolveEntityMappingIfPossible(smartType, access, null);

		fullyIndexed = true;
	}

	private Map<IncrementalAccess, EntityMapping> resolveEntityMappingsHelper(GmEntityType smartType, EmUseCase useCase) {
		Map<IncrementalAccess, EntityAssignment> eas = modelExpert.resolveEntityAssignmentsIfPossible(smartType, useCase);

		return eas != null ? assignmentBasedEntityMappings(eas, smartType, useCase) : null;
	}

	private Map<IncrementalAccess, EntityMapping> assignmentBasedEntityMappings(Map<IncrementalAccess, EntityAssignment> eas, GmEntityType smartType,
			EmUseCase useCase) {

		Map<IncrementalAccess, EntityMapping> result = newMap();

		for (Entry<IncrementalAccess, EntityAssignment> entry : eas.entrySet()) {
			IncrementalAccess access = entry.getKey();
			EntityAssignment ea = entry.getValue();

			EntityMapping em = assignmentBasedEntityMapping(ea, smartType, access, useCase);
			result.put(access, em);
		}

		return result;
	}

	private EntityMapping assignmentBasedEntityMapping(EntityAssignment ea, GmEntityType smartType, IncrementalAccess access, EmUseCase useCase) {

		if (ea instanceof IdentityEntityAssignment || ea instanceof AsIs) {
			return new EntityMapping(smartType, smartType, null, access, useCase);

		} else if (ea instanceof PolymorphicEntityAssignment) {
			// we know useCase is null, cause if it is not, it must be IdentityEntityAssignment
			DiscriminatedHierarchy dh = modelExpert.resolveDiscriminatedHierarchyRootedAt(smartType, access);
			return new EntityMapping(smartType, dh.getDelegateEntityType(), dh, access, useCase);

		} else if (ea instanceof QualifiedEntityAssignment) {
			return new EntityMapping(smartType, ((QualifiedEntityAssignment) ea).getEntityType(), null, access, useCase);

		} else {
			throw new SmartQueryPlannerException("Uknown EntityAssignment type. Assignment: " + ea);
		}
	}

	private Map<String, GmProperty> allProperties(GmEntityType smartType) {
		return modelExpert.staticModelExpert.getAllProperties(smartType);
	}

	// ###############################################################
	// ## . . . . . Find smart-Property for delegate one. . . . . . ##
	// ###############################################################

	public GmProperty findSmartProperty(GmEntityType smartType, IncrementalAccess access, String delegateProperty, EmUseCase useCase) {
		Map<String, GmProperty> delegateToSmartMap = acquireMap(acquireMap(delegateToSmartPropertyCache, access), smartType);

		GmProperty result = delegateToSmartMap.get(delegateProperty);

		if (result == null) {
			if (delegateToSmartMap.containsKey(delegateProperty))
				return null;

			result = findSmartPropertyHelper(smartType, access, delegateProperty, useCase);
			delegateToSmartMap.put(delegateProperty, result);
		}

		return result;
	}

	private GmProperty findSmartPropertyHelper(GmEntityType smartType, IncrementalAccess access, String delegateProperty, EmUseCase useCase) {
		for (GmProperty property : allProperties(smartType).values()) {
			String smartProperty = property.getName();
			PropertyAssignment pa = modelExpert.resolvePropertyAssignmentIfPossible(smartType, access, smartProperty, useCase);

			if (!(pa instanceof DirectPropertyAssignment))
				continue;

			EntityPropertyMapping epm = resolveEpm(smartType, access, smartProperty, useCase);

			if (epm != null && delegateProperty.equals(epm.getDelegatePropertyName()))
				return property;
		}

		// delegateProperty is not mapped directly by any smartProperty;
		return null;
	}

	private EntityPropertyMapping resolveEpm(GmEntityType smartType, IncrementalAccess access, String smartProperty, EmUseCase useCase) {
		try {
			return modelExpert.resolveEntityPropertyMapping(smartType, access, smartProperty, useCase);

		} catch (SmartQueryPlannerException ignored) {
			return null;
		}
	}

	// ###############################################################
	// ## . Resolving entity-property mapping for given smart type .##
	// ###############################################################

	public <T extends EntityPropertyMapping> T resolveEntityPropertyMappingIfPossible(GmEntityType smartType, IncrementalAccess access,
			String smartProperty, EmUseCase useCase) {

		// @formatter:off
		Map<GmEntityType, Map<String, EntityPropertyMapping>> cacheForUseCase = acquireMap(acquireMap(entityPropertyMappingCache, access), useCase);
		Map<String, EntityPropertyMapping> propertyMapping = acquireMap(cacheForUseCase, smartType);
		// @formatter:on

		EntityPropertyMapping result = propertyMapping.get(smartProperty);

		if (result == null) {
			PropertyAssignment pa = modelExpert.resolvePropertyAssignmentIfPossible(smartType, access, smartProperty, useCase);
			if (pa == null)
				return null;

			EntityMapping em = resolveEntityMapping(smartType, access, useCase);
			result = instanceFor(em, pa, smartProperty);

			propertyMapping.put(smartProperty, result);
		}

		return (T) result;
	}

	private EntityPropertyMapping instanceFor(EntityMapping em, PropertyAssignment assignment, String smartProperty) {
		if (assignment instanceof VirtualPropertyAssignment) {
			if (assignment instanceof ConstantPropertyAssignment) {
				GmProperty smartGmProperty = modelExpert.getGmProperty(em.getSmartEntityType(), smartProperty);
				return new ConstantPropertyWrapper(em, smartGmProperty, (ConstantPropertyAssignment) assignment);
			}

		} else if (assignment instanceof DirectPropertyAssignment) {

			if (assignment instanceof AsIs)
				return new PropertyAsIsWrapper(em, smartProperty, null);

			else if (assignment instanceof PropertyAsIs)
				return new PropertyAsIsWrapper(em, smartProperty, ((PropertyAsIs) assignment).getConversion());

			else
				return new QualifiedPropertyWrapper(em, ((QualifiedPropertyAssignment) assignment));

		} else if (assignment instanceof InverseKeyPropertyAssignment) {
			throw new SmartQueryPlannerException("Virtual property - does not work!");

		} else if (assignment instanceof KeyPropertyAssignment) {
			// same as the normal case, right?
			return new QualifiedPropertyWrapper(em, (KeyPropertyAssignment) assignment);

		} else if (assignment instanceof CompositeKeyPropertyAssignment) {
			return new CompositeKpaPropertyWrapper(em, (CompositeKeyPropertyAssignment) assignment);

		} else if (assignment instanceof CompositeInverseKeyPropertyAssignment) {
			return new CompositeKpaPropertyWrapper(em, (CompositeInverseKeyPropertyAssignment) assignment);
		}

		throw new SmartQueryPlannerException("Unknown PropertyAssignment type '" + assignment.getClass().getName() + "', value: " + assignment);
	}

	// ################################################################
	// ## . Resolving constant value for a property in a hierarchy . ##
	// ################################################################

	/** Returns map from delegate signature to constant value. */
	public ConstantPropertyMapping resolveConstantPropertyMapping(GmEntityType smartGmType, IncrementalAccess access, String smartProperty) {

		Map<GmEntityType, Map<String, ConstantPropertyMapping>> cacheForAccess = acquireMap(constantPropertyValueCache, access);
		Map<String, ConstantPropertyMapping> cacheForEntity = acquireMap(cacheForAccess, smartGmType);
		ConstantPropertyMapping cpm = cacheForEntity.get(smartProperty);

		if (cpm == null) {
			Map<String, Object> smartToValue = newMap();
			Map<String, Object> delegateToValue = newMap();

			EntityHierarchyNode hierarchyNode = modelExpert.resolveMappedHierarchyRootedAt(smartGmType, access, null);
			fillDiscriminatorValues(hierarchyNode, access, smartProperty, smartToValue, delegateToValue);

			cpm = new ConstantPropertyMapping(smartToValue, delegateToValue);

			cacheForEntity.put(smartProperty, cpm);
		}

		return cpm;
	}

	private void fillDiscriminatorValues(EntityHierarchyNode hierarchyNode, IncrementalAccess access, String smartProperty,
			Map<String, Object> smartToValue, Map<String, Object> delegateToValue) {

		addEntryForNode(hierarchyNode, access, smartProperty, smartToValue, delegateToValue);

		for (EntityHierarchyNode subNode : hierarchyNode.getSubNodes())
			fillDiscriminatorValues(subNode, access, smartProperty, smartToValue, delegateToValue);
	}

	private void addEntryForNode(EntityHierarchyNode hierarchyNode, IncrementalAccess access, String smartProperty, Map<String, Object> smartToValue,
			Map<String, Object> delegateToValue) {

		ConstantPropertyWrapper cpw = resolveEntityPropertyMappingIfPossible(hierarchyNode.getGmEntityType(), access, smartProperty, null);
		EntityMapping em = resolveEntityMapping(hierarchyNode.getGmEntityType(), access, null);

		smartToValue.put(em.getSmartEntityType().getTypeSignature(), cpw.getConstantValue());
		delegateToValue.put(em.getDelegateEntityType().getTypeSignature(), cpw.getConstantValue());
	}

}
