package com.braintribe.build.cmd.assets.impl;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.braintribe.build.cmd.assets.api.PlatformAssetBuilderContext;
import com.braintribe.build.cmd.assets.api.PlatformAssetNatureBuilder;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.MarkdownDocumentation;

public class MarkdownDocumentationNatureBuilder implements PlatformAssetNatureBuilder<MarkdownDocumentation> {

	private static final String PART_MDOC_ZIP = "mdoc:zip";

	@Override
	public void transfer(PlatformAssetBuilderContext<MarkdownDocumentation> context) {

		if (context.request().getNoDocu()) 	// Maybe in this case don't even register this builder?
			return;
		
		MarkdownDocumentionCompiler collector = context.getCollector(MarkdownDocumentionCompiler.class, MarkdownDocumentionCompiler::new);
		PlatformAsset asset = context.getAsset();

		File file = context.findPartFile(PART_MDOC_ZIP) //
				.orElseThrow(() -> new IllegalStateException(
						"Could not find expected '" + PART_MDOC_ZIP + "' file in asset " + asset.qualifiedRevisionedAssetName()));

		collector.addToCompilation(asset, file);

		JavadocCollector javadocCollector = context.getCollector(JavadocCollector.class, JavadocCollector::new);
		javadocCollector.addToCompilation(context.getClassifiedSolution().solution);
	}

	@Override
	public List<String> relevantParts() {
		return Arrays.asList(PART_MDOC_ZIP);
	}

}
