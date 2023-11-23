// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl;

import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.build.cmd.assets.api.PlatformAssetCollector;
import com.braintribe.build.cmd.assets.api.PlatformAssetDistributionContext;
import com.braintribe.build.cmd.assets.impl.modules.ModuleCollector;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.common.lcd.Pair;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.mdec.ModelDeclaration;
import com.braintribe.model.jvm.reflection.ModelDeclarationParser;
import com.braintribe.model.meta.GmCollectionType;
import com.braintribe.model.meta.GmCustomModelElement;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.itw.analysis.JavaTypeAnalysis;
import com.braintribe.model.processing.manipulation.marshaller.ManInstanceKind;
import com.braintribe.model.processing.manipulation.marshaller.ManInstantiationClassifier;
import com.braintribe.model.processing.manipulation.marshaller.ManMarshaller;
import com.braintribe.setup.tools.TfSetupOutputs;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.classloader.ReverseOrderURLClassLoader;

import tribefire.cortex.asset.resolving.ng.impl.PlatformAssetSolution;

public class ModelCollector implements PlatformAssetCollector {
	private final Map<String, Pair<File, PlatformAssetSolution>> modelJars = new HashMap<>();
	private static ManMarshaller manMarshaller = new ManMarshaller();
	
	public void addModelJar(String modelName, File modelJar, PlatformAssetSolution classifiedSolution) {
		modelJars.put(modelName, new Pair<>(modelJar, classifiedSolution));
	}

	private static class ModelRecordingContext {
		public JavaTypeAnalysis jta;
		public ClassLoader classLoader;
		public Map<String, ModelDeclaration> declarations = new HashMap<>();
		public PlatformAssetDistributionContext distributionContext;
				
		public ModelRecordingContext(PlatformAssetDistributionContext context, URL[] cp) {
		    classLoader = new ReverseOrderURLClassLoader(cp, getClass().getClassLoader());
			distributionContext = context;
			jta = new JavaTypeAnalysis();
			jta.setClassLoader(classLoader);
		}
		
	}
	
	@Override
	public void transfer(PlatformAssetDistributionContext context) {
		if (isModularSetup(context))
			return;
		
		URL[] cp = modelJars.values().stream().map(Pair::getFirst).map(FileTools::toURL).toArray(URL[]::new);
		
		ModelRecordingContext modelRecordingContext = new ModelRecordingContext(context, cp);
		
		println("Reading model-declaration.xml files from model dependencies");
		readModelDeclarations(modelRecordingContext);
		
		
		println("Translating model jars into manipulation primings");
		for (Map.Entry<String, Pair<File, PlatformAssetSolution>> entry: modelJars.entrySet()) {
			String modelName = entry.getKey();
			PlatformAssetSolution classifiedSolution = entry.getValue().getSecond();
			
			if (classifiedSolution.asset.getPlatformProvided())
				continue;
			
			ModelDeclaration modelDeclaration = modelRecordingContext.declarations.get(modelName);
			recordManipulations(modelRecordingContext, modelDeclaration, classifiedSolution);
		}
	}

	private boolean isModularSetup(PlatformAssetDistributionContext context) {
		return context.getCollector(ModuleCollector.class, ModuleCollector::new).isModularSetup();
	}

	private void readModelDeclarations(ModelRecordingContext context) {
		try {
			Enumeration<URL> declarationUrls = context.classLoader.getResources("model-declaration.xml");
			
			while (declarationUrls.hasMoreElements()) {
				URL url = declarationUrls.nextElement();
				
				try (InputStream in = url.openStream()) {
					ModelDeclaration modelDeclaration = ModelDeclarationParser.parse(in);
					context.declarations.put(modelDeclaration.getName(), modelDeclaration);
				}
			}
		}
		catch (IOException e) {
			throw Exceptions.unchecked(e, "Error while reading model declaration", UncheckedIOException::new);
		}
		
	}

	private void recordManipulations(ModelRecordingContext modelRecordingContext, ModelDeclaration modelDeclaration, PlatformAssetSolution classifiedSolution) {
		PlatformAsset asset = classifiedSolution.asset;
		PlatformAssetDistributionContext distributionContext = modelRecordingContext.distributionContext;
		String qualifiedAssetName = asset.qualifiedAssetName();
		
		File file = distributionContext.storageAccessDataStageFolder("cortex", qualifiedAssetName)
				.push("model.man")
				.toFile();
		
		println(
			sequence(
				text("  Translating model jar into manipulation primings: "),
				TfSetupOutputs.solution(classifiedSolution.solution)
			)
		);
		
		GmMetaModel model = analyzeModel(modelRecordingContext, modelDeclaration);
		
		file.getParentFile().mkdirs();
		
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
			// cloneModel(writer, model);
			writeModelMan(writer, model);
		}
		catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while writing model.man for " + qualifiedAssetName);
		}
	}
	
	private void writeModelMan(Writer writer, GmMetaModel model) {
		manMarshaller.marshall(writer, model, GmSerializationOptions.defaultOptions.derive() //
				.setOutputPrettiness(OutputPrettiness.high) //
				.stabilizeOrder(true) //
				.set(ManInstantiationClassifier.class, new ModelInstantiationClassifier(model)) //
				.build());
	}
	
	private GmMetaModel analyzeModel(ModelRecordingContext modelRecordingContext, ModelDeclaration modelDeclaration) {
		GmMetaModel model = GmMetaModel.T.create();
		model.setGlobalId("model:" + modelDeclaration.getName());

		model.setName(modelDeclaration.getName());
		model.setVersion(modelDeclaration.getVersion());

		List<GmMetaModel> dependencies = model.getDependencies();
		
		for (ModelDeclaration modelDeclarationDependency: modelDeclaration.getDependencies()) {
			GmMetaModel modelDependency = GmMetaModel.T.create();
			modelDependency.setName(modelDeclarationDependency.getName());
			dependencies.add(modelDependency);
			
			modelDependency.setGlobalId("model:" + modelDeclarationDependency.getName());
		}

		Set<GmType> types = model.getTypes();

		try {
			for (String typeName : modelDeclaration.getTypes()) {
				Class<?> type = Class.forName(typeName, false, modelRecordingContext.classLoader);
				
				GmType gmType = modelRecordingContext.jta.getGmType(type);

				gmType.setDeclaringModel(model);

				types.add(gmType);
			}
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while analyzing classpath model " + modelDeclaration.getName());
			
		}

		return model;
	}
	
	private static class ModelInstantiationClassifier implements Function<GenericEntity, ManInstanceKind> {
		
		private final GmMetaModel targetModel;
		
		public ModelInstantiationClassifier(GmMetaModel targetModel) {
			super();
			this.targetModel = targetModel;
		}

		@Override
		public ManInstanceKind apply(GenericEntity entity) {
			boolean isUndeclaredInstance = false;
			
			GmMetaModel declaringModel = null;
			
			if (entity instanceof GmMetaModel) {
				declaringModel = (GmMetaModel)entity;
			}
			else if (entity instanceof GmCollectionType) {
				return ManInstanceKind.acquire; 
			}
			else if (entity instanceof GmCustomModelElement) {
				GmCustomModelElement element = (GmCustomModelElement)entity;
				declaringModel = element.declaringModel();
			}else if (entity instanceof GmType) {
				declaringModel = ((GmType) entity).getDeclaringModel();
			} else {
				isUndeclaredInstance = true;
			}
			
			if (isUndeclaredInstance || declaringModel == targetModel) {
				return ManInstanceKind.create;
			}
			else {
				return ManInstanceKind.lookup;
			}
		}
		
	}

}
