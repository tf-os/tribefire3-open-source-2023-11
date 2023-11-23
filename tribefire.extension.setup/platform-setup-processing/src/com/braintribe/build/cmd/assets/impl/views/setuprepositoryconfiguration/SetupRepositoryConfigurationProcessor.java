package com.braintribe.build.cmd.assets.impl.views.setuprepositoryconfiguration;

import static com.braintribe.build.cmd.assets.PlatformAssetDistributionConstants.FILE_REPOSITORY_CONFIGURATION;
import static com.braintribe.build.cmd.assets.PlatformAssetDistributionConstants.FILE_REPOSITORY_VIEW_RESOLUTION;
import static com.braintribe.build.cmd.assets.impl.views.RepositoryViewHelpers.writeYamlFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.braintribe.build.cmd.assets.impl.views.RepositoryViewHelpers;
import com.braintribe.devrock.mc.api.download.PartEnrichingContext;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.selectors.RepositorySelectorExpert;
import com.braintribe.devrock.mc.core.selectors.RepositorySelectors;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.devrock.model.repository.filters.DisjunctionArtifactFilter;
import com.braintribe.devrock.model.repository.filters.StandardDevelopmentViewArtifactFilter;
import com.braintribe.devrock.model.repositoryview.ConfigurationEnrichment;
import com.braintribe.devrock.model.repositoryview.RepositoryView;
import com.braintribe.devrock.model.repositoryview.enrichments.ArtifactFilterEnrichment;
import com.braintribe.devrock.model.repositoryview.enrichments.RepositoryEnrichment;
import com.braintribe.devrock.model.repositoryview.resolution.RepositoryViewResolution;
import com.braintribe.devrock.model.repositoryview.resolution.RepositoryViewSolution;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.analysis.AnalysisTerminal;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.platform.setup.api.GetLockedVersions;
import com.braintribe.model.platform.setup.api.SetupRepositoryConfiguration;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.lcd.CommonTools;
import com.braintribe.utils.lcd.NullSafe;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * Processes {@link SetupRepositoryConfiguration} requests. Transitively traverses all view artifacts, reads the
 * {@link RepositoryView} instances (from <code>repositoryview.yaml</code>) and creates a merged
 * {@link RepositoryConfiguration}. Furthermore, it creates a {@link RepositoryViewResolution} file.
 * 
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
public class SetupRepositoryConfigurationProcessor {

	public static final PartIdentification REPOSITORY_VIEW_PART_IDENTIFICATION = PartIdentification.create("repositoryview", "yaml");

	private static final Logger logger = Logger.getLogger(SetupRepositoryConfigurationProcessor.class);

	private final Map<RepositoryView, AnalysisArtifact> repositoryViews = new LinkedHashMap<>();

