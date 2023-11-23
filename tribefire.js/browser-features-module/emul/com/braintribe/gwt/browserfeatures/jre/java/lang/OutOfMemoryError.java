// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package java.lang;

public class OutOfMemoryError extends VirtualMachineError {
	private static final long serialVersionUID = -5758232548046306300L;
	
	public OutOfMemoryError() {
		super();
	}

	public OutOfMemoryError(String s) {
		super(s);
	}
}
