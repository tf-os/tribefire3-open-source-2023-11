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
package com.braintribe.utils;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * Provides tests for {@link FileTools}.
 *
 *
 */

public class FileToolsTest {

	@Test
	public void replaceIllegalCharactersInFileNameTest() {
		// Filename that contains one illegal character
		String filename = "test\"name";
		String newfilename = FileTools.replaceIllegalCharactersInFileName(filename, "");
		assertThat(newfilename).isEqualTo("testname");

		// Filename that contains two same illegal characters
		filename = "t:estnam:e";
		newfilename = FileTools.replaceIllegalCharactersInFileName(filename, "");
		assertThat(newfilename).isEqualTo("testname");

		// Filename that contains two different illegal characters
		filename = "t:estnam<e";
		newfilename = FileTools.replaceIllegalCharactersInFileName(filename, "");
		assertThat(newfilename).isEqualTo("testname");

		// Filename that is already correct
		filename = "testname";
		newfilename = FileTools.replaceIllegalCharactersInFileName(filename, "");
		assertThat(newfilename).isEqualTo("testname");

		// Filename that contains two different illegal characters and we replace them with the character 0
		filename = "t:estnam<e";
		newfilename = FileTools.replaceIllegalCharactersInFileName(filename, "0");
		assertThat(newfilename).isEqualTo("t0estnam0e");

		// Filename that contains only illegal characters
		filename = "***??\\:>>/>";
		newfilename = FileTools.replaceIllegalCharactersInFileName(filename, "");
		assertThat(newfilename).isEqualTo("");
	}

	@Test
	public void testGetNameWithoutExtension() {
		// A simple filename
		assertThat(FileTools.getNameWithoutExtension("Example.txt")).isEqualTo("Example");

		// A one letter filename
		assertThat(FileTools.getNameWithoutExtension("A.txt")).isEqualTo("A");

		// A no extension filename
		assertThat(FileTools.getNameWithoutExtension("Example")).isEqualTo("Example");
	}

	@Test
	public void testGetName() {
		assertThat(FileTools.getName("/some/path/Example.txt")).isEqualTo("Example.txt");
		assertThat(FileTools.getName("file://localhost:8080/path/Example.txt")).isEqualTo("Example.txt");
		assertThat(FileTools.getName("Example")).isEqualTo("Example");
		assertThat(FileTools.getName("\\mixed/paths\\Example.txt")).isEqualTo("Example.txt");
	}

	@Test
	public void testCleanEmptyFoldersWithEmptyFolders() throws Exception {

		File tmpDir = null;
		try {
			tmpDir = File.createTempFile("FileToolsTest", null);
			if (tmpDir.exists()) {
				tmpDir.delete();
			}
			tmpDir.mkdirs();

			File subdir1 = new File(tmpDir, "1");
			File subdir2 = new File(subdir1, "2");
			File subdir3 = new File(subdir2, "3");
			File subdir4 = new File(subdir3, "4");
			subdir4.mkdirs();

			Assert.assertTrue(subdir4.exists());

			FileTools.cleanEmptyFolders(tmpDir);

			Assert.assertTrue(tmpDir.exists());
			Assert.assertFalse(subdir1.exists());
			Assert.assertFalse(subdir2.exists());
			Assert.assertFalse(subdir3.exists());
			Assert.assertFalse(subdir4.exists());

			File[] files = tmpDir.listFiles();
			if (files != null && files.length > 0) {
				throw new AssertionError("The folder " + tmpDir.getAbsolutePath() + " is not empty as expected.");
			}

			tmpDir.delete();
		} finally {
			if (tmpDir != null && tmpDir.exists()) {
				FileTools.deleteDirectoryRecursively(tmpDir);
			}
		}

	}

