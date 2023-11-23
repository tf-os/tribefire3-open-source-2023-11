// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.api;

import java.util.List;

import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

/**
 * 
 * Used to provide a session to host {@link PlatformAsset} instances originating from the dependency resolution. Also
 * used to record their lifecycle as manipulations. The resulting data/manipulations are used to:
 * <ul>
 * <li>data: Used to create an assets.json file which gives an overview on the assets.</li>
 * <li>manipulations: Used to prime the setup access.</li>
 * </ul>
 */
public interface PlatformAssetStorageRecording {
	ManagedGmSession session();
	List<Manipulation> manipulations();
}
