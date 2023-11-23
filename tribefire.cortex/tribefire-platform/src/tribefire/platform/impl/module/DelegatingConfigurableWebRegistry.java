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
package tribefire.platform.impl.module;

import java.util.EventListener;
import java.util.List;

import com.braintribe.web.api.registry.ConfigurableWebRegistry;
import com.braintribe.web.api.registry.FilterRegistration;
import com.braintribe.web.api.registry.ServletRegistration;
import com.braintribe.web.api.registry.WebsocketEndpointRegistration;
import com.braintribe.web.impl.registry.ConfigurableFilterRegistration;
import com.braintribe.web.impl.registry.ConfigurableServletRegistration;
import com.braintribe.web.impl.registry.ConfigurableWebsocketEndpointRegistration;

public class DelegatingConfigurableWebRegistry implements ConfigurableWebRegistry {
	private final ConfigurableWebRegistry delegate;
	
	public DelegatingConfigurableWebRegistry(ConfigurableWebRegistry delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public void setListeners(List<EventListener> listeners) {
		delegate.setListeners(listeners);
	}

	@Override
	public void setServlets(List<ServletRegistration> servlets) {
		delegate.setServlets(servlets);
	}

	@Override
	public void setFilters(List<FilterRegistration> filters) {
		delegate.setFilters(filters);
	}

	@Override
	public void setWebsocketEndpoints(List<WebsocketEndpointRegistration> websocketEndpoints) {
		delegate.setWebsocketEndpoints(websocketEndpoints);
	}

	@Override
	public boolean addListener(EventListener eventListener) {
		return delegate.addListener(eventListener);
	}

	@Override
	public boolean addServlet(ServletRegistration registration) {
		return delegate.addServlet(registration);
	}

	@Override
	public boolean addFilter(FilterRegistration registration) {
		return delegate.addFilter(registration);
	}

	@Override
	public boolean addWebsocketEndpoint(WebsocketEndpointRegistration registration) {
		return delegate.addWebsocketEndpoint(registration);
	}

	@Override
	public ConfigurableWebRegistry servlets(ConfigurableServletRegistration... registrations) {
		return delegate.servlets(registrations);
	}

	@Override
	public ConfigurableWebRegistry filters(ConfigurableFilterRegistration... registrations) {
		return delegate.filters(registrations);
	}

	@Override
	public ConfigurableWebRegistry websocketEndpoints(ConfigurableWebsocketEndpointRegistration... registrations) {
		return delegate.websocketEndpoints(registrations);
	}
}
