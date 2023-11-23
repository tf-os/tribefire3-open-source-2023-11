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
package com.braintribe.devrock.repolet.parser;

import java.io.File;
import java.io.StringReader;
import java.nio.file.Paths;

import com.braintribe.logging.Logger;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.stream.ReaderInputStream;

public interface ResourceParsers {
	Logger log = Logger.getLogger(ResourceParsers.class);

	static Resource parse(String expression) {
		return Resource.createTransient(() -> new ReaderInputStream(new StringReader(expression)));
	}

	static FileResource parseFileResource(String expression) {

		File file = Paths.get(expression).toFile();
		if (!file.exists()) {
			log.warn("Could not resolve file with path '" + expression + "'.\nCheck if file exists: " + file.getAbsolutePath());
		}
		FileResource resource = FileResource.T.create();
		resource.setPath(expression);
		return resource;
	}
}
