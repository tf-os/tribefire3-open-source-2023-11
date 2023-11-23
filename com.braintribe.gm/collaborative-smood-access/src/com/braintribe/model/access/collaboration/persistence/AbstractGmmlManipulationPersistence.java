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
package com.braintribe.model.access.collaboration.persistence;

import static com.braintribe.common.lcd.Constants.ENCODING_UTF8;
import static com.braintribe.utils.FileTools.getNiceAbsPath;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.swapKeysAndValues;
import static java.util.Objects.requireNonNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.cfg.Configurable;
import com.braintribe.common.lcd.Constants;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.collaboration.persistence.tools.CsaPersistenceTools;
import com.braintribe.model.access.collaboration.persistence.tools.GmValueMarshaller;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.dataio.FileBasedPersistence;
import com.braintribe.model.processing.manipulation.marshaller.LocalManipulationStringifier;
import com.braintribe.model.processing.manipulation.marshaller.ManipulationStringifier;
import com.braintribe.model.processing.manipulation.marshaller.RemoteManipulationStringifier;
import com.braintribe.model.processing.manipulation.parser.api.GmmlManipulatorErrorHandler;
import com.braintribe.model.processing.manipulation.parser.api.GmmlManipulatorParserConfiguration;
import com.braintribe.model.processing.manipulation.parser.api.MutableGmmlManipulatorParserConfiguration;
import com.braintribe.model.processing.manipulation.parser.api.ParseResponse;
import com.braintribe.model.processing.manipulation.parser.api.ProblematicEntitiesRegistry;
import com.braintribe.model.processing.manipulation.parser.impl.Gmml;
import com.braintribe.model.processing.manipulation.parser.impl.ManipulatorParser;
import com.braintribe.model.processing.manipulation.parser.impl.listener.GmmlManipulatorParserListener;
import com.braintribe.model.processing.manipulation.parser.impl.listener.error.LenientErrorHandler;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.collaboration.AbstractPersistenceInitializer;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceAppender;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.EntityManager;
import com.braintribe.model.processing.session.api.managed.ManipulationMode;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.smoodstorage.stages.IndexedStage;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;

/**
 * This is definitely not thread-safe
 * 
 * @author peter.gazdik
 */
public abstract class AbstractGmmlManipulationPersistence extends AbstractPersistenceInitializer implements PersistenceAppender {

	private static final Logger log = Logger.getLogger(AbstractGmmlManipulationPersistence.class);

	private ModelOracle modelOracle;
	private Supplier<Set<GenericEntity>> createdEntitiesSupplier;
	private GmmlManipulatorErrorHandler errorHandler;
	private ProblematicEntitiesRegistry problematicEntitiesRegistry;

	protected final IndexedStage stage = IndexedStage.T.create();

	protected FileBasedPersistence<List<Long>> manMarkerPersistence; // data.man.length [, model.man.length]

	@Configurable
	public void setModelOracle(ModelOracle modelOracle) {
		this.modelOracle = modelOracle;
	}

	/**
	 * Configures a supplies for entities created in this stage, which is needed for efficient implementation of
	 * {@link #append(File, Resource, Map, EntityManager) } (to know when delete manipulation can ignore references - see
	 * {@link GmmlManipulatorParserListener#exitDeleteManipulation}).
	 */
	@Configurable
	public void setCreatedEntitiesSupplier(Supplier<Set<GenericEntity>> createdEntitiesSupplier) {
		this.createdEntitiesSupplier = createdEntitiesSupplier;
	}

	@Configurable
	public AbstractGmmlManipulationPersistence setGmmlErrorHandler(GmmlManipulatorErrorHandler errorHandler) {
		this.errorHandler = requireNonNull(errorHandler);
		return this;
	}

	public AbstractGmmlManipulationPersistence setProblematicEntitiesRegistry(ProblematicEntitiesRegistry problematicEntitiesRegistry) {
		this.problematicEntitiesRegistry = problematicEntitiesRegistry;
		return this;
	}

	@Override
	public PersistenceStage getPersistenceStage() {
		return stage;
	}

	/**
	 * Configures the correct stage name and manipulation file(s) to given GMML persistence.
	 * 
	 * @see BasicGmmlManipulationPersistence#configureStage
	 * @see CortexGmmlManipulationPersistence#configureStage
	 */
	public abstract void configureStage(File parentFolder, String stageName);

	/**
	 * Prepares the file marker persistence for given folder. Assumes the folder exists already.
	 */
	protected final void configureStageFolder(File stageFolder) {
		manMarkerPersistence = new FileBasedPersistence<>();
		manMarkerPersistence.setFile(new File(stageFolder, "marker.txt"));
		manMarkerPersistence.setMarshaller(GmValueMarshaller.INSTANCE);
	}

