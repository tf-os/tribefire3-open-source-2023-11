package com.braintribe.build.cmd.assets.impl;

import static com.braintribe.build.cmd.assets.PlatformAssetDistributionConstants.FILENAME_SETUP_DESCRIPTOR;
import static com.braintribe.build.cmd.assets.PlatformAssetDistributionConstants.FILENAME_SETUP_INFO_DIR;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import com.braintribe.build.cmd.assets.PlatformSetupProcessor;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.model.platform.setup.api.CreateBackup;
import com.braintribe.model.platform.setup.api.data.SetupDescriptor;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.lcd.StringTools;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;

/**
 * Processes {@link CreateBackup} requests.
 */
public class CreateBackupProcessor {

	public static String process(CreateBackup request) {

		Instant start = Instant.now();

		File installationFolder = new File(request.getInstallationFolder());

		if (!installationFolder.exists()) {
			throw new IllegalStateException("Installation folder " + installationFolder.getAbsolutePath() + " doesn't exist!");
		}

		File tribefirePropertiesFile = new File(installationFolder, "conf/tribefire.properties");
		if (!tribefirePropertiesFile.exists()) {
			throw new IllegalStateException(
					"Specified folder (" + installationFolder.getAbsolutePath() + ") is not a valid installation folder (file "
							+ tribefirePropertiesFile.getAbsolutePath() + " not found)! Please try again with a valid installation path.");
		}

		File setupDescriptorFile = new File(installationFolder + "/" + FILENAME_SETUP_INFO_DIR, FILENAME_SETUP_DESCRIPTOR);
		if (!setupDescriptorFile.exists()) {
			throw new IllegalStateException("Setup descriptor file (" + setupDescriptorFile.getAbsolutePath() + ") is not found!");
		}

		String detailedBackupFileName = "";
		if(request.getIncludeHostName()) {
			String hostname = PlatformSetupProcessor.determineHostname();
			if(StringTools.isEmpty(hostname)) {
				throw new IllegalStateException("Could not determine hostname!");
			}
			detailedBackupFileName = "-" + hostname;
		}

		SetupDescriptor setupDescriptor = (SetupDescriptor) FileTools.read(setupDescriptorFile).fromInputStream(new YamlMarshaller()::unmarshall);

		String projectName = "tribefire"; // default value TODO: should be set with initializer?
		if (setupDescriptor.getProjectDescriptor() != null && !StringTools.isEmpty(setupDescriptor.getProjectDescriptor().getName())) {
			projectName = setupDescriptor.getProjectDescriptor().getName();
		}

		String backupFileName = request.getBackupFilename();
		if (StringTools.isEmpty(backupFileName)) {
			detailedBackupFileName = projectName + "-" + setupDescriptor.getVersion() + detailedBackupFileName;
			String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
			backupFileName = detailedBackupFileName + "-" + timeStamp;
		}

		String backupFolderPath = request.getBackupFolder();
		if (StringTools.isEmpty(backupFolderPath)) {
			backupFolderPath = "/opt/braintribe/backups/" + projectName;
		}

		File backupFile = new File(backupFolderPath, backupFileName + ".zip");
		File backupFolder = backupFile.getParentFile();
		if ((backupFolder.exists() && !backupFolder.canWrite()) || (!backupFolder.exists() && !backupFolder.mkdirs())) {
			throw new IllegalStateException("Do not have write permissions in folder " + backupFolderPath);
		}

		zipFolder(installationFolder, backupFile);

		// String installationFolderSize = getFileSizeInMegaBytes(installationFolder);
		String backupFileSize = getFileSizeInMegaBytes(backupFile);

		Instant finish = Instant.now();
		long timeElapsed = Duration.between(start, finish).getSeconds();

		// @formatter:off
		return "Successfully created backup file: " + backupFile.getAbsolutePath() + " (" + backupFileSize + ")" 
			//+ "\nInitial size: " + installationFolderSize
			+ "\nDuration: " + timeElapsed + " secs";
		// @formatter:on
	}

	private static void zipFolder(File src, File archive) {
		ZipFile zipFile = new ZipFile(archive);

		ZipParameters parameters = new ZipParameters();
		parameters.setCompressionMethod(CompressionMethod.DEFLATE);
		parameters.setCompressionLevel(CompressionLevel.NORMAL);
		parameters.setIncludeRootFolder(false);

		try {
			zipFile.addFolder(src, parameters);
		} catch (ZipException e) {
			throw new RuntimeException("Failed while zipping " + src.getAbsolutePath() + " in " + archive.getAbsolutePath(), e);
		}
	}

	public static String getFileSizeInMegaBytes(File file) {
		long size = 0;
		if (file.isDirectory()) {
			size = getFolderSizeInMegaBytes(file);
		} else {
			size = file.length();
		}
		// @formatter:on
		NumberFormat formatter = new DecimalFormat("#0.00");
		return formatter.format((double) size / (1024 * 1024)) + " MB";
	}

	private static long getFolderSizeInMegaBytes(File folder) {
		// @formatter:off
		long size = 0;
		try {
			size = Files.walk(folder.toPath())
		      .filter(p -> p.toFile().isFile())
		      .mapToLong(p -> p.toFile().length())
		      .sum();
		} catch (IOException e) {
			throw new RuntimeException("Could not calculate the size of the folder: " + folder.getAbsolutePath());
		}
		return size;
	}
}
