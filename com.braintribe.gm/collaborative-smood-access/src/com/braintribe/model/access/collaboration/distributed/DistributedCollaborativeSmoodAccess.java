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
package com.braintribe.model.access.collaboration.distributed;

import static com.braintribe.model.processing.manipulation.basic.tools.ManipulationTools.asManipulation;
import static com.braintribe.model.processing.query.stringifier.BasicQueryStringifier.print;
import static com.braintribe.utils.lcd.CommonTools.equalsOrBothNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

import com.braintribe.cfg.DestructionAware;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.UnsupportedEnumException;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.collaboration.CollaborativeAccessManager;
import com.braintribe.model.access.collaboration.CollaborativeSmoodAccess;
import com.braintribe.model.access.collaboration.CsaStatePersistence;
import com.braintribe.model.access.collaboration.binary.CsaBinaryTools;
import com.braintribe.model.access.collaboration.distributed.api.DcsaIterable;
import com.braintribe.model.access.collaboration.distributed.api.DcsaSharedStorage;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaAppendDataManipulation;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaAppendModelManipulation;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaDeleteResource;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaManagePersistence;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaOperation;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaResourceBasedOperation;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaStoreResource;
import com.braintribe.model.access.collaboration.distributed.tools.CsaOperationBuilder;
import com.braintribe.model.access.collaboration.offline.CollaborativeAccessOfflineManager;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.cortexapi.access.collaboration.CollaborativePersistenceRequest;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.resource.persistence.BinaryPersistenceEventSource;
import com.braintribe.model.processing.resource.persistence.BinaryPersistenceListener;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.collaboration.PersistenceAppender.AppendedSnippet;
import com.braintribe.model.processing.session.api.managed.ManipulationReport;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.FileSystemSource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resourceapi.persistence.DeleteBinary;
import com.braintribe.model.resourceapi.persistence.StoreBinary;
import com.braintribe.model.service.api.DomainRequest;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.NullSafe;
import com.braintribe.utils.lcd.StopWatch;

/**
 * 
 */
public class DistributedCollaborativeSmoodAccess extends CollaborativeSmoodAccess implements BinaryPersistenceListener, DestructionAware {

	private static final Logger log = Logger.getLogger(DistributedCollaborativeSmoodAccess.class);

	public static final String TEXT_PLAIN_MIME_TYPE = "text/plain";

	/* IMPLEMENTATION NOTES: */

	/* There are two types of "operations" - read-only (R) and write (W) and for each we have a different approach. The main difference is that W
	 * operations are also stored in the distributed storage, while R are only run locally. */

	/* Before each operation we call a special method named "ensureUpToDate", which updates the local state with the global one without using the
	 * distributed lock (D lock). This update is sufficient for a subsequent R operation to be performed right away, and in case of a W operation
	 * serves as optimization - update before acquiring the D lock, so that the execution guarded by this lock is as small as possible. */

	/* When it comes to R operations, no further special treatment needs to be done, the request is processed based on the local state only, thus
	 * working only with an R lock. */

	/* For W operation, we always start by acquiring the D lock, and right after that also the W lock. Then we do one more update just to be sure. We
	 * then perform the operation locally, store the corresponding CsaOperation in the shared storage, and only then release both locks. */

	/* IMPORTANT: Of course, in case of W operations, we need a globally up-to-date state before we perform the update, therefore we need both the D
	 * and W locks. Since during the update we are applying the W operations (which came from the shared storage), it is absolutely necessary that
	 * these W operations only use a W lock, even if they could be optimized by having some parts done with an R lock only. Since we already have a W,
	 * acquiring an R lock would cause a deadlock. I learned the hard way. */

	/* It is also clear, that the operations done as an update must not be stored in the shared storage. */

	/* package */ DcsaSharedStorage sharedStorage;
	/* package */ Lock distributedLock;
	/* package */ CollaborativeAccessManager collaborativeAccessManager;

	// marker for the last Operation of the distributed setup up until which this instance's state was updated
	private String marker;
	private CsaStatePersistence statePersistence;

	// marks whether we are updating - to not re-broadcast OPs which we received for update
	private boolean isUpdating;

	private BinaryPersistenceEventSource binaryPersistenceEventSource;
	private Path resourcesBaseAbsolutePath;

