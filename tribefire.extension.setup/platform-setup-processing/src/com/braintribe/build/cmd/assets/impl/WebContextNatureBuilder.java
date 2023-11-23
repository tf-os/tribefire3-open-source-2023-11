// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl;

import com.braintribe.build.cmd.assets.api.PlatformAssetBuilderContext;
import com.braintribe.model.asset.natures.WebContext;

public class WebContextNatureBuilder implements WebContextTransfer<WebContext> {

	@Override
	public void transfer(PlatformAssetBuilderContext<WebContext> context) {
		transferWarPart(context);
	}
	
}
