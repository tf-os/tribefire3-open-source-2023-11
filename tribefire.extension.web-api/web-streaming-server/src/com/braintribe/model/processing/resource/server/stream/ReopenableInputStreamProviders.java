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
package com.braintribe.model.processing.resource.server.stream;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.function.Supplier;

import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.utils.stream.MemoryThresholdBuffer;
import com.braintribe.utils.stream.RepeatableInputStream;
import com.braintribe.utils.stream.WriteOnReadInputStream;

public class ReopenableInputStreamProviders {

	public static ReopenableInputStreamProvider create(Supplier<InputStream> originalSupplier) {
		return createSync(originalSupplier);
	}

	/**
	 * <p>
	 * Returns a {@code ReopenableInputStreamProvider} which processes the data read from the provided
	 * {@code InputStream} by {@code originalSupplier} so that it is also synchronously written to a backup storage.
	 */
	public static ReopenableInputStreamProvider createSync(Supplier<InputStream> originalSupplier) {
		return new ReopenableInputStreamProviderSync(originalSupplier);
	}

	/**
	 * <p>
	 * Returns a {@code ReopenableInputStreamProvider} which processes the data read from the provided
	 * {@code InputStream} by {@code originalSupplier} so that it is also written to a pipe, which writes it to a backup
	 * storage in another thread.
	 */
	public static ReopenableInputStreamProvider createAsync(Supplier<InputStream> originalSupplier) {
		return new ReopenableInputStreamProviderAsync(originalSupplier);
	}

	public static abstract class ReopenableInputStreamProvider implements InputStreamProvider, Supplier<InputStream>, Closeable {

		Supplier<InputStream> source;

		public ReopenableInputStreamProvider(Supplier<InputStream> originalSupplier) {
			this.source = Objects.requireNonNull(originalSupplier, "originalSupplier must not be null");
		}

		@Override
		public InputStream get() {
			try {
				return openInputStream();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

	}

	public static class ReopenableInputStreamProviderAsync extends ReopenableInputStreamProvider {

		private RepeatableInputStream buffer;

		protected ReopenableInputStreamProviderAsync(Supplier<InputStream> originalSupplier) {
			super(originalSupplier);
		}

		@Override
		public InputStream openInputStream() throws IOException {
			if (buffer == null) {
				buffer = new RepeatableInputStream(source.get());
				return buffer;
			} else {
				try {
					return buffer.reopen();
				} catch (InterruptedException e) {
					throw new IOException("Interrupted while waiting reopen to be available", e);
				}
			}
		}

		@Override
		public void close() throws IOException {
			if (buffer != null) {
				buffer.destroy();
			}
		}

	}

	public static class ReopenableInputStreamProviderSync extends ReopenableInputStreamProvider {

		private MemoryThresholdBuffer buffer;

		protected ReopenableInputStreamProviderSync(Supplier<InputStream> originalSupplier) {
			super(originalSupplier);
		}

		@Override
		public InputStream openInputStream() throws IOException {
			if (buffer == null) {
				buffer = new MemoryThresholdBuffer();
				WriteOnReadInputStream inputStream = new WriteOnReadInputStream(source.get(), buffer, true, true);
				return inputStream;
			} else {
				return buffer.openInputStream(false);
			}
		}

		@Override
		public void close() throws IOException {
			if (buffer != null) {
				buffer.delete();
			}
		}

	}

}
