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
package tribefire.extension.simple.wire.space;

import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.simple.model.deployment.access.SimpleInMemoryAccess;
import tribefire.extension.simple.model.deployment.service.SimpleEchoService;
import tribefire.extension.simple.model.deployment.terminal.SimpleWebTerminal;
import tribefire.module.api.InitializerBindingBuilder;
import tribefire.module.api.WireContractBindingBuilder;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * This module's javadoc is yet to be written.
 */
@Managed
public class SimpleModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private SimpleDeployablesSpace deployables;

	@Override
	public void bindWireContracts(WireContractBindingBuilder bindings) {
		// Bind wire contracts to make them available for other modules.
		// Note that the Contract class cannot be defined in this module, but must be in a gm-api artifact.
	}

	//
	// Hardwired deployables
	//

	@Override
	public void bindHardwired() {
		// Bind hardwired deployables here.
	}

	//
	// Initializers
	//

	@Override
	public void bindInitializers(InitializerBindingBuilder bindings) {
		// Bind DataInitialiers for various CollaborativeAcceses
	}

	//
	// Deployables
	//

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		// Bind deployment experts for deployable denotation types.
		// @formatter:off
		bindings.bind(SimpleInMemoryAccess.T)
			.component(tfPlatform.binders().incrementalAccess())
			.expertFactory(deployables::simpleInMemoryAccess);

		bindings.bind(SimpleEchoService.T)
			.component(tfPlatform.binders().accessRequestProcessor())
			.expertFactory(deployables::simpleEchoServiceProcessor);

		bindings.bind(SimpleWebTerminal.T)
			.component(tfPlatform.binders().webTerminal())
			.expertFactory(deployables::simpleWebTerminal);
		// @formatter:on
	}
}
