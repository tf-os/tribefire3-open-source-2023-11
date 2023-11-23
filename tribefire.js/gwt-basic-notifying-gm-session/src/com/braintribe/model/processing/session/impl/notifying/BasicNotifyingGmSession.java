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
package com.braintribe.model.processing.session.impl.notifying;

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.localEntityProperty;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.LifecycleManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;
import com.braintribe.model.generic.session.exception.GmSessionRuntimeException;
import com.braintribe.model.generic.tracking.DelegatingManipulationListener;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.core.commons.LocalEntityPropertyComparator;
import com.braintribe.model.processing.session.api.notifying.CompoundNotification;
import com.braintribe.model.processing.session.api.notifying.EntityManipulationListenerRegistry;
import com.braintribe.model.processing.session.api.notifying.GenericManipulationListenerRegistry;
import com.braintribe.model.processing.session.api.notifying.ManipulationListenerRegistry;
import com.braintribe.model.processing.session.api.notifying.NotifyingGmSession;
import com.braintribe.model.processing.session.api.notifying.PropertyAccessInterceptorRegistry;
import com.braintribe.model.processing.session.api.notifying.interceptors.InterceptorIdentification;
import com.braintribe.model.processing.session.impl.session.AbstractGmSession;

@SuppressWarnings("unusable-by-js")
public class BasicNotifyingGmSession extends AbstractGmSession implements NotifyingGmSession {

	/* For JUNIT tests see com.braintribe.gm:basic-persistence-gm-session-test */

	private static final Logger logger = Logger.getLogger(BasicNotifyingGmSession.class);

	protected List<ManipulationListenerEntry> manipulationListeners = newList();
	protected List<ManipulationListenerEntry> manipulationListenersModified = null;

	private final PropertyAccessInterceptorsBuilderImpl propertyAccessInterceptorRegistry = new PropertyAccessInterceptorsBuilderImpl();
	private final GenericManipulationListenerRegistryImpl genericManipulationListenerRegistry = new GenericManipulationListenerRegistryImpl();

	public Map<GenericEntity, ManipulationListener> entityManipulationListeners = newMap();
	public Map<LocalEntityProperty, ManipulationListener> propertyManipulationListeners = CodingMap.create(LocalEntityPropertyComparator.INSTANCE);

	private final Stack<CompoundNotification> compoundNotificationStack = new Stack<>();

	private boolean suppressNoticing;
	private int noticingCount;

	public void setSuppressNoticing(boolean suppressNoticing) {
		this.suppressNoticing = suppressNoticing;
	}

	protected Property getProperty(EntityProperty entityProperty) {
		EntityType<?> entityType = entityProperty.getReference().valueType();
		return entityType.getProperty(entityProperty.getPropertyName());
	}

	@Override
	public Stack<CompoundNotification> getCompoundNotificationStack() {
		return compoundNotificationStack;
	}

	@Override
	public void noticeManipulation(Manipulation manipulation) {
		Objects.requireNonNull(manipulation, "Cannot notice a null manipulation. This indices some problem with the way this session is used.");

		if (suppressNoticing)
			return;

		try {
			noticingCount++;
			LocalEntityProperty entityProperty = null;
			GenericEntity entity = null;

			if (manipulation instanceof PropertyManipulation) {
				Owner owner = ((PropertyManipulation) manipulation).getOwner();
				entityProperty = (LocalEntityProperty) owner;
				entity = entityProperty.getEntity();

				ManipulationListener listener = propertyManipulationListeners.get(entityProperty);
				notifyListener(manipulation, listener, false);

			} else if (manipulation instanceof LifecycleManipulation) {
				entity = ((LifecycleManipulation) manipulation).getEntity();
			}

			ManipulationListener listener = entityManipulationListeners.get(entity);
			notifyListener(manipulation, listener, false);

			for (ManipulationListenerEntry listenerEntry : manipulationListeners)
				notifyListener(manipulation, listenerEntry.listener, listenerEntry.isCore);

			if (manipulation.manipulationType() == ManipulationType.COMPOUND) {
				CompoundManipulation compoundManipulation = (CompoundManipulation) manipulation;
				List<Manipulation> manipulations = compoundManipulation.getCompoundManipulationList();

				BasicCompoundNotification compoundNotification = new BasicCompoundNotification();
				compoundNotification.compoundManipulation = compoundManipulation;
				compoundNotificationStack.push(compoundNotification);

				try {
					for (Manipulation nestedManipulation : manipulations) {
						noticeManipulation(nestedManipulation);
						compoundNotification.index++;
					}
				} finally {
					compoundNotificationStack.pop();
				}
			}

		} finally {
			noticingCount--;
			if (manipulationListenersModified != null) {
				manipulationListeners = manipulationListenersModified;
				manipulationListenersModified = null;
			}
		}
	}

