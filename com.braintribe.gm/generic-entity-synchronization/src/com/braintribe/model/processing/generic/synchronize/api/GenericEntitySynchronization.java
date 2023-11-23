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
package com.braintribe.model.processing.generic.synchronize.api;

import java.util.Collection;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.generic.synchronize.GenericEntitySynchronizationException;
import com.braintribe.model.processing.generic.synchronize.api.builder.BasicIdentityManagerBuilders;
import com.braintribe.model.processing.generic.synchronize.api.builder.SynchronizationResultConvenience;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * The {@link GenericEntitySynchronization} can be used to synchronize (import)
 * given {@link GenericEntity} instances <br />
 * into a given session ({@link PersistenceGmSession}) by respecting provided or
 * pre-configured identity management strategies.
 */
public interface GenericEntitySynchronization {

	/**
	 * Adds the given entity to the collection of entities that should be
	 * synchronized.
	 */
	GenericEntitySynchronization addEntity(GenericEntity entity);

	/**
	 * Adds the given entities to the collection of entities that should be
	 * synchronized.
	 */
	GenericEntitySynchronization addEntities(Collection<? extends GenericEntity> entities);

	/**
	 * Can be called to clear all entities provided before. This is typically
	 * used when the same;@link AbstractSynchronization} <br />
	 * instance is used multiple times.
	 */
	GenericEntitySynchronization clearEntities();

	/**
	 * Sets the target {@link PersistenceGmSession} for the synchronization.
	 */
	GenericEntitySynchronization session(PersistenceGmSession session);

	/**
	 * Adds the given {@link IdentityManager} that should be used during
	 * synchronization.
	 */
	GenericEntitySynchronization addIdentityManager(IdentityManager identityManager);

	/**
	 * Adds the given {@link IdentityManager}'s that should be used during
	 * synchronization.
	 */
	GenericEntitySynchronization addIdentityManagers(Collection<IdentityManager> identityManagers);

	/**
	 * Adds the default identity managers.
	 */
	GenericEntitySynchronization addDefaultIdentityManagers();

	/**
	 * Provides a builder to create and add a new;@link IdentityManager}
	 * fluently.
	 */
	BasicIdentityManagerBuilders<? extends GenericEntitySynchronization> addIdentityManager();

	/**
	 * Tells the synchronization to include id properties during
	 * synchronization.
	 */
	GenericEntitySynchronization includeIdProperties();

	/**
	 * Tells the synchronization to commit the session after successful
	 * synchronization.
	 */
	GenericEntitySynchronization commitAfterSynchronization();

	/**
	 * Synchronizes the given entities based on the given identity management
	 * strategies. <br />
	 * Returns a collection of synchronized (session bound) entities that represents
	 * the initial added entities.
	 */
	SynchronizationResultConvenience synchronize() throws GenericEntitySynchronizationException;

}
