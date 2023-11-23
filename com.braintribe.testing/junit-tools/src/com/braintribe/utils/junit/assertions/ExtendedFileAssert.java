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
package com.braintribe.utils.junit.assertions;

import java.io.File;
import java.io.UncheckedIOException;

import org.fest.assertions.Assertions;
import org.fest.assertions.FileAssert;

import com.braintribe.utils.FileTools;

/**
 * An extension of {@link FileAssert} to provide further assertions not (yet) provided by FEST.
 *
 * @see #hasContent(String)
 * @see #hasContent(String, String)
 *
 *
 */
public class ExtendedFileAssert extends FileAssert {

	public ExtendedFileAssert(final File actual) {
		super(actual);
	}

	/**
	 * Asserts that the file content is as expected, shortcut for calling {@link #hasContent(String, String)} with <code>encoding=UTF-8</code>
	 */
	public ExtendedFileAssert hasContent(final String expected) {
		return hasContent(expected, "UTF-8");
	}

	/**
	 * Compares the content of the file under assertion to an expected String.
	 * <p/>
	 * The file-content is read using {@link FileTools#readStringFromFile(File, String)} with the specified encoding. The actual comparison of content
	 * is performed using {@link Assertions#assertThat(String)}, to re-use the diff provided in the failure-messages of that method.
	 */
	public ExtendedFileAssert hasContent(final String expected, final String encoding) {
		final String fileContent = readFileContent(encoding);
		Assertions.assertThat(fileContent).describedAs("File content differs from expected content").isEqualTo(expected);

		return (ExtendedFileAssert) this.myself;
	}

	private String readFileContent(final String encoding) {
		final String fileContent = "";
		try {
			return FileTools.readStringFromFile(this.actual, encoding);
		} catch (final UncheckedIOException e) {
			fail("Cannot check content, error while reading file '" + this.actual + "'.", e);
		}
		return fileContent;
	}
}
