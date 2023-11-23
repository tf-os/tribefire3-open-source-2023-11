// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules.impl.base;

import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.TribefireModule;

/**
 * @author peter.gazdik
 */
public class TfsTestEntityBuilder {

	public static final String GROUP_ID = "test";

	public static PlatformAsset moduleAsset(String name) {
		PlatformAsset result = platformAsset(name);
		result.setNature(TribefireModule.T.create());

		return result;

	}

	public static PlatformAsset platformAsset(String name) {
		VersionedArtifactIdentification ai = VersionedArtifactIdentification.parse(name);

		PlatformAsset result = PlatformAsset.T.create();
		result.setGroupId(ai.getGroupId());
		result.setName(ai.getArtifactId());
		result.setVersion(ai.getVersion());

		return result;
	}

}
