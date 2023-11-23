// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.braintribe.build.cmd.assets.api.ArtifactResolutionContext;
import com.braintribe.build.cmd.assets.api.PlatformAssetBuilderContext;
import com.braintribe.build.cmd.assets.api.PlatformAssetCollector;
import com.braintribe.build.cmd.assets.api.PlatformAssetStorageRecording;
import com.braintribe.build.cmd.assets.api.ZipEntryTransfer;
import com.braintribe.build.cmd.assets.impl.PackagedPlatformSetupBuilder;
import com.braintribe.common.attribute.TypeSafeAttribute;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.model.platform.setup.api.PlatformSetupConfig;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.collection.api.MultiMap;
import com.braintribe.utils.collection.impl.HashMultiMap;
import com.braintribe.utils.collection.impl.MapAttributeContext;
import com.braintribe.utils.paths.PathList;
import com.braintribe.utils.stream.MultiplierOutputStream;
import com.braintribe.ve.api.VirtualEnvironment;

import tribefire.cortex.asset.resolving.ng.impl.PlatformAssetSolution;

public class PlatformAssetDistributionContextImpl implements PlatformAssetBuilderContext<PlatformAssetNature> {

	private final ServiceRequestContext requestContext;
	private final Map<Class<? extends PlatformAssetCollector>, PlatformAssetCollector> coalescings = new LinkedHashMap<>();
	private final MultiMap<PlatformAsset, File> assetFileAssociation = new HashMultiMap<>();
	private PlatformAssetSolution classifiedSolution;
	private final File packageBaseDir;
	private final PlatformAssetStorageRecording platformAssetStorageRecording;
	private final ArtifactResolutionContext artifactResolutionContext;
	
	private final PlatformSetupConfig request;
	private final PackagedPlatformSetupBuilder packagedPlatformSetupBuilder;
	
	private final MapAttributeContext attributeContext = new MapAttributeContext();

	public PlatformAssetDistributionContextImpl(ServiceRequestContext requestContext, PlatformSetupConfig request,
			VirtualEnvironment virtualEnvironment,
			PlatformAssetStorageRecording platformAssetStorageRecording,
			ArtifactResolutionContext artifactResolutionContext,
			File packageBaseDir,
			PackagedPlatformSetupBuilder packagedPlatformSetupBuilder, boolean doVerboseOutput) {

		this.requestContext = requestContext;
		this.request = request;
		this.platformAssetStorageRecording = platformAssetStorageRecording;
		this.artifactResolutionContext = artifactResolutionContext;
		this.packageBaseDir = packageBaseDir;
		this.virtualEnvironment = virtualEnvironment;
		this.packagedPlatformSetupBuilder = packagedPlatformSetupBuilder;
		this.doVerboseOutput = doVerboseOutput;
	}
	
	private final VirtualEnvironment virtualEnvironment;
	private final Map<Class<?>, Object> customData = new IdentityHashMap<>();
	private final boolean doVerboseOutput;
	
	@Override
	public boolean doVerboseOutput() {
		return doVerboseOutput;
	}
	
	@Override
	public PackagedPlatformSetupBuilder getPackagedPlatformSetupBuilder() {
		return packagedPlatformSetupBuilder;
	}
	
	@Override
	public String getStage() {
		return request.getStage();
	}
	
	@Override
	public boolean isDesigntime() {
		return !request.getRuntime();
	}
	
	@Override
	public boolean isRuntime() {
		return request.getRuntime();
	}
	
	@Override
	public Set<String> getTags() {
		return request.getTags();
	}
	
	@Override
	public VirtualEnvironment getVirtualEnvironment() {
		return virtualEnvironment;
	}
	
	@Override
	public <C> C getSharedInfo(Class<C> key, Supplier<C> supplier) {
		return (C)customData.computeIfAbsent(key, k -> supplier.get());
	}
	
	@Override
	public <C> C findSharedInfo(Class<C> key) {
		return (C)customData.get(key);
	}
	
	@Override
	public <C extends PlatformAssetCollector> C getCollector(Class<C> key, Supplier<C> supplier) {
		return (C) coalescings.computeIfAbsent(key, k -> supplier.get());
	}

	@Override
	public Stream<PlatformAssetCollector> coalescingBuildersStream() {
		return coalescings.values().stream();
	}
	
	@Override
	public PlatformAsset getAsset() {
		return classifiedSolution.asset;
	}
	
	@Override
	public PlatformAssetSolution getClassifiedSolution() {
		return classifiedSolution;
	}
	
	public void setClassifiedSolution(PlatformAssetSolution classifiedSolution) {
		this.classifiedSolution = classifiedSolution;
	}
	
