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
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.function.Supplier;

import com.braintribe.logging.Logger;

/**
 * <p>
 * A wrapper {@link InputStream} which supports late binding of the delegate it wraps.
 *
 * <p>
 * Calls to {@code InputStream} methods on this class will block until another thread responsible for providing the
 * delegate invokes {@link #setDelegate(Supplier, boolean, boolean)}.
 *
 * <p>
 * If the delegate is set as incremental, read operations on this object might block on end-of-file reads from the
 * delegate {@code InputStream} until this object is notified by thread responsible for the delegate integrity that the
 * underlying data is available on its entirely (see {@link #markDelegateAsComplete(long)}). The method
 * {@link #isComplete()} can be called at any time to verify if the underlying data is fully available.
 *
 * <p>
 * Having the underlying data available on its entirely, this class might support re-opening {@link InputStream} for it
 * through {@link #reopen()} . Whether the underlying data supports repeated reads is determined upon the delegate
 * setting (see {@link #setDelegate(Supplier, boolean, boolean)}) and can be checked any time after that through
 * {@link #isReopenable()}.
 *
 * <p>
 * The methods available to set and update the status of the delegate ({@link #setDelegate(Supplier, boolean, boolean)},
 * {@link #markDelegateAsComplete(long)} and {@link #markDelegateAsInvalid(Throwable)}) are thread-safe, whereas the
 * {@code InputStream} methods are not.
 *
 */
public class DeferredInputStream extends InputStream {

	private static final Logger log = Logger.getLogger(DeferredInputStream.class);

	// defaults
	protected static final long DEFAULT_WAIT_INTERVAL = 2000L;
	protected static final long DEFAULT_MAX_WAIT_TIME = 300000L;
	protected static final long DEFAULT_EOF_RETRIES = 0L;
	protected static final long DEFAULT_EOF_RETRY_INTERVAL = 0L;

	// configurable
	protected long waitInterval;
	protected long maxWaitTime;
	protected long eofRetries;
	protected long eofRetryInterval;

	// internals
	protected volatile Supplier<InputStream> delegateSupplier;
	private InputStream delegate;
	protected volatile long consumedBytes = 0;
	protected volatile long expectedBytes = -1;
	protected volatile boolean closed = false;
	protected volatile boolean complete = false;
	protected boolean repeatable = false;
	protected Throwable completionError;
	protected Object monitor = new Object();
	protected Object initLock = new Object();
	protected final String desc = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

	/**
	 * <p>
	 * Creates a default {@code DeferredInputStream}.
	 */
	public DeferredInputStream() {
		this(DEFAULT_WAIT_INTERVAL, DEFAULT_MAX_WAIT_TIME);
	}

	/**
	 * <p>
	 * Creates a new {@code DeferredInputStream} with custom {@code waitInterval} and {@code maxWaitTime} values.
	 *
	 * @param waitInterval
	 *            The maximum amount of time (in milliseconds) this {@code DeferredInputStream} will block without checking
	 *            optional state flags that might result in the wait interruption.
	 * @param maxWaitTime
	 *            The maximum amount of time (in milliseconds) this {@code DeferredInputStream} will block on read
	 *            operations waiting for the delegate to be available.
	 */
	public DeferredInputStream(long waitInterval, long maxWaitTime) {
		this(waitInterval, maxWaitTime, DEFAULT_EOF_RETRIES, DEFAULT_EOF_RETRY_INTERVAL);
	}

