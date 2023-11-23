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
package com.braintribe.eclipse.model.nature;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.devrock.virtualenvironment.VirtualEnvironmentPlugin;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

public class EclipseVirtualEnvironment implements VirtualEnvironment {
	public static final EclipseVirtualEnvironment INSTANCE = new EclipseVirtualEnvironment(); 

	private static final String missing = new String();
	
	@Override
	public String getProperty(String name) {
		return get(name, VirtualEnvironmentPlugin::getPropertyOverrides, StandardEnvironment.INSTANCE::getProperty);
	}
	
	@Override
	public String getEnv(String name) {
		return get(name, VirtualEnvironmentPlugin::getEnvironmentOverrides, StandardEnvironment.INSTANCE::getEnv);
	}
	
	
	private String get(String name, Supplier<Map<String, String>> overridesSupplier, Function<String, String> fallbackLookup) {
		if (VirtualEnvironmentPlugin.getOverrideActivation()) {
			Map<String, String> overrides = overridesSupplier.get();
			
			String value = overrides.getOrDefault(name, missing);
			
			if (value != missing)
				return value;
		}
		
		return fallbackLookup.apply(name);
	}

}
