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
package com.braintribe.testing.tools;

import java.io.File;
import java.io.UncheckedIOException;

import com.braintribe.testing.internal.path.PathBuilder;
import com.braintribe.testing.internal.path.PathTools;
import com.braintribe.testing.internal.path.PathType;

/**
 * This class provides convenience methods that can be used in tests.
 *
 * @author michael.lafite
 */
public abstract class TestTools {

	private TestTools() {
		// nothing to do
	}

	/**
	 * Creates a new directory in temporary files directory. Same as {@link #newTempDir(boolean)} with deletion on exit enabled.
	 */
	public static File newTempDir() throws UncheckedIOException {
		return newTempDir(true);
	}

	/**
	 * Creates a new directory in temporary files directory. Same as {@link #newTempDir(String, boolean)} with relative path <code>Test</code>. Note
	 * that deletion only works, if the directory is empty. Therefore all its child files and directories also must be deleted on exit.
	 */
	public static File newTempDir(boolean deleteOnExit) throws UncheckedIOException {
		return newTempDir("Test", deleteOnExit);
	}

	/**
	 * Creates a new directory in temporary files directory. Same as {@link #newTempDirForClass(Class, boolean)} with deletion on exit enabled.
	 */
	public static File newTempDirForClass(Class<?> testClass) throws UncheckedIOException {
		return newTempDirForClass(testClass, true);
	}

	/**
	 * Creates a new directory in temporary files directory. Same as {@link #newTempDir(String, boolean)} using the simple name of the passed
	 * <code>testClass</code> as relative path.
	 */
	public static File newTempDirForClass(Class<?> testClass, boolean deleteOnExit) throws UncheckedIOException {
		return newTempDir(testClass.getSimpleName(), deleteOnExit);
	}

	/**
	 * Creates a new directory in temporary files directory. Same as {@link #newTempDir(String, boolean)} with deletion on exit enabled.
	 */
	public static File newTempDir(String relativePathPrefix) throws UncheckedIOException {
		return newTempDir(relativePathPrefix, true);
	}

	/**
	 * Creates a new directory in temporary files directory. Same as {@link #newTempDir(String, boolean, boolean)} with timestamp suffix enabled.
	 */
	public static File newTempDir(String relativePath, boolean deleteOnExit) throws UncheckedIOException {
		return newTempDir(relativePath, deleteOnExit, true);
	}

	/**
	 * Creates a new directory in temporary files directory. Same as {@link #newFileOrDir(File, String, boolean, boolean, PathType)} with parent
	 * directory {@link PathTools#tempDir() temp dir} and path type {@link PathType#Directory}.
	 */
	public static File newTempDir(String relativePath, boolean deleteOnExit, boolean addTimestampSuffix) throws UncheckedIOException {
		return newFileOrDir(PathTools.tempDir().toFile(), relativePath, deleteOnExit, addTimestampSuffix, PathType.Directory);
	}

	/**
	 * Creates a new directory in the given <code>parentDirectory</code>. Same as {@link #newDir(File, boolean)} with deletion on exit enabled.
	 */
	public static File newDir(File parentDirectory) throws UncheckedIOException {
		return newDir(parentDirectory, true);
	}

	/**
	 * Creates a new directory in temporary files directory. Same as {@link #newDir(File, String, boolean)} with relative path <code>Test</code>. Note
	 * that deletion only works, if the directory is empty. Therefore all its child files and directories also must be deleted on exit.
	 */
	public static File newDir(File parentDirectory, boolean deleteOnExit) throws UncheckedIOException {
		return newDir(parentDirectory, "Test", deleteOnExit);
	}

	/**
	 * Creates a new directory in temporary files directory. Same as {@link #newDirForClass(File, Class, boolean)} with deletion on exit enabled.
	 */
	public static File newDirForClass(File parentDirectory, Class<?> testClass) throws UncheckedIOException {
		return newDirForClass(parentDirectory, testClass, true);
	}

	/**
	 * Creates a new directory in temporary files directory. Same as {@link #newDir(File, String, boolean)} using the simple name of the passed
	 * <code>testClass</code> as relative path.
	 */
	public static File newDirForClass(File parentDirectory, Class<?> testClass, boolean deleteOnExit) throws UncheckedIOException {
		return newDir(parentDirectory, testClass.getSimpleName(), deleteOnExit);
	}

	/**
	 * Creates a new directory in temporary files directory. Same as {@link #newDir(File, String, boolean)} with deletion on exit enabled.
	 */
	public static File newDir(File parentDirectory, String relativePathPrefix) throws UncheckedIOException {
		return newDir(parentDirectory, relativePathPrefix, true);
	}

