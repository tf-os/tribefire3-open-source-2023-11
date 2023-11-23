package com.braintribe.build.cmd.assets.impl.views;

import static com.braintribe.build.cmd.assets.impl.Constants.ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.braintribe.build.cmd.assets.impl.Constants;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.testing.test.AbstractTest;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.FileTools;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

/**
 * A Junit {@link TestRule} that spawns a {@code Repolet} before the test class executes all its tests. This test
 * creates an {@link OverridingEnvironment} with the following env variables: PORT, M2_REPO and
 * ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS. The values for these variables are the port of the launched repolet, a
 * temporary created folder for the local repository and the absolute path to the settings.xml file that lies in the
 * respective res folder of the running test. The {@code OverridingEnvironment} can be overwritten when we instantiate
 * the {@code RepositoryRule} inside a Junit test. If a {@code repository-configuration.yaml} file is found in the
 * respective res folder of the running test, it will be copied to the created local Maven repository folder and the
 * environment variable DEVROCK_REPOSITORY_CONFIGURATION that points to it will be set as well. Additionally, place
 * holders like ${env.VIEW_ASSET_TESTS_M2_REPO} and ${env.VIEW_ASSET_TESTS_PORT} inside the the repository configuration
 * file will be replaced with the real values. This is helpful for now since the repository configuration does not
 * support environment variable resolving.
 */
public class RepositoryRule extends AbstractTest implements HasOverrideableVirtualEnvironment, TestRule {

	@Override
	public Statement apply(Statement base, Description description) {

		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				Class<?> testClass = description.getTestClass();
				beforeClass(testClass);
				try {
					base.evaluate();
				} catch (Exception e) {
					Logger.getLogger(RepositoryRule.class)
							.info("Something went wrong while setting up repository for class " + testClass.getSimpleName());
				} finally {
					launcher.shutdown();
				}
			}
		};

	}

	private Launcher launcher;
	private final static String PORT = "VIEW_ASSET_TESTS_PORT";
	private final static String M2_REPO = "VIEW_ASSET_TESTS_M2_REPO";

	private void beforeClass(Class<?> clazz) throws FileNotFoundException, IOException {
		File repositoryDescription = AbstractTest.testFile(clazz, "repository.txt");
		RepoletContent content = RepositoryGenerations.parseConfigurationFile(repositoryDescription);
		launcher = Launcher.build().repolet().name(clazz.getSimpleName()).descriptiveContent().descriptiveContent(content).close().close().done();
		launcher.launch();

		overrideableVirtualEnvironment = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		overrideableVirtualEnvironment.setEnv(PORT, Integer.toString(launcher.getAssignedPort()));
		String mavenRepoPath = newTempDir(clazz.getSimpleName() + "_" + DateTools.getTimestampNumber()).getAbsolutePath();
		overrideableVirtualEnvironment.setEnv(M2_REPO, mavenRepoPath);
		overrideableVirtualEnvironment.setEnv(ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS,
				AbstractTest.testFile(clazz, "settings.xml").getAbsolutePath());

		if (AbstractTest.testFile(clazz, "repository-configuration.yaml").exists()) {
			File repositoryConfigurationTarget = new File(mavenRepoPath, "repository-configuration.yaml");
			FileTools.copyFile(AbstractTest.testFile(clazz, "repository-configuration.yaml"), repositoryConfigurationTarget);

			String windowsEscapedMavenRepoPath = mavenRepoPath.replace("\\", "\\\\");

			String repositoryConfigurationAsString = FileTools.readStringFromFile(repositoryConfigurationTarget);
			repositoryConfigurationAsString = repositoryConfigurationAsString.replace("${env." + M2_REPO + "}", windowsEscapedMavenRepoPath);
			repositoryConfigurationAsString = repositoryConfigurationAsString.replace("${env." + PORT + "}",
					Integer.toString(launcher.getAssignedPort()));

			FileTools.writeStringToFile(repositoryConfigurationTarget, repositoryConfigurationAsString);

			overrideableVirtualEnvironment.setEnv(Constants.DEVROCK_REPOSITORY_CONFIGURATION, repositoryConfigurationTarget.getAbsolutePath());
		}
		Logger.getLogger(clazz).info("Started repolet for " + clazz.getSimpleName() + " ...");
	}

	private OverridingEnvironment overrideableVirtualEnvironment;

	@Override
	public OverridingEnvironment getOverrideableVirtualEnvironment() {
		return overrideableVirtualEnvironment;
	}

	@Override
	public void setOverrideableVirtualEnvironment(OverridingEnvironment ove) {
		this.overrideableVirtualEnvironment = ove;
	}

	public OverridingEnvironment copyOverrideableVirtualEnvironment() {
		OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		ove.setEnv(PORT, this.overrideableVirtualEnvironment.getEnv(PORT));

		ove.setEnv(M2_REPO, this.overrideableVirtualEnvironment.getEnv(M2_REPO));
		ove.setEnv(ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS, this.overrideableVirtualEnvironment.getEnv(ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS));
		return ove;
	}
}
