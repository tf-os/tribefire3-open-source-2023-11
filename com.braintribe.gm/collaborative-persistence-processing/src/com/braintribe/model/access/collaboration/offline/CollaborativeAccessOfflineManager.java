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
package com.braintribe.model.access.collaboration.offline;

import static com.braintribe.model.access.collaboration.persistence.tools.CsaPersistenceTools.TRUNK_STAGE;
import static com.braintribe.utils.lcd.CollectionTools2.findFirstIndex;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.UnsupportedEnumException;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.collaboration.CsaStatePersistence;
import com.braintribe.model.access.collaboration.persistence.OfflineBufferedManipulationAppender;
import com.braintribe.model.access.collaboration.persistence.tools.CsaPersistenceTools;
import com.braintribe.model.cortexapi.access.collaboration.CollaborativePersistenceRequest;
import com.braintribe.model.cortexapi.access.collaboration.MergeCollaborativeStage;
import com.braintribe.model.cortexapi.access.collaboration.MergeCollaborativeStageToPredecessor;
import com.braintribe.model.cortexapi.access.collaboration.PushCollaborativeStage;
import com.braintribe.model.cortexapi.access.collaboration.RenameCollaborativeStage;
import com.braintribe.model.csa.CollaborativeSmoodConfiguration;
import com.braintribe.model.csa.ManInitializer;
import com.braintribe.model.csa.SmoodInitializer;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;

/**
 * @author peter.gazdik
 */

public class CollaborativeAccessOfflineManager implements ServiceProcessor<CollaborativePersistenceRequest, Void> {

	private static final Logger log = Logger.getLogger(CollaborativeAccessOfflineManager.class);

	private static class GmmlFilePair {

		public File modelFile;
		public File dataFile;

		public GmmlFilePair(File modelFile, File dataFile) {
			this.modelFile = modelFile;
			this.dataFile = dataFile;
		}

	}

	private File baseFolder;
	private CsaStatePersistence statePersistence;

	private CollaborativeSmoodConfiguration configuration;

	@Required
	public void setBaseFolder(File baseFolder) {
		this.baseFolder = baseFolder;
	}

	@Required
	public void setCsaStatePersistence(CsaStatePersistence statePersistence) {
		this.statePersistence = statePersistence;
	}

	@Override
	public Void process(ServiceRequestContext requestContext, CollaborativePersistenceRequest request) {
		switch (request.collaborativeRequestType()) {
			case GetInitializers:
			case GetStageData:
			case GetStageStats:
				throw new IllegalArgumentException("ReadOnly methods are not supported by this (offline) implementation. Request: " + request);

			case MergeStage:
				mergeStage((MergeCollaborativeStage) request);
				return null;
			case MergeStageToPredecessor:
				mergeStageToPredecessor((MergeCollaborativeStageToPredecessor) request);
				return null;
			case PushStage:
				pushStage((PushCollaborativeStage) request);
				return null;
			case RenameStage:
				renameStage((RenameCollaborativeStage) request);
				return null;
			case Reset:
				resetPersistence();
				return null;
			default:
				throw new UnsupportedEnumException(request.collaborativeRequestType());
		}
	}

	// ###############################################
	// ## . . . . . . . . PushStage . . . . . . . . ##
	// ###############################################

	private void pushStage(PushCollaborativeStage request) {
		String name = request.getName();

		checkStageNotExistsYet(name);
		addNewManInitializerToConfiguration(name);
	}

	private void addNewManInitializerToConfiguration(String name) {
		ManInitializer initializer = ManInitializer.T.create();
		initializer.setName(name);

		configuration.getInitializers().add(initializer);
		storeConfiguration();
	}

	// ###############################################
	// ## . . . . . . . RenameStage . . . . . . . . ##
	// ###############################################

	private void renameStage(RenameCollaborativeStage request) {
		String oldName = request.getOldName();
		String newName = request.getNewName();

		checkStageNotExistsYet(newName);
		renameManInitializerInConfiguration(oldName, newName);
		renameStageInFileSystem(oldName, newName);
	}

	private void renameManInitializerInConfiguration(String oldName, String newName) {
		ManInitializer initializer = getManInitializer(oldName);
		initializer.setName(newName);

		storeConfiguration();
	}

	private ManInitializer getManInitializer(String name) {
		return (ManInitializer) initializers(name) //
				.filter(i -> i instanceof ManInitializer) //
				.findFirst() //
				.orElseThrow(() -> new GenericModelException("No ManInitializer found with name: " + name));
	}

	private void checkStageNotExistsYet(String name) {
		initializers(name) //
				.forEach(this::throwExceptionBecauseStageAlreadyExists);
	}

