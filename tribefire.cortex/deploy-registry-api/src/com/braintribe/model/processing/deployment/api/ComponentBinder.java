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

import java.util.Arrays;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.DeployableComponent;
import com.braintribe.model.generic.reflection.EntityType;

/**
 * <p>
 * Binds a given component implementation for the matching component type.
 * 
 * @author dirk.scheffler
 *
 * @param <D>
 *            The denotation base type
 * @param <T>
 *            The component implementation type
 * 
 */
public interface ComponentBinder<D extends Deployable, T> {

	/**
	 * This method is responsible for providing the actual expert for given {@link Deployable}. It might use the one
	 * from given {@link MutableDeploymentContext#getInstanceToBeBound() context}, or it might create a completely
	 * independent instance, e.g. if a simulation of given deployable should be used instead.
	 */
	Object bind(MutableDeploymentContext<D, T> context) throws DeploymentException;

	/**
	 * This method is called right after a {@link Deployable} is undeployed, i.e. removed from the {@link DeployRegistry}.
	 */
	default void unbind(@SuppressWarnings("unused") UndeploymentContext<D, T> context) {
		// NOOP
	}

	/**
	 * Returns the component type of this binder which must be a sub type of a {@link Deployable} that is marked with
	 * the {@link DeployableComponent} meta data. Note: the return type must also be super type of D but it cannot
	 * expressed with generics. Avoid the pitfall by extending either {@link DirectComponentBinder} or
	 * {@link IndirectComponentBinder}
	 */
	EntityType<? extends Deployable> componentType();

	/**
	 * @return The interfaces the component implements.
	 */
	Class<?>[] componentInterfaces();

	/**
	 * <p>
	 * Returns a {@link ComponentBinder} which serves only as a standardized source of component type and component
	 * interfaces.
	 * 
	 * @param <D>
	 *            The component type
	 * @param <T>
	 *            The component interfaces
	 * @param componentType
	 *            Determines the component type to be exposed by the plain binder via
	 *            {@link ComponentBinder#componentType()}.
	 * @param componentInterface
	 *            Determines the component interface to be exposed by the plain binder.
	 * @return A plain {@link ComponentBinder} based on the given parameters.
	 */
	static <D extends Deployable, T> ComponentBinder<D, T> plainBinder(EntityType<D> componentType, Class<T> componentInterface,
			Class<?>... additionalComponentInterfaces) {

		Class<T>[] interfaces = new Class[1 + additionalComponentInterfaces.length];
		interfaces[0] = componentInterface;
		System.arraycopy(additionalComponentInterfaces, 0, interfaces, 1, additionalComponentInterfaces.length);
		return new PlainComponentBinder<D, T>(componentType, interfaces);
	}

	default public String stringify() {
		StringBuilder sb = new StringBuilder(getClass().getSimpleName());
		sb.append('[');
		EntityType<? extends Deployable> componentType = componentType();
		sb.append("type: ");
		if (componentType == null) {
			sb.append("null");
		} else {
			sb.append(componentType.getTypeSignature());
		}
		sb.append(", interfaces: ");
		Class<?>[] componentInterfaces = componentInterfaces();
		if (componentInterfaces == null) {
			sb.append("null");
		} else {
			sb.append(Arrays.toString(componentInterfaces));
		}
		sb.append(']');
		return sb.toString();
	}

}
