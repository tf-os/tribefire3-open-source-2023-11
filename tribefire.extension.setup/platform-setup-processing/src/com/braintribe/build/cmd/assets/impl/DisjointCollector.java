package com.braintribe.build.cmd.assets.impl;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.braintribe.build.cmd.assets.api.PlatformAssetCollector;
import com.braintribe.build.cmd.assets.api.PlatformAssetDistributionContext;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.MasterCartridge;
import com.braintribe.model.asset.natures.WebContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.platform.setup.api.PlatformSetupConfig;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.setuppackage.PackagedPlatformAsset;
import com.braintribe.model.setuppackage.PackagedPlatformAssetsByNature;
import com.braintribe.model.setuppackage.RuntimeContainer;
import com.braintribe.setup.tools.TfSetupTools;

public class DisjointCollector implements PlatformAssetCollector {

	@Override
	public void transfer(PlatformAssetDistributionContext context) {

		PlatformSetupConfig request = context.request();

		if (request.getDisjointProjection()) {

			ContainerProjectionRegistry containerProjectionRegistry = context.findSharedInfo(ContainerProjectionRegistry.class);

			Set<PlatformAsset> excludes = containerProjectionRegistry != null ? containerProjectionRegistry.getProjectedAssets()
					: Collections.emptySet();

			EntityQuery assetQuery = EntityQueryBuilder.from(PlatformAsset.T).done();

			List<PlatformAsset> allAssets = context.platformAssetStorageRecording().session().query().entities(assetQuery).list();

			for (PlatformAsset asset : allAssets) {
				if (!excludes.contains(asset)) {
					projectAsset(asset, context);
				}
			}
		}
	}

	private static void projectAsset(PlatformAsset asset, PlatformAssetDistributionContext context) {
		String projectionFolderName;
		if (asset.getNature() instanceof MasterCartridge || !(asset.getNature() instanceof WebContext))
			return;

		PackagedPlatformSetupBuilder packagedPlatformSetupBuilder = context.getPackagedPlatformSetupBuilder();

		projectionFolderName = TfSetupTools.natureSensitiveAssetName(asset);

		Path targetBasePath = Paths.get(context.getPackageBaseDir().getPath(), projectionFolderName);

		RuntimeContainer runtimeContainer = RuntimeContainer.T.create();
		String runtimeContainerName = getRuntimeContainerName(projectionFolderName, context.request().getShortenRuntimeContainerNames());
		runtimeContainer.setName(runtimeContainerName);
		runtimeContainer.setPathInPackage(projectionFolderName);

		PackagedPlatformAsset packagedAsset = packagedPlatformSetupBuilder.get(asset);

		// register asset in all of its natures
		Stream<EntityType<?>> natureTypes = TfSetupTools.getNatureTypes(asset.getNature().entityType());
		natureTypes.forEach(t -> {
			PackagedPlatformAssetsByNature packagedPlatformAssetsByNature = PackagedPlatformAssetsByNature.T.create();
			packagedPlatformAssetsByNature.setTypeSignature(t.getTypeSignature());
			packagedPlatformAssetsByNature.getAssets().add(packagedAsset);

			runtimeContainer.getAssets().put(t.getTypeSignature(), packagedPlatformAssetsByNature);
		});

		packagedPlatformSetupBuilder.getPackagedPlatformSetup().getContainers().add(runtimeContainer);

		Path reducedPath = ContainerProjections.projectAsset(context, asset, targetBasePath);

		if (reducedPath != null) {
			Path relativeAssetPath = context.getPackageBaseDir().toPath().relativize(reducedPath);
			String pathInPackage = relativeAssetPath.toString().replace(File.separatorChar, '/');
			packagedAsset.setPathInPackage(pathInPackage);
			packagedPlatformSetupBuilder.getMasterContainerIndexer().remove(packagedAsset);
		}
	}

	public static String getRuntimeContainerName(String projectionFolderName, boolean shortenNames) {
		String result = projectionFolderName;

		if (shortenNames) {
			if (result.contains(".")) {
				// tribefire.extension.demo.demo-cartridge --> demo-cartridge
				result = result.substring(result.lastIndexOf(".") + 1);
			}
		}
		return result;
	}

}
