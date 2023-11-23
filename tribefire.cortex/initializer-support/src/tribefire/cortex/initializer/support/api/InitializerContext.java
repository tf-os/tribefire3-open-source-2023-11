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
package tribefire.cortex.initializer.support.api;

import com.braintribe.model.deployment.Module;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

/**
 * This interface provides initializer (ModulePriming) assets with convenience methods that can
 * be used to link existing external GE instances with GE instances created inside
 * cartridge initializer spaces. <br>
 * 
 * Note: provided functionality is meant to be used by cartridge initializer objects.
 * 
 */
public interface InitializerContext {

	/**
	 * Returns {@link ManagedGmSession session}.
	 */
	ManagedGmSession session();

	/**
	 * Uses {@link ManagedGmSession session} to lookup existing instances by global id.
	 */
	<T extends GenericEntity> T lookup(String globalId);

	/** Just like {@link #lookup(String)}, but throws an exception if given entity cannot be found. */
	<T extends GenericEntity> T require(String globalId);

	/** @return the {@link Module} instance corresponding to the current initializer module. */
	Module currentModule();
}
