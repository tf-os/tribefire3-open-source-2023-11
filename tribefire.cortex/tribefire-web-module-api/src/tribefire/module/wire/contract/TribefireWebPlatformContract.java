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
package tribefire.module.wire.contract;

/**
 * This contract binds everything that is available to a module from the core tribefire platform.
 * <p>
 * This can be used as the only import within the module, but of course any of the underlying contracts can be imported directly.
 * 
 * @see TribefireModuleContract
 * 
 * @author peter.gazdik
 */
public interface TribefireWebPlatformContract extends TribefirePlatformContract {

	// Please keep the contracts in alphabetical order !!!
	@Override
	WebPlatformBindersContract binders();

	@Override
	WebPlatformHardwiredDeployablesContract hardwiredDeployables();

	@Override
	WebPlatformHardwiredExpertsContract hardwiredExperts();

	@Override
	WebPlatformReflectionContract platformReflection();

	@Override
	WebPlatformResourcesContract resources();

	ClusterContract cluster();

	CryptoContract crypto();

	HttpContract http();

	MasterUserAuthContextContract masterUserAuthContext();

	WebPlatformMarshallingContract marshalling();

	MessagingContract messaging();

	RequestUserRelatedContract requestUserRelated();

	ResourceProcessingContract resourceProcessing();

	ServletsContract servlets();

	SystemToolsContract systemTools();

	SystemUserRelatedContract systemUserRelated();

	TopologyContract topology();

	TribefireConnectionsContract tribefireConnections();

	WorkerContract worker();

}
