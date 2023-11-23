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
package com.braintribe.model.processing.webrpc.server.multipart;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import com.braintribe.model.processing.rpc.commons.api.RpcConstants;
import com.braintribe.utils.stream.KeepAliveDelegateOutputStream;
import com.braintribe.utils.stream.api.StreamPipeFactory;
import com.braintribe.web.multipart.api.FormDataWriter;
import com.braintribe.web.multipart.api.MultipartFormat;
import com.braintribe.web.multipart.api.MutablePartHeader;
import com.braintribe.web.multipart.api.PartWriter;
import com.braintribe.web.multipart.impl.MultipartSubFormat;
import com.braintribe.web.multipart.impl.Multiparts;
import com.braintribe.web.multipart.impl.ReflectedMultiplexingFormDataWriter;
import com.braintribe.web.multipart.impl.SequentialParallelFormDataWriter;

public class PartAcquiring implements AutoCloseable {
	
	private boolean multipartResponse = false;
	private final HttpServletResponse resp;
	private final MultipartFormat acceptedMultipart;
	private FormDataWriter formDataWriter;
	private final StreamPipeFactory streamPipeFactory;

	public PartAcquiring(HttpServletResponse resp, MultipartFormat acceptedMultipart, StreamPipeFactory streamPipeFactory) {
		super();
		this.resp = resp;
		this.acceptedMultipart = acceptedMultipart;
		this.streamPipeFactory = streamPipeFactory;
	}
	
	public boolean isMultipartEnabled() {
		return acceptedMultipart != null;
	}
	
	public boolean isExpensiveMultipart() {
		return acceptedMultipart != null && acceptedMultipart.getSubFormat() == MultipartSubFormat.formData;
	}
	
	private FormDataWriter getFormDataWriter() throws IOException {
		if (formDataWriter == null) {
			if (acceptedMultipart == null)
				throw new IllegalStateException("No multipart supported when acceptedMultipart is null");

			switch (acceptedMultipart.getSubFormat()) {
				case formData:
					String boundary = Multiparts.generateBoundary();
					resp.setContentType(acceptedMultipart.getMimeType() + "; boundary=" + boundary);
					
					if (Boolean.TRUE.toString().equals(acceptedMultipart.getParameter("sliceable"))) {
						formDataWriter = new SequentialParallelFormDataWriter(Multiparts.blobFormDataWriter(resp.getOutputStream(), boundary), streamPipeFactory);
					}
					else {
						formDataWriter = new SequentialParallelFormDataWriter(Multiparts.formDataWriter(resp.getOutputStream(), boundary), streamPipeFactory);
					}
					
					break;
				case chunked:
					resp.setContentType(acceptedMultipart.getMimeType());
					formDataWriter = new ReflectedMultiplexingFormDataWriter(Multiparts.chunkedFormDataWriter(resp.getOutputStream()));
					break;
				default:
					throw new IllegalStateException("unsupported multipart format: " + acceptedMultipart);
			}
		}

		return formDataWriter;
	}

	public OutputStream openRpcResponseStream(String contentType) throws IOException {
		if (multipartResponse) {
			FormDataWriter formDataWriter = getFormDataWriter();
			
			// open the part for the rpc response serialization
			MutablePartHeader header = Multiparts.newPartHeader();
			header.setContentType(contentType);
			header.setName(RpcConstants.RPC_MAPKEY_RESPONSE);

			final PartWriter partWriter;
			
			if (formDataWriter instanceof ReflectedMultiplexingFormDataWriter) {
				partWriter = ((ReflectedMultiplexingFormDataWriter) formDataWriter).openSingleplexedPart(header);
			}
			else {
				partWriter = formDataWriter.openPart(header);
			}
			
			OutputStream partOut = partWriter.outputStream();

			return partOut;
		}
		else {
			resp.setContentType(contentType);
			return new KeepAliveDelegateOutputStream(resp.getOutputStream());
		}
	}

	public OutputStream openPartStream(String name) throws IOException {
		if (multipartResponse) {
			FormDataWriter formDataWriter = getFormDataWriter();
			MutablePartHeader header = Multiparts.newPartHeader();
			header.setContentType("application/octet-stream");
			header.setName(name);
			
			return formDataWriter.openPart(header).outputStream();
		}
		else {
			throw new IllegalStateException("Multipart is not enabled for the PartAquiring");
		}
	}

	public void setMultipartResponse(boolean multipartResponse) {
		this.multipartResponse = multipartResponse;
	}
	
	public boolean isMultipartResponse() {
		return multipartResponse;
	}

	@Override
	public void close() throws Exception {
		if (formDataWriter != null) {
			formDataWriter.close();
		}
	}
}