	public List<String> process(SetupRepositoryConfiguration request, VirtualEnvironment virtualEnvironment) {
		return process(request, virtualEnvironment, true);
	}
	/**
	 * Processes the passed request (see {@link SetupRepositoryConfigurationProcessor}).
	 * 
	 * @param includeStandardDependencies
	 *            whether or not to include dependencies. This is only needed from {@link GetLockedVersions} request
	 *            when {@link GetLockedVersions#getIncludeDependencies()} is disabled.
	 * @return all solutions for view artifacts and their parents.
	 */
	public List<String> process(SetupRepositoryConfiguration request, VirtualEnvironment virtualEnvironment, boolean includeStandardDependencies) {
		MavenConfigurationWireModule veModule = new MavenConfigurationWireModule(virtualEnvironment);

		FileTools.createDirectory(request.getInstallationPath());
		List<CompiledDependencyIdentification> dependencies = request.getRepositoryViews().stream() //
				.map(terminalArtifact -> CompiledDependencyIdentification.parseAndRangify(terminalArtifact, true)) //
				.collect(Collectors.toList());

		List<String> terminals;
		List<String> viewsSolutions = new ArrayList<String>();

		try (WireContext<TransitiveResolverContract> wireContext = Wire.context(TransitiveResolverWireModule.INSTANCE, veModule)) {
			logger.info(() -> "Using local repository "
					+ wireContext.contract(RepositoryConfigurationContract.class).repositoryConfiguration().get().getLocalRepositoryPath());

			final String solutionList = dependencies.stream().map(d -> d.asString()).collect(Collectors.joining("\\n"));
			logger.info(() -> "Resolving solutions for:\n" + solutionList);

			PartEnrichingContext peCtx = PartEnrichingContext.build().enrichPart(REPOSITORY_VIEW_PART_IDENTIFICATION).done();

			TransitiveResolutionContext trContext = TransitiveResolutionContext.build() //
					.includeParentDependencies(true) //
					.includeImportDependencies(true) //
					.includeStandardDependencies(includeStandardDependencies) 
					.enrich(peCtx).done();

			AnalysisArtifactResolution resolution = wireContext.contract().transitiveDependencyResolver().resolve(trContext, dependencies);
			
			handleFailures(solutionList, resolution);
			DependencyPrinting dependencyPrinting = new DependencyPrinting(false);
			dependencyPrinting.printAssetDependencyTree(resolution);
			
			terminals = resolution.getTerminals().stream().map(terminal -> ((AnalysisDependency)terminal).getSolution().asString()).collect(Collectors.toList());

			for (AnalysisArtifact solution : resolution.getSolutions()) {
				RepositoryView readRepositoryView = readRepositoryView(solution);
				if (readRepositoryView != null) {
					repositoryViews.put(readRepositoryView, solution);
				}
				viewsSolutions.add(solution.asString());
			}
		}

		RepositoryConfiguration mergedRepositoryConfiguration = createMergedRepositoryConfiguration(repositoryViews,
				request.getEnableDevelopmentMode());
		writeYamlFile(mergedRepositoryConfiguration, new File(request.getInstallationPath() + "/" + FILE_REPOSITORY_CONFIGURATION));

		RepositoryViewResolution repositoryViewResolution = createRepositoryViewResolution(repositoryViews, terminals);
		writeYamlFile(repositoryViewResolution, new File(request.getInstallationPath() + "/" + FILE_REPOSITORY_VIEW_RESOLUTION));
		return viewsSolutions;
	}

	private void handleFailures(final String solutionList, AnalysisArtifactResolution resolution) {
		List<String> failureMsgs = new ArrayList<>();

		if (resolution.hasFailed()) {
			failureMsgs.add(resolution.getFailure().stringify());
		}

		for (AnalysisArtifact analysisArtifact : resolution.getSolutions()) {
			if (analysisArtifact.hasFailed()) {
				failureMsgs.add(analysisArtifact.getFailure().stringify());
			}
		}

		for (AnalysisTerminal analysisTerminal : resolution.getTerminals()) {
			AnalysisDependency ad = (AnalysisDependency) analysisTerminal;
			if (ad.hasFailed()) {
				failureMsgs.add(ad.getFailure().stringify());
			}
		}
		if (!failureMsgs.isEmpty()) {
			String allFailures = IntStream.range(0, failureMsgs.size()) //
					.mapToObj(num -> "Failure " + (num + 1) + ": " + failureMsgs.get(num)) //
					.collect(Collectors.joining("\n"));
			throw new IllegalStateException("Resolution of :\n" + solutionList + "\nhas failed as:\n" + allFailures);
		}
	}

	private static RepositoryView readRepositoryView(AnalysisArtifact analysisArtifact) {
		final Optional<File> partFile = findPartFile(analysisArtifact, REPOSITORY_VIEW_PART_IDENTIFICATION);
		if (partFile.isPresent()) {
			return RepositoryViewHelpers.readYamlFile(partFile.get());
		}
		// this is expected (e.g. for parents)
		return null;
	}

