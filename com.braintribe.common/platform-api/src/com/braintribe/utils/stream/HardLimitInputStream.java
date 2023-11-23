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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Inputstream wrapper that throws an IndexOutOfBoundsException when a specific threshold of bytes read is exceeded.
 */
public class HardLimitInputStream extends InputStream {

	protected InputStream delegate = null;
	protected long count = 0;
	protected long mark = -1;
	private long threshold;

	public HardLimitInputStream(InputStream delegate, boolean wrapBuffer, long threshold) {
		if (threshold <= 0) {
			throw new IllegalArgumentException("The threshold must be a positive number.");
		}
		this.threshold = threshold;
		if (wrapBuffer) {
			this.delegate = new BufferedInputStream(delegate);
		} else {
			this.delegate = delegate;
		}
	}

	public long getCount() {
		return this.count;
	}

	@Override
	public int read() throws IOException {
		int result = this.delegate.read();
		if (result != -1) {
			this.count++;
			checkCount();
		}
		return result;
	}

	@Override
	public int read(byte[] b) throws IOException {
		int result = this.delegate.read(b);
		if (result != -1) {
			this.count += result;
			checkCount();
		}
		return result;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int result = this.delegate.read(b, off, len);
		if (result != -1) {
			this.count += result;
			checkCount();
		}
		return result;
	}

	@Override
	public long skip(long n) throws IOException {
		long result = this.delegate.skip(n);
		this.count += result;
		checkCount();
		return result;
	}

	private void checkCount() {
		if (count > threshold) {
			throw new IndexOutOfBoundsException("The threshold of " + threshold + " bytes has been reached or exceeded (" + count + ").");
		}
	}

	@Override
	public void mark(int readlimit) {
		this.delegate.mark(readlimit);
		mark = this.count;
	}

	@Override
	public void reset() throws IOException {
		if (!this.delegate.markSupported()) {
			throw new IOException("Mark not supported");
		}
		if (mark == -1) {
			throw new IOException("Mark not set");
		}

		this.delegate.reset();
		this.count = mark;
	}

	@Override
	public int available() throws IOException {
		return this.delegate.available();
	}

	@Override
	public void close() throws IOException {
		this.delegate.close();
	}

	@Override
	public boolean markSupported() {
		return this.delegate.markSupported();
	}

}
