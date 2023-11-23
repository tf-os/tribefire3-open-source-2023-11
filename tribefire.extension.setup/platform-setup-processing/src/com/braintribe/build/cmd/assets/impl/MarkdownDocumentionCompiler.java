package com.braintribe.build.cmd.assets.impl;

import static com.braintribe.console.ConsoleOutputs.println;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.braintribe.build.cmd.assets.PlatformAssetDistributionConstants;
import com.braintribe.build.cmd.assets.api.PlatformAssetCollector;
import com.braintribe.build.cmd.assets.api.PlatformAssetDistributionContext;
import com.braintribe.console.ConsoleOutputs;
import com.braintribe.doc.JavadocMerger;
import com.braintribe.doc.MarkdownCompiler;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.utils.FileTools;

public class MarkdownDocumentionCompiler implements PlatformAssetCollector, PlatformAssetDistributionConstants {

	private final Map<PlatformAsset, File> toBeCompiled = new LinkedHashMap<>();
	private final List<File> resources = new ArrayList<>();
	private final File sourcesFolder = FileTools.createNewTempDir("mdoc-" + UUID.randomUUID());
	private final File resourcesFolder = new File(sourcesFolder, MarkdownCompiler.MDOC_SUBPATH);

	public MarkdownDocumentionCompiler() {
		resourcesFolder.mkdirs();
	}

	public void addToCompilation(PlatformAsset asset, File file) {
		toBeCompiled.put(asset, file);
	}

	public File getResourcesFolder() {
		return resourcesFolder;
	}

	@Override
	public void transfer(PlatformAssetDistributionContext context) {
		if (toBeCompiled.isEmpty()) {
			println(ConsoleOutputs.yellow(ConsoleOutputs.text("Documentation configuration found but no MarkdownDocumentation assets. Can't compile documentation. Did you maybe forget to disable the noDocu flag?")));
			return;
		}

		for (Map.Entry<PlatformAsset, File> entry : toBeCompiled.entrySet()) {
			PlatformAsset asset = entry.getKey();
			File zipFile = entry.getValue();

			File assetSourcesFolder = new File(sourcesFolder, asset.getGroupId() + "/" + asset.getName());
			FileTools.unzip(zipFile, assetSourcesFolder);
		}

		File documentationProjectionDir = new File(context.getPackageBaseDir(), PROJECTION_NAME_MASTER + "/documentation");
		File mdocDir = new File(documentationProjectionDir, "mdoc");

		Map<String, Object> data = new HashMap<>();
		String jinniVersion = context.getVirtualEnvironment().getEnv(Constants.JINNI_VERSION_PROPERTY_NAME);
		data.put("jinniVersion", jinniVersion);

		JavadocMerger javadocMerger = context.findCollector(JavadocCollector.class) //
				.map(JavadocCollector::getJavadocMerger) //
				.orElse(null);

		MarkdownCompiler.compile(sourcesFolder, mdocDir, toBeCompiled.keySet(), context.doVerboseOutput(), data, javadocMerger);
	}

	@Override
	public List<Class<? extends PlatformAssetCollector>> priorCollectors() {
		return Collections.singletonList(JavadocCollector.class);
	}

	public void addResources(File file) {
		resources.add(file);
		FileTools.unzip(file, resourcesFolder);
	}

}
