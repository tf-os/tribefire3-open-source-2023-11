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
import java.io.OutputStream;

public abstract class DelegateOutputStream extends OutputStream {
	private OutputStream delegate;

	protected abstract OutputStream openDelegate() throws IOException;

	@Override
	public void write(int b) throws IOException {
		getDelegate().write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		getDelegate().write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		getDelegate().write(b, off, len);
	}

	@Override
	public void flush() throws IOException {
		getDelegate().flush();
	}

	@Override
	public void close() throws IOException {
		getDelegate().close();
	}

	protected OutputStream getDelegate() throws IOException {
		if (delegate == null) {
			delegate = openDelegate();
		}

		return delegate;
	}
}
