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
package com.braintribe.utils.stream.blocks;

import java.io.IOException;
import java.io.OutputStream;

class ExistingByteArrayOutputStream extends OutputStream {
	private final byte[] memory;
	private long pos;

	public ExistingByteArrayOutputStream(byte[] memory) {
		this.memory = memory;
	}

	@Override
	public void write(int b) throws IOException {
		memory[(int) pos] = (byte) b;
		pos ++;
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		System.arraycopy(b, off, memory, (int) pos, len);
		pos += len;
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		this.write(b, 0, b.length);
	}
}