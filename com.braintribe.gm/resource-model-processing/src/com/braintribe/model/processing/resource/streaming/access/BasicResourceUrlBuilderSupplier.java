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
package com.braintribe.model.processing.resource.streaming.access;

import java.net.URL;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.resource.Resource;

public class BasicResourceUrlBuilderSupplier implements Function<Resource, BasicResourceUrlBuilder> {

	protected URL baseStreamingUrl;
	protected Supplier<String> sessionIdProvider;
	protected String responseMimeType;

	@Required
	@Configurable
	public void setBaseStreamingUrl(URL baseStreamingUrl) {
		this.baseStreamingUrl = baseStreamingUrl;
	}

	@Required
	@Configurable
	public void setSessionIdProvider(Supplier<String> sessionIdProvider) {
		this.sessionIdProvider = sessionIdProvider;
	}

	@Configurable
	public void setResponseMimeType(String responseMimeType) {
		this.responseMimeType = responseMimeType;
	}

	@Override
	public BasicResourceUrlBuilder apply(Resource resource) {

		BasicResourceUrlBuilder urlBuilder = new BasicResourceUrlBuilder();
		urlBuilder.setBaseStreamingUrl(baseStreamingUrl);
		urlBuilder.setResponseMimeType(responseMimeType);
		urlBuilder.setSessionId(sessionIdProvider.get());
		urlBuilder.setResource(resource);

		return urlBuilder;

	}

}
