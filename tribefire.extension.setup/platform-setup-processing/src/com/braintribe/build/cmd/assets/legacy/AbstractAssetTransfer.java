// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.legacy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.maven.meta.MavenMetaData;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.PlatformAssetDependency;
import com.braintribe.model.asset.natures.AssetAggregator;
import com.braintribe.model.asset.natures.ContainerProjection;
import com.braintribe.model.asset.natures.LicensePriming;
import com.braintribe.model.asset.natures.ManipulationPriming;
import com.braintribe.model.asset.natures.ModelPriming;
import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.model.asset.natures.RepositoryView;
import com.braintribe.model.asset.natures.RuntimeProperties;
import com.braintribe.model.cortexapi.access.collaboration.CollaborativeStageData;
import com.braintribe.model.cortexapi.access.collaboration.GetCollaborativeStageData;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.processing.platformsetup.ManipulationRecording;
import com.braintribe.model.processing.platformsetup.NatureRecording;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.version.FuzzyVersion;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionExpression;
import com.braintribe.model.version.VersionRange;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.MapTools;
import com.braintribe.utils.paths.PathList;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;

/**
 * @deprecated Review if needed, see {@link MavenInstallAssetTransfer}
 */
@Deprecated
public abstract class AbstractAssetTransfer {

	private static final String MAN_VAR_SELECTOR = "$selector";

	private static final String MAN_VAR_IS_GLOBAL_SETUP_CANDIDATE = "$isGlobalSetupCandidate";

	private static final Logger logger = Logger.getLogger(AbstractAssetTransfer.class);
	
	private static final String PART_IDENTIFIER_DEPENDENCY_MAN = "assetdependency:man";
	
	protected File artifactVersionFolder;
	protected File artifactFolder;

	protected String commonPartIdentifier;

	protected PlatformAssetNature nature;

	protected PlatformAsset asset;

	protected File baseFolder;

	protected String releaseFullQualifiedVersion;

	protected String releaseRevision;

	protected File mavenMetaDataFile;

	protected MavenMetaData mavenMetadata;

	private BiConsumer<AbstractAssetTransfer, ?> natureExpert;
	
	private final boolean keepTransferredAssetData;
	
	private static Map<EntityType<? extends PlatformAssetNature>, BiConsumer<AbstractAssetTransfer, ? >> naturePartExpertRegistry = new IdentityHashMap<>();
	
	private final Evaluator<ServiceRequest> evaluator;
	
	private final Map<InputStreamProvider, String> staticParts = new LinkedHashMap<>();
	
	private static <N extends PlatformAssetNature> void registerNaturePartExpert(EntityType<N> natureType, BiConsumer<AbstractAssetTransfer, ? super N> expert) {
		naturePartExpertRegistry.put(natureType, expert);
	}
	
	static {
		registerNaturePartExpert(ManipulationPriming.T, AbstractAssetTransfer::createDataParts);
		registerNaturePartExpert(AssetAggregator.T, AbstractAssetTransfer::createNoParts);
		registerNaturePartExpert(ContainerProjection.T, AbstractAssetTransfer::createNoParts);
		registerNaturePartExpert(RuntimeProperties.T, AbstractAssetTransfer::createNoParts);
		registerNaturePartExpert(LicensePriming.T, AbstractAssetTransfer::createNoParts);
		registerNaturePartExpert(RepositoryView.T, AbstractAssetTransfer::createNoParts);
	}
	
	public AbstractAssetTransfer(PlatformAsset asset, Evaluator<ServiceRequest> evaluator, File baseFolder, boolean keepTransferredAssetData) {
		this.asset = asset;
		this.evaluator = evaluator;
		this.baseFolder = baseFolder;
		this.keepTransferredAssetData = keepTransferredAssetData;

		Objects.requireNonNull(baseFolder, "baseFolder must not be null!");
	}
	
	public void addStaticPart(InputStreamProvider provider, String part) {
		staticParts.put(provider, part);
	}

