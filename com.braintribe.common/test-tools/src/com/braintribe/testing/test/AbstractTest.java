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
package com.braintribe.testing.test;

import java.io.File;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.braintribe.common.HasSharedLogger;
import com.braintribe.common.lcd.AssertionException;
import com.braintribe.logging.Logger;
import com.braintribe.logging.jul.JulConfigurationHelper;
import com.braintribe.testing.internal.path.PathBuilder;
import com.braintribe.testing.internal.path.PathBuilder.AddTimestampSuffix;
import com.braintribe.testing.internal.path.PathExistence;
import com.braintribe.testing.internal.path.PathTools;
import com.braintribe.testing.internal.path.PathType;
import com.braintribe.testing.tools.TestTools;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.Arguments;

/**
 * Convenience class that may be used as a super type when writing unit tests. It e.g. provides methods to easily access test resources. Examples:
 *
 * <pre>
 * testDir();
 * testDir("relative/path/to/subfolder");
 * existingTestDir("relative/path/to/subfolder");
 * existingTestFile("input.xml");
 * </pre>
 *
 * For information on how the paths are found see {@link #newTestPathBuilder(Class)}.
 * <p>
 * The class also provides convenience methods to create temporary files or folders (in temporary files directory). The names are based on simple test
 * class names. If the path already exists, a timestamp suffix is added. Examples:
 *
 * <pre>
 * newTempDir();
 * newTempFile();
 * </pre>
 *
 * <p>
 * In its static initializer this class automatically {@link JulConfigurationHelper#setSimpleConsoleOnlyLoggingConfigurationUnlessAlreadyConfigured()
 * configures JUL}, so that one doesn't get log messages in two-line format written to <code>System.err</code> (which is the default). Note that
 * logging configuration settings may be changed in future releases, thus one shouldn't expect the logging output to look exactly as it currently
 * does.
 * <p>
 * For logging, sub classes can just use the <code>protected</code> {@link HasSharedLogger#logger logger} field or declare their own {@link Logger} in
 * a field named {@value HasSharedLogger#LOGGER_FIELD}. The own logger is only needed for logging from <code>static</code> methods. For more
 * information see {@link HasSharedLogger}.
 *
 * @author michael.lafite
 */
public class AbstractTest extends HasSharedLogger {

	/**
	 * The path to test resources directory.
	 *
	 * @see #resDirPath()
	 */
	protected static Path resDirPath;

	static {
		JulConfigurationHelper.setSimpleConsoleOnlyLoggingConfigurationUnlessAlreadyConfigured();
	}

	/**
	 * Returns the test resources directory. Other methods from this class, e.g. {@link #testPath(String)} or {@link #existingTestFile(Class, String)}
	 * get the base resources directory by calling this method.<br>
	 * When this method is invoked the first time (and {@link #resDirPath} is not set yet), it checks if one of the following paths exist:
	 * <ol>
	 * <li><code>[working directory]/src/test/resources</code>: This is the expected directory for test resources in artifacts with a standard Maven
	 * structure.</li>
	 * <li><code>working directory]/res</code>: This an alternative, which is e.g. used for separate test artifacts with Ant-based building.</li>
	 * </ol>
	 *
	 * If one wants to use another folder, one has to programmatically set <code>static</code> field {@link #resDirPath} before invoking this method.
	 *
	 * @throws AssertionException
	 *             if the test resources directory cannot be found.
	 */
	public static Path resDirPath() {
		if (resDirPath == null) {
			List<String> possibleTestResourcesPathStrings = CollectionTools.getList("src/test/resources", "res");
			for (String possibleTestResourcesPathString : possibleTestResourcesPathStrings) {
				Path path = Paths.get(FileTools.getWorkingDirectoryPath(), possibleTestResourcesPathString);
				if (path.toFile().exists() && path.toFile().isDirectory()) {
					resDirPath = path;
					break;
				}
			}
			if (resDirPath == null) {
				throw new AssertionException("Test resources directory not found. Searched for artifact-relative paths: "
						+ possibleTestResourcesPathStrings
						+ " If you want to use a special directory, you can programmatically set field 'resDirPath' before calling this method.");
			}
		}
		return resDirPath;
	}

	/**
	 * {@link #testDirPath(Class) Gets} the test directory path for this class.
	 */
	protected Path testDirPath() {
		return testDirPath(getClass());
	}

	/**
	 * Gets the test directory path for the specified <code>testClass</code>. Same as {@link #testPath(Class, String)} with an empty relative path.
	 *
	 * @see #testDirPath()
	 */
	public static Path testDirPath(Class<?> testClass) {
		return testPath(testClass, "");
	}

	/**
	 * {@link #testDir(Class) Gets the test directory path} for this class and returns it as a <code>File</code>.
	 *
	 * @throws AssertionException
	 *             if the directory doesn't exist.
	 */
	protected File testDir() {
		return testDir(getClass());
	}

