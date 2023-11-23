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
import java.util.Date;

import com.braintribe.common.lcd.AssertionException;
import com.braintribe.common.lcd.ConfigurationException;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.ExtSimpleDateFormat;

/**
 * ATTENTION: this class will be moved soon, which means the package name will change!<br>
 * <p>
 * Simple helper to {@link #build() build} a {@link Path} providing configuration settings such as whether to actually {@link #createOnFileSystem()
 * create the path on the file system} or whether to {@link #deleteOnExit() delete it on exit}. Settings are configurable via fluent API. Entry point
 * is {@link #withSettings()}. Example:
 *
 * <pre>
 * PathBuilder.withSettings().baseDirIsWorkingDir().relativePath("path/to/someFile.txt").pathMustNotExist().createOnFileSystem().build();
 * </pre>
 *
 * @author michael.lafite
 */
// TODO: move to Java 8 PlatformApi; remove attention warning from Javadoc
public interface PathBuilder {

	PathType DEFAULT_PATHTYPE = PathType.Any;
	PathExistence DEFAULT_EXISTSCHECK = PathExistence.MayExist;
	CreateParents DEFAULT_PARENTSCREATION = CreateParents.OnCreate;
	AddTimestampSuffix DEFAULT_ADDTIMESTAMPSUFFIX = AddTimestampSuffix.Never;
	boolean DEFAULT_CREATEONFILESYSTEM = false;
	boolean DEFAULT_DELETEONEXIT = false;

	/**
	 * Specifies whether or not to create parent directories.
	 *
	 * @author michael.lafite
	 */
	public enum CreateParents {
		/**
		 * Always create parent directories, if they don't exist.
		 */
		Always,
		/**
		 * Create parent directories, if they don't exist and if {@link PathBuilder#createOnFileSystem() the path is created on file system}.
		 */
		OnCreate,
		/**
		 * Never create parent directories. This causes a failure, if {@link PathBuilder#createOnFileSystem() the path is supposed to be created on
		 * file system} and the parents don't exist.
		 */
		Never;
	}

	/**
	 * Specifies whether or not to add a timestamp based suffix to the path. This is used to create unique file paths.
	 *
	 * @author michael.lafite
	 */
	public enum AddTimestampSuffix {
		/**
		 * Always add timestamp suffix, even if path itself doesn't exist.
		 */
		Always,
		/**
		 * Add timestamp suffix, but only if needed, i.e. if the path {@link PathExistence#MustNotExist must not exist}.
		 */
		IfRequired,
		/**
		 * Never add timestamp suffix. If the path {@link PathExistence#MustNotExist must not exist}, this will cause a failure.
		 */
		Never;
	}

	/**
	 * Sets the {@link PathType}. Default is {@value #DEFAULT_PATHTYPE}.
	 */
	PathBuilder pathType(PathType pathType);

	/**
	 * {@link #pathType(PathType) Sets the PathType} to {@link PathType#File}.
	 */
	PathBuilder pathIsFile();

	/**
	 * {@link #pathType(PathType) Sets the PathType} to {@link PathType#Directory}.
	 */
	PathBuilder pathIsDirectory();

	/**
	 * Returns the set {@link PathType}.
	 */
	PathType getPathType();

	/**
	 * Sets the base dir. This can be combined with a {@link #relativePath relative} {@link #path path}. Default is the {@link #baseDirIsWorkingDir()
	 * working directory}.
	 */
	PathBuilder baseDir(String absoluteBaseDirPath);

	/**
	 * Sets the temporary files directory as {@link #baseDir(String) base dir}.
	 */
	PathBuilder baseDirIsTempDir();

	/**
	 * Sets the working directory as {@link #baseDir(String) base dir}.
	 */
	PathBuilder baseDirIsWorkingDir();

	/**
	 * Returns the set base dir.
	 */
	Path getBaseDir();

	/**
	 * Sets the {@link #createOnFileSystem() created} path to be deleted on exit. Default is {@value #DEFAULT_DELETEONEXIT}.
	 */
	PathBuilder deleteOnExit();

	/**
	 * Specifies whether the {@link #createOnFileSystem() created} path shall be deleted on exit. Default is {@value #DEFAULT_DELETEONEXIT}.
	 */
	PathBuilder deleteOnExit(boolean deleteOnExit);

	/**
	 * Returns whether or not to delete on exit.
	 */
	boolean isDeleteOnExit();

	/**
	 * Sets the path which may be relative to the {@link #baseDir(String) base dir} or absolute. If it is absolute, the base dir will be ignored.
	 * Specifying the path is mandatory.
	 */
	PathBuilder path(String path, String... more);

