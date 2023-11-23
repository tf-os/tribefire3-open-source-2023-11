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

import java.util.EventListener;
import java.util.List;

import com.braintribe.web.impl.registry.ConfigurableFilterRegistration;
import com.braintribe.web.impl.registry.ConfigurableServletRegistration;
import com.braintribe.web.impl.registry.ConfigurableWebsocketEndpointRegistration;

public interface ConfigurableWebRegistry {

	void setListeners(List<EventListener> listeners);

	void setServlets(List<ServletRegistration> servlets);

	void setFilters(List<FilterRegistration> filters);

	void setWebsocketEndpoints(List<WebsocketEndpointRegistration> websocketEndpoints);

	boolean addListener(EventListener eventListener);

	boolean addServlet(ServletRegistration registration);

	boolean addFilter(FilterRegistration registration);

	boolean addWebsocketEndpoint(WebsocketEndpointRegistration registration);

	ConfigurableWebRegistry servlets(ConfigurableServletRegistration... registrations);

	ConfigurableWebRegistry filters(ConfigurableFilterRegistration... registrations);

	ConfigurableWebRegistry websocketEndpoints(ConfigurableWebsocketEndpointRegistration... registrations);

}