	@Override
	public void postConstruct() {
		if (resourcesBaseAbsolutePath != null)
			return; // already initialized

		verifyRequiredConfiguration();

		this.marker = statePersistence.readMarker();
		this.distributedLock = sharedStorage.getLock(getAccessId());

		this.registerCustomPersistenceRequestProcessor(CollaborativePersistenceRequest.T, new DistributedCollaborativeAccessManager(this));
		this.binaryPersistenceEventSource.addPersistenceListener(this);
		this.resourcesBaseAbsolutePath = Paths.get(collaborativeAccessManager.resolveResourceFile("").getAbsolutePath());

		doInitialUpdate();

		/* When initializing cortex, this method leads to a query at the end, where onModelAccessoryOutdated is called. Therefore we must configure
		 * the other things first and only call this (super) method at the end. */
		super.postConstruct();
	}

	private void verifyRequiredConfiguration() {
		NullSafe.nonNull(sharedStorage, "sharedStorage");
		NullSafe.nonNull(statePersistence, "csaStatePersistence");
		NullSafe.nonNull(binaryPersistenceEventSource, "binaryPersistenceEventSource");
		NullSafe.nonNull(collaborativeAccessManager, "collaborativeRequestProcessor");
	}

	@Override
	public void preDestroy() {
		this.binaryPersistenceEventSource.removePersistenceListener(this);
	}

	// @formatter:off
	@Required public void setSharedStorage(DcsaSharedStorage sharedStorage) { this.sharedStorage = sharedStorage; }
	@Required public void setCsaStatePersistence(CsaStatePersistence statePersistence) { this.statePersistence = statePersistence; }
	@Required public void setBinaryPersistenceEventSource(BinaryPersistenceEventSource binaryPersistenceEventSource) { this.binaryPersistenceEventSource = binaryPersistenceEventSource; }
	@Override
	@Required public void setCollaborativeRequestProcessor(CollaborativeAccessManager collaborativeAccessManager) { this.collaborativeAccessManager = collaborativeAccessManager; }	
	// @formatter:on

	// ########################################################
	// ## . . . . . . . . Initial update . . . . . . . . . . ##
	// ########################################################

	private void doInitialUpdate() {
		StopWatch sw = new StopWatch();

		DcsaIterable dcsaIterable = readSharedStorageOperations();
		sw.intermediate("READ_SHARED_STORAGE");

		if (dcsaIterable == null || dcsaIterable.getLastReadMarker() == null)
			return;

		CollaborativeAccessOfflineManager offlineManager = newOfflineManager();

		int count = 0;
		for (CsaOperation csaOperation : dcsaIterable) {
			applyUpdateOperationOffline(csaOperation, offlineManager);
			count++;
		}
		sw.intermediate("APPLY_LOCALLY");

		statePersistence.writeMarker(marker = dcsaIterable.getLastReadMarker());

		log.info("Access [" + getAccessId() + "]: Initial update from shared storage with " + count + " OPs took: " + sw.getElapsedTimesReport());
	}

	private CollaborativeAccessOfflineManager newOfflineManager() {
		CollaborativeAccessOfflineManager bean = new CollaborativeAccessOfflineManager();
		bean.setBaseFolder(manipulationPersistence.getStrogaBase());
		bean.setCsaStatePersistence(statePersistence);

		return bean;
	}

	private void applyUpdateOperationOffline(CsaOperation operation, CollaborativeAccessOfflineManager offlineManager) {
		switch (operation.operationType()) {
			case APPEND_MODEL_MANIPULATION:
				offlineManager.append(new Resource[] { resourceFrom(operation), null });
				return;
			case APPEND_DATA_MANIPULATION:
				offlineManager.append(new Resource[] { null, resourceFrom(operation) });
				return;
			case MANAGE_PERSISTENCE:
				applyManagePersistenceOffline((CsaManagePersistence) operation, offlineManager);
				return;
			case STORE_RESOURCE:
				storeResource((CsaStoreResource) operation);
				return;
			case DELETE_RESOURCE:
				deleteResource((CsaDeleteResource) operation);
				return;
			default:
				throw new UnsupportedEnumException(operation.operationType());
		}
	}

	private void applyManagePersistenceOffline(CsaManagePersistence managePersistence, CollaborativeAccessOfflineManager offlineManager) {
		CollaborativePersistenceRequest persistenceRequest = managePersistence.getPersistenceRequest();

		offlineManager.process(null, persistenceRequest);
	}

	// ########################################################
	// ## . . . . . . . . . . Querying . . . . . . . . . . . ##
	// ########################################################