	/** Listener can be null, this simplifies the caller code - so it doesn'thave to handle null checks. */
	private void notifyListener(Manipulation manipulation, ManipulationListener listener, boolean isCore) {
		try {
			if (listener != null)
				listener.noticeManipulation(manipulation);

		} catch (Exception e) {
			if (isCore)
				throw e;
			else
				logger.error("Error while notifying manipulation listener: " + listener, e);
		}
	}

	@Override
	public GenericManipulationListenerRegistry listeners() {
		return genericManipulationListenerRegistry;
	}

	@Override
	public PropertyAccessInterceptorRegistry interceptors() {
		return propertyAccessInterceptorRegistry;
	}

	protected void addManipulationListener(ManipulationListenerEntry listenerEntry) {
		getManListenersToModify().add(listenerEntry);
	}

	protected void addFirstManipulationListener(ManipulationListenerEntry listenerEntry) {
		getManListenersToModify().add(0, listenerEntry);
	}

	protected void removeManipulationListener(ManipulationListener listener) {
		Iterator<ManipulationListenerEntry> it = getManListenersToModify().iterator();

		while (it.hasNext())
			if (it.next().listener == listener)
				it.remove();
	}

	private List<ManipulationListenerEntry> getManListenersToModify() {
		return noticingCount > 0 ? acquireManListenersModified() : manipulationListeners;
	}

	private List<ManipulationListenerEntry> acquireManListenersModified() {
		if (manipulationListenersModified == null)
			manipulationListenersModified = newList(manipulationListeners);

		return manipulationListenersModified;
	}

	private static class ManipulationListenerEntry {
		public ManipulationListener listener;
		public boolean isCore;
	}

	@SuppressWarnings("unusable-by-js")
	private class PropertyAccessInterceptorsBuilderImpl implements PropertyAccessInterceptorRegistry {

		@Override
		public PropertyAccessInterceptorRegistry with(Class<? extends InterceptorIdentification> identification) {
			interceptors.with(identification);
			return this;
		}

		@Override
		public PropertyAccessInterceptorRegistry before(Class<? extends InterceptorIdentification> identification) {
			interceptors.before(identification);
			return this;
		}

		@Override
		public PropertyAccessInterceptorRegistry after(Class<? extends InterceptorIdentification> identification) {
			interceptors.after(identification);
			return this;
		}

		@Override
		public void add(PropertyAccessInterceptor interceptor) {
			interceptors.add(interceptor);
			refreshInterceptorChain();
		}

		@Override
		public PropertyAccessInterceptor replace(Class<? extends InterceptorIdentification> identification, PropertyAccessInterceptor interceptor) {
			PropertyAccessInterceptor result = interceptors.replace(identification, interceptor);
			refreshInterceptorChain();

			return result;
		}

		@Override
		public void remove(PropertyAccessInterceptor interceptor) {
			interceptors.remove(interceptor);
			refreshInterceptorChain();
		}
		
	}

	@SuppressWarnings("unusable-by-js")
	private class GenericManipulationListenerRegistryImpl implements GenericManipulationListenerRegistry {

		protected boolean isCore;

		@Override
		public ManipulationListenerRegistry asCore(boolean isCore) {
			this.isCore = isCore;
			return this;
		}

		protected ManipulationListenerEntry entryFor(ManipulationListener listener) {
			ManipulationListenerEntry result = new ManipulationListenerEntry();
			result.listener = listener;
			result.isCore = isCore;
			return result;
		}

		@Override
		public void add(ManipulationListener listener) {
			addManipulationListener(entryFor(listener));
		}

		@Override
		public void addFirst(ManipulationListener listener) {
			addFirstManipulationListener(entryFor(listener));
		}

		@Override
		public void remove(ManipulationListener listener) {
			removeManipulationListener(listener);
		}

		@Override
		public EntityManipulationListenerRegistry entity(GenericEntity entity) {
			return new LocalEntityManipulationListenerRegistryImpl(entity);
		}

		@Override
		public ManipulationListenerRegistry entityProperty(GenericEntity entity, String property) {
			return entityProperty(localEntityProperty(entity, property));
		}

		@Override
		public ManipulationListenerRegistry entityProperty(LocalEntityProperty entityProperty) {
			return new LocalPropertyManipulationListenerRegistryImpl(entityProperty);
		}
	}

	@SuppressWarnings("unusable-by-js")
	private class LocalEntityManipulationListenerRegistryImpl implements EntityManipulationListenerRegistry {

		private final GenericEntity entity;

		public LocalEntityManipulationListenerRegistryImpl(GenericEntity entity) {
			this.entity = entity;
		}

		@Override
		public void add(ManipulationListener listener) {
			addFilteredManipulationListener(entity, listener, false, entityManipulationListeners);
		}

		@Override
		public void addFirst(ManipulationListener listener) {
			addFilteredManipulationListener(entity, listener, true, entityManipulationListeners);
		}

		@Override
		public void remove(ManipulationListener listener) {
			removeFilteredManipulationListener(entity, listener, entityManipulationListeners);
		}

		@Override
		public ManipulationListenerRegistry property(String propertyName) {
			LocalEntityProperty entityProperty = localEntityProperty(entity, propertyName);
			return new LocalPropertyManipulationListenerRegistryImpl(entityProperty);
		}

