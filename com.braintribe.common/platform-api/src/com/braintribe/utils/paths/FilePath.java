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
package com.braintribe.utils.paths;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

/**
 * Immutable wrapper around a {@link File} which offers flexibility for deriving related Files (e.g. parents and children).
 *
 * @author peter.gazdik
 */
public interface FilePath {

	static FilePath of(File file) {
		return new FilePathImpl(file);
	}

	static FilePath of(String pathname) {
		return new FilePathImpl(pathname);
	}

	FilePath child(String... childSequence);

	FilePath parent();

	/**
	 * Returns the {@link FilePath} that denotes the n-th parent of this {@link FilePath}. 0 means current path, n-th parent means (n-1)-st parent of
	 * this path's parent.
	 */
	FilePath nthParent(int n);

	FilePath sybling(String siblingName);

	File toFile();
	URL toURL();
	Path toPath();
}
