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
package tribefire.extension.spreadsheet.processing.importing.common;

public class IdentificationNullException extends Exception {

	private static final long serialVersionUID = 5170869083572080429L;

	public IdentificationNullException() {
		// intentionally left empty
	}

	public IdentificationNullException(String message) {
		super(message);
	}

	public IdentificationNullException(Throwable cause) {
		super(cause);
	}

	public IdentificationNullException(String message, Throwable cause) {
		super(message, cause);
	}

}
