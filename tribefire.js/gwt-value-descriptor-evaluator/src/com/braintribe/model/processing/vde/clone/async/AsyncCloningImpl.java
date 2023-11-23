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
package com.braintribe.model.processing.vde.clone.async;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import com.braintribe.gwt.async.client.DeferredExecutor;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.collection.LinearCollectionBase;
import com.braintribe.model.generic.collection.ListBase;
import com.braintribe.model.generic.collection.MapBase;
import com.braintribe.model.generic.collection.SetBase;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EssentialTypes;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.ListType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SetType;
import com.braintribe.model.generic.reflection.VdHolder;
import com.braintribe.model.generic.value.Escape;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.processing.async.api.AsyncCallback;

public class AsyncCloningImpl implements AsyncCloning {

	private final BiConsumer<ValueDescriptor, AsyncCallback<Object>> vdeEvaluator;
	private final DeferredExecutor executor;

	private final Map<GenericEntity, EntityCollector> entities = new IdentityHashMap<>();
	private final Predicate<GenericEntity> skipCloningPredicate;

	public AsyncCloningImpl(BiConsumer<ValueDescriptor, AsyncCallback<Object>> vdeEvaluator, DeferredExecutor executor,
			Predicate<GenericEntity> skipCloningPredicate) {
		this.vdeEvaluator = vdeEvaluator;
		this.executor = executor;
		this.skipCloningPredicate = skipCloningPredicate;
	}

	@Override
	public <T> void cloneList(List<T> list, AsyncCallback<? super ListBase<T>> callback) {
		cloneList(list, EssentialTypes.TYPE_LIST, callback);
	}

	@Override
	public <T> void cloneList(ListBase<T> list, AsyncCallback<? super ListBase<T>> callback) {
		cloneList(list, list.type(), callback);
	}

	@Override
	public <T> void cloneList(List<T> list, ListType listType, AsyncCallback<? super ListBase<T>> callback) {
		cloneCollection(list, listType, (AsyncCallback<Object>) callback);
	}

	@Override
	public <T> void cloneSet(Set<T> set, AsyncCallback<? super SetBase<T>> callback) {
		cloneSet(set, EssentialTypes.TYPE_SET, callback);
	}

	@Override
	public <T> void cloneSet(SetBase<T> set, AsyncCallback<? super SetBase<T>> callback) {
		cloneSet(set, set.type(), callback);
	}

	@Override
	public <T> void cloneSet(Set<T> set, SetType setType, AsyncCallback<? super SetBase<T>> callback) {
		cloneCollection(set, setType, (AsyncCallback<Object>) callback);
	}

	@Override
	public <T> void cloneCollection(Collection<T> collection, AsyncCallback<? super LinearCollectionBase<T>> callback) {
		cloneCollection(collection, GMF.getTypeReflection().getType(collection), callback);
	}

	@Override
	public <T> void cloneCollection(LinearCollectionBase<T> collection, AsyncCallback<? super LinearCollectionBase<T>> callback) {
		cloneCollection(collection, collection.type(), callback);
	}

	@Override
	public <T> void cloneCollection(Collection<T> collection, LinearCollectionType collectionType,
			AsyncCallback<? super LinearCollectionBase<T>> callback) {

		VdYesWorkerContext<LinearCollectionBase<T>> wc = new VdYesWorkerContext<>(callback, executor);
		wc.submit(() -> _cloneCollection(collection, collectionType, wc, wc));
	}

	@Override
	public <K, V> void cloneMap(Map<K, V> map, AsyncCallback<? super MapBase<K, V>> callback) {
		cloneMap(map, EssentialTypes.TYPE_MAP, callback);
	}

	@Override
	public <K, V> void cloneMap(MapBase<K, V> map, AsyncCallback<? super MapBase<K, V>> callback) {
		cloneMap(map, map.type(), callback);
	}

	@Override
	public <K, V> void cloneMap(Map<K, V> map, MapType mapType, AsyncCallback<? super MapBase<K, V>> callback) {
		VdYesWorkerContext<MapBase<K, V>> wc = new VdYesWorkerContext<>(callback, executor);
		wc.submit(() -> _cloneMap(map, mapType, wc, wc));
	}

	@Override
	public <T extends GenericEntity> void cloneEntity(T entity, AsyncCallback<? super T> callback) {
		VdYesWorkerContext<GenericEntity> wc = new VdYesWorkerContext<>((AsyncCallback<GenericEntity>) callback, executor);
		wc.submit(() -> _cloneEntity(entity, wc, wc));
	}

	@Override
	public <T> void cloneValue(Object value, AsyncCallback<? super T> callback) {
		VdYesWorkerContext<T> wc = new VdYesWorkerContext<>(callback, executor);
		wc.submit(() -> _cloneValue(value, wc, wc));
	}

