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

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;

/**
 * This interface simplifies the {@link ComponentBinder} because most of the binder implementations
 * make no actual difference between the componentType and the base denotation type.
 * @author dirk.scheffler
 *
 * @param <D>
 *                 denotation base type and in the same moment component type
 * @param <T>
 *                 type of the instance to be bound
 */
public interface DirectComponentBinder<D extends Deployable, T> extends ComponentBinder<D, T> {

	@Override
	EntityType<D> componentType();
}
