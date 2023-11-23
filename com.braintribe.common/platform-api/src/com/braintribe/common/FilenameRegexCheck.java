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
package com.braintribe.common;

import java.io.File;

import com.braintribe.common.lcd.GenericCheck;
import com.braintribe.utils.lcd.Arguments;

/**
 * A regular expression {@link GenericCheck check} that checks file names.
 *
 * @author michael.lafite
 */
public class FilenameRegexCheck extends AbstractRegexCheck<File> {

	public FilenameRegexCheck() {
		// nothing to do
	}

	public FilenameRegexCheck(final String includeRegex) {
		super(includeRegex);
	}

	public FilenameRegexCheck(final String includeRegex, final String excludeRegex) {
		super(includeRegex, excludeRegex);
	}

	@Override
	protected String getString(final File file) {
		Arguments.notNull(file, "Cannot check file, because it is null!");
		return file.getName();
	}

}