	/**
	 * {@link #testDirPath() Gets the test directory path} for the specified <code>testClass</code>.
	 *
	 * @throws AssertionException
	 *             if the directory doesn't exist.
	 *
	 * @see #testDir()
	 */
	public static File testDir(Class<?> testClass) {
		return PathTools.assertType(testDirPath(testClass), PathType.Directory).toFile();
	}

	/**
	 * {@link #testPath(Class, String) Gets} the test path for the specified <code>relativePath</code>.
	 */
	protected Path testPath(String relativePath) {
		return testPath(getClass(), relativePath);
	}

	/**
	 * Gets the path for a file/directory (specified as <code>relativePath</code>) inside the test {@link #newTestPathBuilder(Class) directory} of the
	 * specified <code>testClass</code>.
	 * <p>
	 * Example: for class <code>com.braintribe.ExampleTest</code> and relative path <code>path/to/testfile.txt</code> the method returns
	 * <code>[TEST_RESOURCES_DIRECTORY]/com/braintribe/ExampleTest/path/to/testfile.txt</code>.
	 *
	 * @see #resDirPath
	 *
	 * @see #testPath(String)
	 */
	public static Path testPath(Class<?> testClass, String relativePath) {
		Arguments.notNullWithNames("testClass", testClass, "relativePath", relativePath);
		Path testPath = newTestPathBuilder(testClass).relativePath(relativePath).build();
		return testPath;
	}

	/**
	 * {@link #testFile(Class, String) Gets a test file} which may exist.
	 */
	protected File testFile(String relativePath) {
		return testFile(getClass(), relativePath);
	}

	/**
	 * Same as {@link #testFile(Class, String, PathExistence)} where file may exist.
	 */
	public static File testFile(Class<?> testClass, String relativePath) {
		return testFile(testClass, relativePath, PathExistence.MayExist);
	}

	/**
	 * {@link #existingTestFile(Class, String) Gets a test file} which must exist.
	 */
	protected File existingTestFile(String relativePath) {
		return existingTestFile(getClass(), relativePath);
	}

	/**
	 * Same as {@link #testFile(Class, String, PathExistence)} where file must exist.
	 */
	public static File existingTestFile(Class<?> testClass, String relativePath) {
		return testFile(testClass, relativePath, PathExistence.MustExist);
	}

	/**
	 * {@link #testFile(Class, String, PathExistence) Gets the test file path} for the specified <code>relativePath</code> and returns it as
	 * <code>File</code>.
	 */
	protected File testFile(String relativePath, PathExistence expectedExistence) {
		return testFile(getClass(), relativePath, expectedExistence);
	}

	/**
	 * Gets the {@link #testPath(Class, String) test path} for the specified <code>relativePath</code> and returns it as <code>File</code>.
	 *
	 * @throws AssertionException
	 *             if the path exists, but is not a {@link PathType#File file} or if <code>expectedExistence</code> check fails.
	 *
	 * @see #newTestPathBuilder()
	 */
	public static File testFile(Class<?> testClass, String relativePath, PathExistence expectedExistence) {
		return PathTools.assertType(testPath(testClass, relativePath), PathType.File, expectedExistence).toFile();
	}

	/**
	 * {@link #testDir(Class, String) Gets a test directory} which may exist.
	 */
	protected File testDir(String relativePath) {
		return testDir(getClass(), relativePath);
	}

	/**
	 * Same as {@link #testDir(Class, String, PathExistence)} where directory must exist.
	 */
	public static File testDir(Class<?> testClass, String relativePath) {
		return testDir(testClass, relativePath, PathExistence.MayExist);
	}

	/**
	 * {@link #existingTestDir(Class, String) Gets a test directory} which must exist.
	 */
	protected File existingTestDir(String relativePath) {
		return existingTestDir(getClass(), relativePath);
	}

	public static File existingTestDir(Class<?> testClass, String relativePath) {
		return testDir(testClass, relativePath, PathExistence.MustExist);
	}

	/**
	 * {@link #testFile(Class, String, PathExistence) Gets the test directory path} for the specified <code>relativePath</code> and returns it as
	 * <code>File</code>.
	 */
	protected File testDir(String relativePath, PathExistence expectedExistence) {
		return PathTools.assertType(testPath(relativePath), PathType.Directory, expectedExistence).toFile();
	}

	/**
	 * Gets the {@link #testPath(String) test path} for the specified <code>relativePath</code> and returns it as <code>File</code>.
	 *
	 * @throws AssertionException
	 *             if the path exists, but is not a {@link PathType#Directory directory}.
	 *
	 * @see #newTestPathBuilder()
	 */
	public static File testDir(Class<?> testClass, String relativePath, PathExistence expectedExistence) {
		return PathTools.assertType(testPath(testClass, relativePath), PathType.Directory, expectedExistence).toFile();
	}

