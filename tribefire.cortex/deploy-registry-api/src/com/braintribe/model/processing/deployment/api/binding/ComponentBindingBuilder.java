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
package com.braintribe.model.processing.deployment.api.binding;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.deployment.api.ComponentBinder;

/**
 * <p>
 * Coming from {@link DenotationBindingBuilder}, the next step of the binding is to define the {@link Deployable} component type and its expert
 * interfaces to progress to the {@link ExpertBindingBuilder} level.
 * 
 * <p>
 * Component types and expert interfaces are normally {@link ComponentBindingBuilder#component(ComponentBinder) given} by a prepared
 * {@link ComponentBinder} implementation. Additionally those needs can be given {@link ComponentBindingBuilder#component(EntityType, Class, Class...)
 * explicitly} or can be partially {@link ComponentBindingBuilder#component(Class, Class...) inferred}.
 * 
 * @author Dirk Scheffler
 *
 * @param <D>
 *            the denotation type for which the binding is being made
 */
public interface ComponentBindingBuilder<D extends Deployable> {
	/**
	 * This is the preferred way of component binding as it makes use of well prepared {@link ComponentBinder} implementations. The API continues to
	 * the actual expert binding on {@link ExpertBindingBuilder} by supplying the component type and the expert type via a {@link ComponentBinder}.
	 * The ComponentBinder will type-safely correlate the component type and the expert type based on Java generics. It will potentially also care for
	 * an automatic expert enriching during deployment.
	 */
	<T> ExpertBindingBuilder<D, T> component(ComponentBinder<? super D, T> componentBinder);

	/**
	 * This way to bind a component is for the rare case that a specific {@link Deployable} type was invented and is to be bound only once, such that
	 * the preparation of a {@link ComponentBinder} would add no value. The API continues to the actual expert binding on {@link ExpertBindingBuilder}
	 * by directly supplying the component type and the expert types. The first expert type is being used to infer type-safety to the expert binding.
	 */
	<T> ExpertBindingBuilder<D, T> component(EntityType<? super D> componentType, Class<T> expertInterface, Class<?>... additionalExpertInterfaces);

	/**
	 * This way to bind a component is for the rare case that a specific {@link Deployable} type was invented and is to be bound only once, such that
	 * the preparation of a {@link ComponentBinder} would add no value. The API continues to the actual expert binding on {@link ExpertBindingBuilder}
	 * by infering the component type from the denotation type that was given on the {@link DenotationBindingBuilder} level and by directly supplying
	 * the expert types. The first expert type is being used to infer type-safety to the expert binding.
	 */
	<T> ExpertBindingBuilder<D, T> component(Class<T> expertInterface, Class<?>... additionalExpertInterfaces);
}
