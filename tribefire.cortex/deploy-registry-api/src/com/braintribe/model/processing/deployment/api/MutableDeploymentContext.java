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

import java.util.function.Supplier;

import com.braintribe.model.deployment.Deployable;

/**
 * <p>
 * A {@link DeploymentContext} which enables state change.
 * 
 * @author dirk.scheffler
 *
 * @param <D>
 *            The {@link Deployable} type bound to this context.
 * @param <T>
 *            The expert type bound to this context.
 */
public interface MutableDeploymentContext<D extends Deployable, T> extends DeploymentContext<D, T> {

	void setInstanceToBeBoundSupplier(Supplier<? extends T> instanceToBeBoundSupplier);

	/**
	 * In some cases, the binder might create a different instance than what the configured supplier would return (e.g.
	 * dummy simulation), in which case this method could return <tt>null</tt>.
	 * 
	 * @return instance from the configured supplier, in case it was used for deployment, or <tt>null</tt> otherwise.
	 */
	T getInstanceToBoundIfSupplied();
}
