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
package com.braintribe.model.processing.webrpc.server.multipart;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.utils.stream.DeferredInputStream;

public class DeferredInputStreamSupplier implements Supplier<InputStream>, InputStreamProvider {

	private Object lock = new Object();
	private volatile boolean concluded = false;
	private Supplier<InputStream> initialInputStreamSupplier;
	private boolean repeatable;
	private Supplier<InputStream> reopenableInputStreamSupplier;
	private Throwable deferralFailure;
	private Set<DeferredInputStream> inputStreams = new HashSet<>();

	public DeferredInputStreamSupplier() {
	}

	public InputStream reopen() {

		if (reopenableInputStreamSupplier != null) {
			return reopenableInputStreamSupplier.get();
		}

		if (deferralFailure != null) {
			if (deferralFailure instanceof RuntimeException) {
				throw (RuntimeException) deferralFailure;
			}
			if (deferralFailure instanceof Error) {
				throw (Error) deferralFailure;
			}
			throw new IllegalStateException("Source backup failed: " + deferralFailure, deferralFailure);
		}

		throw new IllegalStateException(this + " is concluded but has no input stream supplier nor failure information");

	}

	@Override
	public InputStream openInputStream() {
		return get();
	}

	@Override
	public InputStream get() {

		if (!concluded) {
			synchronized (lock) {
				if (!concluded) {
					DeferredInputStream inputStream = new DeferredInputStream();
					if (this.initialInputStreamSupplier != null) {
						inputStream.setDelegate(this.initialInputStreamSupplier, this.repeatable, true);
					}
					inputStreams.add(inputStream);
					return inputStream;
				}
			}
		}

		return reopen();

	}

	public void bindDelegate(Supplier<InputStream> delegateSupplier, boolean repeatable) {

		Objects.requireNonNull(delegateSupplier, "delegateSupplier must not be null");

		synchronized (lock) {

			if (initialInputStreamSupplier != null) {
				throw new IllegalStateException(this + " was already initialized");
			}

			this.initialInputStreamSupplier = delegateSupplier;
			this.repeatable = repeatable;

			for (DeferredInputStream deferredInputStream : inputStreams) {
				deferredInputStream.setDelegate(initialInputStreamSupplier, repeatable, true);
			}

		}

	}

	public void markAsConcluded(Supplier<InputStream> inputStreamSupplier, long bytes) {

		Objects.requireNonNull(inputStreamSupplier, "inputStreamSupplier must not be null");

		synchronized (lock) {

			if (this.concluded) {
				throw new IllegalStateException(this + " was already marked as concluded");
			}

			this.reopenableInputStreamSupplier = inputStreamSupplier;

			this.concluded = true; // No more DeferredInputStreams are provided beyond this point

		}

		// Previously provided DeferredInputStreams are marked as concluded, no need for lock here.
		for (DeferredInputStream deferredInputStream : inputStreams) {
			deferredInputStream.markDelegateAsComplete(bytes);
		}

	}

	public void markAsFailed(Throwable backupFailure) {

		synchronized (lock) {

			if (this.concluded) {
				throw new IllegalStateException(this + " was already marked as concluded");
			}

			this.deferralFailure = Objects.requireNonNull(backupFailure, "backupFailure must not be null");

			this.concluded = true; // No more DeferredInputStreams are provided beyond this point

		}

		// Previously provided DeferredInputStreams are marked as concluded/failed, no need for lock here.
		for (DeferredInputStream deferredInputStream : inputStreams) {
			deferredInputStream.markDelegateAsInvalid(this.deferralFailure);
		}

	}

}
