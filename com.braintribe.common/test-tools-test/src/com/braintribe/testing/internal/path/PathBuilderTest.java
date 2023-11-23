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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThatExecuting;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.braintribe.common.lcd.AssertionException;
import com.braintribe.testing.internal.path.PathBuilder.AddTimestampSuffix;
import com.braintribe.testing.internal.path.PathBuilder.CreateParents;
import com.braintribe.testing.test.AbstractTest;
import com.braintribe.utils.FileTools;

/**
 * Provides tests for {@link PathBuilder}.
 *
 * @author michael.lafite
 */
public class PathBuilderTest extends AbstractTest {

	private static final String TIMESTAMPED_FILE_REGEX = ".*\\d+-\\d+-\\d+([a-zA-Z.])*$";

	@Test
	public void test() throws Exception {
		String baseDir = testDirPath().toString();
		String baseDirForGeneratedFiles = baseDir + "/generatedByTest";

		if (new File(baseDirForGeneratedFiles).exists()) {
			FileTools.deleteDirectoryRecursively(new File(baseDirForGeneratedFiles));
		}

		{
			PathBuilder builder = PathBuilder.withSettings().baseDir(baseDir).path("1").checkExists(PathExistence.MustExist);
			check(builder.build(), baseDir + "/1", true);
			check(builder.pathType(PathType.File).build(), baseDir + "/1", true);
			// not a directory
			assertThatExecuting(() -> builder.pathType(PathType.Directory).build()).fails().with(AssertionException.class);
			// not a file
			assertThatExecuting(() -> PathBuilder.withSettings().baseDir(baseDir).path("a/b").pathType(PathType.File).build()).fails()
					.with(AssertionException.class);
		}

		check(PathBuilder.withSettings().baseDir(baseDir).path("3.x.y").checkExists(PathExistence.MayExist).build(), baseDir + "/3.x.y", true);

		check(PathBuilder.withSettings().baseDir(baseDir).path("DOESNOTEXIST").checkExists(PathExistence.MayExist).build(), baseDir + "/DOESNOTEXIST",
				false);

		check(PathBuilder.withSettings().baseDir(baseDir).path("a/b/DOESNOTEXIST").checkExists(PathExistence.MustNotExist).build(),
				baseDir + "/a/b/DOESNOTEXIST", false);

		check(PathBuilder.withSettings().baseDir(baseDirForGeneratedFiles).path("file1").checkExists(PathExistence.MustNotExist)
				.pathType(PathType.File).createOnFileSystem().build(), baseDirForGeneratedFiles + "/file1", true);
		check(PathBuilder.withSettings().baseDir(baseDirForGeneratedFiles).path("path/to/file2").pathType(PathType.File)
				.checkExists(PathExistence.MustNotExist).pathType(PathType.File).createOnFileSystem().build(),
				baseDirForGeneratedFiles + "/path/to/file2", true);

		check(PathBuilder.withSettings().baseDir(baseDirForGeneratedFiles).path("some/dir").checkExists(PathExistence.MustNotExist)
				.pathType(PathType.Directory).createOnFileSystem().build(), baseDirForGeneratedFiles + "/some/dir", true);

		check(PathBuilder.withSettings().baseDir(baseDirForGeneratedFiles).path("shouldBeDeleted_Dir").checkExists(PathExistence.MustNotExist)
				.pathType(PathType.Directory).createOnFileSystem().deleteOnExit().build(), baseDirForGeneratedFiles + "/shouldBeDeleted_Dir", true);

		check(PathBuilder.withSettings().baseDir(baseDirForGeneratedFiles).path("shouldBeDeleted_File").checkExists(PathExistence.MustNotExist)
				.pathType(PathType.File).createOnFileSystem().deleteOnExit().build(), baseDirForGeneratedFiles + "/shouldBeDeleted_File", true);

		{
			PathBuilder builder = PathBuilder.withSettings().baseDir(baseDirForGeneratedFiles).path("timestamptest/abc.txt")
					.checkExists(PathExistence.MayExist).pathType(PathType.File).createParents(CreateParents.Always).createOnFileSystem()
					.addTimestampSuffix(AddTimestampSuffix.IfRequired);

			// no timestamp added (not required, doesn't exist)
			Path path = builder.build();
			check(path, true);
			assertThat(path.toString()).doesNotMatch(TIMESTAMPED_FILE_REGEX);

			// no timestamp added (file exists now, but may exist --> no timestamp required)
			path = builder.build();
			check(path, true);
			assertThat(path.toString()).doesNotMatch(TIMESTAMPED_FILE_REGEX);

			// timestamp added (file exists now, but must not --> timestamp required)
			path = builder.checkExists(PathExistence.MustNotExist).build();
			check(path, true);
			assertThat(path.toString()).matches(TIMESTAMPED_FILE_REGEX);

			path = builder.path("timestamptest/abc2.x.y.z").addTimestampSuffix(AddTimestampSuffix.Always).build();
			check(path, true);
			assertThat(path.toString()).matches(TIMESTAMPED_FILE_REGEX);

		}
	}

	private static void check(Path expectedAndActual, boolean actualExists) {
		check(expectedAndActual, expectedAndActual.toString(), actualExists);
	}

	private static void check(Path expected, String actual, boolean actualExists) {
		assertThat(expected.toUri().normalize()).isEqualTo(Paths.get(actual).toUri().normalize());
		if (actualExists) {
			assertThat(new File(expected.toString())).exists();
		} else {
			assertThat(new File(expected.toString())).doesNotExist();
		}
	}
}
