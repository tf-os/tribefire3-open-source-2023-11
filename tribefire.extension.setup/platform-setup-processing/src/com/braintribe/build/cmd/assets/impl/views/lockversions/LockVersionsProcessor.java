// ========================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2020 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission.
// To this file the Braintribe License Agreement applies.
// ========================================================================

package com.braintribe.build.cmd.assets.impl.views.lockversions;

import static com.braintribe.console.ConsoleOutputs.brightWhite;
import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.console.ConsoleOutputs.yellow;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.build.cmd.assets.impl.views.RepositoryViewHelpers;
import com.braintribe.common.NumberAwareStringComparator;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.devrock.model.repository.filters.DisjunctionArtifactFilter;
import com.braintribe.devrock.model.repository.filters.LockArtifactFilter;
import com.braintribe.devrock.model.repositoryview.Release;
import com.braintribe.devrock.model.repositoryview.RepositoryView;
import com.braintribe.devrock.model.repositoryview.enrichments.ArtifactFilterEnrichment;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.platform.setup.api.LockVersions;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * The <code>LockVersionsProcessor</code> retrieves the solutions for all terminal artifacts specified in the
 * {@link LockVersions} request, including all transitive dependencies. It uses this as input for a
 * {@link LockArtifactFilter}. This can then be used to lock the respective artifacts.
 * 
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
public class LockVersionsProcessor {

	private static final Logger logger = Logger.getLogger(LockVersionsProcessor.class);

	// TODO: (also) use CLI console output
	public static void process(LockVersions request, VirtualEnvironment virtualEnvironment) {

		if (request.getWriteLocksOnly() && request.getMarkAsRelease()) {
			throw new RuntimeException("The options '" + LockVersions.writeLocksOnly + "' and '" + LockVersions.markAsRelease
					+ "' cannot be combined, i.e. enable only one of them!");
		}

		LockArtifactFilter filter = resolveSolutionsAndCreateLockFilter(request, virtualEnvironment);

		File targetFile = new File(request.getTargetFile());
		writeFile(filter, targetFile, request.getWriteLocksOnly(), request.getMarkAsRelease());
	}

	static LockArtifactFilter resolveSolutionsAndCreateLockFilter(LockVersions request, VirtualEnvironment virtualEnvironment) {
		MavenConfigurationWireModule veModule = new MavenConfigurationWireModule(virtualEnvironment);

		List<CompiledDependencyIdentification> dependencies = request.getTerminalArtifacts().stream() //
				.map(terminalArtifact -> CompiledDependencyIdentification.parseAndRangify(terminalArtifact, true)) //
				.collect(Collectors.toList());

		try (WireContext<TransitiveResolverContract> wireContext = Wire.context(TransitiveResolverWireModule.INSTANCE, veModule)) {
			logger.info(() -> "Using local repository "
					+ wireContext.contract(RepositoryConfigurationContract.class).repositoryConfiguration().get().getLocalRepositoryPath());

			logger.info("Resolving solutions for:\n" + dependencies.stream().map(d -> d.asString()).collect(Collectors.joining("\n")));

			/* Configure resolution context to include all kinds of dependencies. We respect exclusions though and we filter out
			 * based on scope, see filterDeps(). Note that this is the right way to filter out certain scopes, the
			 * customScopeBuilder() is used for something else (info from PST 2021-08-17). */
			TransitiveResolutionContext trContext = TransitiveResolutionContext.build() //
					.dependencyFilter(LockVersionsProcessor::filterDeps) //
					.includeStandardDependencies(true).includeParentDependencies(true) //
					.includeImportDependencies(true) //
					.includeRelocationDependencies(true) //
					.respectExclusions(true) //
					.lenient(false) //
					.done();

			AnalysisArtifactResolution resolution = wireContext.contract().transitiveDependencyResolver().resolve(trContext, dependencies);
			if (resolution.hasFailed()) {
				throw new RuntimeException("\nResolution has failed with:\n" + resolution.getFailure().stringify());
			}

			List<String> resolvedSolutionStrings = resolution.getSolutions().stream() //
					.sorted(numberAwareSolutionComparator) //
					.map(AnalysisArtifact::asString) //
					.collect(Collectors.toList());

			if (!request.getIncludeAlreadyLockedVersions()) {
				RepositoryConfiguration repositoryConfiguration = wireContext.contract(RepositoryConfigurationContract.class)
						.repositoryConfiguration().get();

				if (repositoryConfiguration == null) {
					logger.info("No repository configuration found! Therefore all artifacts will be locked.");
				} else {
					List<String> existingLocks = getExistingLocks(repositoryConfiguration);
					println(yellow("\nIgnore existing locks:\n" + existingLocks.stream().collect(Collectors.joining("\n"))));
					List<String> existingLocksWithoutVersion = existingLocks.stream().map(lock -> StringTools.getSubstringBefore(lock, "#"))
							.collect(Collectors.toList());
					resolvedSolutionStrings = resolvedSolutionStrings.stream().filter(resolvedSolutionString -> {
						String resolvedSolutionStringWithoutVersion = StringTools.getSubstringBefore(resolvedSolutionString, "#");
						return !existingLocksWithoutVersion.contains(resolvedSolutionStringWithoutVersion);
					}).collect(Collectors.toList());
				}
			}

			String locksWithInvalidVersions = resolvedSolutionStrings.stream() //
					.filter(lock -> StringTools.getSubstringAfterLast(lock, "#").matches(request.getInvalidVersionRegex())) //
					.collect(Collectors.joining("\n"));
			if (!locksWithInvalidVersions.isEmpty()) {
				throw new IllegalArgumentException("Found locks with invalid versions. A version is invalid if it matches the specified regex '"
						+ request.getInvalidVersionRegex() + "') \n" + locksWithInvalidVersions);
			}

			println(sequence(brightWhite("\nSuccessfully found " + resolvedSolutionStrings.size() + " locked versions.\nLocked versions: "),
					text(request.getTargetFile())));

			LockArtifactFilter filter = LockArtifactFilter.T.create();
			filter.getLocks().addAll(resolvedSolutionStrings);

			return filter;

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while processing " + LockVersions.T.getShortName() + " request.", IllegalStateException::new);
		}
	}

