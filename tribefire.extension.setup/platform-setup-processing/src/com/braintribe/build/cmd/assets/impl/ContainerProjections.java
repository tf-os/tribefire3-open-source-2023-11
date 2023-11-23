package com.braintribe.build.cmd.assets.impl;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.braintribe.build.cmd.assets.PlatformAssetDistributionConstants;
import com.braintribe.build.cmd.assets.api.PlatformAssetDistributionContext;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.utils.FileTools;

public class ContainerProjections {

	static Path projectAsset(PlatformAssetDistributionContext context, PlatformAsset asset, Path targetBasePath) {
		Path sourceBasePath = Paths.get(context.getPackageBaseDir().getAbsolutePath(), PlatformAssetDistributionConstants.PROJECTION_NAME_MASTER);
		
		PathReducer pathReducer = new PathReducer();
		
		context.getAssociatedFiles(asset)
		.map(File::getAbsoluteFile)
		.map(File::toPath) //
		.map(sourceBasePath::relativize) //
		.forEach(p -> {
			Path sourcePath = sourceBasePath.resolve(p);
			Path targetPath = targetBasePath.resolve(p);
			try {
				Files.createDirectories(targetPath.getParent());
				if (sourcePath.toFile().exists() && (!targetPath.toFile().exists() || targetPath.toFile().isFile())) {
					Files.move(sourcePath, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
					pathReducer.process(targetPath);
					
					// clean up empty folders
					Path currentPath = sourcePath.getParent();
					while(!currentPath.equals(sourceBasePath)) {
						if(FileTools.isEmpty(currentPath.toFile())) {
							Files.delete(currentPath);
							currentPath = currentPath.getParent();
						} else {
							break;
						}
					}
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});

		return pathReducer.getReducedPath();
	}
	
}
