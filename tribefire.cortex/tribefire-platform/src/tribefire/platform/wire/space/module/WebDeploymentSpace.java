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
package tribefire.platform.wire.space.module;

import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.deployment.api.DeployedComponentResolver;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.wire.contract.DeploymentContract;
import tribefire.platform.wire.space.cortex.deployment.DeploymentSpace;

/**
 * @author peter.gazdik
 */
@Managed
public class WebDeploymentSpace implements DeploymentContract {

	@Import
	private DeploymentSpace deployment;

	@Override
	public DeployRegistry deployRegistry() {
		return deployment.registry();
	}

	@Override
	public DeployedComponentResolver deployedComponentResolver() {
		return deployment.proxyingDeployedComponentResolver();
	}

	@Override
	public void runWhenSystemIsDeployed(Runnable r) {
		deployment.systemDeploymentListenerRegistry().runWhenSystemIsDeployed(r);
	}

}
