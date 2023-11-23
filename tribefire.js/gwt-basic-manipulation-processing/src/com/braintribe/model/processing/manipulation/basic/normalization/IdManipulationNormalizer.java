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

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.changeValue;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.entityProperty;
import static com.braintribe.model.processing.manipulation.basic.tools.ManipulationTools.newReference;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.updateMapKey;
import static com.braintribe.utils.lcd.CommonTools.cast;
import static com.braintribe.utils.lcd.CommonTools.equalsOrBothNull;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.CollectionManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EssentialCollectionTypes;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.processing.manipulation.basic.BasicMutableManipulationContext;
import com.braintribe.model.processing.manipulation.basic.tools.clone.ManipulationCloner;

/**
 * This normalizer simply makes sure that if we are editing the id/partition property, we do this as the first two manipulations (after
 * {@link InstantiationManipulation} if preliminary). Also, in a special case that the entity is deleted, the identifying-property
 * manipulations are also removed by the normalizer, as they are not needed.
 * 
 * Note that this first checks if there is at least one identifying-property manipulation, and if not (the most usual case), the original
 * list of manipulations is returned immediately.
 * 
 * @see Normalizer
 * @see SimpleManipulationNormalizer
 * @see CollectionManipulationNormalizer
 */
class IdManipulationNormalizer {

	private final List<AtomicManipulation> manipulations;
	private final BasicMutableManipulationContext context;

	private final Map<EntityReference, ReferenceEntry> referenceEntries = newMap();

	public IdManipulationNormalizer(List<AtomicManipulation> manipulations, NormalizationContext normalizationContext) {
		this.manipulations = manipulations;
		this.context = normalizationContext.manipulationContext;
	}

	public List<AtomicManipulation> normalize() {
		if (!containsIdentifyingPropertyChange())
			return manipulations;

		List<AtomicManipulation> ams = newList();

		// process manipulations
		for (AtomicManipulation am: manipulations) {
			context.setCurrentManipulationSafe(am);
			am = process(am);
			if (am != null) {
				ams.add(am);
			}
		}

		// add instantiations with identifier initialization
		List<AtomicManipulation> result = newList();
		for (ReferenceEntry refEntry: referenceEntries.values()) {
			refEntry.updateReferences();

			String signature = refEntry.reference.getTypeSignature();

			// instantiation -> initialId and initialPartition (both null, can reuse original Instantiation of course)
			if (refEntry.instantiationManipulation != null) {
				result.add(refEntry.instantiationManipulation);
			}

			// changeId -> initialId and initialPartition
			if (refEntry.idPropertyName != null &&
					(refEntry.wasOrinigallyPreliminary() || !equalsOrBothNull(refEntry.initialId, refEntry.currentId))) {

				boolean persistent = !refEntry.wasOrinigallyPreliminary();

				EntityReference ownerRef = newReference(persistent, signature, refEntry.initialId, refEntry.initialPartition);
				result.add(changeValue(refEntry.currentId, entityProperty(ownerRef, refEntry.idPropertyName)));
			}

			// changePartition -> currentId and initialPartition
			if (refEntry.partitionChanged && !equalsOrBothNull(refEntry.initialPartition, refEntry.currentPartition)) {
				boolean persistent = !refEntry.wasOrinigallyPreliminary() || refEntry.idPropertyName != null;
				EntityReference ownerRef = newReference(persistent, signature, refEntry.currentId, refEntry.initialPartition);
				result.add(changeValue(refEntry.currentPartition, entityProperty(ownerRef, GenericEntity.partition)));
			}
		}

		result.addAll(ams);

		return result;
	}

