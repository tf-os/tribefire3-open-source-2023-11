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
package com.braintribe.model.processing.resource.filesystem.path;

import java.nio.file.Path;
import java.util.function.Function;

import com.braintribe.model.resource.source.FileSystemSource;

/**
 * @author peter.gazdik
 */
public interface FsPathResolver {

	Path resolveDomainPath(String domainId);

	default Path resolveSourcePath(Path basePath, FileSystemSource source) {
		return resolveSourcePath(basePath, source.getPath());
	}

	Path resolveSourcePath(Path basePath, String sourcePath);

	default Path resolveSourcePathForDomain(String domainId, FileSystemSource source) {
		return resolveSourcePathForDomain(domainId, source.getPath());
	}

	default Path resolveSourcePathForDomain(String domainId, String sourcePath) {
		Path domainpath = resolveDomainPath(domainId);
		return resolveSourcePath(domainpath, sourcePath);
	}

	// ##################################################################
	// ## . . . . . . . . Resolver for a single domain . . . . . . . . ##
	// ##################################################################

	default Function<String, Path> pathResolverForDomain(String domainId) {
		return sourcePath -> resolveSourcePathForDomain(domainId, sourcePath);
	}

}
