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
package com.braintribe.model.generic.reflection;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;

import jsinterop.annotations.JsType;

/**
 * @see Property
 * @see TransientProperty
 */
@JsType(namespace=GmCoreApiInteropNamespaces.reflection)
@SuppressWarnings("unusable-by-js")
public interface Attribute {

	String getName();

	Class<?> getJavaType();

	/**
	 * Returns the {@link EntityType} which declared this property. Note that this does not mean, that the property is not inherited by the returned
	 * type, it may have just re-declared it with a different initializer.
	 */
	EntityType<?> getDeclaringType();

	/**
	 * Returns the first type in the hierarchy (when examined with depth-first search) where this property was declared. This means this property is
	 * not inherited by the returned type.
	 */
	EntityType<?> getFirstDeclaringType();

	/** This is false only for properties defined as primitive java types. */
	boolean isNullable();

	/** Accesses the attribute in a way equivalent to invoking the corresponding getter or setter. */
	<T> T get(GenericEntity entity);
	/** @see #get */
	void set(GenericEntity entity, Object value);

	/**
	 * Accesses the property directly, bypassing any possible configured {@link PropertyAccessInterceptor}s as well as type checks. The type check is
	 * expected to happen when the result is returned, if at all.
	 */
	<T> T getDirectUnsafe(GenericEntity entity);
	/** Sets the property directly, bypassing any possible configured {@link PropertyAccessInterceptor}s as well as type checks. */
	void setDirectUnsafe(GenericEntity entity, Object value);

	/**
	 * Same as {@link #getDirectUnsafe(GenericEntity)}. See {@link #setDirect(GenericEntity, Object)}.
	 */
	<T> T getDirect(GenericEntity entity);
	/**
	 * Sets the property directly, bypassing any possible configured {@link PropertyAccessInterceptor}s, but does type checking of the value against
	 * the attribute's type.
	 */
	Object setDirect(GenericEntity entity, Object value);

}
