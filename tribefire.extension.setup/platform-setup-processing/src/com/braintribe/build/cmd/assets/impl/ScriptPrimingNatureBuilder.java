// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;

import com.braintribe.build.cmd.assets.PlatformAssetDistributionConstants;
import com.braintribe.build.cmd.assets.api.PlatformAssetBuilderContext;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.asset.natures.ScriptPriming;
import com.braintribe.model.csa.ScriptInitializer;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.FileSystemSource;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.StringTools;

public class ScriptPrimingNatureBuilder extends AbstractStorageBuilder<ScriptPriming> {

	private static final String GROOVY_EXTENSION = ".groovy";
	private static final String MODEL_SCRIPT_PART_TYPE = "model:groovy";
	private static final String DATA_SCRIPT_PART_TYPE = "data:groovy";

	@Override
	public void transfer(PlatformAssetBuilderContext<ScriptPriming> context, StorageConfigurationCollector storageCollector) {
		// create initializer for CSA
		ScriptPriming scriptPriming = context.getNature();
		String stageName = context.getAsset().qualifiedAssetName();
		ScriptInitializer scriptInitializer = ScriptInitializer.T.create();
		
		scriptInitializer.setName(stageName);
		
		scriptPriming.effectiveAccessIds().forEach(a -> storageCollector.appendStage(a, scriptInitializer));

		// copy script resources to CSA
		handleResource(context, "model", MODEL_SCRIPT_PART_TYPE, (p, r) -> p.setModelScript(r));
		handleResource(context, "data", DATA_SCRIPT_PART_TYPE, (p, r) -> p.setDataScript(r));
	}

	private void handleResource(PlatformAssetBuilderContext<ScriptPriming> context, String classifier, String partType,
			BiConsumer<ScriptPriming, Resource> assigner) {
		ScriptPriming scriptPriming = context.getNature();
		File modelScriptFile = context.findPartFile(partType).orElse(null);
		
		if (modelScriptFile == null)
			return;
		
		String stageName = context.getAsset().qualifiedAssetName();
		String scriptFileNameBase = FileTools.replaceIllegalCharactersInFileName(stageName, "_");

		try {
			scriptPriming.effectiveAccessIds().forEach(curAccessId -> {
				String stageFolderRelativePath = context.storageAccessDataStageFolder(curAccessId, stageName, true).toFilePath();
				context.copyPartFileOptional(partType, stageFolderRelativePath, name -> classifier + GROOVY_EXTENSION);
			});
			
			String resourceName = scriptFileNameBase + "." + classifier + GROOVY_EXTENSION;
			
			ManagedGmSession session = context.platformAssetStorageRecording().session();
			
			FileSystemSource source = session.create(FileSystemSource.T);
			source.setPath(resourceName);
			source.setGlobalId("script-priming-source:" + scriptFileNameBase);
			
			Resource resource = session.create(Resource.T);
			resource.setGlobalId("script-priming-resource:" + scriptFileNameBase);
			resource.setMimeType("application/x-groovy");
			resource.setFileSize(modelScriptFile.length());
			resource.setCreated(new Date());
			resource.setMd5(StringTools.toHex(FileTools.getMD5CheckSum(modelScriptFile)));
			resource.setName(resourceName);
			resource.setResourceSource(source);
			
			assigner.accept(scriptPriming, resource);
			
			File targetResourceFile = context.registerPackageFile(context.storageAccessFolder(PlatformAssetDistributionConstants.ACCESS_ID_SETUP, true).push("resources").push(resourceName));
			FileTools.copyFile(modelScriptFile, targetResourceFile);
		} catch (IOException e) {
			throw Exceptions.unchecked(e, "Error while creating script resource in setup access storage");
		}
	}
	
	@Override
	public List<String> relevantParts() {
		return Arrays.asList(MODEL_SCRIPT_PART_TYPE, DATA_SCRIPT_PART_TYPE);
	}
}
