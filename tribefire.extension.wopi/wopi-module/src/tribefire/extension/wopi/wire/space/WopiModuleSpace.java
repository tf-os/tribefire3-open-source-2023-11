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
package tribefire.extension.wopi.wire.space;

import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.model.wopi.connector.WopiWacConnector;
import com.braintribe.model.wopi.service.CleanupWopiSessionWorker;
import com.braintribe.model.wopi.service.ExpireWopiSessionWorker;
import com.braintribe.model.wopi.service.WacHealthCheckProcessor;
import com.braintribe.model.wopi.service.WopiApp;
import com.braintribe.model.wopi.service.WopiIntegrationExample;
import com.braintribe.model.wopi.service.WopiServiceProcessor;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * This module's javadoc is yet to be written.
 */
@Managed
public class WopiModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private DeployablesSpace deployables;

	//
	// Deployables
	//

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		//@formatter:off
		bindings.bind(WopiServiceProcessor.T)
			.component(tfPlatform.binders().accessRequestProcessor())
			.expertFactory(deployables::wopiServiceProcessor);
		
		bindings.bind(CleanupWopiSessionWorker.T)
			.component(tfPlatform.binders().worker())
			.expertFactory(deployables::cleanupWopiSessionWorker);
		
		bindings.bind(ExpireWopiSessionWorker.T)
			.component(tfPlatform.binders().worker())
			.expertFactory(deployables::expireWopiSessionWorker);		
		
		bindings.bind(WopiApp.T)
			.component(tfPlatform.binders().webTerminal())
			.expertFactory(deployables::wopiApp);
		
		bindings.bind(WopiIntegrationExample.T)
			.component(tfPlatform.binders().webTerminal())
			.expertFactory(deployables::wopiIntegrationExample);		
		
		bindings.bind(WopiWacConnector.T)
			.component(WopiWacConnector.T, com.braintribe.model.processing.wopi.WopiWacConnector.class)
			.expertFactory(deployables::wopiWacConnector);	
		
		bindings.bind(WacHealthCheckProcessor.T)
			.component(tfPlatform.binders().checkProcessor())
			.expertFactory(deployables::wacHealthCheckProcessor);		
		//@formatter:on
	}

}
