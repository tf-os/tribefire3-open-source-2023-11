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
package com.braintribe.model.processing.manipulation.basic.normalization;

import static com.braintribe.utils.lcd.CollectionTools.removeByIndices;
import static com.braintribe.utils.lcd.CollectionTools2.acquireList;
import static com.braintribe.utils.lcd.CollectionTools2.acquireMap;
import static com.braintribe.utils.lcd.CollectionTools2.acquireSet;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EssentialCollectionTypes;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.processing.manipulation.basic.BasicMutableManipulationContext;

/**
 * Simple normalizer for a list of {@link AtomicManipulation}s. This normalizer does not examine the details of manipulations, just removes some
 * obviously irrelevant manipulations, based on the target entities/properties. For compacting the list it applies the following rules:
 * 
 * <ul>
 * <li>If an entity is uninstantiated, all manipulations related to that entity may just be deleted. (We assume that one of the Manipulations must
 * have been the instantiation)</li>
 * <li>If an entity is deleted, and then undeleted, these two manipulations may be removed.</li>
 * <li>If an entity is deleted (without an undelete later), all other manipulations related to that entity may be removed (but the deletion itself
 * must be preserved).</li>
 * <li>If a property value is changed multiple times, only the last change is relevant, all previous may be removed. Note that clearing a collection
 * is considered as changing property value as well.</li>
 * <li>If a property of type collection is edited (insert/replace/remove/clear) and later a the entire value is changed (using
 * {@link ChangeValueManipulation} or {@link ClearCollectionManipulation}), then the previous edits may be all removed.</li>
 * </ul>
 * 
 * @see Normalizer
 * @see IdManipulationNormalizer
 * @see CollectionManipulationNormalizer
 */
class SimpleManipulationNormalizer {

	private final List<AtomicManipulation> manipulations;
	private final NormalizationContext normalizationContext;
	private final BasicMutableManipulationContext context;

	/* For given entity, stores all manipulations other than DeleteManipulation */
	private final Map<EntityReference, List<Integer>> instanceRelatedManipulations = newMap();
	/* For given entity, stores all CVM manipulations which set given entity as value */
	private final Map<EntityReference, Set<ChangeValueManipulation>> entitySettingManipulations = newMap();
	/* For given entity, stores the index of last DeleteManipulation */
	private final Map<EntityReference, Integer> deleteManipulations = newMap();
	/* For given entity, propertyName, stores the list of all CollectionManipulations */
	private final Map<EntityReference, Map<String, List<Integer>>> collectionManipulations = newMap();
	/* For given entity, propertyName, stores the index of the last ChangeValueManipulation */
	private final Map<EntityReference, Map<String, Integer>> changeValueManipulations = newMap();

	/* For given entity, property of type object, stores collectionType in case a collection is assigned */
	private final Map<EntityReference, Map<String, CollectionType>> baseCollectionTypes = newMap();
	/* For given manipulation index of a ClearCollection on a base property stores the CVM to replace it. */
	private final Map<Integer, ChangeValueManipulation> cvmForBaseClears = newMap();

	private final Set<Integer> manipulationsToDelete = newSet();

	public SimpleManipulationNormalizer(List<AtomicManipulation> manipulations, NormalizationContext normalizationContext) {
		this.manipulations = manipulations;
		this.normalizationContext = normalizationContext;
		this.context = normalizationContext.manipulationContext;
	}

	public List<AtomicManipulation> normalize() {
		/* Examine manipulations */
		int index = 0;
		for (AtomicManipulation manipulation : manipulations) {
			context.setCurrentManipulationSafe(manipulation);
			examineManipulation(index++);
		}

		// replace all clears of base-type props to CVM with proper empty collection
		for (Entry<Integer, ChangeValueManipulation> e : cvmForBaseClears.entrySet())
			manipulations.set(e.getKey(), e.getValue());

		/* For all deleted entities, mark all editing manipulations as eligible for delete */
		for (EntityReference deletedEntity : deleteManipulations.keySet()) {
			manipulationsToDelete.addAll(nullSafe(instanceRelatedManipulations.get(deletedEntity)));

			for (ChangeValueManipulation entitySettingManipulation : nullSafe(entitySettingManipulations.get(deletedEntity)))
				entitySettingManipulation.setNewValue(null);
		}

		/* Execute the actual removing of manipulations */
		return removeByIndices(manipulations, manipulationsToDelete);
	}

	private void examineManipulation(Integer index) {
		if (context.getTargetReference() == null)
			return;

		switch (context.getCurrentManipulationType()) {
			case CHANGE_VALUE:
				checkIfEntityBeingSet();
				/* We handle clearCollection as CVM, but we check something extra for CVM */
				//$FALL-THROUGH$
			case CLEAR_COLLECTION:
				notifyNewValue(index);
				return;

			case DELETE:
				notifyDelete(index);
				return;

			case INSTANTIATION:
				notifyInstantiation(index);
				return;

			case ADD:
			case REMOVE:
				notifyCollectionOperation(index);
				return;

			default:
				return;
		}
	}

