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
package com.braintribe.util.servlet.stream;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;

/**
 * This is a wrapper around the OutputStream provided by a ServletReponse (and a HttpServletReponse)
 * which waits to open the output stream until the first byte is actually about to be written.
 * This prevents the output stream to be opened too early, thus preventing the ExceptionFilter
 * to send an exception to the client.
 */
public class OnDemandOpeningOutputStream extends ServletOutputStream {

	private ServletResponse response;
	private ServletOutputStream delegate;

	public OnDemandOpeningOutputStream(ServletResponse response) {
		this.response = response;
	}
	
	// OutputStream methods
	
	@Override
	public void write(int b) throws IOException {
		if (delegate == null) {
			delegate = response.getOutputStream();
		}
		delegate.write(b);
	}
	
	@Override
	public void write(byte b[], int off, int len) throws IOException {
		if (delegate == null) {
			delegate = response.getOutputStream();
		}
		delegate.write(b, off, len);
	}

	@Override
	public void write(byte b[]) throws IOException {
		if (delegate == null) {
			delegate = response.getOutputStream();
		}
		delegate.write(b);
	}

	@Override
	public void flush() throws IOException {
		// There could be a valid argument that we should not try to open the stream when we're simply trying to flush it
		// but we don't want to change the behaviour of the underlying output stream. Hence, we open the stream
		// anyway.
		if (delegate == null) {
			delegate = response.getOutputStream();
		}
		delegate.flush();
	}

	@Override
	public void close() throws IOException {
		// There could be a valid argument that we should not try to open the stream when we're simply trying to close it
		// but we don't want to change the behaviour of the underlying output stream. Hence, we open the stream
		// anyway.
		if (delegate == null) {
			delegate = response.getOutputStream();
		}
		delegate.close();
	}

	// ServletOutputStream methods
	
	@Override
	public void print(boolean arg0) throws IOException {
		if (delegate == null) {
			delegate = response.getOutputStream();
		}
		delegate.print(arg0);
	}

	@Override
	public void print(char c) throws IOException {
		if (delegate == null) {
			delegate = response.getOutputStream();
		}
		delegate.print(c);
	}

	@Override
	public void print(double d) throws IOException {
		if (delegate == null) {
			delegate = response.getOutputStream();
		}
		delegate.print(d);
	}

	@Override
	public void print(float f) throws IOException {
		if (delegate == null) {
			delegate = response.getOutputStream();
		}
		delegate.print(f);
	}

	@Override
	public void print(int i) throws IOException {
		if (delegate == null) {
			delegate = response.getOutputStream();
		}
		delegate.print(i);
	}

	@Override
	public void print(long l) throws IOException {
		if (delegate == null) {
			delegate = response.getOutputStream();
		}
		delegate.print(l);
	}

	@Override
	public void print(String arg0) throws IOException {
		if (delegate == null) {
			delegate = response.getOutputStream();
		}
		delegate.print(arg0);
	}

	@Override
	public void println() throws IOException {
		if (delegate == null) {
			delegate = response.getOutputStream();
		}
		delegate.println();
	}

	@Override
	public void println(boolean b) throws IOException {
		if (delegate == null) {
			delegate = response.getOutputStream();
		}
		delegate.println(b);
	}

	@Override
	public void println(char c) throws IOException {
		if (delegate == null) {
			delegate = response.getOutputStream();
		}
		delegate.println(c);
	}

	@Override
	public void println(double d) throws IOException {
		if (delegate == null) {
			delegate = response.getOutputStream();
		}
		delegate.println(d);
	}

	@Override
	public void println(float f) throws IOException {
		if (delegate == null) {
			delegate = response.getOutputStream();
		}
		delegate.println(f);
	}

	@Override
	public void println(int i) throws IOException {
		if (delegate == null) {
			delegate = response.getOutputStream();
		}
		delegate.println(i);
	}

	@Override
	public void println(long l) throws IOException {
		if (delegate == null) {
			delegate = response.getOutputStream();
		}
		delegate.println(l);
	}

	@Override
	public void println(String s) throws IOException {
		if (delegate == null) {
			delegate = response.getOutputStream();
		}
		delegate.println(s);
	}
}
