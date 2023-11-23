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
package com.braintribe.wire.impl.module;

import java.util.LinkedHashSet;

import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.impl.util.Exceptions;

public abstract class TransitiveModuleConfigurer {
	public static void configure(WireContextBuilder<?> contextBuilder, WireModule primaryModule, WireModule... modules) {
		LinkedHashSet<WireModule> collectedModules = new LinkedHashSet<>();
		
		collect(contextBuilder, primaryModule, collectedModules);

		for (WireModule module: modules)
			collect(contextBuilder, module, collectedModules);
		
		for (WireModule currentModule: collectedModules) {
			contextBuilder.registerModule(currentModule);
			currentModule.configureContext(contextBuilder);
		}
	}
	
	public static void configure(WireContextBuilder<?> contextBuilder, WireModule module) {
		LinkedHashSet<WireModule> modules = new LinkedHashSet<>();
		
		collect(contextBuilder, module, modules);
		
		for (WireModule currentModule: modules) {
			contextBuilder.registerModule(currentModule);
			currentModule.configureContext(contextBuilder);
		}
	}

	private static void collect(WireContextBuilder<?> contextBuilder, WireModule module, LinkedHashSet<WireModule> modules) {
		if (modules.contains(module))
			return;
		
		for (WireModule moduleDependency: module.dependencies()) {
			try {
				collect(contextBuilder, moduleDependency, modules);
			} catch(Throwable e) {
				String depName = ""+moduleDependency;
				if (moduleDependency != null) {
					depName = depName + "(" + moduleDependency.getClass().getName() + ")";
				}
				String moduleName = ""+module + "(" + module.getClass().getName() + ")";
				throw Exceptions.unchecked(e, "Error while collecting module dependency "+depName+" of module "+moduleName);
			}
		}
		
		modules.add(module);
	}
	
}
