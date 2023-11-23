// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.braintribe.build.cmd.assets.api.PlatformAssetBuilderContext;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.mimetype.MimeTypeDetector;
import com.braintribe.mimetype.PlatformMimeTypeDetector;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.ResourcePriming;
import com.braintribe.model.csa.ManInitializer;
import com.braintribe.model.processing.manipulation.marshaller.ManMarshaller;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.FileSystemSource;
import com.braintribe.model.resource.specification.ImageSpecification;
import com.braintribe.model.resource.specification.RasterImageSpecification;
import com.braintribe.model.resource.specification.ResourceSpecification;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.paths.PathList;
import com.braintribe.utils.stream.MultiplierOutputStream;

public class ResourcePrimingNatureBuilder extends AbstractStorageBuilder<ResourcePriming> {
	private static Collection<String> supportedRasterImageMimeTypes = Arrays.asList("image/gif", "image/png", "image/jpeg", "image/bmp");
	private static Logger logger = Logger.getLogger(ResourcePrimingNatureBuilder.class);
	
	private MimeTypeDetector mimeTypeDetector = PlatformMimeTypeDetector.instance;

	@Override
	public void transfer(PlatformAssetBuilderContext<ResourcePriming> context, StorageConfigurationCollector storageCoalescing) {
		ResourcePriming resourcePriming = context.getNature();
		String accessId = resourcePriming.getAccessId();
		
		if (accessId==null) {
			throw new IllegalStateException("Access id in " + resourcePriming + " must be set");
		}
		
		PlatformAsset asset = context.getAsset();
		String stageName = asset.qualifiedAssetName();

		File resourcesZip = context.findPartFile("resources:zip").get();
		
		List<File> dataManFiles = new ArrayList<>();
		List<PathList> accessResourcesBaseDirs = new ArrayList<>();
		ManInitializer manInitializer = ManInitializer.T.create();
		manInitializer.setName(stageName);
		
		resourcePriming.effectiveAccessIds().forEach(a -> {
			storageCoalescing.appendStage(a,  manInitializer);
			File dataManFile = context.storageAccessDataStageFolder(a, stageName).push("data.man").toFile();
			dataManFile.getParentFile().mkdirs();
			dataManFiles.add(dataManFile);
			PathList accessResourcesBaseDir = context.storageAccessFolder(a, true).push("resources").push(asset.getGroupId() + "." + asset.getName());
			accessResourcesBaseDirs.add(accessResourcesBaseDir);
		});

		
		List<Resource> resources = new ArrayList<>();
		Date creationDate = new Date(); 
		
		// unzip resources to all storages' resource folders
		
		context.unzip(resourcesZip, accessResourcesBaseDirs, e -> {
			try {
				MessageDigest digest = MessageDigest.getInstance("MD5");
				DigestInputStream digestIn = new DigestInputStream(e.in, digest);
				try (OutputStream out = new MultiplierOutputStream(e.targetFiles)) {
					IOTools.transferBytes(digestIn, out, IOTools.BUFFER_SUPPLIER_8K);
				}

				File singleTargetFile = e.targetFiles.get(0); 
				
				String md5 = StringTools.toHex(digest.digest());
				String slashedPathName = e.slashedPathName;
				
				String fileName = FileTools.getName(slashedPathName);
				
				String resourceGlobalId = "asset-resource://" + context.getAsset().getGroupId() + ':' + asset.getName() + '/' + slashedPathName;
				String sourceGlobalId = "asset-file://" + context.getAsset().getGroupId() + ':' + asset.getName() + '/' + slashedPathName;
				//GSC: 2019-03-11: MimeType detection always provided default mimeType: application/octet-stream -> MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(singleTargetFile);
				// Changed to PlatformMimeTypeDetector which is at least capable of understanding well known mimeTypes per extension. If more sophisticated mimeType detection is required we should consider switching to Tika MimeType detection.
				String detectedMimeType = this.mimeTypeDetector.getMimeType(singleTargetFile, fileName);
				ResourceSpecification specification = null;
				
				if (supportedRasterImageMimeTypes.contains(detectedMimeType)) {
					try (InputStream specIn = new FileInputStream(singleTargetFile)) {
						specification = getSpecification(specIn);
						if (specification != null) {
							String specGlobalId = "asset-spec://" + context.getAsset().getGroupId() + ':' + asset.getName() + '/' + slashedPathName;
							specification.setGlobalId(specGlobalId);
						}
					}
				}

				FileSystemSource source = FileSystemSource.T.create();
				source.setGlobalId(sourceGlobalId);
				source.setPath(asset.getGroupId() + "." + asset.getName() + "/" + slashedPathName);
				
				Resource resource = Resource.T.create();
				resource.setGlobalId(resourceGlobalId);
				resource.setName(fileName);
				resource.setMd5(md5);
				resource.setMimeType(detectedMimeType);
				resource.setCreated(creationDate);
				resource.setCreator("platform-setup-processing");
				resource.setFileSize(singleTargetFile.length());
				resource.setResourceSource(source);
				resource.setSpecification(specification);
				
				resources.add(resource);
			} catch (Exception ex) {
				throw Exceptions.unchecked(ex, "Error while transferring zip entry " + e.slashedPathName + " to " + e.targetFiles);
			}
		});

		// marshall generated resources to all data.man files in all storages

		try (OutputStream out = new MultiplierOutputStream(dataManFiles)) {
			ManMarshaller manMarshaller = new ManMarshaller();
			manMarshaller.marshall(out, resources);
		} catch (IOException e) {
			throw new UncheckedIOException(e.getMessage(), e);
		}
	}
	
	private ImageSpecification getSpecification(InputStream in) throws IOException {
		
		ImageInputStream iis = null;
		ImageReader imageReader = null;
		
		try {
			iis = ImageIO.createImageInputStream(in);
			
			if (iis == null)
				return null;
			
			Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
			
			if (readers.hasNext()) {
				imageReader = readers.next();
				imageReader.setInput(iis);
				int height = imageReader.getHeight(0);
				int width = imageReader.getWidth(0);
				
				RasterImageSpecification specification = RasterImageSpecification.T.create();
				specification.setPixelWidth(width);
				specification.setPixelHeight(height);
				int numberImages = imageReader.getNumImages(true);
				specification.setPageCount(numberImages);
				
				return specification;
				
			}
			return null;
			
		} finally {
			if (imageReader != null) {
				try {
					imageReader.dispose();
				} catch(Exception e) {
					logger.error("Error while disposing the image reader", e);
				}
			}
			
		}
	}


	@Override
	public List<String> relevantParts() {
		return Arrays.asList("resources:zip");
	}
}

