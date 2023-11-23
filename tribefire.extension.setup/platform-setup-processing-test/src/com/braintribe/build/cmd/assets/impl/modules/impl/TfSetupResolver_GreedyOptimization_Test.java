// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules.impl;

import org.junit.Test;

import com.braintribe.build.cmd.assets.impl.modules.api.TfsClasspathOptimizer;
import com.braintribe.model.asset.natures.PlatformLibrary;

/**
 * @author peter.gazdik
 */
public class TfSetupResolver_GreedyOptimization_Test extends AbstractTfSetupResolverTest {

	@Override
	protected TfsClasspathOptimizer newSolutionOptimizer() {
		return new GreedyClasspathOptimizer();
	}

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

		assertCp(TEST_PLATFORM, JAR_A, MODULE_A, JAR_B);
		assertCpEmpty(MODULE_A);
	}

	@Test
	public void moduleWithNoNobles_HasTransitiveDeps() throws Exception {
		withModules(MODULE_A);

		addDependency(TEST_PLATFORM, JAR_A);
		addDependency(JAR_A, JAR_B);

		addDependency(MODULE_A, JAR_C);
		addDependency(JAR_C, JAR_D);

		run();

		assertCp(TEST_PLATFORM, JAR_A, JAR_B, MODULE_A, JAR_C, JAR_D);
		assertCpEmpty(MODULE_A);
	}

	/** Same as {@link #moduleWithNoNobles()}, but the jarB is a private dep, thus nothing can be promoted. */
	@Test
	public void moduleWithPrivateDeps() throws Exception {
		withModules(MODULE_A);

		addDependency(TEST_PLATFORM, JAR_A);
		addDependency(MODULE_A, JAR_B);

		addPrivateDeps(MODULE_A, pattern(JAR_B));

		run();

		assertCp(TEST_PLATFORM, JAR_A);
		assertCp(MODULE_A, JAR_B);
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

		assertCp(TEST_PLATFORM, JAR_A, MODULE_A, API_A);
		assertCpEmpty(MODULE_A);
	}

	/** See also {@link TfSetupResolver_NoOptimization_Test#moduleWithPrivateDeps_NobleDepNotTreatedAsPrivateDep()}*/
	@Test
	public void moduleWithPrivateDeps_NobleDepNotTreatedAsPrivateDepInThatModule() throws Exception {
		withModules(MODULE_A, MODULE_B);

		addDependency(TEST_PLATFORM, JAR_A);

		/* Here the JAR_B is promoted, because it is a dependency of API_A. */
		addDependency(MODULE_A, API_A);
		addDependency(API_A, JAR_B);
		addDependency(MODULE_B, JAR_B);


		addPrivateDeps(MODULE_A, pattern(JAR_B));
		addPrivateDeps(MODULE_B, pattern(JAR_B));

		run();

		assertCp(TEST_PLATFORM, JAR_A, MODULE_A, JAR_B, API_A);
		assertCpEmpty(MODULE_A);
		assertCp(MODULE_B, JAR_B);
	}

	/**
	 * Same as {@link #moduleWithPrivateDeps_NobleNotTreatedAsPrivateDep()} but for {@link PlatformLibrary}.
	 */
	@Test
	public void moduleWithPrivateDeps_PlatformLibraryTreatedAsPrivateDep() throws Exception {
		withModules(MODULE_A);
		withLibraries(LIB_A);

		addDependency(TEST_PLATFORM, JAR_A);
		addDependency(MODULE_A, LIB_A);

		addPrivateDeps(MODULE_A, pattern(LIB_A));

		run();

		assertCp(TEST_PLATFORM, JAR_A, LIB_A);
		assertCp(MODULE_A, LIB_A);
	}

	@Test
	public void moduleWithPrivateDeps_PlatformLibraryDependsOnPrivateDep() throws Exception {
		withModules(MODULE_A);
		withLibraries(LIB_A);

		addDependency(TEST_PLATFORM, JAR_A);
		addDependency(MODULE_A, LIB_A);
		addDependency(LIB_A, JAR_B);

		addPrivateDeps(MODULE_A, pattern(JAR_B));

		run();

		assertCp(TEST_PLATFORM, JAR_A, LIB_A, JAR_B);
		assertCp(MODULE_A, LIB_A, JAR_B);
	}

	/** Same as {@link #moduleWithPrivateDeps()}, but jarB is excluded because it's forbidden by a different module. */
	@Test
	public void moduleWithForbiddenDeps() throws Exception {
		withModules(MODULE_A, MODULE_B);

		addDependency(TEST_PLATFORM, JAR_A);
		addDependency(MODULE_A, JAR_B);

		addForbiddenDeps(MODULE_B, pattern(JAR_B));

		run();

		assertCp(TEST_PLATFORM, JAR_A, MODULE_B);
		assertCp(MODULE_A, JAR_B);
	}

	/** Same as {@link #moduleWithPrivateDeps_NobleNotTreatedAsPrivateDep()}, but apiA is excluded because it's forbidden by a different module. */
	@Test
	public void moduleWithForbiddenDeps_NobleNotTreatedAsForbiddenDep() throws Exception {
		withModules(MODULE_A);

		addDependency(TEST_PLATFORM, JAR_A);
		addDependency(MODULE_A, API_A);

		addForbiddenDeps(MODULE_A, pattern(API_A));

		
		run();

		assertCp(TEST_PLATFORM, JAR_A, MODULE_A, API_A);
		assertCpEmpty(MODULE_A);
	}

	@Test
	public void incompatibleModules() throws Exception {
		withModules(MODULE_A, MODULE_B);

		addDependency(TEST_PLATFORM);
		addDependency(MODULE_A, JAR_A);
		addDependency(MODULE_B, JAR_A_V2);

		run();

		assertCp(TEST_PLATFORM, MODULE_A, JAR_A);
		assertCpEmpty(MODULE_A);
		assertCp(MODULE_B, JAR_A_V2);
	}

	@Test
	public void incompatibleModules_moreComplex() throws Exception {
		withModules(MODULE_A, MODULE_B);

		addDependency(TEST_PLATFORM);
		addDependency(MODULE_A, JAR_A);
		addDependency(MODULE_B, JAR_B);
		addDependency(JAR_B, JAR_C);
		addDependency(JAR_C, JAR_A_V2);

		run();

		assertCp(TEST_PLATFORM, MODULE_A, JAR_A);
		assertCpEmpty(MODULE_A);
		assertCp(MODULE_B, JAR_B, JAR_C, JAR_A_V2);
	}

}
