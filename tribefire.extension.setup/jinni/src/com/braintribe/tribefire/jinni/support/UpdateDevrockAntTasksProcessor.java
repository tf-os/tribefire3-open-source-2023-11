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
package com.braintribe.tribefire.jinni.support;

import static com.braintribe.build.cmd.assets.PlatformSetupProcessor.outFileTransfer;
import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.red;
import static com.braintribe.console.ConsoleOutputs.yellow;
import static com.braintribe.model.service.api.result.Neutral.NEUTRAL;
import static com.braintribe.setup.tools.TfSetupOutputs.outProperty;
import static com.braintribe.setup.tools.TfSetupTools.artifactName;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.build.cmd.assets.api.ArtifactResolutionContext;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.jinni.api.UpdateDevrockAntTasks;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.model.version.Version;
import com.braintribe.setup.tools.TfSetupTools;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.lcd.NullSafe;

public class UpdateDevrockAntTasksProcessor extends AbstractUpdator implements ServiceProcessor<UpdateDevrockAntTasks, Neutral> {

	private static final String REPOSITORY_CONFIGURATION_DEVROCK_YAML = "repository-configuration-devrock.yaml";
	private static boolean BREAK = false;
	private static boolean CONTINUE = true;

	private static final String BT_JAR_NAME = "bt.devrock-ant-tasks-";
	private static final String BT_JAR_PATTERN = "^" + Pattern.quote(BT_JAR_NAME) + "\\d.*\\.jar$";

	private File antLibDir;
	private String libDirOrigin;
	
	@Required
	public void setLibDirOrigin(String libDirOrigin) {
		this.libDirOrigin = libDirOrigin;
	}
	
	@Configurable
	public void setLibDir(File antLibDir) {
		this.antLibDir = antLibDir;
	}

	@Override
	public Neutral process(ServiceRequestContext requestContext, UpdateDevrockAntTasks request) {
		withNewArtifactResolutionContext(arc -> doUpdate(request, arc));
		return NEUTRAL;
	}

	private void doUpdate(UpdateDevrockAntTasks request, ArtifactResolutionContext arc) {
		new DevrockAntTasksUpdater(request, arc).updateJinni();
	}

	private class DevrockAntTasksUpdater {

		private final PartIdentification libsType = PartIdentification.create("libs", "zip");

		private final UpdateDevrockAntTasks request;
		private final ArtifactResolutionContext resolutionContext;

		private List<File> btFiles;
		private String currentVersion;
		private CompiledArtifactIdentification latestDevrockAntTasks;
		private String latestVersion;
		private File libDir = antLibDir;

		private Resource devrockAntTasksZip;

		public DevrockAntTasksUpdater(UpdateDevrockAntTasks request, ArtifactResolutionContext arc) {
			this.request = request;
			this.resolutionContext = arc;
		}

		public void updateJinni() {
			if (tryDevrockAntTasksUpdate())
				printUpdateSuccessful();
		}

		private boolean tryDevrockAntTasksUpdate() {
			return initialize() && //
					shouldDoUpdate() && //
					downloadZip() && //
					backupExistingBtJars() && //
					extractNewFilesFromZip();
		}

		private void printUpdateSuccessful() {
			println("\ndevrock-ant-tasks successfully updated!");
		}

		private boolean initialize() {
			if (libDir == null) {
				outError(libDirOrigin);
				return BREAK;
			}

			outProperty("Ant lib folder via " + libDirOrigin, libDir.getAbsolutePath());
			
			if (!libDir.exists()) {
				outError("\nAnt lib folder does not exist.");
				return BREAK;
			}

			btFiles = existingBtFiles();

			currentVersion = currentVersion();
			if (currentVersion != null)
				outProperty("Current version", currentVersion);

			latestDevrockAntTasks = resolutionContext.resolveArtifactIdentification(devrockAntTasksArtifactNameWithTargetVersion());
			latestVersion = latestDevrockAntTasks.getVersion().asString();

			outProperty("Latest found version", latestVersion);

			return CONTINUE;
		}

		private List<File> existingBtFiles() {
			return Stream.of(libDir.listFiles()) //
					.filter(this::isBtFile) //
					.collect(Collectors.toList());
		}

