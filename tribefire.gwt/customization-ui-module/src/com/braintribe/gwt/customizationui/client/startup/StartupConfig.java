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

import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Loader;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;


public class StartupConfig {
	private Supplier<?> customizationProvider;
	private Supplier<? extends Loader<?>> preparingLoaderProvider;
	private Supplier<Boolean> canRunProvider;
	
	public Supplier<?> getCustomizationProvider() {
		return customizationProvider;
	}
	
	@Configurable
	public void setCanRunProvider(Supplier<Boolean> canRunProvider) {
		this.canRunProvider = canRunProvider;
	}
	
	@Configurable @Required
	public void setCustomizationProvider(Supplier<?> customizationProvider) {
		this.customizationProvider = customizationProvider;
	}
	
	@Configurable
	public void setPreparingLoaderProvider(Supplier<? extends Loader<?>> preparingLoaderProvider) {
		this.preparingLoaderProvider = preparingLoaderProvider;
	}

	public Supplier<? extends Loader<?>> getPreparingLoaderProvider() {
		return preparingLoaderProvider;
	}
	
	public boolean canRun() throws RuntimeException {
		if (canRunProvider != null)
			return canRunProvider.get();
		else
			return true;
	}
}
