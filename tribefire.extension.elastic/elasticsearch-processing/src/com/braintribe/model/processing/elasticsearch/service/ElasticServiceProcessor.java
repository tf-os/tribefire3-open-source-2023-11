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
package com.braintribe.model.processing.elasticsearch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.exception.Exceptions;
import com.braintribe.gwt.utils.genericmodel.GMCoreTools;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.aspect.AspectConfiguration;
import com.braintribe.model.elasticsearch.service.ElasticReflection;
import com.braintribe.model.elasticsearch.service.ElasticRequest;
import com.braintribe.model.elasticsearch.service.ElasticResult;
import com.braintribe.model.elasticsearch.service.GetIndexingStatus;
import com.braintribe.model.elasticsearch.service.IndexingStatus;
import com.braintribe.model.elasticsearch.service.ReIndex;
import com.braintribe.model.elasticsearch.service.ReIndexEntitiesResult;
import com.braintribe.model.elasticsearch.service.ReIndexEntity;
import com.braintribe.model.elasticsearch.service.ReIndexInternal;
import com.braintribe.model.elasticsearch.service.ReIndexResult;
import com.braintribe.model.elasticsearch.service.ReflectElastic;
import com.braintribe.model.elasticsearch.service.ReindexEntities;
import com.braintribe.model.elasticsearchdeployment.ElasticsearchConnector;
import com.braintribe.model.elasticsearchdeployment.aspect.ExtendedFulltextAspect;
import com.braintribe.model.elasticsearchdeployment.indexing.ElasticsearchIndexingMetaData;
import com.braintribe.model.elasticsearchdeployment.indexing.ElasticsearchIndexingWorker;
import com.braintribe.model.elasticsearchdeployment.reindex.ReIndexing;
import com.braintribe.model.elasticsearchdeployment.reindex.ReIndexingStatus;
import com.braintribe.model.elasticsearchreflection.ElasticsearchReflection;
import com.braintribe.model.extensiondeployment.AccessAspect;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.processing.pr.fluent.JunctionBuilder;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessors;
import com.braintribe.model.processing.elasticsearch.ElasticsearchClient;
import com.braintribe.model.processing.elasticsearch.IndexedElasticsearchConnector;
import com.braintribe.model.processing.elasticsearch.IndexedElasticsearchConnectorImpl;
import com.braintribe.model.processing.elasticsearch.fulltext.FulltextProcessing;
import com.braintribe.model.processing.elasticsearch.fulltext.FulltextTypeSupport;
import com.braintribe.model.processing.elasticsearch.indexing.ElasticsearchIndexingWorkerImpl;
import com.braintribe.model.processing.elasticsearch.indexing.IndexingPackage;
import com.braintribe.model.processing.elasticsearch.indexing.ScheduledIndexingReportContext;
import com.braintribe.model.processing.elasticsearch.util.DeployableUtils;
import com.braintribe.model.processing.elasticsearch.util.ElasticsearchReflectionUtil;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelTypes;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resourceapi.persistence.DeleteResource;
import com.braintribe.model.service.api.AsynchronousRequest;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.RandomTools;
import com.braintribe.utils.ThrowableTools;

public class ElasticServiceProcessor implements AccessRequestProcessor<ElasticRequest, ElasticResult>, LifecycleAware {

	private final static Logger logger = Logger.getLogger(ElasticServiceProcessor.class);

	private PersistenceGmSessionFactory sessionFactory;
	private final Map<String, Boolean> propertyIndexingEnabled = new HashMap<>();
	private DeployableUtils deployableUtils;

	private FulltextProcessing fulltextProcessing;

	protected ConcurrentHashMap<String, IndexedElasticsearchConnector> connectorsPerAccess = new ConcurrentHashMap<>();

	private AccessRequestProcessor<ElasticRequest, ElasticResult> delegate = AccessRequestProcessors.dispatcher(dispatching -> {
		dispatching.register(ReflectElastic.T, this::reflectElastic);
		dispatching.register(ReIndex.T, this::reIndex);
		dispatching.register(ReIndexInternal.T, this::reIndexInternal);
		dispatching.register(GetIndexingStatus.T, this::getIndexingStatus);
		dispatching.register(ReindexEntities.T, this::reIndexEntities);
		dispatching.register(ReIndexEntity.T, this::reIndexEntity);
	});