	public void transfer() {
		
		logger.debug(() -> "Artifact files are being created in tmp-directory " + baseFolder.getAbsolutePath());
		if (keepTransferredAssetData) {
			logger.info(() -> "keepTransferredAssetData is set to true, data is kept in " + baseFolder.getAbsolutePath());
		}
		
		validate();

		downloadMavenMetaDataFile();

		determineRevisions();

		determineVersions();
		
		updateMavenMetaDataAndAssetRevision();

		createParts();

		createHashes();

		fileTransfer();
		
		asset.setHasUnsavedChanges(false);

		if (keepTransferredAssetData) {
			logger.info(() -> "keepTransferredAssetData is set to true, asset data can be found in " + artifactFolder.getAbsolutePath());
			return;
		} 
			
		try {
			FileTools.deleteDirectoryRecursively(baseFolder);
		} catch (IOException e) {
			throw Exceptions.unchecked(e, "Error while deleting tmp-directory!");
		}
			
	}

	private void determineVersions() {

		releaseFullQualifiedVersion = asset.getVersion() + "." + releaseRevision;

		artifactVersionFolder = new File(artifactFolder, releaseFullQualifiedVersion);

		if (!artifactVersionFolder.exists()) {
			artifactVersionFolder.mkdir();
		}

		commonPartIdentifier = asset.getName() + "-" + releaseFullQualifiedVersion;

	}

	protected abstract void determineRevisions();

	private void updateMavenMetaDataAndAssetRevision() {

		asset.setResolvedRevision(releaseRevision);

		MavenMetaDataTools.updateMetaData(asset, mavenMetadata, mavenMetaDataFile);
		
	}

	protected abstract void downloadMavenMetaDataFile();

	protected void downloadMetaDataFromFileSystem(PathList artifactBasePath) {
		downloadMetaDataFromFileSystem(artifactBasePath, true);
	}
	
	protected void downloadMetaDataFromFileSystem(PathList artifactBasePath, boolean isInstall) {
		String metaDataFileName = isInstall ? "maven-metadata-local.xml" : "maven-metadata.xml";
		File mavenMetaDataLocal = artifactBasePath.copy().push(metaDataFileName).toFile();
		
		if (mavenMetaDataLocal.exists())
			FileTools.copyFile(mavenMetaDataLocal, mavenMetaDataFile);
		
	}

	protected abstract void fileTransfer();

	protected void localFileTransfer(PathList artifactBasePath) {
		try {
			File pathToClean = new File(artifactBasePath.toFile(), releaseFullQualifiedVersion);
			if (pathToClean.exists()) {
				// delete existing files to avoid mixture of different states when reinstalling
				FileTools.deleteDirectoryRecursively(pathToClean);
			}
			FileTools.copyDirectory(artifactFolder, artifactBasePath.toFile());

		} catch (IOException e) {
			throw new UncheckedIOException(
					"Error while transfering artifactFolder from " + artifactFolder.getAbsolutePath() + " to " + artifactBasePath.toFilePath(), e);
		}
	}

	// ####################################################
	// ## . . . . . . Basic validation/setup . . . . . . ##
	// ####################################################

	protected void validate() {

		this.nature = asset.getNature();
		natureExpert = naturePartExpertRegistry.get(nature.entityType());
		
		if(natureExpert == null) {
			throw new IllegalStateException("Unsupported asset nature type: " + nature.entityType() + ". Supported nature types: " + naturePartExpertRegistry.keySet());
		}
		
		
		if (asset.getResolvedRevision() == null) {
			asset.setResolvedRevision("0");
		}

		String prefix = asset.getGroupId() + "." + asset.getName() + "#" + asset.getVersion() + "-";

		try {

			baseFolder.mkdirs();
			artifactFolder = Files.createTempDirectory(baseFolder.toPath(), prefix).toFile();

		} catch (IOException e) {
			throw new UncheckedIOException("Error while creating temp directory in " + baseFolder.getAbsolutePath() + " for " + prefix, e);
		}

		mavenMetaDataFile = new File(artifactFolder, getMavenMetaDataFilename());
	}

	protected String getMavenMetaDataFilename() {
		return "maven-metadata.xml";
	}

