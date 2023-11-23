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
package com.braintribe.model.processing.itw.synthesis.java;

import com.braintribe.model.processing.itw.asm.DebugInfoProvider;

/**
 * 
 */
public class EmtpyDebugInfoProvider implements DebugInfoProvider {

	public static final EmtpyDebugInfoProvider INSTANCE = new EmtpyDebugInfoProvider();

	private EmtpyDebugInfoProvider() {
	}
	
	@Override
	public boolean hasInfoFor(String typeSignature) {
		return false;
	}

	@Override
	public Integer getMethodLine(String name, String getterName) {
		return null;
	}

	@Override
	public String getSetterParameterName(String className, String setterName) {
		return null;
	}

	
	
}
