package com.braintribe.build.cmd.assets.impl;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.braintribe.build.cmd.assets.api.PlatformAssetBuilderContext;
import com.braintribe.build.cmd.assets.api.PlatformAssetNatureBuilder;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.MarkdownDocumentationConfig;

public class MarkdownDocumentationConfigNatureBuilder implements PlatformAssetNatureBuilder<MarkdownDocumentationConfig> {

	private static final String PART_RES_ZIP = "resources:zip";

	@Override
	public void transfer(PlatformAssetBuilderContext<MarkdownDocumentationConfig> context) {
		MarkdownDocumentionCompiler collector = context.getCollector(MarkdownDocumentionCompiler.class, MarkdownDocumentionCompiler::new);
		PlatformAsset asset = context.getAsset();

		File file = context.findPartFile(PART_RES_ZIP) //
				.orElseThrow(() -> new IllegalStateException(
						"Could not find expected '" + PART_RES_ZIP + "' file in asset " + asset.qualifiedRevisionedAssetName()));

		collector.addResources(file);
	}

	@Override
	public List<String> relevantParts() {
		return Arrays.asList(PART_RES_ZIP);
	}

}
