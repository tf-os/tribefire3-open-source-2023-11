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
package com.braintribe.model.processing.wopi.misc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.impl.EnglishReasonPhraseCatalog;

import com.braintribe.logging.Logger;
import com.braintribe.utils.IOTools;

/**
 * 
 * Streaming of document (resource) itself
 */
public class HttpResponseMessage implements HttpResponse {

	private static final Logger logger = Logger.getLogger(HttpResponseMessage.class);

	private final Map<String, String> headers;
	private final int sc;

	private String ct;
	private InputStream cs;
	private Integer cl;

	@Override
	public final void close() throws Exception {
		if (cs != null)
			cs.close();
	}

	@Override
	public void write(HttpServletResponse resp) throws IOException {
		int status = getStatus();
		String reason = EnglishReasonPhraseCatalog.INSTANCE.getReason(status, Locale.ENGLISH);
		logger.debug(() -> String.format("response: status: '%d' reason: '%s'", status, reason));
		resp.setStatus(getStatus());
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			resp.setHeader(entry.getKey(), entry.getValue());
		}
		resp.setContentType(getContentType());
		resp.setContentLength(getContentLength());
		try (InputStream in = getContentStream(); OutputStream out = resp.getOutputStream()) {
			if (in != null && out != null)
				IOTools.inputToOutput(in, out);
		}
	}

	public HttpResponseMessage(int statusCode) {
		this.headers = new HashMap<String, String>();
		this.sc = statusCode;
	}

	public int getStatus() {
		return sc;
	}

	public void setContentType(String contentType) {
		this.ct = contentType;
	}

	public String getContentType() {
		return ct;
	}

	public void setContentStream(InputStream contentStream) {
		this.cs = contentStream;
	}

	public InputStream getContentStream() {
		return cs;
	}

	public void setContentLength(int contentLength) {
		this.cl = contentLength;
	}

	public int getContentLength() throws IOException {
		return cl != null ? cl : cs == null ? 0 : cs.available();
	}

	public String addHeader(String name, String value) {
		return value == null ? headers.remove(name) : headers.put(name, value);
	}

}
