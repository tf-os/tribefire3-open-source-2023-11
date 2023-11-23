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
package com.braintribe.testing.junit.assertions.assertj.core.api;

import java.io.File;

import org.assertj.core.api.AbstractFileAssert;
import org.assertj.core.internal.Objects;

import com.braintribe.logging.Logger;
import com.braintribe.utils.FileTools;

/**
 * Provides custom {@link File} assertions.
 *
 * @author michael.lafite
 */
public class ExtendedFileAssert extends AbstractFileAssert<ExtendedFileAssert> implements SharedAssert<ExtendedFileAssert, File> {

	@SuppressWarnings("unused") // may be used by SharedAssert methods via reflection
	private static final Logger logger = Logger.getLogger(ExtendedFileAssert.class);

	public ExtendedFileAssert(File actual) {
		super(actual, ExtendedFileAssert.class);
	}

	/**
	 * Asserts that the actual {@link File} has the expected <code>ancestor</code>, which means its {@link File#getParentFile() parent} (or parent of
	 * that parent ...) is the specified <code>ancestor</code>.
	 */
	public ExtendedFileAssert hasAncestor(File ancestor) {
		Objects.instance().assertNotNull(info, actual);
		File currentAncestor = actual.getParentFile();
		boolean ancestorFound = false;
		while (currentAncestor != null) {
			if (currentAncestor.equals(ancestor)) {
				ancestorFound = true;
				break;
			}
			currentAncestor = currentAncestor.getParentFile();
		}

		if (!ancestorFound) {
			failWithMessage("File " + toString(actual.getAbsolutePath()) + " doesn't have ancestor " + toString(ancestor.getAbsolutePath()) + ".");
		}
		return this;
	}

	/**
	 * Asserts that the actual {@link File} {@link File#getPath() path} matches the path of the specified <code>file</code>.
	 */
	public ExtendedFileAssert hasSamePathAs(File file) {
		Objects.instance().assertNotNull(info, actual);
		if (!actual.getPath().equals(file.getPath())) {
			failWithMessage("Path " + toString(actual.getPath()) + " doesn't match expected path " + toString(file.getPath()) + ".");
		}
		return this;
	}

	/**
	 * Asserts that the actual {@link File} {@link File#getPath() path} does not match the path of the specified <code>file</code>.
	 */
	public ExtendedFileAssert doesNotHaveSamePathAs(File file) {
		Objects.instance().assertNotNull(info, actual);
		if (actual.getPath().equals(file.getPath())) {
			failWithMessage("Path " + toString(actual.getPath()) + " (unexpectedly) matches path " + toString(file.getPath()) + ".");
		}
		return this;
	}

	/**
	 * Asserts that the actual {@link File} {@link File#getAbsolutePath() absolute path} matches the absolute path of the specified <code>file</code>.
	 */
	public ExtendedFileAssert hasSameAbsolutePathAs(File file) {
		Objects.instance().assertNotNull(info, actual);
		if (!actual.getAbsolutePath().equals(file.getAbsolutePath())) {
			failWithMessage("Absolute path " + toString(actual.getAbsolutePath()) + " doesn't match expected absolute path "
					+ toString(file.getAbsolutePath()) + ".");
		}
		return this;
	}

	/**
	 * Asserts that the actual {@link File} {@link File#getPath() absolute path} does not match the absolute path of the specified <code>file</code>.
	 */
	public ExtendedFileAssert doesNotHaveSameAbsolutePathAs(File file) {
		Objects.instance().assertNotNull(info, actual);
		if (actual.getAbsolutePath().equals(file.getAbsolutePath())) {
			failWithMessage("Absolute path " + toString(actual.getAbsolutePath()) + " (unexpectedly) matches absolute path "
					+ toString(file.getAbsolutePath()) + ".");
		}
		return this;
	}

	/**
	 * Asserts that the actual {@link File} {@link File#getCanonicalPath() canonical path} matches the canonical path of the specified
	 * <code>file</code>.
	 */
	public ExtendedFileAssert hasSameCanonicalPathAs(File file) {
		Objects.instance().assertNotNull(info, actual);

		String actualCanonicalPath = FileTools.getCanonicalPath(actual);
		String expectedCanonicalPath = FileTools.getCanonicalPath(file);

		if (!actualCanonicalPath.equals(expectedCanonicalPath)) {
			failWithMessage(
					"Canonical path " + toString(actualCanonicalPath) + " doesn't match expected path " + toString(expectedCanonicalPath) + ".");
		}
		return this;
	}

	/**
	 * Asserts that the actual {@link File} {@link File#getCanonicalPath() canonical path} does not match the canonical path of the specified
	 * <code>file</code>.
	 */
	public ExtendedFileAssert doesNotHaveSameCanonicalPathAs(File file) {
		Objects.instance().assertNotNull(info, actual);

		String actualCanonicalPath = FileTools.getCanonicalPath(actual);
		String expectedCanonicalPath = FileTools.getCanonicalPath(file);

		if (actualCanonicalPath.equals(expectedCanonicalPath)) {
			failWithMessage("Canonical path " + toString(actualCanonicalPath) + " (unexpectedly) matches match path "
					+ toString(expectedCanonicalPath) + ".");
		}
		return this;
	}

}
