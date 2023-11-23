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
package tribefire.cortex.asset.resolving.ng.impl;

import static com.braintribe.utils.lcd.CollectionTools2.newConcurrentMap;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.output.ConfigurableConsoleOutputContainer;
import com.braintribe.devrock.mc.api.download.PartEnrichingContext;
import com.braintribe.devrock.mc.api.event.EventBroadcasterAttribute;
import com.braintribe.devrock.mc.api.event.EventContext;
import com.braintribe.devrock.mc.api.event.EventHub;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.ArtifactPartResolver;
import com.braintribe.devrock.mc.api.transitive.ArtifactPathElement;
import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.commons.DownloadMonitor;
import com.braintribe.devrock.model.mc.core.event.OnPartDownloaded;
import com.braintribe.devrock.model.mc.reason.IncompleteArtifactResolution;
import com.braintribe.devrock.model.mc.reason.UnresolvedDependency;
import com.braintribe.devrock.model.mc.reason.UnresolvedPart;
import com.braintribe.devrock.model.repository.MavenFileSystemRepository;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.exception.Exceptions;
import com.braintribe.execution.virtual.VirtualThreadExecutorBuilder;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.consumable.PartEnrichment;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.artifact.info.RepositoryOrigin;
import com.braintribe.model.asset.PlatformAssetDependency;
import com.braintribe.model.asset.natures.Documentation;
import com.braintribe.model.asset.natures.ModelPriming;
import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.model.asset.natures.SupportsNonAssetDeps;
import com.braintribe.model.asset.selector.DependencySelector;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.processing.core.expert.api.DenotationMap;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.braintribe.processing.async.api.Promise;
import com.braintribe.processing.async.impl.HubPromise;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.utils.lcd.LazyInitialized;

import tribefire.cortex.asset.resolving.ng.api.AssetDependencyResolver;
import tribefire.cortex.asset.resolving.ng.api.AssetResolutionContext;
import tribefire.cortex.asset.resolving.ng.api.PlatformAssetResolution;
import tribefire.cortex.asset.resolving.ng.api.PlatformAssetResolvingConstants;

public class PlatformAssetResolver implements AssetDependencyResolver, PlatformAssetResolvingConstants {

	private static final Logger logger = Logger.getLogger(PlatformAssetResolver.class);
	private static DecimalFormat secondsFormat = new DecimalFormat("0.###s", new DecimalFormatSymbols(Locale.US));
	private static PartIdentification assetManPart = PartIdentification.parse(PART_IDENTIFIER_ASSET_MAN);
	private TransitiveDependencyResolver transitiveResolver;
	private RepositoryReflection repositoryReflection;
	private ArtifactPartResolver artifactPartResolver;

	@Required
	@Configurable
	public void setRepositoryReflection(RepositoryReflection repositoryReflection) {
		this.repositoryReflection = repositoryReflection;
	}

	@Required
	@Configurable
	public void setTransitiveDependencyResolver(TransitiveDependencyResolver transitiveResolver) {
		this.transitiveResolver = transitiveResolver;
	}

	@Required
	@Configurable
	public void setArtifactPartResolver(ArtifactPartResolver artifactPartResolver) {
		this.artifactPartResolver = artifactPartResolver;
	}

	@Override
	public PlatformAssetResolution resolve(AssetResolutionContext context, CompiledDependencyIdentification projectDependency,
			List<CompiledDependencyIdentification> setupDependencies) {
		return new StatefullAssetResolver(context, projectDependency, setupDependencies).resolve();
	}

	@Override
	public PlatformAssetResolution resolve(AssetResolutionContext context, CompiledDependencyIdentification setupDependency) {
		return new StatefullAssetResolver(context, null, Collections.singletonList(setupDependency)).resolve();
	}

	@Override
	public PlatformAssetResolution resolve(AssetResolutionContext context, List<CompiledDependencyIdentification> setupDependencies) {
		return new StatefullAssetResolver(context, null, setupDependencies).resolve();
	}

	private class StatefullAssetResolver implements PlatformAssetResolution {
		private final SortedSet<PlatformAssetSolution> effectiveSetupSolutions = new TreeSet<>();
		private final Map<String, PlatformAssetSolution> solutionsByName = new ConcurrentHashMap<>();
		private final Map<String, PlatformAssetSolution> solutionsByDependencyName = new ConcurrentHashMap<>();

		private final DenotationMap<PlatformAssetNature, List<String>> natureParts;

		private final PlatformAssetNatureLoading platformAssetNatureLoading = new PlatformAssetNatureLoading();

