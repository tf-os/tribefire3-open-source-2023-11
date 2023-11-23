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
package com.braintribe.provider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public abstract class ScopedBeanProvider<T> extends AbstractBeanProvider<T> {
	private Map<Object, T> instances = new HashMap<>();
	private Set<Object> creatingScopes = new HashSet<>();
	private ScopeManager<?> scopeManager;
	private ReentrantLock scopeManagerLock = new ReentrantLock();
	private Instantiation instantiation = Instantiation.lazy;

	public enum Instantiation {
		lazy,
		eager
	}

	@SuppressWarnings("unchecked")
	public ScopedBeanProvider(ScopeManager<?> scopeManager) {
		this.scopeManager = scopeManager;
		scopeManager.addScopeListener(new ScopeListenerImpl());
	}

	public void setInstantiation(Instantiation instantiation) {
		this.instantiation = instantiation;
	}

	public void configure(@SuppressWarnings("unused") T bean) throws Exception {
		// default implementation does nothing
	}

	public void dispose(T bean) throws Exception {
		scopeManager.disposeBean(bean);
	}

	protected final T publish(T bean) {
		Object scopeInstance = scopeManager.getCurrentScope();
		instances.put(scopeInstance, bean);
		creatingScopes.remove(scopeInstance);
		return bean;
	}

	protected <E> Supplier<E> forCurrentScope(final Supplier<E> provider) {
		final Object scopeInstance = scopeManager.getCurrentScope();
		final ScopeManager<Object> manager = (ScopeManager<Object>) scopeManager;
		return new Supplier<E>() {
			@Override
			public E get() throws RuntimeException {
				try {
					manager.pushScope(scopeInstance);
					return provider.get();
				} finally {
					manager.popScope();
				}
			}
		};
	}

	@Override
	public T get() throws RuntimeException {
		scopeManagerLock.lock();
		try {
			Object scopeInstance = scopeManager.getCurrentScope();

			if (!instances.containsKey(scopeInstance)) {
				if (creatingScopes.contains(scopeInstance)) {
					throw new RuntimeException("You have problematic circular references. Use configure method to solve that problem");
				}

				T instance = null;
				// create the basic object (in most cases just constructor calls)
				try {
					creatingScopes.add(scopeInstance);
					ensurePreconditions();
					instance = create();
					instances.put(scopeInstance, instance);
				} catch (Exception e) {
					throw new RuntimeException(e);
				} finally {
					creatingScopes.remove(scopeInstance);
				}

				// configure the object (used especially for possible circular references)
				try {
					configure(instance);
					scopeManager.intializeBean(instance);
					ensureAttachmentsInstantiated();

				} catch (Exception e) {
					throw new RuntimeException(e);
				}

				return instance;
			}
			return instances.get(scopeInstance);
		} finally {
			scopeManagerLock.unlock();
		}
	}

	@SuppressWarnings("rawtypes")
	private class ScopeListenerImpl implements ScopeListener {
		@Override
		public void scopeOpened(Object scope) {
			if (instantiation == Instantiation.eager) {
				try {
					get();
				} catch (RuntimeException e) {
					throw new RuntimeException("eager bean could not be instantiated", e);
				}
			}
		}

		@Override
		public void scopeClosed(Object scope) {
			if (instances.containsKey(scope)) {
				T instance = instances.remove(scope);
				try {
					if (instance != null) {
						dispose(instance);
					}
				} catch (Exception e) {
					throw new RuntimeException("error while disposing bean", e);
				}
			}
		}
	}
}
