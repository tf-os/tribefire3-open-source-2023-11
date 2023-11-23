// ========================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2021 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission.
// To this file the Braintribe License Agreement applies.
// ========================================================================

package com.braintribe.build.cmd.assets.impl;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThatExecuting;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.braintribe.build.cmd.assets.impl.UpdateGroupVersionProcessor.MajorMinorVersion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.platform.setup.api.SetupRequest;
import com.braintribe.model.platform.setup.api.UpdateGroupVersion;
import com.braintribe.testing.test.AbstractTest;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.FileTools;

/**
 * Provides {@link UpdateGroupVersionProcessor} tests.
 *
 * @author michael.lafite
 */
public class UpdateGroupVersionProcessorTest extends AbstractTest {

	@Test
	public void testUpdateOldPoms() {
		String inputSubfolderName = "valid-old-poms";
		UpdateGroupVersion request = copyGroupFolderAndCreateRequest(inputSubfolderName);
		request.setVersion("3.2");
		UpdateGroupVersionProcessor.process(request);
		assertThatActualMatchesExpectedFolder(inputSubfolderName);
	}

	@Test
	public void testUpdateNewPoms() {
		String inputSubfolderName = "valid-new-poms";
		UpdateGroupVersion request = copyGroupFolderAndCreateRequest(inputSubfolderName);
		request.setVersion("3.2");
		UpdateGroupVersionProcessor.process(request);
		assertThatActualMatchesExpectedFolder(inputSubfolderName);
	}

	@Test
	public void testGetNewVersion() {
		MajorMinorVersion currentVersion = new MajorMinorVersion(2, 4);
		UpdateGroupVersion request = UpdateGroupVersion.T.create();

		assertThatExecuting(() -> UpdateGroupVersionProcessor.getNewVersion(currentVersion, request)).fails().withUncheckedExceptionWhich()
				.hasMessageMatching(".*version.*incrementMajor.*incrementMinor.*"); // nothing set

		request.setIncrementMajor(true);
		request.setIncrementMinor(true);
		assertThatExecuting(() -> UpdateGroupVersionProcessor.getNewVersion(currentVersion, request)).fails().withUncheckedExceptionWhich()
				.hasMessageMatching(".*incrementMajor.*incrementMinor.*not both.*");

		request.setVersion("3.4");
		// still the same exception, since we check for incrementMajor/incrementMinor first
		assertThatExecuting(() -> UpdateGroupVersionProcessor.getNewVersion(currentVersion, request)).fails().withUncheckedExceptionWhich()
				.hasMessageMatching(".*incrementMajor.*incrementMinor.*not both.*");

		request.setIncrementMajor(false);
		request.setIncrementMinor(true);

		assertThatExecuting(() -> UpdateGroupVersionProcessor.getNewVersion(currentVersion, request)).fails().withUncheckedExceptionWhich()
				.hasMessageMatching(".*version.*incrementMinor.*not both.*");

		request.setIncrementMajor(true);
		request.setIncrementMinor(false);

		assertThatExecuting(() -> UpdateGroupVersionProcessor.getNewVersion(currentVersion, request)).fails().withUncheckedExceptionWhich()
				.hasMessageMatching(".*version.*incrementMinor.*not both.*");

		request.setIncrementMajor(false);

		assertThat(UpdateGroupVersionProcessor.getNewVersion(currentVersion, request).toString()).isEqualTo(request.getVersion());

		request.setVersion(null);
		request.setIncrementMinor(true);
		assertThat(UpdateGroupVersionProcessor.getNewVersion(currentVersion, request).toString()).isEqualTo("2.5");

		request.setIncrementMinor(false);
		request.setIncrementMajor(true);
		assertThat(UpdateGroupVersionProcessor.getNewVersion(currentVersion, request).toString()).isEqualTo("3.0");

		request.setIncrementMajor(false);
		request.setVersion("4.5.6");
		assertThatExecuting(() -> UpdateGroupVersionProcessor.getNewVersion(currentVersion, request)).fails().withUncheckedExceptionWhich()
				.hasMessageMatching(".*invalid.*Please specify as 'major.minor'.*");
	}

