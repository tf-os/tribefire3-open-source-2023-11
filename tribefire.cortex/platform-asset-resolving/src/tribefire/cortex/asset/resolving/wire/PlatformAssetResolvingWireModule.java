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
package tribefire.cortex.asset.resolving.wire;

import static com.braintribe.wire.api.util.Lists.list;

import java.util.List;

import com.braintribe.build.artifacts.mc.wire.buildwalk.BuildDependencyResolverWireModule;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.BuildDependencyResolutionContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.FilterConfigurationContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.GeneralConfigurationContract;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.asset.resolving.wire.space.GeneralConfigurationSpace;

public class PlatformAssetResolvingWireModule implements WireTerminalModule<BuildDependencyResolutionContract> {
	private FilterConfigurationContract filterConfigurationContract;
	private WireModule integrationModule;

	public PlatformAssetResolvingWireModule(FilterConfigurationContract filterConfigurationContract, WireModule integrationModule) {
		this.filterConfigurationContract = filterConfigurationContract;
		this.integrationModule = integrationModule;
	}
	
	@Override
	public List<WireModule> dependencies() {
		return list(BuildDependencyResolverWireModule.DEFAULT, integrationModule);
	}
	
	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		WireTerminalModule.super.configureContext(contextBuilder);
		contextBuilder.bindContract(FilterConfigurationContract.class, filterConfigurationContract);
		contextBuilder.bindContract(GeneralConfigurationContract.class, GeneralConfigurationSpace.class);
	}

	@Override
	public Class<BuildDependencyResolutionContract> contract() {
		return BuildDependencyResolutionContract.class;
	}
}