	/**
	 * Returns a new {@link #newTestPathBuilder(Class) PathBuilder} for this class.
	 *
	 * @see #testPath(String)
	 */
	protected PathBuilder newTestPathBuilder() {
		return newTestPathBuilder(getClass());
	}

	/**
	 * Returns a {@link PathBuilder} with the {@link PathBuilder#baseDir(String) base dir} set to the test directory for given <code>testClass</code>.
	 * This test directory is a path relative to to {@link #resDirPath() resources directory} and based on the fully qualified name of the
	 * <code>testClass</code>.<br>
	 * The easiest way to explain this is with an example. For resources directory <code>./res</code> and class <code>com.acme.ExampleTest</code>
	 * possible test directories are:
	 * <ul>
	 * <li><code>./res/com/acme/ExampleTest</code></li>
	 * <li><code>./res/com.acme.ExampleTest</code></li>
	 * <li><code>./res/acme/ExampleTest</code></li>
	 * <li><code>./res/acme.ExampleTest</code></li>
	 * <li><code>./res/ExampleTest</code></li>
	 * <ul>
	 * The method checks for all these folders (in exactly the given order) and picks the first one that exists.
	 * <p>
	 * Using this approach each test class gets its own directory for test resources. This makes it possible to easily organize test resources.
	 * <p>
	 * Note that for temporary files one can also use the methods provided by {@link TestTools}, e.g. {@link TestTools#newTempFileForClass(Class)}.
	 *
	 * @throws AssertionException
	 *             if there is no test directory for the <code>testClass</code>.
	 *
	 * @see #newTestPathBuilder()
	 */
	public static PathBuilder newTestPathBuilder(Class<?> testClass) {
		Arguments.notNullWithName("testClass", testClass);
		File resDir = resDirPath().toFile();
		String partOfFullyQualifiedName = testClass.getName();

		File testFolder = null;
		while (true) {
			boolean partOfFullyQualifiedNameContainsPackageNamePart = partOfFullyQualifiedName.contains(".");

			if (partOfFullyQualifiedNameContainsPackageNamePart) {
				File possibleTestFolder = new File(resDir, partOfFullyQualifiedName.replace('.', '/'));
				if (possibleTestFolder.exists()) {
					testFolder = possibleTestFolder;
					break;
				}
			}
			File possibleTestFolder = new File(resDir, partOfFullyQualifiedName);
			if (possibleTestFolder.exists()) {
				testFolder = possibleTestFolder;
				break;
			}

			if (!partOfFullyQualifiedNameContainsPackageNamePart) {
				throw new AssertionException(
						"Couldn't find test directory for test class " + testClass.getName() + " in test resources directory " + resDir + "!");
			}

			partOfFullyQualifiedName = StringTools.getSubstringAfter(partOfFullyQualifiedName, ".");
		}

		return PathBuilder.withSettings().baseDir(testFolder.getAbsolutePath());
	}

	/**
	 * Creates a new directory in temporary files directory. Same as {@link #newTempDir(String)} with a relative path based on simple test class name.
	 */
	public File newTempDir() throws UncheckedIOException {
		return newTempDir(getClass().getSimpleName());
	}

	/**
	 * Creates a new directory in temporary files directory based on the given <code>relativeFilePath</code>. If required, a timestamp suffix is added
	 * to make sure the directory doesn't exist yet. The directory will be {@link File#deleteOnExit() deleted on exit}, if it is empty.
	 */
	public File newTempDir(String relativeFilePath) throws UncheckedIOException {
		return newTempFileOrDir(relativeFilePath, PathType.Directory);
	}

	/**
	 * Creates a new file in temporary files directory. Same as {@link #newTempFile(String)} with a relative path based on simple test class name.
	 */
	public File newTempFile() throws UncheckedIOException {
		return newTempFile(getClass().getSimpleName());
	}

	/**
	 * Creates a new file in temporary files directory based on the given <code>relativeFilePath</code>. If required, a timestamp suffix is added to
	 * make sure the file doesn't exist yet. The file will be {@link File#deleteOnExit() deleted on exit}.
	 */
	public File newTempFile(String relativePath) throws UncheckedIOException {
		return newTempFileOrDir(relativePath, PathType.File);
	}

	private File newTempFileOrDir(String relativePath, PathType pathType) throws UncheckedIOException {
		return PathBuilder.withSettings().addTimestampSuffix(AddTimestampSuffix.IfRequired).baseDirIsTempDir().relativePath(relativePath)
				.pathType(pathType).createOnFileSystem().createParents().deleteOnExit().pathMustNotExist().buildFile();
	}

}
