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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.lcd.StringTools;

/**
 * This class can be seen as a {@link ByteArrayOutputStream} which makes sure that the memory consumption is kept low.
 * When a certain threshold in the buffer is reached, the content is transferred to a file and further writes are stored
 * in the file. The {@link InputStream} that can be opened on this buffer can have two modes:
 * <ul>
 * <li>A normal input stream that can be closed without any effect.</li>
 * <li>An input stream that deletes the underlying file, if it exists. This will help to clean up resources when the
 * {@link #delete()} method cannnot be invoked in the code that created the InputStream</li>
 * </ul>
 * To make absolutely sure that the underlying file is deleted, the {@link #delete()} can be called (e.g., in a finally
 * block).
 */
public class MemoryThresholdBuffer extends OutputStream {

	private static Logger logger = Logger.getLogger(MemoryThresholdBuffer.class);

	public static int DEFAULT_THRESHOLD = 512 * (int) Numbers.KILOBYTE;
	protected long fileThreshold = DEFAULT_THRESHOLD;

	protected OutputStream out;
	protected OpenByteArrayOutputStream memory;
	protected File file;
	protected long length = 0;
	protected String fileExtension = null;

	protected AtomicInteger openInputStreamsCount = new AtomicInteger(0);
	protected ReentrantLock updateLock = new ReentrantLock();

	/**
	 * Creates a new instance that uses the given file threshold.
	 */
	public MemoryThresholdBuffer() {
		memory = new OpenByteArrayOutputStream();
		out = memory;
	}

	/**
	 * Creates a new instance that uses the given file threshold.
	 *
	 * @param fileThreshold
	 *            the number of bytes before the stream should switch to buffering to a file
	 */
	public MemoryThresholdBuffer(long fileThreshold) {
		this.fileThreshold = fileThreshold;
		memory = new OpenByteArrayOutputStream();
		out = memory;
	}

	/**
	 * Opens an {@link InputStream} on the data that is in this buffer. If the data is still in memory, a
	 * {@link ByteArrayInputStream} will be returned on the data read to far. If the data has exceeded the threshold, a
	 * {@link FileInputStream} (wrapped by a {@link BufferedInputStream}) will be returned. If deleteOnClose is true, this
	 * buffer will keep a counter on the number of {@link InputStream} opened and closed. When this counter reaches 0, the
	 * underlying file (if it exists) will be deleted.
	 *
	 * @param deleteOnClose
	 *            Indicates whether the closing of the returned {@link InputStream} should also delete the underlying file.
	 * @return An {@link InputStream} on the data written so far into the buffer.
	 * @throws IOException
	 *             Thrown if there is an error with creating a {@link FileInputStream}.
	 */
	public InputStream openInputStream(boolean deleteOnClose) throws IOException {
		if (file != null) {
			if (deleteOnClose) {
				openInputStreamsCount.incrementAndGet();
				return new BufferedInputStream(new FileInputStream(file)) {
					@Override
					public void close() throws IOException {
						try {
							super.close();
						} finally {
							decreaseInputStreamCount();
						}
					}
				};
			} else {
				return new BufferedInputStream(new FileInputStream(file));
			}
		} else {
			return new ByteArrayInputStream(memory.getBuffer(), 0, memory.getCount());
		}
	}

	private void decreaseInputStreamCount() {
		if (openInputStreamsCount.decrementAndGet() == 0) {
			delete();
		}
	}

	public long getLength() {
		return this.length;
	}

	@Override
	public void write(int b) throws IOException {
		update(1);
		out.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		update(len);
		out.write(b, off, len);
	}

	@Override
	public void close() throws IOException {
		if (out != null) {
			out.close();
			out = null;
		}
	}

	@Override
	public void flush() throws IOException {
		if (out != null) {
			out.flush();
		}
	}

	/**
	 * Checks if writing {@code len} bytes would go over threshold, and switches to file buffering if so.
	 */
	private void update(long len) throws IOException {

		updateLock.lock();
		try {

			if ((file == null) && (length + len > fileThreshold)) {

				boolean debug = logger.isDebugEnabled();

				String ext = fileExtension;
				if (!StringTools.isBlank(ext)) {
					if (!ext.startsWith(".")) {
						ext = "." + ext;
					}
				}

				File temp = File.createTempFile("MemoryThresholdOutputStream", ext);
				FileTools.deleteFileWhenOrphaned(temp);

				if (debug) {
					logger.debug("Reached the memory threshold of " + this.fileThreshold + ". Switching to file " + temp.getAbsolutePath());
				}

				OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(temp));
				fileOutputStream.write(memory.getBuffer(), 0, memory.getCount());
				fileOutputStream.flush();

				if (debug) {
					logger.debug("Wrote " + (length + len) + " bytes to the temporary file.");
				}

				// We've successfully transferred the data; switch to writing to file
				out = fileOutputStream;
				file = temp;
				memory = null;
			}

			length += len;

		} finally {
			updateLock.unlock();
		}

	}

	/**
	 * Returns the file holding the data (possibly null).
	 */
	public File getFile() {
		return file;
	}

	public void delete() {
		try {
			this.close();
		} catch (Exception e) {
			logger.debug("Error while trying to delete MemoryThresholdBuffer on delete()", e);
		}

		if (openInputStreamsCount.get() > 0) {
			throw new IllegalArgumentException(
					"There are open InputStreams (" + openInputStreamsCount.get() + "). Cannot delete MemoryThresholdBuffer.");
		}
		if ((this.file != null) && (this.file.exists())) {
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("Deleting temporary file " + this.file.getAbsolutePath());
				}
				this.file.delete();
			} catch (Exception e) {
				logger.debug("Could not delete temporary file " + this.file.getAbsolutePath(), e);
			}
		}
	}

	public boolean multipleReadsPossible() {
		return (file == null);
	}

	/**
	 * ByteArrayOutputStream that exposes its internals.
	 */
	private static class OpenByteArrayOutputStream extends ByteArrayOutputStream {
		byte[] getBuffer() {
			return buf;
		}

		int getCount() {
			return count;
		}
	}

	/**
	 * Set an (optional) file extension of the temporary buffer file
	 *
	 * @param fileExtension
	 *            The intended extension
	 */
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

}
