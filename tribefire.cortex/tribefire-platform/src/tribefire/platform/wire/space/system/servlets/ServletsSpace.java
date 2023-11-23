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
package tribefire.platform.wire.space.system.servlets;

import javax.servlet.ServletContext;

import com.braintribe.util.servlet.remote.RemoteClientAddressResolver;
import com.braintribe.util.servlet.remote.StandardRemoteClientAddressResolver;
import com.braintribe.web.api.WebApps;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.wire.contract.ServletsContract;

@Managed
public class ServletsSpace implements ServletsContract {

	@Override
	public ServletContext context() {
		ServletContext context = WebApps.servletContext();
		return context;
	}

	@Override
	@Managed
	public RemoteClientAddressResolver remoteAddressResolver() {
		StandardRemoteClientAddressResolver resolver = new StandardRemoteClientAddressResolver();
		resolver.setIncludeForwarded(true);
		resolver.setIncludeXForwardedFor(true);
		resolver.setIncludeXRealIp(true);
		resolver.setLenientParsing(true);
		return resolver;
	}

}