	@Override
	public ElasticResult process(AccessRequestContext<ElasticRequest> context) {
		return delegate.process(context);
	}

	public ReIndexEntitiesResult reIndexEntity(AccessRequestContext<ReIndexEntity> context) {

		ReIndexEntity request = context.getRequest();
		String accessId = request.getAccessId();
		String entityTypeSignature = request.getTypeSignature();
		Object entityId = request.getEntityId();
		Set<Resource> resources = request.getResources();

		ReIndexEntitiesResult result = ReIndexEntitiesResult.T.create();

		PersistenceGmSession session = sessionFactory.newSession(accessId);
		GenericEntity entity = session.query().entity(entityTypeSignature, entityId).find();
		if (entity != null) {
			IndexedElasticsearchConnector connector = getConnectorsForAccess(accessId);
			try {
				fulltextProcessing.index(connector, entity, null, session, resources);
				result.setSuccess(true);
			} catch (Exception e) {
				result.setException(Exceptions.stringify(e));
				result.setSuccess(false);
				logger.debug(
						() -> "Error while trying to re-index an entity of type " + entityTypeSignature + " with Id " + entityId + " in " + accessId);
			}
		}
		return result;
	}

	protected IndexedElasticsearchConnector getConnectorsForAccess(String accessId) {
		return connectorsPerAccess.computeIfAbsent(accessId, aid -> {
			com.braintribe.model.processing.elasticsearch.ElasticsearchConnector connector = deployableUtils.getConnector(aid);
			if (connector instanceof IndexedElasticsearchConnector) {
				return (IndexedElasticsearchConnector) connector;
			}
			return null;
		});
	}

	public ReIndexEntitiesResult reIndexEntities(AccessRequestContext<ReindexEntities> context) {
		PersistenceGmSession cortexSession = context.getSession();
		ReindexEntities request = context.getRequest();
		String accessId = request.getAccessId();
		String entityTypeSignature = request.getTypeSignature();
		ReIndexEntitiesResult result = ReIndexEntitiesResult.T.create();
		result.setSuccess(false);

		ScheduledIndexingReportContext reportContext = new ScheduledIndexingReportContext(cortexSession);

		if (accessId == null) {
			result.setMessage("Missing accessId in serviceRequest");
		} else if (entityTypeSignature == null) {
			result.setMessage("Missing typeSignature in serviceRequest");
		} else {
			boolean debug = logger.isDebugEnabled();
			ReIndexing reIndexing = cortexSession.create(ReIndexing.T);
			EntityQuery query = EntityQueryBuilder.from(entityTypeSignature).where().property(GenericEntity.id).in().value(request.getEntityIds())
					.done();
			EntityQuery clonedQuery = EntityQuery.T.clone(new StandardCloningContext() {
				@Override
				public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
					return cortexSession.create(entityType);
				}
			}, query, StrategyOnCriterionMatch.skip);
			reIndexing.setQuery(clonedQuery);
			cortexSession.commit();

			try {

				long processingStart = System.currentTimeMillis();

				PersistenceGmSession session = sessionFactory.newSession(accessId);
				if (debug) {
					logger.debug("Successfully established session to source access: " + accessId);
				}

				ElasticsearchIndexingWorker workerDeployable = deployableUtils.queryWorker(accessId);
				ElasticsearchIndexingWorkerImpl worker = deployableUtils.getElasticsearchWorker(workerDeployable);
				IndexedElasticsearchConnectorImpl connector = deployableUtils.getIndexedElasticsearchConnector(workerDeployable);

				FulltextTypeSupport.updateMappings(connector.getClient(), connector.getIndex(), 100000);

				prepareIndexing(worker, session, reportContext, reIndexing, cortexSession);

				String msg = "Successfully indexed " + reportContext.getIndexedEntities() + " indexable entities.";
				reportContext.setMessage(msg);
				result.setMessage(msg);
				result.setSuccess(true);

				if (debug) {
					logger.debug(msg);
				}

				long duration = (System.currentTimeMillis() - processingStart);
				reportContext.setDuration(duration);

				reportContext.updateReIndexing(reIndexing, ReIndexingStatus.completed);

			} catch (Exception e) {
				String msg = "Error while processing scheduled indexing request for accessId " + accessId;
				logger.warn(msg, e);

				reportContext.setMessage(msg);
				reportContext.registerException(e);

				reportContext.updateReIndexing(reIndexing, ReIndexingStatus.panic);

			}
		}
		if (reportContext.getException() != null) {
			result.setException(ThrowableTools.getStackTraceString(reportContext.getException()));
		}
		result.setIndexedEntities(reportContext.getIndexedEntities());
		result.setMessage(reportContext.getMessage());
		result.setDetailedMessage(reportContext.getDetailedMessage());