	// ###########################################
	// ## . . . . . . Hash creation . . . . . . ##
	// ###########################################

	protected abstract void createHashes();

	// ############################################
	// ## . . . . . . Parts creation . . . . . . ##
	// ############################################

	private void createParts() {

		createPomPart();

		createAssetManPart();

		createNatureParts();
		
		createMavenMetaDataPart();
	}

	private void createNatureParts() {
		
		((BiConsumer<AbstractAssetTransfer, PlatformAssetNature>)natureExpert).accept(this, nature);
		
		for (Map.Entry<InputStreamProvider, String> entry: staticParts.entrySet()) {
			String part = entry.getValue();
			InputStreamProvider inputStreamProvider = entry.getKey();
			
			String[] parts = parsParts(part);
			
			File partFile = buildPartFile(parts[0], parts[1]);
			
			try (InputStream in = inputStreamProvider.openInputStream(); OutputStream out = new FileOutputStream(partFile)) {
				IOTools.transferBytes(in, out, IOTools.BUFFER_SUPPLIER_64K);
			} catch (Exception e) {
				throw Exceptions.unchecked(e, "Error while writing data part file " + partFile.getAbsolutePath());
			}
		}
		
	}

	private String[] parsParts(String s) {
		int i = s.indexOf(":");
		if (i < 0)
			return new String[] { "", s };
		else
			return new String[] { s.substring(0, i), s.substring(i + 1) };
	}

	protected abstract void createMavenMetaDataPart();

	private void createDataParts(ManipulationPriming nature) {

		GetCollaborativeStageData getStageData = GetCollaborativeStageData.T.create();
		getStageData.setName(asset.getGroupId() + ":" + asset.getName() + "#" + asset.getVersion());
		getStageData.setServiceId(nature.getAccessId());

		CollaborativeStageData data = getStageData.eval(evaluator).get();

		Stream.of(data.getDataResource(), data.getModelResource()).filter(Objects::nonNull).forEach(this::transferResource);

		// custom content resources
		createContentResourcesPart(data.getContentResources());

	}
	
	@SuppressWarnings("unused")
	private void createModelPrimingParts(ModelPriming nature) {
		//GmMetaModel model = get
	}
	
	private void createNoParts(@SuppressWarnings("unused") PlatformAssetNature nature) {
		// noop
	}

	private void transferResource(Resource resource) {
		String[] nameParts = resource.getName().split("\\.");
		File resourcePartFile = buildPartFile(nameParts[0], nameParts[1]);

		try (InputStream is = resource.openStream(); OutputStream os = new FileOutputStream(resourcePartFile)) {

			IOTools.transferBytes(is, os);

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while writing data part file " + resourcePartFile.getAbsolutePath());
		}

	}

	// ########################################################
	// ## . . . . . . Content Resources Creation . . . . . . ##
	// ########################################################

	private void createContentResourcesPart(Map<String,Resource> resources) {
		if (MapTools.isEmpty(resources))
			return;

		try {
			File zipFile = buildPartFile("resources", "zip");
			try(ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
				
				for (Entry<String, Resource> entry : resources.entrySet()) {
					String relativePath = entry.getKey();
					Resource resource = entry.getValue();
					
					ZipEntry zipEntry = new ZipEntry(relativePath);
					zos.putNextEntry(zipEntry);
					try(InputStream is = resource.openStream()) {
						IOTools.transferBytes(is, zos);
					}
					zos.closeEntry();
				}
				
			}

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while creating resources zip!");
		}

	}

	private void createAssetManPart() {

		File f = buildPartFile("asset", "man");

		PlatformAssetNature detachedManPriming = nature.clone(new StandardCloningContext() {
			@Override
			public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property,
					GenericEntity instanceToBeCloned, GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {
				
				return !property.isIdentifying();
			}
		});
		
		NatureRecording.stringify(f, detachedManPriming);

	}

