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
package com.braintribe.model.processing.elasticsearch.indexing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.gwt.utils.genericmodel.GMCoreTools;
import com.braintribe.logging.Logger;
import com.braintribe.model.elasticsearchdeployment.indexing.ElasticsearchIndexingMetaData;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.elasticsearch.IndexedElasticsearchConnector;
import com.braintribe.model.processing.elasticsearch.fulltext.FulltextProcessing;
import com.braintribe.model.processing.elasticsearch.status.WorkerStatus;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.worker.api.WorkerContext;
import com.braintribe.model.processing.worker.api.WorkerException;
import com.braintribe.model.query.EntityQuery;

/**
 * This worker implementation covers incoming indexing-events of entities and its properties against elastic. An
 * entity/property to be indexed is marked with the meta data {@link ElasticsearchIndexingMetaData}.
 *
 * @author christina.wilpernig
 */
public class ElasticsearchIndexingWorkerImpl implements ElasticsearchIndexingWorker, Runnable {

	private final static Logger logger = Logger.getLogger(ElasticsearchIndexingWorkerImpl.class);

	private LinkedBlockingQueue<IndexingPackage> queue = null;
	protected Function<String, PersistenceGmSession> sessionFactory = null;

	private GenericEntity workerIdentification;
	private IndexedElasticsearchConnector elasticsearchConnector;
	private boolean started;
	private int queueSize = 1000;
	private int threadCount = 10;

	private ScheduledThreadPoolExecutor scheduledThreadPool;
	private EnsureOpenIndex ensureOpenIndex;
	private ScheduledFuture<?> ensureOpenIndexFuture;

	private static Set<String> ensuredModels = ConcurrentHashMap.newKeySet();

	private AtomicInteger activeWorker = new AtomicInteger(0);
	private ConcurrentHashMap<String, Long> startTimesPerThread = new ConcurrentHashMap<>();
	private AtomicInteger packagesIndexed = new AtomicInteger(0);
	private AtomicInteger entitiesIndexed = new AtomicInteger(0);

	private List<Future<?>> workerFutures = null;
	private Map<String, Boolean> initializedAccesses = new ConcurrentHashMap<>();

	private FulltextProcessing fulltextProcessing;

	@Override
	public boolean enqueue(IndexingPackage indexingPackage) {

		if (started) {
			boolean added = queue.offer(indexingPackage);

			if (added) {
				if (logger.isTraceEnabled()) {
					logger.trace("Enqueued " + indexingPackage);
				}
				return true;
			} else {
				logger.warn(() -> "Could not enqueue indexing package: " + indexingPackage + " because the queue exceeded the maximum limit.");
			}
		} else {
			logger.warn(() -> "Could not enqueue indexing package: " + indexingPackage + " as the indexing worker has not (yet) started.");
		}

		return false;
	}

	@Override
	public GenericEntity getWorkerIdentification() {
		return workerIdentification;
	}

	@Override
	public void start(WorkerContext workerContext) throws WorkerException {
		started = true;

		if (queue == null) {
			queue = new LinkedBlockingQueue<>(queueSize);
		}

		if (logger.isDebugEnabled()) {
			logger.debug(workerContext.getClass().getName() + " initialized. Queue size is " + queueSize + ", thread count is " + threadCount);
		}

		workerFutures = new ArrayList<>(threadCount);
		for (int i = 0; i < threadCount; i++) {
			workerFutures.add(workerContext.submit(this));
		}

		if (scheduledThreadPool != null && elasticsearchConnector != null) {
			logger.debug(() -> "Scheduling thread to ensure index " + elasticsearchConnector.getIndex() + " remains open");
			ensureOpenIndex = new EnsureOpenIndex();
			ensureOpenIndex.setElasticsearchConnector(elasticsearchConnector);
			ensureOpenIndexFuture = scheduledThreadPool.scheduleWithFixedDelay(ensureOpenIndex, 1, 10, TimeUnit.MINUTES);
		} else {
			logger.debug(() -> "NOT scheduling thread to ensure the index remains open because either the thread pool (" + scheduledThreadPool
					+ ") or the connector (" + elasticsearchConnector + ") is null.");
		}
	}

