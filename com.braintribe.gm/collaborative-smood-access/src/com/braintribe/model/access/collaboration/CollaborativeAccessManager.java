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

import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.utils.lcd.CollectionTools2.findFirstIndex;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static java.util.Collections.emptyList;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.UnsupportedEnumException;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.cortexapi.access.collaboration.CollaborativePersistenceRequest;
import com.braintribe.model.cortexapi.access.collaboration.CollaborativeStageData;
import com.braintribe.model.cortexapi.access.collaboration.CollaborativeStageStats;
import com.braintribe.model.cortexapi.access.collaboration.GetCollaborativeStageData;
import com.braintribe.model.cortexapi.access.collaboration.GetCollaborativeStageStats;
import com.braintribe.model.cortexapi.access.collaboration.GetModifiedModelsForStage;
import com.braintribe.model.cortexapi.access.collaboration.MergeCollaborativeStage;
import com.braintribe.model.cortexapi.access.collaboration.MergeCollaborativeStageToPredecessor;
import com.braintribe.model.cortexapi.access.collaboration.PushCollaborativeStage;
import com.braintribe.model.cortexapi.access.collaboration.RenameCollaborativeStage;
import com.braintribe.model.csa.CollaborativeSmoodConfiguration;
import com.braintribe.model.csa.ManInitializer;
import com.braintribe.model.csa.SmoodInitializer;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.meta.GmCustomModelElement;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.manipulation.parser.api.GmmlManipulatorErrorHandler;
import com.braintribe.model.processing.manipulation.parser.impl.listener.error.TrackingErrorHandler;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.collaboration.CollaborativeAccess;
import com.braintribe.model.processing.session.api.collaboration.StageStats;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.api.ResourceBuilder;
import com.braintribe.model.resource.source.FileSystemSource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;

/**
 * Companion of a {@link CollaborativeSmoodAccess} which (alongside some minor tasks) takes care of evaluating
 * {@link CollaborativePersistenceRequest}s.
 * 
 * @see #process(ServiceRequestContext, CollaborativePersistenceRequest)
 * @see #resolveResourceFile(String)
 * 
 * @author peter.gazdik
 */
public class CollaborativeAccessManager implements ServiceProcessor<CollaborativePersistenceRequest, Object>, InitializationAware {

	private CollaborativeAccess access;
	private CsaStatePersistence statePersistence;
	private Function<String, Path> sourcePathResolver;
	private GmmlManipulatorErrorHandler errorHandler;

	private CollaborativeSmoodConfiguration configuration;

	private Resource startupIssues;
	private ResourceBuilder resourceBuilder;

	/** The access does not have to be initialized yet. */
	@Required
	public void setAccess(CollaborativeAccess access) {
		this.access = access;
	}

	@Required
	public void setCsaStatePersistence(CsaStatePersistence statePersistence) {
		this.statePersistence = statePersistence;
	}

	/** A function which can translate a {@link FileSystemSource#getPath() source path} to an actual file system {@link Path}. */
	@Required
	public void setSourcePathResolver(Function<String, Path> sourcePathResolver) {
		this.sourcePathResolver = sourcePathResolver;
	}

