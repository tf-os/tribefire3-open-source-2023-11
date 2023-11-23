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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.concurrent.locks.ReentrantLock;

import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;

/**
 * This class is a wrapper around a provided OutputStream. All actions (write, flush, close,...) are invoked with the
 * delegate OutputStream. The functionality of this wrapper is that it additionally stores the content in an internal
 * ByteArrayOutputStream and writes the content to a provided writer. The size of the stored content is limited by a
 * provided maximum length. A typical use case is to store the first n bytes of data written to an OutputStream so that
 * it can be written to the log or a separate file. {@link com.braintribe.logging.io.LoggingPrintWriter} could be used
 * to write the content directly to the log. The content is written to the writer when either close() is invoked or the
 * maximum length has been reached. This makes sure that the content is encoded to UTF-8 (or any other configurable
 * encoding) in full (otherwise, encoding single bytes might be problematic when a character consist of multiple bytes).
 * After the content has been written to the writer, the internal buffer is cleared and no further recording will be
 * performed.
 */
public class TeeWriterOutputStream extends OutputStream {

	private static Logger logger = Logger.getLogger(TeeWriterOutputStream.class);

	protected OutputStream delegate;
	protected Writer writer;
	protected int maxLogLength;
	protected ByteArrayOutputStream baos = new ByteArrayOutputStream();
	protected String encoding = "UTF-8";
	protected LogLevel logLevel = LogLevel.DEBUG;
	protected ReentrantLock lock = new ReentrantLock();

	public TeeWriterOutputStream(OutputStream delegateStream, Writer writer, int maxWriterLength) {
		this.delegate = delegateStream;
		this.writer = writer;
		this.maxLogLength = maxWriterLength;
	}

	@Override
	public void write(byte b[]) throws IOException {
		this.addToBuffer(b, 0, b.length);
		this.delegate.write(b, 0, b.length);
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		this.addToBuffer(b, off, len);
		this.delegate.write(b, off, len);
	}

	@Override
	public void write(int b) throws IOException {
		this.addToBuffer(b);
		this.delegate.write(b);
	}

	@Override
	public void flush() throws IOException {
		this.delegate.flush();
	}

	@Override
	public void close() throws IOException {
		this.delegate.close();
		this.writeToLog();
	}

	protected void addToBuffer(byte[] b, int off, int length) {
		lock.lock();
		try {
			if (this.baos == null || maxLogLength <= 0) {
				return;
			}

			int currentLength = this.baos.size();
			int remainingLength = this.maxLogLength - currentLength;
			int logLength = Math.min(remainingLength, length);
			if (logLength <= 0) {
				this.writeToLog();
			} else {
				this.baos.write(b, off, logLength);
				if (logLength == remainingLength) {
					this.writeToLog();
				}
			}
		} finally {
			lock.unlock();
		}
	}

	protected void addToBuffer(int b) {
		lock.lock();
		try {
			if (this.baos == null || maxLogLength <= 0) {
				return;
			}

			int currentLength = this.baos.size();
			int remainingLength = this.maxLogLength - currentLength;
			if (remainingLength < 1) {
				this.writeToLog();
			} else {
				this.baos.write(b);
				if (remainingLength == 1) {
					this.writeToLog();
				}
			}
		} finally {
			lock.unlock();
		}
	}

	protected void writeToLog() {
		lock.lock();
		try {
			if (this.baos == null) {
				return;
			}
			try {
				this.baos.flush();
				this.baos.close();
				String bufferString = this.baos.toString(this.encoding);
				this.writer.write(bufferString);
				this.writer.flush();
			} catch (Throwable t) {
				logger.debug("Could not log OutputStream content to log", t);
			} finally {
				this.baos = null;
			}
		} finally {
			lock.unlock();
		}
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public LogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

}