	protected File buildPartFile(String classifier, String type) {

		Objects.requireNonNull(type, "Type used for part file identification must not be null!");

		StringBuilder builder = new StringBuilder();
		builder.append(commonPartIdentifier);
		if (classifier != null && !classifier.isEmpty()) {
			builder.append('-');
			builder.append(classifier);
		}

		builder.append('.');
		builder.append(type);

		File file = new File(artifactVersionFolder, builder.toString());

		return file;

	}

	// ##########################################
	// ## . . . . . . POM creation . . . . . . ##
	// ##########################################

	private void createPomPart() {
		List<PlatformAssetDependency> qualifiedDependencies = asset.getQualifiedDependencies();

		try {
			Document document = DomParser.create().setNamespaceAware(true).makeItSo();
			Element projectElement = document.createElementNS("http://maven.apache.org/POM/4.0.0", "project");
			projectElement.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation", "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd");

			document.appendChild(projectElement);

			Element modelVersionElement = document.createElement("modelVersion");
			projectElement.appendChild(modelVersionElement);
			modelVersionElement.setTextContent("4.0.0");

			writeAssetDependency(projectElement, asset, false);

			Element packagingElement = document.createElement("packaging");
			projectElement.appendChild(packagingElement);
			packagingElement.setTextContent("pom");
			
			Element dependenciesElement = document.createElement("dependencies");
			projectElement.appendChild(dependenciesElement);

			for (PlatformAssetDependency dependency : qualifiedDependencies) {
				Element dependencyElement = document.createElement("dependency");
				dependenciesElement.appendChild(dependencyElement);
				writeAssetDependency(dependencyElement, dependency.getAsset(), true);
				
				DomUtils.setElementValueByPath(dependencyElement, "classifier", "asset", true);
				DomUtils.setElementValueByPath(dependencyElement, "type", "man", true);
				dependencyElement.appendChild(dependencyElement.getOwnerDocument().createProcessingInstruction("tag", "asset"));
				
				if(dependency.getIsGlobalSetupCandidate() || dependency.getSelector() != null) {
					String stringifiedQualification = getStringifiedQualification(dependency);
					ProcessingInstruction processingInstruction = dependencyElement.getOwnerDocument().createProcessingInstruction("part",
							PART_IDENTIFIER_DEPENDENCY_MAN + ' ' + stringifiedQualification);
					dependencyElement.appendChild(processingInstruction);
				}
			}

			DomParser.write().from(document).to(buildPartFile(null, "pom"));

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while generating pom part for " + asset);
		}
	}

	private String getStringifiedQualification(PlatformAssetDependency dependency) {
		StringWriter writer = new StringWriter();
		
		if(dependency.getIsGlobalSetupCandidate()) {
			writer.append(MAN_VAR_IS_GLOBAL_SETUP_CANDIDATE);
			writer.append(" = true");
		}
		
		if(dependency.getSelector() != null) {
			writer.append('\n');
			writer.append(MAN_VAR_SELECTOR);
			
			ManipulationRecording.stringify(writer, dependency.getSelector(), "$selector", null);
		}
		
		return writer.toString();
	}

	private void writeAssetDependency(Element projectElement, PlatformAsset dependency, boolean rangify) {

		writeUnversionedAssetDependency(projectElement, dependency);

		if (rangify) {
			VersionExpression range = toRange(dependency);
			DomUtils.setElementValueByPath(projectElement, "version", range.asString(), true);

		} else {

			DomUtils.setElementValueByPath(projectElement, "version", dependency.getVersion() + '.' + dependency.getResolvedRevision(), true);
		}
	}

	private VersionExpression toRange(PlatformAsset dependency) {
		Version v = Version.parse(dependency.getVersion());
		if (v.getRevision() != null) 
			return VersionRange.from(v, false, Version.create(v.getMajor(), v.getMinor() + 1), true);
		 else 
			return FuzzyVersion.from(v);
	}

	private void writeUnversionedAssetDependency(Element projectElement, PlatformAsset dependency) {
		DomUtils.setElementValueByPath(projectElement, "groupId", dependency.getGroupId(), true);
		DomUtils.setElementValueByPath(projectElement, "artifactId", dependency.getName(), true);

	}

}
