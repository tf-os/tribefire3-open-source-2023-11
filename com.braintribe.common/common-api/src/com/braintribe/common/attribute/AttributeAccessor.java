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
package com.braintribe.common.attribute;

import java.util.Optional;
import java.util.function.Supplier;

import jsinterop.annotations.JsType;
import jsinterop.context.JsInteropNamespaces;

/**
 * A {@link TypeSafeAttribute} accessor that can be defined as a static field of an aspect and can be used instead of a class literal. This is
 * especially useful in tf.js.
 * 
 * Example:
 * 
 * <pre>
 * public interface MyAttribute extends TypeSafeAttribute<String> {
 * 	AttributeAccessor<MyAttribute> $ = AttributeAccessor.create(MyAttribute.class);
 * }
 * 
 * MyAttribute.$.get(typeSafeAttributes);
 * MyAttribute.$.set(evalContext, "MyValue");
 * </pre>
 * 
 * @author peter.gazdik
 */
@JsType(namespace = JsInteropNamespaces.attr)
public interface AttributeAccessor<T> {

	T get(TypeSafeAttributes attributes);

	T findOrNull(TypeSafeAttributes attributes);

	T findOrDefault(TypeSafeAttributes attributes, T defaultValue);

	T findOrSupply(TypeSafeAttributes attributes, Supplier<T> defaultValueSupplier);

	Optional<T> find(TypeSafeAttributes attributes);

	void set(TypeSafeAttribution attribution, T value);

	static <T, A extends TypeSafeAttribute<T>> AttributeAccessor<T> create(Class<A> key) {
		return new BasicAttributeAccessor<>(key);
	}

}

class BasicAttributeAccessor<T, A extends TypeSafeAttribute<T>> implements AttributeAccessor<T> {

	private final Class<A> key;

	public BasicAttributeAccessor(Class<A> key) {
		this.key = key;
	}

	@Override
	public T get(TypeSafeAttributes attributes) {
		return attributes.getAttribute(key);
	}

	@Override
	public T findOrNull(TypeSafeAttributes attributes) {
		return attributes.findOrNull(key);
	}

	@Override
	public T findOrDefault(TypeSafeAttributes attributes, T defaultValue) {
		return attributes.findOrDefault(key, defaultValue);
	}

	@Override
	public T findOrSupply(TypeSafeAttributes attributes, Supplier<T> defaultValueSupplier) {
		return attributes.findOrSupply(key, defaultValueSupplier);
	}

	@Override
	public Optional<T> find(TypeSafeAttributes attributes) {
		return attributes.findAttribute(key);
	}

	@Override
	public void set(TypeSafeAttribution attribution, T value) {
		attribution.setAttribute(key, value);
	}

}
