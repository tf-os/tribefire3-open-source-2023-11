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
package com.braintribe.gwt.customizationui.client.startup;

import com.braintribe.provider.Holder;
import com.google.gwt.core.client.GWT;

public class ModuleContextNameProvider extends Holder<String> {
	
	private static final ModuleContextNameProvider instance = new ModuleContextNameProvider();
	
	public static ModuleContextNameProvider getInstance() {
		return instance;
	}
	
	public ModuleContextNameProvider() {
		String baseUrl = GWT.getModuleBaseURL();
		if (baseUrl.endsWith("/"))
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		
		String tokens[] = baseUrl.split("/");
		
		String contextName = tokens[tokens.length - 2];
		accept(contextName);
	}
}
