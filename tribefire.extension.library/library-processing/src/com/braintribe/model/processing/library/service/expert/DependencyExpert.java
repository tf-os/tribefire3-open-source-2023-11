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
package com.braintribe.model.processing.library.service.expert;

import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.model.generic.typecondition.TypeConditions.orTc;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.cfg.Configurable;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathDependencyResolver;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionScope;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.DependencyResolver;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.mc.core.wirings.classpath.ClasspathResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.classpath.contract.ClasspathResolverContract;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.analysis.AnalysisTerminal;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.asset.natures.PlatformLibrary;
import com.braintribe.model.asset.natures.TribefireModule;
import com.braintribe.model.asset.natures.TribefirePlatform;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.library.DistributionLicense;
import com.braintribe.model.library.Library;
import com.braintribe.model.library.deployment.service.Profile;
import com.braintribe.model.library.service.ArtifactReferences;
import com.braintribe.model.library.service.DependencyContext;
import com.braintribe.model.library.service.dependencies.Dependencies;
import com.braintribe.model.library.service.dependencies.GetDependencies;
import com.braintribe.model.library.service.documentation.HasIgnoreMissing;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.library.service.LibrariesMissingException;
import com.braintribe.model.processing.library.service.LibraryServiceProcessor;
import com.braintribe.model.processing.library.service.settings.RepositoryTemplateContext;
import com.braintribe.model.processing.library.service.util.Comparators;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.JunctionBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.collection.api.MultiMap;
import com.braintribe.utils.collection.impl.HashMultiMap;
import com.braintribe.utils.velocity.VelocityTools;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

import tribefire.cortex.asset.resolving.ng.api.AssetDependencyResolver;
import tribefire.cortex.asset.resolving.ng.api.AssetResolutionContext;
import tribefire.cortex.asset.resolving.ng.api.PlatformAssetResolution;
import tribefire.cortex.asset.resolving.ng.impl.PlatformAssetSolution;
import tribefire.cortex.asset.resolving.ng.wire.AssetResolverWireModule;
import tribefire.cortex.asset.resolving.ng.wire.contract.AssetResolverContract;

public class DependencyExpert {

	private final static Logger logger = Logger.getLogger(DependencyExpert.class);

	//@formatter:off
	public static TraversingCriterion tc = TC.create()
			.conjunction()
				.property()
					.typeCondition(orTc(isKind(TypeKind.entityType), isKind(TypeKind.collectionType)))
						.negation().pattern().entity(Library.T)
				.property(Library.licenses)
			.close()
			.negation()
				.pattern()
					.entity(Library.T).property(Library.distributionType)
				.close()
			.negation()
				.pattern()
					.entity(DistributionLicense.T)
						.property(DistributionLicense.licenseFilePdf)
				.close()
			.close()
		.done();
	//@formatter:on

	private String repositoryBasePath;
	private Profile profile = Profile.dev;
	private String repositoryUsername;
	private String repositoryPassword;
    private String repositoryUrl;
    private String ravenhurstUrl;
	private static final String settingsTemplateLocation = "com/braintribe/model/processing/library/service/templates/settings.xml.vm";

	public Dependencies getDependencies(AccessRequestContext<GetDependencies> context) {

		GetDependencies request = context.getRequest();

		Map<String, DependencyResolutionResult> artifactInformationMap = resolveArtifactAndDependencies(request);

		final Set<String> combinedDeps = new HashSet<>();
		artifactInformationMap.values().forEach(artifactInformation -> {

			if (request.getIncludeTerminalArtifact()) {
				ArtifactIdentification resolvedArtifact = artifactInformation.getResolvedArtifact();
				combinedDeps.add(resolvedArtifact.asString());
			}

			Collection<Artifact> artifactList = artifactInformation.getDependencies();
			if (artifactList != null) {
				for (Artifact artifact : artifactList) {
					combinedDeps.add(artifact.asString());
				}
			}
		});

		Dependencies result = Dependencies.T.create();
		result.getDependencies().addAll(combinedDeps);
		result.setSuccess(true);

		return result;
	}

