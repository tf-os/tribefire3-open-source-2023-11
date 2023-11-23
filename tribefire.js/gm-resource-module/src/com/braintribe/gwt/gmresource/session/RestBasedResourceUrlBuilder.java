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

import com.braintribe.gwt.genericmodel.client.resource.GwtInputStreamProvider;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.resource.ResourceUrlBuilder;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resource.source.TransientSource;
import com.google.gwt.http.client.URL;

public class RestBasedResourceUrlBuilder implements ResourceUrlBuilder {
	private Resource resource;
	private String streamBaseUrl;
	private String accessId;
	private Supplier<String> sessionIdProvider;
	private boolean download;
	
	public RestBasedResourceUrlBuilder(String accessId, Resource resource, String streamBaseUrl, Supplier<String> sessionIdProvider) {
		super();
		this.accessId = accessId;
		this.resource = resource;
		this.streamBaseUrl = streamBaseUrl;
		this.sessionIdProvider = sessionIdProvider;
	}
	
	@Override
	public String asString() {
		try {
			ResourceSource source = resource.getResourceSource();
			if(source instanceof TransientSource) {
				TransientSource ts = (TransientSource)source;
				if(ts.getInputStreamProvider() instanceof GwtInputStreamProvider) {
					GwtInputStreamProvider inputStreamProvider = (GwtInputStreamProvider)ts.getInputStreamProvider();
					return inputStreamProvider.url();
				}				
				return "";
			}else {				
				StringBuilder sb = new StringBuilder(streamBaseUrl);
				
				sb.append("download?");
				
				String domainId = accessId;				
				if(resource.session() instanceof PersistenceGmSession) {
					PersistenceGmSession session = (PersistenceGmSession)resource.session();
					domainId = session.getAccessId() != null && !session.getAccessId().equals("") ? session.getAccessId() : accessId;
				}		
				
				sb.append("domainId=" + domainId + "&");
				sb.append("sessionId=" + sessionIdProvider.get() + "&");
				sb.append("resource.id=%27" + URL.encodeQueryString(resource.getId()) + "%27&");
				if(resource.getPartition() != null && !resource.getPartition().equals(""))
					sb.append("resource.partition=" + URL.encodeQueryString(resource.getPartition()) + "&");
				sb.append("downloadResource=true");	
				if(download)
					sb.append("&saveLocally=true");
				
				return sb.toString();
			}
		} catch (RuntimeException e) {
			throw new RuntimeException("error while retrieving session id", e);
		}
	}
	
	@Override
	public ResourceUrlBuilder download(boolean download) {
		this.download = download;
		return this;
	}

	@Override
	public ResourceUrlBuilder fileName(String fileName) {
		return this;
	}

	@Override
	public ResourceUrlBuilder accessId(String accessId) {
		this.accessId = accessId;
		return this;
	}
	
	@Override
	public ResourceUrlBuilder sessionId(String sessionId) {
		return this;
	}

	@Override
	public ResourceUrlBuilder sourceType(String sourceTypeSignature) {
		return this;
	}

	@Override
	public ResourceUrlBuilder useCase(String useCase) {
		return this;
	}

	@Override
	public ResourceUrlBuilder base(String baseUrl) {
		this.streamBaseUrl = baseUrl;
		return this;
	}

	@Override
	public ResourceUrlBuilder mimeType(String mimeType) {
		return this;
	}

	@Override
	public ResourceUrlBuilder md5(String md5) {
		return this;
	}

	@Override
	public ResourceUrlBuilder specification(String specification) {
		return this;
	}

	@Override
	public ResourceUrlBuilder tags(String tags) {
		return this;
	}

}
