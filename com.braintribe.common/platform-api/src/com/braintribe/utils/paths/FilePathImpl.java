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

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

import com.braintribe.utils.FileTools;

/**
 * @author peter.gazdik
 */
/* package */ class FilePathImpl implements FilePath {

	private final File file;

	public FilePathImpl(String pathname) {
		this(new File(pathname));
	}

	public FilePathImpl(File file) {
		this.file = file;
	}

	@Override
	public FilePath parent() {
		return new FilePathImpl(file.getParentFile());
	}

	@Override
	public FilePath nthParent(int n) {
		File result = file;
		for (int i = 0; i < n; i++) {
			result = file.getParentFile();
			if (result == null) {
				throw new IllegalArgumentException(
						"Cannot get " + n + "-th parent of file '" + file.getPath() + "', as there are only " + i + " parents on this path.");
			}
		}

		return new FilePathImpl(result);
	}

	@Override
	public FilePath child(String... childSequence) {
		File result = file;
		for (String child : requireNonNull(childSequence)) {
			result = new File(result, child);
		}

		return new FilePathImpl(result);
	}

	@Override
	public FilePath sybling(String siblingName) {
		return parent().child(siblingName);
	}

	@Override
	public File toFile() {
		return file;
	}

	@Override
	public URL toURL() {
		return FileTools.toURL(file);
	}

	@Override
	public Path toPath() {
		return file.toPath();
	}

}
