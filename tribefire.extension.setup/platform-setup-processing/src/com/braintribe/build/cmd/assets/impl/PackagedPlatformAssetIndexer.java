package com.braintribe.build.cmd.assets.impl;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.setuppackage.PackagedPlatformAsset;
import com.braintribe.model.setuppackage.PackagedPlatformAssetsByNature;
import com.braintribe.setup.tools.TfSetupTools;

public class PackagedPlatformAssetIndexer {
	private Map<String, PackagedPlatformAssetsByNature> assets;
	
	public PackagedPlatformAssetIndexer(Map<String, PackagedPlatformAssetsByNature> assets) {
		this.assets = assets;
	}
	
	public void register(PackagedPlatformAsset packagedPlatformAsset) {
		
		Stream<EntityType<?>> natureTypes = TfSetupTools.getNatureTypes(packagedPlatformAsset.getAsset().getNature().entityType());
		
		natureTypes.forEach(t -> {
			PackagedPlatformAssetsByNature assetsByNature = acquireAssetsByNature(t);
			assetsByNature.getAssets().add(packagedPlatformAsset);
		});
		
	}

	private PackagedPlatformAssetsByNature acquireAssetsByNature(EntityType<?> t) {
		return assets.computeIfAbsent(t.getTypeSignature(), k -> {
			PackagedPlatformAssetsByNature byNature = PackagedPlatformAssetsByNature.T.create();
			byNature.setTypeSignature(k);
			return byNature;
		});
	}
	
	public PackagedPlatformAsset register(PlatformAsset asset) {
		PackagedPlatformAsset packagedAsset = PackagedPlatformAsset.T.create();
		packagedAsset.setAsset(asset);
		packagedAsset.setName(TfSetupTools.natureSensitiveAssetName(asset));
		
		register(packagedAsset);
		
		return packagedAsset;
	}

	public void remove(PackagedPlatformAsset packagedAsset) {
		Stream<EntityType<?>> natureTypes = TfSetupTools.getNatureTypes(packagedAsset.getAsset().getNature().entityType());
		
		natureTypes.forEach(t -> {
			String natureTypeSignature = t.getTypeSignature();
			PackagedPlatformAssetsByNature packagedPlatformAssetsByNature = assets.get(natureTypeSignature);
			Set<PackagedPlatformAsset> packagedAssets = packagedPlatformAssetsByNature.getAssets();
			packagedAssets.remove(packagedAsset);
			
			if (packagedAssets.isEmpty())
				assets.remove(natureTypeSignature);
		});
	}
}
