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
package com.braintribe.utils.stream;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

/**
 * This class is a wrapper around a provided Writer. All actions are invoked with the delegate Writer. The functionality of this wrapper is that it
 * additionally writes the content to a secondary ("tee") writer. Output to the secondary Writer can be stopped irrevocably by invocing
 * {@link #stopTee()}. This implementation will first try to write the content to the tee Writer and then to the main delegate Writer. If the tee
 * Writer throws an exception, the data will not be written to the delegate Writer. If the delegate Writer throws an Exception, the tee Writer has
 * already gotten the information.
 */
public class TeeWriter extends Writer {

	protected Writer delegate;
	protected Writer writer;

	public TeeWriter(Writer delegateWriter, Writer writer) {
		this.delegate = delegateWriter;
		this.writer = writer;
	}

	public void stopTee() {
		if (writer != null) {
			try {
				writer.flush();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			} finally {
				writer = null;
			}
		}

	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	@Override
	public void flush() throws IOException {
		if (writer != null) {
			writer.flush();
		}
		delegate.flush();
	}

	@Override
	public void close() throws IOException {
		if (writer != null) {
			writer.close();
		}
		delegate.close();
	}

	@Override
	public void write(int c) throws IOException {
		if (writer != null) {
			writer.write(c);
		}
		delegate.write(c);
	}

	@Override
	public void write(char[] buf, int off, int len) throws IOException {
		if (writer != null) {
			writer.write(buf, off, len);
		}
		delegate.write(buf, off, len);
	}

	@Override
	public void write(char[] buf) throws IOException {
		if (writer != null) {
			writer.write(buf);
		}
		delegate.write(buf);
	}

	@Override
	public void write(String s, int off, int len) throws IOException {
		if (writer != null) {
			writer.write(s, off, len);
		}
		delegate.write(s, off, len);
	}

	@Override
	public void write(String s) throws IOException {
		if (writer != null) {
			writer.write(s);
		}
		delegate.write(s);
	}

	@Override
	public Writer append(CharSequence csq) throws IOException {
		if (writer != null) {
			writer.append(csq);
		}
		delegate.append(csq);
		return this;
	}

	@Override
	public Writer append(CharSequence csq, int start, int end) throws IOException {
		if (writer != null) {
			writer.append(csq, start, end);
		}
		delegate.append(csq, start, end);
		return this;
	}

	@Override
	public Writer append(char c) throws IOException {
		if (writer != null) {
			writer.append(c);
		}
		delegate.append(c);
		return this;
	}
}
