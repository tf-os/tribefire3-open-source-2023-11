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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.braintribe.web.multipart.impl.FormDataMultipartConstants;

public class BufferlessChunkedInputStream extends InputStream implements FormDataMultipartConstants {

	private final Dechunker dechunker;
	private final InputStream delegate;

	private int bytesRemainingInChunk;
	private boolean endOfChunkedStreamReached = false;

	private final byte[] oneByteBuffer = new byte[1];

	public BufferlessChunkedInputStream(InputStream delegate) {
		this.delegate = delegate;
		this.dechunker = new Dechunker(delegate, "\n", "\n");
	}

	@Override
	public int read() throws IOException {
		if (endOfChunkedStreamReached || read(oneByteBuffer) == -1) {
			return -1;
		}

		return oneByteBuffer[0] & 0xff;
	}
	
	private void calculateRemainingBytesOfCurrentChunk() throws IOException {
		if (endOfChunkedStreamReached) {
			return;
		}

		if (bytesRemainingInChunk == 0) {
			bytesRemainingInChunk = dechunker.readChunkSize();
		} else if (bytesRemainingInChunk < 0) {
			throw new IndexOutOfBoundsException("Less than 0 bytes remaining in chunk");
		}
		
		if (bytesRemainingInChunk == 0) {
			endOfChunkedStreamReached = true;
		}
		
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		calculateRemainingBytesOfCurrentChunk();
		
		if (bytesRemainingInChunk == 0) {
			return -1;
		}

		int maxBytesToRead = Math.min(bytesRemainingInChunk, len);
		int bytesRead = delegate.read(b, off, maxBytesToRead);

		if (bytesRead == -1) {
			throw new EOFException("Unexpected EOF when reading from delegate stream");
		}

		bytesRemainingInChunk -= bytesRead;

		return bytesRead;
	}

	@Override
	public int available() throws IOException {
		return bytesRemainingInChunk;
	}
	
	@Override
	public void close() throws IOException {
		super.close();

		endOfChunkedStreamReached = true;
	}

}