		private final List<CompiledDependencyIdentification> setupDependencies;
		private final AssetResolutionContext context;
		private final boolean noDocu;
		private final CompiledDependencyIdentification projectDependency;
		private boolean selectorFiltering = true;
		private ExecutorService executorService;
		private final Map<String, Promise<Optional<Part>>> enrichingPromises = Collections.synchronizedMap(new LinkedHashMap<>());
		private boolean modelPrimingUsed;
		private final boolean verbose;
		private final Object sessionMonitor = new Object();
		private AnalysisArtifactResolution resolution;
		private Reason failure;
		private final Map<String, RepositoryOrigin> origins = new HashMap<>();

		public StatefullAssetResolver(AssetResolutionContext context, CompiledDependencyIdentification projectDependency,
				List<CompiledDependencyIdentification> setupDependencies) {
			this.context = context;
			this.projectDependency = projectDependency;
			this.setupDependencies = setupDependencies;
			this.natureParts = context.natureParts();
			this.noDocu = !context.includeDocumentation();
			this.selectorFiltering = context.selectorFiltering();
			this.verbose = context.verboseOutput();
		}
		private AnalysisArtifactResolution resolve(List<CompiledTerminal> terminals) {
			TransitiveResolutionContext transitiveResolutionContext = createTransitiveResolutionContext();
			return transitiveResolver.resolve(transitiveResolutionContext, terminals);
		}

		public PlatformAssetResolution resolve() {
			EventHub eventHub = new EventHub();

			// eventHub.addListener(OnPartDownloaded.T, this::onPartDownloaded);

			AttributeContext attributeContext = AttributeContexts.derivePeek() //
					.set(EventBroadcasterAttribute.class, eventHub) //
					.build(); //

			executorService = VirtualThreadExecutorBuilder.newPool().concurrency(10) //
					.threadNamePrefix("platform-asset-enriching") //
					.build();

			AttributeContexts.push(attributeContext);

			try (DownloadMonitor downloadMonitor = new DownloadMonitor(eventHub)) {
				downloadMonitor.setIndent(2);
				downloadMonitor.setInitialLinebreak(true);

				List<CompiledTerminal> dependencies = new ArrayList<>();

				if (projectDependency != null)
					dependencies.add(CompiledTerminal.from(projectDependency));

				setupDependencies.stream().map(CompiledTerminal::from).forEach(dependencies::add);

				resolution = resolve(dependencies);

				if (resolution.hasFailed()) {
					if (!context.lenient())
						throw new IllegalStateException(resolution.getFailure().stringify());

					failure = resolution.getFailure();

					return this;
				}

				postProcessResolution();

				markCoreSyncModelsAsProvidedIfNeccessary();

				for (Map.Entry<String, Promise<Optional<Part>>> entry : enrichingPromises.entrySet()) {
					Promise<Optional<Part>> future = entry.getValue();
					String partIdentity = entry.getKey();

					try {
						future.get();
					} catch (Exception e) {
						throw Exceptions.unchecked(e, "Error while enriching parts");
					}
				}

				// transfer origins

				for (PlatformAssetSolution assetSolution : solutionsByName.values()) {
					AnalysisArtifact solution = assetSolution.solution;
					solution.getParts().values().stream().map(Part::getRepositoryOrigin).distinct().sorted().map(this::acquireRepositoryOrigin)
							.forEach(assetSolution.asset.getRepositoryOrigins()::add);
				}

			} finally {
				AttributeContexts.pop();
				executorService.shutdown();
			}

			return this;
		}

		private RepositoryOrigin acquireRepositoryOrigin(String repositoryId) {
			return origins.computeIfAbsent(repositoryId, this::buildRepositoryOrigin);
		}

		private RepositoryOrigin buildRepositoryOrigin(String repositoryId) {
			RepositoryOrigin origin = RepositoryOrigin.T.create();
			origin.setName(repositoryId);
			Repository repository = repositoryReflection.getRepository(repositoryId);

			if (repository instanceof MavenHttpRepository) {
				origin.setUrl(((MavenHttpRepository) repository).getUrl());
			} else if (repository instanceof MavenFileSystemRepository) {
				origin.setUrl(new File(((MavenFileSystemRepository) repository).getRootPath()).toURI().toString());
			}

			return origin;
		}

		@Override
		public Reason getFailure() {
			return failure;
		}

		@Override
		public AnalysisArtifactResolution artifactResolution() {
			return resolution;
		}

