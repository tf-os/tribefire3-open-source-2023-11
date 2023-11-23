// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.model.processing.index;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.utils.concurrent.ExclusiveSyncObjectProvider;
import com.braintribe.utils.concurrent.SyncObjectProvider;

// ###################################
// ## . . . . . EMMULATION . . . . .##
// ###################################

public class IndexTools {

	public static <K, V> Map<K, V> newCacheMap() {
		return new HashMap<K, V>();
	}

}