	/**
	 * Same as {@link #path}, but only accepts relative paths (relative to the {@link #baseDir(String) base dir}).
	 *
	 * @throws IllegalArgumentException
	 *             if the path is not a relative path.
	 */
	PathBuilder relativePath(String relativePath, String... more);

	/**
	 * Same as {@link #path}, but only accepts absolute paths.
	 *
	 * @throws IllegalArgumentException
	 *             if the path is not an absolute path.
	 */
	PathBuilder absolutePath(String absolutePath, String... more);

	/**
	 * Returns the set path.
	 */
	Path getPath();

	/**
	 * Specifies whether the {@link #path path} is expected to already exist (on the file system). Default is {@value #DEFAULT_EXISTSCHECK}.
	 */
	PathBuilder checkExists(PathExistence pathExistence);

	/**
	 * {@link #checkExists(PathExistence) Sets the ExistsCheck} to {@link PathExistence#MustExist}.
	 */
	PathBuilder pathMustExist();

	/**
	 * {@link #checkExists(PathExistence) Sets the ExistsCheck} to {@link PathExistence#MayExist}.
	 */
	PathBuilder pathMayExist();

	/**
	 * {@link #checkExists(PathExistence) Sets the ExistsCheck} to {@link PathExistence#MustNotExist}.
	 */
	PathBuilder pathMustNotExist();

	/**
	 * Returns the {@link PathExistence} check setting.
	 */
	PathExistence getCheckExists();

	/**
	 * Specifies when/whether to create the parent directories. Default is {@value #DEFAULT_PARENTSCREATION}.
	 */
	PathBuilder createParents(CreateParents createParents);

	/**
	 * Delegates to {@link #createParents(CreateParents)} passing {@link CreateParents#OnCreate}.
	 */
	PathBuilder createParentsOnCreate();

	/**
	 * Delegates to {@link #createParents(boolean)} passing <code>true</code>.
	 */
	PathBuilder createParents();

	/**
	 * Delegates to {@link #createParents(CreateParents)} passing {@link CreateParents#Always}, if <code>createParents</code> is <code>true</code>,
	 * otherwise {@link CreateParents#Never}.
	 */
	PathBuilder createParents(boolean createParents);

	/**
	 * Returns the {@link CreateParents} setting.
	 */
	CreateParents getCreateParents();

	/**
	 * Specifies when/whether to create a timestamp based suffix that ensures that path doesn't exist yet. Default is
	 * {@link #DEFAULT_ADDTIMESTAMPSUFFIX}.
	 */
	PathBuilder addTimestampSuffix(AddTimestampSuffix addTimestampSuffix);

	/**
	 * Delegates to {@link #addTimestampSuffix(boolean)} passing <code>true</code>.
	 */
	PathBuilder addTimestampSuffix();

	/**
	 * Delegates to {@link #addTimestampSuffix(AddTimestampSuffix)} passing {@link AddTimestampSuffix#Always}, if <code>addTimestampSuffix</code> is
	 * <code>true</code>, otherwise {@link AddTimestampSuffix#Never}.
	 */
	PathBuilder addTimestampSuffix(boolean addTimestampSuffix);

	/**
	 * Returns the {@link AddTimestampSuffix} setting.
	 */
	AddTimestampSuffix getAddTimestampSuffix();

	/**
	 * Specifies to actually create the file/directory on the file system.
	 */
	PathBuilder createOnFileSystem();

	/**
	 * Specifies whether or not to actually create the file/directory on the file system.
	 */
	PathBuilder createOnFileSystem(boolean createOnFileSystem);

	/**
	 * Returns whether or not to create the path on the file system.
	 */
	boolean isCreateOnFileSystem();

	/**
	 * Builds the path and returns it as a {@link Path} instance.
	 *
	 * @throws ConfigurationException
	 *             if the configuration is invalid in any way.
	 * @throws AssertionException
	 *             if an assertion fails, e.g. if the path is a file instead of a directory.
	 * @throws RuntimeException
	 *             if there is any I/O error.
	 */
	Path build();

	/**
	 * Builds the path and returns it as a {@link File} instance. This first delegates to {@link #build()} and therefore throws same exceptions.
	 */
	File buildFile();

	/**
	 * Creates a new {@link PathBuilder} instance ready to be configured. This can be used as entry point for the fluent configuration, e.g.
	 * <code>PathBuilder.withSettings().baseDirIsWorkingDir().path("some/path").deleteOnExit().build();</code>
	 *
	 * @see #newInstance()
	 */
	static PathBuilder withSettings() {
		return new PathBuilderImpl();
	}

	/**
	 * Creates a new {@link PathBuilder}.
	 *
	 * @see #withSettings()
	 */
	static PathBuilder newInstance() {
		return new PathBuilderImpl();
	}

