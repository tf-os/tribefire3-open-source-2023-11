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
package com.braintribe.model.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface FileResource extends Resource {

	EntityType<FileResource> T = EntityTypes.T(FileResource.class);

	String getPath();
	void setPath(String path);

	@Override
	default InputStream openStream() {
		String path = getPath();

		if (path == null || path.isEmpty()) {
			throw new IllegalStateException("path is not set");
		}

		Path p = Paths.get(path);

		InputStream stream;
		try {
			stream = Files.newInputStream(p);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		return stream;
	}
	
	@Override
	default void writeToStream(OutputStream outputStream) {
		byte buffer[] = new byte[64*1024];
		try (InputStream in = openStream()) {
			int read;

			while ((read = in.read(buffer)) != -1) {
				outputStream.write(buffer, 0, read);
			}

		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
