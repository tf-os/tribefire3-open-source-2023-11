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
package com.braintribe.common.attribute.common.impl;

import java.io.File;

import com.braintribe.common.attribute.common.CallerEnvironment;

public class BasicCallerEnvironment implements CallerEnvironment {

	private boolean isLocal;
	private File currentWorkingDirectory;
	
	public BasicCallerEnvironment(boolean isLocal, File currentWorkingDirectory) {
		this.isLocal = isLocal;
		this.currentWorkingDirectory = currentWorkingDirectory;
	}
	
	public void setLocal(boolean isLocal) {
		this.isLocal = isLocal;
	}

	public void setCurrentWorkingDirectory(File currentWorkingDirectory) {
		this.currentWorkingDirectory = currentWorkingDirectory;
	}

	@Override
	public boolean isLocal() {
		return isLocal;
	}

	@Override
	public File currentWorkingDirectory() {
		return currentWorkingDirectory;
	}

}
