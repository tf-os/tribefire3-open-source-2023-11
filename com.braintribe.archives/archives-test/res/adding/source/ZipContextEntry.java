// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.zip.api;

import java.util.zip.ZipEntry;

public interface ZipContextEntry {
	ZipEntry getZipEntry();
	byte [] getZipEntryPayload();
}
