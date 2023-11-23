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

import com.braintribe.wire.api.space.WireSpace;

/**
 * This contract offers everything that is available to a module from the core tribefire platform.
 * <p>
 * This can be used as the only import for platform contracts inside your space, and all other contracts available for given platform can be
 * discovered from here. Once you find which of the contracts offered here you want to use, you can of course import them directly, like this:
 * 
 * <pre>
 * &#64;Import
 * DeloymentContract deployment
 * </pre>
 * 
 * <p>
 * See {@link TribefireModuleContract} for information about what other contracts are available for given module.
 * 
 * @see TribefireModuleContract
 * @see ModuleReflectionContract
 * @see ModuleResourcesContract
 * 
 * @author peter.gazdik
 */
public interface TribefirePlatformContract extends WireSpace {

	// Please keep the contracts in alphabetical order

	ServiceBindersContract binders();

	DeploymentContract deployment();

	HardwiredDeployablesContract hardwiredDeployables();

	HardwiredExpertsContract hardwiredExperts();

	ModelApiContract modelApi();

	PlatformReflectionContract platformReflection();

	RequestProcessingContract requestProcessing();

	PlatformResourcesContract resources();

	SecurityContract security();

	ThreadingContract threading();

}
