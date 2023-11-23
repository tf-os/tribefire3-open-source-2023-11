// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2019 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package java.lang;

/**
 * Empty emulation. Shouldn't be used at all within GWT.
 */
public class Thread implements Runnable {
	
	public static Thread currentThread() {
		return null;
	}
	
	public StackTraceElement[] getStackTrace() {
		return null;
	}
	
	@Override
    public void run() {
    }
	
}