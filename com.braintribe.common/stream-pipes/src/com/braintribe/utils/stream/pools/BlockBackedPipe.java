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
package com.braintribe.utils.stream.pools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.ReferencingDelegateInputStream;
import com.braintribe.utils.stream.api.PipeStatus;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.blocks.Block;

/**
 * The BlockBackedPipe manages a pipe based on a sequence of {@link Block}s that act as buffer and allow for multiple
 * {@link BlockBackedPipe#openInputStream() input streams} and a non blocking {@link #openOutputStream() output stream}.
 * 
 * @author Neidhart Orlich
 * @author Dirk Scheffler
 * 
 * @see StreamPipe
 */
public class BlockBackedPipe implements StreamPipe {
	private final String name;
	private volatile long bytesWritten;
	private volatile PipeStatus status = PipeStatus.feeding;
	private volatile PipeOutputStream outputStream;
	private final ReentrantLock outputLock = new ReentrantLock();
	private final ReentrantLock writeLock = new ReentrantLock();
	private final Condition bytesAvailableForWriting = writeLock.newCondition();
	private volatile Throwable error;
	private final BlockSequence blockSequence;
	private final int autoBufferSize;
	private final ReadWriteLock safeClosingLock = new ReentrantReadWriteLock();

	private static Executor executor;
	private static ReentrantLock executorLock = new ReentrantLock();

	public BlockBackedPipe(BlockSequence blockSequence, String name, int autoBufferSize) {
		this.blockSequence = blockSequence;
		this.name = name;
		this.autoBufferSize = autoBufferSize;
	}

	public BlockBackedPipe(Supplier<Block> blockSupplier, String name, int autoBufferSize) {
		this(new BlockSequence(blockSupplier), name, autoBufferSize);
	}

	public static Executor getExecutor() {
		if (executor == null) {
			executorLock.lock();
			try {
				if (executor == null) {
					executor = Executors.newCachedThreadPool();
				}
			} finally {
				executorLock.unlock();
			}
		}

		return executor;
	}

	@Override
	public PipeStatus getStatus() {
		return status;
	}

	/**
	 * Opens a new {@link InputStream} to the data provided to the pipe via the {@link #openOutputStream() output stream}.
	 * Should all data that has currently been written into this pipe have been read but the output stream did not finish
	 * its writing and has not been closed, {@link InputStream#read()} will block until there is further data (or the output
	 * stream was closed).
	 * <p>
	 * This method can be called multiple times and will always return a new stream that starts from the beginning.
	 */
	@Override
	public InputStream openInputStream() throws IOException {
		Lock openInputStreamLock = safeClosingLock.readLock();
		openInputStreamLock.lock(); // Prevents that input streams can be opened while the pipe is being closed
		try {
			return _openInputStream();
		} finally {
			openInputStreamLock.unlock();
		}
	}

	private InputStream _openInputStream() throws IOException {
		switch (status) {
			case completed: {
				return new ReferencingDelegateInputStream(this, blockSequence.inputStream());
			}
			case feedError:
				throwError();
				// fall through will actually never happen because the throwError() will always throw
				// $FALL-THROUGH$
			case feeding: {
				PipeInputStream in = new PipeInputStream();
				// inputStreams.add(in);
				return in;
			}
			case closed: {
				throw new IllegalStateException("Can't open a new InputStream on an already closed StreamPipe: '" + name + "'.");
			}
			default:
				throw new IllegalStateException("unkown state: " + status);
		}
	}

	@Override
	public void close() {
		outputLock.lock();
		try {
			if (status == PipeStatus.feeding) {
				throw new IllegalStateException("Can't close StreamPipe '" + name + "' because its OutputStream is still open.");
			}

			Lock closingLock = safeClosingLock.writeLock(); // Make sure no new InputStreams are opened while the pipe is being closed
			try {
				closingLock.lock();
				blockSequence.close();
				status = PipeStatus.closed;
			} finally {
				closingLock.unlock();
			}
		} finally {
			outputLock.unlock();
		}
	}

	private void throwError() throws IOException {
		if (error instanceof IOException) {
			throw (IOException) error;
		} else if (error instanceof RuntimeException) {
			throw (RuntimeException) error;
		} else if (error instanceof Error) {
			throw (Error) error;
		} else
			throw new UndeclaredThrowableException(error);
	}

	@Override
	public void feedFrom(InputStream in) {
		getExecutor().execute(() -> {
			try (OutputStream out = openOutputStream()) {
				IOTools.transferBytes(in, out);
			} catch (IOException e) {
				notifyError(e);
			}
		});
	}

	@Override
	public OutputStream acquireOutputStream() {
		return openOutputStream(true);
	}

	@Override
	public OutputStream openOutputStream() {
		return openOutputStream(false);
	}

	@Override
	public boolean wasOutputStreamOpened() {
		return outputStream != null;
	}

	private OutputStream openOutputStream(boolean acquire) {
		outputLock.lock();
		try {
			if (status == PipeStatus.closed) {
				throw new IllegalStateException("Can't open OutputStream on already closed StreamPipe: '" + name + "'.");
			}

			if (outputStream != null) {
				if (acquire)
					return outputStream;

				throw new IllegalStateException("OutputStream was already opened and can be opened only once in StreamPipe: '" + name + "'.");
			}

			outputStream = new PipeOutputStream();

			return outputStream;
		} finally {
			outputLock.unlock();
		}
	}

