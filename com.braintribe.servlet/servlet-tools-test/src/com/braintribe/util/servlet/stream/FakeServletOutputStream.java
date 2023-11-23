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
package com.braintribe.util.servlet.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;

public class FakeServletOutputStream extends ServletOutputStream {

	protected ByteArrayOutputStream baos = new ByteArrayOutputStream();
	protected boolean isClosed = false;
	
	public byte[] getData() {
		return baos.toByteArray();
	}
	
	public boolean isClosed() {
		return this.isClosed;
	}
	
	@Override
	public void write(int b) throws IOException {
		baos.write(b);
	}
	
	@Override
	public void write(byte b[], int off, int len) throws IOException {
		baos.write(b, off, len);
	}

	@Override
	public void write(byte b[]) throws IOException {
		baos.write(b);
	}

	@Override
	public void flush() throws IOException {
		baos.flush();
	}

	@Override
	public void close() throws IOException {
		this.isClosed = true;
		baos.close();
	}
}
