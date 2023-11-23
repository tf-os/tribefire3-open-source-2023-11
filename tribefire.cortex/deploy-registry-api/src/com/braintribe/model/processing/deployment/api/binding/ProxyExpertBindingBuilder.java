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

import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.processing.deployment.api.ExpertContext;

/**
 * Coming from the {@link DenotationBindingBuilder} the last step of the proxy binding is to define the expert that will do the actual work
 * at runtime. It can be done in 3 different ways:
 * 
 * <ol>
 * 	<li>passing a factory {@link #expertFactory(Function) function} that will return the expert instance parameterized with an {@link ExpertContext}</li>
 * 	<li>passing the expert {@link #expert(Object) instance} directly</li>
 * 	<li>passing a {@link #expertSupplier(Supplier) supplier} that will supply the expert instance  at runtime</li>
 * </ol>
 * @author Dirk Scheffler
 *
 * @param <D> component type for which the proxy binding is being made
 * @param <T> interface of the expert
 */
public interface ProxyExpertBindingBuilder<D extends Deployable, T> {
	/**
	 * <p>
	 * Binds a proxy expert by a factory {@link Function function} that will build the expert during deployment. This method is the preferred way of binding an expert. 
	 * The factory function is parameterized with the {@link ExpertContext} and its {@link Deployable}
	 * which the function can use to build the actual expert.
	 */
	void expertFactory(Function<ExpertContext<D>, ? extends T> factory);
	
	/**
	 * Directly binds a proxy expert by its instance and therefore establishes a singleton expert. This method can also be used
	 * to use java 8 lambda expressions if suitable.
	 */
	void expert(T expert);
	
	/**
	 * Binds a proxy expert by a factory {@link Supplier supplier} that will build the expert during deployment.
	 */
	void expertSupplier(Supplier<? extends T> supplier);
}
