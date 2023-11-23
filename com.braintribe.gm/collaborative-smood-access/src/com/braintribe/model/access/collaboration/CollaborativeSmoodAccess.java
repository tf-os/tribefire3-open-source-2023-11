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
package com.braintribe.model.access.collaboration;

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.compound;
import static com.braintribe.model.processing.manipulation.basic.tools.ManipulationTools.asManipulation;
import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.smood.basic.AbstractSmoodAccess;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.cortexapi.access.collaboration.CollaborativePersistenceRequest;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationComment;
import com.braintribe.model.generic.manipulation.util.ManipulationBuilder;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.collaboration.CollaborativeAccess;
import com.braintribe.model.processing.session.api.collaboration.CollaborativeManipulationPersistence;
import com.braintribe.model.processing.session.api.collaboration.PersistenceAppender;
import com.braintribe.model.processing.session.api.collaboration.PersistenceAppender.AppendedSnippet;
import com.braintribe.model.processing.session.api.collaboration.StageStats;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.managed.ManipulationMode;
import com.braintribe.model.processing.session.api.managed.ManipulationReport;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.persistence.CommitContext;
import com.braintribe.model.processing.smood.EmptyManipulationApplicationListener;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.FileSystemSource;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.lcd.StopWatch;
import com.braintribe.utils.lcd.StringTools;

/**
 * {@link CollaborativeAccess} implementation that is backed by {@link Smood}.
 * <p>
 * INITIALIZATION: To initialize correctly, all the relevant properties have to be set and the {@link #postConstruct()}
 * method has to be called. Note that this method is not thread-safe.
 */
public class CollaborativeSmoodAccess extends AbstractSmoodAccess implements CollaborativeAccess, InitializationAware {

	private static final Logger log = Logger.getLogger(CollaborativeSmoodAccess.class);

	protected Smood database;

	protected CollaborativeManipulationPersistence manipulationPersistence;
	protected PersistenceAppender appender;
	protected PersistenceStage currentStage;

	private GmMetaModel metaModel;
	private String selfModelName;

	protected CollaborationSmoodSession session;

	private ModelAccessory modelAccessory;

	private final StageRegistry stageRegistry = new StageRegistry();

	private Map<String, Object> initializerAttributes;

	@Required
	public void setManipulationPersistence(CollaborativeManipulationPersistence manipulationPersistence) {
		this.manipulationPersistence = manipulationPersistence;
	}

	@Required
	public void setCollaborativeRequestProcessor(CollaborativeAccessManager collaborativeAccessManager) {
		this.registerCustomPersistenceRequestProcessor(CollaborativePersistenceRequest.T, collaborativeAccessManager);
	}

	/** Either this or selfModelName should be set; */
	@Configurable
	public void setMetaModel(GmMetaModel metaModel) {
		this.metaModel = metaModel;
	}

	/** @see #setMetaModel */
	@Configurable
	public void setSelfModelName(String selfModelName) {
		this.selfModelName = selfModelName;
	}

	@Configurable
	public void setInitializerAttributes(Map<String, Object> initializerAttributes) {
		this.initializerAttributes = initializerAttributes;
	}

	/** @see com.braintribe.model.access.IncrementalAccess#getMetaModel() */
	@Override
	public GmMetaModel getMetaModel() {
		return metaModel;
	}

	@Override
	public ReadWriteLock getLock() {
		return readWriteLock;
	}

	@Override
	public StageStats getStageStats(String name) {
		return withLock(readLock, () -> stageRegistry.getStageStats(name));
	}

	@Override
	public void pushPersistenceStage(String name) {
		CommonTools.requireNonEmpty(name, "Persistence stage name cannot be null");
		withLock(writeLock, () -> w_pushPersistenceStage(name));
	}

	private Void w_pushPersistenceStage(String name) {
		appender = manipulationPersistence.newPersistenceAppender(name);
		currentStage = appender.getPersistenceStage();
		stageRegistry.onNewStage(currentStage);

		return null;
	}

	@Override
	public void renamePersistenceStage(String oldName, String newName) {
		withLock(writeLock, () -> w_renamePersistenceStage(oldName, newName));
	}

	private Void w_renamePersistenceStage(String oldName, String newName) {
		manipulationPersistence.renamePersistenceStage(oldName, newName);
		stageRegistry.onPersistenceStageRename(oldName, newName);
		return null;
	}

	@Override
	public Stream<Resource> getResourcesForStage(String name) {
		return manipulationPersistence.getResourcesForStage(name);
	}

	@Override
	public Stream<Supplier<Set<GenericEntity>>> getModifiedEntitiesForStage(String name) {
		return manipulationPersistence.getModifiedEntitiesForStage(name);
	}

	@Override
	public Set<GenericEntity> getCreatedEntitiesForStage(String name) {
		return stageRegistry.getEntitiesForStage(name);
	}

