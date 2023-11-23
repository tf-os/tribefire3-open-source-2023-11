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

import java.util.List;
import java.util.function.Supplier;

import com.braintribe.gwt.fileapi.client.FileList;
import com.braintribe.model.processing.session.api.resource.ResourceAccess;
import com.braintribe.model.processing.session.api.resource.ResourceCreateBuilder;
import com.braintribe.model.processing.session.api.resource.ResourceDeleteBuilder;
import com.braintribe.model.processing.session.api.resource.ResourceRetrieveBuilder;
import com.braintribe.model.processing.session.api.resource.ResourceUpdateBuilder;
import com.braintribe.model.processing.session.api.resource.ResourceUrlBuilder;
import com.braintribe.model.resource.Resource;
import com.braintribe.processing.async.api.JsPromise;

@SuppressWarnings("unusable-by-js")
public class RestBasedResourceAccessBuilder implements ResourceAccess {
	protected Supplier<String> accessIdProvider;
	protected String streamBaseUrl;
	protected Supplier<String> sessionIdProvider;
	
	public RestBasedResourceAccessBuilder(Supplier<String> accessIdProvider, String streamBaseUrl, Supplier<String> sessionIdProvider) {
		super();
		this.accessIdProvider = accessIdProvider;
		this.streamBaseUrl = streamBaseUrl;
		this.sessionIdProvider = sessionIdProvider;
	}

	@Override
	public ResourceUrlBuilder url(Resource resource) {
		String accessId = getAccessId();
		return new RestBasedResourceUrlBuilder(accessId, resource, streamBaseUrl, sessionIdProvider);
		
	}	

	@Override
	public ResourceCreateBuilder create() {		
		return new RestBasedResourceCreateBuilder(getAccessId(), streamBaseUrl, sessionIdProvider);
	}

	@Override
	public ResourceRetrieveBuilder retrieve(Resource resource) {
		throw new UnsupportedOperationException(RestBasedResourceAccessBuilder.class.getName() + " does not support the retrieve(Resource) method.");
	}

	@Override
	public ResourceDeleteBuilder delete(Resource resource) {
		throw new UnsupportedOperationException(RestBasedResourceAccessBuilder.class.getName() + " does not support the delete(Resource) method.");
	}
	
	@Override
	public ResourceUpdateBuilder update(Resource resource) {
		return new RestBasedResourceUpdateBuilder(getAccessId(), streamBaseUrl, sessionIdProvider, resource);
	}

	@Override
	public String urlAsString(Resource resource) {
		return url(resource).asString();
	}

	@Override
	public JsPromise<List<Resource>> createFromFiles(FileList files) {
		return create().store(files);
	}

	private String getAccessId() {
		try {
			return accessIdProvider.get();
		} catch (RuntimeException e) {
			throw new RuntimeException("error when retrieving accessId", e);
		}
	}

}
