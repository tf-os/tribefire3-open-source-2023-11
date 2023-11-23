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
package com.braintribe.model.processing.session.impl.session.collection;

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.addManipulation;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.clearManipulation;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.compound;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.removeManipulation;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.newLinkedMap;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.collection.MapBase;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.CollectionManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.manipulation.util.ManipulationBuilder;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.tracking.ManipulationListener;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;
import jsinterop.context.JsInteropNamespaces;

@JsType(namespace = JsInteropNamespaces.gm)
public class EnhancedMap<K, V> extends AbstractMap<K, V> implements MapBase<K, V>, EnhancedCollection {
	private Map<K, V> delegate;
	private MapType mapType;
	private List<ManipulationListener> listeners;
	private LocalEntityProperty owner;
	private GenericEntity entity;
	private boolean incomplete;
	private boolean loaded;

	@JsIgnore
	public EnhancedMap(MapType mapType) {
		this(mapType, newLinkedMap(), false);
	}

	@JsIgnore
	public EnhancedMap(MapType mapType, Map<K, V> delegate) {
		this(mapType, delegate, false);
	}

	public EnhancedMap(MapType mapType, Map<K, V> delegate, boolean absent) {
		this.mapType = mapType;
		this.delegate = delegate;
		this.incomplete = absent;
		this.loaded = !absent;
	}

	@Override
	public void addManipulationListener(ManipulationListener listener) {
		if (listeners == null)
			listeners = new ArrayList<>(2);
		listeners.add(listener);
	}

	@Override
	public void removeManipulationListener(ManipulationListener listener) {
		if (listeners != null) {
			listeners.remove(listener);
			if (listeners.isEmpty())
				listeners = null;
		}
	}

	@Override
	public void setCollectionOwner(LocalEntityProperty owner) {
		this.owner = owner;
		this.entity = owner.getEntity();
	}

	@Override
	public LocalEntityProperty getCollectionOwner() {
		return owner;
	}

	@Override
	public MapType type() {
		return mapType;
	}

	@Override
	public void setIncomplete(boolean incomplete) {
		this.incomplete = incomplete;
	}

	@Override
	public boolean isIncomplete() {
		return incomplete;
	}

	@Override
	public boolean isLoaded() {
		return loaded;
	}

	@Override
	public Map<K, V> getDelegate() {
		return delegate;
	}

	protected GenericModelType getKeyType() {
		return mapType.getKeyType();
	}

	protected GenericModelType getValueType() {
		return mapType.getValueType();
	}

	@Override
	public boolean containsKey(Object key) {
		ensureComplete();
		return delegate.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		ensureComplete();
		return delegate.containsValue(value);
	}

	@Override
	public V get(Object key) {
		ensureComplete();
		return delegate.get(key);
	}

	@Override
	public V getOrDefault(Object key, V defaultValue) {
		ensureComplete();
		return delegate.getOrDefault(key, defaultValue);
	}

	@Override
	public boolean isEmpty() {
		ensureComplete();
		return delegate.isEmpty();
	}

	@Override
	public int size() {
		ensureComplete();
		return delegate.size();
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		ensureComplete();
		return new EntrySet();
	}

	@Override
	public Set<K> keySet() {
		ensureComplete();
		return new KeySet();
	}

	@Override
	public Collection<V> values() {
		ensureComplete();
		return new Values();
	}

	/* methods that will raise manipulation events (non-Javadoc) */

	@Override
	public void clear() {
		if (!isNoticing()) {
			delegate.clear();
			incomplete = false;
			loaded = true;
			return;
		}

		ensureComplete();
		Map<Object, Object> items = newMap();

		for (Map.Entry<K, V> entry : delegate.entrySet()) {
			Object keyDescriptor = entry.getKey();
			Object valueDescriptor = entry.getValue();

			items.put(keyDescriptor, valueDescriptor);
		}

		if (!items.isEmpty()) {
			delegate.clear();

			ClearCollectionManipulation manipulation = clearManipulation(owner);

			AddManipulation inverseManipulation = ManipulationBuilder.addManipulation(items, owner);
			inverseManipulation.setItemsToAdd(items);

			manipulation.linkInverse(inverseManipulation);

			noticeManipulation(manipulation);
		}

	}

