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

import java.util.Optional;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;

/**
 * <p>
 * Offers access to the deployed components during deployment-time.
 * 
 * @author dirk.scheffler
 */
public interface DeployedComponentResolver {

	/**
	 * Equivalent to {@code resolve(deployable.getExternalId, componentType)}
	 * 
	 * @see #resolve(String, EntityType)
	 */
	<E> E resolve(Deployable deployable, EntityType<? extends Deployable> componentType);

	/**
	 * Resolves an expert that was deployed for given {@link Deployable#getExternalId() Deployable's externalId} as given deployable component.
	 */
	<E> E resolve(String externalId, EntityType<? extends Deployable> componentType);

	<E> E resolve(Deployable deployable, EntityType<? extends Deployable> componentType, Class<E> expertInterface, E defaultDelegate);

	<E> E resolve(String externalId, EntityType<? extends Deployable> componentType, Class<E> expertInterface, E defaultDelegate);

	<E> Optional<ResolvedComponent<E>> resolveOptional(String externalId, EntityType<? extends Deployable> componentType, Class<E> expertInterface);
}
