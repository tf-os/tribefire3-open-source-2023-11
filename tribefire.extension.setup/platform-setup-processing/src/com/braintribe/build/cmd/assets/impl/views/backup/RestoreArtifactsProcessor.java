package com.braintribe.build.cmd.assets.impl.views.backup;

import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.white;
import static com.braintribe.console.ConsoleOutputs.yellow;
import static java.util.stream.Collectors.joining;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.build.cmd.assets.impl.Constants;
import com.braintribe.build.cmd.assets.impl.views.RepositoryViewHelpers;
import com.braintribe.devrock.mc.api.deploy.ArtifactDeployer;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.resolver.ArtifactDataResolverModule;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.artifact.consumable.ArtifactResolution;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.platform.setup.api.BackupArtifacts;
import com.braintribe.model.platform.setup.api.RestoreArtifacts;
import com.braintribe.model.resource.FileResource;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.date.ExtSimpleDateFormat;
import com.braintribe.utils.lcd.CommonTools;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * Process {@link RestoreArtifacts} requests to restore artifacts from
 * a previously created backup, see {@link BackupArtifacts}.
 */
// note that there is currently no unit test for this processor.
// we do have the backup/restore test pipeline though.
public class RestoreArtifactsProcessor {

	public static void process(RestoreArtifacts restoreArtifacts, VirtualEnvironment virtualEnvironment) {

		final File reportFile = new File(restoreArtifacts.getFolder(), BackupArtifactsProcessor.BACKUP_REPORT);
		ArtifactsTransferReport report = RepositoryViewHelpers.readYamlFile(reportFile);
		Set<String> repositories = report.getArtifacts().stream() //
				.flatMap(artifact -> artifact.getParts().stream()) //
				.flatMap(part -> part.getRepositories().stream()) //
				.collect(Collectors.toSet());

		RepositoryConfiguration repositoryConfiguration = RepositoryConfiguration.T.create();
		repositoryConfiguration.setLocalRepositoryPath(restoreArtifacts.getFolder());
		for (String repositoryName : repositories) {
			MavenHttpRepository repository = MavenHttpRepository.T.create();
			repository.setUser(restoreArtifacts.getUser());
			repository.setPassword(restoreArtifacts.getPassword());
			repositoryConfiguration.getRepositories().add(repository);
			if (restoreArtifacts.getChangedRepositoryIds().containsKey(repositoryName)) {
				final String changedRepositoryId = restoreArtifacts.getChangedRepositoryIds().get(repositoryName);
				repository.setName(changedRepositoryId);
				repository.setUrl(restoreArtifacts.getUrl() + "/" + changedRepositoryId);
			}
		}

		File repositoryConfigurationFile = FileTools
				.createNewTempFile("repository-configuration-file-" + new ExtSimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date()));
		RepositoryViewHelpers.writeYamlFile(repositoryConfiguration, repositoryConfigurationFile);
		OverridingEnvironment oe = new OverridingEnvironment(virtualEnvironment);
		oe.setEnv(Constants.DEVROCK_REPOSITORY_CONFIGURATION, repositoryConfigurationFile.getAbsolutePath());