	@Test
	public void testCleanEmptyFoldersWithNonEmptyFolders() throws Exception {

		File tmpDir = null;
		try {
			tmpDir = File.createTempFile("FileToolsTest", null);
			if (tmpDir.exists()) {
				tmpDir.delete();
			}
			tmpDir.mkdirs();

			File subdir1 = new File(tmpDir, "1");
			File subdir2 = new File(subdir1, "2");
			File subdir3 = new File(subdir2, "3");
			File subdir4 = new File(subdir3, "4");
			subdir4.mkdirs();

			File nonemptyDir = new File(subdir1, "nonempty");
			nonemptyDir.mkdirs();
			File file = new File(nonemptyDir, "hello.world.txt");
			IOTools.spit(file, "Hello, world!", "UTF-8", false);

			Assert.assertTrue(subdir4.exists());
			Assert.assertTrue(file.exists());

			FileTools.cleanEmptyFolders(tmpDir);

			Assert.assertTrue(tmpDir.exists());
			Assert.assertTrue(subdir1.exists());
			Assert.assertFalse(subdir2.exists());
			Assert.assertFalse(subdir3.exists());
			Assert.assertFalse(subdir4.exists());
			Assert.assertTrue(nonemptyDir.exists());
			Assert.assertTrue(file.exists());

			File[] files = tmpDir.listFiles();
			if (files == null || files.length != 1) {
				throw new AssertionError("The folder " + tmpDir.getAbsolutePath() + " does not contain one element as expected.");
			}

		} finally {
			if (tmpDir != null && tmpDir.exists()) {
				FileTools.deleteDirectoryRecursively(tmpDir);
			}
		}

	}

	@Test
	public void testCleanEmptyFoldersWithEmptyFoldersAndFinalDelete() throws Exception {

		File tmpDir = null;
		try {
			tmpDir = File.createTempFile("FileToolsTest", null);
			if (tmpDir.exists()) {
				tmpDir.delete();
			}
			tmpDir.mkdirs();

			File subdir1 = new File(tmpDir, "1");
			File subdir2 = new File(subdir1, "2");
			File subdir3 = new File(subdir2, "3");
			File subdir4 = new File(subdir3, "4");
			subdir4.mkdirs();

			Assert.assertTrue(subdir4.exists());

			FileTools.cleanEmptyFolders(tmpDir, false);

			Assert.assertFalse(tmpDir.exists());
			Assert.assertFalse(subdir1.exists());
			Assert.assertFalse(subdir2.exists());
			Assert.assertFalse(subdir3.exists());
			Assert.assertFalse(subdir4.exists());

		} finally {
			if (tmpDir != null && tmpDir.exists()) {
				FileTools.deleteDirectoryRecursively(tmpDir);
			}
		}

	}

	@Test
	public void testGetNextFreeFilename() {

		Set<String> filenames = new HashSet<>();
		for (int i = 0; i < 5; ++i) {
			String filename = FileTools.getNextFreeFilename(filenames, "test.txt");
			if (i == 0) {
				assertThat(filename).isEqualTo("test.txt");
			} else {
				assertThat(filename).isEqualTo("test" + (i + 1) + ".txt");
			}
		}
		assertThat(filenames.size()).isEqualTo(5);

		filenames.clear();

		String filename = null;

		filename = FileTools.getNextFreeFilename(filenames, "test.txt");
		assertThat(filename).isEqualTo("test.txt");
		filename = FileTools.getNextFreeFilename(filenames, "Test.txt");
		assertThat(filename).isEqualTo("Test2.txt");
		filename = FileTools.getNextFreeFilename(filenames, "tEst.txt");
		assertThat(filename).isEqualTo("tEst3.txt");
		filename = FileTools.getNextFreeFilename(filenames, "teSt.txt");
		assertThat(filename).isEqualTo("teSt4.txt");
		filename = FileTools.getNextFreeFilename(filenames, "tesT.txt");
		assertThat(filename).isEqualTo("tesT5.txt");

		assertThat(filenames.size()).isEqualTo(5);

		filenames.clear();

		filename = FileTools.getNextFreeFilename(filenames, "1");
		assertThat(filename).isEqualTo("1");
		filename = FileTools.getNextFreeFilename(filenames, "1");
		assertThat(filename).isEqualTo("12");
	}

	@Test
	public void testIsAbsolutePath() {
		// On Linux /tmp is absolute. On Windows it isn't - it's relative to your current drive.
		switch (OsTools.getOperatingSystem()) {
			case Windows:
				assertThat(FileTools.isAbsolutePath("/a/b/c")).isFalse();
				assertThat(FileTools.isAbsolutePath("c:/a/b/c")).isTrue();
				assertThat(FileTools.isAbsolutePath("c:\\a\\b\\c")).isTrue();
				break;
			default:
				assertThat(FileTools.isAbsolutePath("/a/b/c")).isTrue();
				break;
		}

	}
	@Test
	public void testIsRelativePath() {
		assertThat(FileTools.isAbsolutePath("a/b/c")).isFalse();
	}