	@Override
	public SelectQueryResult query(SelectQuery query) {
		log.trace(() -> "SelectQuery: " + print(query));
		ensureUpToDate();
		return super.query(query);
	}

	@Override
	public EntityQueryResult queryEntities(EntityQuery query) {
		log.trace(() -> "EntityQuery: " + print(query));
		ensureUpToDate();
		return super.queryEntities(query);
	}

	@Override
	public PropertyQueryResult queryProperty(PropertyQuery query) {
		log.trace(() -> "PropertyQuery: " + print(query));
		ensureUpToDate();
		return super.queryProperty(query);
	}

	/**
	 * This method guarantees that the after it's invocation the local state of our DCSA is up to date with the distributed state at the moment of the
	 * method's invocation.
	 */
	/* package */ void ensureUpToDate() {
		DcsaIterable dcsaIterable;
		String readMarker;

		readLock.lock();
		try {
			dcsaIterable = readSharedStorageOperations();
			readMarker = marker;

		} finally {
			readLock.unlock();
		}

		// if at the time of reading our marker was up-to-date, we don't do anything
		if (dcsaIterable == null || dcsaIterable.getLastReadMarker() == null)
			return;

		writeLock.lock();
		try {
			/* here we do the update, but since we have released one lock and acquired another one, we have to use the "readMarker" to see if the
			 * local state didn't change in the meantime */
			w_ensureUpToDate(readMarker, dcsaIterable);

		} finally {
			writeLock.unlock();
		}
	}

	private DcsaIterable readSharedStorageOperations() {
		StopWatch sw = new StopWatch();

		DcsaIterable result = sharedStorage.readOperations(getAccessId(), marker);
		log.trace( () -> "Access [" + getAccessId() + "] - reading OPs from shared storage took " + sw.getElapsedTime() + " ms.");

		return result;
	}

	private void w_ensureUpToDate(String readMarker, DcsaIterable dcsaIterable) {
		if (!equalsOrBothNull(marker, readMarker)) {
			dcsaIterable = readSharedStorageOperations();
			if (dcsaIterable == null || dcsaIterable.getLastReadMarker() == null)
				return;
		}

		w_update(dcsaIterable);
	}

	// ########################################################
	// ## . . . . . . . . Apply Manipulation . . . . . . . . ##
	// ########################################################

