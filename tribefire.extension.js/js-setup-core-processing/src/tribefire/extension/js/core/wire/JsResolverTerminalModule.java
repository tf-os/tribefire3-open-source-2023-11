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
package tribefire.extension.js.core.wire;

import java.util.List;

import com.braintribe.build.artifacts.mc.wire.buildwalk.BuildDependencyResolverWireModule;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.FilterConfigurationContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.GeneralConfigurationContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.IntransitiveResolutionContract;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;
import com.braintribe.wire.api.util.Lists;

import tribefire.extension.js.core.wire.contract.JsResolverConfigurationContract;
import tribefire.extension.js.core.wire.contract.JsResolverContract;
import tribefire.extension.js.core.wire.space.JsFilterConfigurationSpace;
import tribefire.extension.js.core.wire.space.JsGeneralConfigurationSpace;
import tribefire.extension.js.core.wire.space.JsIntransitiveResolutionSpace;
import tribefire.extension.js.core.wire.space.JsResolverConfigurationSpace;

/**
 * the {@link WireTerminalModule} for the {@link JsResolverContract}
 * @author pit
 *
 */
public class JsResolverTerminalModule implements WireTerminalModule<JsResolverContract> {

	private JsResolverConfigurationSpace configuration;

	public JsResolverTerminalModule(JsResolverConfigurationSpace configuration) {
		this.configuration = configuration;	
	}
	
	@Override
	public List<WireModule> dependencies() {
		return Lists.list( BuildDependencyResolverWireModule.DEFAULT);											
	}

	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		WireTerminalModule.super.configureContext(contextBuilder);
		contextBuilder
		// overload ve
		.bindContract(GeneralConfigurationContract.class, JsGeneralConfigurationSpace.class)
		// inject part tuples
		.bindContract(FilterConfigurationContract.class, JsFilterConfigurationSpace.class)
		// overload the standard dependency resolver 
		.bindContract(IntransitiveResolutionContract.class, JsIntransitiveResolutionSpace.class)
		// inject the configuration
		.bindContract( JsResolverConfigurationContract.class, configuration)		
		
		.build();
	}

	
	
	
}
