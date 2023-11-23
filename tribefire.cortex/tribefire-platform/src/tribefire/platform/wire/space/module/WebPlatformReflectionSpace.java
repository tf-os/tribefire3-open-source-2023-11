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

import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.api.EnvironmentDenotations;
import tribefire.module.wire.contract.WebPlatformReflectionContract;
import tribefire.platform.wire.space.common.CartridgeInformationSpace;
import tribefire.platform.wire.space.common.EnvironmentSpace;

/**
 * @author peter.gazdik
 */
@Managed
public class WebPlatformReflectionSpace implements WebPlatformReflectionContract {

	@Import
	private CartridgeInformationSpace cartridgeInformation;

	@Import
	private EnvironmentSpace environment;

	@Override
	public InstanceId instanceId() {
		return cartridgeInformation.instanceId();
	}

	@Override
	public String globalId() {
		throw new UnsupportedOperationException("Method 'WebPlatformReflectionSpace.globalId' is not implemented yet!");
	}

	@Managed
	@Override
	public String nodeId() {
		return cartridgeInformation.nodeId();
	}

	@Override
	public String getProperty(String propertyName) {
		return TribefireRuntime.getProperty(propertyName);
	}

	@Override
	public EnvironmentDenotations environmentDenotations() {
		return environment.environmentDenotations();
	}

}
