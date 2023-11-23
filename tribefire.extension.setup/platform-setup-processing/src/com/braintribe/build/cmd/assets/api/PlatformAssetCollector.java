// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.api;

import java.util.Collections;
import java.util.List;

import com.braintribe.model.asset.PlatformAsset;

/**
 * A PlatformAssetCollector is expert that can collect data passed by several {@link PlatformAssetNatureBuilder}. That
 * data is to be written to the {@link PlatformAssetDistributionContext#getPackageBaseDir() package file system} as a
 * whole instead of single files per {@link PlatformAssetNatureBuilder#transfer(PlatformAssetBuilderContext)} call.
 * 
 * @author Dirk Scheffler
 */
public interface PlatformAssetCollector {

	/**
	 * This method gets called for all collectors acquired via
	 * {@link PlatformAssetBuilderContext#getCollector(Class, java.util.function.Supplier)} after the
	 * {@link PlatformAssetNatureBuilder builders} were called per {@link PlatformAsset}.
	 */
	void transfer(PlatformAssetDistributionContext context);

	default List<Class<? extends PlatformAssetCollector>> priorCollectors() {
		return Collections.emptyList();
	}
}
