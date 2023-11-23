// ========================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2020 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission.
// To this file the Braintribe License Agreement applies.
// ========================================================================
package com.braintribe.build.cmd.assets.impl.views.lockversions;

import static com.braintribe.build.cmd.assets.impl.views.lockversions.LockVersionsProcessor.numberAwareSolutionComparator;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.ClassRule;
import org.junit.Test;

import com.braintribe.build.cmd.assets.impl.views.RepositoryRule;
import com.braintribe.devrock.model.repository.filters.LockArtifactFilter;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.platform.setup.api.LockVersions;
import com.braintribe.testing.test.AbstractTest;
import com.braintribe.utils.CollectionTools;
import com.braintribe.ve.impl.OverridingEnvironment;

/**
 * This class contains tests that examine the
 * {@link LockVersionsProcessor#resolveSolutionsAndCreateLockFilter(LockVersions, com.braintribe.ve.api.VirtualEnvironment)}
 * method. All tests depend on a running Repolet which is an in memory servlet that simulates a remote Maven repository.
 * The Repolets are spawned as part of the {@link RepositoryRule} lifecycle and they provide concrete versions of
 * artifacts as if they would exist in a real remote Maven repository. The tests examine use cases such as: lock
 * versions from multiple groups, lock versions while exclusions are set in some of their dependencies, lock versions
 * while one or more dependencies use ranges for their dependencies, lock versions when a repository configuration with
 * a QualifiedArtifactFilter is in use (i.e. DEVROCK_REPOSITORY_CONFIGURATION is set).
 */
public class LockVersionsProcessorTest extends AbstractTest {

	@ClassRule
	public static final RepositoryRule repositoryRule = new RepositoryRule();

	@Test
	public void testLocksAndExclusionsFromTwoGroups() {
		System.out.println("Check the visualization of the use case if needed: " + testFile("lock-versions-use-case.png").getAbsolutePath());
		LockVersions request = LockVersions.T.create();
		// @formatter:off
		request.getTerminalArtifacts().addAll(CollectionTools.getList(
			"com.braintribe.gm:a1#1.0",
			"com.braintribe.gm:a3#1.1.3"
		));

		LockArtifactFilter filter = LockVersionsProcessor.resolveSolutionsAndCreateLockFilter(request, repositoryRule.getOverrideableVirtualEnvironment());
		List<String> expectedLocks = CollectionTools.getList(
			"com.braintribe.gm:parent#1.0.1",
			"com.braintribe.gm:parent#1.1.1",
			"com.braintribe.gm:a1#1.0.2",
			"com.braintribe.gm:a2#1.0.3",
			"com.braintribe.gm:a3#1.1.3",
			"tribefire.cortex:parent#2.0.2",
			"tribefire.cortex:parent#2.1.100",
			"tribefire.cortex:b1#2.0.13",
			"tribefire.cortex:b1#2.1.11",
			"tribefire.cortex:b2#2.0.5"
		);
		// @formatter:on
		assertThat(filter.getLocks()).containsExactlyInAnyOrderElementsOf(expectedLocks);
	}

	@Test
	public void testRangedDependency() {
		System.out.println("Check the visualization of the use case if needed: "
				+ testFile("lock-versions-with-repository-configuration-use-case.png").getAbsolutePath());
		LockVersions request = LockVersions.T.create();
		request.getTerminalArtifacts().add("com.braintribe.gm:a4#1.1");

		LockArtifactFilter filter = LockVersionsProcessor.resolveSolutionsAndCreateLockFilter(request,
				repositoryRule.getOverrideableVirtualEnvironment());

		// @formatter:off
		List<String> expectedLocks = CollectionTools.getList(
			"com.braintribe.gm:parent#1.1.1",
			"com.braintribe.gm:a4#1.1.4",
			"tribefire.cortex:parent#2.2.200",
			"tribefire.cortex:b1#2.2.21"

		);
		// @formatter:on
		assertThat(filter.getLocks()).containsExactlyInAnyOrderElementsOf(expectedLocks);
	}

	@Test
	public void testRangedDependency_WithQualifiedArtifactFilter() {
		System.out.println("Check the visualization of the use case if needed: "
				+ testFile("lock-versions-with-repository-configuration-use-case.png").getAbsolutePath());

		LockVersions request = LockVersions.T.create();
		request.getTerminalArtifacts().add("com.braintribe.gm:a4#1.1");

		// we need to copy OverrideableVirtualEnvironment since the repositoryRule is shared between all class's tests
		OverridingEnvironment overrideableVirtualEnvironment = repositoryRule.copyOverrideableVirtualEnvironment();
		String repositoryConfigurationPath = testFile("custom-repository-configuration.yaml").getAbsolutePath();
		overrideableVirtualEnvironment.setEnv("DEVROCK_REPOSITORY_CONFIGURATION", repositoryConfigurationPath);
		LockArtifactFilter filter = LockVersionsProcessor.resolveSolutionsAndCreateLockFilter(request, overrideableVirtualEnvironment);

		// a4#1.1 has a dependency of tf artifacts with range [2,3)
		// in testRangedDependency() it resolves to tf.cortex:b1#2.2
		// In this test we filter the repo for tf.cortex to only consider [2.1,2.2)

		// @formatter:off
		List<String> expectedLocks = CollectionTools.getList(
			"com.braintribe.gm:parent#1.1.1",
			"com.braintribe.gm:a4#1.1.4",
			"tribefire.cortex:parent#2.1.100",
			"tribefire.cortex:b1#2.1.11",
			"tribefire.cortex:b2#2.1.4"
		);
		// @formatter:on

		assertThat(filter.getLocks()).containsExactlyInAnyOrderElementsOf(expectedLocks);
	}

	@Test
	public void testNumberAwareSolutionComparator() {
		List<AnalysisArtifact> analysisArtifacts = Stream.of( //
				createAnalysisArtifact("com.beust", "jcommander", "1.78"), //
				createAnalysisArtifact("com.beust", "jcommander", "1.48"), //
				createAnalysisArtifact("com.beust", "jcommander", "1.35") //
		).collect(Collectors.toList());

		List<String> sortedAnalysisArtifacts = analysisArtifacts.stream() //
				.sorted(numberAwareSolutionComparator) //
				.map(AnalysisArtifact::asString) //
				.collect(Collectors.toList());

		assertThat(sortedAnalysisArtifacts).containsExactly( //
				"com.beust:jcommander#1.35", //
				"com.beust:jcommander#1.48", //
				"com.beust:jcommander#1.78" //
		);
	}

	private AnalysisArtifact createAnalysisArtifact(final String groupId, final String artifactId, final String versionAsString) {
		AnalysisArtifact analysisArtifact = AnalysisArtifact.T.create();
		analysisArtifact.setArtifactId(artifactId);
		analysisArtifact.setGroupId(groupId);
		analysisArtifact.setVersion(versionAsString);
		return analysisArtifact;
	}
}
