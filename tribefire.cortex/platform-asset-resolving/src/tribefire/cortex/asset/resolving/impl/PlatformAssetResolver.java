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
package tribefire.cortex.asset.resolving.impl;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import com.braintribe.build.artifact.api.DependencyResolver;
import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.enriching.MultiRepositorySolutionEnricher;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.BuildDependencyResolutionContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.space.FilterConfigurationSpace;
import com.braintribe.cfg.Configurable;
import com.braintribe.common.lcd.Pair;
import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.output.ConfigurableConsoleOutputContainer;
import com.braintribe.devrock.model.repositoryview.resolution.RepositoryViewResolution;
import com.braintribe.exception.Exceptions;
import com.braintribe.execution.virtual.VirtualThreadExecutorBuilder;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.info.RepositoryOrigin;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.asset.PlatformAssetDependency;
import com.braintribe.model.asset.natures.Documentation;
import com.braintribe.model.asset.natures.InheritedByDependencies;
import com.braintribe.model.asset.natures.ModelPriming;
import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.model.asset.natures.RepositoryView;
import com.braintribe.model.asset.natures.SupportsNonAssetDeps;
import com.braintribe.model.asset.selector.DependencySelector;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.processing.core.expert.api.DenotationMap;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMap;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.processing.async.api.Promise;
import com.braintribe.processing.async.impl.HubPromise;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.module.WireModule;

import tribefire.cortex.asset.resolving.api.PlatformAssetNatureInheritanceExpert;
import tribefire.cortex.asset.resolving.api.PlatformAssetResolvingConstants;
import tribefire.cortex.asset.resolving.api.PlatformAssetResolvingContext;
import tribefire.cortex.asset.resolving.wire.PlatformAssetResolvingWireModule;

public class PlatformAssetResolver implements PlatformAssetResolvingConstants, AutoCloseable {

	private static final Logger logger = Logger.getLogger(PlatformAssetResolver.class);
	private static DecimalFormat secondsFormat = new DecimalFormat("0.###s", new DecimalFormatSymbols(Locale.US));

	private final SortedSet<PlatformAssetSolution> effectiveSetupSolutions = new TreeSet<>();
	private final Map<String, PlatformAssetSolution> solutionsByName = new ConcurrentHashMap<>();
	private final Map<String, PlatformAssetSolution> solutionsByDependencyName = new ConcurrentHashMap<>();

	private static PartTuple assetPartTuple = PartTupleProcessor.fromString(PART_IDENTIFIER_ASSET_MAN);

	private final DenotationMap<InheritedByDependencies, PlatformAssetNatureInheritanceExpert<?>> inheritanceExperts;

	private DenotationMap<PlatformAssetNature, List<String>> natureParts;

	private final PlatformAssetResolvingContext PlatformAssetResolvingContext;

	private final PlatformAssetNatureLoading platformAssetNatureLoading = new PlatformAssetNatureLoading();

	private final List<Dependency> setupDependencies;
	private final PlatformAssetResolvingContext context;
	private boolean noDocu;
	private final WireContext<BuildDependencyResolutionContract> wireContext;
	private final Dependency projectDependency;
	private boolean selectorFiltering = true;
	private ExecutorService executorService;
	private final List<Promise<PartLoadingInfo>> enrichingPromises = Collections.synchronizedList(new ArrayList<>());
	private final FilterConfiguration filterConfiguration = new FilterConfiguration();
	private boolean modelPrimingUsed;
	private final Object downloadOutputMonitor = new Object();
	private boolean downloadOutputDone;
	private final boolean verbose = false;
	private boolean useFullyQualifiedGlobalAssetId;
	private RepositoryViewResolution repositoryViewResolution;

	public PlatformAssetResolver(WireModule integrationModule, PlatformAssetResolvingContext context, Dependency dependency) {
		this(integrationModule, context, Collections.singletonList(dependency), new PolymorphicDenotationMap<>());
	}

	public PlatformAssetResolver(WireModule integrationModule, PlatformAssetResolvingContext context, List<Dependency> setupDependencies) {
		this(integrationModule, context, setupDependencies, new PolymorphicDenotationMap<>());
	}

	public PlatformAssetResolver(WireModule integrationModule, PlatformAssetResolvingContext context, List<Dependency> setupDependencies,
			DenotationMap<InheritedByDependencies, PlatformAssetNatureInheritanceExpert<?>> inheritanceExperts) {
		this(integrationModule, context, null, setupDependencies, inheritanceExperts);
	}