	private void notifyNewValue(Integer index) {
		/* first remember this change, and if there was a change before, mark the previous one to be deleted */
		EntityReference reference = context.getNormalizedTargetReference();
		String propertyName = context.getTargetPropertyName();

		if (context.getCurrentManipulationType() == ManipulationType.CHANGE_VALUE) {
			ChangeValueManipulation cvm = context.getCurrentManipulation();
			Object newValue = cvm.getNewValue();
			Property targetProperty = context.getTargetProperty();

			// ids are already handled by IdManipulationNormalizer, here we just do a cleanup
			if (newValue == null && normalizationContext.isIdentifier(targetProperty)) {
				manipulationsToDelete.add(index);
				return;
			}

			// When we set a collection on an object property, we want to remember the type
			// Then, any subsequent clear can be replaced with a CVM with an empty collection
			if (targetProperty.getType().isBase()) {
				CollectionType ct = resolveCollectionType(newValue);
				if (ct != null)
					acquireMap(baseCollectionTypes, reference).put(propertyName, ct);
			}

		} else if (context.getCurrentManipulationType() == ManipulationType.CLEAR_COLLECTION) {
			CollectionType ct = acquireMap(baseCollectionTypes, reference).get(propertyName);
			if (ct != null) {
				ClearCollectionManipulation cc = context.getCurrentManipulation();

				ChangeValueManipulation cvm = ChangeValueManipulation.T.create();
				cvm.setOwner(cc.getOwner());
				cvm.setNewValue(ct.createPlain());

				cvmForBaseClears.put(index, cvm);
			}
		}

		Map<String, Integer> propNameToLastChanged = acquireMap(changeValueManipulations, reference);
		// if it was there already, remove it
		Integer prevChangeIndex = propNameToLastChanged.get(propertyName);
		if (prevChangeIndex != null)
			manipulationsToDelete.add(prevChangeIndex);

		/* remember this manipulation as the last one to change given property */
		propNameToLastChanged.put(propertyName, index);

		/* If the property has type Map/Collection and was edited before, mark all previous edits as delete-able */
		removeCollectionEditsIfEligible(reference, propertyName);

		notifyInstanceRelatedManipulation(reference, index);
	}

	public static CollectionType resolveCollectionType(Object newValue) {
		if (newValue instanceof List)
			return EssentialCollectionTypes.TYPE_LIST;
		if (newValue instanceof Set)
			return EssentialCollectionTypes.TYPE_SET;
		if (newValue instanceof Map)
			return EssentialCollectionTypes.TYPE_MAP;

		return null;
	}

	private void removeCollectionEditsIfEligible(GenericEntity instance, String propertyName) {
		Map<String, List<Integer>> collectionPropertyManis = collectionManipulations.get(instance);

		if (collectionPropertyManis == null)
			return;

		List<Integer> collectionEdits = collectionPropertyManis.get(propertyName);
		if (collectionEdits == null)
			return;

		manipulationsToDelete.addAll(collectionEdits);
		collectionEdits.clear();
	}

	private void notifyDelete(Integer index) {
		EntityReference reference = context.getNormalizedTargetReference();

		deleteManipulations.put(reference, index);
		normalizationContext.entitiesToDelete.add(reference);

		if (reference instanceof PreliminaryEntityReference)
			manipulationsToDelete.add(index);
	}

	private void notifyInstantiation(Integer index) {
		notifyInstanceRelatedManipulation(context.getNormalizedTargetReference(), index);
	}

	private void notifyCollectionOperation(Integer index) {
		EntityReference reference = context.getNormalizedTargetReference();
		String propertyName = context.getTargetPropertyName();

		/* Remember this manipulation as collection manipulation for given entity, propertyName */
		Map<String, List<Integer>> collectionPropertyManis = collectionManipulations.get(reference);

		if (collectionPropertyManis == null) {
			collectionPropertyManis = newMap();
			collectionManipulations.put(reference, collectionPropertyManis);
		}

		List<Integer> collectionEdits = collectionPropertyManis.get(propertyName);
		if (collectionEdits == null) {
			collectionEdits = newList();
			collectionPropertyManis.put(propertyName, collectionEdits);
		}

		collectionEdits.add(index);

		notifyInstanceRelatedManipulation(reference, index);
	}

	private void notifyInstanceRelatedManipulation(EntityReference reference, Integer index) {
		acquireList(instanceRelatedManipulations, reference).add(index);
	}

	private void checkIfEntityBeingSet() {
		ChangeValueManipulation manipulation = context.getCurrentManipulation();
		Object newValue = manipulation.getNewValue();

		if (newValue instanceof EntityReference) {
			EntityReference reference = context.getNormalizeReference((EntityReference) newValue);
			acquireSet(entitySettingManipulations, reference).add(manipulation);
		}
	}

}