	private void throwExceptionBecauseStageAlreadyExists(SmoodInitializer si) {
		throw new IllegalArgumentException("Cannot create collaborative stage '" + si.getName()
				+ "' because such a stage already exists. The existing stage is of type: " + si.entityType().getShortName());
	}

	private Stream<SmoodInitializer> initializers(String name) {
		return getConfiguration().getInitializers().stream() //
				.filter(i -> name.equals(i.getName()));
	}

	private void renameStageInFileSystem(String oldName, String newName) {
		File oldFolder = newStageBaseFolder(oldName);
		if (!oldFolder.exists())
			return;

		File newFolder = newStageBaseFolder(newName);

		if (!oldFolder.isDirectory())
			throw new IllegalStateException("Stage folder is not actually a folder: " + oldFolder.getAbsolutePath());

		if (!oldFolder.renameTo(newFolder))
			throw new GenericModelException("Stage '" + oldName + "' cannot be renamed to '" + newName
					+ "', becaue attempt to rename the folder returned false. Old folder: " + oldFolder.getAbsolutePath());
	}

	private File newStageBaseFolder(String name) {
		String fileName = toLegalFileName(name);

		return new File(baseFolder, fileName);
	}

	public static String toLegalFileName(String name) {
		return FileTools.replaceIllegalCharactersInFileName(name, "_");
	}

	// ###############################################
	// ## . . . . . . ResetPersistence . . . . . . .##
	// ###############################################

	private void resetPersistence() {
		if (statePersistence == null)
			throw new UnsupportedOperationException(
					"Cannot reset persistence rooted at: '" + baseFolder + "' because no initial-configuration-supplier was configured.");

		List<SmoodInitializer> currentInitializers = getConfiguration().getInitializers();
		configuration = statePersistence.readOriginalConfiguration();

		Set<String> namesToKeep = getStageNamesToKeepOnReset(configuration);
		for (SmoodInitializer si : currentInitializers)
			if (si instanceof ManInitializer)
				if (!namesToKeep.contains(si.getName()))
					deleteStageFolder(si.getName());

		storeConfiguration();
	}

	private void deleteStageFolder(String name) {
		File folder = newStageBaseFolder(name);
		try {
			FileTools.deleteDirectoryRecursively(folder);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Cannot reset persistence. Error while deleting folder for stage: " + name);
		}
	}

	public static Set<String> getStageNamesToKeepOnReset(CollaborativeSmoodConfiguration configuration) {
		Set<String> namesToKeep = configuration.getInitializers().stream() //
				.filter(si -> !si.getSkip()) //
				.map(SmoodInitializer::getName) //
				.collect(Collectors.toSet());

		namesToKeep.remove(TRUNK_STAGE);

		return namesToKeep;
	}

	// ###############################################
	// ## . . . . . . . MergeStage . . . . . . . . .##
	// ###############################################

	private void mergeStage(MergeCollaborativeStage request) {
		mergeStage(request.getSource(), request.getTarget());
	}

	public void mergeStage(String source, String target) {
		GmmlFilePair sourceStageFiles = getGmmlStageFiles(source);
		GmmlFilePair targetStageFiles = getGmmlStageFiles(target);

		mergeManipulationsTo(sourceStageFiles, targetStageFiles);

		if (!TRUNK_STAGE.equals(source))
			deleteGmmlPersistence(source);

		removeManInitializerFromConfigurationIfNotTrunk(source);
	}

	private void removeManInitializerFromConfigurationIfNotTrunk(String source) {
		if (TRUNK_STAGE.equals(source))
			return;

		getConfiguration().getInitializers().removeIf(si -> source.equals(si.getName()));
		storeConfiguration();
	}

	private void deleteGmmlPersistence(String stageName) {
		File stageBaseFolder = newStageBaseFolder(stageName);
		if (!stageBaseFolder.delete())
			log.error("Unable to delete stage folder: " + stageBaseFolder.getAbsolutePath());

	}

	private void mergeManipulationsTo(GmmlFilePair sourceFilePair, GmmlFilePair targetFilePair) {
		StringJoiner stringJoiner = new StringJoiner(", ");

		_mergeManipulationTo(sourceFilePair.dataFile, targetFilePair.dataFile, stringJoiner);
		_mergeManipulationTo(sourceFilePair.modelFile, targetFilePair.modelFile, stringJoiner);

		if (stringJoiner.length() > 0)
			throw new GenericModelException("Error(s) while merging manipulation files: " + stringJoiner.toString());
	}

	private void _mergeManipulationTo(File sourceFile, File targetFile, StringJoiner stringJoiner) {
		if (sourceFile.exists()) {
			if (!targetFile.exists()) {
				if (!sourceFile.renameTo(targetFile))
					stringJoiner.add("Unable to move file: " + sourceFile.getAbsolutePath() + " to: " + targetFile.getAbsolutePath());

			} else {
				appendManipulations(sourceFile, targetFile);
				if (!sourceFile.delete())
					stringJoiner.add("Unable to delete file: " + sourceFile.getAbsolutePath());
			}

		}
	}

