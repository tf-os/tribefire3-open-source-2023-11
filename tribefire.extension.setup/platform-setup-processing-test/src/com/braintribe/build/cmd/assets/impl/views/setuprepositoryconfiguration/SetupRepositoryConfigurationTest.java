// ========================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2020 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission.
// To this file the Braintribe License Agreement applies.
// ========================================================================
package com.braintribe.build.cmd.assets.impl.views.setuprepositoryconfiguration;

import static com.braintribe.build.cmd.assets.PlatformAssetDistributionConstants.FILE_REPOSITORY_VIEW_RESOLUTION;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.braintribe.build.cmd.assets.PlatformAssetDistributionConstants;
import com.braintribe.build.cmd.assets.impl.views.RepositoryRule;
import com.braintribe.build.cmd.assets.impl.views.RepositoryViewHelpers;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.devrock.model.repository.filters.DisjunctionArtifactFilter;
import com.braintribe.devrock.model.repository.filters.LockArtifactFilter;
import com.braintribe.model.platform.setup.api.SetupRepositoryConfiguration;
import com.braintribe.testing.test.AbstractTest;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.FileTools;

/**
 * This class tests the setup repository configuration request. All tests depend on a running Repolet which is an in
 * memory servlet that simulates a remote maven repository. The repository's description can be found in a
 * repository.txt file in the respective res folder. Once a test executes a setup repository configuration request, we
 * assert the expected locks (locked versions) in the created repository configuration file. We also assert the resolved
 * view solutions for a given repository view. The class contains several tests that check the setup repository
 * configuration for the following use cases: with one repository view, with two similar repository views that have
 * different major minor versions and with two repository views that have different revision versions.
 */
public class SetupRepositoryConfigurationTest extends AbstractTest {

	@ClassRule
	public static final RepositoryRule repositoryRule = new RepositoryRule();

	@Rule
	public TestName name = new TestName();

	@Test
	public void testOneViewSetup() {
		logger.info("Check the visualization of the use case if needed: " + testFile("setup-repository-use-case.png").getAbsolutePath());

		List<String> viewAssets = CollectionTools.getList("tribefire.adx.phoenix:adx-view#2.5");
		List<String> expectedLocks = CollectionTools.getList(
				// adx
				"tribefire.adx.phoenix:adx-aws-deployment-model#2.5.1", //
				"tribefire.adx.phoenix:adx-aws-initializer-module#2.5.1", //
				"tribefire.adx.phoenix:adx-aws-module#2.5.1", //
				// conversion
				"tribefire.extension.conversion:conversion-aspose-license#2.3.3-pc", //
				"tribefire.extension.conversion:conversion-commons#2.3.9-pc", //
				"tribefire.extension.conversion:conversion-deployment-model#2.3.3-pc", //
				// tribefire-standard
				"tribefire.cortex:cortex-api-model#2.0.17-pc", //
				"tribefire.cortex:cortex-cors-handler#2.0.10-pc", //
				"tribefire.cortex:cortex-deployment-model#2.0.26-pc" //
		);

		SetupRepositoryConfigurationResult result = setupRepositoryConfiguration(viewAssets);
		assertThat(extractLocks(result.installationPath)).hasSameElementsAs(expectedLocks);

		List<String> viewsSolutions = CollectionTools.getList(
				"tribefire.adx.phoenix:parent#2.5.1", //
				"tribefire.extension.conversion:parent#2.3.1", //
				"tribefire.cortex.assets:parent#2.0.1", //
				"tribefire.cortex.assets:tribefire-standard-view#2.0.2", //
				"tribefire.extension.conversion:conversion-view#2.3.1", //
				"tribefire.adx.phoenix:adx-view#2.5.1");

		assertThat(result.viewsSolutions).hasSameElementsAs(viewsSolutions);
		assertRepositoryViewResolution(result);
	}

	private void assertRepositoryViewResolution(SetupRepositoryConfigurationResult result) {
		File actualResolutionViewResolution = new File(result.installationPath, FILE_REPOSITORY_VIEW_RESOLUTION);
		File expectedResolutionViewResolution = new File(testDir(name.getMethodName()), "repository-view-resolution.yaml");

		assertThat(actualResolutionViewResolution).hasSameTextualContentAs(expectedResolutionViewResolution);
	}

