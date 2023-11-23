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
package com.braintribe.model.processing.session.impl.managed.merging;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.enhance.EntityFlags;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.util.HistorySuspension;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TransientProperty;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.session.impl.managed.IdentityCompetence;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedCollection;
import com.braintribe.processing.async.api.AsyncCallback;

public class ContinuableMerger<M> {

	private final IdentityCompetence identityCompetence;
	private final HistorySuspension historySuspension;

	private final boolean adopt;
	private final boolean transferTransientProperties;
	private final Function<GenericEntity, GenericEntity> envelopeFactory;

	private final Map<GenericEntity, GenericEntity> sourceDrainMap = newMap();

	protected M mergedData;
	protected M data;

	public ContinuableMerger(IdentityCompetence identityCompetence, boolean adopt) {
		this(identityCompetence, null, adopt, null);
	}

	public ContinuableMerger(IdentityCompetence identityCompetence, HistorySuspension historySuspension, boolean adopt) {
		this(identityCompetence, historySuspension, adopt, null);
	}

	public ContinuableMerger(IdentityCompetence identityCompetence, HistorySuspension historySuspension, boolean adopt,
			Function<GenericEntity, GenericEntity> envelopeFactory) {
		this(identityCompetence, historySuspension, adopt, envelopeFactory, false);
	}

	public ContinuableMerger(IdentityCompetence identityCompetence, HistorySuspension historySuspension, boolean adopt,
			Function<GenericEntity, GenericEntity> envelopeFactory, boolean transferTransientProperties) {

		this.identityCompetence = identityCompetence;
		this.adopt = adopt;
		this.historySuspension = historySuspension;
		this.envelopeFactory = envelopeFactory;
		this.transferTransientProperties = transferTransientProperties;
	}

	public M merge(M data) throws GmSessionException {
		initialize(data);

		suspendHistory();

		try {
			while (currentStep != null) {
				currentStep.doStep();
				currentStep = currentStep.next;
			}
			return mergedData;

		} catch (Exception e) {
			throw new GmSessionException("error while merging data", e);

		} finally {
			resumeHistory();
		}
	}

	protected void suspendHistory() {
		if (historySuspension != null)
			historySuspension.suspendHistory();
	}

	protected void resumeHistory() {
		if (historySuspension != null)
			historySuspension.resumeHistory();
	}

	public void merge(M data, AsyncCallback<M> asyncCallback) {
		try {
			M merged = merge(data);
			asyncCallback.onSuccess(merged);
		} catch (GmSessionException e) {
			asyncCallback.onFailure(e);
		}
	}

	protected void initialize(M data) {
		this.data = data;
		this.sourceDrainMap.clear();
		this.mergedData = null;

		visit(data, BaseType.INSTANCE);
	}

	@SuppressWarnings("incomplete-switch")
	private void visit(Object value, GenericModelType type) {
		if (value == null)
			return;

		switch (type.getTypeCode()) {
			case objectType:
				BaseType baseType = (BaseType) type;
				GenericModelType actualType = baseType.getActualType(value);
				visit(value, actualType);
				return;

			case entityType:
				enqueueVisitor(new EntityVisitor((GenericEntity) value));
				return;

			case listType:
			case setType:
				CollectionType collectionType = (CollectionType) type;
				if (!collectionType.hasSimpleOrEnumContent())
					enqueueVisitor(new CollectionVisitor<>((Collection<Object>) value, collectionType));
				return;

			case mapType:
				CollectionType mapType = (CollectionType) type;
				if (!mapType.hasSimpleOrEnumContent())
					enqueueVisitor(new MapVisitor<>((Map<Object, Object>) value, mapType));
				return;
		}
	}

	protected abstract class Step {
		public Step next;

		public abstract void doStep();
	}

	private abstract class Visitor<T, G extends GenericModelType> extends Step {
		public final T payload;
		public final G type;

		public Visitor(T payload, G type) {
			this.payload = payload;
			this.type = type;
		}
	}

	private class CollectionVisitor<T> extends Visitor<Collection<T>, CollectionType> {
		public CollectionVisitor(Collection<T> collection, CollectionType type) {
			super(collection, type);
		}

