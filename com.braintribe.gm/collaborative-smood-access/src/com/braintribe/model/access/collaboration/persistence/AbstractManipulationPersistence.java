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

import static com.braintribe.model.access.collaboration.offline.CollaborativeAccessOfflineManager.getStageNamesToKeepOnReset;
import static com.braintribe.utils.lcd.CollectionTools2.last;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.updateMapKey;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.collaboration.CsaStatePersistence;
import com.braintribe.model.access.collaboration.persistence.tools.CsaPersistenceTools;
import com.braintribe.model.access.collaboration.persistence.tools.ModifiedEntitiesSupplier;
import com.braintribe.model.csa.CollaborativeSmoodConfiguration;
import com.braintribe.model.csa.CustomInitializer;
import com.braintribe.model.csa.ManInitializer;
import com.braintribe.model.csa.SmoodInitializer;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.processing.manipulation.parser.api.GmmlManipulatorErrorHandler;
import com.braintribe.model.processing.manipulation.parser.api.ProblematicEntitiesRegistry;
import com.braintribe.model.processing.manipulation.parser.impl.listener.error.StrictErrorHandler;
import com.braintribe.model.processing.manipulation.parser.impl.manipulator.AppendingProblematicEntitiesRegistry;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.collaboration.CollaborativeAccess;
import com.braintribe.model.processing.session.api.collaboration.CollaborativeManipulationPersistence;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceAppender;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializer;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.managed.ManipulationMode;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;
import com.braintribe.provider.Holder;
import com.braintribe.provider.ManagedValue;
import com.braintribe.utils.FileTools;

/**
 * This is not intended to be Thread-safe.
 * 
 * @author peter.gazdik
 */
public abstract class AbstractManipulationPersistence<G extends AbstractGmmlManipulationPersistence> implements CollaborativeManipulationPersistence {

	protected static final String COMMENT_PREFIX = "#";
	protected static final String TRUNK_STAGE = "trunk";

	private static final Logger log = Logger.getLogger(AbstractManipulationPersistence.class);

	// Configurable
	private ManagedGmSession csaSession;
	private CollaborativeAccess csa;
	private ModelOracle modelOracle;
	private List<PersistenceInitializer> staticInitializers = emptyList();
	private List<PersistenceInitializer> staticPostInitializers = emptyList();
	private Function<CustomInitializer, ManagedValue<PersistenceInitializer>> customInitializerResolver = AbstractManipulationPersistence::noCustomInitializerAllowed;
	private CollaborativeSmoodConfiguration collaborativeSmoodConfiguration;
	private CsaStatePersistence statePersistence;
	private File storageBase;
	private GmmlManipulatorErrorHandler errorHandler = StrictErrorHandler.INSTANCE;
	private Predicate<AtomicManipulation> manipulationFilter;
	private Consumer<Manipulation> appendedManipulationListener;

	// Internal
	private PersistenceAppender appender;
	private List<PersistenceInitializer> configuredInitializers; // last is also expected to be a PersistenceAppender
	private List<ManagedValue<PersistenceInitializer>> managedInitializers = emptyList();
	private final Map<String, PersistenceInitializer> initializersByName = newMap();

	// This very instance is given to each manipulator as problematic entities, and is also updated by the manipulator
	private final ProblematicEntitiesRegistry problematicEntitiesRegistry = new AppendingProblematicEntitiesRegistry();

	public ProblematicEntitiesRegistry getProblematicEntitiesRegistry() {
		return problematicEntitiesRegistry;
	}

	@Override
	public void onCollaborativeAccessInitialized(CollaborativeAccess csa, ManagedGmSession csaSession) {
		this.csa = csa;
		this.csaSession = csaSession;
	}

	@Override
	public void setModelOracle(ModelOracle modelOracle) {
		this.modelOracle = modelOracle;

		updateGmmlPersistencesModelOracle();
	}

	private void updateGmmlPersistencesModelOracle() {
		for (PersistenceInitializer initializer : configuredInitializers)
			if (initializer instanceof AbstractGmmlManipulationPersistence)
				((AbstractGmmlManipulationPersistence) initializer).setModelOracle(modelOracle);
	}

