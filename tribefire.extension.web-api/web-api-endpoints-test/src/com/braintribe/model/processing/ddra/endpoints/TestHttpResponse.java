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
package com.braintribe.model.processing.ddra.endpoints;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.util.EntityUtils;

import com.braintribe.codec.marshaller.api.EntityVisitorOption;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.processing.ddra.endpoints.ioc.TestMarshallerRegistry;
import com.braintribe.model.processing.rpc.commons.api.RpcConstants;
import com.braintribe.model.resource.source.TransientSource;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipes;
import com.braintribe.web.multipart.api.PartReader;
import com.braintribe.web.multipart.api.SequentialFormDataReader;
import com.braintribe.web.multipart.impl.Multiparts;

public class TestHttpResponse {

	private final org.apache.http.HttpResponse response;

	public TestHttpResponse(org.apache.http.HttpResponse response) {
		this.response = response;
	}

	public org.apache.http.HttpResponse getResponse() {
		return response;
	}

	public int getStatusCode() {
		return response.getStatusLine().getStatusCode();
	}

	public <T> T getContent() {
		try {
			Header mimeTypeHeader = response.getFirstHeader("Content-Type");
			if (mimeTypeHeader == null) {
				throw new RuntimeException("No Content-Type defined.");
			}
			
			String mimeType = mimeTypeHeader.getValue();
			int idx = mimeType.indexOf(';');
			if (idx > 0) {
				mimeType = mimeType.substring(0,idx);
			}
			
			if("text/html".equalsIgnoreCase(mimeType) || "text/plain".equalsIgnoreCase(mimeType)){
				return null;
			}
			
			if ("multipart/form-data".equals(mimeType)) {
				Object result = null;
				try (SequentialFormDataReader formDataReader = Multiparts.buildFormDataReader(response.getEntity().getContent()).contentType(mimeTypeHeader.getValue()).autoCloseInput().sequential()) {
					Map<String, StreamPipe> resourcePipesByGlobalId = new HashMap<>();
					Set<TransientSource> transientSources = new HashSet<>();
					
					PartReader reader = null;
					
					while ((reader = formDataReader.next()) != null) {
						String partName = reader.getName();
						if (partName.equals(RpcConstants.RPC_MAPKEY_RESPONSE)) {
							String contentType = reader.getContentType();
							Marshaller marshaller = TestMarshallerRegistry.getMarshallerRegistry().getMarshaller(contentType);
							if (marshaller == null) {
								throw new RuntimeException("Unsupported mimeType: " + contentType);
							}
							
							try (InputStream in = reader.openStream()) {
								result = marshaller.unmarshall(in, GmDeserializationOptions.defaultOptions.derive().set(EntityVisitorOption.class, e -> {
									if (e instanceof TransientSource) {
										transientSources.add((TransientSource) e);
									}
								}).build());
							}
						}
						else {
							String globalId = partName;
							StreamPipe pipe = StreamPipes.simpleFactory().newPipe("test");
							try (OutputStream out = pipe.acquireOutputStream(); InputStream in = reader.openStream()) {
								IOTools.transferBytes(in, out);
							}
							resourcePipesByGlobalId.put(globalId, pipe);
						}
					}
					
					for (TransientSource source: transientSources) {
						String globalId = source.getGlobalId();
						StreamPipe pipe = resourcePipesByGlobalId.get(globalId);
						
						if (pipe != null) {
							source.setInputStreamProvider(pipe::openInputStream);
						}
						else {
							throw new IllegalStateException("Unbound TransientSource");
						}
					}
					
				} catch (Exception e) {
					throw Exceptions.unchecked(e);
				}
				
				return (T) result;
			}
			else {
				
				InputStream content = response.getEntity().getContent();
				Marshaller marshaller = TestMarshallerRegistry.getMarshallerRegistry().getMarshaller(mimeType);
				if (marshaller == null) {
					throw new RuntimeException("Unsupported mimeType: " + mimeType);
				}
				T result = (T) marshaller.unmarshall(content);
				content.close();
	
				return result;
			}
		} catch (UnsupportedOperationException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void consumeEntity() {
		EntityUtils.consumeQuietly(response.getEntity());
	}
}
