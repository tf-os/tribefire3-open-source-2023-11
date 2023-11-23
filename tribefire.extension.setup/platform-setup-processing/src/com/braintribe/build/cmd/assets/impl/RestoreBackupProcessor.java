package com.braintribe.build.cmd.assets.impl;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import com.braintribe.model.platform.setup.api.RestoreBackup;
import com.braintribe.utils.FileTools;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 * Processes {@link RestoreBackup} requests.
 */
public class RestoreBackupProcessor {

	public static String process(RestoreBackup request) {

		Instant start = Instant.now();

		File installationFolder = new File(request.getInstallationFolder());

		if (installationFolder.exists()) {
			File tribefirePropertiesFile = new File(installationFolder, "conf/tribefire.properties");

			if (tribefirePropertiesFile.exists()) {
				if(request.getForce()) {
					try {
						String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
						File backupInstallationFolder = new File(installationFolder.getAbsolutePath() + "-backup-" + timeStamp);
						FileTools.copyDirectoryUnchecked(installationFolder, backupInstallationFolder);
						FileTools.deleteDirectoryRecursively(installationFolder);
					} catch (IOException e) {
						throw new IllegalStateException("Failed while deleting " + installationFolder.getAbsolutePath());			
					}					
				} else {
					throw new IllegalStateException("Warning! The installation folder " + installationFolder.getAbsolutePath()
							+ " already exists! Please move or delete the folder before running a restore command. One can also force restore (--force true).");					
				}
			} else {
				throw new IllegalStateException(
						"Specified folder (" + installationFolder.getAbsolutePath() + ") is not a valid installation folder (file "
								+ tribefirePropertiesFile.getAbsolutePath() + " not found)! Please try again with a valid installation path.");
			}
		}

		File backupFile = new File(request.getBackupArchive());
		if (!backupFile.exists()) {
			throw new IllegalStateException("Backup file " + backupFile.getAbsolutePath() + " not found!");
		}

		unzipArchive(backupFile, installationFolder);

		Instant finish = Instant.now();
		long timeElapsed = Duration.between(start, finish).getSeconds();

		// @formatter:off
		return "Successfully restored backup: " + installationFolder.getAbsolutePath()
			+ "\nDuration: " + timeElapsed + " secs";
		// @formatter:on
	}

	public static void unzipArchive(File src, File dst) {
		try {
			ZipFile zipFile = new ZipFile(src);
			zipFile.extractAll(dst.getAbsolutePath());
		} catch (ZipException e) {
			throw new IllegalStateException("Failed while unzipping " + src.getAbsolutePath() + " to " + dst.getAbsolutePath(), e);
		}
	}
}