	@Configurable
	public void setStaticInitializers(List<PersistenceInitializer> staticInitializers) {
		this.staticInitializers = staticInitializers;
	}

	@Configurable
	public void setStaticPostInitializers(List<PersistenceInitializer> staticPostInitializers) {
		this.staticPostInitializers = staticPostInitializers;
	}

	// one of these two is required (IndexStorageBase or StorageBase)

	@Required
	public void setStorageBase(File storageBase) {
		this.storageBase = storageBase;
	}

	@Override
	public File getStrogaBase() {
		return storageBase;
	}

	@Required
	public void setCsaStatePersistence(CsaStatePersistence statePersistence) {
		this.statePersistence = statePersistence;
	}

	public void setCustomInitializerResolver(Function<CustomInitializer, ManagedValue<PersistenceInitializer>> customInitializerResolver) {
		this.customInitializerResolver = customInitializerResolver;
	}

	private static ManagedValue<PersistenceInitializer> noCustomInitializerAllowed(CustomInitializer ci) {
		throw new IllegalArgumentException("Unsupported custom initializer: " + ci);
	}

	@Configurable
	public void setManipulationFilter(Predicate<AtomicManipulation> manipulationFilter) {
		this.manipulationFilter = manipulationFilter;
	}

	public void setGmmlErrorHandler(GmmlManipulatorErrorHandler errorHandler) {
		this.errorHandler = requireNonNull(errorHandler);
	}

	@Configurable
	public void setAppendedManipulationListener(Consumer<Manipulation> appendedManipulationListener) {
		this.appendedManipulationListener = appendedManipulationListener;
	}

	// ############################################################
	// ## . . . . . . . . ServiceRequest methods . . . . . . . . ##
	// ############################################################

	@Override
	public void renamePersistenceStage(String oldName, String newName) {
		// we want to check it exists first, before we rename the folder
		G gmmlPersistence = getGmmlPersistence(oldName);

		File oldFolder = newStageBaseFolder(oldName);
		File newFolder = newStageBaseFolder(newName);

		log.debug(() -> "Renaming persistence stage from '" + oldName + "' to: " + newName);

		updateMapKey(initializersByName, oldName, newName);
		renameStageFolder(oldName, newName, oldFolder, newFolder);
		gmmlPersistence.configureStage(newFolder, newName);
	}

	private static void renameStageFolder(String oldName, String newName, File oldFolder, File newFolder) {
		if (!oldFolder.exists())
			return;

		if (!oldFolder.isDirectory())
			throw new IllegalStateException("Stage folder is not actually a folder: " + oldFolder.getAbsolutePath());

		if (!oldFolder.renameTo(newFolder))
			throw new GenericModelException("Stage '" + oldName + "' cannot be renamed to '" + newName
					+ "', becaue attempt to rename the folder returned false. Old folder: " + oldFolder.getAbsolutePath());

		log.debug(() -> "Renamed persistence stage base folder from '" + oldFolder.getAbsolutePath() + "' to: " + newFolder.getAbsolutePath());
	}

	@Override
	public PersistenceAppender newPersistenceAppender(String name) {
		G gmmlPersistence = appendNewGmmlPersistence(name);
		return enhanceAppenderIfNeeded(gmmlPersistence);
	}

	@Override
	public void mergeStage(String source, String target) {
		G sourcePersistence = getGmmlPersistence(source);
		G targetPersistence = getGmmlPersistence(target);

		Stream<File> sourceStageFiles = sourcePersistence.getGmmlStageFiles();

		mergeManipulationsTo(sourceStageFiles, targetPersistence);

		if (TRUNK_STAGE.equals(source))
			sourcePersistence.getVariablesMapStream() // purge the maps
					.forEach(Map::clear);
		else
			deleteGmmlPersistence(source, sourcePersistence);
	}

