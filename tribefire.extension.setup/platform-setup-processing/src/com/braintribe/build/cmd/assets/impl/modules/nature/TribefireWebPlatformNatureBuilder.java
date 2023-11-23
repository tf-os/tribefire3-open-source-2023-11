// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules.nature;

import static com.braintribe.build.cmd.assets.impl.modules.ModuleCollector.CONTEXT_ZIP;
import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.io.File;
import java.util.List;
import java.util.Optional;

import com.braintribe.build.cmd.assets.api.PlatformAssetBuilderContext;
import com.braintribe.build.cmd.assets.impl.ContainerProjectionRegistry;
import com.braintribe.build.cmd.assets.impl.TribefireServicesRegistry;
import com.braintribe.build.cmd.assets.impl.WebContextTransfer;
import com.braintribe.build.cmd.assets.impl.modules.ModuleCollector;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.TribefireWebPlatform;

public class TribefireWebPlatformNatureBuilder implements WebContextTransfer<TribefireWebPlatform> {

	private static final String TRIBEFIRE_SERVICES = "tribefire-services";

	@Override
	public void transfer(PlatformAssetBuilderContext<TribefireWebPlatform> context) {
		TribefireServicesRegistry tfsRegistry = context.getSharedInfo(TribefireServicesRegistry.class, TribefireServicesRegistry::new);
		tfsRegistry.onTfsEncountered(context.getAsset());

		ContainerProjectionRegistry containerProjectionRegistry = context.getSharedInfo(ContainerProjectionRegistry.class, ContainerProjectionRegistry::new);
		containerProjectionRegistry.registerProjectedAsset(context.getAsset());

		PlatformAsset asset = context.getAsset();
		Optional<File> contextPart = context.findPartFile(CONTEXT_ZIP);
		
		ModuleCollector moduleCollector = context.getCollector(ModuleCollector.class, ModuleCollector::new);
		moduleCollector.setTfWebPlatformAsset(asset, contextPart);
		
		// Register the tribefire-service webapps folder as part of this asset
		// This is needed for the container projection
		// Perhaps we need to do this more fine-grained later
		File packageTfsFolder = context.projectionBaseFolder(false).push(WebContextTransfer.WEBAPP_FOLDER_NAME).push(TRIBEFIRE_SERVICES).toFile();
		context.registerPackageFile(packageTfsFolder);
	}
	
	@Override
	public List<String> relevantParts() {
		return asList(CONTEXT_ZIP);
	}
}
