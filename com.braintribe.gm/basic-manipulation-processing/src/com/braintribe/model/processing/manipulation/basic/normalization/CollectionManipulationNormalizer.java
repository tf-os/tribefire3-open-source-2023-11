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

import static com.braintribe.model.processing.manipulation.basic.tools.ManipulationTools.createUpdatedReference;
import static com.braintribe.model.processing.manipulation.basic.tools.ManipulationTools.newGlobalReference;
import static com.braintribe.utils.lcd.CollectionTools2.acquireMap;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.CollectionManipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.manipulation.basic.BasicMutableManipulationContext;

/**
 * This class is responsible for keeping track of {@link AtomicManipulation}s performed on property of type map/collection. The process how the
 * aggregation (merging) of various manipulations is computed is different for different collection types. See specific trackers for more information.
 * 
 * @see ListTracker
 * @see SetTracker
 * @see MapTracker
 */
class CollectionManipulationNormalizer {

	private final List<AtomicManipulation> manipulations;
	private final NormalizationContext normalizationContext;

	private final BasicMutableManipulationContext context;

	private final Map<EntityReference, Map<String, CollectionTracker>> collectionTrackers = newMap();
	private final Set<Integer> manipulationsToDelete = newSet();

	/**
	 * We keep track of instantiated entities so that whenever we encounter a collection manipulation on such, we notify the tracker that we are
	 * starting with empty collection, thus the result will always be a {@link ChangeValueManipulation}.
	 */
	private final Set<EntityReference> createdEntities = newSet();

	private CollectionType propertyType;

	public CollectionManipulationNormalizer(List<AtomicManipulation> manipulations, NormalizationContext normalizationContext) {
		this.manipulations = manipulations;
		this.normalizationContext = normalizationContext;
		this.context = normalizationContext.manipulationContext;
	}

	public List<AtomicManipulation> normalize() {
		/* Examine manipulations */
		Integer index = 0;
		for (AtomicManipulation manipulation : manipulations) {
			context.setCurrentManipulationSafe(manipulation);

			if (examineManipulation())
				manipulationsToDelete.add(index);

			index++;
		}

		/* Execute the actual removing of manipulations */
		for (int positionToDelete : positionsToDeleteDescending()) {
			manipulations.remove(positionToDelete);
		}

		/* Insert merged manipulations instead of the removed ones */
		for (Map<String, CollectionTracker> propertyTrackers : collectionTrackers.values())
			for (CollectionTracker tracker : propertyTrackers.values())
				tracker.appendAggregateManipulations(manipulations, normalizationContext.entitiesToDelete);

		return manipulations;
	}

	private List<Integer> positionsToDeleteDescending() {
		List<Integer> positionsToDelete = newList(manipulationsToDelete);
		Collections.sort(positionsToDelete, Collections.reverseOrder());

		return positionsToDelete;
	}

	private boolean examineManipulation() {
		if (context.getTargetPropertyName() == null) {
			noteInstantiationIfPossible();

			return false;
		}

		if (!initializeCollectionProperty()) {
			updateInstantiationIfEligible();

			return false;
		}

		CollectionTracker tracker = acquireCollectionTracker();

		switch (context.getCurrentManipulationType()) {
			case CHANGE_VALUE:
				tracker.onChangeValue((ChangeValueManipulation) context.getCurrentManipulation());
				return true;

			case CLEAR_COLLECTION:
				tracker.onClearCollection();
				return true;

			case ADD:
				tracker.onBulkInsert((AddManipulation) context.getCurrentManipulation());
				return true;

			case REMOVE:
				tracker.onBulkRemove((RemoveManipulation) context.getCurrentManipulation());
				return true;

			default:
				return false;
		}
	}

	private void noteInstantiationIfPossible() {
		if (context.getCurrentManipulationType() == ManipulationType.INSTANTIATION)
			createdEntities.add(context.getNormalizedTargetReference());
	}

	/**
	 * We update the reference in {@link #createdEntities} if this manipulation is a {@link ChangeValueManipulation} on an identifying property of
	 * such. We need to do that, because the manipulation stack is flawed and assigning an id/partition property means from now on the entity will be
	 * referenced with a new reference.
	 */
	private void updateInstantiationIfEligible() {
		if (context.getCurrentManipulationType() != ManipulationType.CHANGE_VALUE)
			return;

		if (!normalizationContext.isIdentifying(context.getTargetProperty()))
			return;

		EntityReference targetRef = context.getNormalizedTargetReference();

		if (createdEntities.remove(targetRef)) {
			ChangeValueManipulation cvm = context.getCurrentManipulation();
			EntityReference newRef = normalizationContext.globalRefs ? //
					newGlobalReference(true, ((EntityReference) cvm.manipulatedEntity()).getTypeSignature(), cvm.getNewValue()) : //
					createUpdatedReference(targetRef, cvm.getOwner().getPropertyName(), cvm.getNewValue());
			createdEntities.add(context.getNormalizeReference(newRef));
		}
	}

	private CollectionTracker acquireCollectionTracker() {
		Map<String, CollectionTracker> trackersForProperties = acquireMap(collectionTrackers, context.getNormalizedTargetReference());

		CollectionTracker tracker = trackersForProperties.get(context.getTargetPropertyName());
		if (tracker == null) {
			tracker = newTracker();
			trackersForProperties.put(context.getTargetPropertyName(), tracker);

			if (createdEntities.contains(context.getNormalizedTargetReference()))
				tracker.onClearCollection();
		}

		return tracker;
	}

	private CollectionTracker newTracker() {
		Owner owner = ((PropertyManipulation) context.getCurrentManipulation()).getOwner();
		String propertySignature = context.getTargetProperty().getType().getTypeSignature();

		switch (propertyType.getCollectionKind()) {
			case list:
				return new ListTracker(owner, propertySignature);

			case map:
				return new MapTracker(owner, propertySignature);

			case set:
				return new SetTracker(owner, propertySignature);
		}

		throw new RuntimeException("Unsupported collection kind: " + propertyType.getCollectionKind());
	}

	private boolean initializeCollectionProperty() {
		GenericModelType propertyType = context.getTargetProperty().getType();

		if (propertyType.isBase()) {
			if (context.getCurrentManipulationType() == ManipulationType.CHANGE_VALUE) {
				ChangeValueManipulation cvm = context.getCurrentManipulation();
				propertyType = SimpleManipulationNormalizer.resolveCollectionType(cvm.getNewValue());

				if (propertyType == null)
					return false;

			} else if (context.getCurrentManipulation() instanceof CollectionManipulation) {
				Map<String, CollectionTracker> trackersForProperties = acquireMap(collectionTrackers, context.getNormalizedTargetReference());
				CollectionTracker tracker = trackersForProperties.get(context.getTargetPropertyName());
				if (tracker == null)
					return false;

				// we can keep the type null, it won't be needed, as it is only needed to create a new tracker when none exists
				this.propertyType = null;
				return true;
			}

		} else if (!propertyType.isCollection()) {
			return false;
		}

		this.propertyType = (CollectionType) propertyType;

		return true;
	}

}