	private static void mergeManipulationsTo(Stream<File> stageFiles, PersistenceAppender appender) {
		Iterator<File> stageIt = stageFiles.iterator();

		StringJoiner sj = new StringJoiner(", ");

		while (stageIt.hasNext()) {
			File stageFile = stageIt.next();

			if (stageFile == null || !stageFile.exists())
				continue;

			appendManipulations(stageFile, appender);

			if (!stageFile.delete())
				sj.add("Unable to delete file: " + stageFile.getAbsolutePath());
		}

		if (sj.length() > 0)
			throw new GenericModelException("Error(s) while merging manipulation file(s): " + sj.toString());
	}

	private static void appendManipulations(File stageFile, PersistenceAppender appender) {
		BufferedManipulationAppender bufferedAppender = new BufferedManipulationAppender(appender, ManipulationMode.REMOTE);

		CsaPersistenceTools.parseGmmlFile(stageFile, bufferedAppender::append);

		bufferedAppender.flush();
	}

	private void deleteGmmlPersistence(String stageName, G sourcePersistence) {
		initializersByName.remove(stageName);
		configuredInitializers.remove(sourcePersistence);

		File stageBaseFolder = newStageBaseFolder(stageName);
		if (!stageBaseFolder.delete())
			log.error("Unable to delete stage folder: " + stageBaseFolder.getAbsolutePath());
	}

	@Override
	public void reset() {
		/* We only nullify 'configuredInitializers', as the caller is expected to do re-initialization right away, which leads to
		 * ensureConfigurationProcessed and thus update of everything. */
		deleteStageFolders();
		configuredInitializers = null;
	}

	private void deleteStageFolders() {
		Set<String> namesToKeep = getStageNamesToKeepOnReset(statePersistence.readOriginalConfiguration());

		for (PersistenceInitializer initializer : configuredInitializers)
			if (initializer instanceof AbstractGmmlManipulationPersistence)
				if (!namesToKeep.contains(getInitializerName(initializer)))
					deleteStageFolders((AbstractGmmlManipulationPersistence) initializer);
	}

	private void deleteStageFolders(AbstractGmmlManipulationPersistence initializer) {
		Holder<File> gmmlDir = new Holder<>();

		boolean allDeleted = !initializer.getGmmlStageFiles() //
				.peek(f -> gmmlDir.accept(f.getParentFile())) //
				.peek(this::deleteStageFile) //
				.filter(File::exists) //
				.findFirst() //
				.isPresent();

		if (allDeleted)
			deleteStageFile(gmmlDir.get());
	}

	private void deleteStageFile(File file) {
		if (file.exists() && !file.delete())
			log.warn("Issue when resetting manipulation persistence. Cannot delete file: " + file.getAbsolutePath()
					+ ". Reset will proceed, but manual clean-up might be needed.");
	}

	@Override
	public Stream<Resource> getResourcesForStage(String name) {
		return getStageFiles(name).map(AbstractManipulationPersistence::toFileResource);
	}

	private static FileResource toFileResource(File file) {
		if (file == null || !file.exists())
			return null;

		FileResource result = FileResource.T.create();
		result.setPath(file.getAbsolutePath());
		result.setName(file.getName());
		result.setMimeType("gm/man");

		return result;
	}

	@Override
	public Stream<Supplier<Set<GenericEntity>>> getModifiedEntitiesForStage(String name) {
		return getStageFiles(name).map(gmmlFile -> new ModifiedEntitiesSupplier(gmmlFile, csaSession));
	}

	private Stream<File> getStageFiles(String name) {
		// check it exists
		PersistenceInitializer initializer = getInitializer(name);
		if (!(initializer instanceof AbstractGmmlManipulationPersistence))
			return Stream.empty();

		return ((AbstractGmmlManipulationPersistence) initializer).getGmmlStageFiles();
	}

	private G getGmmlPersistence(String name) {
		PersistenceInitializer result = getInitializer(name);

		if (!(result instanceof AbstractGmmlManipulationPersistence))
			throw new GenericModelException("Initializer with name '" + name + "' is not a ManInitializer, but: " + result);

		return (G) result;
	}

	private PersistenceInitializer getInitializer(String name) {
		return initializersByName.computeIfAbsent(name, n -> {
			throw new GenericModelException("No Initializer found with name: " + name);
		});
	}