		@Override
		public void doStep() {
			GenericModelType elementType = type.getCollectionElementType();
			for (T value : payload)
				visit(value, elementType);
		}
	}

	private class MapVisitor<K, V> extends Visitor<Map<K, V>, CollectionType> {
		public MapVisitor(Map<K, V> map, CollectionType type) {
			super(map, type);
		}

		@Override
		public void doStep() {
			GenericModelType keyType = type.getParameterization()[0];
			GenericModelType valueType = type.getParameterization()[1];
			for (Map.Entry<K, V> entry : payload.entrySet()) {
				visit(entry.getKey(), keyType);
				visit(entry.getValue(), valueType);
			}
		}
	}

	private static class SourceDrain {
		public GenericEntity source; // entity from the outside (to be merged)
		public GenericEntity drain; // this might already be in the session
		public EntityReference reference;
	}

	private class Manifestation extends Step {
		public GenericEntity entity;

		@Override
		public void doStep() {
			identityCompetence.bindInstance(entity);
		}
	}

	private final EntityProperty currentWiringProperty = EntityProperty.T.create();

	/** returns null iff source is null or source is a preliminarily deleted entity */
	@SuppressWarnings("incomplete-switch")
	private Object createAdaptedValue(Object source, GenericModelType type) {
		if (source == null)
			return null;

		switch (type.getTypeCode()) {
			case objectType:
				return createAdaptedValue(source, ((BaseType) type).getActualType(source));
			case entityType:
				return sourceDrainMap.get(source);
			case listType:
				return createAdaptedList((List<Object>) source, (CollectionType) type);
			case setType:
				return createAdaptedSet((Set<Object>) source, (CollectionType) type);
			case mapType:
				return createAdaptedMap((Map<Object, Object>) source, (CollectionType) type);
		}

		return source;
	}

	private Map<Object, Object> createAdaptedMap(Map<Object, Object> source, CollectionType collectionType) {
		Map<Object, Object> clone = newMap(source.size());
		GenericModelType keyType = collectionType.getParameterization()[0];
		GenericModelType valueType = collectionType.getParameterization()[1];

		for (Map.Entry<Object, Object> entry : source.entrySet()) {
			Object key = entry.getKey();
			Object value = entry.getValue();

			if (key != null && (key = createAdaptedValue(key, keyType)) == null)
				continue; // this only happens iff "key" was deleted

			if (value != null && (value = createAdaptedValue(value, valueType)) == null)
				continue; // this only happens iff "value" was deleted

			clone.put(key, value);
		}

		return clone;
	}

	private Set<Object> createAdaptedSet(Set<Object> source, CollectionType collectionType) {
		Set<Object> clone = newSet(source.size());
		fillWithAdapted(source, clone, collectionType);
		return clone;
	}

	private List<Object> createAdaptedList(List<Object> source, CollectionType collectionType) {
		List<Object> clone = newList(source.size());
		fillWithAdapted(source, clone, collectionType);
		return clone;
	}

	private void fillWithAdapted(Collection<Object> source, Collection<Object> clone, CollectionType collectionType) {
		switch (collectionType.getCollectionElementType().getTypeCode()) {
			case objectType:
				fillWithAdaptedObjects(source, clone);
				return;
			case entityType:
				fillWithAdaptedEntities(source, clone);
				return;
			default:
				clone.addAll(source);
				return;
		}
	}

	private void fillWithAdaptedObjects(Collection<Object> source, Collection<Object> clone) {
		for (Object element : source)
			if (element == null || (element = createAdaptedValue(element, BaseType.INSTANCE)) != null)
				clone.add(element);
	}

	private void fillWithAdaptedEntities(Collection<Object> source, Collection<Object> clone) {
		for (Object entity : source)
			if (entity == null || (entity = sourceDrainMap.get(entity)) != null)
				clone.add(entity);
	}

	protected abstract class Wiring<T, G extends GenericModelType> extends Step {
		public SourceDrain sourceDrain;
		public Property property;
		public G actualType;
		public T value;
		public Adapter<T, G> adapter;
	}

	@FunctionalInterface
	private interface Adapter<T, G extends GenericModelType> {
		T getAdaptedValue(T value, G type);
	}