	static final Comparator<AnalysisArtifact> numberAwareSolutionComparator = Comparator //
			.comparing(AnalysisArtifact::getGroupId, NumberAwareStringComparator.INSTANCE) //
			.thenComparing(AnalysisArtifact::getArtifactId, NumberAwareStringComparator.INSTANCE) //
			.thenComparing(AnalysisArtifact::getVersion, NumberAwareStringComparator.INSTANCE);

	/**
	 * Filters out <code>test</code>, <code>provided</code> (except for <code>servlet-api</code>) and <code>optional</code>
	 * dependencies.
	 */
	private static boolean filterDeps(AnalysisDependency d) {
		if ("test".equalsIgnoreCase(d.getScope())) {
			return false;
		}
		if ("provided".equalsIgnoreCase(d.getScope())) {
			// we could make this configurable, e.g. via a regex
			if (!d.getArtifactId().equals("javax.servlet-api")) {
				return false;
			}
		}
		if (d.getOptional()) {
			return false;
		}

		// include this dependency
		return true;
	}

	static List<String> getExistingLocks(RepositoryConfiguration repositoryConfiguration) {
		List<String> existingLocks = newList();
		for (Repository repository : repositoryConfiguration.getRepositories()) {
			addExistingLocks(repository.getArtifactFilter(), existingLocks);
		}
		return existingLocks;
	}

	private static void addExistingLocks(ArtifactFilter artifactFilter, List<String> existingLocks) {
		if (artifactFilter instanceof LockArtifactFilter) {
			existingLocks.addAll(((LockArtifactFilter) artifactFilter).getLocks());
		} else if (artifactFilter instanceof DisjunctionArtifactFilter) {
			for (ArtifactFilter operand : ((DisjunctionArtifactFilter) artifactFilter).getOperands()) {
				addExistingLocks(operand, existingLocks);
			}
		}
	}

	private static void writeFile(LockArtifactFilter filter, File file, boolean writeLocksOnly, boolean markAsRelease) {
		GenericEntity entityToWrite;
		if (writeLocksOnly) {
			entityToWrite = filter;
		} else {
			RepositoryView view = RepositoryView.T.create();

			if (markAsRelease) {
				Release release = Release.T.create();
				view.setRelease(release);
				view.setImmutable(true);
			}

			ArtifactFilterEnrichment enrichment = ArtifactFilterEnrichment.T.create();
			enrichment.setArtifactFilter(filter);
			view.getEnrichments().add(enrichment);
			entityToWrite = view;
		}
		RepositoryViewHelpers.writeYamlFile(entityToWrite, file);
	}
}
