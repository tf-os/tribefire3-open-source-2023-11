package com.braintribe.build.cmd.assets.impl;

import java.util.IdentityHashMap;
import java.util.Map;

import com.braintribe.build.cmd.assets.PlatformAssetDistributionConstants;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.setuppackage.CsaStorage;
import com.braintribe.model.setuppackage.PackagedPlatformAsset;
import com.braintribe.model.setuppackage.PackagedPlatformSetup;
import com.braintribe.model.setuppackage.RuntimeContainer;

public class PackagedPlatformSetupBuilder implements PlatformAssetDistributionConstants {
	private PackagedPlatformAssetIndexer totalIndexer;
	private Map<PlatformAsset, PackagedPlatformAsset> packagedAssetByAsset = new IdentityHashMap<>();
	private PackagedPlatformSetup packagedPlatformSetup = PackagedPlatformSetup.T.create();
	private CsaStorage csaStorage = CsaStorage.T.create();
	private PackagedPlatformAssetIndexer csaIndexer;
	private RuntimeContainer masterContainer;
	private PackagedPlatformAssetIndexer masterContainerIndexer;
	
	public PackagedPlatformSetupBuilder() {
		csaIndexer = new PackagedPlatformAssetIndexer(csaStorage.getAssets());
		totalIndexer = new PackagedPlatformAssetIndexer(packagedPlatformSetup.getAssets());
		csaStorage.setPathInPackage(PROJECTION_NAME_MASTER + "/storage");
		masterContainer = RuntimeContainer.T.create();
		masterContainer.setName(PROJECTION_NAME_MASTER);
		masterContainer.setPathInPackage(PROJECTION_NAME_MASTER);
		masterContainer.setStorage(csaStorage);
		masterContainer.setIsMaster(true);
		packagedPlatformSetup.setMasterContainer(masterContainer);
		packagedPlatformSetup.getContainers().add(masterContainer);
		masterContainerIndexer = new PackagedPlatformAssetIndexer(masterContainer.getAssets());
	}

	public void register(PlatformAsset asset) {
		PackagedPlatformAsset packagedAsset = totalIndexer.register(asset);
		packagedAssetByAsset.put(asset, packagedAsset);
	}

	public PackagedPlatformAsset get(PlatformAsset asset) {
		return packagedAssetByAsset.get(asset);
	}
	
	public PackagedPlatformSetup getPackagedPlatformSetup() {
		return packagedPlatformSetup;
	}
	
	public CsaStorage getCsaStorage() {
		return csaStorage;
	}
	
	public PackagedPlatformAssetIndexer getCsaIndexer() {
		return csaIndexer;
	}

	public RuntimeContainer getMasterContainer() {
		return masterContainer;
	}
	
	public PackagedPlatformAssetIndexer getMasterContainerIndexer() {
		return masterContainerIndexer;
	}
}
