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

import static com.braintribe.utils.StringTools.prettyPrintBytesDecimal;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;

/**
 * <p>
 * The {@code RepeatableInputStream} is a {@link InputStream} wrapper which enables an otherwise non-repeatable
 * {@code InputStream} to be re-opened and consumed multiple times.
 *
 * <p>
 * This is achieved by backing up the data on the first read through a piped output/input stream connection.
 *
 * <p>
 * Once the {@code RepeatableInputStream} is read until end-of-file or explicitly closed, the streamed data can be once
 * again retrieved via {@link #reopen()} and {@link #reopen(long)}.
 *
 * <p>
 * In order to free the resources which enable such repetition, the method {@link #destroy()} must be called once the
 * data is no longer needed.
 *
 */
public class RepeatableInputStream extends InputStream {

	private static final Logger log = Logger.getLogger(RepeatableInputStream.class);

	// defaults
	protected static final ExecutorService DEFAULT_EXECUTOR = Executors.newCachedThreadPool();
	protected static final int DEFAULT_PIPE_SIZE = 16384;
	protected static final int DEFAULT_MEMORY_BUFFER_SIZE = MemoryThresholdBuffer.DEFAULT_THRESHOLD;

	// (later) configurable
	protected ExecutorService executor = DEFAULT_EXECUTOR;
	protected int pipeSize = DEFAULT_PIPE_SIZE;
	protected int memoryBufferSize = DEFAULT_MEMORY_BUFFER_SIZE;
	private boolean timeIo = log.isTraceEnabled(); // whether we time I/O shouldn't change in the lifetime of the
													// instance.

	// internals
	protected InputStream in;
	protected long pipeOutCount = 0;
	protected volatile long pipeInCount = 0;
	private OutputStream pipeOut;
	private PipedInputStream pipeIn;
	private Future<?> pipeControl;
	private boolean pipeClosed = false;
	private long totalReadTime = 0;
	private long totalWriteTime = 0;
	private volatile MemoryThresholdBuffer backup;
	private volatile State state = State.NEW;
	private final Object stateMonitor = new Object();
	private final String desc = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

	public RepeatableInputStream(InputStream in) {
		this.in = Objects.requireNonNull(in, "in must not be null");
	}

	/**
	 * <p>
	 * Returns an {@link InputStream} for subsequent consumption of the data already read on this
	 * {@link RepeatableInputStream}.
	 *
	 * <p>
	 * This method blocks indefinitely until the {@link RepeatableInputStream} is ready to be reopened, which means it was
	 * either closed or read until end-of-file.
	 *
	 * @return An {@link InputStream} for subsequent consumption of the data already consumed on this
	 *         {@link RepeatableInputStream}.
	 * @throws InterruptedException
	 *             If the thread is interrupted while this method is blocking.
	 */
	public InputStream reopen() throws InterruptedException {
		return reopen(0);
	}

	/**
	 * <p>
	 * Returns an {@link InputStream} for subsequent consumption of the data already read on this
	 * {@link RepeatableInputStream}.
	 *
	 * <p>
	 * This method blocks until the {@link RepeatableInputStream} is ready to be reopened, which means it was either closed
	 * or read until end-of-file.
	 *
	 * @param timeout
	 *            How long (in milliseconds) this method blocks waiting for the {@link RepeatableInputStream} to be ready
	 *            for reopening.
	 * @return An {@link InputStream} for subsequent consumption of the data already consumed on this
	 *         {@link RepeatableInputStream}.
	 * @throws InterruptedException
	 *             If the thread is interrupted while this method is blocking.
	 */
	public InputStream reopen(long timeout) throws InterruptedException {

		if (!pipeClosed) {
			throw new IllegalStateException("This stream must be fully consumed or closed before it can be reopened");
		}

		State currentState = state;

		if (currentState == State.NEW || currentState == State.DESTROYED) {
			throw new IllegalStateException("Cannot re-open. " + desc + " is " + currentState);
		}

		final long t = log.isDebugEnabled() ? System.currentTimeMillis() : 0;

		try {

			// We intentionally inspect the computation result on every call,
			// to provide the caller with a consistent outcome.

			if (timeout < 1) {
				while (true) {
					try {
						pipeControl.get(1, TimeUnit.SECONDS);
						break;
					} catch (TimeoutException e) {
						log.debug(() -> desc + "'s read(" + timeout + ") is still blocking after " + (System.currentTimeMillis() - t) + "ms");
						continue;
					}
				}
			} else {
				pipeControl.get(timeout, TimeUnit.MILLISECONDS);
			}

			log.trace(() -> desc + " blocked for " + (System.currentTimeMillis() - t) + "ms for the backup task to complete");

		} catch (ExecutionException e) {
			throw new IllegalStateException("Cannot re-open. " + desc + "'s backup failed", e.getCause());
		} catch (CancellationException e) {
			throw new IllegalStateException("Cannot re-open. " + desc + "'s backup was cancelled", e);
		} catch (TimeoutException e) {
			throw new IllegalStateException("Cannot re-open. " + desc + "'s backup hasn't finished after " + timeout + " ms", e);
		}

		try {

			return backup.openInputStream(false);

		} catch (Exception e) {

			synchronized (stateMonitor) {
				if (state == State.DESTROYED) {
					throw new IllegalStateException("Cannot re-open. " + desc + " is " + currentState, e);
				} else {
					throw Exceptions.unchecked(e, "Failed to re-open. " + desc + " in " + currentState + " state");
				}
			}

		}

	}

