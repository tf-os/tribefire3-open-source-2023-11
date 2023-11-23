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
 * An {@link UncheckedIOException} that is thrown, because a file (unexpectedly) already exists.
 *
 * @author michael.lafite
 *
 * @deprecated This seems to be too specific, and since UncheckedIOException is deprecated anyway, and the java equivalent has no String constructor,
 *             there doesn't seem to be an easy way to update this without causing some compatibility problem.
 */
@Deprecated
public class FileAlreadyExistsException extends UncheckedIOException {

	private static final long serialVersionUID = -2207912790179439754L;

	protected File file = null;

	public FileAlreadyExistsException(final String message) {
		super(message);
	}

	public FileAlreadyExistsException(final File file) {
		super("File " + file.getAbsolutePath() + " already exists!");
		this.file = file;
	}

	public File getFile() {
		return this.file;
	}
}
