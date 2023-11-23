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
package com.braintribe.web.impl.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.braintribe.web.api.registry.FilterRegistration;
import com.braintribe.web.api.registry.Registration;
import com.braintribe.web.api.registry.ServletRegistration;
import com.braintribe.web.api.registry.WebRegistry;
import com.braintribe.web.api.registry.WebsocketEndpointRegistration;

public class ConfigurableWebRegistry implements WebRegistry, com.braintribe.web.api.registry.ConfigurableWebRegistry {

	private static final AtomicInteger orderSequence = new AtomicInteger(0);
	protected List<EventListener> listeners;
	protected List<ServletRegistration> servlets;
	protected List<FilterRegistration> filters;
	protected List<WebsocketEndpointRegistration> websocketEndpoints;

	@Override
	public List<EventListener> getListeners() {
		if (listeners == null) {
			return Collections.emptyList();
		}
		return listeners;
	}

	@Override
	public void setListeners(List<EventListener> listeners) {
		this.listeners = listeners;
	}

	@Override
	public List<ServletRegistration> getServlets() {
		if (servlets == null) {
			return Collections.emptyList();
		}
		Collections.<ServletRegistration> sort(servlets);
		return servlets;
	}

	@Override
	public void setServlets(List<ServletRegistration> servlets) {
		if (servlets != null) {
			servlets.forEach(r -> addServlet(r));
		}
	}

	@Override
	public List<FilterRegistration> getFilters() {
		if (filters == null) {
			return Collections.emptyList();
		}
		Collections.<FilterRegistration> sort(filters);
		return filters;
	}

	@Override
	public void setFilters(List<FilterRegistration> filters) {
		if (filters != null) {
			filters.forEach(r -> addFilter(r));
		}
	}

	@Override
	public List<WebsocketEndpointRegistration> getWebsocketEndpoints() {
		if (websocketEndpoints== null) {
			return Collections.emptyList();
		}
		return websocketEndpoints;
	}

	@Override
	public void setWebsocketEndpoints(List<WebsocketEndpointRegistration> websocketEndpoints) {
		this.websocketEndpoints = websocketEndpoints;
	}

	@Override
	public boolean addListener(EventListener eventListener) {
		if (listeners == null) {
			synchronized (this) {
				if (listeners == null) {
					listeners = new ArrayList<>();
				}
			}
		}
		return listeners.add(eventListener);
	}

	@Override
	public boolean addServlet(ServletRegistration registration) {
		if (servlets == null) {
			synchronized (this) {
				if (servlets == null) {
					servlets = new ArrayList<>();
				}
			}
		}
		ensureOrder(registration);
		return servlets.add(registration);
	}

	@Override
	public boolean addFilter(FilterRegistration registration) {
		if (filters == null) {
			synchronized (this) {
				if (filters == null) {
					filters = new ArrayList<>();
				}
			}
		}
		ensureOrder(registration);
		return filters.add(registration);
	}

	@Override
	public boolean addWebsocketEndpoint(WebsocketEndpointRegistration registration) {
		if (websocketEndpoints== null) {
			synchronized (this) {
				if (websocketEndpoints == null) {
					websocketEndpoints = new ArrayList<>();
				}
			}
		}
		return websocketEndpoints.add(registration);
	}

	@Override
	public ConfigurableWebRegistry servlets(ConfigurableServletRegistration... registrations) {
		for (ConfigurableServletRegistration registration : registrations) {
			addServlet(registration);
		}
		return this;
	}

	@Override
	public ConfigurableWebRegistry filters(ConfigurableFilterRegistration... registrations) {
		for (ConfigurableFilterRegistration registration : registrations) {
			addFilter(registration);
		}
		return this;
	}

	@Override
	public ConfigurableWebRegistry websocketEndpoints(ConfigurableWebsocketEndpointRegistration... registrations) {
		for (ConfigurableWebsocketEndpointRegistration registration : registrations) {
			addWebsocketEndpoint(registration);
		}
		return this;
	}

	protected void ensureOrder(Registration registration) {
		if (registration instanceof ConfigurableRegistration) {
			ConfigurableRegistration configurableRegistration = (ConfigurableRegistration) registration;
			if (configurableRegistration.getOrder() == null) {
				configurableRegistration.setOrder(orderSequence.getAndIncrement());
			}
		}
	}

}
