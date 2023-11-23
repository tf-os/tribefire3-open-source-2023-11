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
package com.braintribe.model.processing.itw.synthesis.java.jar;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.model.processing.itw.asm.DebugInfoProvider;

/**
 * 
 */
public class SimpleDebugInfoProvider implements DebugInfoProvider {

	private final Map<String, EntityDebugInfo> entityDebugInfos;

	public SimpleDebugInfoProvider(Map<String, String> entitySources) {
		entityDebugInfos = new HashMap<String, EntityDebugInfo>();

		for (Entry<String, String> entry: entitySources.entrySet()) {
			String typeSignature = entry.getKey();
			String source = entry.getValue();

			entityDebugInfos.put(typeSignature, new EntityDebugInfo(source));
		}
	}

	@Override
	public boolean hasInfoFor(String typeSignature) {
		return entityDebugInfos.containsKey(typeSignature);
	}

	@Override
	public Integer getMethodLine(String className, String methodName) {
		EntityDebugInfo info = entityDebugInfos.get(className);
		return info != null ? info.getMethodLine(methodName) : null;
	}

	@Override
	public String getSetterParameterName(String className, String setterName) {
		return entityDebugInfos.get(className).getSetterParameterName(setterName);
	}

}