	//Using lambda instead of method reference due to a problem with GWT 2.8.0 super dev mode
	private final Adapter<Map<Object, Object>, CollectionType> mapAdapter = (value, type) -> createAdaptedMap(value, type);
	private final Adapter<Set<Object>, CollectionType> setAdapter = (value, type) -> createAdaptedSet(value, type);
	private final Adapter<List<Object>, CollectionType> listAdapter = (value, type) -> createAdaptedList(value, type);
	private final Adapter<GenericEntity, EntityType<?>> entityAdapter = (value, type) -> sourceDrainMap.get(value);
	private final Adapter<Object, GenericModelType> identityAdapter = (value, type) -> value;

	/**
	 * This kind of wiring transfers properties from to an existing entity (identity management) it has to adapt entity
	 * reference properties and keep track of manipulated properties which should not be overwritten. it also avoids
	 * touching properties that would not be changed after all to avoid senseless manipulation notification
	 * 
	 * @author dirk.scheffler
	 *
	 */
	protected class MergeWiring<T, G extends GenericModelType> extends Wiring<T, G> {
		@Override
		public void doStep() {
			/* in case of existing entities we potentially need to transfer and adapt all properties because of
			 * relevance for identity management or manipulation management */
			EntityProperty entityProperty = currentWiringProperty;
			entityProperty.setReference(sourceDrain.reference);
			entityProperty.setPropertyName(property.getName());

			if (!identityCompetence.wasPropertyManipulated(entityProperty)) {
				GenericEntity drain = sourceDrain.drain;
				GenericEntity source = sourceDrain.source;
				T newValue = value;
				Object oldValue = null;

				AbsenceInformation newAi = newValue == null ? property.getAbsenceInformation(source) : null;
				AbsenceInformation oldAi = property.getAbsenceInformation(drain);

				/* If oldAi is null and entity is shallow -> our property is primitive. If we tried to access the
				 * property, we would end in an infinite loop; we do not need to, we always want to set newValue here */
				if (oldAi == null && newAi == null && !EntityFlags.isShallow(drain))
					oldValue = property.get(drain);

				if (newValue != null)
					newValue = adapter.getAdaptedValue(newValue, actualType);

				if (oldAi == null && newAi != null) {
					// do nothing : most common case, so we define it.

				} else if (oldAi == null && newAi == null) {
					/* we have to make sure we do not trigger loading of a value in case it is a not-yet-loaded
					 * collection */
					if (!nullAndNotLoadedCollectionSafeEquals(oldValue, newValue))
						property.set(drain, newValue);

				} else if (oldAi != null && newAi != null) {
					property.setAbsenceInformation(drain, newAi);

				} else if (oldAi != null && newAi == null) {
					property.set(drain, newValue);

					if (EntityFlags.isShallow(drain))
						EntityFlags.setShallow(drain, false);
				}
			}
		}

		private boolean nullAndNotLoadedCollectionSafeEquals(Object o1, Object o2) {
			if (o1 == null)
				return o2 == null;

			if (o2 == null)
				return false;

			if (isNotYetLoadedCollection(o1))
				return isNotYetLoadedCollection(o2) && getCollectionType(o1) == getCollectionType(o2);

			if (isNotYetLoadedCollection(o2))
				return false;

			return o1.equals(o2);
		}

		private CollectionType getCollectionType(Object o) {
			return ((EnhancedCollection) o).type();
		}

		private boolean isNotYetLoadedCollection(Object o) {
			return o instanceof EnhancedCollection && !((EnhancedCollection) o).isLoaded();
		}
	}

	/**
	 * this wiring just needs to adapt entity reference properties, all other stuff can stay as the source and drain
	 * will be identically
	 */
	protected class AdoptionWiring<T, G extends GenericModelType> extends Wiring<T, G> {
		@Override
		public void doStep() {
			T newValue = adapter.getAdaptedValue(value, actualType);
			property.set(sourceDrain.drain, newValue);
		}
	}

	protected class CloneWiring<T, G extends GenericModelType> extends Wiring<T, G> {
		@Override
		public void doStep() {
			GenericEntity drain = sourceDrain.drain;
			GenericEntity source = sourceDrain.source;
			T newValue = value;

			if (newValue != null) {
				newValue = adapter.getAdaptedValue(newValue, actualType);
				property.set(drain, newValue);

			} else {
				property.set(drain, null);
				AbsenceInformation ai = property.getAbsenceInformation(source);
				if (ai != null)
					property.setAbsenceInformation(drain, ai);
			}
		}
	}

