// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules.nature;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.first;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.build.cmd.assets.api.PlatformAssetBuilderContext;
import com.braintribe.build.cmd.assets.impl.AbstractStorageBuilder;
import com.braintribe.build.cmd.assets.impl.StorageConfigurationCollector;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.PlatformAssetDependency;
import com.braintribe.model.asset.natures.DynamicInitializerInput;
import com.braintribe.model.asset.natures.TribefireModule;
import com.braintribe.model.csa.DynamicInitializer;
import com.braintribe.utils.paths.PathList;

public class DynamicInitializerInputNatureBuilder extends AbstractStorageBuilder<DynamicInitializerInput> {

	private static final String RESOURCES_ZIP = "resources:zip";

	@Override
	protected void transfer(PlatformAssetBuilderContext<DynamicInitializerInput> context, StorageConfigurationCollector storageCollector) {
		PlatformAsset asset = context.getAsset();
		DynamicInitializerInput nature = context.getNature();

		PlatformAsset configuringModule = resolveConfiguringModule(asset);

		String stageName = asset.qualifiedAssetName();
		File resourcesZip = context.findPartFile(RESOURCES_ZIP)
				.orElseThrow(() -> new IllegalStateException("resources.zip part not found for asset: " + asset.qualifiedRevisionedAssetName()));

		// Append dynamic stage for relevant CSAs
		DynamicInitializer dynamicInitializer = newDynamicInitializer(stageName, configuringModule);

		nature.effectiveAccessIds().forEach( //
				accessId -> {
					storageCollector.appendStage(accessId, dynamicInitializer);

					PathList stageDir = context.storageAccessDataStageFolder(accessId, stageName, true);
					context.unzip(resourcesZip, stageDir);
				});
	}

	private PlatformAsset resolveConfiguringModule(PlatformAsset asset) {
		List<PlatformAsset> modules = asset.getQualifiedDependencies().stream() //
				.map(PlatformAssetDependency::getAsset) //
				.filter(this::isModule) //
				.collect(Collectors.toList());

		if (modules.size() != 1)
			throw new IllegalArgumentException(DynamicInitializerInput.class.getSimpleName() + " assset '" + asset.qualifiedRevisionedAssetName()
					+ "' has " + modules.size()
					+ " module dependencies. Exactly one module dependency is expected, namely the odule that binds the expert for this input.");

		return first(modules);
	}

	private boolean isModule(PlatformAsset asset) {
		return asset.getNature() instanceof TribefireModule;
	}

	private DynamicInitializer newDynamicInitializer(String stageName, PlatformAsset configuringModule) {
		DynamicInitializer result = DynamicInitializer.T.create();
		result.setName(stageName);
		result.setModuleName(configuringModule.versionlessName());

		return result;
	}

	@Override
	public List<String> relevantParts() {
		return asList(RESOURCES_ZIP);
	}
}