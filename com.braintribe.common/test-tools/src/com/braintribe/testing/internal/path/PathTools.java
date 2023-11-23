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
package com.braintribe.testing.internal.path;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.braintribe.common.Constants;
import com.braintribe.common.lcd.AssertionException;
import com.braintribe.common.lcd.UnknownEnumException;
import com.braintribe.utils.lcd.Arguments;

/**
 * ATTENTION: this class will be moved soon, which means the package name will change!<br>
 * <p>
 * Provides {@link Path} related tools
 *
 * @author michael.lafite
 */
public abstract class PathTools {

	protected PathTools() {
		// no instantiation required
	}

	/**
	 * Returns a new {@link PathBuilder} instance.
	 */
	PathBuilder newPathBuilder() {
		return PathBuilder.newInstance();
	}

	/**
	 * Checks whether the specified <code>path</code> exists.
	 */
	public static boolean exists(Path path) {
		Arguments.notNullWithName("path", path);
		File file = path.toFile();
		boolean result = file.exists();
		return result;
	}

	/**
	 * {@link PathType#get(Path) Gets} the {@link PathType type} of the <code>path</code>.
	 */
	public static PathType type(Path path) {
		return PathType.get(path);
	}

	/**
	 * Asserts that the <code>path</code> does (not) exist, as specified by <code>expectedExistence</code>.
	 *
	 * @throws AssertionException
	 *             if the assertion fails.
	 */
	public static void assertExistence(Path path, PathExistence expectedExistence) {
		boolean exists = exists(path);
		switch (expectedExistence) {
			case MustExist:
				if (!exists) {
					throw new AssertionException("Path " + path.toString() + " unexpectedly doesn't exist!");
				}
				break;
			case MustNotExist:
				if (exists) {
					throw new AssertionException("Path " + path.toString() + " unexpectedly exists!");
				}
				break;
			case MayExist:
				// anything is fine, we don't care
				break;
			default:
				throw new UnknownEnumException(expectedExistence);
		}
	}

	/**
	 * Asserts that the <code>path</code> exists and matches the <code>expectedType</code>. Delegates to {@link #assertType(Path, PathType, boolean)}.
	 */
	public static Path assertType(Path path, PathType expectedType) {
		return assertType(path, expectedType, true);
	}

	/**
	 * Asserts that the <code>path</code> matches the <code>expectedType</code> (but only if it exists). Delegates to
	 * {@link #assertType(Path, PathType, boolean)}.
	 */
	public static Path assertTypeIfExists(Path path, PathType expectedType) {
		return assertType(path, expectedType, false);
	}

	/**
	 * Asserts that the <code>path</code> matches the <code>expectedType</code>. Delegates to {@link #assertType(Path, PathType, PathExistence)}.
	 *
	 * @param mustExist
	 *            indicates whether the <code>path</code> must exist. If it doesn't exist and this flag is disabled, the assertion will succeed (for
	 *            any <code>expectedType</code>).
	 *
	 */
	public static Path assertType(Path path, PathType expectedType, boolean mustExist) {
		return assertType(path, expectedType, mustExist ? PathExistence.MustExist : PathExistence.MayExist);
	}

	/**
	 * Asserts that the <code>path</code> matches the <code>expectedType</code> and <code>expectedExistence</code>. For convenience the method returns
	 * the passed <code>path</code>.
	 *
	 * @throws AssertionException
	 *             if the assertion fails.
	 */
	public static Path assertType(Path path, PathType expectedType, PathExistence expectedExistence) {
		if (exists(path)) {
			if (expectedExistence.equals(PathExistence.MustNotExist)) {
				throw new AssertionException("Path " + path.toString() + " unexpectedly exists!");
			}

			PathType actualType = type(path);
			if (!actualType.equals(expectedType) && !expectedType.equals(PathType.Any)) {
				throw new AssertionException(
						"Path " + path.toString() + " points to a " + actualType + " although a " + expectedType + " was expected!");
			}
		} else if (expectedExistence.equals(PathExistence.MustExist)) {
			throw new AssertionException("Cannot assert type of path " + path.toString() + " since it (unexpectedly) doesn't exist.");
		}
		return path;
	}

	/**
	 * Returns the {@link #canonicalPath(String) canonical} <code>Path</code> to the temporary files directory.
	 */
	public static Path tempDir() {
		return canonicalPath(System.getProperty(Constants.SYSTEMPROPERTY_TEMP_DIR));
	}

	/**
	 * Returns the {@link #canonicalPath(String) canonical} <code>Path</code> to the working directory.
	 */
	public static Path workingDir() {
		return canonicalPath(".");
	}

	/**
	 * Returns the (absolute) {@link File#getCanonicalFile() canonical} path for the given <code>path</code>.
	 */
	public static Path canonicalPath(String path) {
		try {
			Path result = Paths.get(new File(path).getCanonicalPath());
			return result;
		} catch (IOException e) {
			throw new UncheckedIOException("Error while getting canonical path for path '" + path + "'!", e);
		}
	}

	/**
	 * Returns the (absolute) {@link File#getCanonicalFile() canonical} path for the given <code>path</code>.
	 */
	public static Path canonicalPath(Path path) {
		return canonicalPath(path.toString());
	}
}