	/**
	 * <p>
	 * Creates a new {@code DeferredInputStream} with custom {@code waitInterval}, {@code maxWaitTime}, {@code eofRetries}
	 * and {@code eofRetryInterval} values.
	 *
	 * @param waitInterval
	 *            The maximum amount of time (in milliseconds) this {@code DeferredInputStream} will block without checking
	 *            optional state flags that might result in the wait interruption.
	 * @param maxWaitTime
	 *            The maximum amount of time (in milliseconds) this {@code DeferredInputStream} will block on read
	 *            operations waiting for the delegate to be available.
	 * @param eofRetries
	 *            How many times the buffered read operation on this {@code DeferredInputStream} will evaluate an
	 *            end-of-file read from the delegate without returning to the caller.
	 * @param eofRetryInterval
	 *            The amount of time (in milliseconds) the read operation on this {@code DeferredInputStream} will block
	 *            before trying to read from the delegate again after an end-of-file read. For buffered reads, this value is
	 *            only relevant with {@code eofRetries} greater than 0.
	 *
	 */
	public DeferredInputStream(long waitInterval, long maxWaitTime, long eofRetries, long eofRetryInterval) {
		super();
		this.waitInterval = requireNonNegative(waitInterval, "waitInterval");
		this.maxWaitTime = requireNonNegative(maxWaitTime, "maxWaitTime");
		this.eofRetries = requireNonNegative(eofRetries, "eofRetries");
		this.eofRetryInterval = requireNonNegative(eofRetryInterval, "eofRetryInterval");
	}

