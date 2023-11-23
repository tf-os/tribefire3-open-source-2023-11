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
package com.braintribe.model.accessdeployment.orm.meta;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;

public interface NativeOrmFolder extends NativeOrm {

	EntityType<NativeOrmFolder> T = EntityTypes.T(NativeOrmFolder.class);

	String getFolderPath();
	void setFolderPath(String value);

	@Override
	default Set<Resource> resources() {

		String path = getFolderPath();

		if (path == null || path.isEmpty()) {
			throw new IllegalStateException("folder path is not set");
		}

		Path p = Paths.get(path);

		if (!Files.isDirectory(p)) {
			throw new IllegalStateException("folder path is not a directory: " + path);
		}

		Set<Resource> resources = new HashSet<>();

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(p)) {

			for (Path entry : stream) {

				String entryPath = entry.toString();

				FileResource resource = FileResource.T.create();
				resource.setPath(entryPath);

				resources.add(resource);

			}

		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		return resources;

	}

}
