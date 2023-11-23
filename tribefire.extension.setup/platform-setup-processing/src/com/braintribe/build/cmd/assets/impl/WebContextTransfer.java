package com.braintribe.build.cmd.assets.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.build.cmd.assets.PlatformAssetDistributionConstants;
import com.braintribe.build.cmd.assets.api.PlatformAssetBuilderContext;
import com.braintribe.build.cmd.assets.api.PlatformAssetNatureBuilder;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.CoreComponent;
import com.braintribe.model.asset.natures.WebContext;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.paths.PathList;

public interface WebContextTransfer<A extends WebContext> extends PlatformAssetNatureBuilder<A>, PlatformAssetDistributionConstants {
	final String WAR_PART_TYPE = ":war";

	default void transferWarPart(PlatformAssetBuilderContext<? extends WebContext> context) {

		PlatformAsset asset = context.getAsset();

		boolean coreComponent = asset.getNature() instanceof CoreComponent;

		String assetName = coreComponent ? asset.getName() : asset.getGroupId() + '.' + asset.getName();

		PathList webappFolder = context.projectionBaseFolder(true).push(WEBAPP_FOLDER_NAME);
		String webappName = StringTools.splitCamelCase(assetName).stream().map(String::toLowerCase).collect(Collectors.joining("-"));

		context.copyPartFile(WAR_PART_TYPE, webappFolder.toFilePath(), n -> webappName + ".war");
	}

	@Override
	default List<String> relevantParts() {
		return Arrays.asList(WAR_PART_TYPE);
	}
}
