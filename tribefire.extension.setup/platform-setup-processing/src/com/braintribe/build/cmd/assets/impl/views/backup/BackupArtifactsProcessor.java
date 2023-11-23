package com.braintribe.build.cmd.assets.impl.views.backup;

import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.white;
import static com.braintribe.console.ConsoleOutputs.yellow;

import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.build.cmd.assets.impl.Constants;
import com.braintribe.build.cmd.assets.impl.views.RepositoryViewHelpers;
import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.devrock.mc.api.download.PartDownloadManager;
import com.braintribe.devrock.mc.api.download.PartDownloadScope;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryConfigurationLocator;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.PartAvailabilityReflection;
import com.braintribe.devrock.mc.core.configuration.ConfigurableRepositoryConfigurationLoader;
import com.braintribe.devrock.mc.core.configuration.RepositoryConfigurationLocators;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.consumable.PartReflection;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.platform.setup.api.GetLockedVersions;
import com.braintribe.processing.async.api.Promise;
import com.braintribe.utils.DOMTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.lcd.CommonTools;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

public class BackupArtifactsProcessor {

	public static final String BACKUP_REPORT = "report.yaml";

	/**
	 * Creates a backup of the specified artifacts by downloading them.
	 * @param artifactsToDownload the artifacts to download. Each string is supposed to be a fully qualified artifact including the full version (as e.g. provided by {@link GetLockedVersions}.
	 * @param generateMavenMetadata whether or not to generate metadata. To create a (usable) local repository, metadata is required. Otherwise, for a pure backup, it can be skipped.
	 */
	public static ArtifactsTransferReport process(List<String> artifactsToDownload, boolean generateMavenMetadata,
			VirtualEnvironment virtualEnvironment) {

		RepositoryConfigurationLocator configurationLocator = RepositoryConfigurationLocators.build().addLocationEnvVariable(Constants.DEVROCK_REPOSITORY_CONFIGURATION).done();
		
		ConfigurableRepositoryConfigurationLoader loader = new ConfigurableRepositoryConfigurationLoader();
		loader.setLocator(configurationLocator);
		loader.setVirtualEnvironment(virtualEnvironment);
		RepositoryConfiguration repositoryConfiguration = loader.get().get();
		
		ArtifactsTransferReport report = ArtifactsTransferReport.T.create();

		try (WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder(TransitiveResolverWireModule.INSTANCE) //
				.bindContract(RepositoryConfigurationContract.class, () -> Maybe.complete(repositoryConfiguration))
				.build()) {

			for (String artifactToDownload : artifactsToDownload) {
				Map<String, List<String>> partToRepositories = new HashMap<>();
				TransferredArtifact transferredArtifact = TransferredArtifact.T.create();
				report.getArtifacts().add(transferredArtifact);
				transferredArtifact.setName(artifactToDownload);
				println(yellow("\tdownloading " + artifactToDownload));

				// retrieve the part availability data
				CompiledArtifactIdentification compiledTargetIdentification = CompiledArtifactIdentification.parse(artifactToDownload);
				PartAvailabilityReflection partAvailabilityReflection = resolverContext.contract().dataResolverContract()
						.partAvailabilityReflection();

				List<PartReflection> allKnownPartsOfTerminal = partAvailabilityReflection.getAvailablePartsOf(compiledTargetIdentification);
				if (allKnownPartsOfTerminal.isEmpty()) {
					throw new IllegalStateException("No parts found for artifact: " + artifactToDownload + "\nAt least one part is expected!");
				}
				PartDownloadManager downloadManager = resolverContext.contract().dataResolverContract().partDownloadManager();
				PartDownloadScope partDownloadScope = downloadManager.openDownloadScope();

				if (!allKnownPartsOfTerminal.isEmpty()) {
					Map<CompiledPartIdentification, Promise<Maybe<ArtifactDataResolution>>> promises = new HashMap<>(allKnownPartsOfTerminal.size());

					Set<EqProxy<CompiledPartIdentification>> partsToDownload = new HashSet<>();
					List<String> downloadErrors = new ArrayList<>(allKnownPartsOfTerminal.size());

					for (PartReflection pr : allKnownPartsOfTerminal) {
						CompiledPartIdentification cpi = CompiledPartIdentification.from(compiledTargetIdentification, pr);
						if (partToRepositories.containsKey(cpi.asString())) {
							partToRepositories.get(cpi.asString()).add(pr.getRepositoryOrigin());
						} else {
							partToRepositories.put(cpi.asString(), CommonTools.getList(pr.getRepositoryOrigin()));
						}

						EqProxy<CompiledPartIdentification> cpiEqProxy = HashComparators.compiledPartIdentification.eqProxy(cpi);
						if (partsToDownload.add(cpiEqProxy)) {
							promises.put(cpi, partDownloadScope.download(cpi, cpi));
						}
					}
					List<CompiledPartIdentification> successfullyDownloadedParts = new ArrayList<>(promises.size());

					for (Map.Entry<CompiledPartIdentification, Promise<Maybe<ArtifactDataResolution>>> entry : promises.entrySet()) {
						println(white("\t\t" + entry.getKey().asString()));
						TransferredArtifactPart transferredArtifactPart = TransferredArtifactPart.T.create();
						transferredArtifactPart.setName(entry.getKey().asString());
						transferredArtifactPart.setRepositories(partToRepositories.get(entry.getKey().asString()));
						transferredArtifact.getParts().add(transferredArtifactPart);

						Maybe<ArtifactDataResolution> optional = entry.getValue().get();
						if (optional.isUnsatisfied()) {
							downloadErrors.add("couldn't download [" + entry.getKey().asString() + "]");
						} else {
							successfullyDownloadedParts.add(entry.getKey());
						}
					}
					if (!downloadErrors.isEmpty()) {
						throw new RuntimeException(downloadErrors.stream().collect(Collectors.joining("\t\n")));
					}
				}
			}
		}
		if (generateMavenMetadata) {
			generateMavenMetadata(artifactsToDownload, repositoryConfiguration.getLocalRepositoryPath());
		}
		report.sort();
		RepositoryViewHelpers.writeYamlFile(report, new File(repositoryConfiguration.getLocalRepositoryPath(), BACKUP_REPORT));
		return report;
	}

