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
package tribefire.extension.artifact.management.processing;

import static com.braintribe.console.ConsoleOutputs.brightBlack;
import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.console.ConsoleOutputs.yellow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.common.attribute.common.CallerEnvironment;
import com.braintribe.common.lcd.Pair;
import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.output.ConfigurableConsoleOutputContainer;
import com.braintribe.console.output.ConsoleOutput;
import com.braintribe.devrock.env.api.DevEnvironment;
import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.devrock.mc.api.declared.DeclaredGroupExtractionContext;
import com.braintribe.devrock.mc.api.declared.DeclaredGroupExtractionContextBuilder;
import com.braintribe.devrock.mc.api.download.PartEnricher;
import com.braintribe.devrock.mc.api.download.PartEnrichingContext;
import com.braintribe.devrock.mc.api.event.EntityEventListener;
import com.braintribe.devrock.mc.api.event.EventBroadcasterAttribute;
import com.braintribe.devrock.mc.api.event.EventHub;
import com.braintribe.devrock.mc.api.repository.HttpUploader;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.mc.api.resolver.PartAvailabilityReflection;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.commons.ArtifactTreePrinter;
import com.braintribe.devrock.mc.core.commons.DownloadMonitor;
import com.braintribe.devrock.mc.core.declared.group.DeclaredGroupExtractor;
import com.braintribe.devrock.mc.core.wirings.backend.contract.ArtifactDataBackendContract;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.DevelopmentEnvironmentContract;
import com.braintribe.devrock.mc.core.wirings.env.configuration.EnvironmentSensitiveConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.resolver.ArtifactDataResolverModule;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.mc.core.event.OnPartDownloadProcessed;
import com.braintribe.devrock.model.mc.core.event.OnPartDownloaded;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.gm.model.reason.essential.ParseError;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.consumable.PartEnrichment;
import com.braintribe.model.artifact.consumable.PartReflection;
import com.braintribe.model.artifact.declared.DeclaredGroup;
import com.braintribe.model.artifact.declared.License;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.processing.service.api.OutputConfig;
import com.braintribe.model.processing.service.api.OutputConfigAspect;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.model.version.FuzzyVersion;
import com.braintribe.model.version.HasMajorMinor;
import com.braintribe.model.version.Version;
import com.braintribe.utils.DOMTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.utils.paths.UniversalPath;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

import tribefire.extension.artifact.management.api.model.data.ArtifactVersions;
import tribefire.extension.artifact.management.api.model.data.RepositoryRepairData;
import tribefire.extension.artifact.management.api.model.request.ArtifactManagementRequest;
import tribefire.extension.artifact.management.api.model.request.DownloadArtifacts;
import tribefire.extension.artifact.management.api.model.request.GetArtifactVersions;
import tribefire.extension.artifact.management.api.model.request.GetArtifactsVersions;
import tribefire.extension.artifact.management.api.model.request.GetGroupAnalysisData;
import tribefire.extension.artifact.management.api.model.request.RepairLocalRepository;
import tribefire.extension.artifact.management.api.model.request.UpdateGroupDependencies;
import tribefire.extension.artifact.management.api.model.request.UploadArtifacts;
import tribefire.extension.artifact.management.processing.upload.ArtifactVersioningProcessor;
import tribefire.extension.artifact.management.processing.upload.UploadProcessor;

/**
 * @author pit
 *
 */
public class ArtifactManagementProcessor extends AbstractDispatchingServiceProcessor<ArtifactManagementRequest, Object> {
	private VirtualEnvironment virtualEnvironment;
	
