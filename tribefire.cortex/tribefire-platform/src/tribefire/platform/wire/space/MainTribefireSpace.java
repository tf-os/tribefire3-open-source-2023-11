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
package tribefire.platform.wire.space;

import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.utils.lcd.StopWatch;
import com.braintribe.web.api.registry.WebRegistry;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.platform.wire.contract.MainTribefireContract;
import tribefire.platform.wire.space.cortex.deployment.DeploymentSpace;
import tribefire.platform.wire.space.messaging.MulticastSpace;
import tribefire.platform.wire.space.module.ModuleInitializationSpace;
import tribefire.platform.wire.space.system.PreLoadingSpace;
import tribefire.platform.wire.space.system.SystemTasksSpace;
import tribefire.platform.wire.space.system.servlets.WebRegistrySpace;

@Managed
public class MainTribefireSpace implements MainTribefireContract {

	private static final Logger logger = Logger.getLogger(MainTribefireSpace.class);

	// @formatter:off
	@Import	private PreLoadingSpace preLoading; // important, has an onLoaded method, should be triggered ASAP
	@Import	private DeploymentSpace deployment;
	@Import	private ModuleInitializationSpace moduleInitialization;
	@Import	private MulticastSpace multicast;
	@Import	private SystemTasksSpace systemTasks;
	@Import	private WebRegistrySpace webRegistry;
	// @formatter:on

	@Override
	public void activate() {
		StopWatch stopWatch = new StopWatch();

		moduleInitialization.loadModules();
		stopWatch.intermediate("Load Modules");

		initDeployRegistry();
		stopWatch.intermediate("Init Deploy Registry");

		// system threads
		systemTasks.startTasks();
		stopWatch.intermediate("Start Tasks");

		// activation
		deployment.activation().activate();
		stopWatch.intermediate("Activation");

		logger.trace(() -> "onLoaded: " + stopWatch);
	}

	private void initDeployRegistry() {
		deployment.hardwiredBindings().deploy(deployment.registry());
	}

	@Override
	public WebRegistry webRegistry() {
		try {
			return webRegistry.webRegistry();
		} catch (RuntimeException e) {
			throw Exceptions.contextualize(e, "Failed to create the web registry");
		}
	}

}