	// ############################################################
	// ## . . . . . . . . . Initializer methods . . . . . . . . .##
	// ############################################################

	@Override
	public void initializeModels(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		onInitializationStart();

		initializeMetaModels(staticInitializers, context);
		initializeMetaModels(configuredInitializers, context);
		initializeMetaModels(staticPostInitializers, context);
	}

	protected void initializeMetaModels(List<PersistenceInitializer> initializers, PersistenceInitializationContext context)
			throws ManipulationPersistenceException {

		for (PersistenceInitializer initializer : initializers) {
			context.setCurrentPersistenceStage(initializer.getPersistenceStage());
			initializer.initializeModels(context);
		}
	}

	@Override
	public void initializeData(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		initializeData(staticInitializers, context);
		initializeData(configuredInitializers, context);
		initializeData(staticPostInitializers, context);

		onInitializationEnd();
	}

	protected void initializeData(List<PersistenceInitializer> initializers, PersistenceInitializationContext context) {
		for (PersistenceInitializer initializer : initializers) {
			context.setCurrentPersistenceStage(initializer.getPersistenceStage());
			initializer.initializeData(context);
		}
	}

	protected void initializeModelAndData(PersistenceInitializationContext context) {
		onInitializationStart();

		initializeModelAndData(staticInitializers, context);
		initializeModelAndData(configuredInitializers, context);
		initializeModelAndData(staticPostInitializers, context);

		onInitializationEnd();
	}

	protected void initializeModelAndData(List<PersistenceInitializer> initializers, PersistenceInitializationContext context) {
		for (PersistenceInitializer initializer : initializers) {
			context.setCurrentPersistenceStage(initializer.getPersistenceStage());
			initializer.initializeModels(context);
			initializer.initializeData(context);
		}
	}

	private void releaseManagedInitializersIfNeeded() {
		for (ManagedValue<PersistenceInitializer> managedInitializer : managedInitializers)
			managedInitializer.release();

		managedInitializers = emptyList();
	}

	private void onInitializationStart() {
		ensureConfigurationProcessed();

		errorHandler.onStart();
	}

	private void onInitializationEnd() {
		releaseManagedInitializersIfNeeded();

		errorHandler.onEnd();
	}

	// ############################################################
	// ## . . . . . . . . . Configuration/Setup . . . . . . . . .##
	// ############################################################

	@Override
	public Stream<PersistenceStage> getPersistenceStages() {
		Objects.requireNonNull(appender, "This persistence was probably not initialized yet.");

		return Stream.concat(stagesOf(staticInitializers), stagesOf(configuredInitializers));
	}

	private static Stream<PersistenceStage> stagesOf(List<PersistenceInitializer> initializers) {
		return initializers.stream().map(PersistenceInitializer::getPersistenceStage);
	}

	@Override
	public PersistenceStage getPersistenceStage() {
		Objects.requireNonNull(appender, "Manipulation appender is null, this persistence was probably not initialized yet.");
		return appender.getPersistenceStage();
	}

	@Override
	public PersistenceAppender getPersistenceAppender() throws ManipulationPersistenceException {
		ensureConfigurationProcessed();
		return appender;
	}

	protected void ensureConfigurationProcessed() throws ManipulationPersistenceException {
		if (configuredInitializers != null)
			return;

		loadCollaborativeSmoodConfiguration();

		processCollaborativeSmoodConfiguration();

		ensureLastInitializerIsManInitializer();

		appender = enhanceAppenderIfNeeded(last(configuredInitializers));
	}

	private void loadCollaborativeSmoodConfiguration() {
		requireNonNull(statePersistence, "ManipulationPersistence misconfigured - no index file nor CsaStatePersistence set.");

		collaborativeSmoodConfiguration = statePersistence.readConfiguration();

		requireNonNull(collaborativeSmoodConfiguration, "No configuration delivered by given configuration persistence.");
	}