	@Override
	public V put(K key, V value) {
		ensureComplete();
		boolean containedBefore = delegate.containsKey(key);

		V oldValue = delegate.put(key, value);

		if (!isNoticing())
			return oldValue;

		Object keyDescriptor = key;
		Object valueDescriptor = value;
		Object oldValueDescriptor = oldValue;

		AddManipulation manipulation = addManipulation(keyDescriptor, valueDescriptor, owner);

		if (containedBefore) {
			AddManipulation inverseManipulation = addManipulation(keyDescriptor, oldValueDescriptor, owner);

			manipulation.linkInverse(inverseManipulation);
		} else {
			RemoveManipulation inverseManipulation = removeManipulation(keyDescriptor, valueDescriptor, owner);
			inverseManipulation.setItemsToRemove(asMap(keyDescriptor, valueDescriptor));

			manipulation.linkInverse(inverseManipulation);
		}

		noticeManipulation(manipulation);

		return oldValue;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		ensureComplete();
		if (m.isEmpty() || !isNoticing()) {
			delegate.putAll(m);
			return;
		}

		Map<Object, Object> itemsToAdd = newMap();
		Map<Object, Object> itemsToReAdd = newMap();
		Map<Object, Object> itemsToRemove = newMap();

		for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
			K key = entry.getKey();
			V value = entry.getValue();
			Object keyDescriptor = key;
			Object valueDescriptor = value;
			boolean alreadyContained = delegate.containsKey(key);

			V oldValue = delegate.put(key, value);

			itemsToAdd.put(keyDescriptor, valueDescriptor);

			if (alreadyContained)
				itemsToReAdd.put(keyDescriptor, oldValue);
			else
				itemsToRemove.put(keyDescriptor, valueDescriptor);
		}

		AddManipulation manipulation = addManipulation(itemsToAdd, owner);

		List<Manipulation> inverseManipulations = new ArrayList<>(2);

		if (!itemsToReAdd.isEmpty()) {
			AddManipulation inverseManipulation = addManipulation(itemsToReAdd, owner);
			inverseManipulations.add(inverseManipulation);
		}

		if (!itemsToRemove.isEmpty()) {
			RemoveManipulation inverseManipulation = removeManipulation(itemsToRemove, owner);
			inverseManipulations.add(inverseManipulation);
		}

		if (inverseManipulations.size() == 1) {
			Manipulation inverseManipulation = inverseManipulations.get(0);
			manipulation.linkInverse(inverseManipulation);
		} else {
			CompoundManipulation inverseManipulation = compound(inverseManipulations);
			manipulation.linkInverse(inverseManipulation);
		}

