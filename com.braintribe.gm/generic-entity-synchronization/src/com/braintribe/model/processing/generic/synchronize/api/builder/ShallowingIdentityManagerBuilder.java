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
package com.braintribe.model.processing.generic.synchronize.api.builder;

import com.braintribe.model.processing.generic.synchronize.EntityNotFoundInSessionException;
import com.braintribe.model.processing.generic.synchronize.api.GenericEntitySynchronization;
import com.braintribe.model.processing.generic.synchronize.experts.ShallowingIdentityManager;

/**
 * Base builder for builders providing a {@link ShallowingIdentityManager}
 */
public interface ShallowingIdentityManagerBuilder<S extends GenericEntitySynchronization, B extends ShallowingIdentityManagerBuilder<S, B>>
		extends QueryingIdentiyManagerBuilder<S,B> {

	/**
	 * If explicitly set the {@link ShallowingIdentityManager} implementation
	 * will throw an {@link EntityNotFoundInSessionException} in case the
	 * expected entity can't be found in session during synchronization.
	 * Otherwise a shallow instance will be created.
	 */
	B requiredInSession();

}