	protected Map<Object, String> initialize(PersistenceInitializationContext context, File gmmlFile) {
		truncateFileIfMarkerPresent(gmmlFile);

		if (!gmmlFile.exists())
			return newMap();

		Set<String> homeopathicVars = CsaPersistenceTools.resolveHomeopathicVariables(gmmlFile);

		try (InputStream in = new BufferedInputStream(new FileInputStream(gmmlFile))) {
			ParseResponse response = ManipulatorParser.parse(in, ENCODING_UTF8, context.getSession(), parserConfig(gmmlFile, homeopathicVars));
			return invertVariablesMap(response.variables, newMap());

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while parsing file: " + getNiceAbsPath(gmmlFile));
		}

	}

	private void truncateFileIfMarkerPresent(File gmmlFile) {
		List<Long> fileLengths = manMarkerPersistence.get();
		if (fileLengths == null)
			return;

		boolean isData = gmmlFile.getName().startsWith("data");
		long markedFileSize = isData ? fileLengths.get(0) : fileLengths.get(1);

		if (markedFileSize > gmmlFile.length())
			log.warn("Inconsistency with GMML stage: " + stage.getName() + ". File " + gmmlFile.getAbsolutePath() + " has size " + gmmlFile.length()
					+ " and the marker is beyond EoF at: " + markedFileSize);

		try {
			IOTools.truncateFile(gmmlFile, markedFileSize);

		} catch (IOException e) {
			throw new ManipulationPersistenceException("Error occured while truncating manipulation to file: " + gmmlFile.getAbsolutePath()
					+ ". Truncating is only needed if the last write access didn't finish properly. There might be some bigger issue.", e);
		}
	}

	private GmmlManipulatorParserConfiguration parserConfig(File gmmlFile, Set<String> homeopathicVariables) {
		MutableGmmlManipulatorParserConfiguration result = CsaPersistenceTools.parserConfig(gmmlFile);
		prepareStandardParserConfig(result);
		result.setHomeopathicVariables(homeopathicVariables);

		return result;
	}

	/**
	 * In case two different variables reference the same GmModelElement, and we are already initializing the data, it could happen that we have
	 * actually created some model element, then deleted it and then created it again.
	 * 
	 * Imagine we create a model and do some data-manipulations in it. We thus assign a new variable to that model in the data appender. Then we
	 * delete this model and the create a new one with same name and same globalId. When we do another data-manipulation, we allocate a second
	 * variable that points to this second instance, but in the man file the reference (consisting of type+globalId) look the same. When we restart,
	 * the data-man parser will resolve both variables to the same instance. In such case we don't consider that an invalid state and just make sure
	 * the higher number is used for subsequent manipulations.
	 * 
	 * Another possibility for a conflict is after a merge from the following stage. This
	 */
	private Map<Object, String> invertVariablesMap(Map<String, Object> vars, Map<Object, String> invertedVars) {
		for (Entry<String, Object> entry : vars.entrySet()) {
			String variable = entry.getKey();
			Object value = entry.getValue();

			String otherVariable = invertedVars.put(value, variable);
			if (otherVariable != null && !otherVariable.equals(variable) && !(value instanceof GenericEntity))
				throw new IllegalStateException("Cannot create value to variable map, as two different variables ('" + variable + "', '"
						+ otherVariable + "' map to the same non-entity value: " + value);
		}

		return invertedVars;
	}

	protected abstract Stream<Map<Object, String>> getVariablesMapStream();

	protected void logAppendedManipulation(File dataFile, Manipulation manipulation) {
		log.trace(() -> "Appending to " + dataFile.getParentFile().getAbsolutePath() + "/" + stage.getName() + ":\n" + manipulation.stringify());
	}

	protected void storeManMarkers() {
		List<Long> markers = getGmmlStageFiles() //
				.map(File::length) //
				.collect(Collectors.toList());

		manMarkerPersistence.accept(markers);
	}

	protected void deleteManMarkers() {
		manMarkerPersistence.getFile().delete();
	}

	protected AppendedSnippet append(File gmmlFile, Manipulation manipulation, Map<Object, String> variables, ManipulationMode mode)
			throws ManipulationPersistenceException {

		if (manipulation == null)
			return null;

		/* This is a temporary hack to to create an append that marks an end to a transaction. When appending manipulations in bulks, we keep
		 * references as keys inside variables. When the last bulk is done, we call append with mode=null to indicate we want to remove them. This is
		 * important as the next manipulation stack might contain preliminary references where the IDs are the same as the ones in our map. */
		if (mode == null) {
			removeRefVariables(variables);
			return null;
		}

		boolean isRemote = mode == ManipulationMode.REMOTE;

		ManipulationStringifier stringifier = isRemote
				? new RemoteManipulationStringifier(extractTypeVariables(variables), extractRefVariables(variables), variables.values(), modelOracle)
				: new LocalManipulationStringifier(variables);

		stringifier.setSingleBlock(true);

		long newManStartPosition = gmmlFile.length();

		try {
			FileTools.write(gmmlFile).append(true).usingWriter(w -> stringifier.stringify(w, manipulation));

		} catch (Exception e) {
			throw new ManipulationPersistenceException("Error occured while appending manipulation to file '" + gmmlFile.getAbsolutePath()
					+ ". Manipulation: " + manipulation.stringify(), e);
		}

		if (isRemote)
			rememberUsedVariables(variables, (RemoteManipulationStringifier) stringifier);

		long bytesWritten = gmmlFile.length() - newManStartPosition;
		InputStreamProvider isp = () -> FileTools.newInputStream(gmmlFile, newManStartPosition);

		return new AppendedSnippetImpl(bytesWritten, isp);
	}

