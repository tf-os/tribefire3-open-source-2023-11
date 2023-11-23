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
package com.braintribe.model.processing.resource.server.provider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;



public class ResourceStreamingRequestUrlProvider extends StreamingServletRequestBasedProvider implements Supplier<URL> {

	@Override
	public URL get() throws RuntimeException {
		HttpServletRequest httpServletRequest = getStreamingServletRequestInfoProvider().get().getHttpServletRequest();
		try {
			return new URL(httpServletRequest.getRequestURL().toString());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

}
