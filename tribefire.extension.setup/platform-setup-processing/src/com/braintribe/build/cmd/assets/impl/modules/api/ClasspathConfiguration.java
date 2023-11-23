// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2019 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules.api;

import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static tribefire.cortex.asset.resolving.ng.impl.ArtifactOutputs.solution;

import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.build.cmd.assets.impl.modules.ModuleSetupHelper;
import com.braintribe.build.cmd.assets.impl.modules.model.ComponentDescriptor;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.setup.tools.GmNature;
import com.braintribe.setup.tools.TfSetupTools;

/**
 * @author peter.gazdik
 */
public class ClasspathConfiguration {

	/**
	 * WTF nobles?<br>
	 * 
	 * I needed a name for artifacts which cannot be inside modules, but must be promoted to the platform classpath. For now it's APIs and models, but
	 * it might be more in the future. So I wanted a simple name for exactly that, and I couldn't come up with anything better than "nobles". If they
	 * can a have noble gases in chemistry, we can handle this here as well, right?
	 */
	public static final Set<GmNature> NOBLE_GM_NATURES = EnumSet.of(GmNature.api, GmNature.model);

	private final Map<AnalysisArtifact, GmNature> gmNatures = new ConcurrentHashMap<>();

	public final TfsContext context;
	public final TfsComponentSetup platformSetup;
	public final List<TfsComponentSetup> moduleSetups;
	public final List<ComponentDescriptor> libraryDescriptors;

	private final Map<PlatformAsset, ComponentDescriptor> assetToDescriptor = new IdentityHashMap<>();

	// for clash-checking
	private final Map<AnalysisArtifact, AnalysisArtifact> platformClasspathNoVersion = newMap(HashComparators.versionlessArtifactIdentification);
	private final Map<ComponentDescriptor, Set<AnalysisArtifact>> nobleDeps;

	public ClasspathConfiguration(TfsContext context, TfsComponentSetup platformSetup, List<TfsComponentSetup> moduleSetups,
			List<ComponentDescriptor> libDescriptors) {
		this.context = context;
		this.platformSetup = platformSetup;
		this.moduleSetups = moduleSetups;
		this.libraryDescriptors = libDescriptors;

		this.nobleDeps = computeNobleDeps();

		initializeClashChecking();
		indexDescriptorsByAsset();
	}

	/** Clash means having different version of the same artifact in different modules. */
	private void initializeClashChecking() {
		for (AnalysisArtifact platformSolution : platformSetup.classpath)
			platformClasspathNoVersion.put(platformSolution, platformSolution);
	}

	private void indexDescriptorsByAsset() {
		indexDescriptorByAsset(platformSetup.componentDescriptor);

		for (TfsComponentSetup moduleSetup : moduleSetups)
			indexDescriptorByAsset(moduleSetup.componentDescriptor);

		for (ComponentDescriptor libDescriptor : libraryDescriptors)
			indexDescriptorByAsset(libDescriptor);
	}

	private void indexDescriptorByAsset(ComponentDescriptor cd) {
		assetToDescriptor.put(cd.asset, cd);
	}

	private Map<ComponentDescriptor, Set<AnalysisArtifact>> computeNobleDeps() {
		return moduleSetups.stream() //
				.map(TfsComponentSetup::getComponentDescriptor) //
				.collect(Collectors.toMap( //
						descriptor -> descriptor, //
						descriptor -> resolveNobleDepsFor(descriptor) //
				));
	}

	private Set<AnalysisArtifact> resolveNobleDepsFor(ComponentDescriptor cd) {
		try {
			return getTransitiveSolutions(cd.assetSolution).stream() //
					.filter(this::isNoble) //
					.flatMap(noble -> getTransitiveSolutions(noble).stream()) //
					.collect(Collectors.toSet());
		} catch (RuntimeException e) {
			throw Exceptions.contextualize(e, "Error while resolving noble deps for: " + cd.assetSolution.asString());
		}
	}

	public Stream<TfsComponentSetup> componentSetups() {
		return Stream.concat(Stream.of(platformSetup), moduleSetups.stream());
	}

