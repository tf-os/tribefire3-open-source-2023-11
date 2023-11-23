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
package com.braintribe.model.processing.generic.synchronize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.generic.synchronize.api.IdentityManager;
import com.braintribe.model.processing.generic.synchronize.api.SynchronizationContext;

/**
 * Registry that holds {@link IdentityManager}'s and identifies responsible
 * managers for passed entities.
 */
public class IdentityManagerRegistry {

	private static Logger logger = Logger.getLogger(IdentityManagerRegistry.class);

	private List<IdentityManager> identityManagers = new ArrayList<IdentityManager>();

	/**
	 * Default constructor
	 */
	public IdentityManagerRegistry() {}

	/**
	 * Adds a new {@link IdentityManager} to the registry.
	 */
	public void addIdentityManager(IdentityManager identityManager) {
		this.identityManagers.add(identityManager);
	}

	/**
	 * Same as {@link #addIdentityManager(IdentityManager)} but for multiple
	 * managers.
	 */
	public void addIdentityManagers(Collection<IdentityManager> identityManagers) {
		this.identityManagers.addAll(identityManagers);
	}

	/**
	 * Tries to find the first responsible {@link IdentityManager} for given
	 * entity. <br />
	 * Returns null in case no registered {@link IdentityManager} returns
	 * responsibility for given entity.
	 */
	public IdentityManager findIdentityManager(GenericEntity instance, EntityType<? extends GenericEntity> entityType,
			SynchronizationContext context) {

		for (IdentityManager identityManager : identityManagers) {
			if (identityManager.isResponsible(instance, entityType, context)) {
				return identityManager;
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("No responsible identity manager found for entity type: " + entityType.getTypeSignature());
		}
		return null;
	}

}
