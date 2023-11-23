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
package com.braintribe.model.processing.generic.synchronize.api.builder;

import java.util.Collection;

import com.braintribe.model.processing.generic.synchronize.api.GenericEntitySynchronization;
import com.braintribe.model.processing.generic.synchronize.experts.GenericIdentityManager;
import com.braintribe.model.query.conditions.Conjunction;

/**
 * Builder interface for {@link GenericIdentityManager}'s 
 */
public interface GenericIdentityManagerBuilder<S extends GenericEntitySynchronization> extends ConfigurableIdentityManagerBuilder<S,GenericIdentityManagerBuilder<S>> {

	/**
	 * Adds a property (name) of the responsible type that should be used to
	 * build the lookup query when searching for existing instances. All
	 * properties added will be combined in a {@link Conjunction} when running
	 * the lookup query.
	 */
	GenericIdentityManagerBuilder<S> addIdentityProperty(String property);

	/**
	 * Same as {@link #addIdentityProperty(String)} but let you specify multiple
	 * properties in one step.
	 */
	GenericIdentityManagerBuilder<S> addIdentityProperties(Collection<String> properties);
	
	/**
	 * Same as {@link #addIdentityProperty(String)} but let you specify multiple
	 * properties in one step.
	 */
	GenericIdentityManagerBuilder<S> addIdentityProperties(String... properties);

}
