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

public abstract class DelegateInputStream extends InputStream {
	private InputStream delegate;

	protected abstract InputStream openDelegate() throws IOException;

	protected InputStream getDelegate() throws IOException {
		if (delegate == null) {
			delegate = openDelegate();
		}

		return delegate;
	}

	@Override
	public int read() throws IOException {
		return getDelegate().read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return getDelegate().read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return getDelegate().read(b, off, len);
	}
	@Override
	public long skip(long n) throws IOException {
		return getDelegate().skip(n);
	}

	@Override
	public int available() throws IOException {
		return getDelegate().available();
	}
	@Override
	public void close() throws IOException {
		if (delegate != null) {
			delegate.close();
		}
	}

	@Override
	@SuppressWarnings("sync-override")
	public void mark(int readlimit) {
		try {
			getDelegate().mark(readlimit);
		} catch (IOException e) {
			throw new RuntimeException("error while trying to get delegate", e);
		}
	}

	@Override
	@SuppressWarnings("sync-override")
	public void reset() throws IOException {
		getDelegate().reset();
	}

	@Override
	public boolean markSupported() {
		try {
			return getDelegate().markSupported();
		} catch (IOException e) {
			throw new RuntimeException("error while trying to get delegate", e);
		}
	}

}