	@Override
	public PersistenceStage getStageByName(String name) {
		return stageRegistry.getStage(name);
	}

	@Override
	public PersistenceStage findStageForReference(EntityReference reference) {
		GenericEntity entity = database.findEntity(reference);
		return entity == null ? null : findStageForEntity(entity);
	}

	public PersistenceStage findStageForEntity(GenericEntity entity) {
		return stageRegistry.findStage(entity);
	}

	@Override
	public PersistenceStage getStageForReference(EntityReference reference) {
		GenericEntity entity = database.getEntity(reference); // This throws EntityNotfoundException
		return stageRegistry.getStage(entity);
	}

	@Override
	public void mergeStage(String source, String target) {
		withLock(writeLock, () -> w_mergeStage(source, target));
	}

	private Object w_mergeStage(String source, String target) {
		manipulationPersistence.mergeStage(source, target);
		stageRegistry.mergeFirstStageToSecond(source, target);
		return null;
	}

	@Override
	public void reset() {
		withLock(writeLock, this::w_reset);
	}

	private Void w_reset() {
		database = null;
		manipulationPersistence.reset();
		initializeDatabase();

		return null;
	}

	@Override
	protected Smood getDatabase() {
		return database;
	}

	public ManagedGmSession getSmoodSession() {
		return session;
	}

	@Override
	public <R> R readWithCsaSession(Function<ManagedGmSession, R> readingFunction) {
		return withLock(readLock, () -> readingFunction.apply(session));
	}

	/** Initializes the inner Smood. Not thread safe! */
	@Override
	public void postConstruct() {
		// in case somebody would called this twice
		if (database == null)
			initializeDatabase();
	}

	private void initializeDatabase() {
		try {
			tryInitializeDatabase();

		} catch (RuntimeException e) {
			throw Exceptions.contextualize(e, "Error while initializing Collaborative Smood Access: " + getAccessId());
		}
	}

	private void tryInitializeDatabase() {
		session = newSession();

		Smood smood = new Smood(session, EmptyReadWriteLock.INSTANCE);
		smood.setLocaleProvider(localeProvider);
		smood.setDefaultPartition(defaultPartition);
		smood.setUseGlobalIdAsId(true);

		session.setSmood(smood);

		initData_WithTimer(smood);
		appender = manipulationPersistence.getPersistenceAppender();
		smood.setLock(readWriteLock);

		database = smood;

		manipulationPersistence.onCollaborativeAccessInitialized(this, session);

		if (modelAccessory != null)
			onModelAccessoryOutdated();
	}

	protected CollaborationSmoodSession newSession() {
		return new CollaborationSmoodSession();
	}

	private void initData_WithTimer(Smood smood) {
		StopWatch sw = new StopWatch();

		initData(smood);

		log.debug(() -> "Access [" + getAccessId() + "] was initialized in " + sw.getElapsedTime() + "ms, with " + smood.getAllEntities().size()
				+ " entities.");
	}

	private void initData(Smood smood) {
		SmoodInitializationContextImpl context = new SmoodInitializationContextImpl(session, stageRegistry, getAccessId());
		context.setAttributes(initializerAttributes);

		manipulationPersistence.initializeModels(context);

		// If we have a meta-model, we set it here so that all the data is correctly indexed
		configureMetaModelIfGivenExplicitly(smood);

		manipulationPersistence.initializeData(context);

		context.close();

		currentStage = manipulationPersistence.getPersistenceStage();

		stageRegistry.indexStages(manipulationPersistence.getPersistenceStages());

		/* If the model is in this smood, we set it after we init data, cause there might be index info in the data */
		configureMetaModelIfGivenByName(smood);

		smood.ensureIds();
	}

	private void configureMetaModelIfGivenExplicitly(Smood smood) {
		if (selfModelName == null && metaModel != null)
			smood.setMetaModel(metaModel);
	}

	private void configureMetaModelIfGivenByName(Smood smood) {
		if (selfModelName != null) {
			smood.setSelfMetaModel(selfModelName);
			metaModel = smood.getMetaModel();
		}
	}

	// ####################################################
	// ## . . . . . . . Apply Manipulation . . . . . . . ##
	// ####################################################

