package com.braintribe.build.cmd.assets.api.preprocessing;

import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.preprocessing.AssetPreprocessing;

public interface PlatformAssetPreprocessor<P extends AssetPreprocessing> {
	void process(PlatformAssetPreprocessorContext context, PlatformAsset asset, P preprocessing);
}