	public void removePlatformSolutionsFromModules() {
		for (TfsComponentSetup moduleSetup : moduleSetups) {
			Set<AnalysisArtifact> handledCpItems = TfSetupTools.analysisArtifactSet();

			for (AnalysisArtifact s : newList(moduleSetup.classpath))
				if (platformSetup.classpath.contains(s))
					promoteSolutionAndItsDepsIfPossible(s, moduleSetup, false, handledCpItems);
		}
	}

	public void promoteSolutionAndItsDepsIfPossible(AnalysisArtifact solution, TfsComponentSetup originModuleSetup, boolean required,
			Set<AnalysisArtifact> handledCpItems) {

		Set<AnalysisArtifact> deps = getTransitiveSolutions(solution);

		for (AnalysisArtifact promotionCandidate : deps) {
			if (!originModuleSetup.classpath.contains(promotionCandidate))
				continue; // if the solution was already promoted

			if (!handledCpItems.add(promotionCandidate)) {
				if (wasPromoted(promotionCandidate, originModuleSetup))
					continue;

				handleArtifactWithNonPromotableDep(solution, promotionCandidate, handledCpItems, originModuleSetup);
				return;
			}

			if (isPrivateDep(promotionCandidate, originModuleSetup)) {
				printNoPromotePrivateDep(promotionCandidate, originModuleSetup);
				handleArtifactWithNonPromotableDep(solution, promotionCandidate, handledCpItems, originModuleSetup);
				return;
			}

			AnalysisArtifact forbiddingModule = isForbiddenDep(promotionCandidate, originModuleSetup);
			if (forbiddingModule != null) {
				printNoPromoteForbiddenDep(promotionCandidate, forbiddingModule, originModuleSetup);
				handleArtifactWithNonPromotableDep(solution, promotionCandidate, handledCpItems, originModuleSetup);
				return;
			}

			if (!promoteDepIfNoClash(promotionCandidate, solution, originModuleSetup, required)) {
				// We don't have to print the clash as that is already printed inside
				printNoPromoteDueToDep(solution, promotionCandidate, handledCpItems, originModuleSetup);
				return;
			}
		}
	}

	private void printNoPromoteDueToDep(AnalysisArtifact moduleCpItem, AnalysisArtifact dep, Set<AnalysisArtifact> handledCpItems,
			TfsComponentSetup moduleSetup) {

		if (moduleCpItem == dep || !handledCpItems.add(moduleCpItem))
			return;

		printNoPromoteDueToDep(moduleCpItem, dep, moduleSetup);

	}

	private void handleArtifactWithNonPromotableDep(AnalysisArtifact moduleCpItem, AnalysisArtifact dep, Set<AnalysisArtifact> handledCpItems,
			TfsComponentSetup moduleSetup) {

		if (moduleCpItem == dep || !handledCpItems.add(moduleCpItem))
			return;

		// if something is private (or forbidden), we want to print that as the no-promote reason, even if it cannot be promoted due to a dependency
		if (printIfPrivateOrForbidden(moduleCpItem, moduleSetup))
			return;

		printNoPromoteDueToDep(moduleCpItem, dep, moduleSetup);
	}

	private boolean printIfPrivateOrForbidden(AnalysisArtifact moduleCpItem, TfsComponentSetup moduleSetup) {
		if (isPrivateDep(moduleCpItem, moduleSetup)) {
			printNoPromotePrivateDep(moduleCpItem, moduleSetup);
			return true;
		}

		AnalysisArtifact forbiddingModule = isForbiddenDep(moduleCpItem, moduleSetup);
		if (forbiddingModule != null) {
			printNoPromoteForbiddenDep(moduleCpItem, forbiddingModule, moduleSetup);
			return true;
		}

		return false;
	}

	private void printNoPromotePrivateDep(AnalysisArtifact moduleCpItem, TfsComponentSetup moduleSetup) {
		if (context.verbose)
			println(sequence( //
					text("            Will not promote PRIVATE DEPENDENCY: "), //
					solution(moduleCpItem), //
					text(" MODULE: " + moduleSetup.shortComponentName()) //
			));
	}

	private void printNoPromoteForbiddenDep(AnalysisArtifact moduleCpItem, AnalysisArtifact forbiddingModule, TfsComponentSetup moduleSetup) {
		if (context.verbose)
			println(sequence( //
					text("            Will not promote FORBIDDEN DEPENDENCY: "), //
					solution(moduleCpItem), //
					text(" forbidden by:" + forbiddingModule.getArtifactId()), //
					text(" MODULE: " + moduleSetup.shortComponentName()) //
			));
	}

