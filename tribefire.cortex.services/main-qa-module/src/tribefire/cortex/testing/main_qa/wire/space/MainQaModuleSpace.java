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
package tribefire.cortex.testing.main_qa.wire.space;

import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.qa.cartridge.main.model.deployment.access.TestInMemoryAccess;
import com.braintribe.qa.cartridge.main.model.deployment.service.TestDataAccessServiceProcessor;
import com.braintribe.qa.cartridge.main.model.deployment.service.TestEchoServiceProcessor;
import com.braintribe.qa.cartridge.main.model.deployment.terminal.TestBasicTemplateBasedApp;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class MainQaModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private MainQaDeployablesSpace deployables;

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		// @formatter:off
		bindings.bind(TestEchoServiceProcessor.T)
				.component(tfPlatform.binders().serviceProcessor())
				.expertSupplier(deployables::testEchoProcessor);
		bindings.bind(TestDataAccessServiceProcessor.T)
				.component(tfPlatform.binders().accessRequestProcessor())
				.expertSupplier(deployables::testDataAccessProcessor);
		bindings.bind(TestInMemoryAccess.T)
				.component(tfPlatform.binders().incrementalAccess())
				.expertFactory(deployables::testInMemoryAccess);
		bindings.bind(TestBasicTemplateBasedApp.T)
				.component(tfPlatform.binders().webTerminal())
				.expertFactory(deployables::testBasicTemplateBasedApp);
		// @formatter:on
	}

}