		private TransitiveResolutionContext createTransitiveResolutionContext() {
			PartEnrichingContext enrichingContext = PartEnrichingContext.build() //
					.enrichingExpert(this::determineEnrichments) //
					.done();

			TransitiveResolutionContext transitiveResolutionContext = TransitiveResolutionContext.build().artifactPathFilter(this::isAssetSolution)
					.dependencyFilter(this::isAssetDependency).enrich(enrichingContext).done();
			return transitiveResolutionContext;
		}

		private List<PartEnrichment> determineEnrichments(AnalysisArtifact artifact) {
			String name = artifact.asString();

			PlatformAssetSolution assetSolution = solutionsByName.get(name);

			if (assetSolution != null && natureParts != null) {
				List<String> parts = natureParts.find(assetSolution.nature);

				if (parts != null && !parts.isEmpty()) {
					List<PartEnrichment> enrichments = new ArrayList<>(parts.size());
					for (String part : parts) {
						PartIdentification partIdentification = PartIdentification.parse(part);
						PartEnrichment enrichment = PartEnrichment.T.create();
						enrichment.setClassifier(partIdentification.getClassifier());
						enrichment.setType(partIdentification.getType());
						enrichment.setMandatory(false);
						enrichment.setKey(partIdentification.asString());
						enrichments.add(enrichment);
					}

					return enrichments;
				}
			}

			return Collections.emptyList();
		}

		private void onPartDownloaded(EventContext context, OnPartDownloaded event) {
			ConfigurableConsoleOutputContainer sequence = ConsoleOutputs.configurableSequence();
			sequence.append("Downloaded ");
			sequence.append(ArtifactOutputs.part(event.getPart()));

			appendTime(sequence, event.getElapsedTime());

			ConsoleOutputs.println(sequence);
		}

		private void markCoreSyncModelsAsProvidedIfNeccessary() {
			if (modelPrimingUsed) {
				CompiledDependencyIdentification tribefireSyncModelDependency = CompiledDependencyIdentification
						.parse("tribefire.cortex:tribefire-sync-model#[2.0,2.1)");

				CompiledTerminal tribefireSyncModelTerminal = CompiledTerminal.from(tribefireSyncModelDependency);

				AnalysisArtifactResolution resolution = resolve(Collections.singletonList(tribefireSyncModelTerminal));

				for (AnalysisArtifact syncModel : resolution.getSolutions()) {
					PlatformAssetSolution syncModelAssetSolution = getSolutionFor(syncModel);
					if (syncModelAssetSolution != null)
						syncModelAssetSolution.asset.setPlatformProvided(true);
				}
			}
		}

		private void postProcessResolution() {
			List<PlatformAssetSolution> effectiveSetupDependencies = new ArrayList<>();

			// second phase to wire the PlatformAsset dependencies
			for (PlatformAssetSolution assetSolution : solutionsByName.values()) {
				for (AnalysisDependency depender : assetSolution.solution.getDependers()) {
					solutionsByDependencyName.put(CompiledDependencyIdentification.asString(depender.getOrigin()), assetSolution);
				}

				for (AnalysisDependency dependency : assetSolution.filteredDependencies) {
					AnalysisArtifact depSolution = dependency.getSolution();
					if (depSolution == null)
						if (dependency.hasFailed())
							throw new NoSuchElementException("Asset solution is missing for dependency: " + dependency.asString());
						else
							continue;

					PlatformAssetSolution classifiedSolutionDependency = getSolutionFor(depSolution);

					if (classifiedSolutionDependency != null) {
						if (assetSolution.nature instanceof ModelPriming) {
							modelPrimingUsed = true;
						}

						assetSolution.dependencies.add(classifiedSolutionDependency);
						Map<String, Object> info = DependencyDecoding.decodeDependency(dependency);

						PlatformAssetDependency assetDependency = context.session().create(PlatformAssetDependency.T);
						assetDependency.setAsset(classifiedSolutionDependency.asset);

						DependencySelector dependencySelector = (DependencySelector) info.get("$selector");
						Boolean isGlobalSetupCandidate = (Boolean) info.get("$isGlobalSetupCandidate");

						if (dependencySelector != null)
							assetDependency.setSelector(dependencySelector);

						if (isGlobalSetupCandidate != null)
							assetDependency.setIsGlobalSetupCandidate(isGlobalSetupCandidate);

						assetSolution.asset.getQualifiedDependencies().add(assetDependency);

						if (assetDependency.getIsGlobalSetupCandidate()) {
							DependencySelector selector = assetDependency.getSelector();

							if (selector == null || GenericDependencySelectorProcessor.INSTANCE.matches(context, selector)) {
								effectiveSetupDependencies.add(assetSolution);
							}
						}
					}
				}
			}

			for (CompiledDependencyIdentification setupDependency : setupDependencies) {
				PlatformAssetSolution assetSolution = getSolutionFor(setupDependency);
				if (assetSolution != null)
					effectiveSetupDependencies.add(assetSolution);
			}

			for (PlatformAssetSolution setupDependency : effectiveSetupDependencies)
				collectConfigurationSolution(setupDependency);

		}

