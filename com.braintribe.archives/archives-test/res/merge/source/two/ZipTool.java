// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.utils.zip;

import com.braintribe.utils.zip.impl.ZipContextEntryImpl;
import com.braintribe.utils.zip.impl.ZipContextImpl;

/**
 * the starting point of the voyage into the land of ZIP
 * @author pit
 *
 */
public class ZipTool {
	
	/**
	 * the starting point: creates a new {@link ZipContext} and returns it
	 * @return - the created {@link ZipContext}
	 */
	public static ZipContext zip() {
		return new ZipContextImpl();
	}
	
	/**
	 * create an empty {@link ZipContextEntry} and return it
	 * @return - the created {@link ZipContextEntry}
	 */
	public static ZipContextEntry entry() {
		return new ZipContextEntryImpl();
	}
	
}