	@Override
	public <T> void cloneValue(Object value, GenericModelType type, AsyncCallback<? super T> callback) {
		VdYesWorkerContext<T> wc = new VdYesWorkerContext<T>(callback, executor);
		wc.submit(() -> _cloneValue(value, type, wc, wc));
	}

	private <T> void _cloneValue(Object value, AsyncCallback<? super T> consumer, VdeWorkerContext wc) {
		GenericModelType type = VdHolder.isVdHolder(value) ? BaseType.INSTANCE : BaseType.INSTANCE.getActualType(value);
		_cloneValue(value, type, consumer, wc);
	}

	private <T> void _cloneValue(Object value, GenericModelType type, AsyncCallback<? super T> consumer, VdeWorkerContext wc) {
		AsyncCallback<Object> castedAsyncCallback = (AsyncCallback<Object>) consumer;

		boolean isVdHolder = VdHolder.isVdHolder(value);
		if (isVdHolder) {
			VdHolder vdHolder = (VdHolder) value;
			value = vdHolder.vd;
			type = vdHolder.vd.entityType();

			// we want to clone VDs in VdHolder, but when not evaluating, we wrap them back in a VD holder
			if (!wc.evaluateVds())
				castedAsyncCallback = vdHolderWrappingCallback(castedAsyncCallback);

		} else if (value == null || type.isScalar()) {
			castedAsyncCallback.onSuccess(value);
			return;
		}

		switch (type.getTypeCode()) {
			case objectType:
				_cloneValue(value, type.getActualType(value), castedAsyncCallback, wc);
				break;
			case entityType:
				_cloneEntityHandleVd(value, wc, castedAsyncCallback, isVdHolder);
				break;
			case listType:
			case setType:
				_cloneCollection((Collection<?>) value, (LinearCollectionType) type, castedAsyncCallback, wc);
				break;
			case mapType:
				_cloneMap((Map<?, ?>) value, (MapType) type, castedAsyncCallback, wc);
				break;
			default:
				throw new IllegalStateException("unexpected typecode: " + type.getTypeCode());
		}
	}

	private AsyncCallback<Object> vdHolderWrappingCallback(AsyncCallback<Object> downstream) {
		return AsyncCallback.of( //
				future -> downstream.onSuccess(new VdHolder((ValueDescriptor) future)), //
				downstream::onFailure);
	}

	private void _cloneEntityHandleVd(Object value, VdeWorkerContext wc, AsyncCallback<Object> castedAsyncCallback, boolean wasVdHolder) {
		GenericEntity ge = (GenericEntity) value;
		if (wc.evaluateVds() && ge.isVd()) {
			// TODO check it this is at all needed, VDE should be able to evaluate an Escape, so why the special handling?
			if (ge instanceof Escape) {
				handleEscape((Escape) ge, castedAsyncCallback, wc);
				return;
			}

			castedAsyncCallback = vdEvaluatingCallback(castedAsyncCallback, wasVdHolder);
		}

		_cloneEntity(ge, castedAsyncCallback, wc);
	}

	private void handleEscape(Escape ge, AsyncCallback<Object> castedAsyncCallback, VdeWorkerContext wc) {
		_cloneValue(ge.getValue(), castedAsyncCallback, wc.nonVdEvaluatingContext());
	}

	private AsyncCallback<Object> vdEvaluatingCallback(AsyncCallback<Object> downstream, boolean wasVdHolder) {
		return AsyncCallback.of( //
				clonedValue -> handleClonedVd((ValueDescriptor) clonedValue, downstream, wasVdHolder), //
				downstream::onFailure);
	}

	private void handleClonedVd(ValueDescriptor vd, AsyncCallback<Object> downstream, boolean wasVdHolder) {
		if (wasVdHolder)
			downstream = vdHolderWrappingCallbackIfVdNotEvaluated(downstream, vd);
		vdeEvaluator.accept(vd, downstream);
	}

	// This wrapper ensures that if VD evaluated to itself, we wrap it again in a VdHolder
	private AsyncCallback<Object> vdHolderWrappingCallbackIfVdNotEvaluated(AsyncCallback<Object> downstream, ValueDescriptor vd) {
		return AsyncCallback.of( //
				evaluatedVd -> {
					if (evaluatedVd == vd)
						evaluatedVd = new VdHolder(vd);
					downstream.onSuccess(evaluatedVd);
				}, downstream::onFailure);
	}

	private boolean shouldCloneTransitively;