	@Override
	public int read(byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException {

		if (this.delegate == null) {
			waitForDelegate();
		}

		int r = read(b, off, len, 0);

		consumedBytes += r;

		return r;

	}

	@Override
	public int read() throws IOException {

		if (this.delegate == null) {
			waitForDelegate();
		}

		while (true) {
			int b = delegate.read();

			if (b == -1) {
				if (evalEof()) {
					return b;
				} else {
					waitEofRetry();
					continue;
				}
			} else {
				consumedBytes++;
				return b;
			}

		}

	}

	@Override
	public long skip(long n) throws IOException {
		return delegate().skip(n);
	}

	@Override
	public int available() throws IOException {
		return delegate().available();
	}

	@Override
	public void close() throws IOException {

		InputStream delegateToClose;

		synchronized (initLock) {
			closed = true;
			delegateToClose = this.delegate;
		}

		log.trace(() -> desc + " is closing");

		if (delegateToClose != null) {
			delegateToClose.close();
			log.trace(() -> desc + "'s delegate was closed");
		} else {
			log.trace(() -> desc + "'s had no delegate to close");
		}

	}

	@Override
	public void mark(int readlimit) {
		delegate().mark(readlimit);
	}

	@Override
	public void reset() throws IOException {
		delegate().reset();
	}

	@Override
	public boolean markSupported() {
		return delegate().markSupported();
	}

	/**
	 * <p>
	 * Returns whether the data source owner has notified this {@code DeferredInputStream} that the underlying delegate
	 * represents the data on its entirely.
	 *
	 * @return Whether the data source owner has notified this {@code DeferredInputStream} that the underlying delegate
	 *         represents the data on its entirely.
	 */
	public boolean isComplete() {
		return evalComplete();
	}

	/**
	 * <p>
	 * Returns whether the data source supports repeated reads once its underlying data is fully available.
	 *
	 * @return Whether the data source supports repeated reads once its underlying data is fully available.
	 */
	public boolean isReopenable() {
		return repeatable;
	}

	/**
	 * <p>
	 * Re-opens an {@link InputStream} to the original underlying delegate data.
	 *
	 * @return An {@link InputStream} to the original underlying delegate data.
	 * @throws IOException
	 *             If the data source doesn't support re-opens; If the writing of the underlying data failed; If the
	 *             underlying data is not yet fully available.
	 */
	public InputStream reopen() throws IOException {

		if (!repeatable) {
			throw new IOException("Cannot re-open. The underlying source doesn't support this.");
		}

		if (completionError != null) {
			throw new IOException("Cannot re-open. The underlying source failed to be created: " + completionError, completionError);
		}

		if (!evalComplete()) {
			throw new IOException("Cannot re-open. The underlying source is not ready for this.");
		}

		return delegateSupplier.get();

	}

	/**
	 * <p>
	 * Provides the delegate {@code InputStream} supplier.
	 *
	 * <p>
	 * The {@code repeatable} flag indicates whether the {@link Supplier#get() get()} method can be called more than once in
	 * order to supply multiple {@code InputStream}(s) to the underlying data.
	 *
	 * <p>
	 * The {@code incremental} flag indicates whether the delegate data is expected to be incremented while this
	 * {@code DeferredInputStream} is consumed, case in which -1 reads from the delegate {@code InputStream} are only
	 * considered end-of-file after this object is notified by thread responsible for the delegate (through
	 * {@link #markDelegateAsComplete(long)}) that the underlying data is available on its entirely.
	 *
	 * <p>
	 * Invoking this method wakes up all threads that are waiting for the delegate once a {@code InputStream} is
	 * successfully obtained from the given {@code delegateSupplier}.
	 *
	 * <p>
	 * This method must be called only once in the lifetime of the {@code DeferredInputStream} instance. Invoking this
	 * method more than once throws an {@link IllegalStateException}.
	 *
	 * <p>
	 * Invoking this method for the first time after {@link #close()} was invoked does not initialize the delegate, but
	 * awakes, by raising a {@link IllegalStateException}, any thread blocking on {@link InputStream} methods waiting for
	 * the delegate.
	 *
	 * @param delegateSupplier
	 *            The {@code Supplier} responsible for providing the delegate {@code InputStream}
	 * @param repeatable
	 *            Whether the {@link Supplier#get() get()} method can be called more than once in order to supply multiple
	 *            {@code InputStream}(s) to the underlying data.
	 * @param incremental
	 *            Whether the delegate data is expected to be incremented while this {@link DeferredInputStream} is
	 *            consumed.
	 * @throws IllegalStateException
	 *             If the method is called more than once in the lifetime of the {@code DeferredInputStream} instance.
	 */
	public void setDelegate(Supplier<InputStream> delegateSupplier, boolean repeatable, boolean incremental) {

		log.trace(() -> desc + " will bind " + (!repeatable ? "non-" : "") + "repeatable " + (!incremental ? "non-" : "")
				+ "incremental delegate from " + delegateSupplier);

		Objects.requireNonNull(delegateSupplier, "delegateSupplier must not be null");

		boolean initialized = false;

		if (this.delegate == null) {
			synchronized (initLock) {
				if (this.delegate == null) {
					if (closed) {
						// By-passes any initialization if close() was called before a delegate was given.
						log.trace(() -> desc + " will ignore bind as it was closed");
						wakeUp();
						return;
					}
					if (!incremental) {
						this.complete = true;
					}
					if ((this.repeatable = repeatable)) {
						this.delegateSupplier = delegateSupplier;
					}
					this.delegate = delegateSupplier.get();
					initialized = true;
				}
			}
		}

		if (!initialized) {
			throw new IllegalStateException(this + " was already initialized");
		}

		log.trace(() -> desc + " initialized delegate: " + this.delegate);

		wakeUp();

	}

	/**
	 * <p>
	 * Notifies this {@code DeferredInputStream} that the incremental data source is now complete.
	 *
	 * @param bytes
	 *            The length of data on its entirely
	 */
	public void markDelegateAsComplete(long bytes) {
		log.trace(() -> desc + " got notified that source is complete at " + bytes + " bytes");
		this.expectedBytes = bytes;
	}

	/**
	 * <p>
	 * Notifies this {@code DeferredInputStream} that the source data failed to be completed.
	 */
	public void markDelegateAsInvalid(Throwable failure) {
		log.trace(() -> desc + " got notified about failure while writing the source: " + failure);
		this.completionError = failure;
		wakeUpQuietly();
	}

	/**
	 * <p>
	 * Returns the delegate {@code InputStream}, waiting until it is available.
	 *
	 * @return The delegate {@code InputStream} once it is available.
	 */
	protected InputStream delegate() {
		if (this.delegate == null) {
			try {
				waitForDelegate();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		return this.delegate;
	}

	/**
	 * <p>
	 * Waits until the delegate {@code InputStream} is provided to this {@code DeferredInputStream} (see
	 * {@link #setDelegate(Supplier, boolean, boolean)}).
	 *
	 * @param timeout
	 *            For how long (in milliseconds) this method should wait until a delegate is available. A non-positive value
	 *            means this method will block indefinitely until a delegate is available.
	 * @throws IOException
	 *             If the given positive {@code timeout} has elapsed before a delegate is provided to this
	 *             {@link DeferredInputStream}; If the thread is interrupted while waiting.
	 */
	protected void waitForDelegate(long timeout) throws IOException {

		if (this.delegate != null) {
			return;
		}

		timeout = timeout < 0 ? 0 : timeout;
		boolean waited = false;
		long start = System.currentTimeMillis();
		long remaining = timeout;

		synchronized (this.monitor) {
			while (timeout == 0 || remaining > 0) {

				if (closed) {
					throw new IOException("The stream was closed while waiting for the delegate");
				}

				if (completionError != null) {
					throw new IOException("The underlying source failed to be created: " + completionError, completionError);
				}

				if (this.delegate == null) {

					if (log.isTraceEnabled()) {
						log.trace(desc + "'s source is "
								+ (!waited ? "null on first check" : "still null after " + (System.currentTimeMillis() - start) + " ms"));
					}

					long wait = timeout == 0 ? waitInterval : remaining < waitInterval ? remaining : waitInterval;

					try {
						this.monitor.wait(wait);
					} catch (InterruptedException e) {
						throw new IOException("The thread was interrupted while waiting for the data be be available", e);
					}

					waited = true;

					if (timeout > 0) {
						remaining -= wait;
					}

				} else {
					return;
				}

			}
		}

		if (this.delegate == null) {
			throw new IOException("No delegate was notified after " + timeout + " ms");
		}

	}

	/**
	 * <p>
	 * Waits until the delegate {@code InputStream} is provided to this {@code DeferredInputStream} (see
	 * {@link #setDelegate(Supplier, boolean, boolean)}).
	 *
	 * <p>
	 * If the {@code maxWaitTime} has elapsed before a delegate is provided to this {@code DeferredInputStream}, an
	 * {@code IOException} is thrown;
	 *
	 * @throws IOException
	 *             If the {@code maxWaitTime} has elapsed before a delegate is provided to this {@code DeferredInputStream};
	 *             If the thread is interrupted while waiting.
	 */
	protected void waitForDelegate() throws IOException {
		waitForDelegate(this.maxWaitTime);
	}

	protected int read(byte b[], int off, int len, int eofRetry) throws IOException {

		int r = delegate.read(b, off, len);

		if (r == -1) {
			if (evalEof()) {
				return r;
			} else {
				if (eofRetries > 0 && eofRetry <= eofRetries) {
					waitEofRetry();
					return read(b, off, len, ++eofRetry);
				}
				return 0;
			}
		} else {
			return r;
		}

	}

	private void waitEofRetry() {
		if (eofRetryInterval > 0) {
			try {
				Thread.sleep(eofRetryInterval);
			} catch (InterruptedException e) {
				// Ignored
			}
		}
	}

	private boolean evalComplete() {
		if (!complete && expectedBytes != -1 && consumedBytes >= expectedBytes) {
			return complete = true;
		}
		return complete;
	}

	private boolean evalEof() throws IOException {
		if (completionError != null) {
			throw new IOException("The underlying data source failed to be completed: " + completionError, completionError);
		}
		return evalComplete();
	}

	private void wakeUp() {
		synchronized (monitor) {
			monitor.notifyAll();
		}
		log.trace(() -> desc + " woke up waiting threads");
	}

	private void wakeUpQuietly() {
		try {
			wakeUp();
		} catch (Exception e) {
			log.debug("Failed to wake possible waiting readers", e);
		}
	}

	private static long requireNonNegative(long n, String variable) {
		if (n < 0) {
			throw new IllegalArgumentException(variable + " must not be negative. Current value is " + n);
		}
		return n;
	}

}
