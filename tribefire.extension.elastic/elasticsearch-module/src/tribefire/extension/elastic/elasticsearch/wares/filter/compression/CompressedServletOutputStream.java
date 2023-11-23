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
/*
 *
 *  Copyright 2011 Rajendra Patil
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package tribefire.extension.elastic.elasticsearch.wares.filter.compression;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

public class CompressedServletOutputStream extends ServletOutputStream {

	private final OutputStream uncompressedStream;
	private CompressedOutput compressed;
	private final EncodedStreamsFactory encodedStreamsFactory;
	private final CompressedHttpServletResponseWrapper compressedResponseWrapper;
	protected ByteArrayOutputStream buffer = null;
	private boolean useBuffer = true;
	private boolean closed;
	private boolean cancelled;
	private int maxSize;

	CompressedServletOutputStream(OutputStream uncompressedStream, EncodedStreamsFactory encodedStreamsFactory,
			CompressedHttpServletResponseWrapper compressedResponseWrapper, int threshold) {
		this.uncompressedStream = uncompressedStream;
		this.encodedStreamsFactory = encodedStreamsFactory;
		this.compressedResponseWrapper = compressedResponseWrapper;
		closed = false;
		cancelled = false;
		maxSize = threshold;
	}

	private OutputStream getCompressed() throws IOException {
		if (useBuffer || cancelled) {
			return uncompressedStream;
		}
		if (compressed == null) {
			compressed = encodedStreamsFactory.getCompressedStream(uncompressedStream);
			// we are switching to compression here, write compression headers
			compressedResponseWrapper.useCompression();
		}
		return compressed.getCompressedOutputStream();
	}

	private void flushBufferToStream(OutputStream outputStream) throws IOException {
		if (buffer != null) {
			buffer.writeTo(outputStream);
			buffer.flush();
			buffer = null;
			useBuffer = false;
		}
	}

	private boolean canBuffer(int length) throws IOException {
		if (!useBuffer) {
			return useBuffer;
		}

		if (length > maxSize) {
			useBuffer = false;
			getCompressed();
		} else {
			if (buffer == null) {
				buffer = new ByteArrayOutputStream(maxSize);
			}
			useBuffer = (buffer.size() + length) <= maxSize;

		}
		return useBuffer;
	}

	@Override
	public void write(byte[] b) throws IOException {
		assertOpen();
		if (canBuffer(b.length)) {
			buffer.write(b);
		} else {
			flushBufferToStream(getCompressed());
			getCompressed().write(b);
		}
	}

	@Override
	public void write(byte[] b, int offset, int length) throws IOException {
		assertOpen();
		if (canBuffer(length)) {
			buffer.write(b, offset, length);
		} else {
			flushBufferToStream(getCompressed());
			getCompressed().write(b, offset, length);
		}
	}

	@Override
	public void write(int b) throws IOException {
		assertOpen();
		if (canBuffer(1)) {
			buffer.write(b);
		} else {
			flushBufferToStream(getCompressed());
			getCompressed().write(b);
		}
	}

	private void assertOpen() throws IOException {
		if (closed) {
			throw new IOException("Stream has been already closed");
		}
	}

	void reset() {
		if (useBuffer && buffer != null) {
			buffer.reset();
		}
	}

	@Override
	public void flush() throws IOException {
		// do not flush buffer
	}

	@Override
	public void close() throws IOException {
		if (!closed) {
			compressedResponseWrapper.flushBuffer();
			closed = true;
			if (useBuffer || cancelled) { // mean we wrote everything to buffer so far or compressed was cancelled
				// We did not use compressed stream (content less than threshold)
				flushBufferToStream(uncompressedStream);
				compressedResponseWrapper.noCompression();
				uncompressedStream.close();
			} else {// we are not using buffer, means content is more than threshold
				compressedResponseWrapper.useCompression();
				OutputStream outputStream = compressed.getCompressedOutputStream();
				flushBufferToStream(outputStream);
				outputStream.flush();
				compressed.finish();
				outputStream.close();
			}

		}
	}

	boolean isClosed() {
		return closed;
	}

	void cancelCompression() throws IOException {
		if (useBuffer) {
			flushBufferToStream(uncompressedStream);
		}
		cancelled = true;
	}

	boolean isCancelled() {
		return cancelled;
	}
}
