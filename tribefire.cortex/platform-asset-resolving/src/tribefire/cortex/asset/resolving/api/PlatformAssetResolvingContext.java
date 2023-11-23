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
package tribefire.cortex.asset.resolving.api;

import java.util.function.Supplier;

import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.ve.api.VirtualEnvironment;

public interface PlatformAssetResolvingContext extends DependencySelectorContext {
	ManagedGmSession session();

	/**
	 * <p>
	 * Acquires shared information data of a given type. If an instance for that type was already created it is returned
	 * otherwise the <code>supplier</code> is used to create such an instance which is then registered and returned.
	 * 
	 * <p>
	 * Shared information can be used to share data between individual calls to the same or different SOC experts. in
	 * order to aggregate and share information through the whole processing and to support comprehensive operations
	 * that close over the scope of a the single calls.
	 */
	<C> C getSharedInfo(Class<C> key, Supplier<C> supplier);

	/**
	 * This method looks up shared information and returns an instance if existing and otherwise null.
	 * 
	 * @see #getSharedInfo(Class, Supplier)
	 */
	<C> C findSharedInfo(Class<C> key);

	VirtualEnvironment getVirtualEnvironment();
}
