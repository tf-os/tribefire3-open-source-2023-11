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
package com.braintribe.model.processing.email.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import com.braintribe.common.lcd.NotSupportedException;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;

public class ResourceDataSource implements DataSource {

	private Resource resource;

	public ResourceDataSource(StreamPipeFactory pipeStreamFactory, Resource resource) {
		StreamPipe pipe = pipeStreamFactory.newPipe("attachment-" + resource.getName());
		try (OutputStream os = pipe.acquireOutputStream(); InputStream in = resource.openStream()) {
			IOTools.transferBytes(in, os);

			this.resource = Resource.createTransient(pipe::openInputStream);
			this.resource.setName(resource.getName());
			this.resource.setMimeType(resource.getMimeType());
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not access attachment resource.");
		}
	}
	public ResourceDataSource(Resource resource) {
		this.resource = resource;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new BufferedInputStream(resource.openStream());
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		throw new NotSupportedException("Saving Resources is not supported here!");
	}

	@Override
	public String getContentType() {
		return resource.getMimeType();
	}

	@Override
	public String getName() {
		return resource.getName();
	}

}
