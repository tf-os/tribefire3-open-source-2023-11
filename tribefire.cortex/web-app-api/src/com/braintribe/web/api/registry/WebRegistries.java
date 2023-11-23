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
package com.braintribe.web.api.registry;

import com.braintribe.web.impl.registry.ConfigurableFilterRegistration;
import com.braintribe.web.impl.registry.ConfigurableMultipartConfig;
import com.braintribe.web.impl.registry.ConfigurableServletNamesFilterMapping;
import com.braintribe.web.impl.registry.ConfigurableServletRegistration;
import com.braintribe.web.impl.registry.ConfigurableUrlPatternFilterMapping;
import com.braintribe.web.impl.registry.ConfigurableWebRegistry;
import com.braintribe.web.impl.registry.ConfigurableWebsocketEndpointRegistration;

/**
 * <p>
 * This class consists exclusively of static methods for building {@link WebRegistry} instances and related objects.
 * 
 */
public interface WebRegistries {

	static ConfigurableWebRegistry config() {
		return new ConfigurableWebRegistry();
	}

	static ConfigurableServletRegistration servlet() {
		return new ConfigurableServletRegistration();
	}

	static ConfigurableFilterRegistration filter() {
		return new ConfigurableFilterRegistration();
	}

	static ConfigurableWebsocketEndpointRegistration websocketEndpoint() {
		return new ConfigurableWebsocketEndpointRegistration();
	}

	static ConfigurableUrlPatternFilterMapping urlMapping() {
		return new ConfigurableUrlPatternFilterMapping();
	}

	static ConfigurableServletNamesFilterMapping nameMapping() {
		return new ConfigurableServletNamesFilterMapping();
	}

	static ConfigurableMultipartConfig multipartConfig() {
		return new ConfigurableMultipartConfig();
	}

}
