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

import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.red;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.console.ConsoleOutputs.yellow;
import static com.braintribe.model.service.api.result.Neutral.NEUTRAL;
import static com.braintribe.setup.tools.TfSetupOutputs.outProperty;
import static com.braintribe.setup.tools.TfSetupTools.artifactName;

import java.io.File;
import java.util.stream.Stream;

import com.braintribe.build.cmd.assets.api.ArtifactResolutionContext;
import com.braintribe.cfg.Required;
import com.braintribe.console.ConsoleOutputs;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.jinni.api.UpdateJinni;
import com.braintribe.model.jinni.api.UpdateJinniRequest;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.processing.service.impl.ServiceProcessors;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.model.version.Version;
import com.braintribe.setup.tools.TfSetupTools;
import com.braintribe.utils.OsTools;
import com.braintribe.utils.UnixTools;

public class UpdateJinniProcessor extends AbstractUpdator implements ServiceProcessor<UpdateJinniRequest, Neutral> {

	private static boolean BREAK = false;
	private static boolean CONTINUE = true;
	private static final String JINNI_UPDATE_FOLDER = "jinni-update";

	private File installationDir;
	private ServiceProcessor<UpdateJinniRequest, Neutral> dispatcher = ServiceProcessors.dispatcher(this::configureDispatching); 
	
	@Required
	public void setJinniInstallationDir(File installationDir) {
		this.installationDir = installationDir;
	}

	protected void configureDispatching(DispatchConfiguration<UpdateJinniRequest, Neutral> dispatching) {
		dispatching.register(UpdateJinni.T, (ctx, request) -> updateJinni(request));
	}
	
	@Override
	public Neutral process(ServiceRequestContext requestContext, UpdateJinniRequest request) {
		return dispatcher.process(requestContext, request);
	}

	private Neutral updateJinni(UpdateJinni request) {
		withNewArtifactResolutionContext(arc -> doUpdate(request, arc));

		return NEUTRAL;
	}

	private void doUpdate(UpdateJinni request, ArtifactResolutionContext arc) {
		new JinniUpdater(request, arc).updateJinni();
	}

	private class JinniUpdater {

		private final PartIdentification applicationType = PartIdentification.create("application", "zip");

		private final String targetJinniMajorMinor;
		private final boolean force;
		private final ArtifactResolutionContext resolutionContext;

		private PackagedSolution currentJinni;
		private CompiledArtifactIdentification latestJinni;
		private String latestJinniVersion;

		private File targetFolder;
		private Resource jinniZip;

		public JinniUpdater(UpdateJinni request, ArtifactResolutionContext arc) {
			targetJinniMajorMinor = request.getVersion();
			force = request.getForce();
			resolutionContext = arc;
		}

		public void updateJinni() {
			if (tryJinniUpdate())
				printUpdateSuccessful();
		}

		private boolean tryJinniUpdate() {
			return initialize() && //
					jinniCanBeUpdated() && //
					pepareFiles() && //
					extractJinniFiles() && //
					makeExecutableIfOnUnix();
		}

		private void printUpdateSuccessful() {
			outProperty("Jinni prepared in", targetFolderFor().getAbsolutePath());
		}

		private boolean initialize() {
			this.currentJinni = currentJinni();
			outProperty("Current jinni version", currentJinni.version);

			this.latestJinni = resolutionContext.resolveArtifactIdentification(jinniArtifactNameWithTargetVersion());
			this.latestJinniVersion = latestJinni.getVersion().asString();

			outProperty("Latest found jinni version", latestJinniVersion);

			return CONTINUE;
		}

		private PackagedSolution currentJinni() {
			return PackagedSolution.readSolutionsFrom(installationDir).stream() //
					.filter(this::isJinni) //
					.findFirst() //
					.orElseThrow(() -> new GenericModelException("Unable to determine current Jinni version."));
		}

		private boolean isJinni(PackagedSolution s) {
			return s.groupId.equals("tribefire.extension.setup") && s.artifactId.equals("jinni");
		}

		private String jinniArtifactNameWithTargetVersion() {
			// Note that Malaclypse only considers major.minor, but otherwise resolves the latest revision.
			// So using currentJinni.version makes sense, it will resolve latest release with same major.minor
			String targetMajorMinor = targetJinniMajorMinor != null ? targetJinniMajorMinor : currentJinni.version;
			return artifactName("tribefire.extension.setup", "jinni", targetMajorMinor);
		}

		private boolean jinniCanBeUpdated() {
			if (force)
				return CONTINUE;

			if (currentJinniIsUpToDate()) {
				outInfo("\nCurrent jinni version was already up-to-date.");
				return BREAK;
			}

			return CONTINUE;
		}

		private boolean currentJinniIsUpToDate() {
			Version current = Version.parse(currentJinni.version);
			Version latest = latestJinni.getVersion();

			return !latest.isHigherThan(current);
		}

		private boolean pepareFiles() {
			targetFolder = targetFolderFor();
			if (targetFolder.exists()) {
				outError("\nTarget folder for the latest jinni version already exists: " + targetFolder.getAbsolutePath());
				return BREAK;
			}

			jinniZip = resolutionContext.requirePart(latestJinni, applicationType).getResource();

			return CONTINUE;
		}

		private File targetFolderFor() {
			return new File(installationDir, JINNI_UPDATE_FOLDER);
		}

		private boolean extractJinniFiles() {
			outProperty("Extracting latest jinni version", latestJinniVersion);
			TfSetupTools.unzipResource(jinniZip, targetFolder);

			return CONTINUE;
		}

		private void outWarning(String text) {
			println(sequence(yellow("Warning: "), text(text)));
		}
		
		private void outError(String text) {
			println(sequence(red("Error: "), text(text)));
		}
		
		private void outInfo(String text) {
			println(sequence(ConsoleOutputs.brightBlue("Info: "), text(text)));
		}

		private boolean makeExecutableIfOnUnix() {
			if (!OsTools.isUnixSystem())
				return CONTINUE;

			File binFolder = new File(targetFolder, "bin");
			
			Stream<File> bashScriptStream = Stream.of(binFolder.listFiles()).filter(File::isFile).filter(this::isBashScript);
			
			UnixTools.setUnixFilePermissions(bashScriptStream, "rwxr--r--");

			return CONTINUE;
		}
		
		private boolean isBashScript(File file) {
			return !file.getName().endsWith(".bat");
		}
	}

}