	@Configurable
	public void setGmmlErrorHandler(GmmlManipulatorErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	@Configurable
	public void setResourceBuilder(ResourceBuilder resourceBuilder) {
		this.resourceBuilder = resourceBuilder;
	}

	@Override
	public void postConstruct() {
		configuration = statePersistence.readConfiguration();
	}

	public CollaborativeSmoodConfiguration getConfiguration() {
		return configuration;
	}

	// #############################################################
	// ## . . . . . . ServiceProcessor implementation . . . . . . ##
	// #############################################################

	@Override
	public Object process(ServiceRequestContext requestContext, CollaborativePersistenceRequest request) {
		switch (request.collaborativeRequestType()) {
			case GetModifiedModelsForStage:
				return getModifiedModelsForStage((GetModifiedModelsForStage) request);
			case GetInitializers:
				return getInitializers();
			case GetStageData:
				return getStageData((GetCollaborativeStageData) request);
			case GetStageStats:
				return getStageStats((GetCollaborativeStageStats) request);
			case GetInitializationIssues:
				return getInitializationIssues();
			case PushStage:
				return pushStage((PushCollaborativeStage) request);
			case RenameStage:
				return renameStage((RenameCollaborativeStage) request);
			case Reset:
				return reset();
			case MergeStage:
				return mergeStage((MergeCollaborativeStage) request);
			case MergeStageToPredecessor:
				return mergeStageToPredecessor((MergeCollaborativeStageToPredecessor) request);
			default:
				throw new UnsupportedEnumException(request.collaborativeRequestType());
		}
	}

	private List<GmMetaModel> getModifiedModelsForStage(GetModifiedModelsForStage request) {
		Stream<Supplier<Set<GenericEntity>>> modifiedEntities = access.getModifiedEntitiesForStage(request.getName());

		Supplier<Set<GenericEntity>> modifiedModelEntities = getModelPart(modifiedEntities);

		if (modifiedEntities == null)
			return emptyList();
		else
			return findModels(modifiedModelEntities.get());
	}

	private static <E> E getModelPart(Stream<E> stream) {
		Iterator<E> streamIterator = stream.iterator();

		if (!streamIterator.hasNext())
			return null;

		E result = streamIterator.next();

		if (streamIterator.hasNext())
			result = streamIterator.next();

		return result;
	}

	private List<GmMetaModel> findModels(Set<GenericEntity> entities) {
		return entities.stream() //
				.map(this::extractModelIfRelevantMetaModelEntity) //
				.filter(e -> e != null) //
				.collect(Collectors.toList());
	}

	private GmMetaModel extractModelIfRelevantMetaModelEntity(GenericEntity e) {
		if (e instanceof GmMetaModel)
			return (GmMetaModel) e;

		if (e instanceof GmCustomModelElement)
			return ((GmCustomModelElement) e).declaringModel();

		return null;
	}

	private List<SmoodInitializer> getInitializers() {
		return configuration.getInitializers().stream() //
				.filter(i -> !i.getSkip()) //
				.collect(Collectors.toList());
	}

	// ###############################################
	// ## . . . . . . . GetStageData . . . . . . . .##
	// ###############################################

	private CollaborativeStageData getStageData(GetCollaborativeStageData request) {
		String stageName = request.getName();
		SmoodInitializer si = getInitializer(stageName);

		CollaborativeStageData result = CollaborativeStageData.T.create();

		if (si instanceof ManInitializer) {
			Iterator<Resource> resourcesIt = access.getResourcesForStage(si.getName()).iterator();

			if (resourcesIt.hasNext())
				result.setDataResource(resourcesIt.next());

			if (resourcesIt.hasNext())
				result.setModelResource(resourcesIt.next());
		}

		Set<Resource> stageResources = queryResourcesForStage(stageName);
		Map<String, Resource> relativePathToFileResource = extractFileResources(stageResources);

		result.setContentResources(relativePathToFileResource);

		return result;
	}

	private SmoodInitializer getInitializer(String name) {
		return initializers(name) //
				.findFirst() //
				.orElseThrow(() -> new GenericModelException("No ManInitializer found with name: " + name));
	}

	private Set<Resource> queryResourcesForStage(String stageName) {
		SelectQuery query = new SelectQueryBuilder().select("r").from(Resource.T, "r").tc(scalarAndIdAndResourceSourceTc()).done();
		List<Resource> allResources = (List<Resource>) (List<?>) ((IncrementalAccess) access).query(query).getResults();

		PersistenceStage stage = access.getStageByName(stageName);

		return allResources.stream() //
				.filter(r -> isFromStage(r, stage)) //
				.collect(Collectors.toSet());
	}

	private Map<String, Resource> extractFileResources(Set<Resource> resources) {
		StandardCloningContext cloningContext = resourceToFileResourceCloningContext();

		Map<String, Resource> result = newMap();

		for (Resource resource : resources) {
			ResourceSource resourceSource = resource.getResourceSource();
			if (!(resourceSource instanceof FileSystemSource))
				continue;

			String relativePath = ((FileSystemSource) resourceSource).getPath();

			FileResource fileResource = resource.clone(cloningContext);
			fileResource.setPath(resolveResourceFile(relativePath).getAbsolutePath());

			result.put(relativePath, fileResource);
		}

		return result;
	}

	/** @return File within the resources folder, based on the given path relative to this folder. */
	public File resolveResourceFile(String relativePath) {
		return sourcePathResolver.apply(relativePath).toFile();
	}

	private TraversingCriterion scalarAndIdAndResourceSourceTc() {
		// @formatter:off
		return TC.create()
			.negation()
				.disjunction()
					.property(GenericEntity.id)
					.property(Resource.resourceSource)
					.typeCondition(isKind(TypeKind.scalarType))
				.close()
			.done();
		// @formatter:on
	}

	private StandardCloningContext resourceToFileResourceCloningContext() {
		return new StandardCloningContext() {
			@Override
			public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
				return FileResource.T.create();
			}

			@Override
			public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property,
					GenericEntity instanceToBeCloned, GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {
				return property.getType().isScalar() || property.isIdentifier();
			}
		};
	}

	private boolean isFromStage(GenericEntity entity, PersistenceStage stage) {
		return access.getStageForReference(entity.reference()) == stage;
	}

	// ###############################################
	// ## . . . . . . . GetStageStats . . . . . . . ##
	// ###############################################

