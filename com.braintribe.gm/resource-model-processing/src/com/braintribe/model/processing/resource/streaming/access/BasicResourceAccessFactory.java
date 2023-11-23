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

import java.util.function.Function;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.resource.ResourceAccess;
import com.braintribe.model.processing.session.api.resource.ResourceAccessFactory;
import com.braintribe.model.processing.session.api.resource.ResourceUrlBuilder;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.utils.stream.api.StreamPipeFactory;

/**
 * <p>
 * A {@link ResourceAccessFactory} of {@link BasicResourceAccess} instances.
 * 
 */
public class BasicResourceAccessFactory implements ResourceAccessFactory<PersistenceGmSession> {

	protected Function<Resource, ? extends ResourceUrlBuilder> urlBuilderSupplier;
	private StreamPipeFactory streamPipeFactory;
	private boolean shallowifyRequestResource = true;

	@Configurable
	public void setUrlBuilderSupplier(Function<Resource, ? extends ResourceUrlBuilder> urlBuilderSupplier) {
		this.urlBuilderSupplier = urlBuilderSupplier;
	}

	@Configurable
	@Required
	public void setStreamPipeFactory(StreamPipeFactory streamPipeFactory) {
		this.streamPipeFactory = streamPipeFactory;
	}

	/**
	 * Usually you don't want to send all property values of the {@link Resource} and its {@link ResourceSource} when
	 * communicating with the binary processors except when you want to create or update them. In these cases the
	 * resource access can automatically clone the resource and set all its properties (except its id) to absent before
	 * passing it on to the binary processor. That's enough for it to identify the resource and stream or delete it.
	 * <p>
	 * This is set to <tt>true</tt> per default and needs to be explicitly disabled.
	 */
	@Configurable
	public void setShallowifyRequestResource(boolean shallowifyRequestResource) {
		this.shallowifyRequestResource = shallowifyRequestResource;
	}

	@Override
	public ResourceAccess newInstance(PersistenceGmSession session) {
		BasicResourceAccess resourceAccess = new BasicResourceAccess(session);
		resourceAccess.setUrlBuilderSupplier(urlBuilderSupplier);
		resourceAccess.setStreamPipeFactory(streamPipeFactory);
		resourceAccess.setShallowifyRequestResource(shallowifyRequestResource);
		return resourceAccess;
	}

}
