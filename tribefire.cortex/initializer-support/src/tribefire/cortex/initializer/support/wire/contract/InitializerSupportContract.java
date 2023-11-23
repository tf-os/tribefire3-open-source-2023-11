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
package tribefire.cortex.initializer.support.wire.contract;

import com.braintribe.model.deployment.Module;
import com.braintribe.model.descriptive.HasExternalId;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.wire.api.space.WireSpace;

/**
 * This contract provides convenience methods that can be used to create GE instances and link them with existing external GE instances. <br>
 * 
 * Note: provided functionality is meant to be used by initializer spaces.
 * 
 */
public interface InitializerSupportContract extends WireSpace {

	/**
	 * Returns {@link ManagedGmSession session}.
	 */
	ManagedGmSession session();

	/**
	 * Uses {@link ManagedGmSession session} to create requested GE instance.
	 */
	<T extends GenericEntity> T create(EntityType<T> entityType);

	/**
	 * Uses {@link ManagedGmSession session} to lookup existing GE instances by global id.
	 */
	<T extends GenericEntity> T lookup(String globalId);

	/**
	 * @return the {@link Module} instance corresponding to the current initializer module if called when initializing the cortex access.
	 * 
	 * @throws UnsupportedOperationException
	 *             if invoked while initializing an access other than cortex.
	 */
	Module currentModule();

	/**
	 * Uses {@link ManagedGmSession session} to lookup existing GE instances by the external ID.
	 */
	<T extends HasExternalId> T lookupExternalId(String externalId);

	/**
	 * Returns initializer id.
	 */
	String initializerId();

	/**
	 * If entities are not {@link #create(EntityType) created} on a session, they have to be imported to ensure proper globalId management. <br/>
	 * QueryBuilders and CriterionBuilders are examples which require imports.
	 */
	<T> T importEntities(T entities);

}
