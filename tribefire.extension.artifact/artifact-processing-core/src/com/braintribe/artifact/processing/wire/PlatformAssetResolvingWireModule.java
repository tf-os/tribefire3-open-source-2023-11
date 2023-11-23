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
package com.braintribe.artifact.processing.wire;

import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.MavenSettingsContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.NotificationContract;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;

import tribefire.cortex.asset.resolving.wire.contract.PlatformAssetResolvingConfigurationContract;

public class PlatformAssetResolvingWireModule implements WireModule {
	
	private ArtifactProcessingPlatformAssetResolvingConfigurationSpace configurableBuildResolverContract;

	public PlatformAssetResolvingWireModule(ArtifactProcessingPlatformAssetResolvingConfigurationSpace configurableBuildResolverContract) {
		this.configurableBuildResolverContract = configurableBuildResolverContract;

	}

	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {	
		
		WireModule.super.configureContext(contextBuilder);
		contextBuilder.bindContract(PlatformAssetResolvingConfigurationContract.class, configurableBuildResolverContract);
		contextBuilder.bindContract(MavenSettingsContract.class, configurableBuildResolverContract);
		contextBuilder.bindContract(NotificationContract.class, configurableBuildResolverContract);
		
	}

	
}