	private void processCollaborativeSmoodConfiguration() {
		managedInitializers = collaborativeSmoodConfiguration.getInitializers().stream() //
				.filter(si -> !si.getSkip()) //
				.map(this::resolveAndValidateSmoodInitializer) //
				.filter(x -> x != null) //
				.collect(Collectors.toList());

		configuredInitializers = managedInitializers.stream() //
				.map(ManagedValue::get) //
				.collect(Collectors.toList());

		configuredInitializers.stream() //
				.forEach(pi -> initializersByName.put(getInitializerName(pi), pi));
	}

	private String getInitializerName(PersistenceInitializer pi) {
		return pi.getPersistenceStage().getName();
	}

	private ManagedValue<PersistenceInitializer> resolveAndValidateSmoodInitializer(SmoodInitializer si) {
		ManagedValue<PersistenceInitializer> mi = resolveSmoodInitializer(si);
		validateInitializer(mi, si);

		return mi;
	}

	private ManagedValue<PersistenceInitializer> resolveSmoodInitializer(SmoodInitializer si) {
		si.normalize();

		if (si instanceof ManInitializer)
			return resolveMan((ManInitializer) si);

		else if (si instanceof CustomInitializer)
			return resolveCustom((CustomInitializer) si);

		else
			throw new IllegalArgumentException("Unsupported SmoodInitializer: " + si);
	}

	private ManagedValue<PersistenceInitializer> resolveMan(ManInitializer mi) {
		return ManagedValue.of(newGmmlPersistence(mi.getName()));
	}

	private ManagedValue<PersistenceInitializer> resolveCustom(CustomInitializer ci) {
		return customInitializerResolver.apply(ci);
	}

	private void validateInitializer(ManagedValue<PersistenceInitializer> mi, SmoodInitializer si) {
		if (mi == null)
			return; // TODO maybe don't allow null at all? Why are they allowed anyway? How did it start?

		PersistenceInitializer pi = mi.get();
		if (pi == null)
			throw new NullPointerException("Smood initializer resolves to a null persistence initializer: " + si);

		PersistenceStage stage = pi.getPersistenceStage();
		if (stage == null)
			throw new NullPointerException("Persistence initializer '" + pi + "(" + pi.getClass().getName()
					+ ") has null as it's persitenceStage. Configured smood initializer: " + si);
	}

	private void ensureLastInitializerIsManInitializer() {
		if (configuredInitializers.isEmpty() || !(last(configuredInitializers) instanceof PersistenceAppender))
			appendNewGmmlPersistence(TRUNK_STAGE);
	}

	private PersistenceAppender enhanceAppenderIfNeeded(PersistenceAppender appender) {
		PersistenceAppender result = appender;

		if (appendedManipulationListener != null)
			result = new NotifyingPersistenceAppender(result, appendedManipulationListener);

		if (manipulationFilter != null)
			result = new FilteringPersistenceAppender(result, manipulationFilter);

		return result;
	}

	// ###############################################
	// ## . . . . . . . . . Helpers . . . . . . . . ##
	// ###############################################

	private G appendNewGmmlPersistence(String name) {
		G gmmlPersistence = newGmmlPersistence(name);
		configuredInitializers.add(gmmlPersistence);
		initializersByName.put(name, gmmlPersistence);

		return gmmlPersistence;
	}

	/** Never returns <tt>null</tt>. */
	private G newGmmlPersistence(String name) {
		G persistence = createGmmlPersistence();
		persistence.configureStage(newStageBaseFolder(name), name);
		persistence.setGmmlErrorHandler(errorHandler);
		persistence.setProblematicEntitiesRegistry(problematicEntitiesRegistry);
		persistence.setModelOracle(modelOracle);
		persistence.setCreatedEntitiesSupplier(() -> csa.getCreatedEntitiesForStage(name));

		persistence.getPersistenceStage();

		return persistence;
	}

	private File newStageBaseFolder(String name) {
		String fileName = toLegalFileName(name);

		return new File(storageBase, fileName);
	}

	public static String toLegalFileName(String name) {
		return FileTools.replaceIllegalCharactersInFileName(name, "_");
	}

	protected abstract G createGmmlPersistence();

	@Override
	public String toString() {
		return "AbstractManipulationPersistence: Storage Base: " + storageBase;
	}
}
