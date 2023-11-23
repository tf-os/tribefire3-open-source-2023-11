// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.api;

import java.io.File;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.braintribe.build.cmd.assets.PlatformAssetDistributionConstants;
import com.braintribe.build.cmd.assets.impl.PackagedPlatformSetupBuilder;
import com.braintribe.common.attribute.MutableTypeSafeAttributes;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.platform.setup.api.PlatformSetupConfig;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.paths.PathList;

import tribefire.cortex.asset.resolving.ng.api.PlatformAssetResolvingContext;

/**
 * 
 * @author Dirk Scheffler
 *
 */
public interface PlatformAssetDistributionContext extends PlatformAssetResolvingContext, PlatformAssetDistributionConstants, MutableTypeSafeAttributes {
	File getPackageBaseDir();
	
	PackagedPlatformSetupBuilder getPackagedPlatformSetupBuilder();
	
	boolean doVerboseOutput();
	
	ServiceRequestContext requestContext(); 
	PlatformSetupConfig request();
	
	<C extends PlatformAssetCollector> C getCollector(Class<C> key, Supplier<C> supplier);
	<C extends PlatformAssetCollector> Optional<C> findCollector(Class<C> key);
	
	Stream<PlatformAssetCollector> coalescingBuildersStream();
	
	PlatformAssetStorageRecording platformAssetStorageRecording();

	ArtifactResolutionContext artifactResolutionContext();

	public default PathList storageAccessDataStageFolder(String accessId, String stage) {
		return storageAccessDataStageFolder(accessId, stage, false);
	}
	
	public default PathList storageAccessDataFolder(String accessId) {
		return storageAccessDataFolder(accessId, false);
	}
	
	public default PathList storageAccessFolder(String accessId) {
		return storageAccessFolder(accessId, false);
	}
	
	public default PathList storageAccessDataStageFolder(String accessId, String stage, boolean relative) {
		stage = FileTools.replaceIllegalCharactersInFileName(stage, "_");
		
		return storageAccessDataFolder(accessId, relative).push(stage);
	}
	
	public default PathList storageAccessDataFolder(String accessId, boolean relative) {
		return storageAccessFolder(accessId, relative).push("data");
	}
	
	public default PathList storageAccessFolder(String accessId, boolean relative) {
		return storageDatabasesFolder(relative).push(accessId);
	}

	public default PathList storageDatabasesFolder(boolean relative) {
		return storageFolder(relative).push("databases");
	}

	public default PathList storagePublicResourcesFolder(boolean relative) {
		return storageFolder(relative).push("public-resources");
	}
	
	public default PathList storageFolder(boolean relative) {
		return projectionBaseFolder(relative).push("storage");
	}
	
	public default PathList projectionBaseFolder(boolean relative) {
		PathList base = relative? 
				PathList.create():
					PathList.create().push(getPackageBaseDir().getAbsolutePath());
				
				return base.push(PROJECTION_NAME_MASTER);
	}

	Stream<File> getAssociatedFiles(PlatformAsset asset);
}
