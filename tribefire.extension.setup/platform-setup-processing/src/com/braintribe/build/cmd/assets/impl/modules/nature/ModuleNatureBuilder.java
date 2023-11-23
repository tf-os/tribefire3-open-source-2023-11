// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules.nature;

import static com.braintribe.build.cmd.assets.impl.modules.ModuleCollector.RESOURCES_ZIP;
import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.io.File;
import java.util.List;
import java.util.Optional;

import com.braintribe.build.cmd.assets.api.PlatformAssetBuilderContext;
import com.braintribe.build.cmd.assets.impl.AbstractStorageBuilder;
import com.braintribe.build.cmd.assets.impl.StorageConfigurationCollector;
import com.braintribe.build.cmd.assets.impl.modules.ModuleCollector;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.TribefireModule;
import com.braintribe.model.csa.ModuleInitializer;

public class ModuleNatureBuilder extends AbstractStorageBuilder<TribefireModule> {

	@Override
	protected void transfer(PlatformAssetBuilderContext<TribefireModule> context, StorageConfigurationCollector storageCollector) {
		PlatformAsset asset = context.getAsset();
		Optional<File> resourcesPart = context.findPartFile(RESOURCES_ZIP);

		// Append module stage for relevant CSAs
		ModuleInitializer moduleInitializer = newModuleInitializer(asset);
		context.getNature().effectiveAccessIds().forEach( //
				accessId -> storageCollector.appendStage(accessId, moduleInitializer));

		// Register module on collector for "modules" folder preparation
		ModuleCollector moduleCollector = context.getCollector(ModuleCollector.class, ModuleCollector::new);
		moduleCollector.addModuleAsset(asset, resourcesPart);
	}

	private ModuleInitializer newModuleInitializer(PlatformAsset asset) {
		ModuleInitializer result = ModuleInitializer.T.create();
		result.setName(asset.getName());
		result.setModuleId("module://" + asset.getGroupId() + ":" + asset.getName());

		// TODO: remove the ModuleInitializer.redirectedsAccessId because redirected module primings should not use bind with an explicit accessId anyway

		return result;
	}

	@Override
	public List<String> relevantParts() {
		return asList(RESOURCES_ZIP);
	}
}