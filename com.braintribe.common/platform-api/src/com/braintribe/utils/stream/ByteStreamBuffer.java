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
package com.braintribe.utils.stream;

import java.io.IOException;
import java.io.InputStream;

import com.braintribe.logging.Logger;
import com.braintribe.utils.lcd.IOTools;

public class ByteStreamBuffer extends InputStream {

	private static final Logger logger = Logger.getLogger(ByteStreamBuffer.class);

	protected MemoryThresholdBuffer buffer = null;
	protected InputStream in = null;

	public ByteStreamBuffer(InputStream outerIn, int bufferSize, boolean closeInputStream) throws IOException {
		buffer = new MemoryThresholdBuffer(bufferSize);
		try {
			IOTools.pump(outerIn, buffer);
			in = new AutoCloseInputStream(buffer.openInputStream(true));
		} finally {
			if (closeInputStream && outerIn != null) {
				com.braintribe.utils.IOTools.closeCloseable(outerIn, logger);
			}
		}
	}

	@Override
	public int hashCode() {
		return in.hashCode();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return in.read(b);
	}

	@Override
	public boolean equals(Object obj) {
		return in.equals(obj);
	}

	@Override
	public int read() throws IOException {
		return in.read();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return in.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		return in.skip(n);
	}

	@Override
	public int available() throws IOException {
		return in.available();
	}

	@Override
	public boolean markSupported() {
		return in.markSupported();
	}

	@Override
	public void mark(int readAheadLimit) {
		in.mark(readAheadLimit);
	}

	@Override
	public void reset() throws IOException {
		in.reset();
	}

	@Override
	public void close() throws IOException {
		in.close();
	}

}
