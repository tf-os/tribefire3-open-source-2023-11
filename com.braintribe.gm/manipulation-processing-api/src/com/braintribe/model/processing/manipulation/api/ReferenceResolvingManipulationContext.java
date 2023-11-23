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
package com.braintribe.model.processing.manipulation.api;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;

/**
 * An extension of {@link ManipulationExpositionContext} which is able to resolve references and therefore can provide
 * the actual instances this manipulation .
 */
public interface ReferenceResolvingManipulationContext extends ManipulationExpositionContext {

	/**
	 * @return instance corresponding to given {@link Manipulation} if the underlying reference has type
	 *         {@link PersistentEntityReference}, or <tt>null</tt> in other cases.
	 */
	<T extends GenericEntity> T getTargetInstance();

	/**
	 * @return in case of a {@link PropertyManipulation}, this returns the current value of the manipulated property.
	 *         The result value is something like {@code  getTargetProperty().getProperty(getTargetInstance())} (using
	 *         the {@link #getTargetProperty()} and {@link #getTargetInstance()} methods).
	 */
	<T> T getTargetPropertyValue();

	/**
	 * @return entity corresponding to given reference if it is a {@link PersistentEntityReference}, or <tt>null</tt>
	 *         otherwise. This may be e.g. used for resolving the entities referenced as values of various
	 *         {@link PropertyManipulation}s.
	 */
	GenericEntity resolveReference(EntityReference reference) throws ManipulationContextException;

	/**
	 * If the current manipulation is a {@link ChangeValueManipulation} which changes the <tt>id</tt> property of the
	 * owner, then the reference will be returned which will reference the owner from now on. In other case,
	 * <tt>null</tt> is returned. Also, the method returns the normalized reference, so any later call of
	 * {@link #getNormalizedTargetReference()} for manipulations using the same owner will return the same instance as
	 * this method..
	 */
	EntityReference getNormalizedNewReference();

}
