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

import javax.websocket.Endpoint;

import com.braintribe.cfg.Required;
import com.braintribe.web.api.registry.WebsocketEndpointRegistration;

public class ConfigurableWebsocketEndpointRegistration extends ConfigurableRegistration implements WebsocketEndpointRegistration {

	protected String path;
	protected Endpoint endpoint;
	
	@Required
	public void setPath(String path) {
		this.path = path;
	}
	
	@Required
	public void setEndpoint(Endpoint endpoint) {
		this.endpoint = endpoint;
	}

	@Override
	public String getPath() {
		return path;
	}
	
	@Override
	public Endpoint getEndpoint() {
		return endpoint;
	}
	
	
	/* builder methods */

	public ConfigurableWebsocketEndpointRegistration path(String path) {
		setPath(path);
		return this;
	}

	public ConfigurableWebsocketEndpointRegistration instance(Endpoint instance) {
		setEndpoint(instance);
		return this;
	}
}
