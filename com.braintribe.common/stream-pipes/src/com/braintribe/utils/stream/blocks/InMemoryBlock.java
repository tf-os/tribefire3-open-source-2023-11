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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A {@link Block} that persists in a fixed in-memory buffer of given size.
 * 
 * @author Neidhart.Orlich
 *
 */
public class InMemoryBlock extends Block {
	private byte[] buffer;
	
	public InMemoryBlock(int size) {
		buffer = new byte[size];
	}

	@Override
	public InputStream openRawInputStream() {
		return new ByteArrayInputStream(buffer);
	}

	@Override
	public OutputStream openOutputStream() {
		return new ExistingByteArrayOutputStream(buffer);
	}

	@Override
	public int getTreshold() {
		return buffer.length;
	}
	
	@Override
	public void destroy() {
		buffer = null; 
	}
	
	@Override
	public void autoBufferInputStreams(int bufferSize) {
		// Ignore - no additional buffering needed.
	}
	
	public byte[] getBuffer() {
		return buffer;
	}

	@Override
	public boolean isAutoBuffered() {
		return false;
	}

	@Override
	public long getBytesAllocated() {
		return buffer.length;
	}
}