	private void appendManipulations(File sourceFile, File targetFile) {
		try (OfflineBufferedManipulationAppender targetAppender = new OfflineBufferedManipulationAppender(targetFile)) {
			CsaPersistenceTools.parseGmmlFile(sourceFile, targetAppender::append);
			targetAppender.flush();

		} catch (IOException e) {
			throw new GenericModelException("Error while appending manipulations to file: " + targetFile.getAbsolutePath(), e);
		}
	}

	private GmmlFilePair getGmmlStageFiles(String name) {
		return getGmmlStageFiles(newStageBaseFolder(name));
	}

	protected GmmlFilePair getGmmlStageFiles(File stageBase) {
		return createFilePair(stageBase);
	}

	private GmmlFilePair createFilePair(File parentFile) {
		File modelFile = new File(parentFile, getManFileName("model"));
		File dataFile = new File(parentFile, getManFileName("data"));

		modelFile = ensureFileInExistingDirectory(modelFile);
		dataFile = ensureFileInExistingDirectory(dataFile);

		return new GmmlFilePair(modelFile, dataFile);
	}

	protected String getManFileName(String modelOrData) {
		return modelOrData + ".man";
	}

	protected File ensureFileInExistingDirectory(File file) {
		if (file.isDirectory())
			throw new IllegalArgumentException(
					"Folder '" + file.getAbsolutePath() + "' cannot be used for persistence, as it is a .. ehm, folder ... and not a file.");

		try {
			FileTools.ensureDirectoryExists(file.getParentFile());

		} catch (UncheckedIOException | IOException e) {
			throw new RuntimeException("Unable to ensure parent directory for file: " + file.getAbsolutePath(), e);
		}

		return file;
	}

	// ###############################################
	// ## . . . . MergeStageToPredecessor . . . . . ##
	// ###############################################

	private void mergeStageToPredecessor(MergeCollaborativeStageToPredecessor request) {
		String sourceName = request.getName();
		String targetName = getPredecessorName(sourceName);

		mergeStage(sourceName, targetName);
	}

	private String getPredecessorName(String name) {
		int i = findFirstIndex(getConfiguration().getInitializers(), j -> name.equals(j.getName()));
		if (i == 0)
			throw new IllegalArgumentException("Cannot merge stage to predecessor, as this is the first stage: " + name);
		if (i < 0)
			throw new IllegalArgumentException("Cannot merge stage to predecessor, no stage found for name: " + name);

		SmoodInitializer si = getConfiguration().getInitializers().get(i - 1);
		if (!(si instanceof ManInitializer))
			throw new IllegalArgumentException(
					"Cannot merge stage '" + name + "' to predecessor, as the predecessor is not a GMML persistence, but: " + si);

		return si.getName();
	}

	// ###############################################
	// ## . . . . . . . Non-Request API . . . . . . ##
	// ###############################################

	/**
	 * @param gmmlResources
	 *            resources containing GMML snippets for model and data man files, respectively
	 */
	public void append(Resource[] gmmlResources) {
		String stageName = resolveStageForAppend();

		GmmlFilePair stageFiles = getGmmlStageFiles(stageName);

		append(gmmlResources[0], stageFiles.modelFile);
		append(gmmlResources[1], stageFiles.dataFile);
	}

	private void append(Resource gmmlResource, File gmmlFile) {
		if (gmmlResource == null)
			return;

		try (InputStream in = gmmlResource.openStream()) {
			IOTools.inputToFile(in, gmmlFile, true);
		} catch (IOException e) {
			throw new RuntimeException("Error while appending manipulations to file: " + gmmlFile.getAbsolutePath(), e);
		}
	}

	private String resolveStageForAppend() {
		SmoodInitializer lastInitializer = getConfiguration().getInitializers().stream() //
				.filter(si -> !si.getSkip()) //
				.reduce((a, b) -> b) //
				.orElse(null);

		if (lastInitializer instanceof ManInitializer)
			return lastInitializer.getName();
		else
			// If the last initializer is not a ManInitializer, our persistence automatically appends an extra ManInitializer called "trunk"
			// See AbstractManipulationPersistence.ensureLastInitializerIsManInitializer()
			return "trunk";
	}

	// ###############################################
	// ## . . . . . . . . . Common . . . . . . . . .##
	// ###############################################

	public CollaborativeSmoodConfiguration getConfiguration() {
		if (configuration == null)
			configuration = statePersistence.readConfiguration();

		return configuration;
	}

	private void storeConfiguration() {
		if (configuration != null)
			statePersistence.writeConfiguration(configuration);
	}

}