		@Override
		public ManipulationListenerRegistry property(Property property) {
			return property(property.getName());
		}

	}

	@SuppressWarnings("unusable-by-js")
	private class LocalPropertyManipulationListenerRegistryImpl implements ManipulationListenerRegistry {

		private final LocalEntityProperty localEntityProperty;

		public LocalPropertyManipulationListenerRegistryImpl(LocalEntityProperty localEntityProperty) {
			this.localEntityProperty = localEntityProperty;
		}

		@Override
		public void add(ManipulationListener listener) {
			addFilteredManipulationListener(localEntityProperty, listener, false, propertyManipulationListeners);
		}

		@Override
		public void addFirst(ManipulationListener listener) {
			addFilteredManipulationListener(localEntityProperty, listener, true, propertyManipulationListeners);
		}

		@Override
		public void remove(ManipulationListener listener) {
			removeFilteredManipulationListener(localEntityProperty, listener, propertyManipulationListeners);
		}
	}

	public <E> void addFilteredManipulationListener(E key, ManipulationListener listener, boolean addFirst, Map<E, ManipulationListener> listeners) {
		ManipulationListener existingListener = listeners.get(key);
		if (existingListener != null) {
			DelegatingManipulationListener multicastManipulationListener = null;
			if (existingListener instanceof DelegatingManipulationListener) {
				multicastManipulationListener = (DelegatingManipulationListener) existingListener;
			} else {
				multicastManipulationListener = new DelegatingManipulationListener();
				multicastManipulationListener.addDelegate(existingListener);
				listeners.put(key, multicastManipulationListener);
			}
			multicastManipulationListener.addDelegate(listener, addFirst);
		} else {
			listeners.put(key, listener);
		}
	}

	public <E> boolean removeFilteredManipulationListener(E key, ManipulationListener listener, Map<E, ManipulationListener> listeners) {
		ManipulationListener existingListener = listeners.get(key);
		if (existingListener == null)
			return false;

		if (existingListener instanceof DelegatingManipulationListener) {
			DelegatingManipulationListener multicastManipulationListener = (DelegatingManipulationListener) existingListener;
			if (multicastManipulationListener.getDelegates().remove(listener)) {
				if (multicastManipulationListener.getDelegates().isEmpty()) {
					listeners.remove(key);
				}

				return true;
			} else
				return false;
		} else {
			if (existingListener == listener) {
				listeners.remove(key);
				return true;
			} else
				return false;
		}
	}

	@SuppressWarnings("unusable-by-js")
	public static class MultiValue<T> implements Iterable<T> {
		@SuppressWarnings("serial")
		private static class CollectionImpl<E> extends HashSet<E> {
			// nothing
		}

		private Object valueOrValues;

		@Override
		public Iterator<T> iterator() {
			if (valueOrValues == null)
				return Collections.<T> emptySet().iterator();

			if (valueOrValues instanceof CollectionImpl) {
				Collection<T> collection = (CollectionImpl<T>) valueOrValues;
				return collection.iterator();
			}

			return Collections.singleton((T) valueOrValues).iterator();
		}

		public boolean append(T value) {
			if (valueOrValues == null) {
				valueOrValues = value;
				return true;

			} else if (valueOrValues instanceof CollectionImpl<?>) {
				CollectionImpl<T> collection = (CollectionImpl<T>) valueOrValues;
				return collection.add(value);

			} else {
				CollectionImpl<T> collection = new CollectionImpl<T>();
				T singleValue = (T) valueOrValues;
				collection.add(singleValue);
				collection.add(value);
				valueOrValues = collection;
				return true;
			}
		}

		public boolean remove(T value) {
			if (valueOrValues == null) {
				return false;

			} else if (valueOrValues instanceof CollectionImpl<?>) {
				CollectionImpl<T> collection = (CollectionImpl<T>) valueOrValues;
				if (collection.remove(value)) {
					if (collection.size() == 1) {
						valueOrValues = collection.iterator().next();
					}
					return true;
				} else
					return false;

			} else {
				T singleValue = (T) valueOrValues;
				if (value.equals(singleValue)) {
					valueOrValues = null;
					return true;
				} else
					return false;
			}
		}

		public boolean isEmpty() {
			return valueOrValues == null;
		}
	}

	/**
	 * @param entityReference
	 *            reference to be resolved
	 */
	protected GenericEntity resolveEntity(EntityReference entityReference) throws GmSessionRuntimeException {
		throw new UnsupportedOperationException("Usage of remote references is not supported by this session");
	}

	public void cleanup() {
		manipulationListeners.clear();
	}

	private static class BasicCompoundNotification implements CompoundNotification {
		public CompoundManipulation compoundManipulation;
		public int index;

		@Override
		public CompoundManipulation getCompoundManipulation() {
			return compoundManipulation;
		}

		@Override
		public int getIndex() {
			return index;
		}
	}
}
