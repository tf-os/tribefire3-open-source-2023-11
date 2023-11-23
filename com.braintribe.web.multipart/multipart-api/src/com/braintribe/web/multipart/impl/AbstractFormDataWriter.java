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
import java.util.Collection;

import com.braintribe.web.multipart.api.FormDataWriter;
import com.braintribe.web.multipart.api.PartHeader;
import com.braintribe.web.multipart.api.PartWriter;

public abstract class AbstractFormDataWriter implements FormDataWriter, FormDataMultipartConstants {
	protected final OutputStream out;
	private PartWriter currentPartWriter;


	protected AbstractFormDataWriter(OutputStream outputStream) {
		this.out = outputStream;
		
	}
	
	@Override
	public PartWriter openPart(PartHeader header) throws IOException {
		if (currentPartWriter == null) {
			currentPartWriter = openPartImpl(header);
			return currentPartWriter;
		} else {
			throw new IllegalStateException("Cannot open another part as long as the previous part is still open");
		}
	}

	protected abstract PartWriter openPartImpl(PartHeader header) throws IOException;

	@Override
	public void close() throws Exception {
		if (currentPartWriter != null) {
			throw new IllegalStateException("Can't close FormDataWriter because a part is still open: " + currentPartWriter.getFormDataContentDisposition());
		}
		
		out.write(MULTIPART_HYPHENS);
		out.write(HTTP_LINEBREAK);
		out.flush();
	}

	protected void writeHeader(PartHeader partHeader) throws IOException {
		out.write(HTTP_LINEBREAK);
		for (String headerName : partHeader.getHeaderNames()) {
			Collection<String> headerValues = partHeader.getHeaders(headerName);
			for (String headerValue : headerValues) {
				writeHeaderLine(headerName, headerValue);
			}
		}
		writeLineBreak();
	}

	protected void writeHeaderLine(String headerName, String headerValue) throws IOException {
		String line = headerName + ": " + headerValue;
		writeLine(line);
	}

	protected void writeLine(String s) throws IOException {
		byte[] bytes = s.getBytes("ISO-8859-1");
		out.write(bytes);
		writeLineBreak();
	}

	protected void writeLineBreak() throws IOException {
		out.write(HTTP_LINEBREAK);
	}

	
	
	protected void freeCurrentPartWriter() {
		currentPartWriter = null;
	}
	
	
	
}
