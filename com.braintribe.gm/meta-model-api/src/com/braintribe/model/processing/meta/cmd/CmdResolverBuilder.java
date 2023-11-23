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

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.experts.SelectorExpert;

/**
 * @author peter.gazdik
 */
public interface CmdResolverBuilder {

	default CmdResolverBuilder initialize(Consumer<CmdResolverBuilder> initializer) {
		if (initializer != null)
			initializer.accept(this);
		return this;
	}

	<T extends MetaDataSelector> CmdResolverBuilder addExpert(EntityType<? extends T> entityType, SelectorExpert<T> expert);
	CmdResolverBuilder addExperts(Map<EntityType<? extends MetaDataSelector>, SelectorExpert<?>> experts);

	<T, A extends SelectorContextAspect<T>> CmdResolverBuilder addStaticAspect(Class<A> aspect, T value);

	<T, A extends SelectorContextAspect<T>> CmdResolverBuilder addDynamicAspectProvider(Class<A> clazz, Supplier<T> provider);
	CmdResolverBuilder addDynamicAspectProviders(Map<Class<? extends SelectorContextAspect<?>>, Supplier<?>> providers);

	CmdResolverBuilder setSessionProvider(Supplier<?> sessionProvider);

	CmdResolverBuilder setMaxSessionCacheSize(int maxSessionCacheSize);

	CmdResolverBuilder setDefaultMetaData(Set<MetaData> defaultMetaData);

	CmdResolver done();
}