		try (WireContext<ArtifactDataResolverContract> resolverContext = Wire
				.contextBuilder(ArtifactDataResolverModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
				.bindContract(VirtualEnvironmentContract.class, () -> oe).build()) {
			for (TransferredArtifact artifact : report.getArtifacts()) {

				Map<String, Artifact> repoToArtifactWithRelevantParts = createArtifactWithPartsPerRepo(artifact,
						reportFile.getParentFile().getAbsolutePath());

				for (String repositoryId : repoToArtifactWithRelevantParts.keySet()) {
					String targetRepositoryId = repositoryId;
					if (restoreArtifacts.getChangedRepositoryIds().containsKey(repositoryId)) {
						targetRepositoryId = restoreArtifacts.getChangedRepositoryIds().get(repositoryId);
						println(yellow("\tuploading to repository (initial target repository " + repositoryId + ") : " + targetRepositoryId));
					} else {
						println(yellow("\tuploading to repository: " + targetRepositoryId));
					}

					Repository repository = resolverContext.contract().repositoryReflection().getRepository(targetRepositoryId);
					if (repository == null) {
						// no repository found with that name
						throw new IllegalStateException("Repository '" + targetRepositoryId + "' not found!");

					}
					ArtifactDeployer artifactDeployer = resolverContext.contract().backendContract().artifactDeployer(repository);
					Artifact generateArtifact = repoToArtifactWithRelevantParts.get(repositoryId);
					println(white("\t\t" + generateArtifact.asString()));
					String partsAsString = generateArtifact.getParts().keySet().stream().map(Object::toString)
							.collect(joining("\n\t\t\t", "\t\t\t", ""));
					println(white(partsAsString));
					ArtifactResolution resolution = artifactDeployer.deploy(generateArtifact);
					if (resolution.hasFailed()) {
						throw new IllegalStateException(
								"Upload of " + generateArtifact.asString() + " failed as " + resolution.getFailure().stringify());
					}
				}
			}
		}

	}

	private static Map<String, Artifact> createArtifactWithPartsPerRepo(TransferredArtifact transferredArtifact, String repoBasePath) {
		Map<String, Artifact> repoToArtifact = new HashMap<>();
		VersionedArtifactIdentification vai = VersionedArtifactIdentification.parse(transferredArtifact.getName());

		for (TransferredArtifactPart part : transferredArtifact.getParts()) {
			for (String repo : part.getRepositories()) {
				if (repoToArtifact.containsKey(repo)) {
					Artifact artifact = repoToArtifact.get(repo);
					setPart(repoBasePath, part, artifact);
				} else {
					Artifact artifact = Artifact.T.create();
					artifact.setGroupId(vai.getGroupId());
					artifact.setArtifactId(vai.getArtifactId());
					artifact.setVersion(vai.getVersion());
					repoToArtifact.put(repo, artifact);
					setPart(repoBasePath, part, artifact);
				}
			}
		}
		return repoToArtifact;
	}

	private static void setPart(String repoBasePath, TransferredArtifactPart part, Artifact artifact) {
		String partKeys = part.getName().substring(artifact.asString().length() + 1);
		String[] partClassifierAndType = partKeys.split(":");

		Part p = Part.T.create();

		p.setType(partClassifierAndType[1]);

		FileResource fileResource = FileResource.T.create();
		String suffix;
		if (CommonTools.isEmpty(partClassifierAndType[0])) {
			p.setClassifier(null);
			suffix = "." + p.getType();
			artifact.getParts().put(p.getType(), p);
		} else {
			p.setClassifier(partClassifierAndType[0]);
			suffix = "-" + p.getClassifier() + "." + p.getType();
			artifact.getParts().put(partKeys, p);
		}
		File partFile = new File(buildPartialPath(artifact, repoBasePath) + artifact.getArtifactId() + "-" + artifact.getVersion() + suffix);
		if (!partFile.exists()) {
			throw new RuntimeException("Did not find part '" + partKeys + "' for artifact: " + artifact.asString());
		}
		fileResource.setPath(partFile.getAbsolutePath());
		p.setResource(fileResource);
	}

	static String buildPartialPath(ArtifactIdentification artifact, String prefix) {
		String version = null;
		if (artifact instanceof VersionedArtifactIdentification) {
			version = ((VersionedArtifactIdentification) artifact).getVersion();
		}
		String partialSubPath = null;
		if (version != null) {
			partialSubPath = artifact.getGroupId().replace(".", "/") + "/" + artifact.getArtifactId() + "/" + version + "/";
		} else {
			partialSubPath = artifact.getGroupId().replace(".", "/") + "/" + artifact.getArtifactId() + "/";
		}
		if (prefix.endsWith("/") || prefix.endsWith("\\")) {
			return prefix + partialSubPath;
		} else {
			return prefix + "/" + partialSubPath;
		}

	}
}
