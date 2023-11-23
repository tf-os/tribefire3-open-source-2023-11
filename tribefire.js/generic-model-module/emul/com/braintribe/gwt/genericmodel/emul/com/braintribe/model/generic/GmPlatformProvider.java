// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.model.generic;

import com.braintribe.gwt.genericmodel.client.GwtGmPlatform;
import com.google.gwt.core.client.GwtScriptOnly;

/**
 * Emulation of GmPlatformProvider from GenericModel artifact 
 */
@GwtScriptOnly
public class GmPlatformProvider {

	private static GmPlatform platform = new GwtGmPlatform();
	
	public static GmPlatform provide() {
		return platform;
	}

}
