// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules.impl;

import static com.braintribe.build.cmd.assets.impl.modules.impl.base.TfsTestEntityBuilder.moduleAsset;
import static com.braintribe.build.cmd.assets.impl.modules.impl.base.TfsTestEntityBuilder.platformAsset;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.rules.TestName;

import com.braintribe.build.cmd.assets.api.ArtifactResolutionContext;
import com.braintribe.build.cmd.assets.impl.modules.api.TfsClasspathOptimizer;
import com.braintribe.build.cmd.assets.impl.modules.api.TfsContext;
import com.braintribe.build.cmd.assets.impl.modules.impl.base.ArtifactNames;
import com.braintribe.build.cmd.assets.impl.modules.impl.base.ModuleTestsSolutionEnricher;
import com.braintribe.build.cmd.assets.impl.modules.model.ComponentSetup;
import com.braintribe.build.cmd.assets.impl.modules.model.TfSetup;
import com.braintribe.build.cmd.assets.wire.artifact.contract.ArtifactResolutionContract;
import com.braintribe.build.cmd.assets.wire.artifact.space.ArtifactResolutionSpace;
import com.braintribe.console.Console;
import com.braintribe.console.ConsoleConfiguration;
import com.braintribe.console.PlainSysoutConsole;
import com.braintribe.console.VoidConsole;
import com.braintribe.devrock.mc.api.commons.PartIdentifications;
import com.braintribe.devrock.mc.core.wirings.classpath.ClasspathResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.js.JsResolverWireModule;
import com.braintribe.devrock.model.repolet.content.Artifact;
import com.braintribe.devrock.model.repolet.content.Property;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.TribefireModule;
import com.braintribe.setup.tools.GmNature;
import com.braintribe.setup.tools.impl.RepoContentBuilder;
import com.braintribe.setup.tools.impl.RepoContext;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.asset.resolving.ng.wire.AssetResolverWireModule;

/**
 * @author peter.gazdik
 */
public abstract class AbstractTfSetupResolverTest implements ArtifactNames {

	@Rule
	public TestName testName= new TestName();
	
	// module configuration
	protected PlatformAsset tfContainerAsset;
	protected List<PlatformAsset> moduleAssets = newList();
	protected List<PlatformAsset> modelAssets = emptyList();
	protected List<PlatformAsset> libraryAssets = newList();

	private final Map<String, PlatformAsset> assetByName = newMap();

	protected RepoContentBuilder repoBuilder = new RepoContentBuilder();

	// result
	protected TfSetup result;
	protected Map<String, ComponentSetup> setupsByAssetName = newMap();

	protected static Property gmApiMavenProperty = Property.create(GmNature.mavenPropertyName, GmNature.api.name());
	protected static Property platformOnlyMavenProperty = Property.create(GmNature.mavenPropertyName, GmNature.platformOnly.name());

	private boolean verbose = false;

	protected void setVerbose() {
		if (Console.get() instanceof VoidConsole)
			ConsoleConfiguration.install(new PlainSysoutConsole());
		verbose = true;
	}

	protected void run() {
		if (tfContainerAsset == null)
			throw new IllegalStateException("Test was not initialize. Call 'withModules' first");

		try (RepoContext repoCtx = newRepoContext(); //
				WireContext<ArtifactResolutionContract> wireContext = newWireContext(repoCtx)) {

			result = TfSetupResolver.resolve(newTfsContext(wireContext.contract()));
		}

		setupsByAssetName.put(TEST_PLATFORM, result.platformSetup);
		for (ComponentSetup moduleSetup : result.moduleSetups)
			setupsByAssetName.put(nameOf(moduleSetup), moduleSetup);
	}

	private RepoContext newRepoContext() {
		return new RepoContext(testName.getMethodName(), repoBuilder.build());
	}

	private WireContext<ArtifactResolutionContract> newWireContext(RepoContext repoCtx) {
		return Wire.contextBuilder(new TestArtifactResolutionWireModule()) //
				.bindContract(RepositoryConfigurationContract.class, () -> Maybe.complete(repositoryConfiguration(repoCtx))) //
				.build();
	}

	private static class TestArtifactResolutionWireModule implements WireTerminalModule<ArtifactResolutionContract> {

		@Override
		public List<WireModule> dependencies() {
			return Arrays.asList( //
					ClasspathResolverWireModule.INSTANCE, //
					AssetResolverWireModule.INSTANCE, //
					JsResolverWireModule.INSTANCE //
			);
		}

