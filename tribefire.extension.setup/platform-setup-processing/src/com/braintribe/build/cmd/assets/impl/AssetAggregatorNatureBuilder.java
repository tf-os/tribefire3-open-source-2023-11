// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl;

import java.util.Collections;
import java.util.List;

import com.braintribe.build.cmd.assets.api.PlatformAssetBuilderContext;
import com.braintribe.build.cmd.assets.api.PlatformAssetNatureBuilder;
import com.braintribe.model.asset.natures.AssetAggregator;

public class AssetAggregatorNatureBuilder implements PlatformAssetNatureBuilder<AssetAggregator> {

	@Override
	public void transfer(PlatformAssetBuilderContext<AssetAggregator> context) {
		// noop
	}

	@Override
	public List<String> relevantParts() {
		return Collections.emptyList();
	}


}