	@Configurable @Required
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}

	@Override
	protected void configureDispatching(DispatchConfiguration<ArtifactManagementRequest, Object> dispatching) {
		dispatching.register(UploadArtifacts.T, this::uploadArtifacts);
		dispatching.register(DownloadArtifacts.T, this::downloadArtifacts);
		dispatching.register(GetArtifactVersions.T, this::getArtifactVersions);
		dispatching.register(GetArtifactsVersions.T, this::getArtifactsVersions);
		dispatching.register(GetGroupAnalysisData.T, this::getGroupAnalysisData);
		dispatching.registerReasoned(UpdateGroupDependencies.T, this::updateGroupDependencies);
		dispatching.registerReasoned(RepairLocalRepository.T, this::repairLocalRepository);
	}

	private WireContext<ArtifactDataResolverContract> openWireContext(ServiceRequestContext context) {
		File devEnvRoot = context.findAttribute(DevEnvironment.class).map(DevEnvironment::getRootPath).orElse(null);
		
		return Wire.contextBuilder(ArtifactDataResolverModule.INSTANCE, EnvironmentSensitiveConfigurationWireModule.INSTANCE) //
				.bindContract(DevelopmentEnvironmentContract.class, () -> devEnvRoot) //
				.bindContract(VirtualEnvironmentContract.class, () -> virtualEnvironment) //
				.build();
	}
	
	private class StatefulDownloadArtifacts {
		
		private DownloadArtifacts request;
		private TransitiveResolverContract contract;
		private List<CompiledDependencyIdentification> terminals;
		private int partCount;
		private ServiceRequestContext context;
		private AnalysisArtifactResolution resolution;
		private File targetPath;
		
		public StatefulDownloadArtifacts(ServiceRequestContext context, DownloadArtifacts request) {
			super();
			this.context = context;
			this.request = request;
		}
		
		private WireContext<TransitiveResolverContract> openDownloadWireContext() {
			File devEnvRoot = context.findAttribute(DevEnvironment.class).map(DevEnvironment::getRootPath).orElse(null);
			
			return Wire.contextBuilder(TransitiveResolverWireModule.INSTANCE, EnvironmentSensitiveConfigurationWireModule.INSTANCE) //
					.bindContract(DevelopmentEnvironmentContract.class, () -> devEnvRoot) //
					.bindContract(VirtualEnvironmentContract.class, () -> virtualEnvironment) //
					.build();
		}
		
		public void process() {
			validate();
			
			try (WireContext<TransitiveResolverContract> wireContext = openDownloadWireContext()) {
				contract = wireContext.contract();
				
				
				resolveDependenciesAndParts();
				copyParts();
			}
		}
		
		private void validate() {
			targetPath = CallerEnvironment.resolveRelativePath(request.getPath());
			terminals = request.getArtifacts().stream().map(CompiledDependencyIdentification::parse).collect(Collectors.toList());
		}
		
		private void copyParts() {
			List<Part> copyFailedParts = new ArrayList<>();
			int i = 0;
			ConfigurableConsoleOutputContainer configurableSequence = null;
			
			for (AnalysisArtifact artifact: resolution.getSolutions()) {
				for (Part part: artifact.getParts().values()) {
					if (part.hasFailed()) 
						continue;

					File targetFile = ArtifactAddressBuilder.build().root(targetPath.getAbsolutePath()) //
						.versionedArtifact(artifact) //
						.part(part) //
						.toPath() //
						.toFile();
						
					Resource partResource = part.getResource();
					
					try {
						targetFile.getParentFile().mkdirs();
						try (InputStream in = partResource.openStream(); OutputStream out = new FileOutputStream(targetFile)) {
							IOTools.transferBytes(in, out);
						}
						i++;
					} catch (Exception e1) {
						copyFailedParts.add(part);
					}
					
					configurableSequence = ConsoleOutputs.configurableSequence();
					configurableSequence.resetPosition(true);
					
					int percent = i * 100 / partCount;
					
					String progressMessage = String.format("Copied %d%% (%d/%d) of artifact parts to target path " + targetPath.getAbsolutePath(), percent, i, partCount);
					configurableSequence.append(progressMessage);
					configurableSequence.append("\n");
					
					ConsoleOutputs.print(configurableSequence);
				}
			}
			
			if (configurableSequence != null) {
				configurableSequence.resetPosition(false);
				ConsoleOutputs.print(configurableSequence);
			}
		}

		private void resolveDependenciesAndParts() {
			ConsoleOutputs.println("Resolving Artifacts");
			EventHub eventHub = new EventHub();
			
			AttributeContext attributeContext = AttributeContexts.derivePeek() //
				.set(EventBroadcasterAttribute.class, eventHub) //
				.build(); //
			
			AttributeContexts.push(attributeContext);

			try (DownloadMonitor monitor = new DownloadMonitor(eventHub, false, 0, true)) {
				monitor.addPhase(ConsoleOutputs.text("Resolving Dependencies"));
				monitor.addPhase(ConsoleOutputs.text("Detecting Artifact Parts"));
				monitor.addPhase(ConsoleOutputs.text("Resolving Artifact Parts"));

				monitor.nextPhase();
				
				boolean transitive = request.getTransitive();
				
				// determine artifacts
				TransitiveResolutionContext resolutionContext = TransitiveResolutionContext.build() //
						.includeImportDependencies(transitive) //
						.includeParentDependencies(transitive) //
						.includeRelocationDependencies(transitive) //
						.includeStandardDependencies(transitive) //
						.dependencyFilter(new DownloadDependencyFilter(request.getScopes(), request.getIncludeOptional()))
						.done(); 
				
				resolution = contract.transitiveDependencyResolver().resolve(resolutionContext, terminals);
				monitor.nextPhase(resolution.getSolutions().size());
				
				// determine parts
				PartAvailabilityReflection partAvailabilityReflection = contract.dataResolverContract().partAvailabilityReflection();

				Map<AnalysisArtifact, List<PartEnrichment>> enrichments = new ConcurrentHashMap<>();
				
				AtomicInteger partCount = new AtomicInteger();
				
				resolution.getSolutions().parallelStream().forEach(a -> {
					List<PartReflection> partReflections = partAvailabilityReflection.getAvailablePartsOf(CompiledArtifactIdentification.from(a));
					
					Map<String, PartEnrichment> parts = new HashMap<>();
					
					for (PartReflection reflection: partReflections) {
						String partType = reflection.getType();
						
						if (HashPartFilter.INSTANCE.test(partType)) {
							continue;
						}
						
						PartEnrichment enrichment = PartEnrichment.T.create();
						enrichment.setClassifier(reflection.getClassifier());
						enrichment.setType(partType);
						enrichment.setMandatory(true);
						
						parts.put(PartIdentification.asString(enrichment), enrichment);
						
					}
					
					partCount.getAndUpdate(i -> i + parts.size());
					enrichments.put(a, new ArrayList<>(parts.values()));
					
					monitor.increasePhaseItems(1);
				});
				
				this.partCount = partCount.get();
				
				monitor.nextPhase(this.partCount);
				
				// download parts
				PartEnrichingContext enrichingContext = PartEnrichingContext.build() //
				.enrichingExpert(a -> {
					return enrichments.getOrDefault(a, Collections.emptyList());
				}).done(); 
				
				EntityEventListener<OnPartDownloadProcessed> listener = (c,e) -> monitor.increasePhaseItems(1); 
				
				eventHub.addListener(OnPartDownloadProcessed.T, listener);

				PartEnricher partEnricher = contract.dataResolverContract().partEnricher();
				partEnricher.enrich(enrichingContext, resolution);
				
				eventHub.removeListener(OnPartDownloadProcessed.T, listener);
				
				monitor.nextPhase();
				
			}
			finally {
				AttributeContexts.pop();
			}

			ConsoleOutputs.println("Downloaded artifacts with parts");
			
			ArtifactTreePrinter artifactTreePrinter = new ArtifactTreePrinter();
			artifactTreePrinter.setOutputParts(true);
			artifactTreePrinter.setOutputLicense(request.getLicenseInfo());
			artifactTreePrinter.printDependencyTree(resolution);
		}
	}
	
	private Neutral downloadArtifacts(ServiceRequestContext context, DownloadArtifacts request) {
		new StatefulDownloadArtifacts(context, request).process(); 
		return Neutral.NEUTRAL;
	}

	
	
	/**
	 * uploader 
	 * @param context - the {@link ServiceRequestContext}
	 * @param request - the {@link UploadArtifacts} request
	 * @return - a {@link Neutral}, aka nothing as an object
	 */
	private Neutral uploadArtifacts(ServiceRequestContext context, UploadArtifacts request) {
		File workingDirectory = CallerEnvironment.resolveRelativePath(request.getPath());

		String repoId = request.getRepoId();
		boolean update = request.getUpdate();
				
		List<File> detectedPoms = enumeratePoms(workingDirectory);
		
		try (WireContext<ArtifactDataResolverContract> wireContext = openWireContext(context)) {

			HttpUploader httpUploader = wireContext.contract(ArtifactDataBackendContract.class).httpUploader();
			RepositoryReflection repositoryReflection = wireContext.contract().repositoryReflection();
			
			UploadProcessor uploadProcessor = new UploadProcessor();
			uploadProcessor.setPoms(detectedPoms);
			uploadProcessor.setRepositoryId(repoId);
			uploadProcessor.setRepositoryReflection(repositoryReflection);
			uploadProcessor.setUploader(httpUploader);
			uploadProcessor.setUpdate(update);
			uploadProcessor.execute();
		}
		
		return Neutral.NEUTRAL;
	}
	

	private List<File> enumeratePoms( File directory) {
		ConsoleOutputs.println("Detecting artifact descriptors in directory " + directory.getAbsolutePath());
		List<File> files = new ArrayList<>();
		enumeratePoms(directory, files);
		ConsoleOutputs.println("Detected " + files.size() + " artifact descriptor(s)");
		return files;
	}
	
	private void enumeratePoms( File directory, List<File> result) {
		File [] contents = directory.listFiles();
		if (contents == null || contents.length == 0)
			return;
		for (File file : contents) {
			if (file.isDirectory()) {
				enumeratePoms( file, result);
			}
			else {
				if (file.getName().endsWith( ".pom")) {
					result.add( file);
				}
			}
		}
	}
	
	/**
	 * retrieves all versions of a single artifact 
	 * @param context - the {@link ServiceRequestContext}
	 * @param request - the {@link GetArtifactVersions} request
	 * @return - a single {@link ArtifactVersions}
	 */
	private ArtifactVersions getArtifactVersions(ServiceRequestContext context, GetArtifactVersions request) {
		try (WireContext<ArtifactDataResolverContract> wireContext = openWireContext(context)) {
			
			String expression = request.getArtifact();
			
			ArtifactVersions artifactVersions = resolveVersions(wireContext, expression, false);
			
			return artifactVersions;
		}
	}

	/**
	 * retrieves all versions of multiple artifacts
	 * @param context - the {@link ServiceRequestContext}
	 * @param request - he {@link GetArtifactsVersions} request
	 * @return - a {@link List} of {@link ArtifactVersions}, one per entry in the request
	 */
	private List<ArtifactVersions> getArtifactsVersions(ServiceRequestContext context, GetArtifactsVersions request) {
		try (WireContext<ArtifactDataResolverContract> wireContext = openWireContext(context)) {
			
			List<String> artifacts = request.getArtifacts();
			List<ArtifactVersions> artifactVersionsList = new ArrayList<>(artifacts.size());
			
			for (String expression: artifacts) {
				artifactVersionsList.add(resolveVersions(wireContext, expression, true));
			}
			
			return artifactVersionsList;
		}
	}

	private ArtifactVersions resolveVersions(WireContext<ArtifactDataResolverContract> wireContext, String expression, boolean outputIdentification) {
		final List<Version> versions;
		final ArtifactIdentification ai;
		
		if (expression.contains("#")) {
			CompiledDependencyIdentification cdi = CompiledDependencyIdentification.parse(expression);
			ai = cdi;
			versions = ArtifactVersioningProcessor.getArtifactVersions(cdi, wireContext.contract().artifactResolver());
		}
		else {
			ai = ArtifactIdentification.parse(expression);
			versions = ArtifactVersioningProcessor.getArtifactVersions(ai, wireContext.contract().artifactResolver());
		}
		
		List<String> versionsAsStrings = versions.stream().map(Version::asString).collect(Collectors.toList());
		
		// output result in protocol
		if (outputIdentification)
			ConsoleOutputs.println(ConsoleOutputs.cyan(expression));
		
		// output result in protocol
		for (String version: versionsAsStrings) {
			if (outputIdentification)
				ConsoleOutputs.print("  ");
			
			ConsoleOutputs.println(version);
		}
		
		ArtifactVersions artifactVersions = ArtifactVersions.T.create();
		
		artifactVersions.setGroupId(ai.getGroupId());
		artifactVersions.setArtifactId(ai.getArtifactId());
		artifactVersions.setVersionsAsStrings(versionsAsStrings);
		artifactVersions.setVersions(versions);
		return artifactVersions;
	}
	
	// ***************************************************************************************************
		// Update Group Dependencies
		// ***************************************************************************************************
		
		private Maybe<Neutral> updateGroupDependencies(ServiceRequestContext context, UpdateGroupDependencies request) {
			// Directory determination and validation
			File groupFolderDir = CallerEnvironment.resolveRelativePath(request.getGroupFolder());
			File parentPomFile = UniversalPath.from(groupFolderDir).push("parent").push("pom.xml").toFile();
			String parentPomAbsolutePath = parentPomFile.getAbsolutePath();
			
			if (!parentPomFile.exists()) {
				return Reasons.build(NotFound.T).text(parentPomAbsolutePath + " not found.").toMaybe();
			}
			
			println(
				sequence(
					text("Found parent pom in "),
					yellow(groupFolderDir.getAbsolutePath())
					)
				);
			
			println("Updating group dependencies...\n");
			
			Maybe<Document> documentMaybe = readParentPom(parentPomFile);
			if (documentMaybe.isUnsatisfied()) 
				return Maybe.empty(documentMaybe.whyUnsatisfied());

			Document document = documentMaybe.get();
			
			// Try to set new version and collect missing groups, if any 
			Map<String,Element> groupVersionElements = indexGroupVersionElements(document);
			List<String> missingGroups = new ArrayList<>(); 
			
			Map<String, String> groupDependencies = request.getGroupDependencies();
			for (Map.Entry<String, String> entry : groupDependencies.entrySet()) {
				String groupId = entry.getKey();
				Element element = groupVersionElements.get(groupId);
				
				if (element == null) {
					missingGroups.add(groupId);
					continue;
				}
				
				String oldVersion = element.getTextContent();
				// Set new version
				String version = mavenCanonizeVersionExpression(entry.getValue());
				element.setTextContent(version);
				
				println(
					sequence(
						text("Updated "),
						brightBlack(groupId),
						text(" from "),
						yellow(oldVersion),
						text(" to "),
						yellow(version)
						)
					);
			}
			
			// Handle missing groups
			if (request.getFailOnMissingGroupsInParent() && !missingGroups.isEmpty()) {
				return Reasons.build(NotFound.T).text("Missing groups in parent pom: " + missingGroups.stream().collect(Collectors.joining(", "))).toMaybe();
			}
			
			// Write document with updated values
			try {
				String content = StringTools.normalizeLineSeparatorsInTextFileString(DOMTools.toString(document), null);
				FileTools.writeStringToFile(parentPomFile, content);
			} catch (Exception e) {
				throw Exceptions.unchecked(e);
			}
			
			return Maybe.complete(Neutral.NEUTRAL);
		}

		private String mavenCanonizeVersionExpression(String versionExpression) {
			if (versionExpression.endsWith("~")) {
				HasMajorMinor majorMinorVersion = Version.parse(versionExpression.substring(0, versionExpression.length()-1));
				return FuzzyVersion.from(majorMinorVersion).asString();
			} 

			return versionExpression;
		}

		private Maybe<Document> readParentPom(File file) {
			try {
				return Maybe.complete(DomParser.load().from(file));
			} catch (DomParserException e) {
				return Reasons.build(ParseError.T) //
				.text("Could not parse " + file.getAbsolutePath()) //
				.cause(InternalError.from(e)).toMaybe();
			} catch (Exception e) {
				throw Exceptions.unchecked(e);
			}
		}
		
		private Map<String, Element> indexGroupVersionElements(Document document) {
			Map<String,Element> groupVersionElements = new HashMap<>();
			
			NodeList propertiesElements = document.getDocumentElement().getElementsByTagName("properties");
			
			if (propertiesElements.getLength() == 0)
				return groupVersionElements;
			
			Node currentNode = propertiesElements.item(0).getFirstChild();
			
			while (currentNode != null) {
				try {
					if (currentNode.getNodeType() != Node.ELEMENT_NODE) {
						continue;
					}
					
					Element element = (Element) currentNode;
					String tagName = element.getTagName();
					if (tagName.startsWith("V.")) {
						String groupName = tagName.substring(2);
						groupVersionElements.put(groupName, element);
					}
					
				} finally {
					currentNode = currentNode.getNextSibling();
				}
			}
			
			return groupVersionElements;
		}
		
		/**
		 * processor function for the group analysis
		 * @param context - the {@link ServiceRequestContext}
		 * @param request - the {@link GetGroupAnalysisData} request
		 * @return - the {@link DeclaredGroup}
		 */
		private DeclaredGroup getGroupAnalysisData(ServiceRequestContext context, GetGroupAnalysisData request) {
			
			DeclaredGroupExtractor extractor = new DeclaredGroupExtractor();
			
			DeclaredGroupExtractionContextBuilder builder = DeclaredGroupExtractionContext.build().location( request.getGroupLocation());
			if (request.getSort()) {
				builder.sort( true);
			}
			if (request.getIncludeFilterExpression() != null) {
				builder.inclusions(request.getIncludeFilterExpression());
			}
			if (request.getExcludeFilterExpression() != null) {
				builder.exclusions( request.getExcludeFilterExpression());
			}
			if (request.getRequireRanges()) {
				builder.enforceRanges(true);
			}
			if (request.getSimplifyOutput()) {
				builder.simplifyRange(true);
			}
			if (request.getIncludeSelfreferences()) {
				builder.includeSelfreferences(true);
			}
					
			DeclaredGroupExtractionContext extractionContext = builder.done();
			
			Maybe<DeclaredGroup> maybe = extractor.extractGroup(extractionContext);
					
			boolean verbose = context.getAspect(OutputConfigAspect.class, OutputConfig.empty).verbose();
			
			DeclaredGroup declaredGroup;
			if (maybe.hasValue()) {
				declaredGroup = maybe.value();
			}
			else {
				// if no value is found, the maybe is unsatisfied, 
				// but we need to create a return value
				declaredGroup = DeclaredGroup.T.create();
				declaredGroup.setFailure(maybe.whyUnsatisfied());	
			}
			
			// output 
			if (verbose) {
				ConfigurableConsoleOutputContainer oc = ConsoleOutputs.configurableSequence(); 
				if (maybe.hasValue()) {		
					if (maybe.isIncomplete()) {				
						putToConsole( oc, request, declaredGroup);							
					}
					else if (maybe.isSatisfied()) {				
						putToConsole( oc, request, declaredGroup);
					}
				}
				else {
					// failed						
					putToConsole( oc, declaredGroup.getFailure(), true);
				}
				// produce 
				ConsoleOutputs.println( oc);
			}
									
			return declaredGroup;
		}
		
		/**
		 * console logger for the getGroupAnalysisData
		 * @param oc - the {@link ConfigurableConsoleOutputContainer}
		 * @param request - the {@link GetGroupAnalysisData} request (as a context)
		 * @param declaredGroup - the {@link DeclaredGroup} data 
		 */
		private void putToConsole(ConfigurableConsoleOutputContainer oc, GetGroupAnalysisData request, DeclaredGroup declaredGroup) {
			// group id
			oc.append("analysis of group : " + declaredGroup.getGroupId() + "\n");
			// version
			oc.append( "\tNon-revisionend version :" + declaredGroup.getMajor() + "." + declaredGroup.getMinor() + "\n");
		
			// location
			oc.append( "\tlocation : " + declaredGroup.getLocation() +"\n");
			
			// failure
			if (declaredGroup.hasFailed()) {			
				putToConsole(oc, declaredGroup.getFailure(), false);
			}
						
			// map < group > : <version>
			oc.append("\treferences to other groups: " + declaredGroup.getGroupDependencies().size() + "\n");
			
			for (Map.Entry<String,String> entry : declaredGroup.getGroupDependencies().entrySet()) {
				oc.append("\t\t" + entry.getKey() + " -> " + entry.getValue() + "\n");
			}
				
		}

		/**
		 * @param oc - the {@link ConfigurableConsoleOutputContainer}
		 * @param offset - the offset 
		 * @param whyUnsatisfied - the {@link Reason}
		 * @param failed - true it's a major issue 
		 */
		private void putToConsole(ConfigurableConsoleOutputContainer oc, Reason whyUnsatisfied, boolean fatal) {
			if (fatal)
				oc.append( ConsoleOutputs.red( whyUnsatisfied.asFormattedText() +"\n"));
			else
				oc.append( ConsoleOutputs.yellow( whyUnsatisfied.asFormattedText() +"\n"));
		}
		
		/**
		 * @param context - the {@link ServiceRequestContext}
		 * @param request - the {@link RepairLocalRepository} request - currently no more than a marker
		 * @return - a {@link Maybe} of the {@link RepositoryRepairData} as reported from the purger
		 */
		private Maybe<RepositoryRepairData> repairLocalRepository(ServiceRequestContext context, RepairLocalRepository request) {
			try (WireContext<ArtifactDataResolverContract> wireContext = openWireContext(context)) {
				String localRepositoryPath = wireContext.contract().repositoryReflection().getRepositoryConfiguration().getLocalRepositoryPath();
				FilesystemLockPurger purger = new FilesystemLockPurger();
				purger.setRepositoryRoot(localRepositoryPath);
				Maybe<Pair<Integer,List<File>>> retval = purger.purgeFilesytemLockFiles();
				boolean verbose = context.getAspect(OutputConfigAspect.class, OutputConfig.empty).verbose();
				
				Maybe<RepositoryRepairData> mrData;
				
				if (retval.isSatisfied()) {
					Pair<Integer, List<File>> pair = retval.get();					
					RepositoryRepairData rdata = RepositoryRepairData.T.create();
					rdata.setLocalRepositoryPath(localRepositoryPath);
					if (pair.first != null) {
						rdata.setNumberOfCleanedLockFiles( pair.first);
					}
					rdata.setCleanedLockFiles(pair.second.stream().map(f -> f.getAbsolutePath()).collect(Collectors.toList()));
					mrData = Maybe.complete( rdata);
				}
				else {
					mrData =  retval.whyUnsatisfied();
				}
				
				if (verbose) {
					ConfigurableConsoleOutputContainer oc = ConsoleOutputs.configurableSequence();
					putToConsole( oc, mrData);
				}
				return mrData;
			}			
		}
		
		/**
		 * output of the {@link RepositoryRepairData} as produced by {@link #repairLocalRepository(ServiceRequestContext, RepairLocalRepository)}
		 * @param oc - the {@link ConfigurableConsoleOutputContainer}
		 * @param rdata - the {@link RepositoryRepairData} to display
		 */
		private void putToConsole( ConfigurableConsoleOutputContainer oc, RepositoryRepairData rdata) {
			oc.append("Successfully repaired local repository at :" + rdata.getLocalRepositoryPath() + "\n");
			// num of files
			oc.append( "\tCleared lockfiles :" + rdata.getNumberOfCleanedLockFiles());
			// list files
			if (rdata.getCleanedLockFiles().size() > 0) {
				for (String cleaned : rdata.getCleanedLockFiles()) {
					oc.append( "\t\t" + cleaned);
				}
			}
		}
		
		
		/**
		 * output of the direct return value of {@link #repairLocalRepository(ServiceRequestContext, RepairLocalRepository)}
		 * @param oc - the {@link ConfigurableConsoleOutputContainer}
		 * @param maybe - the {@link Maybe} of {@link RepositoryRepairData}
		 */
		private void putToConsole( ConfigurableConsoleOutputContainer oc, Maybe<RepositoryRepairData> maybe) {
						
			if (maybe.hasValue()) {		
				if (maybe.isIncomplete()) {				
					putToConsole( oc, maybe.whyUnsatisfied(), false);							
				}
				else if (maybe.isSatisfied()) {				
					putToConsole( oc, maybe.get());
				}
			}
			else {
				// failed						
				putToConsole( oc, maybe.whyUnsatisfied(), true);
			}
			// produce 
			ConsoleOutputs.println( oc);
			
		}

}
