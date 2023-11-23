// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2019 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl;

import com.braintribe.model.asset.PlatformAsset;

/**
 * This is used to detect multiple configurations of TF services (as tribefire-services and tribefire-container)
 * artifacts.
 * 
 * @author peter.gazdik
 */
public class TribefireServicesRegistry {

	private PlatformAsset asset;

	public void onTfsEncountered(PlatformAsset platformAsset) {
		if (asset == null)
			asset = platformAsset;
		else
			throw new RuntimeException("Invalid setup. There are two different tribefire-services assets in your setup. FIRST: "
					+ asset.qualifiedAssetName() + ", SECOND: " + platformAsset.qualifiedAssetName());
	}

}