	private void _cloneEntity(GenericEntity entity, AsyncCallback<? super GenericEntity> callback, VdeWorkerContext wc) {
		EntityCollector collector = getEntityCollector(entity);
		collector.andThenSubmit(callback, wc);

		if (shouldCloneTransitively)
			_cloneProperties(entity.entityType().getProperties().iterator(), entity, collector, wc);
	}

	private EntityCollector getEntityCollector(GenericEntity entity) {
		shouldCloneTransitively = false;
		return entities.computeIfAbsent(entity, e -> {
			shouldCloneTransitively = true;
			return newEntityCollectorFor(entity);
		});
	}

	private EntityCollector newEntityCollectorFor(GenericEntity entity) {
		if (skipCloningPredicate.test(entity))
			return EntityCollector.forNonClonedEntity(entity);
		else
			return new EntityCollector(entity.entityType().createRaw());
	}

	private void _cloneProperties(Iterator<Property> it, GenericEntity entity, EntityCollector collector, VdeWorkerContext wc) {
		int maxProperties = wc.allowedNumberOfOps();
		int counter = 0;
		while (it.hasNext() && counter++ < maxProperties) {
			Property property = it.next();
			GenericModelType propertyType = property.getType();
			Object propertyValue = property.getDirectUnsafe(entity);
			_cloneValue(propertyValue, propertyType, collector.collectProperty(property), wc);
		}

		wc.notifyNumberOfOps(counter);

		if (it.hasNext())
			wc.submit(() -> _cloneProperties(it, entity, collector, wc));
	}

	private <T> void _cloneCollection(Collection<T> collection, LinearCollectionType type, AsyncCallback<? super LinearCollectionBase<T>> consumer,
			VdeWorkerContext wc) {

		LinearCollectionBase<T> cloned = (LinearCollectionBase<T>) type.createPlain();
		_cloneCollection(collection, type, cloned, consumer, wc);
	}

	private <T, C extends LinearCollectionBase<T>> void _cloneCollection(Collection<T> collection, LinearCollectionType collectionType,
			C clonedCollection, AsyncCallback<? super C> callback, VdeWorkerContext wc) {

		GenericModelType elementType = collectionType.getCollectionElementType();

		ArrayCollector arrayCollector = new ArrayCollector(collection.size());
		arrayCollector.andThen( //
				wc.submittingCallbackOf( //
						future -> {
							for (Object element : future)
								clonedCollection.add((T) element);

							callback.onSuccess(clonedCollection);
						}, //
						callback::onFailure));

		_cloneElements(collection.iterator(), 0, elementType, arrayCollector, wc);
	}

	private <E> void _cloneElements(Iterator<E> it, int currentIndex, GenericModelType elementType, ArrayCollector arrayCollector,
			VdeWorkerContext wc) {
		int maxElements = wc.allowedNumberOfOps();
		int counter = 0;
		while (it.hasNext() && counter++ < maxElements) {
			_cloneValue(it.next(), elementType, arrayCollector.collect(currentIndex++), wc);
		}

		wc.notifyNumberOfOps(counter);

		if (it.hasNext()) {
			int i = currentIndex;
			wc.submit(() -> _cloneElements(it, i, elementType, arrayCollector, wc));
		}
	}

	private <K, V> void _cloneMap(Map<K, V> map, MapType mapType, AsyncCallback<? super MapBase<K, V>> callback, VdeWorkerContext wc) {
		MapBase<K, V> clonedMap = (MapBase<K, V>) mapType.createPlain();

		AsyncCollector<MapBase<K, V>> mapCollector = AsyncCollector.into(clonedMap, map.size());
		mapCollector.andThenSubmit(callback, wc);

		_cloneEntries(map.entrySet().iterator(), mapType, mapCollector, wc);
	}

	private <K, V> void _cloneEntries(Iterator<Entry<K, V>> it, MapType mapType, AsyncCollector<MapBase<K, V>> mapCollector, VdeWorkerContext wc) {
		GenericModelType keyType = mapType.getKeyType();
		GenericModelType valueType = mapType.getValueType();

		int maxEntries = wc.allowedNumberOfOps() / 2;
		int counter = 0;
		while (it.hasNext() && counter++ < maxEntries) {
			Entry<K, V> entry = it.next();

			CoupleCollector coupleCollector = new CoupleCollector();

			_cloneValue(entry.getKey(), keyType, coupleCollector.collectFirst(), wc);
			_cloneValue(entry.getValue(), valueType, coupleCollector.collectSecond(), wc);

			coupleCollector.andThen( //
					mapCollector.collect((m, c) -> m.put((K) c.first, (V) c.second)));
		}

		wc.notifyNumberOfOps(2 * counter);

		if (it.hasNext())
			wc.submit(() -> _cloneEntries(it, mapType, mapCollector, wc));
	}

}