	private void printNoPromoteDueToDep(AnalysisArtifact moduleCpItem, AnalysisArtifact dep, TfsComponentSetup moduleSetup) {
		if (context.verbose)
			println(sequence( //
					text("            Will not promote '"), //
					solution(moduleCpItem), //
					text("' as it's DEPENDENCY CANNOT BE PROMOTED: "), //
					solution(dep), //
					text(" MODULE: " + moduleSetup.shortComponentName()) //
			));
	}

	/** The dep is a dependency of the depender, or the depender himself. */
	private boolean promoteDepIfNoClash(AnalysisArtifact dependency, AnalysisArtifact depender, TfsComponentSetup originModuleSetup,
			boolean required) {
		AnalysisArtifact clashingPlatformSolution = promoteToPlatformIfNoClashes(dependency, originModuleSetup);

		boolean promoted = clashingPlatformSolution == null;
		if (!promoted && required)
			throwIllegalSetupException(dependency, depender, originModuleSetup, clashingPlatformSolution);

		return promoted;
	}

	/** The pomotionCandidate is a dependency of the noble, or the noble itself. */
	private void throwIllegalSetupException(AnalysisArtifact promotionCandidate, AnalysisArtifact depender, TfsComponentSetup originModuleSetup,
			AnalysisArtifact clashingPlatformSolution) {
		GmNature nobleDepNature = getGmNature(promotionCandidate);

		String msg = String.format(
				"Houston, we have a problem. Cannot prepare this setup :(, problem occured with module '%s' "
						+ " %sartifact '%s' of module(s) '%s' cannot be promoted to the platform classpath, as it conflicts with '%s' from '%s'.",
				originModuleSetup.componentName(), natureIfRelevant(nobleDepNature), promotionCandidate.asString(), origins(promotionCandidate),
				clashingPlatformSolution.asString(), origins(clashingPlatformSolution));

		if (!isNoble(nobleDepNature))
			msg += " This artifact cannot remain in the module, as it is a dependency of " + getGmNature(depender) + " artifact: "
					+ depender.asString();

		throw new IllegalArgumentException(msg);
	}

	private String natureIfRelevant(GmNature nobleDepNature) {
		return isNoble(nobleDepNature) ? nobleDepNature.name() + " " : "";
	}

	private String origins(AnalysisArtifact solution) {
		return context.solutionOracle.origins(solution);
	}

	/** @return <tt>null</tt> if the promotionCandidate was promoted, or the clashing Solution which prohibited the promotion. */
	private AnalysisArtifact promoteToPlatformIfNoClashes(AnalysisArtifact promotionCandidate, TfsComponentSetup originModuleSetup) {
		AnalysisArtifact result = promoteToPlatformIfNoClashes(promotionCandidate, originModuleSetup.componentDescriptor, originModuleSetup);

		if (result == null)
			originModuleSetup.classpath.remove(promotionCandidate);

		return result;
	}

	public AnalysisArtifact promoteToPlatformIfNoClashes(AnalysisArtifact promotionCandidate, ComponentDescriptor originDescriptor) {
		return promoteToPlatformIfNoClashes(promotionCandidate, originDescriptor, null);
	}

	private AnalysisArtifact promoteToPlatformIfNoClashes(AnalysisArtifact promotionCandidate, ComponentDescriptor originDescriptor,
			TfsComponentSetup optionalModuleSetup) {

		AnalysisArtifact matchingPlatformSolution = platformClasspathNoVersion.computeIfAbsent(promotionCandidate, s -> s);
		if (!denoteExactlyTheSameSolution(matchingPlatformSolution, promotionCandidate)) {
			printWillNotPromoteClashingSolution(promotionCandidate, matchingPlatformSolution, originDescriptor);
			return matchingPlatformSolution;
		}

		if (platformSetup.classpath.add(promotionCandidate))
			printWillPromoteSolution(promotionCandidate, originDescriptor);
		else if (optionalModuleSetup != null && !isNobleOrNobleDep(promotionCandidate, optionalModuleSetup))
			printWouldPromoteSolution(promotionCandidate, originDescriptor);

		return null;
	}