	/**
	 * {@link PathBuilder} implementation.
	 *
	 * @author michael.lafite
	 */
	static class PathBuilderImpl implements PathBuilder {

		private PathType pathType = DEFAULT_PATHTYPE;
		private PathExistence pathExistence = DEFAULT_EXISTSCHECK;
		private CreateParents createParents = DEFAULT_PARENTSCREATION;
		private AddTimestampSuffix addTimestampSuffix = DEFAULT_ADDTIMESTAMPSUFFIX;
		private Path baseDir = Paths.get(FileTools.getWorkingDirectoryPath());
		private Path path;
		private boolean deleteOnExit = DEFAULT_DELETEONEXIT;
		private boolean createOnFileSystem = DEFAULT_CREATEONFILESYSTEM;

		@Override
		public PathBuilder pathType(PathType pathType) {
			this.pathType = pathType;
			return this;
		}

		@Override
		public PathBuilder pathIsFile() {
			return pathType(PathType.File);
		}

		@Override
		public PathBuilder pathIsDirectory() {
			return pathType(PathType.Directory);
		}

		@Override
		public PathType getPathType() {
			return pathType;
		}

		@Override
		public PathBuilder baseDir(String absoluteBaseDirPath) {
			baseDir = Paths.get(absoluteBaseDirPath);
			if (!baseDir.isAbsolute()) {
				throw new IllegalArgumentException("Specified base dir path is not absolute: '" + absoluteBaseDirPath + "'");
			}
			return this;
		}

		@Override
		public PathBuilder baseDirIsTempDir() {
			return baseDir(PathTools.tempDir().toString());
		}

		@Override
		public PathBuilder baseDirIsWorkingDir() {
			return baseDir(PathTools.workingDir().toString());
		}

		@Override
		public Path getBaseDir() {
			return baseDir;
		}

		@Override
		public PathBuilder deleteOnExit() {
			return deleteOnExit(true);
		}

		@Override
		public PathBuilder deleteOnExit(boolean deleteOnExit) {
			this.deleteOnExit = deleteOnExit;
			return this;
		}

		@Override
		public boolean isDeleteOnExit() {
			return deleteOnExit;
		}

		@Override
		public PathBuilder path(String path, String... more) {
			this.path = Paths.get(path, more);
			return this;
		}

		@Override
		public PathBuilder relativePath(String first, String... more) {
			path = Paths.get(first, more);
			if (path.isAbsolute()) {
				throw new IllegalArgumentException("Specified path is absolute: '" + path.toString() + "'");
			}

			return this;
		}

		@Override
		public PathBuilder absolutePath(String absolutePath, String... more) {
			path = Paths.get(absolutePath, more);
			if (!path.isAbsolute()) {
				throw new IllegalArgumentException("Specified path is relative: '" + path.toString() + "'");
			}
			return this;
		}

		@Override
		public Path getPath() {
			return path;
		}

		@Override
		public PathBuilder checkExists(PathExistence pathExistence) {
			this.pathExistence = pathExistence;
			return this;
		}

		@Override
		public PathBuilder pathMustExist() {
			return checkExists(PathExistence.MustExist);
		}

		@Override
		public PathBuilder pathMayExist() {
			return checkExists(PathExistence.MayExist);
		}

		@Override
		public PathBuilder pathMustNotExist() {
			return checkExists(PathExistence.MustNotExist);
		}

		@Override
		public PathExistence getCheckExists() {
			return pathExistence;
		}

		@Override
		public PathBuilder createParents(CreateParents createParents) {
			this.createParents = createParents;
			return this;
		}

		@Override
		public PathBuilder createParentsOnCreate() {
			return createParents(CreateParents.OnCreate);
		}

		@Override
		public PathBuilder createParents() {
			return createParents(true);
		}

		@Override
		public PathBuilder createParents(boolean createParents) {
			return createParents(createParents ? CreateParents.Always : CreateParents.Never);
		}

		@Override
		public CreateParents getCreateParents() {
			return createParents;
		}

		@Override
		public PathBuilder addTimestampSuffix(AddTimestampSuffix addTimestampSuffix) {
			this.addTimestampSuffix = addTimestampSuffix;
			return this;
		}

		@Override
		public PathBuilder addTimestampSuffix() {
			return addTimestampSuffix(true);
		}

		@Override
		public PathBuilder addTimestampSuffix(boolean addTimestampSuffix) {
			return addTimestampSuffix(addTimestampSuffix ? AddTimestampSuffix.Always : AddTimestampSuffix.Never);
		}

		@Override
		public AddTimestampSuffix getAddTimestampSuffix() {
			return addTimestampSuffix;
		}

