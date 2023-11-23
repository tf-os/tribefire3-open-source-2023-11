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
package tribefire.platform.wire.space.common;

import java.net.URI;
import java.net.URL;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

@Managed
public class EndpointsSpace implements WireSpace {

	@Managed
	public URL servicesUrl() {
		try {
			String servicesUrl = TribefireRuntime.getServicesUrl();
			URL bean = new URL(servicesUrl);
			return bean;

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to obtain the services URL");
		}
	}

	@Managed
	public URL rpcUrl() {
		URL bean = resolveServicesUrl("rpc");
		return bean;
	}

	@Managed
	public URL streamingUrl() {
		URL bean = resolveServicesUrl("streaming");
		return bean;
	}

	private URL resolveServicesUrl(String path) {
		URL servicesUrl = servicesUrl();
		return resolveUrl(servicesUrl.toString(), path);
	}

	private URL resolveUrl(String base, String path) {
		try {
			return new URI(base + "/" + path).normalize().toURL();
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to resolve " + path + " against " + base);
		}
	}

}