	private Map<String, String> extractTypeVariables(Map<Object, String> variables) {
		return variables.entrySet().stream() //
				.filter(e -> (e.getKey() instanceof GenericModelType)) //
				.collect(Collectors.toMap( //
						e -> getTypeSignature(e.getKey()), //
						Map.Entry::getValue //
				));
	}

	private String getTypeSignature(Object type) {
		return ((GenericModelType) type).getTypeSignature();
	}

	private Map<EntityReference, String> extractRefVariables(Map<Object, String> variables) {
		return variables.entrySet().stream() //
				.filter(this::isEntityReferenceEntry) //
				.map(e -> (Map.Entry<EntityReference, String>) (Map.Entry<?, ?>) e) //
				.collect(Collectors.toMap( //
						Map.Entry::getKey, //
						Map.Entry::getValue //
				));
	}
	private void removeRefVariables(Map<Object, String> variables) {
		Map<EntityReference, String> refVariables = extractRefVariables(variables);

		for (Entry<EntityReference, String> entry : refVariables.entrySet()) {
			EntityReference entityReference = entry.getKey();
			String variableName = entry.getValue();

			variables.remove(entityReference);
			variables.put(variableName, variableName);
		}
	}

	private boolean isEntityReferenceEntry(Map.Entry<?, ?> e) {
		Object o = e.getKey();
		return o instanceof EntityReference && ((EntityReference) o).session() == null;
	}

	private void rememberUsedVariables(Map<Object, String> variables, RemoteManipulationStringifier rms) {
		rememberVars(variables, rms.getTypeToVar());
		rememberVars(variables, rms.getReferenceToVar());
	}

	private void rememberVars(Map<Object, String> variables, Map<?, String> somethingToVar) {
		variables.putAll(somethingToVar);
	}

	protected void append(File gmmlFile, Resource gmmlResource, Map<Object, String> variables, EntityManager entityManager) {
		if (gmmlResource == null)
			return;

		try {
			tryAppend(gmmlFile, gmmlResource, variables, entityManager);

		} catch (IOException e) {
			throw new ManipulationPersistenceException("Error wile applying manipulations from a GMML resource.", e);
		}
	}

	private void tryAppend(File gmmlFile, Resource gmmlResource, Map<Object, String> variables, EntityManager entityManager) throws IOException {
		// apply in memory
		try (InputStream in = gmmlResource.openStream()) {
			ParseResponse response = ManipulatorParser.parse(in, Constants.ENCODING_UTF8, entityManager, parserConfig(variables));
			invertVariablesMap(response.newVariables, variables);
		}

		// append to file
		try (InputStream in = gmmlResource.openStream()) {
			IOTools.inputToFile(in, gmmlFile, true);
		}
	}

	private GmmlManipulatorParserConfiguration parserConfig(Map<Object, String> variables) {
		MutableGmmlManipulatorParserConfiguration result = Gmml.manipulatorConfiguration();
		prepareStandardParserConfig(result);

		result.setErrorHandler(LenientErrorHandler.INSTANCE);
		result.setVariables(swapKeysAndValues(variables));
		result.setPreviouslyCreatedEntities(createdEntitiesSupplier.get());

		return result;
	}

	private void prepareStandardParserConfig(MutableGmmlManipulatorParserConfiguration result) {
		result.setStageName(getPersistenceStage().getName());
		result.setParseSingleBlock(true);
		result.setErrorHandler(errorHandler);
		result.setProblematicEntitiesRegistry(problematicEntitiesRegistry);
	}

	protected static File createStorageFile(File parentFile, String modelOrData) {
		File dataFile = new File(parentFile, modelOrData + ".man");

		return ensureFileInExistingDirectory(dataFile);
	}

	protected static File ensureFileInExistingDirectory(File file) {
		if (file.isDirectory())
			throw new IllegalArgumentException(
					"Folder '" + file.getAbsolutePath() + "' cannot be used for persistence, as it is a .. ehm, folder ... and not a file.");

		try {
			FileTools.ensureDirectoryExists(file.getParentFile());

		} catch (IOException e) {
			throw new RuntimeException("Unable to ensure parent directory for file: " + file.getAbsolutePath(), e);
		}

		return file;
	}

	/**
	 * Returns the data and model files in this order. This simply normalizes the handling for basic and cortex versions. This only works iff
	 * config.json setup is used.
	 */
	public abstract Stream<File> getGmmlStageFiles();

}