	private static Optional<File> findPartFile(AnalysisArtifact solution, PartIdentification partIdentification) {
		Part part = solution.getParts().get(partIdentification.asString());
		if (part == null)
			return Optional.empty();

		Resource r = part.getResource();
		if (!(r instanceof FileResource))
			throw new IllegalStateException("Resource for part type '" + part.getType() + "' is not a FileResource. Resource: " + r);

		FileResource fr = (FileResource) r;
		return Optional.of(new File(fr.getPath()));
	}

	static RepositoryViewResolution createRepositoryViewResolution(Map<RepositoryView, AnalysisArtifact> repositoryViews, List<String> terminals) {
		RepositoryViewResolution repositoryViewResolution = RepositoryViewResolution.T.create();
		Map<String, RepositoryViewSolution> nameToRepositoryViewSolutions = new HashMap<>();

		for (RepositoryView repositoryView : repositoryViews.keySet()) {
			RepositoryViewSolution repositoryViewSolution = RepositoryViewSolution.T.create();
			final AnalysisArtifact analysisArtifact = repositoryViews.get(repositoryView);

			nameToRepositoryViewSolutions.put(analysisArtifact.asString(), repositoryViewSolution);

			repositoryViewSolution.setArtifact(analysisArtifact.asString());
			repositoryViewSolution.setRepositoryView(repositoryView);
			repositoryViewSolution.setDependencies(extractDependencies(nameToRepositoryViewSolutions, analysisArtifact));
			repositoryViewResolution.getSolutions().add(repositoryViewSolution);
			if (terminals.contains(analysisArtifact.asString())) {
				repositoryViewResolution.getTerminals().add(repositoryViewSolution);
			}
		}

		return repositoryViewResolution;
	}

	private static List<RepositoryViewSolution> extractDependencies(Map<String, RepositoryViewSolution> nameToRepositoryViewSolutions,
			final AnalysisArtifact analysisArtifact) {
		List<RepositoryViewSolution> dependencies = analysisArtifact.getDependencies().stream() //
				.filter(d -> !d.getArtifactId().equals("parent")).map(d -> d.getSolution().asString())
				.map(name -> Optional.ofNullable(nameToRepositoryViewSolutions.get(name)) //
						.orElseThrow(() -> new IllegalStateException("Could not find artifact " + name + " while resolving repository views."))) //
				.collect(Collectors.toList());
		return dependencies;
	}

	static RepositoryConfiguration createMergedRepositoryConfiguration(Map<RepositoryView, AnalysisArtifact> repositoryViews,
			boolean enableDevelopmentMode) {

		RepositoryConfiguration mergedRepositoryConfiguration = RepositoryConfiguration.T.create();
		List<Repository> mergedRepositoryConfigurationRepositories = mergedRepositoryConfiguration.getRepositories();

		// We silently remove the null key from the repositoryViews. This can happen if a YAML file of ReposisotyView
		// file is empty.
		repositoryViews.keySet().removeIf(Objects::isNull);
		validateRepositoryViews(repositoryViews);

		List<ConfigurationEnrichment> configurationEnrichments = new ArrayList<ConfigurationEnrichment>();

		for (RepositoryView repositoryView : repositoryViews.keySet()) {
			mergeRepositories(repositoryView.getRepositories(), mergedRepositoryConfigurationRepositories);
			configurationEnrichments.addAll(NullSafe.collection(repositoryView.getEnrichments()));
		}

		for (ConfigurationEnrichment configurationEnrichment : configurationEnrichments) {
			enrichRepositories(mergedRepositoryConfigurationRepositories, configurationEnrichment);
		}
		normalizeDisjunctionArtifactFilters(mergedRepositoryConfigurationRepositories);
		if (enableDevelopmentMode) {
			wrapFiltersWithStandardDevelopmentViewArtifactFilters(mergedRepositoryConfigurationRepositories);
		}
		return mergedRepositoryConfiguration;
	}

