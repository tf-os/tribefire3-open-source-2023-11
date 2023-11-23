// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.model.processing.platformsetup;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.PlatformAssetDependency;
import com.braintribe.model.asset.natures.AssetAggregator;
import com.braintribe.model.asset.natures.ContainerProjection;
import com.braintribe.model.asset.natures.LicensePriming;
import com.braintribe.model.asset.natures.ManipulationPriming;
import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.model.asset.natures.RepositoryView;
import com.braintribe.model.asset.natures.RuntimeProperties;
import com.braintribe.model.cortexapi.access.collaboration.CollaborativeStageData;
import com.braintribe.model.cortexapi.access.collaboration.GetCollaborativeStageData;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.ConfigurableCloningContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.platformsetup.api.response.AssetResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.api.ResourceBuilder;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.version.FuzzyVersion;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionExpression;
import com.braintribe.model.version.VersionRange;
import com.braintribe.utils.MapTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.ZipTools;
import com.braintribe.utils.io.ZipEntryWriter;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

/**
 * @author peter.gazdik
 */
/* package */ class AssetArtifactBuilder {

	private final Evaluator<ServiceRequest> evaluator;
	private final ResourceBuilder resourceBuilder;
	private final PlatformAsset asset;
	private final PlatformAssetNature nature;

	private final String pathPrefix;
	private ZipEntryWriter zew;

	private static final String MAN_VAR_SELECTOR = "$selector";
	private static final String MAN_VAR_IS_GLOBAL_SETUP_CANDIDATE = "$isGlobalSetupCandidate";
	private static final String PART_IDENTIFIER_DEPENDENCY_MAN = "assetdependency:man";

	public AssetArtifactBuilder(PlatformAsset asset, Evaluator<ServiceRequest> evaluator, ResourceBuilder resourceBuilder) {
		this.evaluator = evaluator;
		this.asset = asset;
		this.nature = asset.getNature();
		this.resourceBuilder = resourceBuilder;
		this.pathPrefix = assetPathPrefix();

		validateNature();
	}

	private void validateNature() {
		Set<EntityType<? extends PlatformAssetNature>> supportedNatures = asSet( //
				ManipulationPriming.T, //
				AssetAggregator.T, //
				ContainerProjection.T, //
				RuntimeProperties.T, //
				LicensePriming.T, //
				RepositoryView.T //
		);

		if (!supportedNatures.contains(nature.entityType()))
			throw new IllegalArgumentException("Unsupported asset nature: " + nature.entityType() + ". Supported: " + supportedNatures);
	}

	private String assetPathPrefix() {
		String folderPrefix = asset.getGroupId() + "/" + asset.getName() + "/" + asset.getVersion() + "/";

		String releaseFullQualifiedVersion = asset.getVersion() + "." + determineRevision();
		String commonPartIdentifier = asset.getName() + "-" + releaseFullQualifiedVersion;

		return folderPrefix + commonPartIdentifier;
	}

	private String determineRevision() {
		String resolvedRevision = asset.getResolvedRevision();
		if (resolvedRevision == null)
			return "1-pc";

		else if (!resolvedRevision.endsWith("-pc"))
			return String.valueOf(Integer.parseInt(resolvedRevision) + 1) + "-pc";

		else
			return resolvedRevision;
	}

	// ###################################################
	// ## . . . . . . . . . . Build . . . . . . . . . . ##
	// ###################################################

	public AssetResource build() {
		AssetResource result = AssetResource.T.create();
		result.setResource(buildResource());
		return result;
	}

	private Resource buildResource() {
		return resourceBuilder.newResource() //
				.withName(resourceName()) //
				.usingOutputStream(os -> writeZip(os));
	}

	private String resourceName() {
		return asset.getName() + "-" + asset.getVersion() + ".zip";
	}

	private void writeZip(OutputStream os) throws IOException {
		ZipTools.writeZipTo(os, zew -> writeZipEntries(zew));
	}

	private void writeZipEntries(ZipEntryWriter zew) {
		this.zew = zew;

		writePomPart();
		writeAssetManPart();
		writeNatureSpecificParts();
	}

	private void writePomPart() {
		zew.writeZipEntry(partName(null, "pom"), wb -> {
			wb.usingOutputStream(os -> {
				writePomTo(os);
			});
		});
	}

	private void writePomTo(OutputStream os) {
		try {
			Document document = createPomDocument();
			DomParser.write().from(document).to(os);

		} catch (DomParserException e) {
			throw Exceptions.unchecked(e, "Error while generating pom part for " + asset);
		}
	}

	private Document createPomDocument() throws DomParserException {
		Document document = DomParser.create().setNamespaceAware(true).makeItSo();
		Element projectElement = document.createElementNS("http://maven.apache.org/POM/4.0.0", "project");
		projectElement.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation",
				"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd");

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

		for (PlatformAssetDependency dependency : asset.getQualifiedDependencies()) {
			Element dependencyElement = document.createElement("dependency");
			dependenciesElement.appendChild(dependencyElement);
			writeAssetDependency(dependencyElement, dependency.getAsset(), true);

			DomUtils.setElementValueByPath(dependencyElement, "classifier", "asset", true);
			DomUtils.setElementValueByPath(dependencyElement, "type", "man", true);
			dependencyElement.appendChild(dependencyElement.getOwnerDocument().createProcessingInstruction("tag", "asset"));

			if (dependency.getIsGlobalSetupCandidate() || dependency.getSelector() != null) {
				String stringifiedQualification = getStringifiedQualification(dependency);
				ProcessingInstruction processingInstruction = dependencyElement.getOwnerDocument().createProcessingInstruction("part",
						PART_IDENTIFIER_DEPENDENCY_MAN + ' ' + stringifiedQualification);
				dependencyElement.appendChild(processingInstruction);
			}
		}

		return document;
	}

	private String getStringifiedQualification(PlatformAssetDependency dependency) {
		StringWriter writer = new StringWriter();

		if (dependency.getIsGlobalSetupCandidate()) {
			writer.append(MAN_VAR_IS_GLOBAL_SETUP_CANDIDATE);
			writer.append(" = true");
		}

		if (dependency.getSelector() != null) {
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

	/////////////

	private void writeAssetManPart() {
		String partName = partName("asset", "man");
		String text = stringifyNature();

		zew.writeZipEntry(partName, wb -> {
			wb.string(text);
		});
	}

	private String stringifyNature() {
		ConfigurableCloningContext cc = ConfigurableCloningContext.build().skipIndentifying(true).done();
		PlatformAssetNature cloned = nature.clone(cc);
		return NatureRecording.stringify(cloned);
	}

	/////////////

	private void writeNatureSpecificParts() {
		if (nature instanceof ManipulationPriming)
			createDataParts((ManipulationPriming) nature);
	}

	private void createDataParts(ManipulationPriming nature) {
		GetCollaborativeStageData getStageData = GetCollaborativeStageData.T.create();
		getStageData.setName(asset.getGroupId() + ":" + asset.getName() + "#" + asset.getVersion());
		getStageData.setServiceId(nature.getAccessId());

		CollaborativeStageData data = getStageData.eval(evaluator).get();

		Stream.of(data.getDataResource(), data.getModelResource()) //
				.filter(Objects::nonNull) //
				.forEach(this::transferResource);

		// custom content resources
		createContentResourcesPart(data.getContentResources());
	}

	private void createContentResourcesPart(Map<String, Resource> resources) {
		if (MapTools.isEmpty(resources))
			return;

		try {
			String resZipEntryName = partName("resources", "zip");

			zew.writeZipEntry(resZipEntryName, wb -> {
				wb.withName(resZipEntryName) //
						.usingOutputStream(os -> {
							zipResourcesTo(resources, os);
						});
			});

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while creating resources zip!");
		}

	}

	private void zipResourcesTo(Map<String, Resource> resources, OutputStream os) throws IOException {
		ZipTools.writeZipTo(os, zew -> {
			writeResourcesToZew(resources, zew);
		});
	}

	private void writeResourcesToZew(Map<String, Resource> resources, ZipEntryWriter zew) {
		for (Entry<String, Resource> entry : resources.entrySet()) {
			String relativePath = entry.getKey();
			Resource resource = entry.getValue();

			zew.writeZipEntry(relativePath, wb -> {
				wb.withName(relativePath) //
						.fromInputStreamFactory(resource::openStream);
			});
		}
	}

	private void transferResource(Resource resource) {
		String[] nameParts = resource.getName().split("\\.");
		String entryName = partName(nameParts[0], nameParts[1]);

		zew.writeZipEntry(entryName, wb -> {
			wb.fromInputStreamFactory(resource::openStream);
		});
	}

	/////////////

	private String partName(String classifier, String type) {
		Objects.requireNonNull(type, "Type used for part file identification must not be null!");

		StringBuilder builder = new StringBuilder();
		builder.append(pathPrefix);
		if (!StringTools.isEmpty(classifier)) {
			builder.append('-');
			builder.append(classifier);
		}

		builder.append('.');
		builder.append(type);

		return builder.toString();
	}

}
