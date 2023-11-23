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

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.junit.After;
import org.junit.Test;

public class FileToolsCopyDirectoryTest {

	private File target;

	@Test
	public void testDirCopy() throws IOException {
		File source = FileTools.getFile("res/DirCopyTest/", true, false, false, true, true);
		target = FileTools.createNewTempDir("tempFile" + UUID.randomUUID());
		FileTools.copyDirectory(source, target);

		File file1 = new File(target, "folder1/file1");
		File file2 = new File(target, "folder2/file2");
		File file11 = new File(target, "folder1/folder11/file11");

		File emptyFolder = new File(target, "emptyFolder");

		assertFilesExistAndAreFiles(file1, file2, file11);
		assertFilesExistAndAreDirectories(emptyFolder);
	}

	// PGA: What exactly is this test doing on Linux? At what point is the exception expected?
	@Test(expected = RuntimeException.class)
	public void testDirCopy_noFollowLinks() throws IOException {
		if (OsTools.getOperatingSystem() == OsTools.OperatingSystem.Linux) {
			File source = FileTools.getFile("res/DirCopyTestWithLinks/", true, false, false, true, true);
			target = FileTools.createNewTempDir("tempFile" + UUID.randomUUID());
			FileTools.copyDirectory(source, target);
		} else {
			throw new RuntimeException("thrown on purpose");
		}
	}

	@Test
	public void testDirCopy_followLinks() throws IOException {
		if (OsTools.getOperatingSystem().equals(OsTools.OperatingSystem.Linux)) {
			File source = FileTools.getFile("res/DirCopyTestWithLinks/", true, false, false, true, true);
			target = FileTools.createNewTempDir("tempFile" + UUID.randomUUID());
			FileTools.copyDirectory(source, target, true);

			File file1 = new File(target, "folder1/file1");
			File file2 = new File(target, "folder2/file2");
			File file11 = new File(target, "folder1/folder11/file11");
			File linkFile1 = new File(target, "linkFile1");

			File linkFolder1 = new File(target, "linkFolder1");
			File linkFolder1_file1 = new File(target, "linkFolder1/file1");
			File linkFolder1_file11 = new File(target, "linkFolder1/folder11/file11");

			assertFilesExistAndAreFiles(file1, file2, file11, linkFile1, linkFolder1_file1, linkFolder1_file11);
			assertFilesExistAndAreDirectories(linkFolder1);
		}
	}

	@After
	public void cleanUp() throws IOException {
		FileTools.deleteDirectoryRecursively(target);
	}

	private void assertFilesExistAndAreFiles(File... files) {
		for (File file : files) {
			assertThat(file).exists();
			assertThat(file).isFile();
		}
	}

	private void assertFilesExistAndAreDirectories(File... files) {
		for (File file : files) {
			assertThat(file).exists();
			assertThat(file).isDirectory();
		}
	}
}