	private static void validateRepositoryViews(Map<RepositoryView, AnalysisArtifact> repositoryViews) {
		String validationChecksSummary = "";
		for (RepositoryView repositoryView : repositoryViews.keySet()) {
			String validationCheckInfo = "";
			for (Repository repository : NullSafe.iterable(repositoryView.getRepositories())) {
				if (repository == null) {
					validationCheckInfo += " - Repository should not be null.\n";
				} else if (CommonTools.isBlank(repository.getName())) {
					validationCheckInfo += " - Repository name should be set.\n";
				}
			}
			if (!StringTools.isEmpty(validationCheckInfo)) {
				AnalysisArtifact analysisArtifact = repositoryViews.get(repositoryView);
				String analysisArtifactInfo = analysisArtifact != null
						? "Check " + AnalysisArtifact.class.getSimpleName() + " " + analysisArtifact + "\n"
						: "";
				validationChecksSummary += analysisArtifactInfo + validationCheckInfo;
			}
		}

		if (!StringTools.isEmpty(validationChecksSummary)) {
			throw new IllegalStateException("Repository view(s) have not passed validation checks:\n" + validationChecksSummary);
		}
	}

	private static void enrichRepositories(List<Repository> repositories, ConfigurationEnrichment configurationEnrichment) {
		RepositorySelectorExpert expert = RepositorySelectors.forDenotation(configurationEnrichment.getSelector());

		for (int i = 0; i < NullSafe.size(repositories); i++) {
			Repository repository = repositories.get(i);
			if (expert.selects(repository)) {
				if (configurationEnrichment instanceof RepositoryEnrichment) {
					Repository enrichingRepo = ((RepositoryEnrichment) configurationEnrichment).getRepository();
					repositories.set(i, merge(enrichingRepo, repository));
				} else if (configurationEnrichment instanceof ArtifactFilterEnrichment) {
					ArtifactFilter artifactFilter = ((ArtifactFilterEnrichment) configurationEnrichment).getArtifactFilter();
					mergeArtifactFilter(artifactFilter, repository);
				} else {
					throw new IllegalStateException("Unsupported configuration enrichment '" + configurationEnrichment.getClass().getSimpleName());
				}
			}
		}
	}

	private static void mergeRepositories(List<Repository> sourceRepositories, List<Repository> targetRepositories) {
		for (Repository sourceRepository : NullSafe.iterable(sourceRepositories)) {
			Repository targetRepository = findRepository(sourceRepository, targetRepositories);
			if (targetRepository == null) {
				targetRepository = (Repository) sourceRepository.entityType().create();
				targetRepositories.add(merge(sourceRepository, targetRepository));
			} else {
				// we need to remove because merge may return a new instance
				targetRepositories.set(targetRepositories.indexOf(targetRepository), merge(sourceRepository, targetRepository));
			}
		}
	}

	static <R extends Repository> R createNewTargetRepository(EntityType<R> type, String name) {
		R result = type.create();
		result.setName(name);
		return result;
	}

	private static Repository findRepository(Repository repository, List<Repository> targetRepositories) {
		for (Repository targetRepository : NullSafe.iterable(targetRepositories)) {
			if (targetRepository.getName().equals(repository.getName())) {
				return targetRepository;
			}
		}
		return null;
	}

	private static void normalizeDisjunctionArtifactFilters(List<Repository> repositories) {
		Map<List<? extends ArtifactFilter>, DisjunctionArtifactFilter> reusableDisjunctionFilters = new LinkedHashMap<>();

		for (Repository repository : repositories) {
			if (repository.getArtifactFilter() instanceof DisjunctionArtifactFilter) {
				DisjunctionArtifactFilter disjunctionFilter = (DisjunctionArtifactFilter) repository.getArtifactFilter();
				if (disjunctionFilter.getOperands().size() == 1) {
					ArtifactFilter artifactFilter = disjunctionFilter.getOperands().get(0);
					repository.setArtifactFilter(artifactFilter);
				} else if (disjunctionFilter.getOperands().isEmpty()) {
					// It's fine that we have no filter. Everything will be matched.
					repository.setArtifactFilter(null);
				} else {
					if (reusableDisjunctionFilters.containsKey(disjunctionFilter.getOperands())) {
						// There already exists another disjunction filter with the same operands. Let's re-use it.
						// (Purpose is to make the yaml representation of the repository configuration easier to read.)
						repository.setArtifactFilter(reusableDisjunctionFilters.get(disjunctionFilter.getOperands()));
					} else {
						// Remember this filter so that we can re-use it in subsequent loop iterations.
						reusableDisjunctionFilters.put(disjunctionFilter.getOperands(), disjunctionFilter);
					}
				}
			}
		}
	}

