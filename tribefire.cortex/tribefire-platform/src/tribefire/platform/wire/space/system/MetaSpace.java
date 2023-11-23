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
package tribefire.platform.wire.space.system;

import com.braintribe.cartridge.common.processing.deployment.DeploymentScope;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.impl.deployment.DeploymentScopeOriginManager;

@Managed
public class MetaSpace implements WireSpace {

	@Import
	private DeploymentScope deploymentScope;
	
	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		// add listener that registers BeanHolder origins for reflective purposes of the DeploymentComponent
		configuration.addLifecycleListener(deploymentScopeOriginManager());
	}

	@Managed
	public DeploymentScopeOriginManager deploymentScopeOriginManager() {
		DeploymentScopeOriginManager bean = new DeploymentScopeOriginManager();
		bean.setScope(deploymentScope);
		return bean;
	}

}
