// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl;

import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.braintribe.build.cmd.assets.PlatformAssetDistributionConstants;
import com.braintribe.build.cmd.assets.api.PlatformAssetBuilderContext;
import com.braintribe.build.cmd.assets.api.PlatformAssetCollector;
import com.braintribe.build.cmd.assets.api.PlatformAssetDistributionContext;
import com.braintribe.build.cmd.assets.api.PlatformAssetStorageRecording;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.csa.CollaborativeSmoodConfiguration;
import com.braintribe.model.csa.ManInitializer;
import com.braintribe.model.csa.SmoodInitializer;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processing.manipulation.marshaller.LocalManipulationStringifier;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;
import com.braintribe.utils.FileTools;

public class StorageConfigurationCollector implements PlatformAssetCollector, PlatformAssetDistributionConstants {

	private static final String FILENAME_CONFIG_JSON = "config.json";

	private final Map<String, List<SmoodInitializer>> storageInitializers = new HashMap<>();

	@Override
	public void transfer(PlatformAssetDistributionContext context) {
		
		prepareSetupAccessData(context);
		
		// each configured access receives a trunk man initializer stage
		appendTrunkStages();

		// write any storage config.json file in the according folder
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		for (Map.Entry<String, List<SmoodInitializer>> entry : storageInitializers.entrySet()) {
			String accessId = entry.getKey();
			List<SmoodInitializer> initializers = entry.getValue();

			CollaborativeSmoodConfiguration smoodConfiguration = CollaborativeSmoodConfiguration.T.create();
			smoodConfiguration.getInitializers().addAll(initializers);

			File accessConfigFile = context.storageAccessDataFolder(accessId) //
					.push(FILENAME_CONFIG_JSON) //
					.toFile();

			FileTools.write(accessConfigFile).usingOutputStream(os -> marshaller.marshall(os, smoodConfiguration,
					GmSerializationOptions.deriveDefaults().setOutputPrettiness(OutputPrettiness.high).build()));
		}
	}

	private void prepareSetupAccessData(PlatformAssetDistributionContext context) {
		println("Building storage for setup access");
		PlatformAssetStorageRecording platformAssetStorageRecording = context.platformAssetStorageRecording();

		LocalManipulationStringifier stringifier = new LocalManipulationStringifier();
		stringifier.setSingleBlock(true);

		File manipulationFile = context.storageAccessDataStageFolder(ACCESS_ID_SETUP, "initial")
				.push("data.man")
				.toFile();

		manipulationFile.getParentFile().mkdirs();

		ManInitializer manInitializer = ManInitializer.T.create();
		manInitializer.setName("initial");

		appendStage(ACCESS_ID_SETUP, manInitializer);

		// TODO use ManMarshaller
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(manipulationFile), "UTF-8")){
			stringifier.stringify(writer, platformAssetStorageRecording.manipulations());
		}
		catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while writing storage manipulations for access: " + ACCESS_ID_SETUP);
		}
		
	}

	private void appendTrunkStages() {
		for (List<SmoodInitializer> initializers : storageInitializers.values())
			initializers.add(newManInitializer("trunk"));
	}

	public void appendCortexStage(PlatformAssetBuilderContext<?> context, Consumer<ManagedGmSession> recorder) {
		appendStage(context, "cortex", recorder);
	}
	
	public void appendStage(PlatformAssetBuilderContext<?> context, String accessId, Consumer<ManagedGmSession> recorder) {
		String qualifiedAssetName = context.getAsset().qualifiedAssetName();

		appendManStage(accessId, qualifiedAssetName);

		File dataManFile = context.storageAccessDataStageFolder(accessId, qualifiedAssetName).push("data.man").toFile();
		dataManFile.getParentFile().mkdirs();
		recordStoragePriming(dataManFile, recorder);
	}

	private void appendManStage(String accessId, String stageName) {
		ManInitializer initializer = newManInitializer(stageName);
		appendStage(accessId, initializer);
	}

	private ManInitializer newManInitializer(String qualifiedAssetName) {
		ManInitializer initializer = ManInitializer.T.create();
		initializer.setName(qualifiedAssetName);
		return initializer;
	}

	private void recordStoragePriming(File file, Consumer<ManagedGmSession> recorder) {
		List<Manipulation> manipulations = newList();
		BasicManagedGmSession session = new BasicManagedGmSession();
		session.listeners().add(manipulations::add);
		recorder.accept(session);

		LocalManipulationStringifier stringifier = new LocalManipulationStringifier();
		stringifier.setSingleBlock(true);

		FileTools.write(file).usingWriter(w -> stringifier.stringify(w, manipulations));
	}

	public void appendStage(String accessId, SmoodInitializer initializer) {
		storageInitializers.computeIfAbsent(accessId, id -> newList()).add(initializer);
	}

}
