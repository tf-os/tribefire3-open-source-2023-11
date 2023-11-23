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
package com.braintribe.devrock.artifactcontainer.ui.ant;

@SuppressWarnings("serial")
public class AntRunException extends Exception {

	public AntRunException() {
		super();		
	}

	public AntRunException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public AntRunException(String arg0) {
		super(arg0);
	}

	public AntRunException(Throwable arg0) {
		super(arg0);
	}

}
