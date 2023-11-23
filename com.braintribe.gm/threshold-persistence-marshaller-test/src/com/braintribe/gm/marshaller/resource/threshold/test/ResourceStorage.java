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
package com.braintribe.gm.marshaller.resource.threshold.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.model.processing.accessrequest.api.AbstractDispatchingAccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.DispatchConfiguration;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resourceapi.base.ResourceRequest;
import com.braintribe.model.resourceapi.persistence.UploadResource;
import com.braintribe.model.resourceapi.persistence.UploadResourceResponse;
import com.braintribe.model.resourceapi.stream.GetBinaryResponse;
import com.braintribe.model.resourceapi.stream.GetResource;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.CountingOutputStream;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipes;

public class ResourceStorage extends AbstractDispatchingAccessRequestProcessor<ResourceRequest, Object>{
	private Map<String, Map<String, Resource>> resources = new ConcurrentHashMap<>();
	
	@Override
	protected void configureDispatching(DispatchConfiguration dispatching) {
		dispatching.register(UploadResource.T, this::uploadResource);
		dispatching.register(GetResource.T, this::getResource);
	}
	
	private UploadResourceResponse uploadResource(AccessRequestContext<UploadResource> context) {
		
		UploadResource request = context.getOriginalRequest();
		
		Resource transientResource = request.getResource();
		
		StreamPipe pipe = StreamPipes.simpleFactory().newPipe("test");
		
		long size;
		
		try (InputStream in = transientResource.openStream(); CountingOutputStream out = new CountingOutputStream(pipe.acquireOutputStream())) {
			IOTools.transferBytes(in, out, IOTools.BUFFER_SUPPLIER_64K);
			
			size = out.getCount();
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		String uuid = UUID.randomUUID().toString();
		
		Resource resource = Resource.createTransient(pipe::openInputStream);
		resource.setCreated(new Date());
		resource.setName(transientResource.getName());
		resource.setFileSize(size);
		resource.setId(uuid);
		
		
		Map<String, Resource> accessResources = getAccessResources(request);
		accessResources.put(uuid, resource);
		
		UploadResourceResponse uploadResourceResponse = UploadResourceResponse.T.create();
		uploadResourceResponse.setResource(resource);
		
		return uploadResourceResponse;
	}

	private Map<String, Resource> getAccessResources(ResourceRequest request) {
		return resources.computeIfAbsent(request.getDomainId(), id -> new ConcurrentHashMap<>());
	}
	
	private GetBinaryResponse getResource(AccessRequestContext<GetResource> context) {
		GetResource request = context.getOriginalRequest();
		
		Resource resource = request.getResource();
		
		String id = resource.getId();
		
		Map<String, Resource> map = getAccessResources(request);
		
		Resource persistedResource = map.get(id);
		
		if (persistedResource == null)
			throw new NoSuchElementException("Resource with id [" + id + "] does not exist in access " + request.getDomainId());
		
		GetBinaryResponse response = GetBinaryResponse.T.create();
		response.setResource(persistedResource);
		
		return response;
		
	}

}
