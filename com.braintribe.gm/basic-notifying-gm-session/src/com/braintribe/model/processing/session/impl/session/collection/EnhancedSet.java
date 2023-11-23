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
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.removeManipulation;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.voidManipulation;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.newLinkedSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.SetType;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.tracking.ManipulationListener;

public class EnhancedSet<E> implements Set<E>, EnhancedCollection {
	private Set<E> delegate;
	private SetType setType;
	private List<ManipulationListener> listeners;
	private LocalEntityProperty owner;
	private GenericEntity entity;
	private boolean incomplete;
	private boolean loaded;
	private ReentrantLock loadLock = new ReentrantLock();

	public EnhancedSet(SetType setType) {
		this(setType, newLinkedSet(), false);
	}

	public EnhancedSet(SetType setType, Set<E> delegate) {
		this(setType, delegate, false);
	}

	public EnhancedSet(SetType setType, Set<E> delegate, boolean absent) {
		this.setType = setType;
		this.delegate = delegate;
		this.incomplete = absent;
		this.loaded = !absent;
	}

	@Override
	public void addManipulationListener(ManipulationListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<ManipulationListener>(2);
		}
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
	public SetType type() {
		return setType;
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
	public Set<E> getDelegate() {
		return delegate;
	}

	@Override
	public boolean contains(Object o) {
		ensureComplete();
		return delegate.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		ensureComplete();
		return delegate.containsAll(c);
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
	public Object[] toArray() {
		ensureComplete();
		return delegate.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		ensureComplete();
		return delegate.toArray(a);
	}

	protected GenericModelType getElementType() {
		return setType.getCollectionElementType();
	}

	// tracking sensitive methods

	@Override
	public boolean add(E e) {
		ensureComplete();
		boolean added = delegate.add(e);

		if (added && isNoticing()) {
			AddManipulation manipulation = addManipulation(asMap(e, e), owner);
			RemoveManipulation inverseManipulation = removeManipulation(asMap(e, e), owner);

			manipulation.linkInverse(inverseManipulation);

			noticeManipulation(manipulation);
		}

		return added;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		ensureComplete();
		Map<Object, Object> items = new HashMap<Object, Object>();
		Map<Object, Object> addedItems = new HashMap<Object, Object>();

		for (E e : c) {
			Object valueDescriptor = e;
			if (delegate.add(e)) {
				addedItems.put(valueDescriptor, valueDescriptor);
			}
			items.put(valueDescriptor, valueDescriptor);
		}

		boolean added = !addedItems.isEmpty();

		if (added && isNoticing()) {
			AddManipulation manipulation = addManipulation(items, owner);
			RemoveManipulation inverseManipulation = removeManipulation(addedItems, owner);

			manipulation.linkInverse(inverseManipulation);

			noticeManipulation(manipulation);
		}

		return added;
	}

	@Override
	public void clear() {
		if (!isNoticing()) {
			delegate.clear();
			incomplete = false;
			loaded = true;
			return;
		}

		ensureComplete();
		if (delegate.isEmpty())
			return;

		Map<Object, Object> items = new HashMap<Object, Object>();

		for (E e : delegate) {
			Object valueDescriptor = e;
			items.put(valueDescriptor, valueDescriptor);
		}

		delegate.clear();

		if (isNoticing()) {
			ClearCollectionManipulation manipulation = clearManipulation(owner);
			AddManipulation inverseManipulation = addManipulation(items, owner);

			manipulation.linkInverse(inverseManipulation);

			noticeManipulation(manipulation);
		}
	}

	@Override
	public Iterator<E> iterator() {
		ensureComplete();
		final Iterator<E> delegateIt = delegate.iterator();
		return new Iterator<E>() {
			private E lastReturnedElement;

			@Override
			public boolean hasNext() {
				return delegateIt.hasNext();
			}

			@Override
			public E next() {
				lastReturnedElement = delegateIt.next();
				return lastReturnedElement;
			}

			@Override
			public void remove() {
				delegateIt.remove();

				if (isNoticing()) {
					Object element = lastReturnedElement;

					RemoveManipulation manipulation = removeManipulation(element, element, owner);
					AddManipulation inverseManipulation = addManipulation(element, element, owner);

					manipulation.linkInverse(inverseManipulation);

					noticeManipulation(manipulation);
				}
			}
		};
	}

	@Override
	public boolean remove(Object o) {
		ensureComplete();
		boolean removed = delegate.remove(o);

		if (isNoticing()) {
			E element = (E) o;

			RemoveManipulation manipulation = removeManipulation(element, element, owner);

			if (removed) {
				AddManipulation inverseManipulation = addManipulation(element, element, owner);
				manipulation.linkInverse(inverseManipulation);

			} else {
				setVoidInverseFor(manipulation);
			}

			noticeManipulation(manipulation);
		}

		return removed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		ensureComplete();
		Map<Object, Object> items = new HashMap<Object, Object>();
		Map<Object, Object> deletedItems = new HashMap<Object, Object>();

		Collection<E> elementCollection = (Collection<E>) c;

		for (E element : elementCollection) {
			Object valueDescriptor = element;
			if (delegate.remove(element)) {
				deletedItems.put(valueDescriptor, valueDescriptor);
			}
			items.put(valueDescriptor, valueDescriptor);
		}

		boolean removed = !deletedItems.isEmpty();

		if (isNoticing()) {
			RemoveManipulation manipulation = removeManipulation(items, owner);

			if (removed) {
				AddManipulation inverseManipulation = addManipulation(deletedItems, owner);
				manipulation.linkInverse(inverseManipulation);

			} else {
				setVoidInverseFor(manipulation);
			}

			noticeManipulation(manipulation);
		}

		return removed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		ensureComplete();
		Map<Object, Object> items = new HashMap<Object, Object>();

		Iterator<E> delegateIt = delegate.iterator();

		while (delegateIt.hasNext()) {
			E element = delegateIt.next();

			boolean remove = !c.contains(element);

			if (remove) {
				delegateIt.remove();
				Object valueDescriptor = element;
				items.put(valueDescriptor, valueDescriptor);
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
		if (loaded || GMF.platform().isSingleThreaded()) {
			return;
		}

		ensureCompleteSync();
	}

	private void ensureCompleteSync() {
		if (loaded)
			return;

		loadLock.lock();
		try {
			if (loaded)
				return;

			EnhanceUtil.<Set<E>> loadCollectionLazily(entity, owner, value -> delegate.addAll(value));

			loaded = true;
			incomplete = false;
		} finally {
			loadLock.unlock();
		}
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

	protected void setVoidInverseFor(Manipulation manipulation) {
		manipulation.setInverseManipulation(voidManipulation());
	}

	protected <T extends PropertyManipulation> T prepareManipulation(T collectionManipulation) {
		collectionManipulation.setOwner(owner);
		return collectionManipulation;
	}

}
