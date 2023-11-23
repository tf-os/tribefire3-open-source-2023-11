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
package com.braintribe.model.processing.meta.cmd.context;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.meta.cmd.CascadingMetaDataException;
import com.braintribe.model.processing.meta.oracle.ModelOracle;

/** Standard implementation of {@link MutableSelectorContext}. */
public class SelectorContextImpl implements MutableSelectorContext {

	//
	// CAREFUL!!!
	//
	// Any new fields here have to be reflected in the "copy" method.
	//

	private final ModelOracle modelOracle;

	private final Supplier<?> sessionProvider;
	private Object cachedSession;

	private Map<Class<? extends SelectorContextAspect<?>>, Supplier<?>> dynamicAspectValueProviders;
	private final Map<Class<? extends SelectorContextAspect<?>>, Object> aspectValues = newMap();

	private boolean ignoreSelectors;
	private Set<EntityType<?>> notIgnoredSelectors = emptySet();

	private static final Logger log = Logger.getLogger(SelectorContextImpl.class);

	public SelectorContextImpl(ModelOracle modelOracle, Supplier<?> sessionProvider) {
		this.modelOracle = modelOracle;
		this.sessionProvider = sessionProvider;
	}

	public void setDynamicAspectValueProviders(Map<Class<? extends SelectorContextAspect<?>>, Supplier<?>> dynamicAspectValueProviders) {
		this.dynamicAspectValueProviders = dynamicAspectValueProviders;
	}

	@Override
	public ModelOracle getModelOracle() {
		return modelOracle;
	}

	@Override
	public <T, A extends SelectorContextAspect<? super T>> void put(Class<A> aspect, T value) {
		aspectValues.put(aspect, requireNonNull(value, "Value of an aspect cannot be null."));
	}

	public void putAll(Map<Class<? extends SelectorContextAspect<?>>, Object> aspects) {
		aspectValues.putAll(aspects);
	}

	@Override
	public <T, A extends SelectorContextAspect<T>> T getNotNull(Class<A> aspect) throws CascadingMetaDataException {
		T value = get(aspect);
		if (value == null)
			throw new CascadingMetaDataException("Aspect not found in the context: " + aspect.getName());

		return value;
	}

	@Override
	public <T, A extends SelectorContextAspect<T>> T get(Class<A> aspect) throws CascadingMetaDataException {
		T value = (T) aspectValues.get(aspect);

		if (value == null && dynamicAspectValueProviders != null) {
			Supplier<T> provider = (Supplier<T>) dynamicAspectValueProviders.get(aspect);

			if (provider != null) {
				try {
					value = provider.get();
					aspectValues.put(aspect, value);

				} catch (RuntimeException e) {
					throw new CascadingMetaDataException("Error while providing value for aspect: " + aspect.getName(), e);
				}
			}
		}

		return value;
	}

	@Override
	public Object provideSession() throws CascadingMetaDataException {
		if (cachedSession == null)
			cachedSession = provideSessionHelper();

		return cachedSession;
	}

	private Object provideSessionHelper() throws CascadingMetaDataException {
		if (sessionProvider == null) {
			log.warn("CMD Resolver not configured properly. No session provider found, will not cache session-scoped meta-data.");

			return newSessionObject();
		}

		try {
			return sessionProvider.get();

		} catch (RuntimeException e) {
			log.warn("Error wile providing session object, will not cache session-scoped meta-data", e);

			return newSessionObject();
		}
	}

	private Object newSessionObject() {
		return new Object();
	}

	@Override
	public boolean shouldIgnoreSelectors() {
		return ignoreSelectors;
	}

	@Override
	public Set<EntityType<?>> notIgnoredSelectors() {
		return notIgnoredSelectors;
	}

	@Override
	public void ignoreSelectors(EntityType<?>... exceptions) {
		ignoreSelectors = true;
		notIgnoredSelectors = asSet(exceptions);
	}

	@Override
	public MutableSelectorContext copy() {
		SelectorContextImpl result = new SelectorContextImpl(modelOracle, sessionProvider);
		result.dynamicAspectValueProviders = dynamicAspectValueProviders;
		result.cachedSession = cachedSession;
		result.aspectValues.putAll(aspectValues);
		result.ignoreSelectors = ignoreSelectors;
		result.notIgnoredSelectors = notIgnoredSelectors;

		return result;
	}
}