		private boolean isBtFile(File f) {
			String name = f.getName();
			return !f.isDirectory() && name.startsWith("bt.") && name.endsWith(".jar");
		}

		private String currentVersion() {
			return btFiles.stream() //
					.map(File::getName) //
					.filter(name -> name.matches(BT_JAR_PATTERN)) //
					.map(t -> extractVersionFromBtAntTasksJarFileName(t)) // expects bt.devrock-ant-tasks-${version}.jar
					.findFirst() //
					.orElse(null);
		}

		private String extractVersionFromBtAntTasksJarFileName(String t) {
			t = t.substring(BT_JAR_NAME.length());
			t = t.substring(0, t.length() - ".jar".length());
			return t;
		}

		private String devrockAntTasksArtifactNameWithTargetVersion() {
			// Note that Malaclypse only considers major.minor, but otherwise resolves the latest revision.
			// So using currentJinni.version makes sense, it will resolve latest release with same major.minor
			String targetMajorMinor = NullSafe.get(request.getVersion(), currentVersion);
			if (targetMajorMinor == null) {
				targetMajorMinor = "[0,999999]";
				outWarning("No versions information given, and no previous version found in the ant lib folder. Will try to find the latest one.");
			}

			return artifactName("com.braintribe.devrock.ant", "devrock-ant-tasks", targetMajorMinor);
		}

		private boolean shouldDoUpdate() {
			if (currentVersion == null || request.getForce())
				return CONTINUE;

			if (latestVersion.endsWith("-pc")) {
				println("Will not compare versions and just do an update as the latest version is a pc.");
				return CONTINUE;
			}

			if (currentBtAndTasksIsUpToDate()) {
				outWarning("\nCurrent devrock-ant-tasks version seems to be up-to-date.");
				return BREAK;
			}

			return CONTINUE;
		}

		private boolean currentBtAndTasksIsUpToDate() {
			Version current = Version.parse(currentVersion);
			Version latest = latestDevrockAntTasks.getVersion();

			return !latest.isHigherThan(current);
		}

		private boolean downloadZip() {
			devrockAntTasksZip = resolutionContext.requirePart(latestDevrockAntTasks, libsType).getResource();

			if (devrockAntTasksZip instanceof FileResource)
				outProperty("Zip file", ((FileResource) devrockAntTasksZip).getPath());

			return CONTINUE;
		}

		private boolean backupExistingBtJars() {
			if (btFiles.isEmpty())
				return CONTINUE;

			String backupDirName = backupDirName();
			File result = new File(libDir, backupDirName);
			if (result.exists()) {
				outError("\nCannot backup jar files as backup folder already exists: " + result.getName() + ". Try again.");
				return BREAK;
			}

			if (!result.mkdir()) {
				outError("\nFailed to create backup folder: " + backupDirName);
				return BREAK;
			}

			outFileTransfer("Creating bt jars backup. Moving them", "", libDir.getAbsolutePath(), backupDirName);

			boolean allOk = moveBtFilesTo(result);
			if (!allOk) {
				outError("\nSome bt jars could not be moved to the backup folder. Please finish the update manually. Sorry.");
				return BREAK;
			}

			return CONTINUE;
		}

		private boolean moveBtFilesTo(File result) {
			boolean allOk = true;
			for (File btFile : btFiles)
				if (!btFile.renameTo(new File(result, btFile.getName()))) {
					outError("Error while backing up bt jar '" + btFile.getName());
					allOk = false;
				}

			return allOk;
		}

		private String backupDirName() {
			String v = currentVersion;
			return "bak-" + (v == null ? "" : v + "-") + DateTools.getCurrentDateString("yyyy-MM-dd--HH-mm-ss-SSS");
		}

		private boolean extractNewFilesFromZip() {
			println("Extracting zip file.");
			TfSetupTools.unzipResource(devrockAntTasksZip, libDir);

			return CONTINUE;
		}

		private void outWarning(String text) {
			println(yellow("WARNING: " + text));
		}

		private void outError(String text) {
			println(red(text));
		}

	}

}
