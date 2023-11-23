// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.wire.artifact.space;

import com.braintribe.build.cmd.assets.wire.artifact.contract.ArtifactResolutionContract;
import com.braintribe.devrock.mc.api.classpath.ClasspathDependencyResolver;
import com.braintribe.devrock.mc.api.js.JsLibraryLinker;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.mc.api.resolver.ArtifactResolver;
import com.braintribe.devrock.mc.api.resolver.DependencyResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.devrock.mc.core.wirings.classpath.contract.ClasspathResolverContract;
import com.braintribe.devrock.mc.core.wirings.js.contract.JsResolverContract;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.asset.resolving.ng.api.AssetDependencyResolver;
import tribefire.cortex.asset.resolving.ng.wire.contract.AssetResolverContract;

/**
 * @author peter.gazdik
 */
@Managed
public class ArtifactResolutionSpace implements ArtifactResolutionContract {

	@Import
	private ClasspathResolverContract classpathResolver;

	@Import
	private TransitiveResolverContract transitiveResolver;

	@Import
	private ArtifactDataResolverContract artifactDataResolver;

	@Import
	private JsResolverContract jsResolver;

	@Import
	private AssetResolverContract assetResolver;

	@Override
	public ClasspathDependencyResolver classpathResolver() {
		return classpathResolver.classpathResolver();
	}

	@Override
	public TransitiveDependencyResolver transitiveDependencyResolver() {
		return transitiveResolver.transitiveDependencyResolver();
	}

	@Override
	public DependencyResolver dependencyResolver() {
		return artifactDataResolver.dependencyResolver();
	}

	@Override
	public ArtifactResolver dataResolver() {
		return artifactDataResolver.artifactResolver();
	}

	@Override
	public JsLibraryLinker jsLibraryLinker() {
		return jsResolver.jsLibraryLinker();
	}

	@Override
	public AssetDependencyResolver assetDependencyResolver() {
		return assetResolver.assetDependencyResolver();
	}

	@Override
	public RepositoryReflection repositoryReflection() {
		return transitiveResolver.dataResolverContract().repositoryReflection();
	}

}
