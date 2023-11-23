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
package com.braintribe.model.access.smart.manipulation.tools;

import static com.braintribe.model.access.smart.manipulation.tools.SmartManipulationTools.newRefMap;
import static com.braintribe.model.access.smart.manipulation.tools.SmartManipulationTools.newRefSet;
import static com.braintribe.model.generic.manipulation.ManipulationType.ADD;
import static com.braintribe.model.generic.value.EntityReferenceType.persistent;
import static com.braintribe.utils.lcd.CollectionTools2.acquireSet;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;
import static com.braintribe.utils.lcd.CollectionTools2.removeFirst;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.smart.meta.DirectPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.PropertyAssignment;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.processing.smart.SmartAccessException;
import com.braintribe.model.processing.smart.query.planner.structure.ModelExpert;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EmUseCase;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityMapping;

/**
 * @author peter.gazdik
 */
class AccessInferer {

	private static final EmUseCase USE_CASE = null;

	private final Manipulation smartManipulation;
	private final ModelExpert modelExpert;

	private final Map<EntityReference, Set<PropertyManipulation>> manipulationsByOwner = newRefMap();
	private final Map<EntityReference, IncrementalAccess> resolvedAccesses = newRefMap();
	private final Set<EntityReference> resolvedNonVisitedReferences = newRefSet();

	private boolean resolvedDefaultPartitions = false;
	private boolean inferenceRunning = true;

	public AccessInferer(Manipulation smartManipulation, ModelExpert modelExpert) {
		this.smartManipulation = smartManipulation;
		this.modelExpert = modelExpert;

		initialize();
	}

	private void initialize() {
		index(smartManipulation);
		while (inferenceRunning) {
			runDependencyInference();
			runOwnerInference();
			resolveDefaultPartitionsIfEligible();
		}
	}

	public IncrementalAccess resolveAccess(EntityReference smartReference) {
		return resolvedAccesses.get(smartReference);
	}

	private void resolveDefaultPartitionsIfEligible() {
		if (resolvedDefaultPartitions) {
			inferenceRunning = false;
			return;
		}

		resolvedDefaultPartitions = true;
		inferenceRunning = false;

		for (EntityReference ownerRef : manipulationsByOwner.keySet()) {
			IncrementalAccess access = modelExpert.resolveDefaultDelegate(ownerRef.getTypeSignature());
			if (access != null) {
				inferenceRunning = true;
				assignAccess(ownerRef, access);
			}
		}
	}

	// ##########################################
	// ## . . . . . . . Indexing . . . . . . . ##
	// ##########################################

	private void index(Manipulation manipulation) {
		switch (manipulation.manipulationType()) {
			case COMPOUND:
				index((CompoundManipulation) manipulation);
				return;

			case ADD:
			case CHANGE_VALUE:
				index((PropertyManipulation) manipulation);
				return;

			case INSTANTIATION:
				index((InstantiationManipulation) manipulation);
				return;

			default:
				return;
		}
	}

	private void index(CompoundManipulation manipulation) {
		for (Manipulation m : manipulation.getCompoundManipulationList())
			index(m);
	}

	/**
	 * This just makes sure we have an entity for given entity if it was only created. Thus, if we would not assign any
	 * partition anywhere, this would be done in the {@link #resolveDefaultPartitionsIfEligible()} method (see that it
	 * is iterating over the keySet of {@link #manipulationsByOwner}).
	 */
	private void index(InstantiationManipulation im) {
		acquireSet(manipulationsByOwner, (PreliminaryEntityReference) im.getEntity());
	}

	private void index(PropertyManipulation pm) {
		EntityProperty smartOwner = (EntityProperty) pm.getOwner();
		EntityReference ownerRef = smartOwner.getReference();

		acquireSet(manipulationsByOwner, ownerRef).add(pm);
		assignPartitionIfEasy(ownerRef);
		for (EntityReference dependerRef : extractStrongReferences(pm, true))
			assignPartitionIfEasy(dependerRef);
	}

	private void assignPartitionIfEasy(EntityReference smartRef) {
		if (resolvedAccesses.containsKey(smartRef))
			return;

		IncrementalAccess access = resolvePartitionIfEasy(smartRef);
		if (access != null)
			assignAccess(smartRef, access);
	}