	private static void wrapFiltersWithStandardDevelopmentViewArtifactFilters(List<Repository> repositories) {
		Map<ArtifactFilter, StandardDevelopmentViewArtifactFilter> reusableStandardDevelopmentViewArtifactFilters = new LinkedHashMap<>();
		for (Repository repository : repositories) {
			if (repository.getArtifactFilter() != null) {
				if (reusableStandardDevelopmentViewArtifactFilters.containsKey(repository.getArtifactFilter())) {
					// There already exists another development view filter with the same delegate. Let's re-use it.
					// (Purpose is to make the yaml representation of the repository configuration easier to read.)
					repository.setArtifactFilter(reusableStandardDevelopmentViewArtifactFilters.get(repository.getArtifactFilter()));
				} else {
					StandardDevelopmentViewArtifactFilter filter = StandardDevelopmentViewArtifactFilter.T.create();
					filter.setRestrictionFilter(repository.getArtifactFilter());
					repository.setArtifactFilter(filter);

					// Remember this filter so that we can re-use it in subsequent loop iterations.
					reusableStandardDevelopmentViewArtifactFilters.put(filter.getRestrictionFilter(), filter);
				}
			}
		}
	}

	static <S extends Repository, T extends Repository> S merge(S sourceRepository, T targetRepository) {

		final S mergedRepository;
		if (sourceRepository.entityType().isAssignableFrom(targetRepository.entityType())) {
			mergedRepository = (S) targetRepository;
		} else {
			mergedRepository = (S) sourceRepository.entityType().create();
			for (Property property : targetRepository.entityType().getProperties()) {
				Object propertyValue = property.get(targetRepository);
				if (!property.isAbsent(targetRepository) && mergedRepository.entityType().getProperties().contains(property)) {
					property.set(mergedRepository, propertyValue);
				}
			}
		}

		for (Property property : sourceRepository.entityType().getProperties()) {
			if (property.getDeclaringType() == GenericEntity.T) {
				continue; // Won't process GenericEntity properties (like globalId, partition)
			}
			Object propertyValue = property.get(sourceRepository);
			// Won't process absent properties
			if (!property.isAbsent(sourceRepository) && mergedRepository.entityType().getProperties().contains(property)) {
				if (property.getName().equals(Repository.artifactFilter)) {
					mergeArtifactFilter((ArtifactFilter) propertyValue, mergedRepository);
				} else {
					property.set(mergedRepository, propertyValue);
				}
			}
		}
		return mergedRepository;
	}

	private static void mergeArtifactFilter(ArtifactFilter artifactFilter, Repository targetRepository) {

		if (artifactFilter == null) {
			return;
		}

		if (targetRepository.getArtifactFilter() == null) {
			targetRepository.setArtifactFilter(artifactFilter);
			return;
		}

		final DisjunctionArtifactFilter disjunctionFilter;
		if (targetRepository.getArtifactFilter() instanceof DisjunctionArtifactFilter) {
			disjunctionFilter = (DisjunctionArtifactFilter) targetRepository.getArtifactFilter();
		} else {
			disjunctionFilter = DisjunctionArtifactFilter.T.create();
			disjunctionFilter.getOperands().add(targetRepository.getArtifactFilter());
			targetRepository.setArtifactFilter(disjunctionFilter);
		}

		if (artifactFilter instanceof DisjunctionArtifactFilter) {
			disjunctionFilter.getOperands().addAll(((DisjunctionArtifactFilter) artifactFilter).getOperands());
		} else {
			disjunctionFilter.getOperands().add(artifactFilter);
		}
	}
}
