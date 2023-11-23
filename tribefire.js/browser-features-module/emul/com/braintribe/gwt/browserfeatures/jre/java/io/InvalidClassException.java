// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package java.io;

import java.io.ObjectStreamException;

public class InvalidClassException extends ObjectStreamException {

	private static final long serialVersionUID = -4333316296251054416L;

	/**
	 * Name of the invalid class.
	 *
	 * @serial Name of the invalid class.
	 */
	public String classname;

	/**
	 * Report an InvalidClassException for the reason specified.
	 *
	 * @param reason
	 *            String describing the reason for the exception.
	 */
	public InvalidClassException(String reason) {
		super(reason);
	}

	/**
	 * Constructs an InvalidClassException object.
	 *
	 * @param cname
	 *            a String naming the invalid class.
	 * @param reason
	 *            a String describing the reason for the exception.
	 */
	public InvalidClassException(String cname, String reason) {
		super(reason);
		classname = cname;
	}

	/**
	 * Produce the message and include the classname, if present.
	 */
	public String getMessage() {
		if (classname == null)
			return super.getMessage();
		else
			return classname + "; " + super.getMessage();
	}
}