	private void printWillPromoteSolution(AnalysisArtifact promotionCandidate, ComponentDescriptor originDescriptor) {
		if (context.verbose)
			println(sequence( //
					text("            Promoting "), //
					solution(promotionCandidate), //
					text(" FROM " + originDescriptor.componentType().name().toUpperCase() + ": " + originDescriptor.assetSolution.getArtifactId()) //
			));
	}

	private void printWouldPromoteSolution(AnalysisArtifact promotionCandidate, ComponentDescriptor originDescriptor) {
		if (context.verbose)
			println(sequence( //
					text("            Solution "), //
					solution(promotionCandidate), //
					text(" is ALREADY IN THE PLATFORM. Removing it FROM " + originDescriptor.componentType().name().toUpperCase() + ": "
							+ originDescriptor.assetSolution.getArtifactId()) //
			));
	}

	private void printWillNotPromoteClashingSolution(AnalysisArtifact candidate, AnalysisArtifact clashingSolution,
			ComponentDescriptor originDescriptor) {
		if (context.verbose)
			println(sequence( //
					text("            Will not promote "), //
					solution(candidate), //
					text(" as the PLATFORM CONTAINS A CONFLICTING VERSION: "), //
					solution(clashingSolution), //
					text(" " + originDescriptor.componentType().name().toUpperCase() + ": " + originDescriptor.assetSolution.getArtifactId()) //
			));
	}

	public boolean isPrivateDep(AnalysisArtifact solution, TfsComponentSetup moduleSetup) {
		if (isNobleOrNobleDep(solution, moduleSetup))
			return false;
		else
			return context.isPrivateDep(moduleSetup.componentDescriptor.asset, solution);
	}

	public AnalysisArtifact isForbiddenDep(AnalysisArtifact solution, TfsComponentSetup moduleSetup) {
		if (isNobleOrNobleDep(solution, moduleSetup))
			return null;

		PlatformAsset pa = context.isForbiddenDep(solution);

		return pa != null ? assetToDescriptor.get(pa).assetSolution : null;
	}

	private boolean denoteExactlyTheSameSolution(AnalysisArtifact s1, AnalysisArtifact s2) {
		return s1 == s2 || HashComparators.versionedArtifactIdentification.compare(s1, s2);
	}

	public Set<AnalysisArtifact> getTransitiveSolutions(AnalysisArtifact root) {
		return context.solutionOracle.getTransitiveSolutions(root);
	}

	public boolean isNobleOrNobleDep(AnalysisArtifact solution, TfsComponentSetup moduleSetup) {
		return nobleDeps.get(moduleSetup.componentDescriptor).contains(solution);
	}

	public boolean isNoble(AnalysisArtifact solution) {
		return TfSetupTools.isPackagedAsJar(solution) && isNoble(getGmNature(solution));
	}

	public boolean isPlatformOnly(AnalysisArtifact solution) {
		return getGmNature(solution) == GmNature.platformOnly;
	}

	private boolean isNoble(GmNature nature) {
		return NOBLE_GM_NATURES.contains(nature);
	}

	private GmNature getGmNature(AnalysisArtifact solution) {
		return gmNatures.computeIfAbsent(solution, this::computeGmNature);
	}

	private GmNature computeGmNature(AnalysisArtifact solution) {
		Optional<GmNature> result = getGmNatureFromMavenProperties(solution);
		if (result.isPresent())
			return result.get();

		String jarFileName = ModuleSetupHelper.findJarLocation_NoClassifier(solution);

		return jarFileName == null ? GmNature.library : GmNature.fromJarFileName(jarFileName);
	}

	private Optional<GmNature> getGmNatureFromMavenProperties(AnalysisArtifact solution) {
		String value = solution.getOrigin().getProperties().get(GmNature.mavenPropertyName);
		if (value == null)
			return Optional.empty();
		else
			return Optional.of(GmNature.valueOfSafe(value));
	}

	public boolean wasPromoted(AnalysisArtifact solution, TfsComponentSetup moduleSetup) {
		return platformSetup.classpath.contains(solution) && !isPrivateDep(solution, moduleSetup);
	}

}
