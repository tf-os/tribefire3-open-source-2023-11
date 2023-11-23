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
package com.braintribe.gwt.gmresource.session;

import java.util.function.Supplier;

import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.processing.session.api.persistence.AccessDescriptor;
import com.braintribe.model.processing.session.api.resource.ResourceAccess;
import com.braintribe.model.processing.session.api.resource.ResourceAccessFactory;

public class GwtSessionResourceSupport implements ResourceAccessFactory<AccessDescriptor> {
	protected String streamBaseUrl;
	protected Supplier<String> sessionIdProvider;
	protected boolean accessoryAxis = false;

	@Configurable
	public void setAccessoryAxis(boolean accessoryAxis) {
		this.accessoryAxis = accessoryAxis;
	}

	@Required
	public void setStreamBaseUrl(String streamBaseUrl) {
		this.streamBaseUrl = streamBaseUrl;
	}

	@Required
	public void setSessionIdProvider(Supplier<String> sessionIdProvider) {
		this.sessionIdProvider = sessionIdProvider;
	}

	@Override
	public ResourceAccess newInstance(AccessDescriptor accessInfo) {
		Supplier<String> accessIdProvider = accessoryAxis ? () -> "cortex" : accessInfo::accessId;
		return new RestBasedResourceAccessBuilder(accessIdProvider, streamBaseUrl, sessionIdProvider);
	}

	public ResourceAccess newInstanceForDomainId(String domainId) {
		Supplier<String> domainIdProvider = accessoryAxis ? () -> "cortex" : () -> domainId;
		return new RestBasedResourceAccessBuilder(domainIdProvider, streamBaseUrl, sessionIdProvider);
	}

}