	@Override
	public PlatformAssetNature getNature() {
		return classifiedSolution.nature;
	}
	
	@Override
	public File getPackageBaseDir() {
		return packageBaseDir;
	}

	@Override
	public Optional<File> findPartFile(String partType) {
		Part part = classifiedSolution.solution.getParts().get(partType);
		if (part == null)
			return Optional.empty();
		
		Resource r = part.getResource();
		if (!(r instanceof FileResource))
			throw new IllegalStateException("Resource for part type '"+partType+"' is not a FileResource. Resource: " + r);
		
		FileResource fr = (FileResource) r;
		return Optional.of(new File(fr.getPath()));
	}
	
	@Override
	public PlatformAssetStorageRecording platformAssetStorageRecording() {
		return platformAssetStorageRecording;
	}
	
	@Override
	public ArtifactResolutionContext artifactResolutionContext() {
		return artifactResolutionContext;
	}

	/**
	 * Same as {@link #copyPartFile(String, String, Function)} but returning an empty {@link Optional} if the requested partFile doesn't exist.
	 */
	@Override
	public Optional<File> copyPartFileOptional(String partType, String packageTargetSubdir, Function<String, String> nameTransformer) {
		return findPartFile(partType) //
				.map(partFile -> copyPartFile(packageTargetSubdir, nameTransformer, partFile));
	}

	private File copyPartFile(String packageTargetSubdir, Function<String, String> nameTransformer, File partFileTo) {
		File targetDir = getPackageBaseDir().toPath().resolve(packageTargetSubdir).toFile();
		String fileName = nameTransformer.apply(partFileTo.getName());
		File targetFile = new File(targetDir, fileName);
		targetDir.mkdirs();
		
		FileTools.copyFile(partFileTo, targetFile);
		
		registerPackageFile(targetFile);
		
		return targetFile;
	}

	
	@Override
	public File registerPackageFile(PathList relativePath) {
		File file = new File(getPackageBaseDir(), relativePath.toFilePath());
		registerPackageFile(file);
		return file;
	}
	
	@Override
	public void registerPackageFile(File file) {
		assetFileAssociation.put(getAsset(), file);
	}
	
	@Override
	public Stream<File> getAssociatedFiles(PlatformAsset asset) {
		return assetFileAssociation.getAll(asset).stream();
	}
	
	@Override
	public ServiceRequestContext requestContext() {
		return requestContext;
	}
	
	@Override
	public PlatformSetupConfig request() {
		return request;
	}
	
	@Override
	public void unzip(File zipFile, PathList relativePath) {
		unzip(zipFile, Collections.singletonList(relativePath), e -> {
			try (OutputStream out = new MultiplierOutputStream(e.targetFiles)) {
				IOTools.transferBytes(e.in, out, IOTools.BUFFER_SUPPLIER_8K);
			} catch (Exception ex) {
				throw Exceptions.unchecked(ex, "Error while transferring zip entry " + e.slashedPathName + " to " + e.targetFiles);
			}
		});
	}
	
	@Override
	public void unzip(File zipFile, List<PathList> relativePaths, Consumer<ZipEntryTransfer> transferer) {
		try (ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile))) {
			ZipEntry zipEntry = null;
			
			while ((zipEntry = zin.getNextEntry()) != null) {
				String slashedPathName = zipEntry.getName();
				
				List<File> targetFiles = new ArrayList<>(relativePaths.size());
				
				for (PathList relativePath: relativePaths) {
					File targetFile = registerPackageFile(relativePath.copy().pushSlashPath(slashedPathName));
					if (zipEntry.isDirectory()) {
						// create directory because it maybe empty and it would be an information loss otherwise
						targetFile.mkdirs();
					}
					else {
						targetFiles.add(targetFile);
						targetFile.getParentFile().mkdirs();
					}
				}
				
				transferer.accept(new ZipEntryTransfer(targetFiles, zin, slashedPathName));
				
				zin.closeEntry();
			}
		}
		catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while unpacking zip: " + zipFile);
		}
	}

	@Override
	public <C extends PlatformAssetCollector> Optional<C> findCollector(Class<C> key) {
		return Optional.ofNullable((C) coalescings.get(key));
	}

	@Override
	public ManagedGmSession session() {
		return platformAssetStorageRecording().session();
	}

	// Type Safe Attribute Context handling
	
	@Override
	public <A extends TypeSafeAttribute<V>, V> Optional<V> findAttribute(Class<A> attribute) {
		return attributeContext.findAttribute(attribute);
	}

	@Override
	public <A extends TypeSafeAttribute<? super V>, V> void setAttribute(Class<A> attribute, V value) {
		attributeContext.setAttribute(attribute, value);
	}

}