	private void assignAccess(EntityReference smartRef, IncrementalAccess access) {
		resolvedAccesses.put(smartRef, access);

		resolvedNonVisitedReferences.add(smartRef);
	}

	// ##########################################
	// ## . Resolving Partition Where Easy . . ##
	// ##########################################

	private IncrementalAccess resolvePartitionIfEasy(EntityReference smartReference) {
		String partition = smartReference.getRefPartition();
		if (partition != null)
			return modelExpert.getAccess(partition);

		return resolveAccessIfMappedToSingleDelegate(smartReference);
	}

	/* Also used by AccessResolver */
	protected final IncrementalAccess resolveAccessIfMappedToSingleDelegate(EntityReference smartReference) {
		GmEntityType smartType = modelExpert.resolveSmartEntityType(smartReference.getTypeSignature());
		Map<IncrementalAccess, EntityMapping> ems = modelExpert.resolveEntityMappingsIfPossible(smartType, null);
		if (ems == null)
			return null;

		if (ems.size() == 1)
			return first(ems.keySet());
		else
			return null;
	}

	// ##########################################
	// ## . . . . . Actual Inference . . . . . ##
	// ##########################################

	private void runDependencyInference() {
		resolvedNonVisitedReferences.addAll(resolvedAccesses.keySet());

		while (!resolvedNonVisitedReferences.isEmpty()) {
			EntityReference reference = removeFirst(resolvedNonVisitedReferences);
			visit(reference);
		}
	}

	private void visit(EntityReference ownerRef) {
		Set<PropertyManipulation> pms = manipulationsByOwner.remove(ownerRef);

		for (PropertyManipulation pm : nullSafe(pms)) {
			Set<EntityReference> dependerRefs = extractStrongReferences(pm, true);

			for (EntityReference dependerRef : dependerRefs)
				ensureSameAccess(ownerRef, dependerRef);
		}
	}

	private void runOwnerInference() {
		while (!manipulationsByOwner.isEmpty()) {
			if (infereOneOwner())
				runDependencyInference();
			else
				return;
		}
	}

	private boolean infereOneOwner() {
		for (Entry<EntityReference, Set<PropertyManipulation>> entry : manipulationsByOwner.entrySet()) {
			EntityReference ownerRef = entry.getKey();
			Set<PropertyManipulation> pms = entry.getValue();

			for (PropertyManipulation pm : nullSafe(pms)) {
				Set<EntityReference> dependerRefs = extractStrongReferences(pm, false);

				for (EntityReference dependerRef : dependerRefs) {
					IncrementalAccess access = resolvedAccesses.get(dependerRef);

					/* It may happen, if we did not manipulate the depender (which can only happen if it is a persistent
					 * entity) that we have not resolved it yet. In such case, let's do it now. */
					if (access == null && dependerRef.referenceType() == persistent)
						access = resolveAccessFor((PersistentEntityReference) dependerRef);

					if (access != null) {
						ensureSameAccess(dependerRef, ownerRef);
						return true;
					}
				}
			}
		}

		return false;
	}

	private IncrementalAccess resolveAccessFor(PersistentEntityReference dependerRef) {
		String partition = dependerRef.getRefPartition();
		IncrementalAccess access = modelExpert.getAccess(partition);

		resolvedAccesses.put(dependerRef, access);

		return access;
	}

	private Set<EntityReference> extractStrongReferences(PropertyManipulation pm, boolean onlyIfAccessResolved) {
		GenericModelType propertyType = findDirectPropertyType(pm, onlyIfAccessResolved);

		if (propertyType == null)
			return Collections.emptySet();

		switch (propertyType.getTypeCode()) {
			case entityType:
				return extractStrongReferenceWhenSingleValue(pm);

			case setType:
			case listType:
				return extractStrongReferencesFromCollection(pm, (CollectionType) propertyType);

			case mapType:
				return extractStrongReferencesFromMap(pm, (CollectionType) propertyType);

			default:
				break;
		}
		return Collections.emptySet();
	}

