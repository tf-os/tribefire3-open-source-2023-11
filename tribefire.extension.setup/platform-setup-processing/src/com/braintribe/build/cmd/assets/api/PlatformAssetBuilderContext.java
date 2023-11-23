// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.api;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.utils.paths.PathList;

import tribefire.cortex.asset.resolving.ng.impl.PlatformAssetSolution;

public interface PlatformAssetBuilderContext<N extends PlatformAssetNature> extends PlatformAssetDistributionContext {
	PlatformAsset getAsset();
	PlatformAssetSolution getClassifiedSolution();
	
	N getNature();
	
	/**
	 * Use same notation as for {@link PlatformAssetNatureBuilder#relevantParts()} to access the downloaded data as a file.
	 */
	Optional<File> findPartFile(String partType);
	
	/**
	 * Finds a part file by its part type and copies it to a subdirectory of the package base directory
	 * Additionally the copied file is being registered as being transmitted by the current asset associated to this context.
	 * One usecase of that association is the container projection asset handling which would move all files associated to an asset to another subfolder of the package.
	 * 
	 * @param partType As in {@link #findPartFile(String)}.
	 * @param packageTargetSubdir path relative to {@link #getPackageBaseDir()} of the directory where file should be copied to. 
	 * 
	 * @return the successfully copied target {@link File}
	 * 
	 * @throws IllegalStateException when no part file of passed type could be found
	 * @throws RuntimeException when file could not be copied
	 * 
	 */
	default File copyPartFile(String partType, String packageTargetSubdir) {
		return copyPartFile(partType, packageTargetSubdir, Function.identity());
	}
	
	/**
	 * Finds a part file by its part type and copies it to a subdirectory of the package base directory
	 * Additionally the copied file is being registered as being transmitted by the current asset associated to this context.
	 * One usecase of that association is the container projection asset handling which would move all files associated to an asset to another subfolder of the package.
	 * 
	 * @param partType As in {@link #findPartFile(String)}.
	 * @param packageTargetSubdir path relative to {@link #getPackageBaseDir()} of the directory where file should be copied to. 
	 * @param nameTransformer Transforms the given simple part file name to another
	 * 
	 * @return the successfully copied target {@link File}
	 * 
	 * @throws IllegalStateException when no part file of passed type could be found
	 * @throws RuntimeException when file could not be copied
	 * 
	 */
	default File copyPartFile(String partType, String packageTargetSubdir, Function<String, String> nameTransformer) {
		return copyPartFileOptional(partType, packageTargetSubdir, nameTransformer)
				.orElseThrow(() -> new IllegalStateException("Could not find a part file of type '" + partType + "' in asset '" + getAsset().qualifiedAssetName() + "'."));
	}

	/**
	 * Same as {@link #copyPartFile(String, String, Function)} but returning an empty {@link Optional} if the requested partFile doesn't exist.
	 */
	Optional<File> copyPartFileOptional(String partType, String packageTargetSubdir, Function<String, String> nameTransformer);
	
	/**
	 * creates a file object that absolutely points to the according file in the package folder structure without actually creating the file. 
	 * Additionally the resulting file is being registered as being transmitted by the current asset associated to this context.
	 * One usecase of that association is the container projection asset handling which would move all files associated to an asset to another subfolder of the package.
	 * 
	 * @return the file that is now associated to the current asset and that can be used to write to. 
	 */
	File registerPackageFile(PathList relativePath);
	
	void registerPackageFile(File file);

	/**
	 * Unzips the zip file and registers all the unzipped file for the current asset. 
	 * @see #registerPackageFile(PathList) 
	 */
	void unzip(File zipFile, PathList relativePath);
	
	/**
	 * Unzips the zip file and registers all the unzipped file for the current asset.
	 * @param transferer is being called for eached entry to be transferred 
	 * @see #registerPackageFile(PathList) 
	 */
	void unzip(File zipFile, List<PathList> relativePaths, Consumer<ZipEntryTransfer> transferer);
}