		@Override
		public void configureContext(WireContextBuilder<?> contextBuilder) {
			WireTerminalModule.super.configureContext(contextBuilder);
			contextBuilder.bindContract(ArtifactResolutionContract.class, ArtifactResolutionSpace.class);
		}
	}

	private RepositoryConfiguration repositoryConfiguration(RepoContext repoCtx) {
		MavenHttpRepository repo = MavenHttpRepository.T.create();
		repo.setUrl("http://localhost:" + repoCtx.launcher.getAssignedPort() + "/archive/");
		repo.setName(repoCtx.repoName);

		RepositoryConfiguration result = RepositoryConfiguration.T.create();
		result.setLocalRepositoryPath(repoCtx.repoFolder.getAbsolutePath());
		result.getRepositories().add(repo);

		return result;
	}

	private TfsContext newTfsContext(ArtifactResolutionContext resolutionContext) {
		return new TfsContext(tfContainerAsset, moduleAssets, modelAssets, libraryAssets, resolutionContext, newSolutionOptimizer(), false, verbose);
	}

	protected TfsClasspathOptimizer newSolutionOptimizer() {
		return TfsClasspathOptimizer.emptyOptimizer();
	}

	// ###############################################
	// ## . . . . . . . . Before run. . . . . . . . ##
	// ###############################################

	protected void withModules(String... names) {
		tfContainerAsset = newAsset(platformAsset(TEST_PLATFORM));

		for (String name : names)
			moduleAssets.add(newAsset(moduleAsset(name)));
	}

	private PlatformAsset newAsset(PlatformAsset pa) {
		String name = pa.qualifiedAssetName();

		ensureJar(name);
		assetByName.put(name, pa);

		return pa;
	}

	protected void withLibraries(String... names) {
		for (String name : names)
			libraryAssets.add(newAsset(platformAsset(name)));
	}

	protected void addPrivateDeps(String moduleName, String... privateDeps) {
		TribefireModule moduleNature = (TribefireModule) assetByName.get(moduleName).getNature();
		moduleNature.setPrivateDeps(asList(privateDeps));
	}

	protected void addForbiddenDeps(String moduleName, String... forbiddenDeps) {
		TribefireModule moduleNature = (TribefireModule) assetByName.get(moduleName).getNature();
		moduleNature.setForbiddenDeps(asList(forbiddenDeps));
	}

	protected void addDependency(String root, String... deps) {
		ensureJar(root);

		for (String dep : deps)
			repoBuilder.addDependency(root, ensureJar(dep));
	}

	private String ensureJar(String name) {
		Artifact artifact = repoBuilder.acquireArtifact(name);
		artifact.getParts().computeIfAbsent(PartIdentifications.jar.asString(), n -> ModuleTestsSolutionEnricher.jarResource(artifact));

		return name;
	}

	protected String pattern(String artifactName) {
		return ".*" + artifactName + ".*";
	}

	// ###############################################
	// ## . . . . . . . . . Asserts . . . . . . . . ##
	// ###############################################

	protected void assertCpEmpty(String componentName) {
		List<String> resolvedClasspath = toCpSolutionNames(componentName);

		assertThat(resolvedClasspath).isEmpty();
	}

	protected void assertCp(String componentName, String... otherJars) {
		List<String> resolvedClasspath = toCpSolutionNames(componentName);

		assertThat(resolvedClasspath).containsExactlyInAnyOrderElementsOf(union(componentName, otherJars));
	}

	private List<String> toCpSolutionNames(String componentName) {
		ComponentSetup componentSetup = setupsByAssetName.get(componentName);

		return componentSetup.classpath.stream() //
				.map(this::nameOf) //
				.collect(Collectors.toList());
	}

	protected void assertAllModulesCp(String... allJars) {
		Set<String> resolvedClasspath = result.allModulesCpSolutions.stream() //
				.map(this::nameOf) //
				.collect(Collectors.toSet());

		assertThat(resolvedClasspath).containsOnly(allJars);
	}

	private static <T> List<T> union(T single, T... rest) {
		List<T> result = asList(rest);
		result.add(single);

		return result;
	}

	private String nameOf(ComponentSetup setup) {
		return nameOf(setup.descriptor.assetSolution);
	}

	private String nameOf(AnalysisArtifact s) {
		return s.asString();
	}

}
