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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public class WriterOutputStream extends OutputStream {

	protected Writer writer;
	protected String encoding = "UTF-8";
	protected int maxBufferSize = 100000;

	protected ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	protected int bufferSize = 0;

	public WriterOutputStream(Writer writer, String encoding) {
		this.writer = writer;
		if (encoding != null) {
			this.encoding = encoding;
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		this.buffer.write(b);
		this.bufferSize += b.length;
		this.flushBufferConditionally();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		this.buffer.write(b, off, len);
		this.bufferSize += len;
		this.flushBufferConditionally();
	}

	@Override
	public void write(int b) throws IOException {
		this.buffer.write(b);
		this.bufferSize++;
		this.flushBufferConditionally();
	}

	@Override
	public void close() throws IOException {
		this.flush();
		this.writer.close();
	}

	@Override
	public void flush() throws IOException {
		this.writer.write(this.buffer.toString(encoding));
		this.writer.flush();
		this.buffer.reset();
		this.bufferSize = 0;
	}

	protected void flushBufferConditionally() throws IOException {
		if (this.bufferSize >= this.maxBufferSize) {
			this.flush();
		}
	}

	public void setMaxBufferSize(int maxBufferSize) {
		this.maxBufferSize = maxBufferSize;
	}

}
