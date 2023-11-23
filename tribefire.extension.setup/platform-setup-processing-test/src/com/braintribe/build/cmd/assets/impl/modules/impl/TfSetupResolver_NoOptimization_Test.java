// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules.impl;


import org.junit.Test;

import com.braintribe.devrock.model.repolet.content.Artifact;
import com.braintribe.setup.tools.GmNature;

/**
 * @author peter.gazdik
 */
public class TfSetupResolver_NoOptimization_Test extends AbstractTfSetupResolverTest {

	@Test
	public void containerOnly() throws Exception {
		withModules();

		addDependency(TEST_PLATFORM, JAR_A);

		run();

		assertCp(TEST_PLATFORM, JAR_A);
	}

	@Test
	public void moduleWithNoNobles() throws Exception {
		withModules(MODULE_A);

		addDependency(TEST_PLATFORM, JAR_A);
		addDependency(MODULE_A, JAR_B);

		run();

		assertCp(TEST_PLATFORM, JAR_A);
		assertCp(MODULE_A, JAR_B);
	}

	@Test
	public void moduleWithNoNobles_HasTransitiveDeps() throws Exception {
		withModules(MODULE_A);

		addDependency(TEST_PLATFORM, JAR_A);
		addDependency(JAR_A, JAR_B);

		addDependency(MODULE_A, JAR_C);
		addDependency(JAR_C, JAR_D);

		run();

		assertCp(TEST_PLATFORM, JAR_A, JAR_B);
		assertCp(MODULE_A, JAR_C, JAR_D);
	}

	@Test
	public void moduleWithDepsCommonWithContainer() throws Exception {
		withModules(MODULE_A);

		addDependency(TEST_PLATFORM, JAR_A);
		addDependency(MODULE_A, JAR_A, JAR_B);

		run();

		assertCp(TEST_PLATFORM, JAR_A);
		assertCp(MODULE_A, JAR_B /* JAR_A was removed as it's in container */);
	}

	/**
	 * We cannot promote JAR_B from MODULE_A as it depends on JAR_A_V2. If we promote it, we could mix classes from JAR_As loaded by different
	 * class-loaders (main and module-specific).
	 */
	@Test
	public void moduleWithDepsCommonWithContainer_ButDifferentVersion() throws Exception {
		withModules(MODULE_A);

		addDependency(TEST_PLATFORM, JAR_B);
		addDependency(MODULE_A, JAR_B);
		addDependency(JAR_B, JAR_A);

		// JAR_B cannot be removed, even though it's already in the platform, as it has an incompatible dependency in this setup
		addDependency(MODULE_A, JAR_A_V2);

		run();

		assertCp(TEST_PLATFORM, JAR_B, JAR_A);
		assertCp(MODULE_A, JAR_B, JAR_A_V2);
	}

	@Test
	public void moduleWithDepsCommonWithContainer_ButForbidden() throws Exception {
		withModules(MODULE_A);

		addDependency(TEST_PLATFORM, JAR_A);
		addDependency(MODULE_A, JAR_A, JAR_B);

		addPrivateDeps(MODULE_A, pattern(JAR_A));

		run();

		assertCp(TEST_PLATFORM, JAR_A);
		assertCp(MODULE_A, JAR_A, JAR_B);
	}

	/* ############################################################################################################################################ */
	/* Note that these tests are actually testing something as this is a no-optimization use-case. If something is promoted, it must have been
	 * recognized as a library or API */
	/* ############################################################################################################################################ */

	@Test
	public void moduleWithApi() throws Exception {
		withModules(MODULE_A);

		addDependency(TEST_PLATFORM, JAR_A);
		addDependency(MODULE_A, API_A);

		run();

		assertCp(TEST_PLATFORM, JAR_A, API_A /* API was promoted */);
		assertCp(MODULE_A);
	}

	@Test
	public void moduleWithMavenApi() throws Exception {
		withModules(MODULE_A);

		addDependency(TEST_PLATFORM, JAR_A);
		addDependency(MODULE_A, MVN_API_A);

		repoBuilder.requireArtifact(MVN_API_A).getProperties().add(gmApiMavenProperty);

		run();

		// assertCp(TEST_PLATFORM, JAR_A, MVN_API_A /* MVN API was promoted */);
		assertCp(MODULE_A);
	}

	@Test
	public void moduleWithApi_ApiWithDeps() throws Exception {
		withModules(MODULE_A);

		addDependency(TEST_PLATFORM, JAR_A);
		addDependency(MODULE_A, API_A);
		addDependency(API_A, JAR_B);
		addDependency(JAR_B, JAR_C);

		run();

		assertCp(TEST_PLATFORM, JAR_A, JAR_B, JAR_C, API_A /* API + deps were promoted */);
		assertCp(MODULE_A);
	}

	@Test
	public void moduleWithApi_ApiWithDepsWhichAreAlsoInAnotherModule() throws Exception {
		withModules(MODULE_A, MODULE_B);

		addDependency(TEST_PLATFORM, JAR_A);

		/* Here the JAR_B is promoted, because it is a dependency of API_A. */
		addDependency(MODULE_A, API_A);
		addDependency(API_A, JAR_B);

		/* Judging by only this we don't need to promote JAR_B, but because of MODULE_A setup we have to. */
		addDependency(MODULE_B, JAR_B);

		run();

		assertCp(TEST_PLATFORM, JAR_A, JAR_B, API_A /* API + dep are promoted from both modules */);
		assertCp(MODULE_A);
		assertCp(MODULE_B /* JAR_B was promoted */);
		assertAllModulesCp(MODULE_A, MODULE_B);
	}