	private boolean containsIdentifyingPropertyChange() {
		for (AtomicManipulation am: manipulations) {
			if (am.manipulationType() == ManipulationType.CHANGE_VALUE) {
				context.setCurrentManipulationSafe(am);

				if (context.getTargetProperty().isIdentifying() && ((ChangeValueManipulation) am).getNewValue() != null) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * @return true iff we want to keep that manipulation as part of the result, for now. We want to remove all the Instantiations and
	 *         identifying property changes, cause they will be added later at the very beginning.
	 */
	@SuppressWarnings("incomplete-switch")
	private AtomicManipulation process(AtomicManipulation m) {
		switch (m.manipulationType()) {
			case ABSENTING: // should not happen
			case MANIFESTATION: // should not happen
				return m;

			case CLEAR_COLLECTION:
				m = clone(m);
				notifyPropertyManipulation((PropertyManipulation) m);
				return m;

			case INSTANTIATION:
				process((InstantiationManipulation) m);
				return null;

			case DELETE:
				return handleCurrentTargetRefEntryAsDeleted((DeleteManipulation) m);

			case CHANGE_VALUE:
				return process((ChangeValueManipulation) m);

			case ADD: {
				m = clone(m);
				AddManipulation am = (AddManipulation) m;
				return processCollectionEdit(am, am.getItemsToAdd());
			}
			case REMOVE: {
				m = clone(m);
				RemoveManipulation rm = (RemoveManipulation) m;
				return processCollectionEdit(rm, rm.getItemsToRemove());
			}
		}

		return m;
	}

	private AtomicManipulation processCollectionEdit(CollectionManipulation m, Map<Object, Object> items) {
		Property p = context.getTargetProperty();

		GenericModelType type = p.getType();
		if (type.isBase())
			type = EssentialCollectionTypes.TYPE_MAP;

		CollectionType collectionType = (CollectionType) type;

		notifyPropertyManipulation(m);

		if (!collectionType.areEntitiesReachable())
			return m;

		switch (collectionType.getCollectionKind()) {
			case list:
				processListAsMapEdit(items);
				return m;
			case set:
				processSetAsMapEdit(items);
				return m;
			case map:
				processMapEdit(items);
				return m;
		}

		throw new RuntimeException("Unsuppored collection kind: " + collectionType.getCollectionKind());
	}

	// #####################################################
	// ## . . . . . . Collection Adds/Removes . . . . . . ##
	// #####################################################

	private void processListAsMapEdit(Map<Object, Object> items) {
		for (Entry<Object, Object> entry: items.entrySet()) {
			Integer index = (Integer) entry.getKey();
			Object value = entry.getValue();

			if (value instanceof EntityReference) {
				EntityReference ref = (EntityReference) value;
				ReferenceEntry refEntry = acquireReferenceEntry(ref);

				refEntry.referenceItems.add(new ListAsMapReference(items, index));
			}
		}
	}

	private void processListEdit(List<?> items) {
		int counter = 0;
		for (Object value: items) {

			if (value instanceof EntityReference) {
				EntityReference ref = (EntityReference) value;
				ReferenceEntry refEntry = acquireReferenceEntry(ref);

				refEntry.referenceItems.add(new ListReference(items, counter));
			}
			counter++;
		}
	}

	private void processSetAsMapEdit(Map<Object, Object> items) {
		for (Object value: items.keySet()) {
			if (value instanceof EntityReference) {
				EntityReference ref = (EntityReference) value;
				ReferenceEntry refEntry = acquireReferenceEntry(ref);

				refEntry.referenceItems.add(new SetAsMapReference(items, ref));
			}
		}
	}

	private void processSetEdit(Set<?> items) {
		for (Object value: items) {
			if (value instanceof EntityReference) {
				EntityReference ref = (EntityReference) value;
				ReferenceEntry refEntry = acquireReferenceEntry(ref);

				refEntry.referenceItems.add(new SetReference(items, ref));
			}
		}
	}

	private void processMapEdit(Map<?, ?> items) {
		for (Entry<?, ?> entry: items.entrySet()) {
			Object key = entry.getKey();
			Object value = entry.getValue();

			MapValueReference mvr = null;

			if (value instanceof EntityReference) {
				EntityReference ref = (EntityReference) value;
				ReferenceEntry refEntry = acquireReferenceEntry(ref);

				mvr = new MapValueReference(items, key);
				refEntry.referenceItems.add(mvr);
			}

			if (key instanceof EntityReference) {
				EntityReference ref = (EntityReference) key;
				ReferenceEntry refEntry = acquireReferenceEntry(ref);

				refEntry.referenceItems.add(new MapKeyReference(items, ref, mvr));
			}
		}
	}

	// #####################################################
	// ## . . . . . . . Instantiation/Delete . . . . . . .##
	// #####################################################

	private void process(InstantiationManipulation m) {
		EntityReference ref = context.getNormalizedTargetReference();
		referenceEntries.put(ref, new ReferenceEntry(ref, m));
	}

	private DeleteManipulation handleCurrentTargetRefEntryAsDeleted(DeleteManipulation m) {
		EntityReference ref = context.getNormalizedTargetReference();
		ReferenceEntry refEntry = referenceEntries.get(ref);

		if (refEntry != null && refEntry.removeAnyTracksOfChangingIdentifyingProperties()) {
			m = clone(m);
			m.setEntity(refEntry.currentReference());
		}

		return m;
	}

	private AtomicManipulation process(ChangeValueManipulation m) {
		Property p = context.getTargetEntityType().findProperty(context.getTargetPropertyName());
		if (p == null) {
			// target property not found. ignore manipulation.
			return null;
		}

		if (p.isIdentifying()) {
			processIdentifierChange(m.getNewValue(), p);
			return null;

		} else {
			m = clone(m);
			Object newVal = m.getNewValue();
			notifyPropertyManipulation(m);

			if (newVal instanceof EntityReference) {
				ReferenceEntry refEntry = acquireReferenceEntry((EntityReference) newVal);
				refEntry.referenceItems.add(new CvmReference(m));

			} else if (newVal instanceof Set) {
				processSetEdit((Set<?>) newVal);

			} else if (newVal instanceof List) {
				processListEdit((List<?>) newVal);

			} else if (newVal instanceof Map) {
				processMapEdit((Map<?, ?>) newVal);
			}
		}

		return m;
	}

	private void processIdentifierChange(Object newVal, Property p) {
		EntityReference ref = context.getNormalizedTargetReference();
		ReferenceEntry refEntry = acquireReferenceEntry(ref);

		if (p.isIdentifier()) {
			if (newVal == null) {
				/* If id is assigned null value we ignore it for preliminary or throw exception for persistent. */
				throwExceptionIfPersistent(ref);
				return;
			}

			refEntry.currentId = newVal;
			refEntry.idPropertyName = p.getName();

		} else {
			refEntry.currentPartition = (String) newVal;
			refEntry.partitionChanged = true;
		}

		EntityReference newRef = context.getNormalizeReference(refEntry.currentReference());
		updateMapKey(referenceEntries, ref, newRef);
	}

	private void throwExceptionIfPersistent(EntityReference ref) {
		if (ref instanceof PersistentEntityReference) {
			throw new RuntimeException("Cannot set id to null for persistent reference: " + ref.getTypeSignature() + "[id: " + ref.getRefId() +
					", partition: " + ref.getRefPartition() + "]");
		}
	}

	private void notifyPropertyManipulation(PropertyManipulation m) {
		acquireReferenceEntry(context.getNormalizedTargetReference()).propertyManipulations.add(m);
	}

	private <M extends AtomicManipulation> M clone(M m) {
		return ManipulationCloner.clone(m, false);
	}

	// #####################################################
	// ## . . . . . . EntityReference helpers . . . . . . ##
	// #####################################################

	private ReferenceEntry acquireReferenceEntry(EntityReference ref) {
		EntityReference normalizedRef = context.getNormalizeReference(ref);
		ReferenceEntry result = referenceEntries.get(normalizedRef);

		if (result == null) {
			result = new ReferenceEntry(normalizedRef, null);
			referenceEntries.put(normalizedRef, result);
		}

		return result;
	}

	static class ReferenceEntry {
		public final EntityReference reference;
		public final InstantiationManipulation instantiationManipulation;
		public final Object initialId;
		public final String initialPartition;

		public Object currentId;
		public String currentPartition;

		public String idPropertyName; // this not being null indicates id was changed
		public boolean partitionChanged;

		public List<PropertyManipulation> propertyManipulations = newList();
		public List<ReferenceItem> referenceItems = newList();

		public ReferenceEntry(EntityReference reference, InstantiationManipulation instantiationManipulation) {
			this.reference = reference;
			this.instantiationManipulation = instantiationManipulation;
			this.initialId = this.currentId = reference.getRefId();
			this.initialPartition = this.currentPartition = reference.getRefPartition();
		}

		/**
		 * returns true iff the {@link DeleteManipulation} needs to be adjusted (i.e. if we changed some identifying properties, we need to
		 * set change that manipulation to contain the original reference before the change)
		 */
		public boolean removeAnyTracksOfChangingIdentifyingProperties() {
			if (!partitionChanged && !idPropertyChanged()) {
				// no identifying property was touched
				propertyManipulations.clear();
				referenceItems.clear();

				return false;
			}

			partitionChanged = false;
			idPropertyName = null;

			currentId = initialId;
			currentPartition = initialPartition;
			return true;
		}

		public void updateReferences() {
			EntityReference newReference = currentReference();

			for (PropertyManipulation pm: propertyManipulations)
				((EntityProperty) pm.getOwner()).setReference(newReference);

			for (ReferenceItem refItem: referenceItems)
				refItem.updateReference(newReference);
		}

		private EntityReference currentReference() {
			boolean persistent = !wasOrinigallyPreliminary() || idPropertyChanged();
			return newReference(persistent, reference.getTypeSignature(), currentId, currentPartition);
		}

		private boolean idPropertyChanged() {
			return idPropertyName != null;
		}

		public boolean wasOrinigallyPreliminary() {
			return reference instanceof PreliminaryEntityReference;
		}
	}

	static abstract class ReferenceItem {
		abstract void updateReference(EntityReference reference);
	}

	static class CvmReference extends ReferenceItem {
		private final ChangeValueManipulation cvm;

		public CvmReference(ChangeValueManipulation cvm) {
			this.cvm = cvm;
		}

		@Override
		void updateReference(EntityReference reference) {
			cvm.setNewValue(reference);
		}
	}

	static class ListAsMapReference extends ReferenceItem {
		private final Map<Object, Object> list;
		private final Integer index;

		public ListAsMapReference(Map<Object, Object> list, Integer index) {
			this.list = list;
			this.index = index;
		}

		@Override
		void updateReference(EntityReference reference) {
			list.put(index, reference);
		}
	}

	static class ListReference extends ReferenceItem {
		private final List<Object> list;
		private final Integer index;

		public ListReference(List<?> list, Integer index) {
			this.list = cast(list);
			this.index = index;
		}

		@Override
		void updateReference(EntityReference reference) {
			list.set(index, reference);
		}
	}

	static class SetAsMapReference extends ReferenceItem {
		private final Map<Object, Object> set;
		private EntityReference currentReference;

		public SetAsMapReference(Map<Object, Object> set, EntityReference currentReference) {
			this.set = set;
			this.currentReference = currentReference;
		}

		@Override
		void updateReference(EntityReference reference) {
			set.remove(currentReference);
			set.put(reference, reference);
			currentReference = reference;
		}
	}

	static class SetReference extends ReferenceItem {
		private final Set<Object> set;
		private EntityReference currentReference;

		public SetReference(Set<?> set, EntityReference currentReference) {
			this.set = cast(set);
			this.currentReference = currentReference;
		}

		@Override
		void updateReference(EntityReference reference) {
			set.remove(currentReference);
			set.add(reference);
			currentReference = reference;
		}
	}

	static class MapKeyReference extends ReferenceItem {
		private final Map<Object, Object> map;
		private EntityReference currentReference;
		private final MapValueReference mapValueReference;

		public MapKeyReference(Map<?, ?> map, EntityReference currentReference, MapValueReference mapValueReference) {
			this.map = cast(map);
			this.currentReference = currentReference;
			this.mapValueReference = mapValueReference;
		}

		@Override
		void updateReference(EntityReference reference) {
			Object value = map.remove(currentReference);
			map.put(reference, value);
			currentReference = reference;

			if (mapValueReference != null) {
				mapValueReference.key = reference;
			}
		}
	}

	static class MapValueReference extends ReferenceItem {
		Map<Object, Object> map;
		Object key;

		public MapValueReference(Map<?, ?> map, Object key) {
			this.map = cast(map);
			this.key = key;
		}

		@Override
		void updateReference(EntityReference reference) {
			map.put(key, reference);
		}
	}

}
