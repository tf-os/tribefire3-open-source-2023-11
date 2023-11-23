// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import com.braintribe.build.cmd.assets.api.PlatformAssetBuilderContext;
import com.braintribe.build.cmd.assets.api.PlatformAssetNatureBuilder;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.PlatformAssetDependency;
import com.braintribe.model.asset.natures.ContainerProjection;
import com.braintribe.model.setuppackage.PackagedPlatformAsset;
import com.braintribe.model.setuppackage.RuntimeContainer;

public class ContainerProjectionNatureBuilder implements PlatformAssetNatureBuilder<ContainerProjection> {

	@Override
	public void transfer(PlatformAssetBuilderContext<ContainerProjection> context) {
		ContainerProjection containerProjection = context.getNature();
		
		File packageBaseDir = context.getPackageBaseDir();
		Path targetBasePath = Paths.get(packageBaseDir.getPath(), containerProjection.getContainerName());
		PlatformAsset projectionAsset = context.getAsset();
		
		ContainerProjectionRegistry containerProjectionRegistry = context.getSharedInfo(ContainerProjectionRegistry.class, ContainerProjectionRegistry::new);
		PackagedPlatformSetupBuilder packagedPlatformSetupBuilder = context.getPackagedPlatformSetupBuilder();

		RuntimeContainer runtimeContainer = RuntimeContainer.T.create();
		runtimeContainer.setName(containerProjection.getContainerName());
		runtimeContainer.setProjection(projectionAsset);
		PackagedPlatformAssetIndexer indexer = new PackagedPlatformAssetIndexer(runtimeContainer.getAssets());
		
		projectionAsset.getQualifiedDependencies().stream() //
			.map(PlatformAssetDependency::getAsset)
			.peek(containerProjectionRegistry::registerProjectedAsset) //
			.forEach(a -> {
				Path assetPath = ContainerProjections.projectAsset(context, a, targetBasePath);
				Path relativeAssetPath = packageBaseDir.toPath().relativize(assetPath);
				String pathInPackage = relativeAssetPath.toString().replace(File.separatorChar, '/');
				PackagedPlatformAsset packagedPlatformAsset = packagedPlatformSetupBuilder.get(a);
				packagedPlatformAsset.setPathInPackage(pathInPackage);
				indexer.register(packagedPlatformAsset);
				
				packagedPlatformSetupBuilder.getMasterContainerIndexer().remove(packagedPlatformAsset);
			});
		
		packagedPlatformSetupBuilder.get(projectionAsset).setPathInPackage(containerProjection.getContainerName());
		
		
		packagedPlatformSetupBuilder.getPackagedPlatformSetup().getContainers().add(runtimeContainer);
	}

	@Override
	public List<String> relevantParts() {
		return Collections.emptyList();
	}

}
