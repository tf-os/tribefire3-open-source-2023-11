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
package tribefire.extension.modelling.processing.management;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.artifact.processing.ArtifactIdentification;
import com.braintribe.model.artifact.processing.ArtifactResolution;
import com.braintribe.model.artifact.processing.QualifiedPartIdentification;
import com.braintribe.model.artifact.processing.ResolvedArtifact;
import com.braintribe.model.artifact.processing.ResolvedArtifactPart;
import com.braintribe.model.artifact.processing.service.data.ArtifactPartData;
import com.braintribe.model.artifact.processing.service.request.GetArtifactPartData;
import com.braintribe.model.artifact.processing.service.request.ResolveArtifactDependencies;
import com.braintribe.model.cortexapi.access.collaboration.CollaborativeStageData;
import com.braintribe.model.cortexapi.access.collaboration.GetCollaborativeStageData;
import com.braintribe.model.cortexapi.access.collaboration.PushCollaborativeStage;
import com.braintribe.model.cortexapi.access.collaboration.RenameCollaborativeStage;
import com.braintribe.model.cortexapi.access.collaboration.ResetCollaborativePersistence;
import com.braintribe.model.deploymentapi.request.Redeploy;
import com.braintribe.model.generic.mdec.ModelDeclaration;
import com.braintribe.model.generic.reflection.ConfigurableCloningContext;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.jvm.reflection.ModelDeclarationParser;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.accessrequest.api.AbstractStatefulAccessRequestProcessor;
import com.braintribe.model.processing.itw.analysis.JavaTypeAnalysis;
import com.braintribe.model.processing.manipulation.marshaller.ManMarshaller;
import com.braintribe.model.processing.manipulation.parser.api.MutableGmmlParserConfiguration;
import com.braintribe.model.processing.manipulation.parser.impl.Gmml;
import com.braintribe.model.processing.manipulation.parser.impl.ManipulatorParser;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.classloader.ReverseOrderURLClassLoader;
import com.braintribe.utils.lcd.CollectionTools2;
import com.braintribe.utils.paths.UniversalPath;

import tribefire.extension.modelling.commons.ModellingConstants;
import tribefire.extension.modelling.management.ModellingProject;
import tribefire.extension.modelling.management.api.ModellingManagementResponse;
import tribefire.extension.modelling.management.api.RebaseProject;
import tribefire.extension.modelling.processing.tools.ModellingTools;

