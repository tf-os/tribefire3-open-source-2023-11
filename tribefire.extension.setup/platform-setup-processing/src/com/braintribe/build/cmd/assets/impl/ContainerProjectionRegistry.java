package com.braintribe.build.cmd.assets.impl;

import java.util.HashSet;
import java.util.Set;

import com.braintribe.model.asset.PlatformAsset;

public class ContainerProjectionRegistry {
	private Set<PlatformAsset> projectedAssets = new HashSet<>();
	
	public void registerProjectedAsset(PlatformAsset asset) {
		projectedAssets.add(asset);
	}
	
	public Set<PlatformAsset> getProjectedAssets() {
		return projectedAssets;
	}
}
