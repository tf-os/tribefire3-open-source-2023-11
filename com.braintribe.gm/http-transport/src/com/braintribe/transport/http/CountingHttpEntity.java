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
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

import com.braintribe.utils.stream.CountingInputStream;
import com.braintribe.utils.stream.CountingOutputStream;

public class CountingHttpEntity implements HttpEntity {

	private HttpEntity delegate;
	
	private CountingInputStream countingInputStream;
	private long inputStreamCount = 0;
	private long outputStreamCount = 0;
	
	public CountingHttpEntity(HttpEntity delegate) {
		this.delegate = delegate;
	}
	
	public long getCount() {
		return inputStreamCount+outputStreamCount;
	}

	@Override
	public boolean isRepeatable() {
		return delegate.isRepeatable();
	}

	@Override
	public boolean isChunked() {
		return delegate.isChunked();
	}

	@Override
	public long getContentLength() {
		return delegate.getContentLength();
	}

	@Override
	public Header getContentType() {
		return delegate.getContentType();
	}

	@Override
	public Header getContentEncoding() {
		return delegate.getContentEncoding();
	}

	@Override
	public InputStream getContent() throws IOException, UnsupportedOperationException {
		InputStream delegateInputStream = delegate.getContent();
		if (delegateInputStream == null) {
			return null;
		}
		// Reset
		inputStreamCount = 0;
		countingInputStream = new CountingInputStream(delegateInputStream, false);
		return countingInputStream;
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		CountingOutputStream countingOutputStream = new CountingOutputStream(outstream);
		try {
			delegate.writeTo(countingOutputStream);
		} finally {
			outputStreamCount = countingOutputStream.getCount();
		}
	}

	@Override
	public boolean isStreaming() {
		return delegate.isStreaming();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void consumeContent() throws IOException {
		delegate.consumeContent();
	}
	
}