	public Map<String, DependencyResolutionResult> resolveArtifactAndDependencies(ArtifactReferences artifactReferences) {

		//@formatter:off
		AssetResolutionContext parc = AssetResolutionContext.build()
					.includeDocumentation(false)
					.lenient(false)
					.runtime(true)
					.selectorFiltering(true)
					.done();
		//@formatter:on

		Pair<File, Set<String>> settingsPair = createSettingsFile(artifactReferences);
		File settingsFile = settingsPair.first;
		Set<String> ignoredLibraryGroups = settingsPair.second;

		OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		ove.setEnv("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settingsFile.getAbsolutePath());
		ove.setEnv("PROFILE_USECASE", "core");

		ClassLoader classLoader = this.getClass().getClassLoader();
		try (WireContext<AssetResolverContract> resolverContext = Wire
				.contextBuilder(AssetResolverWireModule.INSTANCE, ClasspathResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
				.bindContract(VirtualEnvironmentContract.class, () -> ove).loadSpacesFrom(classLoader).build()) {

			ArtifactDataResolverContract dataResolverContract = resolverContext.contract().transitiveResolverContract().dataResolverContract();
			DependencyResolver dependencyResolver = dataResolverContract.dependencyResolver();
			// TODO: check maybe

			Map<String, DependencyResolutionResult> result = new LinkedHashMap<>();

			for (String artifactId : artifactReferences.getArtifactIdList()) {

				CompiledDependencyIdentification identification = CompiledDependencyIdentification.parse(artifactId);
				CompiledArtifactIdentification compiledArtifactIdentification = dependencyResolver.resolveDependency(identification).get();

				DependencyResolutionResult entry = new DependencyResolutionResult();
				entry.setResolvedArtifact(compiledArtifactIdentification);
				result.put(artifactId, entry);

				if (artifactReferences.getResolveDependencies()) {

					List<CompiledTerminal> terminalClasspathArtifacts = getTerminalClasspathArtifact(parc, resolverContext, identification,
							compiledArtifactIdentification, dataResolverContract);

					//@formatter:off
					ClasspathResolutionContext resolutionContext = ClasspathResolutionContext.build()
						.enrichJar(true)
						.lenient(true)
						.scope(ClasspathResolutionScope.runtime)
						.done();
					//@formatter:on

					ClasspathDependencyResolver classpathResolver = resolverContext.contract(ClasspathResolverContract.class).classpathResolver();

					ArtifactIndexer artifactIndexer = new ArtifactIndexer();

					for (CompiledTerminal terminalArtifact : terminalClasspathArtifacts) {
						AnalysisArtifactResolution artifactResolution = classpathResolver.resolve(resolutionContext, terminalArtifact);

						if (terminalClasspathArtifacts.size() == 1) { // it is a plain artifact and NOT an asset
							List<AnalysisTerminal> terminals = artifactResolution.getTerminals();
							if (terminals.size() == 1) {
								AnalysisTerminal terminal = terminals.iterator().next();
								if (terminal instanceof AnalysisDependency) {
									AnalysisDependency dep = (AnalysisDependency) terminal;
									entry.setResolvedArtifact(dep);
								}
							}
						}

						List<AnalysisArtifact> solutions = artifactResolution.getSolutions();

						for (AnalysisArtifact artifact : solutions) {
							String groupId = artifact.getGroupId();
							boolean ignore = false;
							for (String i : ignoredLibraryGroups) {
								if (groupId.startsWith(i)) {
									ignore = true;
									break;
								}
							}
							if (!ignore) {
								artifactIndexer.addResultingArtifact(artifact);
							}
						}

					}

					entry.setDependencies(artifactIndexer.getResultingArtifacts());
					entry.setTransitiveDependersMap(artifactIndexer.getTransitiveDependersMap());
					entry.setTerminalClasspathArtifacts(terminalClasspathArtifacts);
				}

			}

			return result;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private List<CompiledTerminal> getTerminalClasspathArtifact(AssetResolutionContext parc, WireContext<AssetResolverContract> resolverContext,
			CompiledDependencyIdentification identification, CompiledArtifactIdentification compiledArtifactIdentification,
			ArtifactDataResolverContract dataResolverContract) {

		List<CompiledTerminal> terminalClasspathArtifacts = new ArrayList<>();

		Maybe<ArtifactDataResolution> assetPartMaybe = dataResolverContract.artifactResolver().resolvePart(compiledArtifactIdentification,
				PartIdentification.create("asset", "man"));

		boolean isAsset = false;
		if (!assetPartMaybe.isUnsatisfiedBy(NotFound.T)) {
			ArtifactDataResolution partResolution = assetPartMaybe.get();
			if (partResolution.isBacked()) {
				// It is an asset!
				isAsset = true;
			}
		}

		if (isAsset) {
			AssetDependencyResolver assetDependencyResolver = resolverContext.contract().assetDependencyResolver();

			PlatformAssetResolution resolution = assetDependencyResolver.resolve(parc, identification);

			for (PlatformAssetSolution assetResolution : resolution.getSolutions()) {

				if (assetResolution.nature instanceof PlatformLibrary || assetResolution.nature instanceof TribefireModule
						|| assetResolution.nature instanceof TribefirePlatform) {
					AnalysisArtifact solution = assetResolution.solution;
					CompiledTerminal terminal = CompiledTerminal.from(solution);
					terminalClasspathArtifacts.add(terminal);
				}
			}
		} else {

			CompiledTerminal terminal = CompiledTerminal.from(compiledArtifactIdentification);
			terminalClasspathArtifacts.add(terminal);

		}

		return terminalClasspathArtifacts;
	}

	private Pair<File, Set<String>> createSettingsFile(DependencyContext depContext) {

		logger.debug(() -> "Creating a custom settings.xml file. Profile: " + profile + ", repositoryBasePath: " + repositoryBasePath);

		Template template = null;
		VelocityEngine engine = null;

		Thread currentThread = Thread.currentThread();
		ClassLoader originalClassLoader = currentThread.getContextClassLoader();
		try {
			currentThread.setContextClassLoader(LibraryServiceProcessor.class.getClassLoader());
			engine = VelocityTools.newResourceLoaderVelocityEngine(true);
			template = engine.getTemplate(settingsTemplateLocation, "UTF-8");
		} finally {
			currentThread.setContextClassLoader(originalClassLoader);
		}

		File libraryFolder = new File(repositoryBasePath);
		if (!libraryFolder.exists()) {
			libraryFolder.mkdirs();
		}

		VelocityContext context = new VelocityContext();
		context.put("LOCAL_REPOSITORY", libraryFolder.getAbsolutePath());
		if (repositoryUsername == null || repositoryPassword == null) {
			String errMsg = "Repository credentials are not configured: user=" + repositoryUsername + ", pwd=" + repositoryPassword;
			logger.error(errMsg);
			throw new RuntimeException(errMsg);
		}
		context.put("REPOSITORY_USERNAME", repositoryUsername);
		context.put("REPOSITORY_PASSWORD", repositoryPassword);
		context.put("REPOSITORY_URL", repositoryUrl);
		context.put("RAVENHURST_URL", ravenhurstUrl);
		context.put("PROFILE", profile.name());

		List<RepositoryTemplateContext> contexts = new ArrayList<>();
		contexts.add(new RepositoryTemplateContext("third-party", repositoryUsername, repositoryPassword));
		contexts.add(new RepositoryTemplateContext("core-dev", repositoryUsername, repositoryPassword));

		for (String repo : depContext.getAdditionalRepositories()) {
			logger.debug(() -> "Adding additional repository: " + repo);
			contexts.add(new RepositoryTemplateContext(repo, repositoryUsername, repositoryPassword));
		}
		context.put("repositoryContexts", contexts);

		StringWriter sw = new StringWriter();
		template.merge(context, sw);

		File customSettingsFile;
		try {
			customSettingsFile = File.createTempFile("settings", ".xml");
		} catch (IOException e) {
			throw new UncheckedIOException("Could not create a temporary settings.xml file", e);
		}
		FileTools.writeStringToFile(customSettingsFile, sw.toString(), "UTF-8");

		logger.debug(() -> "Settings file: \n" + StringTools.asciiBoxMessage(sw.toString()));

		if (!customSettingsFile.exists()) {
			throw new RuntimeException("The settings file " + customSettingsFile.getAbsolutePath() + " does not exist.");
		}

		Set<String> ignoredLibraryGroups = new HashSet<>(); // CollectionTools2.asSet("com.braintribe", "tribefire");
		for (String g : depContext.getIgnoredDependencies()) {
			ignoredLibraryGroups.add(g);
		}

		return new Pair<>(customSettingsFile, ignoredLibraryGroups);
	}

	public List<Library> getLibraries(DependencyContext dependencyContext, PersistenceGmSession librarySession, List<String> dependencyList,
			TreeSet<String> librariesFound, HasIgnoreMissing request) throws Exception {
		if (dependencyList == null || dependencyList.isEmpty()) {
			throw new Exception("The dependency list is null or empty.");
		}

		List<Library> result = new ArrayList<>();

		AbstractSet<String> dependencySet = LibraryTools.splitDependencyList(dependencyContext.getIgnoredDependencies(), dependencyList);

		int batchSize = 20;
		Iterator<String> it = dependencySet.iterator();
		TreeSet<String> missing = new TreeSet<>();

		while (it.hasNext()) {

			List<String> batchList = new ArrayList<>();
			while (it.hasNext() && batchList.size() < batchSize) {
				batchList.add(it.next());
			}

			if (!batchList.isEmpty()) {
				JunctionBuilder<EntityQueryBuilder> disjunction = EntityQueryBuilder.from(Library.T).where().disjunction();

				for (String b : batchList) {
					String groupId = StringTools.getSubstringBefore(b, ":");
					String artifactId = StringTools.getSubstringBetween(b, ":", "#");
					String version = StringTools.getSubstringAfter(b, "#");
					disjunction = disjunction.conjunction().property(Library.groupId).eq(groupId).property(Library.artifactId).eq(artifactId)
							.property(Library.version).eq(version).close();
				}

				EntityQuery query = disjunction.close().tc(tc).done();

				List<Library> libraryList = librarySession.query().entities(query).list();
				for (Library l : libraryList) {
					String id = l.getGroupId() + ":" + l.getArtifactId() + "#" + l.getVersion();
					if (!StringTools.isEmpty(l.getCopyright()) && !l.getLicenses().isEmpty()) {
						batchList.remove(id);
						librariesFound.add(id);
						result.add(l);
					} else {
						logger.info(() -> "Library " + id + " has either no copyright or no licenses attached.");
					}
				}

				if (!batchList.isEmpty()) {
					for (String m : batchList) {
						missing.add(m);
					}
				}
			}
		}

		if (missing.size() > 0) {
			Boolean ignoreMissing = request != null ? request.getIgnoreMissing() : Boolean.FALSE;
			if (ignoreMissing == null || !ignoreMissing.booleanValue()) {
				throw new LibrariesMissingException(missing);
			} else {
				logger.info("Could not find the following libraries: " + missing + ". But the request tells us to ignore missing libs.");
			}
		}

		List<Library> sorted = new ArrayList<>();
		result.stream().sorted(Comparators.libraryComparator()).forEachOrdered(l -> sorted.add(l));

		return sorted;
	}

	private static class ArtifactIndexer {
		private Map<EqProxy<AnalysisArtifact>, AnalysisArtifact> artifactIdentityMap = new HashMap<>();
		private Set<AnalysisArtifact> visited = new HashSet<>();
		private Set<Artifact> resultingArtifacts = new HashSet<>();
		private MultiMap<Artifact, Artifact> transitiveDependersMap = new HashMultiMap<>();

		public AnalysisArtifact acquireArtifact(AnalysisArtifact analysisArtifact) {

			AnalysisArtifact identityManagedArtifact = artifactIdentityMap.computeIfAbsent(HashComparators.analysisArtifact.eqProxy(analysisArtifact),
					k -> analysisArtifact);
			if (!visited.add(analysisArtifact)) {
				return identityManagedArtifact;
			}

			for (AnalysisDependency depender : analysisArtifact.getDependers()) {
				AnalysisArtifact d = depender.getDepender();
				if (d != null) {
					AnalysisArtifact directDependerArtifact = this.acquireArtifact(d);
					transitiveDependersMap.put(identityManagedArtifact, directDependerArtifact);
				}
			}

			return identityManagedArtifact;
		}

		public void addResultingArtifact(AnalysisArtifact analysisArtifact) {
			AnalysisArtifact acquiredArtifact = acquireArtifact(analysisArtifact);
			resultingArtifacts.add(acquiredArtifact);
		}

		public Set<Artifact> getResultingArtifacts() {
			return resultingArtifacts;
		}

		public MultiMap<Artifact, Artifact> getTransitiveDependersMap() {
			return transitiveDependersMap;
		}
	}

	@Configurable
	public void setRepositoryBasePath(String repositoryBasePath) {
		if (!StringTools.isBlank(repositoryBasePath)) {
			this.repositoryBasePath = repositoryBasePath;
		}
	}
	@Configurable
	public void setProfile(Profile profile) {
		if (profile != null) {
			this.profile = profile;
		}
	}
	@Configurable
	public void setRepositoryUsername(String repositoryUsername) {
		this.repositoryUsername = repositoryUsername;
	}
	@Configurable
	public void setRepositoryPassword(String repositoryPassword) {
		this.repositoryPassword = repositoryPassword;
	}
	public void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}
        public void setRavenhurstUrl(String ravenhurstUrl) { 
		this.ravenhurstUrl = ravenhurstUrl;
	}
}
