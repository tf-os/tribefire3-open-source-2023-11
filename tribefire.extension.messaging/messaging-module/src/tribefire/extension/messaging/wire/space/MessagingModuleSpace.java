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
package tribefire.extension.messaging.wire.space;

import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.messaging.model.deployment.service.HealthCheckProcessor;
import tribefire.extension.messaging.model.deployment.service.MessagingAspect;
import tribefire.extension.messaging.model.deployment.service.MessagingProcessor;
import tribefire.extension.messaging.model.deployment.service.MessagingWorker;
import tribefire.extension.messaging.model.deployment.service.test.TestGetObjectProcessor;
import tribefire.extension.messaging.model.deployment.service.test.TestReceiveMessagingProcessor;
import tribefire.extension.messaging.model.deployment.service.test.TestUpdateObjectProcessor;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;
import tribefire.module.wire.contract.WebPlatformBindersContract;

@Managed
public class MessagingModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private WebPlatformBindersContract commonComponents;

	@Import
	private DeployablesSpace deployables;

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		//@formatter:off
		//----------------------------
		// SERVICE
		//----------------------------
		bindings.bind(MessagingProcessor.T)
			.component(tfPlatform.binders().serviceProcessor())
			.expertFactory(deployables::messagingProcessor);
		//TODO HERE SHOULD BIND A POST-PROCESSOR FROM CONFIG ??? (NO Task is allocated as the question targets message consumption which is not in priority as of 22.08.2022)
		//----------------------------
		// ASPECT
		//----------------------------
		bindings.bind(MessagingAspect.T)
			.component(tfPlatform.binders().serviceProcessor())
			.expertFactory(deployables::messagingAspect)
			.component(tfPlatform.binders().serviceAroundProcessor())
			.expertFactory(deployables::messagingAspect);

		//----------------------------
		// WORKER
		//----------------------------

		bindings.bind(MessagingWorker.T)
			.component(commonComponents.worker())
			.expertFactory(deployables::messagingWorker);

		//----------------------------
		// TEST
		//----------------------------
		bindings.bind(TestReceiveMessagingProcessor.T)
			.component(tfPlatform.binders().serviceProcessor())
			.expertFactory(deployables::testReceiveMessagingProcessor);

		bindings.bind(TestUpdateObjectProcessor.T)
				.component(tfPlatform.binders().serviceProcessor())
				.expertFactory(deployables::testUpdateObjectProcessor);

		bindings.bind(TestGetObjectProcessor.T)
				.component(tfPlatform.binders().serviceProcessor())
				.expertFactory(deployables::testGetObjectProcessor);

		//----------------------------
		// HEALTH
		//----------------------------
		bindings.bind(HealthCheckProcessor.T)
			.component(tfPlatform.binders().checkProcessor())
			.expertFactory(deployables::healthCheckProcessor);
		//@formatter:on
	}

}
