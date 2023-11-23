// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.api;

import com.braintribe.devrock.mc.api.classpath.ClasspathDependencyResolver;
import com.braintribe.devrock.mc.api.js.JsLibraryLinker;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.ArtifactResolver;
import com.braintribe.devrock.mc.api.resolver.DependencyResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;

import tribefire.cortex.asset.resolving.ng.api.AssetDependencyResolver;

/**
 * @author peter.gazdik
 */
public interface ArtifactResolutionContext {

	ClasspathDependencyResolver classpathResolver();

	TransitiveDependencyResolver transitiveDependencyResolver();

	DependencyResolver dependencyResolver();

	ArtifactResolver dataResolver();

	JsLibraryLinker jsLibraryLinker();

	AssetDependencyResolver assetDependencyResolver();

	RepositoryReflection repositoryReflection();

	default CompiledArtifactIdentification resolveArtifactIdentification(String artifactName) {
		CompiledDependencyIdentification cdi = CompiledDependencyIdentification.parseAndRangify(artifactName);
		return dependencyResolver().resolveDependency(cdi).get();
	}

	default ArtifactDataResolution requirePart(CompiledArtifactIdentification artifact, PartIdentification part) {
		Maybe<ArtifactDataResolution> partMaybe = dataResolver().resolvePart(artifact, part);
		
		if (partMaybe.isUnsatisfiedBy(NotFound.T))
			throw new IllegalStateException("Part '" + part.asString() + "' not found for artifact: " + artifact.asString());
			
		return partMaybe.get();
	}
}
