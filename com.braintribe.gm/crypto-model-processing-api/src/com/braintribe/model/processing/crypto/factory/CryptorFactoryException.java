// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.model.processing.crypto.factory;

public class CryptorFactoryException extends Exception {

	private static final long serialVersionUID = 1L;

	public CryptorFactoryException() {
		super();
	}

	public CryptorFactoryException(String message, Throwable cause) {
		super(message, cause);
	}

	public CryptorFactoryException(String message) {
		super(message);
	}

	public CryptorFactoryException(Throwable cause) {
		super(cause);
	}

	/**
	 * <p>
	 * Builds a {@link CryptorFactoryException} based on the given arguments.
	 * 
	 * @param message
	 *            The message of the resulting {@link CryptorFactoryException}
	 * @param cause
	 *            The cause of the resulting {@link CryptorFactoryException}
	 * @return A {@link CryptorFactoryException} constructed based on the given arguments.
	 */
	public static CryptorFactoryException wrap(String message, Throwable cause) {

		if (cause == null) {
			return new CryptorFactoryException(message);
		}

		if (cause.getMessage() != null && !cause.getMessage().isEmpty()) {
			message += ": " + cause.getMessage();
		}

		return new CryptorFactoryException(message, cause);

	}

}