	public void destroy() {
		if (state != State.DESTROYED) {
			synchronized (stateMonitor) {
				if (state != State.DESTROYED) {

					state = State.DESTROYED;

					if (pipeControl != null) {
						try {
							pipeControl.cancel(true);
						} catch (Exception e) {
							log.error(desc + " failed to cancel the pipe thread due to " + e, e);
						}
					}

					if (backup != null) {
						try {
							backup.delete();
						} catch (Exception e) {
							log.error(desc + " failed to delete the backed up data due to " + e, e);
						}
					}

				}
			}
		}
	}

	@Override
	public int read() throws IOException {

		long t = timeIo ? System.currentTimeMillis() : 0;

		int b = in.read();

		if (timeIo) {
			totalReadTime += System.currentTimeMillis() - t;
		}

		if (!pipeClosed) { // To avoid further processing on subsequent read() calls after end-of-file has been reached.

			// We ensure the pipe is connected only after the first successful read to
			// avoid creating unnecessary pipes in case the delegate fails to deliver.
			ensureConnected();

			t = timeIo ? System.currentTimeMillis() : 0;

			if (b > -1) {
				pipeOut.write(b);
				pipeOutCount++;
			} else if (b == -1) {
				disconnect();
			}

			if (timeIo) {
				totalWriteTime += System.currentTimeMillis() - t;
			}

		}

		return b;

	}

