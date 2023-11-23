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
package tribefire.cortex.module.loading.api;

import java.util.function.Consumer;

import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.module.loading.ModuleLoader;

/**
 * This registry is given to a Tribefire platform to bind all the contracts relevant for that platform.
 * <p>
 * With current implementation, the platform passes a {@link Consumer} of this interface to the {@link ModuleLoader}, and this consumer is the expert
 * that knows how to bind all the platform's contracts.
 * 
 * @author peter.gazdik
 */
public interface PlatformContractsRegistry {

	/**
	 * Binds a space to given contract.
	 * 
	 * @see #bindAllContractsOf(WireSpace)
	 */
	<T extends WireSpace> void bind(Class<T> contractClass, T space);

	/**
	 * Binds all the contracts implemented by given space to the this space instance. This is relevant if some space implements multiple contracts,
	 * which are either independent, or form a type hierarchy.
	 * 
	 * <b>Example:</b> Let's say we have the following contracts:
	 * 
	 * <pre>
	 * ResourcesContract extends WireSpace
	 * ConnectionsContract extends WireSpace
	 * TribefirePlatformContract extends WireSpace
	 * TribefireCloudPlatformContract extends TribefirePlatformContract
	 * </pre>
	 * 
	 * And we have a single space that implements all of them:
	 * 
	 * <pre>
	 * TribefireCloudPlatformSpace implements TribefireCloudPlatformContract, ResourcesContract, ConnectionsContract
	 * </pre>
	 * 
	 * In order to get this effect:
	 * 
	 * <pre>
	 * TribefireCloudPlatformSpace space = ...;
	 * 
	 * bind(ResourcesContract.class, space);
	 * bind(ConnectionsContract.class, space);
	 * bind(TribefirePlatformContract.class, space);
	 * bind(TribefireCloudPlatformContract.class, space);
	 * </pre>
	 * 
	 * We can just do:
	 * 
	 * <pre>
	 * TribefireCloudPlatformSpace space = ...;
	 * 
	 * bindAllContractsOf(space);
	 * </pre>
	 * 
	 * Note that this method could be used even for spaces which implement just one contract, but binding them explicitly with
	 * {@link #bind(Class, WireSpace)} is probably more readable / natural.
	 * 
	 */
	void bindAllContractsOf(WireSpace space);

}
