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
package tribefire.platform.impl.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;

/**
 * This is an implementation of a {@link ServletOutputStream} that keeps the first n bytes
 * of the output in a buffer. When the {@link #close()} or {@link #flush()} method is called,
 * the implementation decides whether the output should be sent as-is (plain) or encoded using GZIP.
 * <br>
 * The decision is made based on the size of the internal buffer. When the provided {@link #zipThreshold} is 
 * exceeded, the content will be GZIP-encoded.
 * <br>
 * The rationale is that it is an unnecessary overhead to GZIP content when it is only a small amount of data.
 * <br>
 * Note that this class is not thread-safe.
 */
class GZipServletOutputStream extends ServletOutputStream {
	
	private OutputStream targetOutputStream = null;
	private ByteArrayOutputStream baos;

	private OutputStream theOutputStream = null;

	boolean writingToBuffer = true; 
	
	private int zipThreshold;
	private int zipBufferSize;
	private Consumer<Boolean> switchToZipEventConsumer;
	
	/**
	 * Initializes the GZipServletOutputStream object.
	 * 
	 * @param output The underlying output stream.
	 * @param zipBufferSize The buffer size that should be used to create a new instance of {@link GZIPOutputStream}. This parameter will be ignored if the {@link #zipThreshold} is not reached.
	 * @param zipThreshold The maximum size of the output that will be sent as-is. If the threshold is exceeded, the output will be GZIP encoded.
	 * 	If this is negative or zero, the output will be immediately GZIP encoded. 
	 * @param switchToZipEventConsumer A listener that gets called (with the parameter TRUE) when the output is switched to GZIP encoding. This gives the 
	 * 	enclosing {@link GZipServletResponseWrapper} to set the corresponding {@code Content-Encoding} header.
	 * @throws IOException Thrown if the {@link GZIPOutputStream} could not be constructed. 
	 */
	public GZipServletOutputStream(OutputStream output, int zipBufferSize, int zipThreshold, Consumer<Boolean> switchToZipEventConsumer) throws IOException {
		super();
		this.targetOutputStream = output;
		this.zipThreshold = zipThreshold;
		this.zipBufferSize = zipBufferSize;
		this.switchToZipEventConsumer = switchToZipEventConsumer;
		if (zipThreshold > 0) {
			this.baos = new ByteArrayOutputStream(zipThreshold << 1); //We double the size of the buffer so that we do not have to increase the buffer size right before switching to another stream.
			this.theOutputStream = this.baos;
			writingToBuffer = true;
		} else {
			theOutputStream = new GZIPOutputStream(targetOutputStream, zipBufferSize);
			writingToBuffer = false;
		}
	}

	@Override
	public void close() throws IOException {
		if (writingToBuffer) {
			if (baos.size() > zipThreshold) {
				switchToZip();
			} else {
				commitToPlain();
			}
		}
		theOutputStream.close();
	}

	@Override
	public void flush() throws IOException {
		if (writingToBuffer) {
			if (baos.size() > zipThreshold) {
				switchToZip();
			} else {
				commitToPlain();
			}
		}
		theOutputStream.flush();
	}

	@Override
	public void write(byte b[]) throws IOException {
		theOutputStream.write(b);
		if (writingToBuffer) {
			if (baos.size() > zipThreshold) {
				switchToZip();
			}
		}
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		theOutputStream.write(b, off, len);
		if (writingToBuffer) {
			if (baos.size() > zipThreshold) {
				switchToZip();
			}
		}
	}

	@Override
	public void write(int b) throws IOException {
		theOutputStream.write(b);
		if (writingToBuffer) {
			if (baos.size() > zipThreshold) {
				switchToZip();
			}
		}
	}

	/**
	 * The decision is made to GZIP encode the output. A new {@link GZIPOutputStream} instance
	 * will be created and the accumulated data from the buffer written to that stream.
	 * Before that happens, the callback will be invoked so that the {@code Content-Encoding} header
	 * can be written before the actual content starts.
	 * @throws IOException Thrown when the {@link GZIPOutputStream} could not be created or written to.
	 */
	private void switchToZip() throws IOException {
		if (switchToZipEventConsumer != null) {
			switchToZipEventConsumer.accept(Boolean.TRUE);
		}
		theOutputStream = new GZIPOutputStream(targetOutputStream, zipBufferSize);
		baos.writeTo(theOutputStream);
		writingToBuffer = false;
		baos = null;
	}
	/**
	 * The decision is made to write the output as-is (hence, no zipping).
	 * @throws IOException Thrown when the data could not be written to the original output stream.
	 */
	private void commitToPlain() throws IOException {
		baos.writeTo(targetOutputStream);
		theOutputStream = targetOutputStream;
		writingToBuffer = false;
		baos = null;
	}
	
}