	@Override
	public ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest) throws ModelAccessException {
		// update before acquiring the distributed lock - if there is a big update, don't block the whole cluster
		ensureUpToDate();

		/* OPTIMIZATION IDEA: in the future we could improve this by implementing a method that returns only iff we have an up-to-date state and at
		 * the same time are holding the distributed lock. The point is that it would try to acquire the distributed lock, but as long as there are
		 * updates available, it would release it again and do the update, this minimizing the time the distributed lock is being held. */

		distributedLock.lock();

		try {
			return super.applyManipulation(manipulationRequest);

		} finally {
			distributedLock.unlock();
		}
	}

	/**
	 * DW - This method can only be called when guarded by a D lock. The name does not reflect that as it is inherited.
	 */
	@Override
	protected ManipulationReport w_applyInSmoodAndPersist(ManipulationRequest manipulationRequest) throws ModelAccessException {
		dw_ensureUpToDate();

		return super.w_applyInSmoodAndPersist(manipulationRequest);
	}

	/**
	 * This method guarantees that after it's finished the local state is the up-to-date distributed state, and since it's guarded by D and W locks,
	 * update can follow.
	 */
	/* package */ void dw_ensureUpToDate() {
		DcsaIterable dcsaIterable = readSharedStorageOperations();
		if (dcsaIterable == null || dcsaIterable.getLastReadMarker() == null)
			return;

		w_update(dcsaIterable);
	}

	/** This method is called iff dcsaIterable contains updates to be applied (lastReadMarker != null) */
	private void w_update(DcsaIterable dcsaIterable) {
		isUpdating = true;

		DcsaUpdateProfiler profiler = null;
		if (log.isTraceEnabled()) {
			profiler = new DcsaUpdateProfiler();
		}

		try {

			for (CsaOperation csaOperation : dcsaIterable) {
				if (profiler != null)
					profiler.onUpdateSingleOperation(csaOperation);

				w_applyUpdateOperation(csaOperation);
			}

			if (profiler != null)
				log.trace(profiler.finalizeAndReport());

			database.ensureIds();

			statePersistence.writeMarker(marker = dcsaIterable.getLastReadMarker());

		} finally {
			isUpdating = false;
		}
	}

	/** DW, similar to {@link #w_applyInSmoodAndPersist} */
	@Override
	protected AppendedSnippet[] w_persistAppliedLocalManipulation(Manipulation manipulation) {
		AppendedSnippet[] snippets = super.w_persistAppliedLocalManipulation(manipulation);

		if (!isUpdating)
			dw_storeAppendManipulation(snippets);

		return snippets;
	}

	private void dw_storeAppendManipulation(AppendedSnippet[] snippets) {
		dw_storeAppendManipulation(CsaAppendModelManipulation.T, snippets[0]);
		dw_storeAppendManipulation(CsaAppendDataManipulation.T, snippets[1]);
	}

	private void dw_storeAppendManipulation(EntityType<? extends CsaResourceBasedOperation> operationType, AppendedSnippet snippet) {
		if (snippet == null)
			return;

		Resource payload = Resource.createTransient(snippet);
		payload.setFileSize(snippet.sizeInBytes());
		payload.setMimeType(TEXT_PLAIN_MIME_TYPE);

		dw_storeCsaOperation(CsaOperationBuilder.resourceBasedOp(operationType, payload));
	}

	private void w_applyUpdateOperation(CsaOperation operation) {
		switch (operation.operationType()) {
			case APPEND_MODEL_MANIPULATION:
				w_append(new Resource[] { resourceFrom(operation), null });
				return;
			case APPEND_DATA_MANIPULATION:
				w_append(new Resource[] { null, resourceFrom(operation) });
				return;
			case MANAGE_PERSISTENCE:
				w_applyManagePersistence((CsaManagePersistence) operation);
				return;
			case STORE_RESOURCE:
				storeResource((CsaStoreResource) operation);
				return;
			case DELETE_RESOURCE:
				deleteResource((CsaDeleteResource) operation);
				return;
			default:
				throw new UnsupportedEnumException(operation.operationType());
		}
	}

	private void w_append(Resource[] gmmlResources) {
		CsaCollector csaCollector = new CsaCollector();
		database.getGmSession().listeners().add(csaCollector);

		try {
			appender.append(gmmlResources, session);

			w_notifyStageRegistry_OnUpdate(csaCollector.manipulations);

		} finally {
			database.getGmSession().listeners().remove(csaCollector);
		}
	}

	// This method is only used when updating
	private void w_notifyStageRegistry_OnUpdate(List<Manipulation> manipulations) {
		if (!manipulations.isEmpty())
			super.w_notifyStageRegistry(asManipulation(manipulations));
	}

	private Resource resourceFrom(CsaOperation operation) {
		return ((CsaResourceBasedOperation) operation).getPayload();
	}

	private void w_applyManagePersistence(CsaManagePersistence managePersistence) {
		CollaborativePersistenceRequest persistenceRequest = managePersistence.getPersistenceRequest();

		collaborativeAccessManager.process(null, persistenceRequest);
	}

	@Override
	public PersistenceStage findStageForReference(EntityReference reference) {
		ensureUpToDate();
		return super.findStageForReference(reference);
	}

	@Override
	public PersistenceStage getStageForReference(EntityReference reference) {
		ensureUpToDate();
		return super.getStageForReference(reference);
	}

	/* package */ void dw_storeCsaOperation(CsaOperation operation) {
		marker = sharedStorage.storeOperation(getAccessId(), operation);
		statePersistence.writeMarker(marker);
	}

	// ########################################################
	// ## . . . . . . . Resource Persistence . . . . . . . . ##
	// ########################################################

	private void storeResource(CsaStoreResource operation) {
		String resourceRelativePath = operation.getResourceRelativePath();

		Resource resource = operation.getPayload();

		// if it's null, we assume lazy-loading, so we can skip storing it here
		if (resource != null) {
			File resourceFile = collaborativeAccessManager.resolveResourceFile(resourceRelativePath);
			storeResource(resource, resourceFile);
		}
	}

	private void storeResource(Resource resource, File resourceFile) {
		FileTools.ensureFolderExists(resourceFile.getParentFile());

		try (InputStream in = resource.openStream()) {
			IOTools.inputToFile(in, resourceFile);

		} catch (IOException e) {
			throw Exceptions.unchecked(e, "Error while storing resource for file: " + resourceFile.getAbsolutePath());
		}
	}

	private void deleteResource(CsaDeleteResource operation) {
		String resourceRelativePath = operation.getResourceRelativePath();
		File resourceFile = collaborativeAccessManager.resolveResourceFile(resourceRelativePath);

		if (resourceFile.exists()) // file might not exists if it wasn't yet lazy-loaded
			if (!resourceFile.delete())
				throw new GenericModelException("Unable to delete resource. Resolved file: " + resourceFile.getAbsolutePath());
	}

	@Override
	public void onStore(ServiceRequestContext context, StoreBinary request, Resource resource) {
		storeResourceOpIfEligible(request, request.getCreateFrom(), resource, CsaOperationBuilder::storeResource);
	}

	@Override
	public void onDelete(ServiceRequestContext context, DeleteBinary request) {
		storeResourceOpIfEligible(request, null, request.getResource(), CsaOperationBuilder::deleteResource);
	}

	private void storeResourceOpIfEligible(DomainRequest request, Resource streamableResource, Resource resourceWithSource,
			BiFunction<Resource, String, CsaOperation> opFactory) {

		if (!isRelatedToThisAccess(request))
			return;

		String path = resolvePathIfPossible(resourceWithSource.getResourceSource());
		if (path == null)
			return;

		ensureUpToDate();

		distributedLock.lock();
		try {
			writeLock.lock();
			try {

				dw_ensureUpToDate();

				CsaOperation operation = opFactory.apply(streamableResource, path);
				dw_storeCsaOperation(operation);

			} finally {
				writeLock.unlock();
			}
		} finally {
			distributedLock.unlock();
		}
	}

	private boolean isRelatedToThisAccess(DomainRequest request) {
		return getAccessId().equals(request.getDomainId());
	}

	/**
	 * Returns a normalized path for given {@link ResourceSource} iff this is a {@link FileSystemSource} whose path is either relative, or both
	 * absolute and starting with {@link #resourcesBaseAbsolutePath} (i.e. the absolute path for the resources folder).
	 * 
	 * The normalization of the returned path simply means the path separator is slash ('/') and not backslash.
	 */
	private String resolvePathIfPossible(ResourceSource resourceSource) {
		if (!(resourceSource instanceof FileSystemSource))
			return null;

		String result = ((FileSystemSource) resourceSource).getPath();

		return CsaBinaryTools.resolveRelativePath(result, resourcesBaseAbsolutePath);
	}

	/**
	 * This method does not do an update, because it assumes given {@link FileSystemSource} originated from this instance, thus the corresponding
	 * marker has to be already indexed.
	 */
	@Override
	public boolean experimentalLazyLoad(FileSystemSource source) {
		writeLock.lock();
		try {
			return r_experimentalLazyLoad(source);

		} finally {
			writeLock.unlock();
		}
	}

	private boolean r_experimentalLazyLoad(FileSystemSource source) {
		String relativePath = source.getPath();

		File resourceFile = collaborativeAccessManager.resolveResourceFile(relativePath);
		if (resourceFile.exists())
			return true;

		log.debug(() -> "Accesss '" + getAccessId() + "' will try to lazy-load source: " + source.getPath());

		Resource resource = sharedStorage.readResource(getAccessId(), Arrays.asList(relativePath)).get(relativePath);
		if (resource == null)
			throw new IllegalStateException("Resource not found in the shared storage for path: " + relativePath + ". Access: " + getAccessId());

		storeResource(resource, resourceFile);
		return true;
	}

	// ###############################################
	// ## . . . . . . . . Profiling . . . . . . . . ##
	// ###############################################

	class DcsaUpdateProfiler {

		private final StringJoiner sj = new StringJoiner(", ", "DCSA UPDATE OPS: ", "");
		private final StopWatch sw = new StopWatch();
		private long operationStart;
		private CsaOperation operation;

		public void onUpdateSingleOperation(CsaOperation csaOperation) {
			if (operation != null)
				onUpdateFinished();

			operation = csaOperation;
			operationStart = System.nanoTime();
		}

		public String finalizeAndReport() {
			sj.add("TOTAL: " + sw.getElapsedTime());
			return sj.toString();
		}

		private void onUpdateFinished() {
			long time = (System.nanoTime() - operationStart) / (1000 * 1000);
			sj.add(operation.entityType().getShortName() + "-" + time);

			if (time > 100)
				log.warn("Single CSA operation (" + operation.entityType().getShortName() + ") took: " + time + " ms.");
		}

	}
}