	@Test
	public void testRegisterTemporaryFileForDeletion() throws IOException, InterruptedException {

		File tempFile = File.createTempFile("deletiontest", ".bin");
		File checkFile = new File(tempFile.getAbsolutePath());
		try {

			FileTools.deleteFileWhenOrphaned(tempFile);
			tempFile = null;
			System.gc();

			@SuppressWarnings("unused")
			byte[] putPressureOnMemory = new byte[10000000];

			System.gc();

			long waitTime = 10000L; // let's wait 10 seconds max
			long interval = 500L;
			long start = System.currentTimeMillis();
			while (true) {
				if (!checkFile.exists()) {
					break;
				}
				Thread.sleep(interval);
				long now = System.currentTimeMillis();
				if ((now - start) > waitTime) {
					break;
				}
			}
			if (checkFile.exists()) {
				throw new AssertionError("The file " + checkFile.getAbsolutePath() + " has not been deleted.");
			}

		} finally {
			// If something goes wrong....
			if (checkFile.exists()) {
				checkFile.delete();
			}
		}

	}

	@Test
	public void testNormalizeFilename() {
		assertThat(FileTools.normalizeFilename("hello.world", '_')).isEqualTo("hello.world");
		assertThat(FileTools.normalizeFilename("helloworld", '_')).isEqualTo("helloworld");
		assertThat(FileTools.normalizeFilename("hello-world", '_')).isEqualTo("hello-world");
		assertThat(FileTools.normalizeFilename("hello_world", '_')).isEqualTo("hello_world");
		assertThat(FileTools.normalizeFilename("hello$world", '_')).isEqualTo("hello_world");
		assertThat(FileTools.normalizeFilename("hello\\world", '_')).isEqualTo("hello_world");
		assertThat(FileTools.normalizeFilename("hello/world", '_')).isEqualTo("hello_world");
		assertThat(FileTools.normalizeFilename("hello#world", '_')).isEqualTo("hello_world");
		assertThat(FileTools.normalizeFilename("hello^world", '_')).isEqualTo("hello_world");
		assertThat(FileTools.normalizeFilename("hello\u2714world", '_')).isEqualTo("hello_world");

	}

	@Test
	public void deletesDirectoryRecursively() throws Exception {
		File tempFolder = FileTools.createNewTempDir("testCollectEmptySuperFolders-" + RandomTools.newStandardUuid());

		File a = new File(tempFolder, "a");
		File b = new File(a, "b");
		b.mkdirs();

		assertThat(a.isDirectory()).isTrue();
		assertThat(b.isDirectory()).isTrue();

		FileTools.deleteDirectoryRecursively(tempFolder);

		assertThat(b.exists()).isFalse();
		assertThat(a.exists()).isFalse();
	}

	@Test
	public void testCollectEmptySuperFolders() throws Exception {

		File tempFolder = FileTools.createNewTempDir("testCollectEmptySuperFolders-" + RandomTools.newStandardUuid());
		try {

			List<File> result;

			// a/b/c -> collect all
			File test1 = new File(tempFolder, "test1");
			File a1 = new File(test1, "a");
			File b1 = new File(a1, "b");
			File c1 = new File(b1, "c");
			c1.mkdirs();
			result = FileTools.collectEmptySuperFolders(c1, test1);
			assertThat(result).containsExactly(c1, b1, a1);

			// a/b/c -> collect just a/b/c
			// a/b/d
			File test2 = new File(tempFolder, "test2");
			File a2 = new File(test2, "a");
			File b2 = new File(a2, "b");
			File c2 = new File(b2, "c");
			File d2 = new File(b2, "d");
			c2.mkdirs();
			d2.mkdirs();
			result = FileTools.collectEmptySuperFolders(c2, test2);
			assertThat(result).containsExactly(c2);

			// a/b/c -> collect a/b/c and a/b
			// a/test.txt
			File test3 = new File(tempFolder, "test3");
			File a3 = new File(test3, "a");
			File b3 = new File(a3, "b");
			File c3 = new File(b3, "c");
			c3.mkdirs();
			File f3 = new File(a3, "test.txt");
			FileTools.writeStringToFile(f3, "hello, world");
			result = FileTools.collectEmptySuperFolders(c3, test3);
			assertThat(result).containsExactly(c3, b3);

			// a/b/c/test.txt -> collect nothing
			File test4 = new File(tempFolder, "test4");
			File a4 = new File(test4, "a");
			File b4 = new File(a4, "b");
			File c4 = new File(b4, "c");
			c4.mkdirs();
			File f4 = new File(c4, "test.txt");
			FileTools.writeStringToFile(f4, "hello, world");
			result = FileTools.collectEmptySuperFolders(c4, test4);
			assertThat(result).isEmpty();

		} finally {
			FileTools.deleteDirectoryRecursively(tempFolder);
		}
	}

