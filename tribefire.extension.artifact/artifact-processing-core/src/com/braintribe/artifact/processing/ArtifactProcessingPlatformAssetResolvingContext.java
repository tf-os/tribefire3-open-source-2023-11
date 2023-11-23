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
package com.braintribe.artifact.processing;

import java.util.Set;

import com.braintribe.model.artifact.processing.AssetFilterContext;
import com.braintribe.ve.api.VirtualEnvironment;

import tribefire.cortex.asset.resolving.impl.AbstractPlatformAssetResolvingContext;

public class ArtifactProcessingPlatformAssetResolvingContext extends AbstractPlatformAssetResolvingContext {
	private VirtualEnvironment virtualEnvironment;
	private boolean isRuntime;

	private String stage;
	private Set<String> tags;
	
	public ArtifactProcessingPlatformAssetResolvingContext(AssetFilterContext context) {
		if (context != null) {
			isRuntime = context.getRuntime();		
			stage = context.getStage();
			tags = context.getTags();	
		}
	}
	
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}

	@Override
	public VirtualEnvironment getVirtualEnvironment() {	
		return virtualEnvironment;
	}

	@Override
	public boolean isRuntime() {		
		return isRuntime;
	}

	@Override
	public boolean isDesigntime() {
		return !isRuntime;
	}

	@Override
	public String getStage() {
		return stage;
	}

	@Override
	public Set<String> getTags() {
		return tags;
	}

}
