package com.braintribe.build.cmd.assets.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.braintribe.build.cmd.assets.PlatformSetupProcessor;
import com.braintribe.console.ConsoleOutputs;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.model.platform.setup.api.GetAssetsFromPackagedPlatformSetup;
import com.braintribe.model.setuppackage.PackagedPlatformSetup;
import com.braintribe.utils.StringTools;

/**
 * Processes {@link GetAssetsFromPackagedPlatformSetup} requests.
 *
 * @author michael.lafite
 */
public class GetAssetsFromPackagedPlatformSetupProcessor {

	public static List<String> process(GetAssetsFromPackagedPlatformSetup request,
			PackagedPlatformSetup packagedPlatformSetup) {

		String nature = request.getNature();
		if (nature == null) {
			nature = PlatformAssetNature.class.getName();
		}

		Set<PlatformAsset> assets = PlatformSetupProcessor.getPlatformAssetsByNature(packagedPlatformSetup, nature);

		List<String> result = new ArrayList<>();

		for (PlatformAsset asset : assets) {
			String assetString = asset.getGroupId() + ":" + asset.getName() + "#" + asset.getVersion();
			if (request.getIncludeRevision()) {
				if(StringTools.isEmpty(asset.getResolvedRevision())) {
					throw new RuntimeException("Missing resolved revision for asset '" + asset.getGlobalId() + "'");
				}
				assetString += "." + asset.getResolvedRevision();
			}
			result.add(assetString);
		}

		Collections.sort(result);

		result.forEach(ConsoleOutputs::println);

		return result;
	}
}
