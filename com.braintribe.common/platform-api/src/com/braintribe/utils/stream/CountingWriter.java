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
import java.io.Writer;

/**
 * Simple Writer that delegates all invocations to the delegate Writer but also counts the number of characters written.
 */
public class CountingWriter extends Writer {

	private Writer delegate;
	private int count;

	public CountingWriter(Writer delegate) {
		super();
		this.delegate = delegate;
	}

	/**
	 * Returns the number of characters written since the initialization or the last call to {@link #resetCount()}.
	 *
	 * @return The number of characters.
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Resets the internal counter to 0.
	 */
	public void resetCount() {
		count = 0;
	}

	@Override
	public void write(int c) throws IOException {
		count++;
		delegate.write(c);
	}

	@Override
	public void write(char[] cbuf) throws IOException {
		count += cbuf.length;
		delegate.write(cbuf);
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		count += len;
		delegate.write(cbuf, off, len);
	}

	@Override
	public void write(String str) throws IOException {
		count += str.length();
		delegate.write(str);
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		count += len;
		delegate.write(str, off, len);
	}

	@Override
	public Writer append(CharSequence csq) throws IOException {
		count += csq.length();
		return delegate.append(csq);
	}

	@Override
	public Writer append(CharSequence csq, int start, int end) throws IOException {
		count += end - start;
		return delegate.append(csq, start, end);
	}

	@Override
	public Writer append(char c) throws IOException {
		count++;
		return delegate.append(c);
	}

	@Override
	public void flush() throws IOException {
		delegate.flush();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}
}
