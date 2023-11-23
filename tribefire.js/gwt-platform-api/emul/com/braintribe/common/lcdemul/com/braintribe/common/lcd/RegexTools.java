// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.common.lcd;

import java.util.regex.Pattern;

/**
 * This class is emulated in GWT.
 * 
 * @author peter.gazdik
 */
public class RegexTools {

	/** @see http://stackoverflow.com/questions/2593637/how-to-escape-regular-expression-in-javascript */ 
	public static native String quote(String s) /*-{
		return (s+'').replace(/[.?*+^$[\]\\(){}|-]/g, "\\$&");
	}-*/;

}