		@Override
		public PlatformAssetSolution getSolutionFor(AnalysisArtifact solution) {
			return getSolutionForName(solution.asString());
		}

		@Override
		public PlatformAssetSolution getSolutionForName(String name) {
			return solutionsByName.get(name);
		}

		@Override
		public PlatformAssetSolution getSolutionFor(CompiledDependencyIdentification dependency) {
			return solutionsByDependencyName.get(CompiledDependencyIdentification.asString(dependency));
		}

		private <T> Promise<T> submit(Callable<T> callable) {
			HubPromise<T> promise = new HubPromise<>();

			executorService.execute(() -> {
				try {
					promise.accept(callable.call());
				} catch (Throwable e) {
					promise.onFailure(e);
				}
			});

			return promise;
		}

		private void collectConfigurationSolution(PlatformAssetSolution classifiedSolution) {
			if (effectiveSetupSolutions.add(classifiedSolution)) {
				for (PlatformAssetDependency assetDependency : classifiedSolution.asset.getQualifiedDependencies()) {
					if (selectorFiltering) {
						DependencySelector selector = assetDependency.getSelector();

						if (selector != null && !GenericDependencySelectorProcessor.INSTANCE.matches(context, selector)) {
							assetDependency.setSkipped(true);
							continue;
						}

						PlatformAssetSolution classifiedSolutionDependency = getSolutionForName(
								assetDependency.getAsset().qualifiedRevisionedAssetName());

						if (classifiedSolutionDependency != null) {
							collectConfigurationSolution(classifiedSolutionDependency);
						}
					} else {
						PlatformAssetSolution classifiedSolutionDependency = getSolutionForName(
								assetDependency.getAsset().qualifiedRevisionedAssetName());
						collectConfigurationSolution(classifiedSolutionDependency);
					}
				}
			}
		}

		@Override
		public SortedSet<PlatformAssetSolution> getSolutions() {
			return effectiveSetupSolutions;
		}

		private boolean isAssetDependency(AnalysisDependency dependency) {
			// TODO: change to a simplified check after asset tag got a requirement in all asset dependencies (no implicit asset
			// dependencies)
			// In that case we can have 3 sequential phases of resolution (pom, man, payload) each in itself parallelized
			// See isExplicitAssetDependency()

			AnalysisArtifact depender = dependency.getDepender();

			CompiledDependency compiledDependency = dependency.getOrigin();
			boolean filtered = !compiledDependency.getTags().contains("functional") && //
					!compiledDependency.getOptional() && //
					!DependencyManagementTools.excludedScopes.contains(compiledDependency.getScope());

			PlatformAssetSolution assetSolution = depender != null ? getSolutionFor(depender) : null;

			if (assetSolution != null)
				if (assetSolution.nature instanceof SupportsNonAssetDeps)
					filtered = filtered && compiledDependency.getTags().contains("asset");

			if (filtered)
				assetSolution.filteredDependencies.add(dependency);

			return filtered;
		}

		private final Map<AnalysisArtifact, LazyInitialized<AssetSolutionInfo>> isAssetSolutionCache = newConcurrentMap();

		private boolean isAssetSolution(ArtifactPathElement element) {
			AnalysisArtifact solution = element.getArtifact();
			AssetSolutionInfo asi = isAssetSolutionCache.computeIfAbsent(solution, s -> new LazyInitialized<>(() -> new AssetSolutionInfo(s))).get();

			if (asi.hasMissingMan) {
				AnalysisDependency dependency = element.getParent().getDependency();

				if (isExplicitAssetDependency(dependency)) {

					UnresolvedPart cause2 = TemplateReasons.build(UnresolvedPart.T).assign(UnresolvedPart::setPart, assetManPart).toReason();

					IncompleteArtifactResolution cause = TemplateReasons.build(IncompleteArtifactResolution.T) //
							.assign(IncompleteArtifactResolution::setArtifact, CompiledArtifactIdentification.from(solution)) //
							.cause(cause2) //
							.toReason();

					UnresolvedDependency reason = TemplateReasons.build(UnresolvedDependency.T) //
							.assign(UnresolvedDependency::setDependency, CompiledDependencyIdentification.from(dependency.getOrigin())) //
							.cause(cause) //
							.toReason();
					dependency.setFailure(reason);
				}
			}

			return asi.isAssetSolution;
		}

