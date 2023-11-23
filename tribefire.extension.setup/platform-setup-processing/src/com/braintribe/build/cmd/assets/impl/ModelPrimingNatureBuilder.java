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

import com.braintribe.build.cmd.assets.api.PlatformAssetBuilderContext;
import com.braintribe.build.cmd.assets.api.PlatformAssetDistributionContext;
import com.braintribe.build.cmd.assets.impl.modules.ModuleCollector;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.ModelPriming;
import com.braintribe.model.csa.ManInitializer;

public class ModelPrimingNatureBuilder extends AbstractStorageBuilder<ModelPriming> {

	
	@Override
	public void transfer(PlatformAssetBuilderContext<ModelPriming> context, StorageConfigurationCollector storageCoalescing) {
		File modelJar = context.findPartFile(":jar").get();
		PlatformAsset asset = context.getAsset();
		String modelName = asset.getGroupId() + ":" + context.getAsset().getName();

		ModelCollector collector = context.getCollector(ModelCollector.class, ModelCollector::new);
		collector.addModelJar(modelName, modelJar, context.getClassifiedSolution());
		
		if (asset.getPlatformProvided())
			return;
		
		
		if (!isModularSetup(context)) {
			ManInitializer modelInitializer = ManInitializer.T.create();
			String name = asset.qualifiedAssetName();
			
			modelInitializer.setName(name);
			storageCoalescing.appendStage("cortex", modelInitializer);
		}
		
		
		ModuleCollector moduleCollector = context.getCollector(ModuleCollector.class, ModuleCollector::new);
		moduleCollector.addModelAsset(asset);
	}
	
	private boolean isModularSetup(PlatformAssetDistributionContext context) {
		return context.getCollector(ModuleCollector.class, ModuleCollector::new).isModularSetup();
	}
	
	@Override
	public List<String> relevantParts() {
		return Arrays.asList(":jar");
	}

}