	/**
	 * Creates a new directory in temporary files directory. Same as {@link #newDir(File, String, boolean, boolean)} with timestamp suffix enabled.
	 */
	public static File newDir(File parentDirectory, String relativePath, boolean deleteOnExit) throws UncheckedIOException {
		return newDir(parentDirectory, relativePath, deleteOnExit, true);
	}

	/**
	 * Creates a new directory in the given <code>parentDirectory</code>. Same as {@link #newFileOrDir(File, String, boolean, boolean, PathType)} with
	 * path type {@link PathType#Directory}.
	 */
	public static File newDir(File parentDirectory, String relativePath, boolean deleteOnExit, boolean addTimestampSuffix)
			throws UncheckedIOException {
		return newFileOrDir(parentDirectory, relativePath, deleteOnExit, addTimestampSuffix, PathType.Directory);
	}

	/**
	 * Creates a new file in temporary files directory. Same as {@link #newTempFile(boolean)} with deletion on exit enabled.
	 */
	public static File newTempFile() throws UncheckedIOException {
		return newTempFile(true);
	}

	/**
	 * Creates a new file in temporary files directory. Same as {@link #newTempFile(String, boolean)} with relative path <code>Test</code>.
	 */
	public static File newTempFile(boolean deleteOnExit) throws UncheckedIOException {
		return newTempFile("Test", deleteOnExit);
	}

	/**
	 * Creates a new file in temporary files directory. Same as {@link #newTempFileForClass(Class, boolean)} with deletion on exit enabled.
	 */
	public static File newTempFileForClass(Class<?> testClass) throws UncheckedIOException {
		return newTempFileForClass(testClass, true);
	}

	/**
	 * Creates a new file in temporary files directory. Same as {@link #newTempFileForClass(Class, String, boolean)} with deletion on exit enabled.
	 */
	public static File newTempFileForClass(Class<?> testClass, String extension) throws UncheckedIOException {
		return newTempFileForClass(testClass, extension, true);
	}

	/**
	 * Creates a new file in temporary files directory. Same as {@link #newTempFileForClass(Class, String, boolean)} without an extension.
	 */
	public static File newTempFileForClass(Class<?> testClass, boolean deleteOnExit) throws UncheckedIOException {
		return newTempFileForClass(testClass, null, deleteOnExit);
	}

	/**
	 * Creates a new file in temporary files directory. Same as {@link #newTempFile(String, boolean)} using the simple name of the passed
	 * <code>testClass</code> and the specified (optional) <code>extension</code> as relative path.
	 * <p>
	 * If more options are needed, see {@link PathBuilder}.
	 */
	public static File newTempFileForClass(Class<?> testClass, String extension, boolean deleteOnExit) throws UncheckedIOException {
		String extensionToAdd = "";
		if (extension != null) {
			if (!extension.startsWith(".")) {
				extensionToAdd += ".";
			}
			extensionToAdd += extension;
		}

		return newTempFile(testClass.getSimpleName() + extensionToAdd, deleteOnExit);
	}

	/**
	 * Creates a new file in temporary files directory. Same as {@link #newTempFile(String, boolean)} with deletion on exit enabled.
	 */
	public static File newTempFile(String relativePath) throws UncheckedIOException {
		return newTempFile(relativePath, true);
	}

	/**
	 * Creates a new file in temporary files directory. Same as {@link #newTempFile(String, boolean, boolean)} with timestamp suffix enabled.
	 */
	public static File newTempFile(String relativePath, boolean deleteOnExit) throws UncheckedIOException {
		return newTempFile(relativePath, deleteOnExit, true);
	}

	/**
	 * Creates a new file in temporary files directory. Same as {@link #newFileOrDir(File, String, boolean, boolean, PathType)} with parent directory
	 * {@link PathTools#tempDir() temp dir} and path type {@link PathType#File}.
	 */
	public static File newTempFile(String relativePath, boolean deleteOnExit, boolean addTimestampSuffix) throws UncheckedIOException {
		return newFileOrDir(PathTools.tempDir().toFile(), relativePath, deleteOnExit, addTimestampSuffix, PathType.File);
	}

	/**
	 * Creates a new file in the given <code>parentDirectory</code>. Same as {@link #newFile(File, boolean)} with deletion on exit enabled.
	 */
	public static File newFile(File parentDirectory) throws UncheckedIOException {
		return newFile(parentDirectory, true);
	}

	/**
	 * Creates a new file in the given <code>parentDirectory</code>. Same as {@link #newFile(File, String, boolean)} with relative path
	 * <code>Test</code>.
	 */
	public static File newFile(File parentDirectory, boolean deleteOnExit) throws UncheckedIOException {
		return newFile(parentDirectory, "Test", deleteOnExit);
	}

	/**
	 * Creates a new file in the given <code>parentDirectory</code>. Same as {@link #newFileForClass(File, Class, boolean)} with deletion on exit
	 * enabled.
	 */
	public static File newFileForClass(File parentDirectory, Class<?> testClass) throws UncheckedIOException {
		return newFileForClass(parentDirectory, testClass, true);
	}