	@Test
	public void testGetEclipseProjectNameWithoutVersion() {
		MajorMinorVersion majorMinorVersion = new MajorMinorVersion(2, 3);

		// short project name (based on artifactId) + groupId as prefix and suffix are supported.
		// since there is no major.minor version, the same string is returned.
		assertThat(
				UpdateGroupVersionProcessor.getEclipseProjectNameWithoutVersion("example-setup", majorMinorVersion, "org.example", "example-setup"))
						.isEqualTo("example-setup");
		assertThat(UpdateGroupVersionProcessor.getEclipseProjectNameWithoutVersion("org.example.example-setup", majorMinorVersion, "org.example",
				"example-setup")).isEqualTo("org.example.example-setup");
		assertThat(UpdateGroupVersionProcessor.getEclipseProjectNameWithoutVersion("example-setup - org.example", majorMinorVersion, "org.example",
				"example-setup")).isEqualTo("example-setup - org.example");
		assertThat(UpdateGroupVersionProcessor.getEclipseProjectNameWithoutVersion("org.example.parent", majorMinorVersion, "org.example", "parent"))
				.isEqualTo("org.example.parent");
		assertThat(
				UpdateGroupVersionProcessor.getEclipseProjectNameWithoutVersion("parent - org.example", majorMinorVersion, "org.example", "parent"))
						.isEqualTo("parent - org.example");

		// the major.minor version is part of the project name AND it is a suffix AND it has the correct version.
		// in that case the version is removed
		assertThat(UpdateGroupVersionProcessor.getEclipseProjectNameWithoutVersion("example-setup-2.3", majorMinorVersion, "org.example",
				"example-setup")).isEqualTo("example-setup");
		assertThat(UpdateGroupVersionProcessor.getEclipseProjectNameWithoutVersion("org.example.example-setup-2.3", majorMinorVersion, "org.example",
				"example-setup")).isEqualTo("org.example.example-setup");

		// wrong version number
		assertThatExecuting(() -> UpdateGroupVersionProcessor.getEclipseProjectNameWithoutVersion("example-setup-2.2", majorMinorVersion,
				"org.example", "example-setup")).fails().withUncheckedExceptionWhich()
						.hasMessageContaining("contains a major.minor version, but not the expected one");

		// unexpected format
		assertThatExecuting(() -> UpdateGroupVersionProcessor.getEclipseProjectNameWithoutVersion("example-setup-2.3-bla", majorMinorVersion,
				"org.example", "example-setup")).fails().withUncheckedExceptionWhich().hasMessageContaining("but not as expected suffix");
	}

	@Test
	public void testValidateGroupFolder() {
		assertThatExecuting(() -> UpdateGroupVersionProcessor.validateGroupFolder(gf("dependency-management-section"))).fails()
				.withUncheckedExceptionWhich().hasMessageContaining("'dependencyManagement' section").hasMessageContaining("remove");
		{
			// same as above, only purpose is to make sure that validation is also run as part of process method
			UpdateGroupVersion request = UpdateGroupVersion.T.create();
			request.setGroupFolder(gf("dependency-management-section").getPath());
			request.setIncrementMinor(true);
			assertThatExecuting(() -> UpdateGroupVersionProcessor.process(request)).fails().withUncheckedExceptionWhich()
					.hasMessageContaining("'dependencyManagement' section").hasMessageContaining("remove");
		}

		assertThatExecuting(() -> UpdateGroupVersionProcessor.validateGroupFolder(gf("invalid-group-folder"))).fails().withUncheckedExceptionWhich()
				.hasMessageContaining("'invalid-group-name' is not a valid group id");

		assertThatExecuting(() -> UpdateGroupVersionProcessor.validateGroupFolder(gf("invalid-minor-version"))).fails().withUncheckedExceptionWhich()
				.hasMessageContaining("--invalid minor version--");

		assertThatExecuting(() -> UpdateGroupVersionProcessor.validateGroupFolder(gf("invalid-parent-pom"))).fails().withUncheckedExceptionWhich()
				.hasMessageContaining("pom.xml is not a valid XML") // message part including file name
				.hasMessageContaining("--- not a valid POM ---"); // file content

		assertThatExecuting(() -> UpdateGroupVersionProcessor.validateGroupFolder(gf("missing-group-build-script"))).fails()
				.withUncheckedExceptionWhich().hasMessageContaining("build.xml not found!");

		assertThatExecuting(() -> UpdateGroupVersionProcessor.validateGroupFolder(gf("missing-parent-folder"))).fails().withUncheckedExceptionWhich()
				.hasMessageContaining("parent not found!");

		assertThatExecuting(() -> UpdateGroupVersionProcessor.validateGroupFolder(gf("missing-parent-pom"))).fails().withUncheckedExceptionWhich()
				.hasMessageContaining("pom.xml not found!");

		assertThatExecuting(() -> UpdateGroupVersionProcessor.validateGroupFolder(gf("wrong-group"))).fails().withUncheckedExceptionWhich()
				.hasMessageContaining("'org.example'") // the group folder
				.hasMessageContaining("'wrong.group'") // the group specified in the parent
				.hasMessageContaining("doesn't match"); // part of the message
	}

