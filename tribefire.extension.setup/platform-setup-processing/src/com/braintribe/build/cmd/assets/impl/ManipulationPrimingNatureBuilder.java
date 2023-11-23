// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.braintribe.build.cmd.assets.api.PlatformAssetBuilderContext;
import com.braintribe.model.asset.natures.ManipulationPriming;
import com.braintribe.model.csa.ManInitializer;
import com.braintribe.utils.paths.PathList;

public class ManipulationPrimingNatureBuilder extends AbstractStorageBuilder<ManipulationPriming> {

	@Override
	public void transfer(PlatformAssetBuilderContext<ManipulationPriming> context, StorageConfigurationCollector storageCoalescing) {
		ManipulationPriming manInitializerNature = context.getNature();
		String accessId = manInitializerNature.getAccessId();
		
		if (accessId==null) {
			throw new IllegalStateException("Access id in " + manInitializerNature + " must be set");
		}
		
		String stageName = context.getAsset().qualifiedAssetName();
		ManInitializer manInitializer = ManInitializer.T.create();
		manInitializer.setName(stageName);

		manInitializerNature.effectiveAccessIds().forEach(a -> {
			storageCoalescing.appendStage(a,  manInitializer);	
			String stageFolderRelativePath = context.storageAccessDataStageFolder(a, stageName, true).toFilePath();
			
			context.copyPartFileOptional("model:man", stageFolderRelativePath, name -> "model.man");
			context.copyPartFileOptional("data:man", stageFolderRelativePath, name -> "data.man");
		});
		

		Optional<File> resourcesOptional = context.findPartFile("resources:zip");
		
		if (resourcesOptional.isPresent()) {
			File resourcesZip = resourcesOptional.get();

			manInitializerNature.effectiveAccessIds().forEach(a -> {
				PathList accessResourcesBaseDir = context.storageAccessFolder(a, true).push("resources");
				context.unzip(resourcesZip, accessResourcesBaseDir);
			});
		}
	}

	@Override
	public List<String> relevantParts() {
		return Arrays.asList("model:man", "data:man", "resources:zip");
	}
}
