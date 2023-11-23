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
package com.braintribe.transport.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.entity.AbstractHttpEntity;

import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.MemoryThresholdBuffer;

public class MemoryThresholdBufferEntity extends AbstractHttpEntity {

	protected MemoryThresholdBuffer buffer;
	
	public MemoryThresholdBufferEntity(MemoryThresholdBuffer buffer) {
		this.buffer = buffer;
	}
	
	@Override
	public boolean isRepeatable() {
		return buffer.multipleReadsPossible();
	}

	@Override
	public long getContentLength() {
		return this.buffer.getLength();
	}

	@Override
	public InputStream getContent() throws IOException, IllegalStateException {
		return this.buffer.openInputStream(true);
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		try (InputStream is = this.buffer.openInputStream(true)) {
			IOTools.pump(is, outstream);
		}
	}

	@Override
	public boolean isStreaming() {
		return true;
	}

}
