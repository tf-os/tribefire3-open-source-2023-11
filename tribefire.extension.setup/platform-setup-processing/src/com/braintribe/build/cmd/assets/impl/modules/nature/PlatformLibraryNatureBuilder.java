// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules.nature;

import java.util.Collections;
import java.util.List;

import com.braintribe.build.cmd.assets.api.PlatformAssetBuilderContext;
import com.braintribe.build.cmd.assets.api.PlatformAssetNatureBuilder;
import com.braintribe.build.cmd.assets.impl.modules.ModuleCollector;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.PlatformLibrary;

public class PlatformLibraryNatureBuilder implements PlatformAssetNatureBuilder<PlatformLibrary> {

	@Override
	public void transfer(PlatformAssetBuilderContext<PlatformLibrary> context) {
		PlatformAsset asset = context.getAsset();

		ModuleCollector moduleCollector = context.getCollector(ModuleCollector.class, ModuleCollector::new);
		moduleCollector.addLibraryAsset(asset);
	}

	@Override
	public List<String> relevantParts() {
		return Collections.emptyList();
	}

}