	private Set<EntityReference> extractStrongReferenceWhenSingleValue(PropertyManipulation pm) {
		// it must be a CVM
		EntityReference ref = (EntityReference) ((ChangeValueManipulation) pm).getNewValue();
		return ref != null ? asSet(ref) : Collections.emptySet();
	}

	private Set<EntityReference> extractStrongReferencesFromCollection(PropertyManipulation pm, CollectionType ct) {
		Set<EntityReference> result = newSet();

		if (ct.getParameterization()[0] instanceof EntityType) {
			boolean isAdd = pm.manipulationType() == ADD;

			Collection<?> linkedCollection = isAdd ? ((AddManipulation) pm).getItemsToAdd().values()
					: (Collection<?>) ((ChangeValueManipulation) pm).getNewValue();

			addLinkedRefs(result, linkedCollection);
		}

		return result;
	}

	private Set<EntityReference> extractStrongReferencesFromMap(PropertyManipulation pm, CollectionType ct) {
		Set<EntityReference> result = newSet();
		boolean isAdd = pm.manipulationType() == ADD;

		Map<?, ?> linkedMap = isAdd ? ((AddManipulation) pm).getItemsToAdd() : (Map<?, ?>) ((ChangeValueManipulation) pm).getNewValue();

		if (ct.getParameterization()[0] instanceof EntityType)
			addLinkedRefs(result, linkedMap.keySet());

		if (ct.getParameterization()[1] instanceof EntityType)
			addLinkedRefs(result, linkedMap.values());

		return result;
	}

	private void addLinkedRefs(Set<EntityReference> set, Collection<?> added) {
		for (Object ref : added)
			if (ref != null)
				set.add((EntityReference) ref);
	}

	private GenericModelType findDirectPropertyType(PropertyManipulation pm, boolean onlyIfAccessResolved) {
		EntityProperty currentSmartOwner = (EntityProperty) pm.getOwner();
		EntityReference smartReference = currentSmartOwner.getReference();
		String smartProperty = currentSmartOwner.getPropertyName();
		String smartSignature = smartReference.getTypeSignature();

		GmEntityType smartType = modelExpert.resolveSmartEntityType(smartSignature);
		IncrementalAccess access = resolvedAccesses.get(smartReference);

		if (access != null) {
			PropertyAssignment pa = resolvePropertyAssignment(smartType, access, smartProperty);
			if (!(pa instanceof DirectPropertyAssignment))
				return null;

		} else if (!onlyIfAccessResolved) {
			/* Check that every possible assignment is a DirectPropertyAssignment, then we know the dependency must be
			 * within the same access */
			Map<IncrementalAccess, PropertyAssignment> pas = modelExpert.resolvePropertyAssignmentsIfPossible(smartType, smartProperty, USE_CASE);
			for (PropertyAssignment pa : pas.values()) {
				if (!(pa instanceof DirectPropertyAssignment))
					return null;
			}

		} else {
			return null;
		}

		EntityType<GenericEntity> et = GMF.getTypeReflection().getType(smartSignature);
		return et.getProperty(smartProperty).getType();
	}

	private PropertyAssignment resolvePropertyAssignment(GmEntityType smartType, IncrementalAccess access, String smartProperty) {
		return modelExpert.resolvePropertyAssignment(smartType, access, smartProperty, USE_CASE);
	}

	private void ensureSameAccess(EntityReference ownerRef, EntityReference dependerRef) {
		IncrementalAccess ownerAccess = resolvedAccesses.get(ownerRef);
		IncrementalAccess dependerAccess = resolvedAccesses.get(dependerRef);

		if (dependerAccess != null) {
			if (dependerAccess != ownerAccess) {
				throw new SmartAccessException("Manipulation validation failed. The references '" + ownerRef + "' and '" + dependerRef
						+ "' must have the same access, but the resolved accesses were '" + ownerAccess.getExternalId() + "' and '"
						+ dependerAccess.getExternalId() + "', respectively.");
			}
			return;
		}

		resolvedAccesses.put(dependerRef, ownerAccess);
		resolvedNonVisitedReferences.add(dependerRef);
	}
}
