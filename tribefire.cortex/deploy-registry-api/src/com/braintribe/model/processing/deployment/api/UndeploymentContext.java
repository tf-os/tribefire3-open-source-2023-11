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

/**
 * A container class holding data used when undeploying a {@link Deployable}.
 */
public interface UndeploymentContext<D extends Deployable, T> {

	D getDeployable();

	/**
	 * The
	 */
	T getBoundInstance();

	/**
	 * @return instance from the configured supplier, or <tt>null</tt> if supplier wasn't used.
	 * 
	 * @see MutableDeploymentContext#getInstanceToBoundIfSupplied()
	 */
	T getSuppliedInstance();

}