	@Override
	public ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest) throws ModelAccessException {
		ManipulationReport report = applyInSmoodAndPersist(manipulationRequest);

		return buildManipulationResponse(report);
	}

	protected ManipulationReport applyInSmoodAndPersist(ManipulationRequest manipulationRequest) {
		writeLock.lock();

		try {
			return w_applyInSmoodAndPersist(manipulationRequest);

		} finally {
			writeLock.unlock();
		}
	}

	protected ManipulationReport w_applyInSmoodAndPersist(ManipulationRequest manipulationRequest) {
		CsaCollector csaCollector = new CsaCollector();
		prependCommentIfEligible(csaCollector, manipulationRequest);
		ManipulationReport report = w_applyInSmood(csaCollector, manipulationRequest);
		w_processAppliedLocalManipulations(csaCollector.manipulations);

		return report;
	}

	private void prependCommentIfEligible(CsaCollector csaCollector, ManipulationRequest mr) {
		Map<String, Object> metaData = mr.getMetaData();

		if (isEmpty(metaData))
			return;

		String text = (String) metaData.get(CommitContext.COMMENT_META_DATA);
		if (StringTools.isEmpty(text))
			return;

		ManipulationComment comment = ManipulationBuilder.comment(text);
		csaCollector.manipulations.add(comment);
	}

	private ManipulationReport w_applyInSmood(CsaCollector csaCollector, ManipulationRequest manipulationRequest) {
		database.getGmSession().listeners().add(csaCollector);

		try {
			return database.apply() //
					.generateId(true) //
					.checkRefereesOnDelete(true) //
					.manipulationApplicationListener(csaCollector) //
					.request2(manipulationRequest);

		} finally {
			database.getGmSession().listeners().remove(csaCollector);
		}
	}

	public static class CsaCollector extends EmptyManipulationApplicationListener implements ManipulationListener {
		public final List<Manipulation> manipulations = newList();
		private boolean enabled = true;

		@Override
		public void onBeforePersistenceIdAssignment() {
			enabled = false;
		}

		@Override
		public void onAfterPersistenceIdAssignment() {
			enabled = true;
		}

		@Override
		public void noticeManipulation(Manipulation manipulation) {
			if (enabled)
				manipulations.add(manipulation);
		}
	}

	private void w_processAppliedLocalManipulations(List<Manipulation> manipulations) {
		if (manipulations.isEmpty())
			return;

		Manipulation manipulation = asManipulation(manipulations);

		w_persistAppliedLocalManipulation(manipulation);
	}

	protected AppendedSnippet[] w_persistAppliedLocalManipulation(Manipulation manipulation) {
		// update stage info / statistics
		w_notifyStageRegistry(manipulation);

		// persist manipulations
		try {
			return appender.append(manipulation, ManipulationMode.LOCAL);

		} catch (Exception e) {
			throw new ModelAccessException("An error while persisting manipulations.", e);
		}
	}

	protected void w_notifyStageRegistry(Manipulation manipulation) {
		stageRegistry.onManipulation(manipulation, currentStage);
	}

	private ManipulationResponse buildManipulationResponse(ManipulationReport report) {
		ManipulationResponse response = report.getManipulationResponse();
		Collection<GenericEntity> newEntities = report.getInstantiations().values();

		List<Manipulation> partitionAssignments = createPartitionAssignmentsWhereNeeded(newEntities);
		appendPartitionAssignmentsToResponse(partitionAssignments, response);

		return response;
	}

	private List<Manipulation> createPartitionAssignmentsWhereNeeded(Collection<GenericEntity> newEntities) {
		List<Manipulation> partitionAssignments = newList();
		for (GenericEntity newEntity : newEntities)
			if (newEntity.getPartition() == null)
				partitionAssignments.add(createPartitionAssignmentManipulation(newEntity));

		return partitionAssignments;
	}

	private void appendPartitionAssignmentsToResponse(List<Manipulation> partitionAssignments, ManipulationResponse response) {
		if (!partitionAssignments.isEmpty()) {
			List<Manipulation> newInducedManipulations = newList();
			Manipulation currentInducedManipulation = response.getInducedManipulation();
			if (currentInducedManipulation != null)
				newInducedManipulations.add(currentInducedManipulation);

			newInducedManipulations.addAll(partitionAssignments);
			response.setInducedManipulation(compound(newInducedManipulations));
		}
	}

	public boolean experimentalLazyLoad(@SuppressWarnings("unused") FileSystemSource source) {
		return false;
	}

	// ####################################################
	// ## . . . . . . Handling model changes . . . . . . ##
	// ####################################################

	public void setModelAccessory(ModelAccessory modelAccessory) {
		this.modelAccessory = modelAccessory;

		modelAccessory.addListener(this::onModelAccessoryOutdated);
	}

	private void onModelAccessoryOutdated() {
		withLock(writeLock, this::w_onModelAccessoryOutdated);
	}

	private Void w_onModelAccessoryOutdated() {
		database.setCmdResolver(modelAccessory.getCmdResolver());
		metaModel = modelAccessory.getModel();
		manipulationPersistence.setModelOracle(modelAccessory.getOracle());

		return null;
	}

	private static <T> T withLock(Lock lock, Supplier<T> callable) {
		lock.lock();
		try {
			return callable.get();

		} finally {
			lock.unlock();
		}
	}

}
