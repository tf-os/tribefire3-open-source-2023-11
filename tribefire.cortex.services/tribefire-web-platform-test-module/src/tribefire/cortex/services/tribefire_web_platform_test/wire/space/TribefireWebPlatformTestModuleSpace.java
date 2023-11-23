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
package tribefire.cortex.services.tribefire_web_platform_test.wire.space;

import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.services.tribefire_web_platform_test.tests.PlatformHolder;
import tribefire.cortex.services.tribefire_web_platform_test.tests.accessory.ModelAccessoryFactoryTests;
import tribefire.cortex.services.tribefire_web_platform_test.tests.hardwired.HardwiredBindOnConfigurationModelTests;
import tribefire.cortex.services.tribefire_web_platform_test.tests.hardwired.HardwiredCmdSelectorExpertTests;
import tribefire.cortex.services.tribefire_web_platform_test.tests.hardwired.HardwiredDenotationTransformerTests;
import tribefire.cortex.services.tribefire_web_platform_test.tests.hardwired.HardwiredDeployablesTests;
import tribefire.cortex.testing.junit.wire.space.JUnitTestRunnerModuleSpace;
import tribefire.module.api.InitializerBindingBuilder;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * @see JUnitTestRunnerModuleSpace
 */
@Managed
public class TribefireWebPlatformTestModuleSpace extends JUnitTestRunnerModuleSpace {

	@Import
	public TribefireWebPlatformContract tfPlatform;

	@Override
	public void bindHardwired() {
		HardwiredDeployablesTests.bindHardwired(tfPlatform);
		HardwiredBindOnConfigurationModelTests.bindHardwired(tfPlatform);
		HardwiredCmdSelectorExpertTests.bindHardwired(tfPlatform);
		HardwiredDenotationTransformerTests.bindHardwired(tfPlatform);
	}

	@Override
	public void bindInitializers(InitializerBindingBuilder bindings) {
		ModelAccessoryFactoryTests.bindInitializers(bindings);
		HardwiredCmdSelectorExpertTests.bindInitializers(bindings);
	}

	@Override
	public void onAfterBinding() {
		/* This only works because this module is also the runner module, thus this loaded PlatformHolder is also used when the tests are running */
		PlatformHolder.platformContract = this.tfPlatform;
	}

}
