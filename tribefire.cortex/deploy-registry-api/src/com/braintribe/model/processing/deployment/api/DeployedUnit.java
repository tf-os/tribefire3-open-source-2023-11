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

import java.util.Map;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;

/**
 * <p>
 * Provides access to bundled components resulted from one deployment operation.
 * 
 * @author dirk.scheffler
 */
public interface DeployedUnit {

	/**
	 * <p>
	 * Returns the component matching the given {@code componentType}.
	 * 
	 * <p>
	 * Unlike {@link #findComponent(EntityType)}, this method throws a {@link DeploymentException} if no component is
	 * found matching the given {@code componentType}.
	 * 
	 * @param componentType
	 *            The {@code componentType} for which a component is to be returned.
	 * @return The bound value of the bundled component within the unit matching the given {@code componentType}.
	 * @throws DeploymentException
	 *             If no component is found matching the given {@code componentType}.
	 */
	<C> C getComponent(EntityType<? extends Deployable> componentType) throws DeploymentException;

	/**
	 * <p>
	 * Returns the component matching the given {@code componentType}.
	 * 
	 * <p>
	 * Unlike {@link #getComponent(EntityType)}, this method returns {@code null} if no component is found matching the
	 * given {@code componentType}.
	 * 
	 * @param componentType
	 *            The {@code componentType} for which a component is to be returned.
	 * @return The bound value of the bundled component within the unit matching the given {@code componentType}.
	 */
	<C> C findComponent(EntityType<? extends Deployable> componentType);
	
	/**
	 * <p>
	 * Returns the component matching the given {@code componentType}.
	 * 
	 * <p>
	 * Unlike {@link #findDeployedComponent(EntityType)}, this method throws a {@link DeploymentException} if no component is
	 * found matching the given {@code componentType}.
	 * 
	 * @param componentType
	 *            The {@code componentType} for which a component is to be returned.
	 * @return The bundled {@link DeployedComponent} within the unit matching the given {@code componentType}.
	 * @throws DeploymentException
	 *             If no component is found matching the given {@code componentType}.
	 */
	DeployedComponent getDeployedComponent(EntityType<? extends Deployable> componentType);
	
	/**
	 * <p>
	 * Returns the component matching the given {@code componentType}.
	 * 
	 * <p>
	 * Unlike {@link #getDeployedComponent(EntityType)}, this method returns {@code null} if no component is found matching the
	 * given {@code componentType}.
	 * 
	 * @param componentType
	 *            The {@code componentType} for which a component is to be returned.
	 * @return The bundled {@link DeployedComponent} within the unit matching the given {@code componentType}.
	 */
	DeployedComponent findDeployedComponent(EntityType<? extends Deployable> componentType);

	/**
	 * <p>
	 * Returns a map of components bundled within this {@link DeployedUnit}, keyed by {@code componentType}.
	 * 
	 * @return A map of components bundled within this {@link DeployedUnit}.
	 */
	Map<EntityType<? extends Deployable>, DeployedComponent> getComponents();

}