		noticeManipulation(manipulation);
	}

	@Override
	public V remove(Object key) {
		ensureComplete();
		if (!isNoticing())
			return delegate.remove(key);

		boolean contained = delegate.containsKey(key);

		if (contained) {
			V value = delegate.remove(key);

			Object keyDescriptor = key;
			Object valueDescriptor = value;

			RemoveManipulation manipulation = removeManipulation(keyDescriptor, valueDescriptor, owner);
			AddManipulation inverseManipulation = addManipulation(keyDescriptor, valueDescriptor, owner);

			manipulation.linkInverse(inverseManipulation);

			noticeManipulation(manipulation);

			return value;
		} else
			return null;
	}

	@Override
	@JsIgnore
	public void forEach(BiConsumer<? super K, ? super V> action) {
		ensureComplete();
		delegate.forEach(action);
	}

	// view collection support
	@JsType(namespace = JsInteropNamespaces.gm)
	private class EntrySet extends AbstractSet<Map.Entry<K, V>> {
		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new EntryIterator();
		}

		@Override
		public boolean contains(Object o) {
			return delegate.entrySet().contains(o);
		}

		@Override
		public int size() {
			return delegate.size();
		}

		@Override
		public void clear() {
			EnhancedMap.this.clear();
		}

		@Override
		public boolean remove(Object o) {
			boolean removed = delegate.entrySet().remove(o);

			if (removed && isNoticing()) {
				Map.Entry<K, V> entry = (Map.Entry<K, V>) o;

				Object keyDescriptor = entry.getKey();
				Object valueDescriptor = entry.getValue();

				RemoveManipulation manipulation = removeManipulation(keyDescriptor, valueDescriptor, owner);
				AddManipulation inverseManipulation = addManipulation(keyDescriptor, valueDescriptor, owner);

				manipulation.linkInverse(inverseManipulation);

				noticeManipulation(manipulation);
			}

			return removed;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			if (!isNoticing())
				return delegate.entrySet().removeAll(c);

			Collection<Map.Entry<K, V>> entryCollection = (Collection<Map.Entry<K, V>>) c;

			Map<Object, Object> items = newMap();

			Set<Map.Entry<K, V>> delegateEntrySet = delegate.entrySet();

			for (Map.Entry<K, V> entry : entryCollection) {
				if (delegateEntrySet.remove(entry)) {
					K key = entry.getKey();
					V value = entry.getValue();

					Object keyDescriptor = key;
					Object valueDescriptor = value;
					items.put(keyDescriptor, valueDescriptor);
				}
			}

			if (!items.isEmpty()) {
				RemoveManipulation manipulation = removeManipulation(items, owner);
				AddManipulation inverseManipulation = addManipulation(items, owner);

				manipulation.linkInverse(inverseManipulation);

				noticeManipulation(manipulation);

				return true;
			} else
				return false;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			if (!isNoticing())
				return delegate.entrySet().retainAll(c);

			Map<Object, Object> items = newMap();

			Iterator<Map.Entry<K, V>> delegateIt = delegate.entrySet().iterator();
			while (delegateIt.hasNext()) {
				Map.Entry<K, V> entry = delegateIt.next();

				boolean remove = !c.contains(entry);

				if (remove) {
					delegateIt.remove();
					Object keyDescriptor = entry.getKey();
					Object valueDescriptor = entry.getValue();
					items.put(keyDescriptor, valueDescriptor);
				}
			}

			if (items.isEmpty()) {
				return false;
			}

			if (isNoticing()) {
				RemoveManipulation manipulation = removeManipulation(items, owner);
				AddManipulation inverseManipulation = addManipulation(items, owner);

				manipulation.linkInverse(inverseManipulation);

				noticeManipulation(manipulation);
			}

			return true;
		}
		
		@Override
		public Object[] toArray() {
			return delegate.entrySet().toArray();
		}

	}

	@JsType(namespace = JsInteropNamespaces.gm)
	private class KeySet extends AbstractSet<K> {

		@Override
		public boolean contains(Object o) {
			return EnhancedMap.this.containsKey(o);
		}

		@Override
		public Iterator<K> iterator() {
			return new KeyIterator();
		}

		@Override
		public int size() {
			return delegate.size();
		}

		@Override
		public void clear() {
			EnhancedMap.this.clear();
		}

		@Override
		public boolean remove(Object o) {
			return EnhancedMap.this.remove(o) != null;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			if (!isNoticing())
				return delegate.keySet().removeAll(c);

			Collection<K> keyCollection = (Collection<K>) c;
			Map<Object, Object> items = newMap();

			for (K key : keyCollection) {
				if (EnhancedMap.this.containsKey(key)) {
					V value = delegate.remove(key);

					Object keyDescriptor = key;
					Object valueDescriptor = value;
					items.put(keyDescriptor, valueDescriptor);
				}
			}

			if (!items.isEmpty()) {
				RemoveManipulation manipulation = removeManipulation(items, owner);
				AddManipulation inverseManipulation = addManipulation(items, owner);

				manipulation.linkInverse(inverseManipulation);

				noticeManipulation(manipulation);

				return true;
			} else
				return false;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			if (!isNoticing())
				return delegate.keySet().retainAll(c);

			Map<Object, Object> items = newMap();

			Iterator<Map.Entry<K, V>> delegateIt = delegate.entrySet().iterator();

			while (delegateIt.hasNext()) {
				Map.Entry<K, V> entry = delegateIt.next();

				K key = entry.getKey();
				V value = entry.getValue();

				boolean remove = !c.contains(key);

				if (remove) {
					delegateIt.remove();
					Object keyDescriptor = key;
					Object valueDescriptor = value;
					items.put(keyDescriptor, valueDescriptor);
				}
			}

			if (items.isEmpty()) {
				return false;
			}

			if (isNoticing()) {
				RemoveManipulation manipulation = removeManipulation(items, owner);
				AddManipulation inverseManipulation = addManipulation(items, owner);

				manipulation.linkInverse(inverseManipulation);

				noticeManipulation(manipulation);
			}

			return true;
		}

	}

	@JsType(namespace = JsInteropNamespaces.gm)
	private class Values extends AbstractCollection<V> {

		@Override
		public Iterator<V> iterator() {
			return new ValueIterator();
		}

		@Override
		public boolean contains(Object o) {
			return delegate.values().contains(o);
		}

		@Override
		public int size() {
			return EnhancedMap.this.size();
		}

		@Override
		public void clear() {
			EnhancedMap.this.clear();
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return removeAll(c, false);
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return removeAll(c, true);
		}

		@JsIgnore
		protected boolean removeAll(Collection<?> c, boolean notContained) {
			Map<Object, Object> items = newMap();
			Iterator<Map.Entry<K, V>> delegateIt = delegate.entrySet().iterator();

			while (delegateIt.hasNext()) {
				Map.Entry<K, V> entry = delegateIt.next();

				K key = entry.getKey();
				V value = entry.getValue();

				boolean remove = c.contains(value) ^ notContained;

				if (remove) {
					delegateIt.remove();
					Object keyDescriptor = key;
					Object valueDescriptor = value;
					items.put(keyDescriptor, valueDescriptor);
				}
			}

			if (items.isEmpty()) {
				return false;
			}

			if (isNoticing()) {
				RemoveManipulation manipulation = removeManipulation(items, owner);
				AddManipulation inverseManipulation = addManipulation(items, owner);

				manipulation.linkInverse(inverseManipulation);

				noticeManipulation(manipulation);
			}

			return true;
		}

	}

	@JsType(namespace = JsInteropNamespaces.gm)
	private class EntryIterator implements Iterator<Map.Entry<K, V>> {
		private final Iterator<Map.Entry<K, V>> delegateIt = delegate.entrySet().iterator();
		private Map.Entry<K, V> lastEntry = null;

		@Override
		public boolean hasNext() {
			return delegateIt.hasNext();
		}

		@Override
		public Map.Entry<K, V> next() {
			lastEntry = delegateIt.next();
			return lastEntry;
		}

		@Override
		public void remove() {
			delegateIt.remove();

			if (!isNoticing())
				return;

			V value = lastEntry.getValue();

			Object valueDescriptor = value;
			Object keyDescriptor = lastEntry.getKey();

			RemoveManipulation manipulation = removeManipulation(keyDescriptor, valueDescriptor, owner);
			AddManipulation inverseManipulation = addManipulation(keyDescriptor, valueDescriptor, owner);

			manipulation.linkInverse(inverseManipulation);

			noticeManipulation(manipulation);
		}
	}

	private class KeyIterator implements Iterator<K> {
		private final EntryIterator delegateIt = new EntryIterator();

		@Override
		public boolean hasNext() {
			return delegateIt.hasNext();
		}

		@Override
		public K next() {
			return delegateIt.next().getKey();
		}

		@Override
		public void remove() {
			delegateIt.remove();
		}
	}

	private class ValueIterator implements Iterator<V> {
		private final EntryIterator delegateIt = new EntryIterator();

		@Override
		public boolean hasNext() {
			return delegateIt.hasNext();
		}

		@Override
		public V next() {
			return delegateIt.next().getValue();
		}

		@Override
		public void remove() {
			delegateIt.remove();
		}
	}

	public GenericEntity getEntity() {
		return entity;
	}

	protected boolean isNoticing() {
		if (entity != null && entity.session() != null)
			return true;

		return listeners != null;
	}

	private void noticeManipulation(Manipulation manipulation) {
		if (entity != null) {
			GmSession gmSession = entity.session();
			if (gmSession != null) {
				gmSession.noticeManipulation(manipulation);
			}
		}

		if (listeners != null) {
			ManipulationListener[] la = listeners.toArray(new ManipulationListener[listeners.size()]);
			for (ManipulationListener listener : la) {
				listener.noticeManipulation(manipulation);
			}
		}
	}

	private void ensureComplete() {
		if (loaded || GMF.platform().isSingleThreaded())
			return;

		ensureCompleteSync();
	}

	private synchronized void ensureCompleteSync() {
		if (loaded)
			return;

		EnhanceUtil.<Map<K, V>> loadCollectionLazily(entity, owner, value -> delegate.putAll(value));

		loaded = true;
		incomplete = false;
	}

	@Override
	public int hashCode() {
		ensureComplete();
		return delegate.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		ensureComplete();
		return delegate.equals(obj);
	}

	@Override
	public String toString() {
		ensureComplete();
		return delegate.toString();
	}

	protected <T extends CollectionManipulation> T prepareManipulation(T collectionManipulation) {
		collectionManipulation.setOwner(owner);
		return collectionManipulation;
	}
}