	private File gf(String inputSubfolderName) {
		return gf(getClass(), inputSubfolderName);
	}

	private UpdateGroupVersion copyGroupFolderAndCreateRequest(String inputSubfolderName) {
		return copyGroupFolderAndCreateRequest(getClass(), UpdateGroupVersion.T, inputSubfolderName);
	}
	
	private void assertThatActualMatchesExpectedFolder(String inputSubfolderName) {
		assertThatActualMatchesExpectedFolder(getClass(), inputSubfolderName);
	}
	
	static File gf(Class<? extends AbstractTest> testClass, String inputSubfolderName) {
		File groupFolderParent = existingTestDir(testClass, "input/" + inputSubfolderName);
		List<File> groupFolderParentFiles = CollectionTools.getParameterizedList(File.class, (Object[]) groupFolderParent.listFiles());
		if (groupFolderParentFiles.size() != 1) {
			throw new IllegalStateException("Folder " + groupFolderParent
					+ " does not have exactly one sub folder. This is an error in the test resources, not in the implementation.");
		}
		File groupFolder = groupFolderParentFiles.get(0);
		return groupFolder;
	}
	
	static <R extends SetupRequest> R copyGroupFolderAndCreateRequest(Class<? extends AbstractTest> testClass, EntityType<R> requestType, String inputSubfolderName) {
		File groupFolder = gf(testClass, inputSubfolderName);
		File groupFolderParent = groupFolder.getParentFile();

		// copy folder first and then update the copy folder only

		File copyGroupFolderParent = testDir(testClass, "output/" + groupFolderParent.getName() + "/actual");
		if (copyGroupFolderParent.exists()) {
			try {
				FileTools.deleteDirectoryRecursively(copyGroupFolderParent);
			} catch (IOException e) {
				throw new IllegalStateException("Couldn't delete folder " + copyGroupFolderParent + "!", e);
			}
		}

		File copyGroupFolder = new File(copyGroupFolderParent, groupFolder.getName());
		try {
			FileTools.copyDirectory(groupFolder, copyGroupFolder);
		} catch (IOException e) {
			throw new IllegalStateException("Couldn't copy folder " + groupFolder + " to " + copyGroupFolder + "!", e);
		}

		groupFolder = copyGroupFolder;

		R request = requestType.create();
		// use reflection to set group folder (property could be moved to common super type instead)
		requestType.getProperty(UpdateGroupVersion.groupFolder).set(request, groupFolder.getPath());
		
		return request;
	}
	
	static void assertThatActualMatchesExpectedFolder(Class<? extends AbstractTest> testClass, String inputSubfolderName) {
		File actual = existingTestDir(testClass, "output/" + inputSubfolderName + "/actual");
		File expected = existingTestDir(testClass, "output/" + inputSubfolderName + "/expected");
		recursivelyAssertThatActualMatchesExpectedFolder(actual, expected);
	}

	static void recursivelyAssertThatActualMatchesExpectedFolder(File actual, File expected) {
		assertThat(actual.listFiles())
				.as("Folders " + actual.getAbsolutePath() + " and " + expected.getAbsolutePath() + " do not have the same number files!")
				.hasSameSizeAs(expected.listFiles());

		for (File file : actual.listFiles()) {
			if (file.isDirectory()) {
				recursivelyAssertThatActualMatchesExpectedFolder(new File(actual, file.getName()), new File(expected, file.getName()));
			} else {
				assertThat(file).hasSameTextualContentAs(new File(expected, file.getName()));
			}
		}
	}
}
