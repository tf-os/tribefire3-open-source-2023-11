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
package tribefire.cortex.testrunner.wire.space;

import java.util.List;

import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.testing.user.UserRelatedTestApi;
import tribefire.cortex.testrunner.TestRunningProcessor;
import tribefire.cortex.testrunner.api.ModuleTestRunner;
import tribefire.cortex.testrunner.api.RunTests;
import tribefire.cortex.testrunner.wire.TestRunningContract;
import tribefire.module.api.WireContractBindingBuilder;
import tribefire.module.wire.contract.HardwiredDeployablesContract;
import tribefire.module.wire.contract.RequestUserRelatedContract;
import tribefire.module.wire.contract.ResourceProcessingContract;
import tribefire.module.wire.contract.SystemUserRelatedContract;
import tribefire.module.wire.contract.TribefireModuleContract;

@Managed
public class TestRunnerModuleSpace implements TribefireModuleContract {

	@Import
	private RequestUserRelatedContract requestUserRelated;

	@Import
	private SystemUserRelatedContract systemUserRelated;

	@Import
	private HardwiredDeployablesContract hardwiredDeployables;

	@Import
	private ResourceProcessingContract resourceProcessing;

	@Import
	private TestRunningSpace testRunning;

	@Override
	public void bindWireContracts(WireContractBindingBuilder bindings) {
		bindings.bind(TestRunningContract.class, testRunning);
	}

	@Override
	public void onBeforeBinding() {
		UserRelatedTestApi.systemSessionFactory = systemUserRelated.sessionFactory();
		UserRelatedTestApi.userSessionFactory = requestUserRelated.sessionFactory();
	}

	@Override
	public void bindHardwired() {
		hardwiredDeployables.bindOnExistingServiceDomain("cortex") //
				.serviceProcessor("test.running.processor", "Test Running Processor", RunTests.T, testRunningProcessor());
	}

	@Managed
	private TestRunningProcessor testRunningProcessor() {
		TestRunningProcessor bean = new TestRunningProcessor();
		bean.setResourceBuilder(resourceProcessing.transientResourceBuilder());

		List<ModuleTestRunner> testRunners = testRunning.registry().getTestRunners();
		bean.setTestRunners(testRunners);

		return bean;
	}

}
