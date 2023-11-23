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
package com.braintribe.utils.lcd;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.exception.Exceptions;
import com.braintribe.provider.Holder;

/**
 * Convenient wrapper for an instance that should be initialized lazily. This wrapper instance is configured with a
 * constructor and an optional "destructor". The underlying instance can be accesses via the {@link #get()} method,
 * where the first invocation creates the instance using the supplied constructor, and each subsequent invocation
 * returns the exact same instance.
 * <p>
 * The underlying instance can also be closed using the {@link #close()} method.
 * <p>
 * Newly created and closed wrapper is in an "uninitialized" state, and the first invocation of the {@link #get()}
 * method initializes it. Any subsequent {@linkplain #get()} invocation always returns the same instance. If we close
 * the wrapper, the next {@linkplain #get()} invocation returns a new instance.
 * <p>
 * For simply executing some initialization code without initializing any field use {@link LazyInitialization}.
 * <p>
 * This implementation is thread safe.
 *
 * @see LazyInitialization
 */
public class LazyInitialized<T> implements Supplier<T> {

	private volatile Supplier<T> itBox;

	private final Supplier<T> constructor;
	private final Consumer<T> destructor;
	private final ReentrantLock boxLock = new ReentrantLock();

	public LazyInitialized(Supplier<T> constructor) {
		this(constructor, null);
	}

	public LazyInitialized(Supplier<T> constructor, Consumer<T> destructor) {
		this.constructor = requireNonNull(constructor);
		this.destructor = destructor;
	}

	@Override
	public T get() {
		Supplier<T> currentIt = itBox;

		if (currentIt == null) {
			boxLock.lock();
			try {
				if (itBox == null) {
					itBox = Holder.of(constructor.get());
				}
				currentIt = itBox;
			} finally {
				boxLock.unlock();
			}
		}

		return currentIt.get();
	}

	public boolean isInitialized() {
		return itBox != null;
	}

	/**
	 * Closes the underlying instance if possible. If a "destructor" was given via
	 * {@link #LazyInitialized(Supplier, Consumer) constructor}, it will be invoked with the underlying instance, otherwise
	 * if the instance is {@link AutoCloseable}, its <code>close</code> method will be called.
	 * <p>
	 * Calling this in an uninitialized state (see {@link LazyInitialized class documentation}) has no effect.
	 */
	public void close() {
		if (itBox == null) {
			return;
		}

		syncClose();
	}

	private void syncClose() {

		boxLock.lock();
		try {
			if (itBox == null) {
				return;
			}

			T it = itBox.get();

			if (destructor != null) {
				destructor.accept(it);
			} else if (it instanceof AutoCloseable) {
				try {
					((AutoCloseable) it).close();
				} catch (Exception e) {
					throw Exceptions.unchecked(e);
				}
			}

			// This should only be nulled iff we have successfully closed our object, i.e. it's done as the last thing,
			// after close was done
			itBox = null;
		} finally {
			boxLock.unlock();
		}
	}

}
