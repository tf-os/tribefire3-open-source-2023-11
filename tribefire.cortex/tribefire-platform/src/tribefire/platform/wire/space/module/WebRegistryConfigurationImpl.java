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

import com.braintribe.web.api.registry.FilterConfiguration;

import tribefire.module.api.WebRegistryConfiguration;
import tribefire.platform.impl.module.DelegatingConfigurableWebRegistry;
import tribefire.platform.wire.space.system.servlets.WebRegistrySpace;

public class WebRegistryConfigurationImpl extends DelegatingConfigurableWebRegistry implements WebRegistryConfiguration {

	private final WebRegistrySpace webRegistry;

	public WebRegistryConfigurationImpl(WebRegistrySpace webRegistry) {
		super(webRegistry.moduleWebRegistry());
		this.webRegistry = webRegistry;
	}
	
	@Override
	public FilterConfiguration lenientAuthFilter() {
		return webRegistry.lenientAuthFilterConfiguration();
	}
	
	@Override
	public FilterConfiguration loginRedirectingAuthFilter() {
		return webRegistry.strictAuthFilterConfiguration();
	}
	
	@Override
	public FilterConfiguration compressionFilter() {
		return webRegistry.compressionFilter();
	}
	
	@Override
	public FilterConfiguration threadRenamingFilter() {
		return webRegistry.threadRenamingFilter();
	}

	@Override
	public FilterConfiguration strictAuthFilter() {
		return webRegistry.strictAuthFilterConfiguration();
	}

}
