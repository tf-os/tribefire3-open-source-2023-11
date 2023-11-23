// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl;

import com.braintribe.build.cmd.assets.api.PlatformAssetBuilderContext;
import com.braintribe.model.asset.natures.MasterCartridge;

public class MasterCartridgeNatureBuilder implements WebContextTransfer<MasterCartridge> {

	@Override
	public void transfer(PlatformAssetBuilderContext<MasterCartridge> context) {
		TribefireServicesRegistry tfsRegistry = context.getSharedInfo(TribefireServicesRegistry.class, TribefireServicesRegistry::new);
		tfsRegistry.onTfsEncountered(context.getAsset());

		transferWarPart(context);
		
		ContainerProjectionRegistry containerProjectionRegistry = context.getSharedInfo(ContainerProjectionRegistry.class, ContainerProjectionRegistry::new);
		containerProjectionRegistry.registerProjectedAsset(context.getAsset());
	}
	
}
