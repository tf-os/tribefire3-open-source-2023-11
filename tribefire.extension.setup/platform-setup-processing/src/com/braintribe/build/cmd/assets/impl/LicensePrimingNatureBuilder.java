// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.auxilii.glf.client.exception.LicenseException;
import com.braintribe.build.cmd.assets.api.PlatformAssetBuilderContext;
import com.braintribe.common.lcd.Pair;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.LicensePriming;
import com.braintribe.model.license.License;
import com.braintribe.model.processing.license.glf.LicenseTools;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.FileSystemSource;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.MultiplierOutputStream;

public class LicensePrimingNatureBuilder extends AbstractStorageBuilder<LicensePriming> {
	
	private static final String PART_TYPE = "license:glf";
	
	@Override
	public void transfer(PlatformAssetBuilderContext<LicensePriming> context, StorageConfigurationCollector storageCoalescing) {
		try {
			PlatformAsset asset = context.getAsset();
			File licenseFile = context.findPartFile(PART_TYPE).get();
			
			Pair<com.auxilii.glf.client.License, String> licenseInformation = LicenseTools.validateLicense(() -> new FileInputStream(licenseFile), licenseFile);
			
			String standardLicenseFileName = "license.sigxml.glf";
			
			String resourceGlobalId = "asset-resource://" + context.getAsset().getGroupId() + ':' + asset.getName() + '/' + standardLicenseFileName;
			String sourceGlobalId = "asset-file://" + context.getAsset().getGroupId() + ':' + asset.getName() + '/' + standardLicenseFileName;

			List<File> targetFiles = new ArrayList<>();
			
			context.getNature().effectiveAccessIds().forEach(accessId -> {
				
				File targetFile = context.storageAccessFolder(accessId, false).push("resources").push(asset.getGroupId() + "." + asset.getName()).push(standardLicenseFileName).toFile();
				targetFiles.add(targetFile);
				targetFile.getParentFile().mkdirs();
				
				storageCoalescing.appendStage(context, accessId, session -> {
					try {
						
						com.auxilii.glf.client.License glfLicense = licenseInformation.first();
						
						FileSystemSource source = session.create(FileSystemSource.T);
						source.setGlobalId(sourceGlobalId);
						source.setPath(asset.getGroupId() + "." + asset.getName() + "/" + standardLicenseFileName);
						
						Resource resource = session.create(Resource.T);
						resource.setGlobalId(resourceGlobalId);
						resource.setName(standardLicenseFileName);
						resource.setFileSize(licenseFile.length());
						resource.setMimeType("text/plain");
						resource.setMd5(licenseInformation.second());
						resource.setResourceSource(source);
						resource.setCreated(glfLicense.getIssueDate());
						resource.setCreator(glfLicense.getLicensor());
						
						License license = session.create(License.T);
						license.setGlobalId("license:" + glfLicense.getLicenseId());
						license.setLicenseResource(resource);
						license.setUploadDate(glfLicense.getIssueDate());
						license.setActive(true);
						license.setUploader(license.getLicensor());
						license.setExpiryDate(glfLicense.getExpiryDate());
						license.setLicensee(glfLicense.getLicensee());
						license.setLicensor(glfLicense.getLicensor());
						license.setIssueDate(glfLicense.getIssueDate());
						license.setLicenseeAccount(glfLicense.getLicenseeAccount());
					} catch (LicenseException e) {
						throw new IllegalStateException("Error while creating license entity from " + context.getAsset().qualifiedRevisionedAssetName(), e);
					}
				});
			});
			
			try (InputStream in = new FileInputStream(licenseFile); OutputStream out = new MultiplierOutputStream(targetFiles)) {
				IOTools.transferBytes(in, out, IOTools.BUFFER_SUPPLIER_64K);
			}
			
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while creating license entity from " + context.getAsset().qualifiedRevisionedAssetName());
		}
	}
	
	@Override
	public List<String> relevantParts() {
		return Arrays.asList(PART_TYPE);
	}
}
