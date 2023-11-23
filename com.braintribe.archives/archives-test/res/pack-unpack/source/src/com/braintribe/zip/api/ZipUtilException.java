// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.zip.api;

/**
 * @author pit
 *
 */
public class ZipUtilException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3195233097116207187L;

	public ZipUtilException() {	
	}

	public ZipUtilException(String message) {
		super(message);	
	}

	public ZipUtilException(Throwable cause) {
		super(cause);	
	}

	public ZipUtilException(String message, Throwable cause) {
		super(message, cause);
	}

}