	protected static boolean nullSafeEquals(Object o1, Object o2) {
		return o1 == o2 || //
				(o1 != null && o1.equals(o2));
	}

	private static interface WiringPreparation {
		void enqueueIfNeccessary(SourceDrain sourceDrain, Property property, GenericModelType propertyType, Object value);
	}

	private final WiringPreparation adoptionWiringPreparation = new WiringPreparation() {
		@SuppressWarnings("incomplete-switch")
		@Override
		public void enqueueIfNeccessary(SourceDrain sourceDrain, Property property, GenericModelType propertyType, Object value) {
			if (value == null)
				return;

			Adapter<?, ?> adapter = null;

			switch (propertyType.getTypeCode()) {
				case objectType:
					GenericModelType actualType = propertyType.getActualType(value);
					enqueueIfNeccessary(sourceDrain, property, actualType, value);
					return;

				case entityType:
					adapter = entityAdapter;
					break;

				case mapType:
					if (!((CollectionType) propertyType).hasSimpleOrEnumContent())
						adapter = mapAdapter;
					break;

				case setType:
					if (!((CollectionType) propertyType).hasSimpleOrEnumContent())
						adapter = setAdapter;
					break;

				case listType:
					CollectionType collectionType = (CollectionType) propertyType;
					if (!collectionType.hasSimpleOrEnumContent())
						adapter = listAdapter;
					break;
			}

			if (adapter != null) {
				AdoptionWiring<Object, GenericModelType> wiring = new AdoptionWiring<>();
				wiring.actualType = propertyType;
				wiring.adapter = (Adapter<Object, GenericModelType>) adapter;
				wiring.sourceDrain = sourceDrain;
				wiring.property = property;
				wiring.value = value;
				enqueueWiring(wiring);
			}
		}

	};

	private class MergeCloneWiringPreparation implements WiringPreparation {

		private final Supplier<Wiring<?, ?>> wiringFactory;

		public MergeCloneWiringPreparation(Supplier<ContinuableMerger<M>.Wiring<?, ?>> wiringFactory) {
			this.wiringFactory = wiringFactory;
		}

		@Override
		public void enqueueIfNeccessary(SourceDrain sourceDrain, Property property, GenericModelType propertyType, Object value) {
			if (value != null && propertyType == BaseType.INSTANCE)
				propertyType = propertyType.getActualType(value);

			Wiring<Object, GenericModelType> wiring = (Wiring<Object, GenericModelType>) wiringFactory.get();
			wiring.actualType = propertyType;
			wiring.adapter = (Adapter<Object, GenericModelType>) resolveAdapter(propertyType);
			wiring.sourceDrain = sourceDrain;
			wiring.property = property;
			wiring.value = value;
			enqueueWiring(wiring);
		}

		private Adapter<?, ?> resolveAdapter(GenericModelType propertyType) {
			switch (propertyType.getTypeCode()) {
				case entityType:
					return entityAdapter;
				case mapType:
					return mapAdapter;
				case setType:
					return setAdapter;
				case listType:
					return listAdapter;
				default:
					return identityAdapter;
			}
		}
	}

	//Using lambda instead of method reference due to a problem with GWT 2.8.0 super dev mode
	private final WiringPreparation mergeWiringPreparation = new MergeCloneWiringPreparation(() -> new MergeWiring<>());
	private final WiringPreparation cloneWiringPreparation = new MergeCloneWiringPreparation(() -> new CloneWiring<>());

	protected class EntityVisitor extends Visitor<GenericEntity, EntityType<GenericEntity>> {
		public EntityVisitor(GenericEntity entity) {
			super(entity, entity.entityType());
		}

