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
package com.braintribe.web.multipart.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.braintribe.utils.IOTools;
import com.braintribe.web.multipart.api.FormDataWriter;
import com.braintribe.web.multipart.api.MutablePartHeader;
import com.braintribe.web.multipart.api.PartHeader;
import com.braintribe.web.multipart.api.PartHeaders;
import com.braintribe.web.multipart.api.PartWriter;

public class ReflectedMultiplexingFormDataWriter implements FormDataWriter {
	private final FormDataWriter formDataWriter;
	private final List<PartWriter> logicalParts = new ArrayList<>();
	private final Lock multiplexingLock = new ReentrantLock(); 

	public ReflectedMultiplexingFormDataWriter(FormDataWriter formDataWriter) {
		super();
		this.formDataWriter = formDataWriter;
	}

	@Override
	public void close() throws Exception {

		for (PartWriter logicalPart : logicalParts) {
			logicalPart.outputStream().close();
		}

		formDataWriter.close();
	}

	public PartWriter openSingleplexedPart(PartHeader header) throws IOException {
		return new DirectPart(header);
	}
	
	@Override
	public PartWriter openPart(PartHeader header) throws IOException {
		LogicalPart logicalPart = new LogicalPart(header);
		logicalParts.add(logicalPart);
		return logicalPart;
	}
	
	private enum LogicalMode { first, intermediate, last, compact }
	
	private void physicalWrite(LogicalPart logicalPart, byte[] b, int off, int len, LogicalMode logicalMode) throws IOException {
		multiplexingLock.lock();
		
		try {
			MutablePartHeader header = new MutablePartHeaderImpl();
			
			header.setName(logicalPart.getName());
			header.setContentLength(String.valueOf(len));

			switch (logicalMode) {
				case first:
					header.addHeader(PartHeaders.MULTIPLEXED, Boolean.TRUE.toString());
					
					logicalPart.getHeaders().forEach(e -> {
						String key = e.getKey();
						
						if (!key.equals("Content-Disposition")) {
							key = "Logical-" + key;
							for (String value: e.getValue()) {
								header.addHeader(key, value);
							}
						}
					});
					break;
					
				case intermediate:
					break;
					
				case last:
					header.setHeader(PartHeaders.LOGICAL_EOF, Boolean.TRUE.toString());
					break;
					
				case compact:
				default:
					break;
			}
			
			PartWriter partWriter = formDataWriter.openPart(header);
			
			try (OutputStream out = partWriter.outputStream()) {
				out.write(b, off, len);
			}
		}
		finally {
			multiplexingLock.unlock();
		}
	}

	private class DirectPart extends DelegatingPartHeader implements PartWriter {
		private OutputStream outputStream;

		public DirectPart(PartHeader delegate) throws IOException {
			super(delegate);
			
			multiplexingLock.lock();
			
			try {
				PartWriter physicalWriter = formDataWriter.openPart(this);
				outputStream = new DirectOutputStream(physicalWriter.outputStream());
			}
			catch (RuntimeException | Error e) {
				multiplexingLock.unlock();
				throw e;
			}
			catch (IOException e) {
				multiplexingLock.unlock();
				throw e;
			}
		}
		
		@Override
		public OutputStream outputStream() {
			return outputStream;
		}
	}
	
	private class DirectOutputStream extends OutputStream {
		private final OutputStream delegate;

		public DirectOutputStream(OutputStream delegate) {
			this.delegate = delegate;
		}
		
		@Override
		public void write(int b) throws IOException {
			delegate.write(b);
		}
		
		@Override
		public void write(byte[] b) throws IOException {
			delegate.write(b);
		}
		
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			delegate.write(b, off, len);
		}
		
		@Override
		public void flush() throws IOException {
			delegate.flush();
		}
		
		@Override
		public void close() throws IOException {
			try {
				delegate.close();
			}
			finally {
				multiplexingLock.unlock();
			}
		}
		
	}
	
	private class LogicalPart extends DelegatingPartHeader implements PartWriter {
		private final OutputStream out;

		public LogicalPart(PartHeader partHeader) {
			super(partHeader);
			this.out = new LogicalOutputStream(this);
		}

		@Override
		public OutputStream outputStream() {
			return out;
		}
	}

	private class LogicalOutputStream extends OutputStream {

		private final LogicalPart logicalPart;
		private final ByteBuffer buffer = ByteBuffer.allocate(IOTools.SIZE_16K);
		private boolean isClosed = false;
		private LogicalMode logicalMode = LogicalMode.first; 

		public LogicalOutputStream(LogicalPart logicalPart) {
			this.logicalPart = logicalPart;
		}
		
		public LogicalMode nextLogicalMode() {
			LogicalMode mode = logicalMode;
			logicalMode = LogicalMode.intermediate;
			return mode;
		}

		@Override
		public void write(int b) throws IOException {
			write(new byte[] { (byte) b }, 0, 1);
		}

		@Override
		public void write(byte[] b) throws IOException {
			write(b, 0, b.length);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			if (buffer.position() > 0) {
				int bytesToWrite = Math.min(buffer.remaining(), len);
				buffer.put(b, off, bytesToWrite);
				off += bytesToWrite;
				len -= bytesToWrite;
			}
				
			boolean firstDirectWrite = true;
			
			while (len >= buffer.capacity() && len > 0) {
				if (firstDirectWrite) {
					firstDirectWrite = false;
					flush();
				}
				int bytesToWrite = Math.min(buffer.capacity(), len);
				physicalWrite(logicalPart, b, off, bytesToWrite, nextLogicalMode());
				off += bytesToWrite;
				len -= bytesToWrite;
			}
			
			if (len > 0) {
				int bytesToWrite = Math.min(buffer.remaining(), len);
				buffer.put(b, off, bytesToWrite);
				off += bytesToWrite;
				len -= bytesToWrite;
				
				if (buffer.remaining() == 0) {
					flush();
				}
				
				buffer.put(b, off, len);
			}
			
		}

		@Override
		public void flush() throws IOException {
			if (buffer.position() > 0)
				flush(nextLogicalMode());
		}
		
		private void flush(LogicalMode mode) throws IOException {
			physicalWrite(logicalPart, buffer.array(), 0, buffer.position(), mode);

			// This cast is necessary for Java backwards compatibility
			// See: https://stackoverflow.com/questions/61267495/exception-in-thread-main-java-lang-nosuchmethoderror-java-nio-bytebuffer-flip
			((Buffer) buffer).clear();
		}

		@Override
		public void close() throws IOException {
			if (isClosed)
				return;
			
			if (logicalMode == LogicalMode.first)
				flush(LogicalMode.compact);
			else
				flush(LogicalMode.last);
			
			isClosed = true;
		}
	}
}
