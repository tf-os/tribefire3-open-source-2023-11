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
package com.braintribe.processing.http.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.entity.AbstractHttpEntity;

import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resourceapi.stream.GetBinaryResponse;
import com.braintribe.model.resourceapi.stream.GetResource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.IOTools;

public class ResourceHttpEntity extends AbstractHttpEntity {

	private final Resource resource;
	private Evaluator<ServiceRequest> evaluator;

	public ResourceHttpEntity(Resource resource, Evaluator<ServiceRequest> evaluator) {
		this.resource = resource;
		this.evaluator = evaluator;
	}

	@Override
	public boolean isRepeatable() {
		return true;
	}

	@Override
	public long getContentLength() {
		Long fileSize = resource.getFileSize();
		if (fileSize == null || fileSize <= 0) {
			return -1;
		}
		return fileSize;
	}

	@Override
	public InputStream getContent() throws IOException, UnsupportedOperationException {
		final Resource transientResource;

		if (!resource.isTransient()) {
			GetResource gr = GetResource.T.create();
			gr.setDomainId(resource.getPartition());
			gr.setResource(resource);
			GetBinaryResponse response = gr.eval(evaluator).get();
			transientResource = response.getResource();
		} else {
			transientResource = resource;
		}
		return transientResource.openStream();
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		try (InputStream in = getContent()) {
			IOTools.transferBytes(in, outstream);
		}
	}

	@Override
	public boolean isStreaming() {
		return true;
	}

	@Override
	public String toString() {
		return "Resource Stream: " + resource.getName() + " (" + resource.getFileSize() + ")";
	}
}
