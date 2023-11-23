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
package com.braintribe.model.processing.core.expert.api;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMap;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMultiMap;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * Multi-map which maps {@link EntityType}s to multiple values. The way how the value is resolved depends on the implementation.
 * <p>
 * The get/find methods inherited from {@link DenotationMap} return the first element of the list returned from {@link #findAll(EntityType)} (or
 * <tt>null</tt> / throw exception if the list is empty).
 * <p>
 * The basic use-case is a multi-map where values are inherited alongside the type hierarchy - i.e. when resolving values associated with given type,
 * the result also includes all values associated with all the super-types.
 * <p>
 * For use-cases where just the value associated with the most specific super-type is returned see {@link PolymorphicDenotationMap}.
 * 
 * @see DenotationMap
 * @see PolymorphicDenotationMultiMap
 * 
 * @author peter.gazdik
 */
@JsType(namespace = GmCoreApiInteropNamespaces.util)
@SuppressWarnings("unusable-by-js")
public interface DenotationMultiMap<B extends GenericEntity, V> extends DenotationMap<B, V> {

	@JsMethod(name = "findAllByType")
	<T extends V> List<T> findAll(EntityType<? extends B> denotationType);

	<T extends V> List<T> findAll(B denotation);
}
