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
package com.braintribe.devrock.mj.ui.dialog.analyzer;

public class AnalyzerException extends Exception {
	private static final long serialVersionUID = -2664177611056306015L;

	public AnalyzerException() {	
	}

	public AnalyzerException(String message) {
		super(message);	
	}

	public AnalyzerException(Throwable cause) {
		super(cause);
	}

	public AnalyzerException(String message, Throwable cause) {
		super(message, cause);
	}
}
