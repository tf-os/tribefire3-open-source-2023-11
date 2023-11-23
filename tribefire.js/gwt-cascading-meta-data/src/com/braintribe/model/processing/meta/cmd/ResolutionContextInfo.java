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
import com.braintribe.model.processing.meta.cmd.context.SelectorContext;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.experts.SelectorExpert;
import com.braintribe.model.processing.meta.oracle.ModelOracle;

/**
 * Wrapper for all the parameters for configuration of {@link CmdResolverImpl}.
 */
public class ResolutionContextInfo {

	private final ModelOracle modelOracle;
	private Map<EntityType<? extends MetaDataSelector>, SelectorExpert<?>> experts = newMap();
	private Map<Class<? extends SelectorContextAspect<?>>, Object> aspectValues = Collections.emptyMap();
	private Map<Class<? extends SelectorContextAspect<?>>, Supplier<?>> dynamicAspectValueProviders; // nullable
	private Set<MetaData> defaultMetaData = Collections.emptySet();
	private Supplier<?> sessionProvider; // nullable
	private int maxSessionCacheSize;

	public ResolutionContextInfo(ModelOracle modelOracle) {
		this.modelOracle = modelOracle;
	}

	public ModelOracle getModelOracle() {
		return modelOracle;
	}

	/** Sets the {@link SelectorExpert}s */
	public void setExperts(Map<EntityType<? extends MetaDataSelector>, SelectorExpert<?>> experts) {
		this.experts = experts;
	}

	public Map<EntityType<? extends MetaDataSelector>, SelectorExpert<?>> getExperts() {
		return experts;
	}

	/**
	 * Sets the 'static' aspects for given resolver. This gives us an option to provide some aspects which should be
	 * part of every {@link SelectorContext} (i.e. for every single resolution). This may be for instance used for
	 * aspects describing the environment like operating system, JVM version or word size (32/64 bit).
	 */
	public void setStaticAspects(Map<Class<? extends SelectorContextAspect<?>>, Object> aspectValues) {
		this.aspectValues = aspectValues;
	}

	public Map<Class<? extends SelectorContextAspect<?>>, Object> getStaticAspects() {
		return aspectValues;
	}

	/**
	 * Sets providers which are able to provide a given {@link SelectorContextAspect}. The resolver tries to retrieve a
	 * given aspect only iff it does not the value specified in the {@link SelectorContext} directly. This may be
	 * therefore used to provide a backup (overridable default) value of aspect.
	 */
	public void setDynamicAspectValueProviders(Map<Class<? extends SelectorContextAspect<?>>, Supplier<?>> dynamicAspectValueProviders) {
		this.dynamicAspectValueProviders = dynamicAspectValueProviders;
	}

	public Map<Class<? extends SelectorContextAspect<?>>, Supplier<?>> getDynamicAspectValueProviders() {
		return dynamicAspectValueProviders;
	}

	/**
	 * Default {@link MetaData} for given meta data type.
	 */
	public Set<MetaData> getDefaultMetaData() {
		return defaultMetaData;
	}

	public void setDefaultMetaData(Set<MetaData> defaultMetaData) {
		this.defaultMetaData = defaultMetaData;
	}

	/**
	 * Sets a provider, which provides an object that represents a given session. The provided object itself doesn't
	 * have to be the session, it's just important, that the provider returns the same value for all invocations within
	 * the same session. This object is merely used as a key in the session-scoped cache.
	 */
	public void setSessionProvider(Supplier<?> sessionProvider) {
		this.sessionProvider = sessionProvider;
	}

	public Supplier<?> getSessionProvider() {
		return sessionProvider;
	}

	/**
	 * Sets the maximum number of sessions for which meta data are cached at the same time. If we try to retrieve meta
	 * data for more sessions at once, some of the cached entries must be removed before we put new entries to the
	 * cache.
	 */
	public void setMaxSessionCacheSize(int maxSessionCacheSize) {
		this.maxSessionCacheSize = maxSessionCacheSize;
	}

	public int getMaxSessionCacheSize() {
		return maxSessionCacheSize;
	}
}