		private boolean isExplicitAssetDependency(AnalysisDependency dependency) {
			CompiledDependency origin = dependency.getOrigin();
			return origin.getTags().contains("asset") || assetManPart.compare(origin) == 0;
		}

		class AssetSolutionInfo {

			private final AnalysisArtifact solution;
			private final boolean isAssetSolution;
			private boolean hasMissingMan;

			public AssetSolutionInfo(AnalysisArtifact solution) {
				this.solution = solution;
				this.isAssetSolution = isAssetSolution();
			}

			private boolean isAssetSolution() {
				try {
					String solutionName = solution.asString();
					logger.debug("testing " + solutionName + " to be an asset");

					// TODO: use pom property in yaml notation to have a more concise and eager asset nature
					Maybe<Part> manPartOptional = loadPart(solution, assetManPart);

					if (manPartOptional.isUnsatisfiedBy(NotFound.T)) {
						// hasMissingMan = true; // Deactivating because jinni was failing for old branches
						logger.debug("Artifact [" + solutionName + "] is no asset and will not be processed any further.");
						return false;
					}

					PlatformAssetNature nature = platformAssetNatureLoading.loadNatureFromResource(manPartOptional.get().getResource());

					if (nature instanceof Documentation && noDocu)
						return false;

					solutionsByName.computeIfAbsent(solution.asString(), k -> buildPlatformAssetSolution(solution, nature, k));

					return true;
				} catch (RuntimeException e) {
					throw Exceptions.unchecked(e, "Error while checking if resolved artifact is an asset: " + solution.asString());
				}
			}
		}

		private PlatformAssetSolution buildPlatformAssetSolution(AnalysisArtifact solution, PlatformAssetNature nature, String name) {
			synchronized (sessionMonitor) {
				return new PlatformAssetSolution(solution, ensureNatureInSession(nature, name), context.session());
			}
		}

		private Maybe<Part> loadPart(AnalysisArtifact solution, PartIdentification partIdentification) {
			Maybe<ArtifactDataResolution> optionalPart = artifactPartResolver.resolvePart(solution.getOrigin(), partIdentification);

			if (optionalPart.isUnsatisfied()) {
				return optionalPart.emptyCast();
			}

			ArtifactDataResolution resolution = optionalPart.get();

			Part part = Part.T.create();
			part.setClassifier(partIdentification.getClassifier());
			part.setType(partIdentification.getType());
			part.setRepositoryOrigin(resolution.repositoryId());
			part.setResource(resolution.getResource());

			synchronized (solution) {
				solution.getParts().put(partIdentification.asString(), part);
			}

			return Maybe.complete(part);
		}

		private PlatformAssetNature ensureNatureInSession(PlatformAssetNature nature, String id) {
			if (nature.session() == context.session())
				return nature;

			class CloningContextImpl extends StandardCloningContext {
				@Override
				public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
					return context.session().createRaw(entityType);
				}

				@Override
				public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property,
						GenericEntity instanceToBeCloned, GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {
					Object value = property.get(instanceToBeCloned);

					return !equalsNullSafe(value, property.getDefaultRawValue());
				}

				private boolean equalsNullSafe(Object o1, Object o2) {
					if (o1 == o2)
						return true;

					if (o1 == null || o2 == null)
						return false;

					return o1.equals(o2);
				}
			}

			nature = nature.clone(new CloningContextImpl());
			if (nature.getGlobalId() == null)
				nature.setGlobalId(nature.entityType().getShortName() + ":" + id);

			return nature;
		}

	}

	private static void appendTime(ConfigurableConsoleOutputContainer sequence, TimeSpan span) {
		double seconds = span.convertTo(TimeUnit.second).getValue();
		String time = span.formatWithFloorUnitAndSubUnit();

		if (seconds < 1) {
			sequence.append(ConsoleOutputs.brightBlack(time));
		} else if (seconds < 5) {
			sequence.append(ConsoleOutputs.yellow(time));
		} else {
			sequence.append(ConsoleOutputs.red(time));
		}
	}

}
