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
package tribefire.extension.activemq.wire.space;

import com.braintribe.model.activemqdeployment.ActiveMqWorker;
import com.braintribe.model.activemqdeployment.HealthCheckProcessor;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;

import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;
import tribefire.module.wire.contract.WebPlatformBindersContract;

/**
 * This module's javadoc is yet to be written.
 */
@Managed
public class ActivemqModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;
	
	@Import
	private WebPlatformBindersContract commonComponents; 

	@Import
	private ActivemqDeployablesSpace deployables;
	
	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		// TODO Auto-generated method stub
		TribefireModuleContract.super.onLoaded(configuration);
	}
	
	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		// Bind deployment experts for deployable denotation types.
		// Note that the basic component binders (for e.g. serviceProcessor or incrementalAccess) can be found via tfPlatform.deployment().binders().
		
		bindings.bind(ActiveMqWorker.T)
			.component(commonComponents.worker())
			.expertFactory(deployables::worker);

		bindings.bind(HealthCheckProcessor.T)
			.component(commonComponents.checkProcessor())
			.expertFactory(deployables::healthCheckProcessor);
		
	}

}
