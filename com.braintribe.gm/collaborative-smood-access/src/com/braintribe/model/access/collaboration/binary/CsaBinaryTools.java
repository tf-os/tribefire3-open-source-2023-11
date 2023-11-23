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
package com.braintribe.model.access.collaboration.binary;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author peter.gazdik
 */
public class CsaBinaryTools {

	public static String resolveRelativePath(String resourcePath, Path resourcesBaseAbsolutePath) {
		if (resourcePath == null)
			return null;

		Path path = Paths.get(resourcePath);
		if (path.isAbsolute()) {
			if (!path.startsWith(resourcesBaseAbsolutePath))
				return null;

			resourcePath = resourcesBaseAbsolutePath.relativize(path).toString();
		}

		return normalizePathSeparator(resourcePath);
	}

	public static String normalizePathSeparator(String path) {
		return path.replaceAll("\\\\", "/");
	}

}