		@Override
		public void doStep() {
			if (sourceDrainMap.containsKey(payload))
				return;

			// visit the entity as it is the first time it occurs during traversing
			PersistentEntityReference reference = getPersistentReference(payload);

			SourceDrain sourceDrain = new SourceDrain();
			sourceDrain.reference = reference;
			sourceDrain.source = payload;

			/* reference is null iff "payload" has no id set -> it doesn't come from persistence layer, but is a
			 * "data container" (e.g. QueryResult as a container for the actual data) */
			if (reference == null)
				doStepForDataContainer(sourceDrain);
			else
				doStepForPersistentData(sourceDrain);
		}

		private void doStepForDataContainer(SourceDrain sourceDrain) {
			sourceDrain.drain = envelopeFactory != null ? envelopeFactory.apply(payload) : copy(payload);
			sourceDrainMap.put(payload, sourceDrain.drain);

			/* We set this value to the original value, which is correct, iff the value only contains simple/enum types.
			 * If however the value contains entities, the next step would enqueue wiring for that property, which would
			 * adapt the value to only use the right entities. */
			for (Property p : type.getProperties()) {
				Object value = p.get(payload);
				p.set(sourceDrain.drain, value);
			}

			handleAttributes(sourceDrain, adoptionWiringPreparation);
		}

		private void doStepForPersistentData(SourceDrain sourceDrain) {
			sourceDrain.drain = identityCompetence.findExistingEntity(sourceDrain.reference);

			WiringPreparation wiringPreparation = null;

			if (sourceDrain.drain != null) {
				wiringPreparation = mergeWiringPreparation;

			} else {
				if (identityCompetence.isPreliminarilyDeleted(sourceDrain.reference)) {
					sourceDrainMap.put(payload, null);
					return;
				}

				Manifestation manifestation = new Manifestation();

				if (adopt) {
					sourceDrain.drain = manifestation.entity = payload;
					wiringPreparation = adoptionWiringPreparation;
				} else {
					sourceDrain.drain = manifestation.entity = identityCompetence.createUnboundInstance(type);
					wiringPreparation = cloneWiringPreparation;
				}

				enqueueManifestation(manifestation);
			}

			sourceDrainMap.put(payload, sourceDrain.drain);

			handleAttributes(sourceDrain, wiringPreparation);
		}

		private void handleAttributes(SourceDrain sourceDrain, WiringPreparation wiringPreparation) {
			handleProperties(sourceDrain, wiringPreparation);
			handleTransientProperties(sourceDrain);
		}

		private void handleProperties(SourceDrain sourceDrain, WiringPreparation wiringPreparation) {
			for (Property property : type.getProperties()) {
				GenericModelType propertyType = property.getType();
				Object value = property.get(payload);

				wiringPreparation.enqueueIfNeccessary(sourceDrain, property, propertyType, value);

				visit(value, propertyType);
			}
		}

		private void handleTransientProperties(SourceDrain sd) {
			if (!transferTransientProperties || sd.source == sd.drain)
				return;

			for (TransientProperty tp : sd.source.entityType().getTransientProperties())
				tp.set(sd.drain, tp.get(sd.source));
		}

		private GenericEntity copy(GenericEntity entity) {
			return entity.entityType().create();
		}

		private PersistentEntityReference getPersistentReference(GenericEntity payload) {
			return payload.getId() == null ? null : (PersistentEntityReference) payload.reference();
		}
	}

	private class SeparatorStep extends Step {
		public SeparatorStep(Step next) {
			this.next = next;
		}

		@Override
		public void doStep() {
			// do nothing here because it only separates
		}
	}

	private class ResultPreparation extends Step {
		@Override
		public void doStep() {
			mergedData = (M) createAdaptedValue(data, BaseType.INSTANCE);
		}
	}

	private Step manifestationSection = new ResultPreparation();
	private Step wiringSection = new SeparatorStep(manifestationSection);
	private Step visitSection = new SeparatorStep(wiringSection);

	protected Step currentStep = visitSection;

	private void enqueueVisitor(Visitor<?, ?> step) {
		visitSection = enqueueStep(step, visitSection);
	}

	private void enqueueWiring(Wiring<?, ?> step) {
		wiringSection = enqueueStep(step, wiringSection);
	}

	private void enqueueManifestation(Manifestation step) {
		manifestationSection = enqueueStep(step, manifestationSection);
	}

	private Step enqueueStep(Step step, Step section) {
		step.next = section.next;
		section.next = step;
		return step;
	}

}