	@Override
	public void notifyError(Throwable e) {
		writeLock.lock();
		try {

			this.status = PipeStatus.feedError;
			this.error = e;

			bytesAvailableForWriting.signalAll();

		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * This method registers in a synchronized way the bytes written so far and the status of completion and notifies
	 * waiting threads.
	 */
	private void notifyWriteProgress(int bytesWritten, boolean completed) {
		writeLock.lock();
		try {

			BlockBackedPipe.this.bytesWritten += bytesWritten;

			if (completed)
				this.status = PipeStatus.completed;

			bytesAvailableForWriting.signalAll();
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * A delegating input stream to the buffer that will distinguish between the logical eof and the physical eof to block
	 * until further data from the output stream is available or the logical end is reached.
	 */
	private class PipeInputStream extends InputStream {
		private final InputStream delegate;
		private long bytesRead;

		public PipeInputStream() {
			delegate = blockSequence.inputStream();
		}

		@Override
		public int read() throws IOException {
			while (true) {

				int ret = -1;
				ret = (bytesRead < bytesWritten) ? delegate.read() : -1;

				if (ret != -1) {
					bytesRead++;
					return ret;
				} else {
					if (waitForDataAndCheckLogicalEof())
						return -1;
				}
			}
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			while (true) {
				int maxLen = Math.min(len, (int) (bytesWritten - bytesRead));
				int ret = maxLen > 0 ? delegate.read(b, off, maxLen) : -1;

				if (ret != -1) {
					bytesRead += ret;
					return ret;
				} else {
					if (waitForDataAndCheckLogicalEof())
						return -1;
				}
			}

		}

		/**
		 * @return true if logical eof is reached
		 */
		private boolean waitForDataAndCheckLogicalEof() throws IOException {
			writeLock.lock();
			try {
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
							bytesAvailableForWriting.await();
						} catch (InterruptedException e) {
							throw new IllegalStateException("Unexpected InterruptedException", e);
						}
						break;

					default:
						throw new IllegalStateException("unkown state: " + status);
				}
			} finally {
				writeLock.unlock();
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
		public void mark(int readlimit) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void reset() throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean markSupported() {
			return false;
		}

	}

	private class PipeOutputStream extends OutputStream {

		private OutputStream delegate;
		private Block currentBlock;
		private ByteBuffer byteBuffer;
		private final byte[] oneByteBuffer = new byte[1];

		public PipeOutputStream() {
		}

		@Override
		public void write(int b) throws IOException {
			if (capacity() == 0) {
				nextBlock();
			}
			if (currentBlock.isAutoBuffered()) {
				oneByteBuffer[0] = (byte) b;
				writeThroughBuffer(oneByteBuffer, 0, 1);
			} else {
				delegate.write(b);
				delegate.flush();
				currentBlock.notifyBytesWritten(1);
				notifyWriteProgress(1, false);
			}
		}

		@Override
		public void write(byte[] b) throws IOException {
			this.write(b, 0, b.length);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			int bytesToWrite = len;
			while (bytesToWrite > 0) {
				if (capacity() == 0) {
					nextBlock();
				}

				len = Math.min(bytesToWrite, capacity());

				if (currentBlock.isAutoBuffered()) {
					writeThroughBuffer(b, off, len);
				} else {
					writeToDelegate(b, off, len);
				}

				bytesToWrite -= len;
				off += len;
			}
		}

		private int capacity() {
			if (currentBlock == null)
				return 0;

			if (currentBlock.getTreshold() < 0) {
				return Integer.MAX_VALUE;
			}

			return (int) Math.min(Integer.MAX_VALUE, currentBlock.getTreshold() - currentBlock.getBytesWritten());
		}

		private void nextBlock() throws IOException {
			if (status == PipeStatus.closed) {
				throw new IllegalStateException("Can't write to already closed StreamPipe: '" + name + "'.");
			}

			if (delegate != null) {
				delegate.close();
			}

			currentBlock = blockSequence.addNewBlock();

			if (currentBlock.isAutoBuffered()) {
				currentBlock.autoBufferInputStreams(autoBufferSize);
				byteBuffer = ByteBuffer.allocate(autoBufferSize);
			} else {
				byteBuffer = null;
			}

			delegate = currentBlock.openOutputStream();
		}

		@Override
		public void flush() throws IOException {
			if (byteBuffer != null) {
				flushBuffer();
			}
		}

		@Override
		public void close() throws IOException {
			flush();
			byteBuffer = null;

			if (delegate != null) {
				delegate.close();
				delegate = null;
				currentBlock = null;
			}
			notifyWriteProgress(0, true);
		}

		private void writeThroughBuffer(byte[] b, int off, int len) throws IOException {
			while (len > 0) {
				int remainingBytesInBuffer = byteBuffer.limit() - byteBuffer.position();
				int bytesToWrite = Math.min(len, remainingBytesInBuffer);
				byteBuffer.put(b, off, bytesToWrite);

				if (remainingBytesInBuffer <= len) {
					flushBuffer();
				}

				len -= bytesToWrite;
				off += bytesToWrite;
			}
		}

		private void writeToDelegate(byte[] b, int off, int len) throws IOException {
			if (len < 0) {
				// TODO: Remove this then redundant check after bug was found (CORETS-753)
				throw new IllegalArgumentException(
						"Attempt to write a negative amount of bytes: len=" + len + ", off=" + off + ", pipestatus=" + status);
			}

			delegate.write(b, off, len);
			delegate.flush();
			currentBlock.notifyBytesWritten(len);
			notifyWriteProgress(len, false);
		}

		private void flushBuffer() throws IOException {
			writeToDelegate(byteBuffer.array(), 0, byteBuffer.position());

			// This cast is necessary for Java backwards compatibility
			// See:
			// https://stackoverflow.com/questions/61267495/exception-in-thread-main-java-lang-nosuchmethoderror-java-nio-bytebuffer-flip
			((Buffer) byteBuffer).clear();
		}

	}

}
