// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.common.lcd;

/**
 * {@link #getSeparator() Provides} <code>\n</code> as file separator. This class is used to emulate
 * <code>com.braintribe.common.lcd.FileSeparatorProvider</code> in GWT (because it contains GWT-incomatible code).
 * 
 * @author michael.lafite
 */
public class FileSeparatorProvider {

	private static final String SEPARATOR = "/";

	/**
	 * Returns <code>/</code>.
	 */
	public static final String getSeparator() {
		return SEPARATOR;
	}

}
