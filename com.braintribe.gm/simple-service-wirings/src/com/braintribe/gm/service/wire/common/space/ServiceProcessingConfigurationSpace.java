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
package com.braintribe.gm.service.wire.common.space;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.braintribe.gm.service.wire.common.contract.ServiceProcessingConfigurationContract;
import com.braintribe.model.processing.securityservice.commons.service.InMemorySecurityServiceProcessor;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class ServiceProcessingConfigurationSpace implements ServiceProcessingConfigurationContract {

	@Override
	public void registerSecurityConfigurer(Consumer<InMemorySecurityServiceProcessor> configurer) {
		securityConfigurers().add(configurer);
	}
	
	@Override
	public void registerServiceConfigurer(Consumer<ConfigurableDispatchingServiceProcessor> configurer) {
		serviceConfigurers().add(configurer);
	}
	
	@Managed
	public List<Consumer<ConfigurableDispatchingServiceProcessor>> serviceConfigurers() {
		return new ArrayList<>();
	}
	
	@Managed
	public List<Consumer<InMemorySecurityServiceProcessor>> securityConfigurers() {
		return new ArrayList<>();
	}

	
}