	@Override
	public int read(byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException {

		if (len < 1) {
			return 0;
		}

		long t = timeIo ? System.currentTimeMillis() : 0;

		int r = in.read(b, off, len);

		if (timeIo) {
			totalReadTime += System.currentTimeMillis() - t;
		}

		if (!pipeClosed) { // To avoid further processing on subsequent read() calls after end-of-file has been reached.

			// We ensure the pipe is connected only after the first successful read to
			// avoid creating unnecessary pipes in case the delegate fails to deliver.
			ensureConnected();

			t = timeIo ? System.currentTimeMillis() : 0;

			if (r > 0) {
				pipeOut.write(b, off, r);
				pipeOutCount += r;
			} else if (r == -1) {
				disconnect();
			}

			if (timeIo) {
				totalWriteTime += System.currentTimeMillis() - t;
			}

		}

		return r;

	}

	@Override
	public long skip(long n) throws IOException {
		throw new IOException("Seek not supported");
	}

	@Override
	public int available() throws IOException {
		return in.available();
	}

	@Override
	public void close() throws IOException {

		IOException error = null;

		try {
			in.close();
		} catch (IOException e) {
			error = e;
		}

		if (pipeOut != null) {

			try {
				disconnect();
			} catch (IOException e) {
				if (error != null) {
					e.addSuppressed(error);
				}
				error = e;
			}
		}

		if (error != null) {
			throw error;
		}

	}

	@Override
	public void mark(int readlimit) {
		// Ignored.
	}

	@Override
	public void reset() throws IOException {
		throw new IOException("Mark not supported");
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public String toString() {
		return desc;
	}

	private void ensureConnected() throws IOException {
		switch (state) {
			case NEW:
				connect();
				break;
			case CLOSED:
			case DESTROYED:
				throw new IllegalStateException("Cannot read. The " + desc + " is " + state);
			default:
				return; // Already connected
		}
	}

	private void connect() throws IOException {
		synchronized (stateMonitor) {
			if (state == State.NEW) {
				pipeIn = new PipedInputStream(pipeSize);
				pipeOut = new BufferedOutputStream(new PipedOutputStream(pipeIn), pipeSize);
				state = State.CONNECTED;
				pipeControl = executor.submit(this::backup);
			}
		}
	}

	private void disconnect() throws IOException {
		pipeOut.close();
		pipeClosed = true;
	}

	private void backup() {

		backup = new MemoryThresholdBuffer(memoryBufferSize);

		long t = timeIo ? System.currentTimeMillis() : 0;

		long totalPipeRead = 0;
		long totalBackupWrite = 0;

		long ti = 0;

		try (InputStream in = pipeIn; OutputStream out = backup) {

			final byte[] buffer = new byte[pipeSize];

			int count;

			try {
				while (true) {

					ti = timeIo ? System.currentTimeMillis() : 0;

					count = in.read(buffer);

					if (timeIo) {
						totalPipeRead += System.currentTimeMillis() - ti;
					}

					if (count == -1) {
						break;
					}

					try {

						ti = timeIo ? System.currentTimeMillis() : 0;

						out.write(buffer, 0, count);

						totalBackupWrite += System.currentTimeMillis() - ti;

					} catch (IOException e) {
						throw new UncheckedIOException(
								"Backup write failed. Data transferred so far: " + pipeInCount + ". Current buffer size: " + count, e);
					}
					pipeInCount += count;

				}

			} catch (IOException e) {
				throw new UncheckedIOException("Pipe read failed. Transferred " + pipeInCount + "  of  " + pipeOutCount, e);
			}

		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		if (log.isTraceEnabled()) {
			log.trace(finalizationLog(t, totalPipeRead, totalBackupWrite));
		}

		// We care for the closed state if nobody already called destroy()
		if (state == State.CONNECTED) {
			synchronized (stateMonitor) {
				if (state == State.CONNECTED) {
					state = State.CLOSED;
					log.trace(() -> desc + " was set to " + State.CLOSED);
				}
			}
		}

	}

	private String finalizationLog(long backupStart, long totalPipeRead, long totalBackupWrite) {

		StringBuilder msg = new StringBuilder();

		msg.append("Backup finalized for ");
		msg.append(desc);
		msg.append(". Transferred ");
		msg.append(prettyPrintBytesDecimal(pipeOutCount));
		msg.append(" of ");
		msg.append(prettyPrintBytesDecimal(pipeInCount));

		if (timeIo) {
			msg.append(". Read speed: ").append(formatSpeed(pipeInCount, totalReadTime));
			msg.append(". Pipe write speed: ").append(formatSpeed(pipeInCount, totalWriteTime));
			msg.append(". Pipe read speed: ").append(formatSpeed(pipeInCount, totalPipeRead));
			msg.append(". Backup write speed: ").append(formatSpeed(pipeInCount, totalBackupWrite));
			msg.append(". Backup task:  ").append((System.currentTimeMillis() - backupStart)).append(" ms");
		}

		return msg.toString();

	}

	private static String formatSpeed(long bytes, long milliseconds) {
		if (milliseconds == 0) {
			return bytes + " bytes in < 1 ms";
		} else {
			double speed = bytes / milliseconds / 125;
			return String.format("%.2f Mbps (%s bytes in %s ms)", speed, bytes, milliseconds);
		}

	}

	/**
	 * <p>
	 * Represents the state of this InputStream regarding its piping.
	 */
	private static enum State {

		/**
		 * This state means that no pipe was established, as no read() call occurred or completed.
		 */
		NEW,

		/**
		 * In this state, the pipe was established after at least one successful read() call on the delegate {@link InputStream}
		 * occurred.
		 */
		CONNECTED,

		/**
		 * This state is triggered by the {@link RepeatableInputStream#close()} method, but it is only set if the pipe concluded
		 * the asynchronous data backup, meaning {@link RepeatableInputStream#reopen()} and
		 * {@link RepeatableInputStream#reopen(long)}) calls are possible.
		 */
		CLOSED,

		/**
		 * This state is set upon calling {@link RepeatableInputStream#destroy()}. In this state, resources are expected to be
		 * completely freed and no call whatsoever is valid on the {@link RepeatableInputStream}.
		 */
		DESTROYED

	}

}