		return result;
	}

	public ReIndexResult reIndex(AccessRequestContext<ReIndex> context) {

		PersistenceGmSession session = context.getSession();
		ReIndex request = context.getRequest();
		IncrementalAccess access = request.getAccess();

		ReIndexResult result = ReIndexResult.T.create();
		result.setStatus(ReIndexingStatus.initialized);

		if (access == null) {
			result.setStatus(ReIndexingStatus.panic);

		} else {

			SelectQuery selectQuery = new SelectQueryBuilder().from(ReIndexing.T, "r").join("r", ReIndexing.access, "a").where()
					.property("a", IncrementalAccess.externalId).eq(access.getExternalId()).select("r").done();

			ReIndexing reIndexing = session.query().select(selectQuery).first();
			if (reIndexing == null) {
				reIndexing = session.create(ReIndexing.T);
			} else {
				reset(reIndexing, session);
			}

			EntityQuery query = request.getQuery();
			if (query != null) {
				EntityQuery clonedQuery = EntityQuery.T.clone(new StandardCloningContext() {
					@Override
					public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
						return session.create(entityType);
					}
				}, query, StrategyOnCriterionMatch.skip);
				reIndexing.setQuery(clonedQuery);
			}

			reIndexing.setAccess(access);

			session.commit();

			String correlationId = RandomTools.newStandardUuid();

			ReIndexInternal internalRequest = ReIndexInternal.T.create();
			internalRequest.setReIndexing(reIndexing);
			internalRequest.setSessionId(request.getSessionId());

			AsynchronousRequest async = AsynchronousRequest.T.create();
			async.setServiceRequest(internalRequest);
			async.setCorrelationId(correlationId);
			async.setPriority(-1d);
			async.setSessionId(request.getSessionId());

			async.eval(session).get();

			logger.debug("Invoked asynchronous request ReIndexInternal with correlation Id: " + correlationId + ", referencing " + reIndexing);
		}

		return result;

	}

	private void reset(ReIndexing reIndexing, PersistenceGmSession session) {
		reIndexing.setDuration(null);
		reIndexing.setIndexableEntities(null);
		reIndexing.setMessage(null);
		reIndexing.setReIndexingStatus(ReIndexingStatus.initialized);

		Resource resource = reIndexing.getReport();
		if (resource != null) {
			try {
				DeleteResource deleteResource = DeleteResource.T.create();
				deleteResource.setResource(resource);
				deleteResource.eval(session).get();
			} catch (NotFoundException nfe) {
				logger.trace(() -> "Could not find a physical file/entry for resource " + resource.getId());
			} catch (Exception e) {
				logger.trace(() -> "Error while invoking DeleteResource for resource " + resource.getId(), e);
			}
			reIndexing.setReport(null);
		}
	}

	public ReIndexResult reIndexInternal(AccessRequestContext<ReIndexInternal> context) {

		ReIndexInternal request = context.getRequest();
		PersistenceGmSession cortexSession = context.getSession();

		ReIndexing reIndexing = request.getReIndexing();

		reIndexing.setReIndexingStatus(ReIndexingStatus.executing);

		ReIndexResult result = ReIndexResult.T.create();
		result.setReIndexing(reIndexing);

		String rep = null;
		boolean debug = logger.isDebugEnabled();

		ScheduledIndexingReportContext reportContext = new ScheduledIndexingReportContext(cortexSession);

		try {

			initializeModel(context.getSession());

		} catch (Exception e) {
			String msg = "Error while processing ActionRequest for " + context.getSession().getAccessId() + ". Refer to the report for more details.";
			logger.warn(msg);

			reportContext.setMessage(msg);
			reportContext.registerException(e);
			reportContext.updateReIndexing(reIndexing, ReIndexingStatus.panic);

			result.setStatus(ReIndexingStatus.panic);

			return result;
		}

		rep = "Detected Indexing ActionRequest: " + GMCoreTools.getDescription(request);
		reportContext.append(rep);

		if (debug) {
			logger.debug(rep);
		}

		IncrementalAccess access = reIndexing.getAccess();
		String accessId = access.getExternalId();
		reportContext.setAccessId(accessId);

		try {
			long processingStart = System.currentTimeMillis();

			PersistenceGmSession session = sessionFactory.newSession(accessId);
			rep = "Successfully established session to source access: " + accessId;
			reportContext.append(rep);
			if (debug) {
				logger.debug(rep);
			}

			ElasticsearchIndexingWorker workerDeployable = deployableUtils.queryWorker(accessId);
			ElasticsearchIndexingWorkerImpl worker = deployableUtils.getElasticsearchWorker(workerDeployable);
			IndexedElasticsearchConnectorImpl connector = deployableUtils.getIndexedElasticsearchConnector(workerDeployable);

			FulltextTypeSupport.updateMappings(connector.getClient(), connector.getIndex(), 100000);

			prepareIndexing(worker, session, reportContext, reIndexing, cortexSession);

			String msg = "Successfully indexed " + reportContext.getIndexedEntities() + " indexable entities.";
			reportContext.setMessage(msg);
			if (debug) {
				logger.debug(msg);
			}

			long duration = (System.currentTimeMillis() - processingStart);
			reportContext.setDuration(duration);

			reportContext.updateReIndexing(reIndexing, ReIndexingStatus.completed);

			result.setStatus(ReIndexingStatus.completed);

			return result;

		} catch (Exception e) {
			String msg = "Error while processing scheduled indexing request for accessId " + accessId;
			logger.warn(msg);

			reportContext.setMessage(msg);
			reportContext.registerException(e);

			reportContext.updateReIndexing(reIndexing, ReIndexingStatus.panic);

			result.setStatus(ReIndexingStatus.panic);

			return result;
		}
	}

	protected boolean resolveCascadingAttachment(IncrementalAccess access) {
		AspectConfiguration aspectConfiguration = access.getAspectConfiguration();
		if (aspectConfiguration != null) {
			List<AccessAspect> aspects = aspectConfiguration.getAspects();
			if (aspects != null) {
				for (AccessAspect aspect : aspects) {
					if (aspect instanceof ExtendedFulltextAspect) {
						ExtendedFulltextAspect efa = (ExtendedFulltextAspect) aspect;
						return efa.getCascadingAttachment();
					}
				}
			}
		}
		return true;
	}

	/**
	 * Iterates through all {@link com.braintribe.model.meta.GmEntityType GmEntityTypes} of the Meta Model attached to
	 * the access. If properties of these {@link com.braintribe.model.meta.GmEntityType GmEntityTypes} have the Meta
	 * Data {@link ElasticsearchIndexingMetaData} attached, they are indexed.
	 *
	 * @param worker
	 *            - The worker which processes the indexing
	 * @param session
	 *            - The session to query the Meta Model and entities for
	 * @param reIndexing
	 * @param cortexSession
	 * @throws Exception
	 *             - If the processing fails for any reason
	 */
	private void prepareIndexing(ElasticsearchIndexingWorkerImpl worker, PersistenceGmSession session, ScheduledIndexingReportContext reportContext,
			ReIndexing reIndexing, PersistenceGmSession cortexSession) throws Exception {

		GmMetaModel model = session.getModelAccessory().getModel();
		String m = null;
		final boolean debug = logger.isDebugEnabled();

		/**
		 * We need to collect information if the worker has finished the indexing for each potential indexing package
		 */
		final AtomicInteger activityCount = new AtomicInteger(0);
		ReentrantLock lock = new ReentrantLock();
		Condition allPackagesProcessed = lock.newCondition();

		Runnable callback = new Runnable() {

			@Override
			public void run() {
				if (debug) {
					logger.debug("Received notification of processed package, current count is " + activityCount.get());
				}

				if (activityCount.decrementAndGet() == 0) {

					if (debug) {
						logger.debug("Received notifications for all indexing packages, notifying monitor.");
					}

					lock.lock();
					try {
						allPackagesProcessed.signal();
					} finally {
						lock.unlock();
					}
				}
			}
		};

		/**
		 * It must be ensured that the count is never set to 0 until the processor has not processed all of the
		 * packages.
		 */
		activityCount.incrementAndGet();

		if (reIndexing.getQuery() != null) {
			EntityType<GenericEntity> entityType = GMF.getTypeReflection().getEntityType(reIndexing.getQuery().getEntityTypeSignature());
			processEntityTypeIndexing(worker, session, reportContext, reIndexing, cortexSession, debug, activityCount, callback, entityType);
		} else {

			ModelOracle oracle = new BasicModelOracle(model);
			ModelTypes modelTypes = oracle.getTypes();
			List<GmEntityType> list = modelTypes.onlyEntities().<GmEntityType> asGmTypes().filter(t -> !t.getIsAbstract())
					.collect(Collectors.toList());

			for (GmEntityType gmEntityType : list) {
				EntityType<GenericEntity> entityType = GMF.getTypeReflection().getEntityType(gmEntityType.getTypeSignature());
				processEntityTypeIndexing(worker, session, reportContext, reIndexing, cortexSession, debug, activityCount, callback, entityType);

			}
		}
		/**
		 * All packages are indexed now so the helper count can be decreased
		 */
		activityCount.decrementAndGet();

		try {
			if (debug) {
				logger.debug("Waiting for notifications of indexing packages ...");
			}
			if (activityCount.get() > 0) {
				lock.lock();
				try {
					allPackagesProcessed.await();
				} finally {
					lock.unlock();
				}
			}
			if (debug) {
				logger.debug("Successfully received notifications of indexing packages.");
			}

		} catch (InterruptedException e) {
			logger.info("Received interrupted event, not waiting for further notifications. Actual activity count is " + activityCount.get());
			return;
		}

	}

	private void processEntityTypeIndexing(ElasticsearchIndexingWorkerImpl worker, PersistenceGmSession session,
			ScheduledIndexingReportContext reportContext, ReIndexing reIndexing, PersistenceGmSession cortexSession, final boolean debug,
			final AtomicInteger activityCount, Runnable callback, EntityType<GenericEntity> entityType) throws Exception {
		String m;
		List<Property> properties = new ArrayList<>();

		logger.info(() -> "Checking type: " + entityType.getTypeSignature());

		for (Property property : entityType.getProperties()) {

			if (isIndexingEnabled(entityType.getTypeSignature(), property.getName(), session)) {
				properties.add(property);
			}
		}

		if (!CollectionTools.isEmpty(properties)) {

			logger.info(() -> "Indexing entites of type: " + entityType.getTypeSignature());

			processIndexing(worker, session, entityType, properties, reportContext, activityCount, callback, reIndexing, cortexSession);

		} else {

			m = "No indexable properties found for Entity Type " + entityType.getTypeSignature();
			reportContext.append(m);
			if (debug) {
				logger.debug(m);
			}
		}
	}

	/**
	 * Queries for indexable entities and hands these instances over to the worker which processes the indexing.
	 *
	 * @param worker
	 *            - The worker to process the indexing
	 * @param session
	 *            - The session to query the entities
	 * @param entityType
	 *            - The type of the entity to query for
	 * @param properties
	 *            - The properties to index
	 * @param reportContext
	 *            - The wrapper to collect data for the report file
	 * @param activityCount
	 *            - The amount of currently processed {@link IndexingPackage IndexingPackages}
	 * @param callback
	 *            - The callback which is assigned to the {@link IndexingPackage} to notify the processor after the
	 *            package has been indexed
	 * @param reIndexing
	 * @param cortexSession
	 * @throws Exception
	 *             - If the processing fails for any reason
	 */
	private void processIndexing(ElasticsearchIndexingWorkerImpl worker, PersistenceGmSession session, EntityType<GenericEntity> entityType,
			List<Property> properties, ScheduledIndexingReportContext reportContext, AtomicInteger activityCount, Runnable callback,
			ReIndexing reIndexing, PersistenceGmSession cortexSession) throws Exception {

		int pageStart = 0;
		int pageSize = 100;
		int indexableEntitiesCount = 0;
		EntityQueryResult result;
		String m = null;

		String typeSignature = entityType.getTypeSignature();
		long lastUpdate = System.currentTimeMillis();

		String accessId = session.getAccessId();

		do {

			JunctionBuilder<EntityQueryBuilder> disjunction = EntityQueryBuilder.from(entityType).tc().negation().disjunction();

			disjunction.property(GenericEntity.id);

			disjunction.conjunction().property().typeCondition(TypeConditions.isKind(TypeKind.simpleType)).close();

			for (Property p : properties) {
				if (!p.isIdentifier()) {
					disjunction.property(p.getName());
				}
			}

			EntityQuery query = disjunction.close().paging(pageSize, pageStart).done();
			if (reIndexing.getQuery() != null && typeSignature.equals(reIndexing.getQuery().getEntityTypeSignature())) {
				if (reIndexing.getQuery().getRestriction() != null && reIndexing.getQuery().getRestriction().getCondition() != null) {
					query.getRestriction().setCondition(reIndexing.getQuery().getRestriction().getCondition());
				}

			}

			result = session.query().entities(query).result(); // TODO try-catch einbauen
			List<GenericEntity> entities = result.getEntities();

			if (entities != null && entities.size() > 0) {

				// filter(entities, typeSignature);

				IndexingPackage indexingPackage = new IndexingPackage(accessId, typeSignature);
				for (GenericEntity entity : entities) {
					indexingPackage.addIndexableEntity(entity, properties);
				}

				indexingPackage.setCallback(callback);

				m = "Collected indexing package: " + indexingPackage;
				if (logger.isDebugEnabled()) {
					logger.debug(m);
				}

				Integer packageSize = indexingPackage.getPackageSize();
				boolean enqueued = worker.enqueue(indexingPackage);
				if (enqueued) {

					activityCount.incrementAndGet();
					reportContext.incrementIndexedEntitiesCount(packageSize);

				} else {
					reportContext.append(indexingPackage + " was not enqueued. Please refer to the logfile for more information.");
				}
				indexableEntitiesCount += packageSize;

				pageStart += pageSize;

			} else {
				if (logger.isTraceEnabled()) {
					logger.trace("No entities of type " + typeSignature + " found.");
				}
			}

			long now = System.currentTimeMillis();
			if ((now - lastUpdate) > Numbers.MILLISECONDS_PER_MINUTE) {
				lastUpdate = now;
				String message = "Submitted " + indexableEntitiesCount + " indexable entities of type " + typeSignature
						+ " to worker. Total entities so far: " + reportContext.getIndexedEntities();
				reIndexing.setMessage(message);
				cortexSession.commit();
			}

		} while (result.getHasMore());

		if (indexableEntitiesCount > 0) {
			m = "Submitted " + indexableEntitiesCount + " indexable entities of type " + typeSignature + " to worker.";
			reportContext.append(m);
			if (logger.isDebugEnabled()) {
				logger.debug(m);
			}

			reIndexing.setMessage(m + " Total entities so far: " + reportContext.getIndexedEntities());
			cortexSession.commit();
		}
	}

	/**
	 * Ensures that all required {@link com.braintribe.model.meta.GmMetaModel GmMetaModels} are available for further
	 * processing.
	 *
	 * @throws Exception
	 *             - If the initializing fails for any reason
	 */
	private void initializeModel(PersistenceGmSession session) throws Exception {

		ManagedGmSession managedSession = session.getModelAccessory().getModelSession();
		EntityQuery metaModelQuery = EntityQueryBuilder.from(GmMetaModel.T).done();
		List<GmMetaModel> metaModels = managedSession.query().entities(metaModelQuery).list();

		if (!metaModels.isEmpty()) {
			for (GmMetaModel model : metaModels) {
				model.deploy();
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Successfully ensured model types.");
			}

		}
	}

	/**
	 * Checks if the incoming {@link com.braintribe.model.meta.GmProperty property} holds the
	 * {@link ElasticsearchIndexingMetaData}.
	 *
	 * @param gmEntityType
	 *            - The {@link com.braintribe.model.meta.GmEntityType entity} holding the property
	 * @param property
	 *            - The {@link com.braintribe.model.meta.GmProperty property} to search for the meta data on
	 * @return - <code>true</code> if the checked entity holds the meta data, <code>false</code> otherwise
	 */
	private boolean isIndexingEnabled(String typeSignature, String property, PersistenceGmSession session) {

		String cacheKey = typeSignature + ":" + property;
		Boolean indexingEnabled = propertyIndexingEnabled.get(cacheKey);

		if (indexingEnabled == null) {
			synchronized (propertyIndexingEnabled) {
				ElasticsearchIndexingMetaData metaData = session.getModelAccessory().getCmdResolver().getMetaData().entityTypeSignature(typeSignature)
						.property(property).meta(ElasticsearchIndexingMetaData.T).exclusive();

				indexingEnabled = (metaData != null);

				propertyIndexingEnabled.put(cacheKey, indexingEnabled);
			}
		}

		return indexingEnabled;
	}

	/**
	 * Removes polymorphic entities.
	 *
	 * @param entities
	 *            - The entities to check
	 * @param typeSignature
	 *            - The type signature to check the polymorphic status for
	 */
	private void filter(List<GenericEntity> entities, String typeSignature) {

		for (Iterator<GenericEntity> it = entities.iterator(); it.hasNext();) {
			GenericEntity entity = it.next();
			String currentTypeSignature = entity.entityType().getTypeSignature();
			if (!(currentTypeSignature.equals(typeSignature))) {
				it.remove();
			}
		}
	}

	public ElasticReflection reflectElastic(AccessRequestContext<ReflectElastic> context) {

		ReflectElastic request = context.getRequest();
		ElasticsearchConnector conDeployable = request.getElasticsearchConnector();

		String externalId = conDeployable != null ? conDeployable.getExternalId() : null;
		com.braintribe.model.processing.elasticsearch.ElasticsearchConnector con = deployableUtils.findConnectorOrDefault(externalId);

		ElasticReflection er = ElasticReflection.T.create();

		if (con != null) {
			ElasticsearchClient client = con.getClient();
			ElasticsearchReflection elasticsearchReflection = ElasticsearchReflectionUtil.getElasticsearchReflection(client);

			er.setElasticsearchReflection(elasticsearchReflection);
		}

		return er;
	}

	public IndexingStatus getIndexingStatus(AccessRequestContext<GetIndexingStatus> context) {

		GetIndexingStatus request = context.getRequest();
		String accessId = request.getAccessId();

		Set<Object> ids = request.getIds();

		com.braintribe.model.processing.elasticsearch.ElasticsearchConnector connector = deployableUtils.getConnector(accessId);

		SearchRequestBuilder srb = fulltextProcessing.createIdsQuery(ids, connector);

		IndexingStatus status = IndexingStatus.T.create();

		Set<Object> indexedIds = status.getIndexedIds();
		Set<Object> unkownIds = status.getUnknownIds();
		unkownIds.addAll(ids);

		SearchResponse response = srb.setExplain(false).execute().actionGet();
		if (response != null) {
			SearchHits hits = response.getHits();
			if (hits != null) {
				SearchHit[] hitArray = hits.getHits();
				if (hitArray != null && hitArray.length > 0) {

					for (SearchHit hit : hitArray) {
						String id = hit.getId();
						indexedIds.add(id);
						unkownIds.remove(id);
					}
				}
			}
		}

		return status;
	}

	@Override
	public void postConstruct() {
		logger.debug(() -> ElasticServiceProcessor.class.getSimpleName() + " deployed.");
	}

	@Override
	public void preDestroy() {
		logger.debug(() -> ElasticServiceProcessor.class.getSimpleName() + " undeployed.");
	}

	@Required
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	@Required
	@Configurable
	public void setDeployableUtils(DeployableUtils deployableUtils) {
		this.deployableUtils = deployableUtils;
	}
	@Configurable
	@Required
	public void setFulltextProcessing(FulltextProcessing fulltextProcessing) {
		this.fulltextProcessing = fulltextProcessing;
	}
}
