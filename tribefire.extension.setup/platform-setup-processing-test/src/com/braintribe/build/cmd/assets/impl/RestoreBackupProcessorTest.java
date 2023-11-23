package com.braintribe.build.cmd.assets.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.util.List;

import org.assertj.core.util.Files;
import org.junit.Test;

import com.braintribe.model.platform.setup.api.RestoreBackup;
import com.braintribe.testing.test.AbstractTest;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.OsTools;

/**
 * Provides tests for {@link RestoreBackupProcessor}.
 */
public class RestoreBackupProcessorTest extends AbstractTest {

	@Test
	public void testRestoreBackup() {

		String actualInstallationFolder = newTempDir().getAbsolutePath();
		String expectedInstallationFolder = testPath("expectedInstallationFolder").toString();

		RestoreBackup request = RestoreBackup.T.create();
		request.setInstallationFolder(actualInstallationFolder);
		request.setBackupArchive(testFile("backup-file.zip").getAbsolutePath());

		// make sure that the installation folder does not exist
		FileTools.deleteDirectoryRecursivelyUnchecked(new File(actualInstallationFolder));
		RestoreBackupProcessor.process(request);

		assertFolders(actualInstallationFolder, expectedInstallationFolder);
	}

	@Test
	public void testRestoreBackupWithPreviousInstallation() {

		String actualInstallationFolder = newTempDir().getAbsolutePath();
		String expectedInstallationFolder = testPath("expectedInstallationFolder").toString();

		RestoreBackup request = RestoreBackup.T.create();
		
		// Create an installation folder. Restore must fail cause it is not a valid tribefire installation folder.
		File actualInstallationFolderFile = new File(actualInstallationFolder);
		File createNewTempFile = new File(actualInstallationFolderFile, "SomeFile.txt");
		FileTools.writeStringToFile(createNewTempFile, "Some content");
		FileTools.copyFileToExistingDirectory(createNewTempFile, actualInstallationFolderFile);
	
		request.setInstallationFolder(actualInstallationFolder);
		request.setBackupArchive(testFile("backup-file.zip").getAbsolutePath());

		// @formatter:off
		assertThatThrownBy(() ->  RestoreBackupProcessor.process(request))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageMatching("Specified folder.*is not a valid installation folder.*Please try again with a valid installation path.");
		// @formatter:on

		// Preparing a tribefire folder. Since we haven't set the force option to true, the installation will throw an Exception.  
		FileTools.copyDirectoryUnchecked(new File(expectedInstallationFolder), actualInstallationFolderFile);
		// @formatter:off
		assertThatThrownBy(() ->  RestoreBackupProcessor.process(request))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageMatching("Warning! The installation folder.*already exists! Please move or delete the folder before running a restore command\\. One can also force restore \\(--force true\\).");
		// @formatter:on
		
		request.setForce(true);	
		RestoreBackupProcessor.process(request);
		assertFolders(actualInstallationFolder, expectedInstallationFolder);
		
		// Check if backup folder is created and its content
		File[] listFiles = actualInstallationFolderFile.getParentFile().listFiles(file -> file.isDirectory() && file.getName().startsWith(actualInstallationFolderFile.getName() + "-backup"));
		assertThat(listFiles).hasSize(1);
		
		// This fails on windows, because there are somefile.txt and SomeFile.txt in place, but on windows it's the same file
		if (!OsTools.isWindowsOperatingSystem())
			assertFolders(listFiles[0].getAbsolutePath(), expectedInstallationFolder, CollectionTools.getList(createNewTempFile.getName()));
	}
	
	private void assertFolders(String actualFolder, String expectedFolder, List<String> additionalExpectedFiles) {
		List<String> actualFileNames = Files.fileNamesIn(actualFolder, true);
		List<String> expectedFileNames = Files.fileNamesIn(expectedFolder, true);
		expectedFileNames.addAll(additionalExpectedFiles);
		actualFileNames.replaceAll(s -> s.replace(actualFolder + File.separator, ""));
		expectedFileNames.replaceAll(s -> s.replace(expectedFolder + File.separator, ""));

		assertThat(actualFileNames).containsExactlyInAnyOrderElementsOf(expectedFileNames);
	}
	
	private void assertFolders(String actualFolder, String expectedFolder) {
		assertFolders(actualFolder, expectedFolder, CollectionTools.getList());
	}
	
}