		@Override
		public PathBuilder createOnFileSystem() {
			return createOnFileSystem(true);
		}

		@Override
		public PathBuilder createOnFileSystem(boolean createOnFileSystem) {
			this.createOnFileSystem = createOnFileSystem;
			return this;
		}

		@Override
		public boolean isCreateOnFileSystem() {
			return createOnFileSystem;
		}

		@Override
		public Path build() {
			Path pathWithoutTimestampSuffix = path;
			if (!pathWithoutTimestampSuffix.isAbsolute()) {
				pathWithoutTimestampSuffix = baseDir.resolve(pathWithoutTimestampSuffix);
			}

			Path parentPath = pathWithoutTimestampSuffix.getParent();
			boolean parentExists = parentPath.toFile().exists();
			if (!parentExists) {
				// TODO: for all parent dirs (below base dir) it'd probably make sense to also set delete on exit (if
				// enabled)
				if (createParents.equals(CreateParents.Always) || (createParents.equals(CreateParents.OnCreate) && createOnFileSystem)) {
					FileTools.createDirectory(parentPath.toString());
					parentExists = true;
				}
			}

			String nameAndExtension = pathWithoutTimestampSuffix.getFileName().toString();
			String name;
			String extension;
			if (nameAndExtension.contains(".")) {
				name = StringTools.getSubstringBefore(nameAndExtension, ".");
				extension = StringTools.getSubstringFrom(nameAndExtension, ".");
			} else {
				name = nameAndExtension;
				extension = "";
			}

			Path result = pathWithoutTimestampSuffix;
			File resultAsFile = result.toFile();
			boolean resultExists = resultAsFile.exists();

			if (addTimestampSuffix.equals(AddTimestampSuffix.Always) || (resultExists && pathExistence.equals(PathExistence.MustNotExist)
					&& addTimestampSuffix.equals(AddTimestampSuffix.IfRequired))) {
				// no reason the check the path type, since we will create a new one anyway
			} else {
				// fail if the path exists, but doesn't match the type
				PathTools.assertType(result, pathType, false);
			}

			int tries = 0;
			boolean timestampAdded = false;
			while (true) {

				if (addTimestampSuffix.equals(AddTimestampSuffix.Always) || (resultExists && pathExistence.equals(PathExistence.MustNotExist)
						&& addTimestampSuffix.equals(AddTimestampSuffix.IfRequired))) {
					timestampAdded = true;
					final String timestamp = new ExtSimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date());
					result = parentPath.resolve(name + "_" + timestamp + extension);
					resultAsFile = result.toFile();
					resultExists = resultAsFile.exists();
				}

				if (resultExists && pathExistence.equals(PathExistence.MustNotExist)) {
					if (addTimestampSuffix.equals(AddTimestampSuffix.Never)) {
						throw new RuntimeException("File does not exist: " + result.toFile().getAbsolutePath());
					}
					// try again
					CommonTools.sleep(1);
					continue;

				}

				if (!resultExists && pathExistence.equals(PathExistence.MustExist)) {
					throw new RuntimeException("File does not exist: " + resultAsFile.getAbsolutePath());
				}

				if (createOnFileSystem && !resultExists) {
					if (!parentExists) {
						throw new RuntimeException(
								"Cannot create " + result + " because the parent directory doesn't exist and automatic creation has been disabled.");
					}

					if (pathType.equals(PathType.Any)) {
						throw new ConfigurationException(
								"Setting createOnFileSystem cannot be combined with " + PathType.class.getName() + " " + PathType.Any + "!");
					}

					boolean success = false;
					IOException exception = null;
					if (pathType.equals(PathType.File)) {
						try {
							success = resultAsFile.createNewFile();
						} catch (IOException e) {
							exception = e;
							// although unlikely, the file may exist (e.g. if multiple threads use this method),
							// therefore we continue
						}
					} else {
						success = resultAsFile.mkdir();
					}
					if (!success) {
						if (!timestampAdded) {
							// since the result won't change, no need to continue
							if (exception == null) {
								throw new RuntimeException("Couldn't create " + result + "! (reason unknown, no exception thrown)");
							}
							throw new UncheckedIOException("Couldn't create " + result + "!", exception);
						}
						tries++;
						if (tries > 1000) {
							throw new UncheckedIOException("Couldn't create " + result + ", even after trying multiple time stamp based names!",
									exception);
						}
						continue;
					}
				}

				// we did it :)

				if (deleteOnExit) {
					resultAsFile.deleteOnExit();
				}

				break;
			}

			return result;
		}

		@Override
		public File buildFile() {
			return build().toFile();
		}

	}
}
