// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules.model;

import java.util.List;

import com.braintribe.devrock.mc.api.classpath.ClasspathDependencyResolver;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.ModelPriming;
import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.model.asset.natures.PlatformLibrary;
import com.braintribe.model.asset.natures.TribefireModule;
import com.braintribe.model.asset.natures.TribefirePlatform;

/**
 * Bundles together a {@link PlatformAsset} with it's corresponding {@link AnalysisArtifact} as well as all the transitive solutions. This describes
 * the resolved state of a component, prior to the actual classpath resolution for the entire setup.
 * 
 * @author peter.gazdik
 */
public class ComponentDescriptor {

	public PlatformAsset asset;
	public AnalysisArtifact assetSolution;
	public AnalysisArtifactResolution resolution;
	/**
	 * All the transitive solutions of given asset, including the solution for the asset itself (i.e. the {@link #assetSolution}).
	 * 
	 * @see ClasspathDependencyResolver
	 */
	public List<AnalysisArtifact> transitiveSolutions;

	public ComponentType componentType() {
		if (asset == null)
			throw new IllegalStateException("Asset is null, which is not expected by the time this method is called. This is probably a bug, sorry.");

		PlatformAssetNature nature = asset.getNature();
		if (nature instanceof ModelPriming)
			return ComponentType.Model;
		if (nature instanceof TribefireModule)
			return ComponentType.Module;
		if (nature instanceof PlatformLibrary)
			return ComponentType.Library;
		if (nature instanceof TribefirePlatform)
			return ComponentType.Platform;

		throw new IllegalStateException("Unexpected asset nature: " + nature + " of asset: " + asset);
	}

}
