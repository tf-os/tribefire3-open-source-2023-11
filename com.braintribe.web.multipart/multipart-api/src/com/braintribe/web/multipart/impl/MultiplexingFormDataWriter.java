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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.braintribe.web.multipart.api.FormDataWriter;
import com.braintribe.web.multipart.api.PartHeader;
import com.braintribe.web.multipart.api.PartWriter;

public class MultiplexingFormDataWriter implements FormDataWriter {
	private final FormDataWriter formDataWriter;
	private LogicalPart lastActiveLogicalPart;
	private OutputStream currentPartOut;
	private final List<LogicalPart> logicalParts = new ArrayList<>();

	public MultiplexingFormDataWriter(FormDataWriter formDataWriter) {
		super();
		this.formDataWriter = formDataWriter;
	}

	@Override
	public void close() throws Exception {

		for (LogicalPart logicalPart: logicalParts) {
			logicalPart.outputStream().close();
		}

		currentPartOut.close();
		formDataWriter.close();
	}

	@Override
	public PartWriter openPart(PartHeader header) throws IOException {
		LogicalPart logicalPart = new LogicalPart(header);
		logicalParts.add(logicalPart);
		return logicalPart;
	}

	private void logicalWrite(LogicalPart logicalPart, byte[] b, int off, int len) throws IOException {
		synchronized(this) {
			// continue in block or open new block?
			if (lastActiveLogicalPart != logicalPart) {
				if (lastActiveLogicalPart != null) {
					currentPartOut.close();
				}

				lastActiveLogicalPart = logicalPart;
				PartWriter partWriter = formDataWriter.openPart(logicalPart);
				currentPartOut = partWriter.outputStream();

			}

			currentPartOut.write(b, off, len);
		}
	}

	private class LogicalPart extends DelegatingPartHeader implements PartWriter {
		private final OutputStream out; 
		public LogicalPart(PartHeader partHeader) {
			super(partHeader);
			this.out = new BufferedOutputStream(new LogicalOutputStream(this));
		}

		@Override
		public OutputStream outputStream() {
			return out;
		}
	}

	private class LogicalOutputStream extends OutputStream {

		private final LogicalPart logicalPart;

		public LogicalOutputStream(LogicalPart logicalPart) {
			this.logicalPart = logicalPart;
		}

		@Override
		public void write(int b) throws IOException {
			write(new byte[]{(byte)b}, 0, 1);
		}

		@Override
		public void write(byte[] b) throws IOException {
			write(b, 0, b.length);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			logicalWrite(logicalPart, b, off, len);
		}

		@Override
		public void flush() throws IOException {
		}

		@Override
		public void close() throws IOException {
		}
	}

}