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
package com.braintribe.web.multipart.streams;

import java.io.IOException;
import java.io.OutputStream;

import com.braintribe.utils.IOTools;

public class InMemoryChunkedOutputStream extends ChunkedOutputStream {
	private byte[] chunkBuffer;
	
	public InMemoryChunkedOutputStream(OutputStream delegate, boolean proprietaryMode) {
		this(delegate, IOTools.SIZE_8K, proprietaryMode);
	}

	public InMemoryChunkedOutputStream(OutputStream delegate, int chunkSize, boolean proprietaryMode) {
		super(delegate, chunkSize, proprietaryMode);

		chunkBuffer = new byte[chunkSize];
	}

	@Override
	protected void addBytesToCurrentChunk(byte[] b, int off, int numOfBytesToWriteInTotal) {
		System.arraycopy(b, off, chunkBuffer, currentChunkSize, numOfBytesToWriteInTotal);
	}
	

	@Override
	protected void writeOutCurrentChunkContent() throws IOException {
		delegate.write(chunkBuffer, 0, currentChunkSize);
	}
}