	@Test
	public void moduleWithModels() throws Exception {
		withModules(MODULE_A);

		addDependency(TEST_PLATFORM, JAR_A);
		addDependency(MODULE_A, MODEL_A, MODEL_B);

		run();

		assertCp(TEST_PLATFORM, JAR_A, MODEL_A, MODEL_B /* Models were promoted */);
		assertCp(MODULE_A);
	}

	/**
	 * Noble artifacts are not considered when determining whether an artifact is a private dependency. That is to make it easier to "mark" all the
	 * non-nobles as private deps with say ".*".
	 */
	@Test
	public void moduleWithPrivateDeps_NobleNotTreatedAsPrivateDep() throws Exception {
		withModules(MODULE_A);

		addDependency(TEST_PLATFORM, JAR_A);
		addDependency(MODULE_A, API_A);

		addPrivateDeps(MODULE_A, pattern(API_A));

		run();

		assertCp(TEST_PLATFORM, JAR_A, API_A);
		assertCp(MODULE_A);
	}

	@Test
	public void moduleWithPrivateDeps_NobleDepNotTreatedAsPrivateDep() throws Exception {
		withModules(MODULE_A);

		addDependency(TEST_PLATFORM, JAR_A);

		/* Here the JAR_B is promoted, because it is a dependency of API_A. */
		addDependency(MODULE_A, API_A);
		addDependency(API_A, JAR_B);

		addPrivateDeps(MODULE_A, pattern(JAR_B));

		run();

		assertCp(TEST_PLATFORM, JAR_A, JAR_B, API_A /* API + dep are promoted despite being private */);
		assertCp(MODULE_A);
	}

	/** Same as {@link #moduleWithPrivateDeps_NobleNotTreatedAsPrivateDep()}, but apiA is excluded because it's forbidden by a different module. */
	@Test
	public void moduleWithForbiddenDeps_NobleNotTreatedAsForbiddenDep() throws Exception {
		withModules(MODULE_A);

		addDependency(TEST_PLATFORM, JAR_A);
		addDependency(MODULE_A, API_A);

		addForbiddenDeps(MODULE_A, pattern(API_A));

		run();

		assertCp(TEST_PLATFORM, JAR_A, API_A);
		assertCp(MODULE_A);
	}

	/**
	 * This will mean we have two different Solution instances for MODEL_A and both have to be promoted. This checks solution comparison works
	 * properly.
	 */
	@Test
	public void modulesWithSameModel() throws Exception {
		withModules(MODULE_A, MODULE_B);

		addDependency(TEST_PLATFORM, JAR_A);
		addDependency(MODULE_A, MODEL_A);
		addDependency(MODULE_B, MODEL_A);

		run();

		assertCp(TEST_PLATFORM, JAR_A, MODEL_A /* Models were promoted */);
		assertCp(MODULE_A);
		assertCp(MODULE_B);
	}

	@Test(expected = IllegalArgumentException.class)
	public void modulesWithSameModel_DifferentModelDeps() throws Exception {
		withModules(MODULE_A, MODULE_B);

		addDependency(MODULE_A, MODEL_A);
		addDependency(MODULE_B, MODEL_A);

		/* This cannot work as in MODULE_A MODEL_A depends on JAR_A and in MODULE_B it depends on JAR_A_V2. */
		addDependency(MODEL_A, JAR_A);
		addDependency(MODULE_B, JAR_A_V2);

		run();
	}

	@Test
	public void moduleWithPlatformLibrary() throws Exception {
		withModules(MODULE_A);
		withLibraries(LIB_A);

		addDependency(TEST_PLATFORM, JAR_A);
		addDependency(MODULE_A, LIB_A);

		run();

		assertCp(TEST_PLATFORM, JAR_A, LIB_A);
		assertCp(MODULE_A);
	}

	@Test
	public void platformLibraryHasDependency() throws Exception {
		withModules(MODULE_A);
		withLibraries(LIB_A);

		addDependency(TEST_PLATFORM, JAR_A);
		addDependency(MODULE_A, LIB_A);
		addDependency(LIB_A, JAR_B);

		run();

		assertCp(TEST_PLATFORM, JAR_A, LIB_A, JAR_B);
		assertCp(MODULE_A);
	}

	@Test
	public void platformLibraryIsPomAndHasDependency() throws Exception {
		withModules(MODULE_A);
		withLibraries(LIB_A);

		repoBuilder.addDependency(MODULE_A, LIB_A).setType("pom");
		addDependency(LIB_A, JAR_B);

		Artifact libArt = repoBuilder.requireArtifact(LIB_A);
		libArt.setPackaging("pom");

		libArt.getParts().clear();

		run();

		assertCp(TEST_PLATFORM, JAR_B);
		assertCp(MODULE_A);
	}

	/** Module has a dependency of nature {@link GmNature#platformOnly}, which is an error and an {@link IllegalArgumentException}. */
	@Test(expected = IllegalArgumentException.class)
	public void moduleWithPlatformOnlyDependency() throws Exception {
		withModules(MODULE_A);
		withLibraries(LIB_A);

		addDependency(TEST_PLATFORM, JAR_A);
		addDependency(MODULE_A, PLATFORM_ONLY_A);

		repoBuilder.requireArtifact(PLATFORM_ONLY_A).getProperties().add(platformOnlyMavenProperty);

		run();
	}

}
