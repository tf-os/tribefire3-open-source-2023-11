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
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.PluginPriming;
import com.braintribe.model.csa.PluginInitializer;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.paths.PathList;

public class PluginPrimingNatureBuilder extends AbstractStorageBuilder<PluginPriming> {

	private static final String PLUGIN_ZIP_PART_TYPE = "plugin:zip";
	private static final String PLUGIN_FOLDER_NAME = "plugins";

	@Override
	public void transfer(PlatformAssetBuilderContext<PluginPriming> context, StorageConfigurationCollector storageCollector) {
		PluginPriming javaInitializer = context.getNature();
		String accessId = javaInitializer.getAccessId();
		PlatformAsset asset = context.getAsset();
		
		if (accessId == null) {
			throw new IllegalStateException("No 'accessId' stated in JavaInitializer nature");
		}
		
		String pluginName = asset.qualifiedAssetName();
		String pluginSelector = FileTools.replaceIllegalCharactersInFileName(pluginName, "_");
		
		File pluginPartFile = context.findPartFile(PLUGIN_ZIP_PART_TYPE)
				.orElseThrow(() -> new IllegalStateException("Could not find the plugin .zip part file. Was looking for part type '" + PLUGIN_ZIP_PART_TYPE + "'."));
		
		PathList relativeTargetPath = context.projectionBaseFolder(true).push(PLUGIN_FOLDER_NAME).push(pluginSelector);
		context.unzip(pluginPartFile, relativeTargetPath);
		
		PluginInitializer pluginInitializer = PluginInitializer.T.create();
		pluginInitializer.setSelector(pluginSelector);
		pluginInitializer.setName(pluginName);

		javaInitializer.effectiveAccessIds().forEach(a -> storageCollector.appendStage(a,  pluginInitializer));
	}
	
	@Override
	public List<String> relevantParts() {
		return Arrays.asList(PLUGIN_ZIP_PART_TYPE);
	}
}