	/**
	 * Creates a new file in the given <code>parentDirectory</code>. Same as {@link #newFileForClass(File, Class, String, boolean)} with deletion on
	 * exit enabled.
	 */
	public static File newFileForClass(File parentDirectory, Class<?> testClass, String extension) throws UncheckedIOException {
		return newFileForClass(parentDirectory, testClass, extension, true);
	}

	/**
	 * Creates a new file in the given <code>parentDirectory</code>. Same as {@link #newFileForClass(File, Class, String, boolean)} without an
	 * extension.
	 */
	public static File newFileForClass(File parentDirectory, Class<?> testClass, boolean deleteOnExit) throws UncheckedIOException {
		return newFileForClass(parentDirectory, testClass, null, deleteOnExit);
	}

	/**
	 * Creates a new file in the given <code>parentDirectory</code>. Same as {@link #newFile(File, String, boolean)} using the simple name of the
	 * passed <code>testClass</code> and the specified (optional) <code>extension</code> as relative path.
	 */
	public static File newFileForClass(File parentDirectory, Class<?> testClass, String extension, boolean deleteOnExit) throws UncheckedIOException {
		String extensionToAdd = "";
		if (extension != null) {
			if (!extension.startsWith(".")) {
				extensionToAdd += ".";
			}
			extensionToAdd += extension;
		}

		return newFile(parentDirectory, testClass.getSimpleName() + extensionToAdd, deleteOnExit);
	}

	/**
	 * Creates a new file in the given <code>parentDirectory</code>. Same as {@link #newFile(File, String, boolean)} with deletion on exit enabled.
	 */
	public static File newFile(File parentDirectory, String relativePath) throws UncheckedIOException {
		return newFile(parentDirectory, relativePath, true);
	}

	/**
	 * Creates a new file in the given <code>parentDirectory</code>. Same as {@link #newFile(File, String, boolean, boolean)} with timestamp suffix
	 * enabled.
	 */
	public static File newFile(File parentDirectory, String relativePath, boolean deleteOnExit) throws UncheckedIOException {
		return newFile(parentDirectory, relativePath, deleteOnExit, true);
	}

	/**
	 * Creates a new file in the given <code>parentDirectory</code>. Same as {@link #newFileOrDir(File, String, boolean, boolean, PathType)} with path
	 * type {@link PathType#File}.
	 */
	public static File newFile(File parentDirectory, String relativePath, boolean deleteOnExit, boolean addTimestampSuffix)
			throws UncheckedIOException {
		return newFileOrDir(parentDirectory, relativePath, deleteOnExit, addTimestampSuffix, PathType.File);
	}

	/**
	 * Creates a new file or directory in the given <code>parentDirectory</code>. Optionally a timestamp based suffix will be added to the
	 * <code>relativePath</code> to make the path unique. (If the path ends with a file extension, the timestamp suffix will be added to the base
	 * name, i.e. before the extension.) The specified path may contain sub folders.
	 * <p>
	 * If more options are needed, see {@link PathBuilder}.
	 */
	public static File newFileOrDir(File parentDirectory, String relativePath, boolean deleteOnExit, boolean addTimestampSuffix, PathType pathType)
			throws UncheckedIOException {
		File result = PathBuilder.withSettings().baseDir(parentDirectory.getAbsolutePath()).relativePath(relativePath).deleteOnExit(deleteOnExit)
				.pathType(pathType).createParentsOnCreate().createOnFileSystem().addTimestampSuffix(addTimestampSuffix).pathMustNotExist()
				.buildFile();
		return result;
	}

	public static PathBuilder newTempDirBuilder() throws UncheckedIOException {
		return newDirBuilder(PathTools.tempDir().toFile()) //
				.deleteOnExit(true) //
				.addTimestampSuffix(true);
	}

	public static PathBuilder newDirBuilder(File baseDir) throws UncheckedIOException {
		return PathBuilder.withSettings() //
				.pathType(PathType.Directory) //
				.baseDir(baseDir.getAbsolutePath()) //
				.createParentsOnCreate() //
				.createOnFileSystem() //
				.pathMustNotExist();
	}

	/**
	 * Helps to determine if we are in a CI environment or if the test is run locally on a developer's machine. This can matter because for example we
	 * might want to delete all test data in the first case, but leave it on the disk for further inspection in the latter.
	 *
	 * @return <b>true</b> when the current JVM is running in an CI enviroment e.g. in a Jenkins pipeline or an automatic test deployment on the
	 *         cloud.
	 *         <p>
	 *         <b>false</b> otherwise.
	 */
	public static boolean isCiEnvironment() {
		return Boolean.TRUE.toString().equals(System.getenv("CI_ENVIRONMENT"));
	}
}