	@Test
	public void testTwoViewsSetupWithDifferentMajorVersions() {
		List<String> viewAssets = CollectionTools.getList(
			"tribefire.cortex.assets:tribefire-standard-view#2.0.2", //
			"tribefire.cortex.assets:tribefire-standard-view#3.0.2" //
		);

		List<String> expectedLocks = CollectionTools.getList(
				// tribefire-standard-view#2.0.2
				"tribefire.cortex:cortex-api-model#2.0.17-pc", //
				"tribefire.cortex:cortex-cors-handler#2.0.10-pc", //
				"tribefire.cortex:cortex-deployment-model#2.0.26-pc", //
				// tribefire-standard-view#3.0.2
				"tribefire.cortex:cortex-api-model#3.0.170", //
				"tribefire.cortex:cortex-cors-handler#3.0.100", //
				"tribefire.cortex:cortex-deployment-model#3.0.260" //
		);

		SetupRepositoryConfigurationResult result = setupRepositoryConfiguration(viewAssets);
		assertThat(extractLocks(result.installationPath)).hasSameElementsAs(expectedLocks);

		List<String> viewsSolutions = CollectionTools.getList(
				"tribefire.cortex.assets:parent#2.0.1", // 
				"tribefire.cortex.assets:tribefire-standard-view#2.0.2", //
				"tribefire.cortex.assets:parent#3.0.1", //
				"tribefire.cortex.assets:tribefire-standard-view#3.0.2");
		assertThat(result.viewsSolutions).hasSameElementsAs(viewsSolutions);
		assertRepositoryViewResolution(result);
	}

	@Test
	public void testTwoViewsSetupWithDifferentRevisions() {
		List<String> viewAssets = asList( //
				"tribefire.cortex.assets:tribefire-standard-view#2.0.1", //
				"tribefire.cortex.assets:tribefire-standard-view#2.0.2" //
		);

		List<String> expectedLocks = CollectionTools.getList(
				// tribefire-standard-view#2.0.1
				"tribefire.cortex:cortex-api-model#2.0.1", //
				"tribefire.cortex:cortex-cors-handler#2.0.2", //
				"tribefire.cortex:cortex-deployment-model#2.0.3", //
				// tribefire-standard-view#2.0.2
				"tribefire.cortex:cortex-api-model#2.0.17-pc", //
				"tribefire.cortex:cortex-cors-handler#2.0.10-pc", //
				"tribefire.cortex:cortex-deployment-model#2.0.26-pc" //
		);

		SetupRepositoryConfigurationResult result = setupRepositoryConfiguration(viewAssets);
		assertThat(extractLocks(result.installationPath)).hasSameElementsAs(expectedLocks);

		List<String> viewsSolutions = CollectionTools.getList(
				"tribefire.cortex.assets:parent#2.0.1", //
				"tribefire.cortex.assets:tribefire-standard-view#2.0.1", //
				"tribefire.cortex.assets:tribefire-standard-view#2.0.2");
		assertThat(result.viewsSolutions).hasSameElementsAs(viewsSolutions);
		assertRepositoryViewResolution(result);
	}

	/**
	 * returns the installation path and the views solutions
	 */
	private SetupRepositoryConfigurationResult setupRepositoryConfiguration(final List<String> viewAssets) {

		SetupRepositoryConfigurationProcessor setupRepositoryConfigurationProcessor = new SetupRepositoryConfigurationProcessor();
		String installationPath = newTempDir(getClass().getSimpleName()).getAbsolutePath();

		SetupRepositoryConfiguration setupRepositoryConfiguration = SetupRepositoryConfiguration.T.create();
		setupRepositoryConfiguration.setInstallationPath(installationPath);
		setupRepositoryConfiguration.setRepositoryViews(viewAssets);

		List<String> viewsSolutions = setupRepositoryConfigurationProcessor.process(setupRepositoryConfiguration,
				repositoryRule.getOverrideableVirtualEnvironment());
		return new SetupRepositoryConfigurationResult(installationPath, viewsSolutions);
	}

	private List<String> extractLocks(String installationPath) {
		File repoConfigFile = new File(installationPath, PlatformAssetDistributionConstants.FILE_REPOSITORY_CONFIGURATION);

		logger.info("Installation path: " + installationPath);
		logger.info("Installation path content:\n" + FileTools.readStringFromFile(repoConfigFile));

		RepositoryConfiguration repositoryConfiguration = RepositoryViewHelpers.readYamlFile(repoConfigFile);

		assertThat(repositoryConfiguration.getRepositories()).hasSize(1);

		ArtifactFilter artifactFilter = repositoryConfiguration.getRepositories().get(0).getArtifactFilter();
		assertThat(artifactFilter).isInstanceOfAny(LockArtifactFilter.class, DisjunctionArtifactFilter.class);

		return getLockOperandsOf(artifactFilter).stream() //
				.map(LockArtifactFilter::getLocks) //
				.flatMap(Collection::stream) //
				.collect(Collectors.toList());
	}

	private List<LockArtifactFilter> getLockOperandsOf(ArtifactFilter filter) {
		List<?> operands = getOperandsOf(filter);
		assertThat(operands).allMatch(LockArtifactFilter.class::isInstance);
		return (List<LockArtifactFilter>) operands;
	}

	private List<ArtifactFilter> getOperandsOf(ArtifactFilter filter) {
		if (filter instanceof DisjunctionArtifactFilter)
			return ((DisjunctionArtifactFilter) filter).getOperands();
		else
			return asList(filter);
	}

	class SetupRepositoryConfigurationResult {

		String installationPath;
		List<String> viewsSolutions;

		public SetupRepositoryConfigurationResult(String installationPath, List<String> viewsSolutions) {
			this.installationPath = installationPath;
			this.viewsSolutions = viewsSolutions;
		}
	}
}
