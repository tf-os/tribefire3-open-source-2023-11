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
package com.braintribe.utils.stream.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.ReferencingFileInputStream;
import com.braintribe.utils.stream.api.PipeStatus;
import com.braintribe.utils.stream.api.StreamPipe;

/**
 * The FileBackedPipe manages a pipe based on a temporary file that acts as buffer and allows for multiple {@link FileBackedPipe#openInputStream() input streams}
 * and a non blocking {@link #openOutputStream() output stream}. The temporary file used by this pipe is deleted when no further reference is hold to it or when
 * exiting the JVM. As references are considered the pipe itself and its streams.
 * 
 * A monitor {@link #writeMonitor object} is used to tightly couple write and read operations to avoid a trade-off between responsiveness and wasted cpu cycles.
 *  
 * @author Neidhart Orlich
 * @author Dirk Scheffler
 */
public class FileBackedPipe implements StreamPipe {
	private File file;
	private PipeStatus status = PipeStatus.feeding;
	private OutputStream outputStream;
	private final Object outputLock = new Object();
	private final Object writeMonitor = new Object();
	private int bytesWritten;
	private Throwable error;

	private static Executor executor;
	private final int autoBufferSize;
	
	public static synchronized Executor getExecutor() {
		if (executor == null) {
			executor = Executors.newCachedThreadPool();
		}

		return executor;
	}
	/**
	 * Creates a pipe that will use the name as a prefix for the temporary file that is created within this constructor.
	 */
	public FileBackedPipe(String name, int autoBufferSize) {
		this.autoBufferSize = autoBufferSize;
		
		try {
			file = Files.createTempFile(name, ".tmp").toFile();
			FileTools.deleteFileWhenOrphaned(file);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	
	@Override
	public PipeStatus getStatus() {
		return status;
	}
	
	/**
	 * Opens an input stream to the pipe backup. This method can be called multiple times and will always start a new input stream for the data.
	 */
	@Override
	public InputStream openInputStream() throws IOException {
		switch (status) {
			case completed: return new ReferencingFileInputStream(file);
			case feedError: throwError();
				// fall through will actually never happen because the throwError() will always throw
				//$FALL-THROUGH$
			case feeding: return new PipeInputStream();
			default:
				throw new IllegalStateException("unkown state: " + status);
		}
	}
	
	private void throwError() throws IOException {
		if (error instanceof IOException) {
			throw (IOException)error;
		}
		else if (error instanceof RuntimeException) {
			throw (RuntimeException)error;
		}
		else if (error instanceof Error) {
			throw (Error)error;
		}
		else 
			throw new UndeclaredThrowableException(error);
	}
	
	@Override
	public void feedFrom(InputStream in) {
		getExecutor().execute(() -> {
			try (OutputStream out = openOutputStream()) {
				IOTools.transferBytes(in, out);
			}
			catch (IOException e) {
				notifyError(e);
			}
		});
	}
	
	/**
	 * Opens or simply returns the already opened OutputStream. You should be aware to use it properly as this
	 * always returns the one and only OutputStream that pipe can have.
	 */
	@Override
	public OutputStream acquireOutputStream() {
		return openOutputStream(true);
	}
	
	/**
	 * Opens the output stream that is to be used to fill the pipe.
	 * 
	 * @throws IllegalStateException when this method is called more than once.
	 */
	@Override
	public OutputStream openOutputStream() {
		return openOutputStream(false);
	}
	
	@Override
	public boolean wasOutputStreamOpened() {
		return outputStream != null;
	}
	
	private OutputStream openOutputStream(boolean acquire) {
		synchronized (outputLock) {
			if (outputStream != null) {
				if (acquire)
					return outputStream;
				
				throw new IllegalStateException("OutputStream was already opened and can be opened only once");
			}
			
			outputStream = new PipeOutputStream();
			
			return outputStream;
		}
	}
	
	public void notifyError(Throwable e) {
		synchronized (writeMonitor) {
			
			this.status = PipeStatus.feedError;
			this.error = e;
			
			writeMonitor.notifyAll();
		}
	}
	
	/**
	 * This method registers in a synchronized way the bytes written so far and the status of completion and notifies
	 * waiting threads.
	 */
	private void notifyWriteProgress(int bytesWritten, boolean completed) {
		synchronized (writeMonitor) {
			
			FileBackedPipe.this.bytesWritten += bytesWritten;
			
			if (completed)
				this.status = PipeStatus.completed;
			
			writeMonitor.notifyAll();
		}
	}
	
	/**
	 * A delegating input stream to the buffer that will distinguish between the logical eof and the physical eof to block until further data
	 * from the output stream is available or the logical end is reached.
	 */
	private class PipeInputStream extends InputStream {
		private InputStream delegate;
		private long bytesRead;
		
		public PipeInputStream() {
			try {
				delegate = Files.newInputStream(file.toPath(), StandardOpenOption.READ);
				
				if (autoBufferSize > 0) {
					delegate = new BufferedInputStream(delegate, autoBufferSize);
				}
				
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		@Override
		public int read() throws IOException {
			while (true) {
				int ret = delegate.read();
				
				if (ret != -1) {
					bytesRead++;
					return ret;
				}
				else {
					if (waitForDataAndCheckLogicalEof())
						return -1;
				}
			}
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			while (true) {
				int ret = delegate.read(b, off, len);
				
				if (ret != -1) {
					bytesRead += ret;
					return ret;
				}
				else {
					if (waitForDataAndCheckLogicalEof())
						return -1;
				}
			}
			
		}

		/**
		 * @return true if logical eof is reached
		 */
		private boolean waitForDataAndCheckLogicalEof() throws IOException {
			synchronized (writeMonitor) {
				switch (status) {
					case completed:
						// logical eof?
						return bytesRead == bytesWritten;
						
					case feedError:
						// will actually always throw an error
						throwError();
						//$FALL-THROUGH$
						
					case feeding:
						// still something to read
						try {
							writeMonitor.wait();
						} catch (InterruptedException e) {
							throw new IllegalStateException("Unexpected InterruptionException", e);
						}
						break;
					
					default:
						throw new IllegalStateException("unkown state: " + status);
				}
			}
			
			return false;
		}
		
		@Override
		public int available() throws IOException {
			return delegate.available();
		}

		@Override
		public void close() throws IOException {
			delegate.close();
		}

		@Override
		public synchronized void mark(int readlimit) {
			throw new UnsupportedOperationException();
		}

		@Override
		public synchronized void reset() throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean markSupported() {
			return false;
		}
		
	}
	
	private class PipeOutputStream extends OutputStream {
		
		private OutputStream delegate;
		
		public PipeOutputStream() {
			try {
				delegate = Files.newOutputStream(file.toPath());
				
				if (autoBufferSize > 0) {
					delegate = new BufferedOutputStream(delegate, autoBufferSize);
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		@Override
		public void write(int b) throws IOException {
			delegate.write(b);
			delegate.flush();
			notifyWriteProgress(1, false);
		}

		@Override
		public void write(byte[] b) throws IOException {
			delegate.write(b);
			delegate.flush();
			notifyWriteProgress(b.length, false);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			delegate.write(b, off, len);
			delegate.flush();
			notifyWriteProgress(len, false);
		}

		@Override
		public void flush() throws IOException {
			// noop
		}

		@Override
		public void close() throws IOException {
			delegate.close();
			notifyWriteProgress(0, true);
		}
		
	}
	
	
}
