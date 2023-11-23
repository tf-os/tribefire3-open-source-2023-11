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
package com.braintribe.ve.impl;

import java.util.function.Consumer;

import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.ve.api.ConfigurableVirtualEnvironment;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.api.VirtualEnvironmentAttribute;

/**
 * A {@link VirtualEnvironment} implementation that delegates access to the {@link VirtualEnvironment} retrieved with {@link VirtualEnvironmentAttribute} 
 * from the {@link AttributeContexts#peek() current AttributeContext}. If there is no such attribute the delegate will be {@link StandardEnvironment#INSTANCE} 
 * @author Dirk Scheffler
 *
 */
public class ContextualizedVirtualEnvironment implements VirtualEnvironment {
	public static VirtualEnvironment INSTANCE = new ContextualizedVirtualEnvironment();
	
	private ContextualizedVirtualEnvironment() {
		
	}

	/**
	 * This methods creates a new VirtualEnvironment that overrides the from {@link #environment() current environment} with an {@link OverridingEnvironment}.
	 * @param configurer A consumer that is called to configure the newly created {@link OverridingEnvironment} through the {@link ConfigurableVirtualEnvironment} interface before it is returned.
	 */
	public static VirtualEnvironment deriveEnvironment(Consumer<ConfigurableVirtualEnvironment> configurer) {
		ConfigurableVirtualEnvironment environment = new OverridingEnvironment(environment());
		configurer.accept(environment);
		return environment();
	}
	
	/**
	 * Returns the environment from the {@link AttributeContexts#peek() current AttributeContext} if present otherwise {@link StandardEnvironment#INSTANCE}
	 */
	public static VirtualEnvironment environment() {
		return AttributeContexts.peek().findAttribute(VirtualEnvironmentAttribute.class).orElse(StandardEnvironment.INSTANCE);
	}

	@Override
	public String getProperty(String name) {
		return environment().getProperty(name);
	}

	@Override
	public String getEnv(String name) {
		return environment().getEnv(name);
	}

}
