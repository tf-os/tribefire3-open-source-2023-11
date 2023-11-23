// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules.api;

import static com.braintribe.utils.lcd.CollectionTools2.index;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.braintribe.build.cmd.assets.api.ArtifactResolutionContext;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.TribefireModule;

/**
 * @author peter.gazdik
 */
public class TfsContext {

	public final PlatformAsset tfPlatformAsset;
	public final List<PlatformAsset> moduleAssets;
	public final List<PlatformAsset> modelAssets;
	public final List<PlatformAsset> libraryAssets;
	public final ArtifactResolutionContext resolutionContext;
	public final TfsClasspathOptimizer classpathOptimizer;
	public final boolean preProcessClasspaths;
	public final boolean verbose;

	public final TfsSolutionOracle solutionOracle = new TfsSolutionOracle();

	public final Map<Pattern, PlatformAsset> forbiddenDepPatterns;
	private final Map<PlatformAsset, List<Pattern>> privateDepPatterns = newMap();

	public TfsContext( //
			PlatformAsset tfPlatformAsset, //
			List<PlatformAsset> moduleAssets, //
			List<PlatformAsset> modelAssets, //
			List<PlatformAsset> libraryAssets, //
			ArtifactResolutionContext resolutionContext, //
			TfsClasspathOptimizer classpathOptimizer, //
			boolean preProcessClasspaths, boolean verbose) {

		this.tfPlatformAsset = tfPlatformAsset;
		this.moduleAssets = moduleAssets;
		this.modelAssets = modelAssets;
		this.libraryAssets = libraryAssets;
		this.resolutionContext = resolutionContext;
		this.classpathOptimizer = classpathOptimizer;
		this.preProcessClasspaths = preProcessClasspaths;
		this.verbose = verbose;

		this.forbiddenDepPatterns = computeForbiddenDepChecking();
	}

	private Map<Pattern, PlatformAsset> computeForbiddenDepChecking() {
		return index(moduleAssets) //
				.byMany(this::forbiddenPatternsOf) //
				.unique();
	}

	private List<Pattern> forbiddenPatternsOf(PlatformAsset moduleAsset) {
		return getModuleNature(moduleAsset).getForbiddenDeps().stream() //
				.map(Pattern::compile) //
				.collect(Collectors.toList());
	}

	public boolean isPrivateDep(PlatformAsset moduleAsset, AnalysisArtifact artifact) {
		List<Pattern> privateDepPatterns = acquirePrivateDepPatternsFor(moduleAsset);
		if (privateDepPatterns.isEmpty())
			return false;

		return findMatchingPattern(artifact, privateDepPatterns) != null;
	}

	public PlatformAsset isForbiddenDep(AnalysisArtifact solution) {
		if (forbiddenDepPatterns.isEmpty())
			return null;

		Pattern p = findMatchingPattern(solution, forbiddenDepPatterns.keySet());
		return p != null  ? forbiddenDepPatterns.get(p) : null;
	}

	private List<Pattern> acquirePrivateDepPatternsFor(PlatformAsset moduleAsset) {
		return privateDepPatterns.computeIfAbsent(moduleAsset, this::resolvePrivateDepPatterns);
	}

	private List<Pattern> resolvePrivateDepPatterns(PlatformAsset moduleAsset) {
		return getModuleNature(moduleAsset).getPrivateDeps().stream() //
				.map(Pattern::compile) //
				.collect(Collectors.toList());
	}

	private Pattern findMatchingPattern(AnalysisArtifact solution, Collection<Pattern> privateDepPatterns) {
		String solutionName = solution.asString();

		for (Pattern pattern : privateDepPatterns)
			if (pattern.matcher(solutionName).matches())
				return pattern;

		return null;
	}


	private TribefireModule getModuleNature(PlatformAsset moduleAsset) {
		return (TribefireModule) moduleAsset.getNature();
	}

}
