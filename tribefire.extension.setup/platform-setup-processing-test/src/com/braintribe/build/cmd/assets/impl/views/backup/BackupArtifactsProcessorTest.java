// ========================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2020 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission.
// To this file the Braintribe License Agreement applies.
// ========================================================================
package com.braintribe.build.cmd.assets.impl.views.backup;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.ClassRule;
import org.junit.Test;

import com.braintribe.build.cmd.assets.impl.views.RepositoryRule;
import com.braintribe.gwt.utils.genericmodel.GMCoreTools;
import com.braintribe.testing.test.AbstractTest;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.lcd.CommonTools;

/**
 * Provides tests for {@link BackupArtifactsProcessor}.
 */
public class BackupArtifactsProcessorTest extends AbstractTest {

	@ClassRule
	public static final RepositoryRule repositoryRule = new RepositoryRule();

	@Test
	public void testBackupArtifacts() {

		// @formatter:off
		List<String> artifactsToBackup = CollectionTools.getList(
			"com.braintribe.gm:parent#1.0.1",
			"com.braintribe.gm:a1#1.0.1",
			"com.braintribe.gm:a1#1.0.2",
			"com.braintribe.gm:a1#1.0.10"
		);

		ArtifactsTransferReport actualReport = BackupArtifactsProcessor.process(artifactsToBackup, false, repositoryRule.getOverrideableVirtualEnvironment());
		ArtifactsTransferReport expectedReport = createReport(
			createArtifact("com.braintribe.gm:a1#1.0.1", "/:pom", "/random:yaml", "/repositoryview:yaml"),
			createArtifact("com.braintribe.gm:a1#1.0.2", "/:pom", "/repositoryview:yaml"),
			createArtifact("com.braintribe.gm:a1#1.0.10", "/:pom", "/repositoryview:yaml"),
			createArtifact("com.braintribe.gm:parent#1.0.1", "/:pom")
		);
				
		// @formatter:on
		actualReport.sort();
		assertThat(GMCoreTools.getDescription(actualReport)).isEqualTo(GMCoreTools.getDescription(expectedReport));
	}

	private TransferredArtifact createArtifact(String artifactName, String... parts) {
		TransferredArtifact transferredArtifact = TransferredArtifact.T.create();
		transferredArtifact.setName(artifactName);
		for (String part : parts) {
			TransferredArtifactPart artifactPart = TransferredArtifactPart.T.create();
			artifactPart.setName(transferredArtifact.getName() + part);
			artifactPart.setRepositories(CommonTools.getList("backup-artifacts-processor-test-repo"));
			transferredArtifact.getParts().add(artifactPart);
		}
		return transferredArtifact;
	}

	private ArtifactsTransferReport createReport(TransferredArtifact... artifacts) {
		ArtifactsTransferReport report = ArtifactsTransferReport.T.create();
		for (TransferredArtifact transferredArtifact : artifacts) {
			report.getArtifacts().add(transferredArtifact);
		}
		return report;
	}

	@Test
	public void testGenerateMavenMetadata() {
		final File newTempDir = newTempDir();
		final File generatedMavenMetadata = new File(newTempDir, "com/braintribe/gm/gm-core-api/maven-metadata.xml");
		generatedMavenMetadata.getParentFile().mkdirs();

		final List<String> artifacts = Stream.of("com.braintribe.gm:gm-core-api#1.0.108", "com.braintribe.gm:gm-core-api#1.2.108")
				.collect(Collectors.toList());
		BackupArtifactsProcessor.generateMavenMetadata(artifacts, newTempDir.toPath().toString());
		assertThat(FileTools.readStringFromFile(generatedMavenMetadata)) //
				.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>") //
				.contains("<versioning>") //
				.contains("<latest>1.2.108</latest>") //
				.contains("<release>1.2.108</release>") //
				.contains("</metadata>");
	}

}
