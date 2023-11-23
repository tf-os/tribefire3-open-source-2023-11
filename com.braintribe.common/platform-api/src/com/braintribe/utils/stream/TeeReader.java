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
import java.io.Reader;

import com.braintribe.utils.LimitedStringBuilder;

/**
 * Extension of the Reader class that delegates all calls to a provided Reader object. In addition, it records the data read from the Reader in an
 * internal buffer (which can be limited by size) for later re-use.
 */
public class TeeReader extends Reader {

	private Reader delegate = null;
	private LimitedStringBuilder buffer;

	public TeeReader(Reader delegate) {
		this.delegate = delegate;
		this.buffer = new LimitedStringBuilder(Integer.MAX_VALUE);
	}
	public TeeReader(Reader delegate, int maxSize) {
		this.delegate = delegate;
		this.buffer = new LimitedStringBuilder(maxSize);
	}

	public String getBuffer() {
		return this.buffer.toString();
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public int read() throws IOException {
		int read = delegate.read();
		if (read > 0) {
			this.buffer.append((char) read);
		}
		return read;
	}

	@Override
	public int read(char[] cbuf) throws IOException {
		int read = delegate.read(cbuf);
		if (read > 0) {
			for (int i = 0; i < read; ++i) {
				this.buffer.append(cbuf[i]);
			}
		}
		return read;
	}

	@Override
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int read = delegate.read(cbuf, off, len);
		if (read > 0) {
			for (int i = off; i < (off + read); ++i) {
				this.buffer.append(cbuf[i]);
			}
		}
		return read;
	}

	@Override
	public long skip(long n) throws IOException {
		return delegate.skip(n);
	}

	@Override
	public boolean ready() throws IOException {
		return delegate.ready();
	}

	@Override
	public boolean markSupported() {
		return delegate.markSupported();
	}

	@Override
	public void mark(int readAheadLimit) throws IOException {
		delegate.mark(readAheadLimit);
	}

	@Override
	public void reset() throws IOException {
		delegate.reset();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

}