	private CollaborativeStageStats getStageStats(GetCollaborativeStageStats request) {
		StageStats stageStats = access.getStageStats(request.getName());

		CollaborativeStageStats result = CollaborativeStageStats.T.create();
		result.setInstantiations(stageStats.getInstantiations());
		result.setDeletes(stageStats.getDeletes());
		result.setUpdates(stageStats.getUpdates());

		return result;
	}

	// ###############################################
	// ## . . . . . GetInitializationIssues . . . . ##
	// ###############################################

	private Resource getInitializationIssues() {
		if (startupIssues != null)
			return startupIssues;

		if (!(errorHandler instanceof TrackingErrorHandler) || resourceBuilder == null)
			return null;

		TrackingErrorHandler teh = (TrackingErrorHandler) errorHandler;
		if (!teh.hasErrors())
			return null;

		return resourceBuilder.newResource() //
				.withName(access.getAccessId() + "-startup-issues") //
				.usingWriter(teh::writeReport);
	}

	// ###############################################
	// ## . . . . . . . . PushStage . . . . . . . . ##
	// ###############################################

	private Boolean pushStage(PushCollaborativeStage request) {
		runWithWriteLock(() -> pushStage(request.getName()));

		return Boolean.TRUE;
	}

	private void pushStage(String name) {
		checkStageNotExistsYet(name);
		addNewManInitializerToConfiguration(name);
		pushStageInPersistence(name);
	}

	private void addNewManInitializerToConfiguration(String name) {
		ManInitializer initializer = ManInitializer.T.create();
		initializer.setName(name);

		configuration.getInitializers().add(initializer);

		storeConfiguration();
	}

	private void pushStageInPersistence(String name) {
		access.pushPersistenceStage(name);
	}

	// ###############################################
	// ## . . . . . . . RenameStage . . . . . . . . ##
	// ###############################################

	private Object renameStage(RenameCollaborativeStage request) {
		runWithWriteLock(() -> renameStage(request.getOldName(), request.getNewName()));

		return Boolean.TRUE;
	}

	private void renameStage(String oldName, String newName) {
		checkStageNotExistsYet(newName);
		renameManInitializerInConfiguration(oldName, newName);
		renameStageInPersistence(oldName, newName);
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
		return configuration.getInitializers().stream() //
				.filter(i -> name.equals(i.getName()));
	}

	private void renameStageInPersistence(String oldName, String newName) {
		access.renamePersistenceStage(oldName, newName);
	}

	// ###############################################
	// ## . . . . . . . MergeStage . . . . . . . . .##
	// ###############################################

	private Object mergeStageToPredecessor(MergeCollaborativeStageToPredecessor request) {
		String sourceName = request.getName();
		String targetName = getPredecessorName(sourceName);

		return mergeStage(sourceName, targetName);
	}

	private String getPredecessorName(String name) {
		int i = findFirstIndex(configuration.getInitializers(), j -> name.equals(j.getName()));
		if (i == 0)
			throw new IllegalArgumentException("Cannot merge stage to predecessor, as this is the first stage: " + name);
		if (i < 0)
			throw new IllegalArgumentException("Cannot merge stage to predecessor, no stage found for name: " + name);

		SmoodInitializer si = configuration.getInitializers().get(i - 1);
		if (!(si instanceof ManInitializer))
			throw new IllegalArgumentException(
					"Cannot merge stage '" + name + "' to predecessor, as the predecessor is not a GMML persistence, but: " + si);

		return si.getName();
	}

	private Boolean reset() {
		runWithWriteLock(this::w_reset);

		return Boolean.TRUE;
	}

	private void w_reset() {
		configuration = statePersistence.readOriginalConfiguration();
		statePersistence.writeConfiguration(configuration);
		access.reset();
	}

	private Object mergeStage(MergeCollaborativeStage request) {
		return mergeStage(request.getSource(), request.getTarget());
	}

	private Boolean mergeStage(String sourceName, String targetName) {
		runWithWriteLock(() -> w_mergeStage(sourceName, targetName));
		return Boolean.TRUE;
	}

	private void w_mergeStage(String sourceName, String targetName) {
		access.mergeStage(sourceName, targetName);
		removeManInitializerFromConfigurationIfNotTrunk(sourceName);
	}

	private void removeManInitializerFromConfigurationIfNotTrunk(String source) {
		if ("trunk".equals(source))
			return;

		configuration.getInitializers().removeIf(si -> source.equals(si.getName()));
		storeConfiguration();
	}

	// ###############################################
	// ## . . . . . . . . . Common . . . . . . . . .##
	// ###############################################

	private void runWithWriteLock(Runnable task) {
		Lock writeLock = access.getLock().writeLock();

		writeLock.lock();
		try {
			task.run();

		} finally {
			writeLock.unlock();
		}
	}

	private void storeConfiguration() {
		statePersistence.writeConfiguration(configuration);
	}

}
