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

/**
 * By simply holding an explicit reference to a resource, you make sure it won't get garbage collected as long as your InputStream is functional.
 *
 * @author Neidhart.Orlich
 *
 */
public class ReferencingDelegateInputStream extends InputStream {

	@SuppressWarnings("unused")
	private Object reference;
	private final InputStream delegate;
	private final boolean dropReferenceOnClose;

	/**
	 * @param reference
	 *            By simply holding an explicit reference to a resource, you make sure it won't get garbage collected as long as your InputStream is
	 *            functional.
	 * @param delegate
	 *            All operations are delegated to this actual {@link InputStream} implementation
	 * @param dropReferenceOnClose
	 *            On <tt>true</tt> the reference is dropped already when {@link #close()} is called, otherwise the reference lives as long as this
	 *            instance.
	 */
	public ReferencingDelegateInputStream(Object reference, InputStream delegate, boolean dropReferenceOnClose) {
		this.reference = reference;
		this.delegate = delegate;
		this.dropReferenceOnClose = dropReferenceOnClose;
	}

	/**
	 * @param reference
	 *            The object will keep being referenced until {@link #close()} is called.
	 * @param delegate
	 *            All operations are delegated to this actual {@link InputStream} implementation
	 */
	public ReferencingDelegateInputStream(Object reference, InputStream delegate) {
		this(reference, delegate, true);
	}

	@Override
	public int read() throws IOException {
		return delegate.read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return delegate.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return delegate.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		return delegate.skip(n);
	}

	@Override
	public int available() throws IOException {
		return delegate.available();
	}

	@Override
	public void close() throws IOException {
		delegate.close();

		if (dropReferenceOnClose) {
			reference = null;
		}
	}

	@Override
	public void mark(int readlimit) {
		delegate.mark(readlimit);
	}

	@Override
	public void reset() throws IOException {
		delegate.reset();
	}

	@Override
	public boolean markSupported() {
		return delegate.markSupported();
	}

}
