// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package java.lang;

abstract public class VirtualMachineError extends Error {
	private static final long serialVersionUID = -4133529127026997486L;
	
	public VirtualMachineError() {
		super();
	}

	public VirtualMachineError(String s) {
		super(s);
	}
}
