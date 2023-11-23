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
package com.braintribe.model.processing.resource.server.stream;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.function.Function;

import javax.servlet.ServletOutputStream;

/**
 * <p>
 * A {@link Function}-based variant of {@link com.braintribe.util.servlet.stream.OnDemandOpeningOutputStream}.
 * 
 * <p>
 * The function is notified whether the {@link ServletOutputStream} is being demanded for a write operation or only for
 * flushing/closing.
 * 
 * @see com.braintribe.util.servlet.stream.OnDemandOpeningOutputStream
 */
public class OnDemandSuppliedOutputStream extends ServletOutputStream {

	private ServletOutputStream delegate;
	private Function<Boolean, ServletOutputStream> outputStreamSupplier;

	public OnDemandSuppliedOutputStream(Function<Boolean, ServletOutputStream> outputStreamSupplier) {
		Objects.requireNonNull(outputStreamSupplier, "outputStreamSupplier must not be null");
		this.outputStreamSupplier = outputStreamSupplier;
	}

	// OutputStream methods

	@Override
	public void write(int b) throws IOException {
		if (delegate == null) {
			delegate = getDelegate(true);
		}
		delegate.write(b);
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		if (delegate == null) {
			delegate = getDelegate(true);
		}
		delegate.write(b, off, len);
	}

	@Override
	public void write(byte b[]) throws IOException {
		if (delegate == null) {
			delegate = getDelegate(true);
		}
		delegate.write(b);
	}

	@Override
	public void flush() throws IOException {
		if (delegate == null) {
			delegate = getDelegate(false);
		}
		delegate.flush();
	}

	@Override
	public void close() throws IOException {
		if (delegate == null) {
			delegate = getDelegate(false);
		}
		delegate.close();
	}

	// ServletOutputStream methods

	@Override
	public void print(boolean arg0) throws IOException {
		if (delegate == null) {
			delegate = getDelegate(true);
		}
		delegate.print(arg0);
	}

	@Override
	public void print(char c) throws IOException {
		if (delegate == null) {
			delegate = getDelegate(true);
		}
		delegate.print(c);
	}

	@Override
	public void print(double d) throws IOException {
		if (delegate == null) {
			delegate = getDelegate(true);
		}
		delegate.print(d);
	}

	@Override
	public void print(float f) throws IOException {
		if (delegate == null) {
			delegate = getDelegate(true);
		}
		delegate.print(f);
	}

	@Override
	public void print(int i) throws IOException {
		if (delegate == null) {
			delegate = getDelegate(true);
		}
		delegate.print(i);
	}

	@Override
	public void print(long l) throws IOException {
		if (delegate == null) {
			delegate = getDelegate(true);
		}
		delegate.print(l);
	}

	@Override
	public void print(String arg0) throws IOException {
		if (delegate == null) {
			delegate = getDelegate(true);
		}
		delegate.print(arg0);
	}

	@Override
	public void println() throws IOException {
		if (delegate == null) {
			delegate = getDelegate(true);
		}
		delegate.println();
	}

	@Override
	public void println(boolean b) throws IOException {
		if (delegate == null) {
			delegate = getDelegate(true);
		}
		delegate.println(b);
	}

	@Override
	public void println(char c) throws IOException {
		if (delegate == null) {
			delegate = getDelegate(true);
		}
		delegate.println(c);
	}

	@Override
	public void println(double d) throws IOException {
		if (delegate == null) {
			delegate = getDelegate(true);
		}
		delegate.println(d);
	}

	@Override
	public void println(float f) throws IOException {
		if (delegate == null) {
			delegate = getDelegate(true);
		}
		delegate.println(f);
	}

	@Override
	public void println(int i) throws IOException {
		if (delegate == null) {
			delegate = getDelegate(true);
		}
		delegate.println(i);
	}

	@Override
	public void println(long l) throws IOException {
		if (delegate == null) {
			delegate = getDelegate(true);
		}
		delegate.println(l);
	}

	@Override
	public void println(String s) throws IOException {
		if (delegate == null) {
			delegate = getDelegate(true);
		}
		delegate.println(s);
	}

	private ServletOutputStream getDelegate(boolean forWriting) throws IOException {
		try {
			return outputStreamSupplier.apply(forWriting);
		} catch (UncheckedIOException e) {
			throw e.getCause();
		}
	}

}