	public PlatformAssetResolver(WireModule integrationModule, PlatformAssetResolvingContext context, Dependency projectDependency,
			List<Dependency> setupDependencies, DenotationMap<InheritedByDependencies, PlatformAssetNatureInheritanceExpert<?>> inheritanceExperts) {
		this.context = context;
		this.projectDependency = projectDependency;
		this.setupDependencies = setupDependencies;
		this.PlatformAssetResolvingContext = context;
		this.inheritanceExperts = inheritanceExperts;

		PlatformAssetResolvingWireModule module = new PlatformAssetResolvingWireModule(filterConfiguration, integrationModule);

		wireContext = Wire.context(module);
	}

	@Configurable
	public void setUseFullyQualifiedGlobalAssetId(boolean useFullyQualifiedGlobalAssetId) {
		this.useFullyQualifiedGlobalAssetId = useFullyQualifiedGlobalAssetId;
	}

	public WireContext<BuildDependencyResolutionContract> getWireContext() {
		return wireContext;
	}

	public void setNatureParts(DenotationMap<PlatformAssetNature, List<String>> natureParts) {
		this.natureParts = natureParts;
	}

	public void setSelectorFiltering(boolean selectorFiltering) {
		this.selectorFiltering = selectorFiltering;
	}

	public void resolve() {
		executorService = VirtualThreadExecutorBuilder.newPool().concurrency(10).build();
		try {
			BuildDependencyResolutionContract contract = wireContext.contract();

			repositoryViewResolution = contract.repositoryReflection().getRepositoryViewResolution();

			DependencyResolver resolver = contract.buildDependencyResolver();

			List<Dependency> dependencies = new ArrayList<>();

			if (projectDependency != null)
				dependencies.add(projectDependency);

			dependencies.addAll(setupDependencies);
			resolver.resolve(dependencies);
			postProcessResolution();

			markCoreSyncModelsAsProvidedIfNeccessary(resolver);

			for (Promise<PartLoadingInfo> future : enrichingPromises) {
				try {
					PartLoadingInfo partLoadingInfo = future.get();

					if (partLoadingInfo != null)
						partLoadingInfo.solution.getParts().addAll(partLoadingInfo.parallelSolution.getParts());

				} catch (Exception e) {
					throw Exceptions.unchecked(e, "Error while enriching parts");
				}
			}
		} finally {
			executorService.shutdown();
		}
	}

	@Override
	public void close() throws Exception {
		wireContext.close();
	}

	private void markCoreSyncModelsAsProvidedIfNeccessary(DependencyResolver resolver) {
		if (modelPrimingUsed) {
			Dependency tribefireSyncModelDependency = NameParser
					.parseCondensedDependencyNameAndAutoRangify("tribefire.cortex:tribefire-sync-model#2.0");
			filterConfiguration.setCoreSyncModelMode(true);
			Set<Solution> syncModels = resolver.resolve(tribefireSyncModelDependency);

			for (Solution syncModel : syncModels) {
				PlatformAssetSolution syncModelAssetSolution = solutionsByName.get(NameParser.buildName(syncModel));
				if (syncModelAssetSolution != null)
					syncModelAssetSolution.asset.setPlatformProvided(true);
			}
		}
	}