	// works only on an empty local repository (i.e. won't merge into any existing metadata) 
	public static void generateMavenMetadata(List<String> artifactsToDownload, String localRepositoryPath) {

		Map<String, List<String>> artifactIdsWithoutVersionToArtifacts = artifactsToDownload.stream()
				.collect(Collectors.groupingBy(artifactToDownload -> StringTools.getSubstringBefore(artifactToDownload, "#")));

		Document document = DOMTools.newDocumentBuilder().newDocument();
		Element rootElement = DOMTools.addChildElement(document, "metadata");
		rootElement.setAttribute("modelVersion", "1.1.0");
		Element groupIdElement = DOMTools.addChildElement(rootElement, "groupId");
		Element artitfactIdELement = DOMTools.addChildElement(rootElement, "artifactId");
		Element versionElement = DOMTools.addChildElement(rootElement, "version");
		Element versioningElement = DOMTools.addChildElement(rootElement, "versioning");
		Element latestElement = DOMTools.addChildElement(versioningElement, "latest");
		Element releaseElement = DOMTools.addChildElement(versioningElement, "release");

		Element versionsElement = DOMTools.addChildElement(versioningElement, "versions");
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");

		String dateString = format.format(new Date());
		DOMTools.addChildElement(versioningElement, "lastUpdated").setTextContent(dateString);

		artifactIdsWithoutVersionToArtifacts.forEach((artifactIdWithoutVersion, artifacts) -> {
			ArtifactIdentification artifactIdentification = ArtifactIdentification.parse(artifactIdWithoutVersion);
			String fullPath = RestoreArtifactsProcessor.buildPartialPath(artifactIdentification, localRepositoryPath);
			final File file = Paths.get(fullPath).resolve("maven-metadata.xml").toFile();

			groupIdElement.setTextContent(artifactIdentification.getGroupId());
			artitfactIdELement.setTextContent(artifactIdentification.getArtifactId());

			// removing concrete versionElements if exists from previous iteration
			while (versionsElement.hasChildNodes()) {
				versionsElement.removeChild(versionsElement.getFirstChild());
			}

			artifacts.forEach(artifact -> {
				VersionedArtifactIdentification artifactWithVersionIdentification = VersionedArtifactIdentification.parse(artifact);
				versionElement.setTextContent(artifactWithVersionIdentification.getVersion());
				latestElement.setTextContent(artifactWithVersionIdentification.getVersion());
				releaseElement.setTextContent(artifactWithVersionIdentification.getVersion());
				Element concreteVersionElement = DOMTools.addChildElement(versionsElement, "version");
				concreteVersionElement.setTextContent(artifactWithVersionIdentification.getVersion());
			});
			final String DomElementAsXmlString = com.braintribe.utils.xml.dom.DOMTools.format(rootElement);
			FileTools.writeStringToFile(file, DomElementAsXmlString);
		});

	}
}
