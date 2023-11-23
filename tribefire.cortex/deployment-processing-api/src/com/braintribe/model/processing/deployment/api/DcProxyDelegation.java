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

/**
 * Allows access to the actual deployable behind a {@link DcProxy}. Every {@link DcProxy} has an instance of this type which contains the
 * identification of the deployable this proxy is there for (its externalId), and can be configured with the actual deployed instance. This interface
 * offers read-only access on the delegation, while {@link ConfigurableDcProxyDelegation} also offers methods to configure the delegation.
 * <p>
 * <b>Example:</b>
 * 
 * <pre>
 * DcProxyDelegation delegation = DcProxy.getDelegateManager(dcProxyInstance);
 * <b>return</b> delegation.getDeployedUnit();
 * </pre>
 * 
 * @author peter.gazdik
 */
public interface DcProxyDelegation {

	DeployedUnit getDeployedUnit();

	<E> ResolvedComponent<E> getDelegateOptional();

	Object getDelegate() throws DeploymentException;

}