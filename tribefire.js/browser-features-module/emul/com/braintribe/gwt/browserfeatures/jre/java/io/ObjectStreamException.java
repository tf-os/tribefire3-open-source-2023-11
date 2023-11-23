// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package java.io;

public abstract class ObjectStreamException extends IOException {

	private static final long serialVersionUID = 7260898174833392607L;

	/**
	 * Create an ObjectStreamException with the specified argument.
	 *
	 * @param classname
	 *            the detailed message for the exception
	 */
	protected ObjectStreamException(String classname) {
		super(classname);
	}

	/**
	 * Create an ObjectStreamException.
	 */
	protected ObjectStreamException() {
		super();
	}
}