	@Override
	public void stop(WorkerContext workerContext) throws WorkerException {

		if (ensureOpenIndexFuture != null) {
			try {
				ensureOpenIndexFuture.cancel(true);
				ensureOpenIndex = null;
			} catch (Exception e) {
				logger.debug(() -> "Error while trying to cancel the EnsureOpenIndex future", e);
			} finally {
				ensureOpenIndexFuture = null;
			}
		}

		if (workerContext != null) {
			if (logger.isDebugEnabled()) {
				logger.debug(workerContext.getClass().getName() + " stopped.");
			}
		}

		started = false;
		if (queue != null) {
			try {
				IndexingPackage p = null;
				while ((p = queue.remove()) != null) {
					p.close("Worker stopped, not indexed");
				}

			} catch (NoSuchElementException e) {
				// ignore
			}
		}

		if (workerFutures != null) {
			for (Future<?> f : workerFutures) {
				f.cancel(true);
			}
		}
	}

	public void stop() throws WorkerException {
		if (logger.isDebugEnabled()) {
			logger.debug("Stopping worker " + workerIdentification.getClass().getSimpleName() + "...");
		}
		stop(null);
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

	@Override
	public WorkerStatus getStatus() {
		WorkerStatus status = WorkerStatus.T.create();
		status.setWorkerIdentification(workerIdentification);
		status.setIndex(elasticsearchConnector.getIndex());
		status.setQueueSize(queueSize);
		status.setEnqueued(queue.size());
		status.setWorkerCount(threadCount);
		status.setActiveWorker(activeWorker.intValue());
		status.setIndexedPackagesCount(packagesIndexed.intValue());
		status.setIndexedEntitiesCount(entitiesIndexed.intValue());
		TreeSet<Long> times = new TreeSet<>(startTimesPerThread.values());
		if (!times.isEmpty()) {
			Long first = null;
			while (first == null && !times.isEmpty()) {
				first = times.pollFirst();
			}
			if (first != null) {
				long duration = System.currentTimeMillis() - first;
				status.setMaxActiveRuntime(duration);
			} else {
				status.setMaxActiveRuntime(0);
			}
		} else {
			status.setMaxActiveRuntime(0);
		}
		return status;
	}

	@Override
	public void run() {
		int packageCount = 0;
		int processedEntitiesCount = 0;

		String workerThreadId = UUID.randomUUID().toString();

		while (started) {
			try {
				IndexingPackage indexingPackage = queue.poll(500, TimeUnit.MILLISECONDS);

				if (indexingPackage != null) {

					/**
					 * If the initialization does not work, errors will be logged and the worker thread will end here.
					 */
					if (!initialize(indexingPackage.getAccessId())) {
						return;
					}

					activeWorker.incrementAndGet();
					startTimesPerThread.put(workerThreadId, System.currentTimeMillis());
					try {
						if (logger.isDebugEnabled()) {
							logger.debug("Fetched package " + indexingPackage + " from queue, remaining queue items: " + queue.size());
						}
						processedEntitiesCount += indexingPackage.getPackageSize();
						packagesIndexed.incrementAndGet();
						entitiesIndexed.addAndGet(indexingPackage.getPackageSize());

						handleIndexing(indexingPackage, ++packageCount, processedEntitiesCount);
					} finally {
						activeWorker.decrementAndGet();
						startTimesPerThread.remove(workerThreadId);
					}
				}

			} catch (InterruptedException e) {
				logger.debug("Worker got interrupted, stopping.");
				break;
			} catch (Exception e) {
				logger.error("Unexpected runtime exception while performing work! Worker will not be stopped.", e);
			}
		}
	}

	private void handleIndexing(IndexingPackage indexingPackage, int packageCount, int processedEntitiesCount) throws Exception {

		PersistenceGmSession session = sessionFactory.apply(indexingPackage.getAccessId());
		logger.pushContext(indexingPackage + ". Package no:'" + packageCount + "'");
		long indexingduration = 0;

		try {
			Map<GenericEntity, List<Property>> entities = indexingPackage.getEntitiesToIndex();
			for (Map.Entry<GenericEntity, List<Property>> entry : entities.entrySet()) {

				if (!started) {
					if (logger.isDebugEnabled()) {
						logger.debug("Stop processing of indexable data (as requested).");
					}
					break;
				}

				GenericEntity entity = entry.getKey();
				List<Property> properties = entry.getValue();

				if (logger.isTraceEnabled()) {
					logger.trace("Indexing " + GMCoreTools.getDescription(entity) + " with changed properties "
							+ GMCoreTools.getDescriptionForObject(properties));
				}

				long t1 = System.currentTimeMillis();

				try {
					fulltextProcessing.index(elasticsearchConnector, entity, properties, session, null);
				} catch (Exception e) {
					logger.warn(() -> "Could not index entity " + entity, e);
				}

				indexingduration += (System.currentTimeMillis() - t1);
			}

		} finally {

			indexingPackage.close(null);

			if (logger.isDebugEnabled()) {
				logger.debug(
						"Processed " + indexingPackage + " (total: " + processedEntitiesCount + "). The indexing took: " + indexingduration + " ms.");
			}
			logger.popContext();
		}
	}

	/**
	 * Ensures that all required {@link com.braintribe.model.meta.GmMetaModel GmMetaModels} are available for further
	 * processing.
	 *
	 * @return <code>true</code> if the initialization was successful, <code>false</code> otherwise
	 */
	private synchronized boolean initialize(String accessId) {

		return initializedAccesses.computeIfAbsent(accessId, aid -> {
			PersistenceGmSession session;
			try {
				session = sessionFactory.apply(accessId);

				ManagedGmSession managedSession = session.getModelAccessory().getModelSession();
				EntityQuery metaModelQuery = EntityQueryBuilder.from(GmMetaModel.T).done();
				List<GmMetaModel> metaModels = managedSession.query().entities(metaModelQuery).list();

				for (GmMetaModel model : metaModels) {

					// We have to make sure that a model is only ensured once here
					// Multiple workers may try to do the same here.

					String name = model.getName();
					if (!ensuredModels.contains(name)) {
						ensuredModels.add(name);

						try {
							model.deploy();
						} catch (Exception e) {
							logger.error("Error while ensuring model types for model '" + name + "'!", e);
							return false;
						}

					}
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Successfully initialized worker and model types.");
				}

				return true;

			} catch (Exception e) {
				logger.error("Error while initializing worker " + this.getClass().getSimpleName(), e);
				return false;
			}
		});

	}

	/**
	 * Getter, Setter
	 */

	@Required
	public void setElasticsearchConnector(IndexedElasticsearchConnector elasticsearchConnector) {
		this.elasticsearchConnector = elasticsearchConnector;
	}

	@Required
	public void setWorkerIdentification(GenericEntity workerIdentification) {
		this.workerIdentification = workerIdentification;
	}

	@Required
	public void setSessionFactory(Function<String, PersistenceGmSession> sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Configurable
	public void setQueueSize(Integer queueSize) {
		if (queueSize != null) {
			this.queueSize = queueSize.intValue();
		}
	}

	@Configurable
	public void setThreadCount(Integer threadCount) {
		if (threadCount != null) {
			this.threadCount = threadCount;
		}
	}

	@Configurable
	@Required
	public void setScheduledThreadPool(ScheduledThreadPoolExecutor scheduledThreadPool) {
		this.scheduledThreadPool = scheduledThreadPool;
	}

	@Configurable
	@Required
	public void setFulltextProcessing(FulltextProcessing fulltextProcessing) {
		this.fulltextProcessing = fulltextProcessing;
	}
}
