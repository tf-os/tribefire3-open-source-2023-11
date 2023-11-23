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
package com.braintribe.model.processing.meta.cmd;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.experts.SelectorExpert;
import com.braintribe.model.processing.meta.oracle.ModelOracle;

/**
 * 
 */
public class ResolutionContextBuilder implements CmdResolverBuilder {

	private final ResolutionContextInfo rci;

	private final Map<EntityType<? extends MetaDataSelector>, SelectorExpert<?>> experts = newMap();
	private final Map<Class<? extends SelectorContextAspect<?>>, Object> aspectValues = newMap();
	private final Map<Class<? extends SelectorContextAspect<?>>, Supplier<?>> dynamicAspectValueProviders = newMap();
	private Set<MetaData> defaultMetaData = Collections.emptySet();
	private Supplier<?> sessionProvider;
	private int maxSessionCacheSize = 50;

	public ResolutionContextBuilder(ModelOracle modelOracle) {
		this.rci = new ResolutionContextInfo(modelOracle);
	}

	@Override
	public <T extends MetaDataSelector> ResolutionContextBuilder addExpert(EntityType<? extends T> entityType, SelectorExpert<T> expert) {
		if (expert != null)
			experts.put(entityType, expert);
		return this;
	}

	@Override
	public ResolutionContextBuilder addExperts(Map<EntityType<? extends MetaDataSelector>, SelectorExpert<?>> experts) {
		if (experts != null)
			this.experts.putAll(experts);
		return this;
	}

	@Override
	public <T, A extends SelectorContextAspect<T>> ResolutionContextBuilder addStaticAspect(Class<A> aspect, T value) {
		if (value != null)
			aspectValues.put(aspect, value);

		return this;
	}

	@Override
	public <T, A extends SelectorContextAspect<T>> ResolutionContextBuilder addDynamicAspectProvider(Class<A> clazz, Supplier<T> provider) {
		if (provider != null)
			dynamicAspectValueProviders.put(clazz, provider);

		return this;
	}

	@Override
	public ResolutionContextBuilder addDynamicAspectProviders(Map<Class<? extends SelectorContextAspect<?>>, Supplier<?>> providers) {
		if (providers != null)
			dynamicAspectValueProviders.putAll(providers);

		return this;
	}

	@Override
	public ResolutionContextBuilder setSessionProvider(Supplier<?> sessionProvider) {
		this.sessionProvider = sessionProvider;
		return this;
	}

	@Override
	public ResolutionContextBuilder setMaxSessionCacheSize(int maxSessionCacheSize) {
		this.maxSessionCacheSize = maxSessionCacheSize;
		return this;
	}

	@Override
	public ResolutionContextBuilder setDefaultMetaData(Set<MetaData> defaultMetaData) {
		this.defaultMetaData = defaultMetaData;
		return this;
	}

	public ResolutionContextInfo build() {
		rci.setStaticAspects(aspectValues);
		rci.setExperts(experts);
		rci.setDynamicAspectValueProviders(dynamicAspectValueProviders);
		rci.setSessionProvider(sessionProvider);
		rci.setMaxSessionCacheSize(maxSessionCacheSize);
		rci.setDefaultMetaData(defaultMetaData);

		return rci;
	}

	@Override
	public CmdResolver done() {
		return new CmdResolverImpl(build());
	}

}