public class RebaseProjectProcessor
		extends AbstractStatefulAccessRequestProcessor<RebaseProject, ModellingManagementResponse>
		implements ModellingConstants {

	private ModellingManagementProcessorConfig config;
	private File contextualizedTempDir;
	private boolean trunkExists;
	
	private String pAccessId;
	
	private JavaTypeAnalysis jta = new JavaTypeAnalysis();
	private Map<String, GmMetaModel> modelsByName = new HashMap<>();
	
	public RebaseProjectProcessor(ModellingManagementProcessorConfig config) {
		this.config = config;
	}
	
	@Override
	public ModellingManagementResponse process() {
		ModellingProject project = request().getProject();
		pAccessId = project.getAccessId();
		
		// prepare temp directory
		File tempDir = config.getTempDir();
		contextualizedTempDir = UniversalPath.from(tempDir) //
				.push("rebasing") //
				.push(pAccessId + "-" + UUID.randomUUID().toString()).toFile();

		contextualizedTempDir.mkdirs();
		
		
		trunkExists = ModellingTools.trunkExists(pAccessId, session());
		// TODO may be empty?
		if (trunkExists)
			preserveTrunkState(pAccessId, tempDir);
		
		// remove stages from access
		ResetCollaborativePersistence reset = ResetCollaborativePersistence.T.create();
		reset.setServiceId(pAccessId);
		reset.eval(session()).get();
		
		// redeploy the access
		Redeploy redeploy = Redeploy.T.create();
		redeploy.setExternalIds(CollectionTools2.asSet(pAccessId));
		
		redeploy.eval(session()).get();

		
		// resolve assets and filter for models
		List<ResolvedArtifact> models = filterModels(project.getAssets());
		Map<QualifiedPartIdentification, Resource> modelJars = getModelJars(models); // TODO Retry if UnknownHost
		
		if(!modelJars.isEmpty())
			recordModelManipulationsToSession(modelJars);
		
		
		// rename current trunk stage to the name of the asset to be closed
		RenameCollaborativeStage renameStage = RenameCollaborativeStage.T.create();
		renameStage.setNewName(STAGE_NAME_MODELS);
		renameStage.setOldName("trunk");
		renameStage.setServiceId(pAccessId);
		renameStage.eval(session()).get();
		
		// create new trunk stage
		PushCollaborativeStage pushStage = PushCollaborativeStage.T.create();
		pushStage.setName("trunk");
		pushStage.setServiceId(pAccessId);
		pushStage.eval(session()).get();
		
		
		// bring back trunk data
		if (contextualizedTempDir != null) {
			File dataFile = new File(contextualizedTempDir, "data.man");

			if (dataFile.exists())
				createTrunkData(dataFile, session());
			
			// remove temporary trunk files
			try {
				FileTools.deleteDirectoryRecursively(contextualizedTempDir);
			} catch (IOException e) {
				throw Exceptions.unchecked(e, "Error while deleting temp directory " + contextualizedTempDir.getAbsolutePath());
			}
		}
		
		
		// return response
		return null;
	}

	/**
	 * Record model manipulations into the session, because in the end we want a trunk
	 * stage which holds these manipulations to be closed.
	 */
	private void recordModelManipulationsToSession(Map<QualifiedPartIdentification, Resource> modelJars) {
		Map<String, ModelDeclaration> modelDeclarations = new HashMap<>();
		
		File libs = new File(contextualizedTempDir, "libs");
		libs.mkdirs();
		
		// create URLs to jars for later classloader processing
		URL[] urls = new URL[modelJars.size()];
		int i = 0;
		
		for (Entry<QualifiedPartIdentification, Resource> entry : modelJars.entrySet()) {
			String modelName = entry.getKey().getArtifactId() + "-" + entry.getKey().getVersion() + ".jar";
			File lib = new File(libs, modelName);

			try {
				ModellingTools.transferResource(entry.getValue(), lib);
				
				urls[i] = lib.toURI().toURL();
				i++;
			} catch (IOException e) {
				throw Exceptions.unchecked(e, "Error while transferring model jar " + lib, UncheckedIOException::new);
			}
		}
		
		
		// get model-declaration.xml files
		Enumeration<URL> declarationUrls;
		List<GmMetaModel> models = new ArrayList<>();
			
		try(URLClassLoader mdClassLoader = new URLClassLoader(urls, null)) {
			declarationUrls = mdClassLoader.getResources("model-declaration.xml");
			
			while (declarationUrls.hasMoreElements()) {
				URL url = declarationUrls.nextElement();
				
				try (InputStream in = url.openStream()) {
					ModelDeclaration modelDeclaration = ModelDeclarationParser.parse(in);
					modelDeclarations.putIfAbsent(modelDeclaration.getName(), modelDeclaration);
				}
			}
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while reading model declaration.");
		}
			
		
		try(ReverseOrderURLClassLoader classLoader = new ReverseOrderURLClassLoader(urls, getClass().getClassLoader())) {
			jta.setClassLoader(classLoader);
			
			// get meta-model instances from model declarations
			for (ModelDeclaration modelDeclaration : modelDeclarations.values()) {
				GmMetaModel model = analyzeModel(modelDeclaration, classLoader);
				models.add(model);
			}
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while reading model declaration.");
		}
		
		
		// clone models into session
		PersistenceGmSession accessSession = config.sessionFactory().newSession(pAccessId);
		ConfigurableCloningContext cc = ConfigurableCloningContext.build().supplyRawCloneWith(accessSession).done();
		models.forEach(m -> m.clone(cc));
		
		accessSession.commit();
		
	}

	private void preserveTrunkState(String projectAccessId, File tempDir) {
		// preserve trunk stage
		GetCollaborativeStageData getTrunkData = GetCollaborativeStageData.T.create();
		getTrunkData.setName("trunk");
		getTrunkData.setServiceId(projectAccessId);

		CollaborativeStageData trunkData = getTrunkData.eval(session()).get();
		Resource dataMan = trunkData.getDataResource();
		
		if (dataMan != null) {
			File dataFile = new File(contextualizedTempDir, "data.man");
			ModellingTools.transferResource(dataMan, dataFile);
		}
	}
	
	//
	// Helpers and Convenience
	//
	
	/**
	 * Marshals manipulations from the file and transfers them into the session.
	 * 
	 * @param file - Either data or model manipulation file
	 */
	private void createTrunkData(File file, PersistenceGmSession session) {
		try (OutputStream out = new FileOutputStream(file)) {
			ManMarshaller manMarshaller = new ManMarshaller();
			manMarshaller.marshall(out, System.out);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		MutableGmmlParserConfiguration configuration = Gmml.configuration();
		configuration.setLenient(true);
		configuration.setParseSingleBlock(true);
		
		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
			ManipulatorParser.parse(reader, session, configuration);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		session.commit();
	}
	
	private Map<QualifiedPartIdentification, Resource> getModelJars(List<ResolvedArtifact> models) {
		Map<QualifiedPartIdentification, Resource> modelJars = new LinkedHashMap<>();
		for(ResolvedArtifact model : models) {
			
			QualifiedPartIdentification pi = QualifiedPartIdentification.T.create();
			pi.setArtifactId(model.getArtifactId());
			pi.setGroupId(model.getGroupId());
			pi.setVersion(model.getVersion());
			pi.setType("jar");

			String url = model.getParts().stream()
				.filter(p -> "jar".equals(p.getType()) && "".equals(p.getClassifier()))
				.findFirst()
				.map(ResolvedArtifactPart::getUrl)
				.orElse(null);
			
			if (url == null)
				continue;

			FileResource resource = FileResource.T.create();
			try {
				resource.setPath(new File(new URI(url)).getAbsolutePath());
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
			
			modelJars.put(pi, resource);
		}
		
		return modelJars;
	}

	private List<ResolvedArtifact> filterModels(List<String> assets) {
		 
		Set<EqProxy<ResolvedArtifact>> models = new HashSet<>();
		for (String asset : assets) {
			VersionedArtifactIdentification ai = VersionedArtifactIdentification.parse(asset);
			
			ArtifactResolution resolution = getArtifactResolution(ai);
			
			Set<ResolvedArtifact> modelsFromResolution = getModelsFromArtifactResolution(resolution);
			
			// manage identities before adding
			modelsFromResolution.forEach(m -> models.add(ModellingTools.artifactComparator.eqProxy(m)));
		}
		
		return models.stream().map(Supplier::get).collect(Collectors.toList());
	}
	
	private ArtifactResolution getArtifactResolution(VersionedArtifactIdentification ai) {
		ResolveArtifactDependencies resolve = ResolveArtifactDependencies.T.create();
		
		ArtifactIdentification artifact = ArtifactIdentification.T.create();
		artifact.setGroupId(ai.getGroupId());
		artifact.setArtifactId(ai.getArtifactId());
		artifact.setVersion(ai.getVersion());
		
		resolve.setArtifact(artifact);
		resolve.setRepositoryConfigurationName(config.getRepositoryConfigurationName());
		
		ArtifactResolution resolution = null;
		try {
			resolution = resolve.eval(session()).get();
			
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not resolve [" + artifact + "]", e);
		}
		
		return resolution;
	}
	
	private Set<ResolvedArtifact> getModelsFromArtifactResolution(ArtifactResolution resolution) {
		Set<ResolvedArtifact> models = new HashSet<>();
		
		collectModels(resolution.getResolvedArtifact(), models);
		
		return models;
	}
	
	private void collectModels(ResolvedArtifact resolvedArtifact, Set<ResolvedArtifact> models) {
		if (!models.add(resolvedArtifact))
			return;
		
		if (resolvedArtifact.getArtifactId().equals("root-model") && resolvedArtifact.getGroupId().equals("com.braintribe.gm"))
			return;
		
		for (ResolvedArtifact dep: resolvedArtifact.getDependencies())
			collectModels(dep, models);
	}

	private GmMetaModel analyzeModel(ModelDeclaration modelDeclaration, ClassLoader classLoader) {
		
		String modelName = modelDeclaration.getName();
		GmMetaModel model = modelsByName.computeIfAbsent(modelName, k -> GmMetaModel.T.create());
		model.setGlobalId("model:" + modelDeclaration.getName());

		model.setName(modelName);
		model.setVersion(modelDeclaration.getVersion());

		List<GmMetaModel> dependencies = model.getDependencies();
		
		for (ModelDeclaration dependency: modelDeclaration.getDependencies()) {
			GmMetaModel modelDependency = modelsByName.computeIfAbsent(dependency.getName(), k -> GmMetaModel.T.create());
			dependencies.add(modelDependency);
			
			modelDependency.setGlobalId("model:" + dependency.getName());
		}

		Set<GmType> types = model.getTypes();

		try {
			if(modelName.equals(NAME_ROOT_MODEL)) {
				GmType gmBaseType = jta.getGmType(Object.class);
				types.add(gmBaseType);
				gmBaseType.setDeclaringModel(model);
	
				for (SimpleType simpleType : SimpleTypes.TYPES_SIMPLE) {
					GmType gmSimpleType = jta.getGmType(simpleType.getJavaType());
					types.add(gmSimpleType);
					gmSimpleType.setDeclaringModel(model);
				}
			}
			
			for (String typeName : modelDeclaration.getTypes()) {
				Class<?> type = Class.forName(typeName, false, classLoader);
				
				GmType gmType = jta.getGmType(type);

				gmType.setDeclaringModel(model);

				types.add(gmType);
			}
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while analyzing classpath model " + modelDeclaration.getName());
			
		}

		return model;
	}
}
