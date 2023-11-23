// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.legacy;

import java.io.File;
import java.util.Objects;

import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.platform.setup.api.CreateProject;
import com.braintribe.model.platform.setup.api.InstallLicense;
import com.braintribe.model.platformsetup.api.request.TransferAsset;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.paths.PathList;

/**
 * @deprecated Review if needed. These were copied here to support {@link CreateProject} and {@link InstallLicense} (as theis handlers use it). The
 *             former is questionable and the latter is itself deprecated.
 */
@Deprecated
public class MavenInstallAssetTransfer extends AbstractAssetTransfer {

	private final File localRepo;
	private PathList artifactBasePath;
	
	public MavenInstallAssetTransfer(AccessRequestContext<? extends TransferAsset> context, File baseFolder, File localRepo, boolean keepTransferredAssetData) {
		super(context.getRequest().getAsset(), context, baseFolder, keepTransferredAssetData); 
		
		this.localRepo = localRepo;
		
		Objects.requireNonNull(localRepo, "localRepo must not be null!");
	}
	
	public MavenInstallAssetTransfer(PlatformAsset asset, Evaluator<ServiceRequest> evaluator, File baseFolder, File localRepo, boolean keepTransferredAssetData) {
		super(asset, evaluator, baseFolder, keepTransferredAssetData);
		
		this.localRepo = localRepo;
		
		Objects.requireNonNull(localRepo, "localRepo must not be null!");
	}

	@Override
	protected void downloadMavenMetaDataFile() {
		
		downloadMetaDataFromFileSystem(artifactBasePath);
		mavenMetadata = MavenMetaDataTools.acquireMetadata(asset, mavenMetaDataFile);

	}
	
	@Override
	protected void validate() {
		super.validate();
		artifactBasePath = PathList.create() //
				.push(localRepo.getAbsolutePath()) //
				.push(asset.getGroupId(), ".") //
				.push(asset.getName());

		File file = artifactBasePath.toFile();
		file.mkdirs();
		
	}

	@Override
	protected void createHashes() {
		// noop
	}

	@Override
	protected void createMavenMetaDataPart() {
		// noop
	}

	@Override
	protected void determineRevisions() {
		String resolvedRevision = asset.getResolvedRevision();
		if (!resolvedRevision.endsWith("-pc"))
			releaseRevision = String.valueOf(Integer.parseInt(resolvedRevision)+1) + "-pc";
		else
			releaseRevision = asset.getResolvedRevision();
	}

	@Override
	protected void fileTransfer() {
		localFileTransfer(artifactBasePath);
	}
	
	@Override
	protected String getMavenMetaDataFilename() {
		return "maven-metadata-local.xml";
	}
	
}