	@Test
	public void testFindRecursively() throws Exception {

		File tempDir = FileTools.createNewTempDir("testFindRecursively-" + RandomTools.newStandardUuid());
		try {

			File d1 = new File(tempDir, "d1");
			File d2 = new File(d1, "d2");
			File d3 = new File(d2, "d3");
			d3.mkdirs();
			File d2f1 = new File(d2, "d2f1.txt");
			FileTools.writeStringToFile(d2f1, "d2f1", "UTF-8");
			File d3f1 = new File(d3, "d3f1.txt");
			FileTools.writeStringToFile(d3f1, "d3f1", "UTF-8");

			List<File> list = FileTools.findRecursively(d1, null);
			assertThat(list.size()).isEqualTo(2);

			list = FileTools.findRecursively(d1, f -> {
				if (f.isDirectory() && f.getName().equals("d3")) {
					return false;
				}
				return true;
			});
			assertThat(list.size()).isEqualTo(1);

			list = FileTools.findRecursively(d1, f -> {
				if (f.isDirectory() && f.getName().startsWith("d2")) {
					return false;
				}
				return true;
			});
			assertThat(list.size()).isEqualTo(0);

		} finally {
			FileTools.deleteDirectoryRecursively(tempDir);
		}

	}

	@Test
	public void testTruncateFilenameByUtf8BytesLength() {
		assertThat(FileTools.truncateFilenameByUtf8BytesLength(null, 1)).isNull();
		assertThat(FileTools.truncateFilenameByUtf8BytesLength("h", 0)).isEqualTo("");
		assertThat(FileTools.truncateFilenameByUtf8BytesLength("hello.txt", 4)).isEqualTo("hell");
		assertThat(FileTools.truncateFilenameByUtf8BytesLength("hello.txt", 5)).isEqualTo("h.txt");
		assertThat(FileTools.truncateFilenameByUtf8BytesLength("\u0ca1.txt", 1)).isEqualTo("");
		assertThat(FileTools.truncateFilenameByUtf8BytesLength("hello world.txt", 9)).isEqualTo("hello.txt");
		assertThat(FileTools.truncateFilenameByUtf8BytesLength("h\u0ca1.txt", 5)).isEqualTo("h.txt");
		assertThat(FileTools.truncateFilenameByUtf8BytesLength("h\u0ca1.txt", 7)).isEqualTo("h.txt");
		assertThat(FileTools.truncateFilenameByUtf8BytesLength("h\u0ca1.txt", 8)).isEqualTo("h\u0ca1.txt");
		// assertThat(FileTools.truncateFilenameByBytesLength("\u0ca1\u0ca4", 3)).isEqualTo("\u0ca1");
		// assertThat(FileTools.truncateFilenameByBytesLength("\u0ca1\u0ca4", 4)).isEqualTo("\u0ca1");
		// assertThat(FileTools.truncateFilenameByBytesLength("\u0ca1\u0ca4", 6)).isEqualTo("\u0ca1\u0ca4");
		// assertThat(FileTools.truncateFilenameByBytesLength("\u0ca1\u0ca4", 7)).isEqualTo("\u0ca1\u0ca4");

	}

	@Test
	public void testCreateTempFileSecure() throws Exception {
		File file = FileTools.createTempFileSecure("test", ".tmp");
		assertThat(file).exists();

		String expected = "hello, world";
		IOTools.spit(file, expected, "UTF-8", false);
		String actual = IOTools.slurp(file, "UTF-8");
		assertThat(actual).isEqualTo(expected);
		file.delete();
	}
}
