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

import java.io.PrintWriter;

/**
 * Simple Writer that delegates all invocations to the delegate Writer but also counts the number of characters written.
 */
public class CountingPrintWriter extends PrintWriter {

	private PrintWriter delegate;
	private int count;

	public CountingPrintWriter(PrintWriter delegate) {
		super(new NullOutputStream());
		this.delegate = delegate;
		this.count = 0;
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
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	@Override
	public void flush() {
		delegate.flush();
	}

	@Override
	public void close() {
		delegate.close();
	}

	@Override
	public boolean checkError() {
		return delegate.checkError();
	}

	@Override
	public void write(int c) {
		count++;
		delegate.write(c);
	}

	@Override
	public void write(char[] buf, int off, int len) {
		count += len;
		delegate.write(buf, off, len);
	}

	@Override
	public void write(char[] buf) {
		count += buf.length;
		delegate.write(buf);
	}

	@Override
	public void write(String s, int off, int len) {
		count += len;
		delegate.write(s, off, len);
	}

	@Override
	public void println() {
		count++;
		delegate.println();
	}

}
