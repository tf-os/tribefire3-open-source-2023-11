// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules.api;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static java.util.Objects.requireNonNull;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisDependency;

/**
 * @author peter.gazdik
 */
public class TfsSolutionOracle {

	private List<TfsComponentSetup> tfsSetups;

	/** @see #getTransitiveSolutions */
	private final Map<AnalysisArtifact, Set<AnalysisArtifact>> transitiveSolutions = new ConcurrentHashMap<>();

	public void setComponentSetups(TfsComponentSetup tfsPlatformSetup, List<TfsComponentSetup> moduleSetups) {
		tfsSetups = asList(tfsPlatformSetup);
		tfsSetups.addAll(moduleSetups);
	}

	public void purgeTransitiveSolutionsCache() {
		transitiveSolutions.clear();
	}

	public String origins(AnalysisArtifact solution) {
		return getOriginSolutions(solution) //
				.map(AnalysisArtifact::asString) //
				.collect(Collectors.joining(", ", "[", "]"));
	}

	private Stream<AnalysisArtifact> getOriginSolutions(AnalysisArtifact solution) {
		return tfsSetups.stream() //
				.filter(tfsSetup -> tfsSetup.originalClasspath.contains(solution)) //
				.map(tfsSetup -> tfsSetup.componentDescriptor.assetSolution);
	}

	/**
	 * Returns a linked set of all transitive dependencies of given root, including the root itself, in the depth first order, i.e. if you iterate
	 * over it, you always get all the dependencies of a solution before the solution itself. The root is therefore always the last element in this
	 * set.
	 */
	/* Yes, this is the same as what Malaclypse does, but it uses known Solution instances, rather then creating new ones per call. Also, all the
	 * transitiveSolutions are cached. */
	public Set<AnalysisArtifact> getTransitiveSolutions(AnalysisArtifact root) {
		// Recursive, so we cannot use computeIfAbsent!!!
		Set<AnalysisArtifact> result = transitiveSolutions.get(root);
		if (result == null) {
			Set<AnalysisArtifact> newResult = computeTransitiveSolutions(root);
			newResult.add(root);

			result = transitiveSolutions.computeIfAbsent(root, r -> newResult);
		}

		return result;
	}

	private Set<AnalysisArtifact> computeTransitiveSolutions(AnalysisArtifact root) {
		return root.getDependencies().stream() //
				.map(this::getNonNullSolution) //
				// Self-cycle is allowed (with a classifier - not checking, assuming Malaclypse returned reasonable output)
				.filter(solution -> solution != root) //
				.map(this::getTransitiveSolutions) //
				.flatMap(Set::stream) //
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	private AnalysisArtifact getNonNullSolution(AnalysisDependency ad) {
		return requireNonNull(ad.getSolution(), () -> "Solution was null for dependency: " + ad.asString());
	}

}
