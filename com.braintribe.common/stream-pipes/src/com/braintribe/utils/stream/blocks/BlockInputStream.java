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
import java.io.InputStream;

class BlockInputStream extends InputStream {
	private final Block block;
	private final InputStream delegate;
	private long pos;
	
	public BlockInputStream(Block block) {
		this.block = block;
		delegate = block.openRawInputStream();
	}
	
	@Override
	public int read() throws IOException {
		if (available() <= 0) {
			return -1;
		}
		
		pos ++;
		return delegate.read();
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (len < 0) {
			// TODO: Remove this then redundant check after bug was found (CORETS-753)
			throw new IllegalArgumentException("Attempt to read a negative amount of bytes: len=" + len + ", off=" + off + ", available=" + available());
		}
		
		if (available() == 0)
			return -1;
		
		len = Math.min(len, available());
		int readBytes = delegate.read(b, off, len);
		
		pos += readBytes;
		
		return readBytes;
	}
	
	@Override
	public int available() throws IOException {
		if (block.getBytesWritten() < pos) {
			// TODO: Remove this then redundant check after bug was found (CORETS-753)
			throw new IllegalStateException("Internal state corrupted: Pos > written bytes: pos=" + pos + ", bytes written=" + block.getBytesWritten());
		}
		return (int) Math.min(Integer.MAX_VALUE, block.getBytesWritten() - pos);
	}
	
	@Override
	public void close() throws IOException {
		delegate.close();
	}
	
}