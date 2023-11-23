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
package com.braintribe.common.uncheckedcounterpartexceptions;

import java.io.File;

import com.braintribe.common.lcd.uncheckedcounterpartexceptions.UncheckedIOException;

/**
 * Unchecked counterpart of {@link java.io.FileNotFoundException}. It is thrown, because a <code>File</code> (unexpectedly) doesn't exist.
 *
 * @author michael.lafite
 *
 * @deprecated see {@link FileAlreadyExistsException}
 */
@Deprecated
public class FileNotFoundException extends UncheckedIOException {

	private static final long serialVersionUID = -8227441643028585016L;

	protected File file = null;

	public FileNotFoundException(final String message) {
		super(message);
	}

	public FileNotFoundException(final File file) {
		super("File " + file.getAbsolutePath() + " doesn't exist!");
		this.file = file;
	}

	public File getFile() {
		return this.file;
	}
}
