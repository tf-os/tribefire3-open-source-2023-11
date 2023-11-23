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
package com.braintribe.model.processing.deployment.api;

import java.util.function.Supplier;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;

public interface MultiplexComponentBinding<D extends Deployable> {

	/**
	 * Binds a {@link Deployable} denotation type hold by this builder to an expert with the help of a {@link ComponentBinder}. This binding will be used
	 * during a deployment of and instance of the denotation type.
	 * 
	 * @param componentBinder the component binder that acts as a type-safety correlation between denotation type and expert. It is also a 
	 * 	potential enricher of the expert being supplied by the valueSupplier param. Another quality of the component binder is the announcement of the interfaces the final expert will have.
	 * @param valueSupplier the supplier that will supply the actual expert during a deployment of the deployable type
	 * @return <code>this</code> in order to allow the continuation of the multiplex bindings in a fluent way
	 */
	<T> MultiplexComponentBinding<D> bind(ComponentBinder<? super D, T> componentBinder, Supplier<? extends T> valueSupplier);

	/**
	 * Binds the {@link Deployable} denotation type hold by this builder to an expert without the help of a convenient {@link ComponentBinder}. Because of that no enriching will take place
	 * and the expert interfaces must be announced manually.
	 * 
	 * @param componentType the component type for which you are binding an expert
	 * @param valueSupplier the supplier that will supply the actual expert during a deployment of the deployable type
	 * @param componentInterfaces the interfaces that the expert will support.
	 * @return <code>this</code> in order to allow the continuation of the multiplex bindings in a fluent way
	 */
	<T> MultiplexComponentBinding<D> bindPlain(EntityType<? super D> componentType, Supplier<? extends T> valueSupplier, Class<T> ... componentInterfaces);
}
