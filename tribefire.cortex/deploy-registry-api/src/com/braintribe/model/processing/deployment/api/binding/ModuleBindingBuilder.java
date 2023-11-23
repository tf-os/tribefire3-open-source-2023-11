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

/**
 * First step in the {@link DenotationBindingBuilder} chain when binding deployables in a module. This is {@link #bindForModule(String) first step} is
 * done by the module loader, and the resulting builder is then given to actual module, and all the bound deployables are associated with their module
 * of origin.
 * 
 * @see DenotationBindingBuilder
 */
public interface ModuleBindingBuilder {

	/**
	 * Starts a fluent builder for a module given by it's globalId. This binds a number of experts to a {@link Deployable} denotation
	 * {@link EntityType type}.
	 */
	DenotationBindingBuilder bindForModule(String moduleGlobalId);

}
