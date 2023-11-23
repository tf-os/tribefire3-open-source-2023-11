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

import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;

/**
 * <p>
 * Offers access to the component interfaces of bound component types.
 * 
 * @author dirk.scheffler
 */
public interface ComponentInterfaceBindings {

	static final Logger log = Logger.getLogger(ComponentInterfaceBindings.class);

	/**
	 * <p>
	 * Returns the component interfaces for the given component type.
	 * 
	 * <p>
	 * {@code null} is returned if no interface is bound to the given type.
	 * 
	 * @param componentType
	 *            The component type for which component interfaces are to be retrieved.
	 * @return The component interfaces for the given component type or {@code null} if no interface is bound to the
	 *         given type.
	 */
	Class<?>[] findComponentInterfaces(EntityType<? extends Deployable> componentType);

	/**
	 * <p>
	 * Returns the component interfaces for the given component type.
	 * 
	 * <p>
	 * {@link DeploymentException} is thrown if no interface is bound to the given type.
	 * 
	 * @param componentType
	 *            The component type for which component interfaces are to be retrieved.
	 * @return The component interfaces for the given component type.
	 * @throws DeploymentException
	 *             If no interface is bound to the given type
	 */
	Class<?>[] getComponentInterfaces(EntityType<? extends Deployable> componentType) throws DeploymentException;
}
