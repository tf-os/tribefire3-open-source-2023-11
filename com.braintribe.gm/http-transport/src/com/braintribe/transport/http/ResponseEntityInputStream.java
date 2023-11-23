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
package com.braintribe.transport.http;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;

import com.braintribe.logging.Logger;

public class ResponseEntityInputStream extends InputStream {

	private static final Logger logger = Logger.getLogger(ResponseEntityInputStream.class);

	private final HttpResponse response;
	private final InputStream content;

	public ResponseEntityInputStream(HttpResponse response) throws UnsupportedOperationException, IOException {
		this.response = response;
		content = response.getEntity().getContent();
	}


	@Override
	public void close() throws IOException {
		try {
			content.close();
		} finally {
			if (response instanceof CloseableHttpResponse) {
				CloseableHttpResponse cr = (CloseableHttpResponse) response;
				try {
					cr.close();
				} catch(Exception e) {
					logger.error("Could not close HTTP response.", e);
				}
			} else {
				logger.trace(() -> "Http response object is of type "+response.getClass());
			}
		}
	}


	// Delegate methods

	@Override
	public int read() throws IOException {
		return content.read();
	}

	@Override
	public int hashCode() {
		return content.hashCode();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return content.read(b);
	}

	@Override
	public boolean equals(Object obj) {
		return content.equals(obj);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return content.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		return content.skip(n);
	}

	@Override
	public String toString() {
		return content.toString();
	}

	@Override
	public int available() throws IOException {
		return content.available();
	}

	@Override
	public void mark(int readlimit) {
		content.mark(readlimit);
	}

	@Override
	public void reset() throws IOException {
		content.reset();
	}

	@Override
	public boolean markSupported() {
		return content.markSupported();
	}
}
