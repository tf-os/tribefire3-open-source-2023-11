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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.function.Supplier;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.session.InputStreamProvider;

public class TemporaryFileInputStreamSupplier implements Supplier<InputStream>, InputStreamProvider, Closeable {

	private static final Logger log = Logger.getLogger(TemporaryFileInputStreamSupplier.class);
	private Path path;

	public TemporaryFileInputStreamSupplier(File file) {
		this(Objects.requireNonNull(file, "file must not be null").toPath());
	}

	public TemporaryFileInputStreamSupplier(Path path) {
		this.path = Objects.requireNonNull(path, "path must not be null");
	}

	@Override
	public InputStream openInputStream() {
		return get();
	}

	@Override
	public InputStream get() {
		try {
			return Files.newInputStream(path, StandardOpenOption.READ);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void close() throws IOException {
		if (path != null) {
			try {
				Files.deleteIfExists(path);
				log.info("Deleted temp file " + path);
			} catch (Throwable t) {
				log.error("Failed to delete temmporary file at \"" + path + "\" due to: " + t, t);
			}
		}
	}

}
