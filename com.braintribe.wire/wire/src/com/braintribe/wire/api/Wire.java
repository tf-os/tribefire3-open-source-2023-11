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
package com.braintribe.wire.api;

import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;
import com.braintribe.wire.api.space.WireSpace;
import com.braintribe.wire.impl.context.WireContextBuilderImpl;
import com.braintribe.wire.impl.module.TransitiveModuleConfigurer;

public interface Wire {
	public static <S extends WireSpace> WireContext<S> context(WireTerminalModule<S> module) {		
		return contextBuilder( module).build();
	}

	public static <S extends WireSpace> WireContext<S> context(WireTerminalModule<S> module, WireModule... additionalModules) {		
		return contextBuilder( module, additionalModules).build();
	}
	
	public static <S extends WireSpace> WireContextBuilder<S> context(Class<S> wireSpace) {
		if (!wireSpace.isInterface())
			throw new IllegalArgumentException("only contracts (WireSpace interfaces) can be used as top level space for WireContexts. " + wireSpace + " is a class");
		
		return new WireContextBuilderImpl<>(wireSpace);
	}
	
	/**
	 * Calls {@link #context(Class)} and {@link WireContextBuilder#bindContracts(String)} with the parent package of the
	 * given {@code wireSpace}.
	 */
	public static <S extends WireSpace> WireContextBuilder<S> contextWithStandardContractBinding(Class<S> wireSpace) {
		String packageName = wireSpace.getPackage().getName();
		String basePackage = packageName.substring(0, packageName.lastIndexOf('.'));

		return context(wireSpace).bindContracts(basePackage);

	}
	
	public static <S extends WireSpace> WireContextBuilder<S> contextBuilder(WireTerminalModule<S> module) {
		WireContextBuilder<S> contextBuilder = context(module.contract());
		TransitiveModuleConfigurer.configure(contextBuilder, module);
		return contextBuilder;
	}
	
	public static <S extends WireSpace> WireContextBuilder<S> contextBuilder(WireTerminalModule<S> module, WireModule... additionalModules) {
		WireContextBuilder<S> contextBuilder = context(module.contract());
		TransitiveModuleConfigurer.configure(contextBuilder, module, additionalModules);
		return contextBuilder;
	}
}