	private void postProcessResolution() {
		List<PlatformAssetSolution> effectiveSetupDependencies = new ArrayList<>();

		// second phase to wire the PlatformAsset dependencies
		for (PlatformAssetSolution assetSolution : solutionsByName.values()) {
			for (Dependency requestorDependency : assetSolution.solution.getRequestors()) {
				solutionsByDependencyName.put(NameParser.buildName(requestorDependency), assetSolution);
			}
		}

		for (PlatformAssetSolution assetSolution : solutionsByName.values()) {
			for (Dependency dependency : assetSolution.filteredDependencies) {
				PlatformAssetSolution classifiedSolutionDependency = getSolutionFor(dependency);

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

		for (Dependency setupDependency : setupDependencies) {
			PlatformAssetSolution assetSolution = getSolutionFor(setupDependency);
			if (assetSolution != null)
				effectiveSetupDependencies.add(assetSolution);
		}

		for (PlatformAssetSolution setupDependency : effectiveSetupDependencies)
			collectConfigurationSolution(setupDependency);

	}

	public RepositoryViewResolution getRepositoryViewResolution() {
		return repositoryViewResolution;
	}

	public PlatformAssetSolution getSolutionForName(String name) {
		return solutionsByName.get(name);
	}

	public PlatformAssetSolution getSolutionFor(Solution solution) {
		return getSolutionForName(NameParser.buildName(solution));
	}

	public PlatformAssetSolution getSolutionFor(Dependency dependency) {
		return solutionsByDependencyName.get(NameParser.buildName(dependency));
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

	public SortedSet<PlatformAssetSolution> getSolutions() {
		return effectiveSetupSolutions;
	}

	public void setNoDocu(boolean noDocu) {
		this.noDocu = noDocu;
	}

	private class FilterConfiguration extends FilterConfigurationSpace {
		private boolean coreSyncModelMode = false;

		public void setCoreSyncModelMode(boolean coreSyncModelMode) {
			this.coreSyncModelMode = coreSyncModelMode;
		}

		@Override
		public Predicate<? super Solution> solutionFilter() {
			return this::isAssetSolution;
		}

		@Override
		public BiPredicate<? super Solution, ? super Dependency> solutionDependencyFilter() {
			return (s, d) -> {

				boolean filtered = !"functional".equals(d.getGroup()) && !d.getOptional()
						&& !DependencyManagementTools.excludedScopes.contains(d.getScope());

				if (coreSyncModelMode)
					return filtered;

				PlatformAssetSolution assetSolution = getSolutionFor(s);

				if (assetSolution != null) {
					PlatformAssetNature platformAssetNature = assetSolution.nature;
					if (platformAssetNature instanceof SupportsNonAssetDeps) {
						filtered = filtered && d.getTags().contains("asset");
					}

					// if (platformAssetNature instanceof InheritedByDependencies) {
					// InheritedByDependencies inheritedByDependencies = (InheritedByDependencies)platformAssetNature;
					// PlatformAssetNatureInheritanceExpert<?> expert = inheritanceExperts.find(inheritedByDependencies);
					// filtered = filtered && expert.isValidDependencyForInheritance(PlatformAssetResolvingContext, d);
					// }
				}

				if (filtered)
					assetSolution.filteredDependencies.add(d);

				return filtered;
			};
		}

		private boolean isAssetSolution(Solution solution) {
			try {
				if (coreSyncModelMode)
					return true;

				String solutionName = NameParser.buildName(solution);
				logger.debug("testing " + solutionName + " to be an asset");
				// updateProgress();

				// TODO: check if we can get a better walkScopeId or finally get rid of it
				String walkScopeId = "unknown";

				BuildDependencyResolutionContract contract = wireContext.contract();
				MultiRepositorySolutionEnricher solutionEnricher = contract.solutionEnricher();
				RepositoryReflection repositoryReflection = contract.repositoryReflection();

				// TODO: use pom to provide an optional hint for the nature to boost the resolution
				PartLoadingInfo manLoadingInfo = loadPart(solution, walkScopeId, solutionEnricher, assetPartTuple);

				if (manLoadingInfo != null)
					manLoadingInfo.solution.getParts().addAll(manLoadingInfo.parallelSolution.getParts());

				PlatformAssetNature natureFoundForSolution = platformAssetNatureLoading.findNature(solution);
				PlatformAssetNature nature; // must be (effectively) final to be used below
				if (natureFoundForSolution != null) {
					nature = natureFoundForSolution;
				} else if (solutionName.contains("-view#")) {
					// no nature found, but this is expected for repository views, since views are no longer assets.
					// to still support them in Jinni 2.0 we inject the nature here.
					nature = RepositoryView.T.create();
				} else {
					// not an asset
					nature = null;
				}

				// check for missing asset.man (nature) and fail in that case
				if (nature == null) {
					// throw new IllegalStateException("Asset [" + solutionName + "] is missing asset.man part.");
					logger.warn("Asset [" + solutionName + "] is missing asset.man part.");
					return false;
				}

				if (nature instanceof Documentation && noDocu)
					return false;

				DownloadReflector downloadReflector = new DownloadReflector(solution);

				if (natureParts != null) {
					List<String> parts = natureParts.find(nature);

					if (parts != null && !parts.isEmpty()) {

						downloadReflector.increaseExpectedParts(parts.size());

						for (String part : parts) {
							PartTuple partTuple = PartTupleProcessor.fromString(part);

							Promise<PartLoadingInfo> enrichingFuture = submit(() -> {
								return loadPart(solution, walkScopeId, solutionEnricher, partTuple);
							});

							enrichingFuture.get(downloadReflector);

							enrichingPromises.add(enrichingFuture);
						}
					}
				}

				downloadReflector.onSuccess(manLoadingInfo);

				String name = NameParser.buildName(solution);

				PlatformAssetSolution assetSolution = solutionsByName.computeIfAbsent(name, k -> {
					synchronized (solutionsByName) {
						List<RepositoryOrigin> repositoryOrigins = repositoryReflection.acquireArtifactReflectionExpert(solution)
								.getVersionOrigin(solution.getVersion()).getRepositoryOrigins();
						return new PlatformAssetSolution(solution, ensureNatureInSession(nature, k), context.session(), repositoryOrigins,
								useFullyQualifiedGlobalAssetId);
					}
				});

				return true;
			} catch (Exception e) {
				throw Exceptions.unchecked(e, "Error while checking if resolved artifact is an asset: " + NameParser.buildName(solution));
			}
		}

		private PartLoadingInfo loadPart(Solution solution, String walkScopeId, MultiRepositorySolutionEnricher solutionEnricher,
				PartTuple partTuple) {
			ensureDownloadOpeningOutput();
			long s = System.currentTimeMillis();

			Solution parallelSolution = Solution.T.create();
			ArtifactProcessor.transferIdentification(parallelSolution, solution);

			Pair<Part, Boolean> resolvedPart = solutionEnricher.enrichAndReflectDownload(walkScopeId, parallelSolution, partTuple);
			if (resolvedPart == null)
				return null;

			long e = System.currentTimeMillis();
			return new PartLoadingInfo(partTuple, resolvedPart.getSecond(), e - s, parallelSolution, solution);
		}

		private long lastProgressUpdate = System.currentTimeMillis();
		private static final long PROGRESS_UPDATE_INTERVALL = 1000;

		@SuppressWarnings("unused")
		private void updateProgress() {
			long currentTimeMillis = System.currentTimeMillis();

			if (currentTimeMillis - lastProgressUpdate > PROGRESS_UPDATE_INTERVALL) {
				lastProgressUpdate = currentTimeMillis;

				ConsoleOutputs.print("Dependency resolution in progress. Number of resolved assets so far: " + solutionsByName.size());
			}

		}

		@Override
		public boolean filterSolutionBeforeVisit() {
			return true;
		}

	}

	private void ensureDownloadOpeningOutput() {
		if (!downloadOutputDone) {
			synchronized (this.downloadOutputMonitor) {
				if (!downloadOutputDone) {
					ConsoleOutputs.println("\nChecking missing asset parts...");
					downloadOutputDone = true;
				}
			}
		}
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

	private static class PartLoadingInfo {
		boolean freshDownload;
		long milliSeconds;
		PartTuple part;
		Solution parallelSolution;
		Solution solution;

		public PartLoadingInfo(PartTuple part, boolean freshDownload, long milliSeconds, Solution parallelSolution, Solution solution) {
			super();
			this.part = part;
			this.freshDownload = freshDownload;
			this.milliSeconds = milliSeconds;
			this.parallelSolution = parallelSolution;
			this.solution = solution;
		}

		public boolean isFreshDownload() {
			return freshDownload;
		}

	}

	private class DownloadReflector implements AsyncCallback<PartLoadingInfo> {
		private final List<PartLoadingInfo> infos;
		private final Solution solution;
		private int expectedParts = 1;
		private final long startTime = System.currentTimeMillis();

		public DownloadReflector(Solution solution) {
			this.solution = solution;
			this.infos = new ArrayList<>();
		}

		public void increaseExpectedParts(int expectedParts) {
			this.expectedParts += expectedParts;
		}

		@Override
		public void onSuccess(PartLoadingInfo info) {
			if (info != null && info.isFreshDownload()) {
				synchronized (infos) {
					infos.add(info);
				}
			}
			checkForOutput();
		}

		@Override
		public void onFailure(Throwable t) {
			checkForOutput();
		}

		private void checkForOutput() {
			if (--expectedParts == 0) {
				if (!infos.isEmpty()) {
					ConfigurableConsoleOutputContainer sequence = ConsoleOutputs.configurableSequence();
					sequence.append("Downloaded parts for: ");
					sequence.append(ArtifactOutputs.solution(solution));

					if (verbose) {
						sequence.append(ConsoleOutputs.brightBlack(" -> "));

						// Downloaded parts for foo.bar:X#1.0 -> jar in 0.5s, asset.man in

						boolean first = true;
						for (PartLoadingInfo info : infos) {
							if (first)
								first = false;
							else
								sequence.append(", ");

							String classifier = info.part.getClassifier();
							if (!StringTools.isEmpty(classifier)) {
								sequence.append(classifier);
								sequence.append(".");
							}

							sequence.append(info.part.getType());
							sequence.append(" ");

							appendTime(sequence, info.milliSeconds);
						}
					} else {
						long ms = System.currentTimeMillis() - startTime;
						sequence.append(" ");
						appendTime(sequence, ms);
					}

					ConsoleOutputs.println(sequence);
				}

			}
		}

		private void appendTime(ConfigurableConsoleOutputContainer sequence, long ms) {
			String time = secondsFormat.format(ms / 1000D);

			if (ms < 1000) {
				sequence.append(ConsoleOutputs.brightBlack(time));
			} else if (ms < 5000) {
				sequence.append(ConsoleOutputs.yellow(time));
			} else {
				sequence.append(ConsoleOutputs.red(time));
			}
		}
	}

}
