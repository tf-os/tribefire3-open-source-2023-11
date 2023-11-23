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

import java.util.Set;
import java.util.function.Function;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;

/**
 * <p>
 * Offers access to the {@link DeployedUnit} of bound denotation types and the {@link ComponentBinding} of bound
 * component types.
 * 
 * @author dirk.scheffler
 */
public interface DenotationTypeBindings extends ComponentInterfaceBindings {

	/**
	 * <p>
	 * Resolves a {@link DeployedUnit} supplier can be resolved for the given {@link Deployable}.
	 * 
	 * @param deployable
	 *            The {@link Deployable} for which a {@link DeployedUnit} supplier must be resolved.
	 * @return A {@link DeployedUnit} supplier can be resolved for the given {@link Deployable}.
	 * @throws DeploymentException
	 *             If no DeployedUnit supplier can be resolved for the given {@link Deployable}.
	 */
	Function<MutableDeploymentContext<?, ?>, DeployedUnit> resolveDeployedUnitSupplier(Deployable deployable) throws DeploymentException;

	ComponentBinding findComponentProxyBinding(EntityType<? extends Deployable> componentType);

	Set<EntityType<? extends Deployable>> boundTypes();

}
