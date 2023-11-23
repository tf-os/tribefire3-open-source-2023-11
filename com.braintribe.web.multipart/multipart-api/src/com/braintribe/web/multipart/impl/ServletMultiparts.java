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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.web.multipart.api.FormDataMultipartsBuilder;
import com.braintribe.web.multipart.api.FormDataWriter;
import com.braintribe.web.multipart.api.MalformedMultipartDataException;

public abstract class ServletMultiparts extends Multiparts {
	public static boolean isFormDataMultipartsRequest(HttpServletRequest request) {
		return isFormDataMultipartsContentType(request.getContentType());
	}
	
	public static MultipartSubFormat getMultipartSubFormat(HttpServletRequest request) {
		return getMultipartSubFormat(request.getContentType());
	}

	public static FormDataMultipartsBuilder formDataReader(HttpServletRequest request) throws IOException, MalformedMultipartDataException {
		if (!isFormDataMultipartsRequest(request)) {
			throw new MalformedMultipartDataException(
					"invalid request: expected content type multipart/form-data but found " + request.getContentType());
		}

		String contentType = request.getContentType();
		return formDataReader(request.getInputStream(), extractBoundaryFromContentType(contentType));
	}
	
	public static FormDataWriter formDataWriter(HttpServletResponse response) throws IOException {
		String boundary = generateBoundary();
		response.setContentType("multipart/form-data; boundary=" + boundary);
		return formDataWriter(response.getOutputStream(), boundary);
	}
}

class ConsoleOutputStream extends OutputStream {
	private OutputStream delegate;
	private OutputStream capture;

	public ConsoleOutputStream(OutputStream delegate) throws IOException {
		super();
		this.delegate = delegate;
		this.capture = new FileOutputStream("c:\\braintribe\\research\\rpc.txt", true);
		this.capture.write("\n==============================================================\n".getBytes());
	}

	@Override
	public void write(int b) throws IOException {
		delegate.write(b);
		this.capture.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		delegate.write(b);
		this.capture.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		delegate.write(b, off, len);
		this.capture.write(b, off, len);
	}

	@Override
	public void flush() throws IOException {
		delegate.flush();
	}
}
