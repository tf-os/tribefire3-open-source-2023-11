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

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.provider.Holder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.testing.processing.extensions.TestBasicTemplateBasedApp;
import tribefire.cortex.testing.processing.extensions.TestInMemoryAccess;
import tribefire.cortex.testing.processing.services.TestDataAccessServiceProcessor;
import tribefire.cortex.testing.processing.services.TestEchoServiceProcessor;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed  
public class MainQaDeployablesSpace implements WireSpace {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Managed
	public TestInMemoryAccess testInMemoryAccess(ExpertContext<com.braintribe.qa.cartridge.main.model.deployment.access.TestInMemoryAccess> context) {

		TestInMemoryAccess bean = new TestInMemoryAccess();

		com.braintribe.qa.cartridge.main.model.deployment.access.TestInMemoryAccess deployable = context.getDeployable();

		Holder<GmMetaModel> metaModelProvider = new Holder<>(deployable.getMetaModel());

		bean.setMetaModelProvider(metaModelProvider);
		bean.setAccessId(deployable.getExternalId());
		return bean;
	}

	@Managed
	public TestBasicTemplateBasedApp testBasicTemplateBasedApp(ExpertContext<com.braintribe.qa.cartridge.main.model.deployment.terminal.TestBasicTemplateBasedApp> context) {
		
		TestBasicTemplateBasedApp bean = new TestBasicTemplateBasedApp();
		
		com.braintribe.qa.cartridge.main.model.deployment.terminal.TestBasicTemplateBasedApp deployable = context.getDeployable();
		bean.setTimestamp(deployable.getTimestamp());
		return bean;
	}

	@Managed
	public TestEchoServiceProcessor testEchoProcessor() {
		TestEchoServiceProcessor bean = new TestEchoServiceProcessor();
		return bean;
	}
	
	@Managed
	public TestDataAccessServiceProcessor testDataAccessProcessor() {
		TestDataAccessServiceProcessor bean = new TestDataAccessServiceProcessor();
		return bean;
	}
	
}
