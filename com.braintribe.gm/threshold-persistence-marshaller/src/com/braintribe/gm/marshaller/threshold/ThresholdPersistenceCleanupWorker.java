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
package com.braintribe.gm.marshaller.threshold;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.building.EntityQueries;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.worker.api.Worker;
import com.braintribe.model.processing.worker.api.WorkerContext;
import com.braintribe.model.processing.worker.api.WorkerException;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resourceapi.persistence.DeleteResource;
import com.braintribe.model.resourceapi.persistence.DeletionScope;

public class ThresholdPersistenceCleanupWorker implements Worker {

	private Logger logger = Logger.getLogger(ThresholdPersistenceCleanupWorker.class);
	
	private Duration cleanupInterval = Duration.ofMinutes(5);
	private Duration resourceTtl = Duration.ofMinutes(20);
	private Supplier<PersistenceGmSession> sessionFactory;
	private int batchSize = 100;
	private GenericEntity workerIdentification;
	private Future<?> cleanupWorkerFuture;
	
	/**
	 * Configures the interval in which expired resources will be found and deleted
	 */
	@Configurable
	public void setCleanupInterval(Duration cleanupInterval) {
		this.cleanupInterval = cleanupInterval;
	}
	
	@Configurable
	public void setResourceTtl(Duration resourceExpiry) {
		this.resourceTtl = resourceExpiry;
	}
	
	@Configurable
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	
	@Required
	public void setWorkerIdentification(GenericEntity workerIdentification) {
		this.workerIdentification = workerIdentification;
	}
	
	@Required
	public void setSessionFactory(Supplier<PersistenceGmSession> sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public void start(WorkerContext workerContext) throws WorkerException {
		cleanupWorkerFuture = workerContext.submit(this::cleanup);
	}

	@Override
	public void stop(WorkerContext workerContext) throws WorkerException {
		if (cleanupWorkerFuture != null)
			cleanupWorkerFuture.cancel(true);
	}
	
	@Override
	public GenericEntity getWorkerIdentification() {
		return workerIdentification;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
	
	private void cleanup() {
		logger.debug(() -> "starting cleanup thread");
		
		while (true) {
			try {
				Thread.sleep(cleanupInterval.toMillis());
				new StatefulCleanup().process();
			}
			catch (InterruptedException e) {
				// worker was canceled
				break;
			}
			catch (Throwable t) {
				logger.error("Error in cleanup interval loop", t);
			}
		}
		
		logger.debug(() -> "ending cleanup thread");
	}
	
	private class StatefulCleanup extends EntityQueries {
		
		private PersistenceGmSession session = sessionFactory.get();
		private String cleanupIntervalId = UUID.randomUUID().toString();
		private Date limitDate = new Date(System.currentTimeMillis() - resourceTtl.toMillis());
		
		public void process() {
			
			logger.pushContext("cleanup-interval-" + cleanupIntervalId);
			
			try {
				
				logger.debug(() -> "starting resource cleanup interval with batch size " + batchSize);
				
				boolean hasMore;
				
				do {
					Pair<List<Resource>, Boolean> flaggedOutdatedResources = queryExpiredResources();
					
					List<Resource> outdatedResources = flaggedOutdatedResources.first();
					hasMore = flaggedOutdatedResources.second();
	
					if (outdatedResources.isEmpty()) {
						logger.debug(() -> "no expired resources found");
						break;
					}
					
					logger.debug(() -> "deleting " + outdatedResources.size() + " expired resource(s)");
					
					for (Resource outdatedResource: outdatedResources) {
						DeleteResource deleteResource = DeleteResource.T.create();
						deleteResource.setDeletionScope(DeletionScope.resource);
						deleteResource.setResource(outdatedResource);
						session.eval(deleteResource).get();
					}
					
				} while (hasMore);
				
				logger.debug(() -> "ending resource cleanup interval");
			}
			finally {
				logger.popContext();
			}
		}

		private Pair<List<Resource>, Boolean> queryExpiredResources() {
			logger.debug(() -> "querying for resources exceeding the creation limit: " + limitDate);
			
			EntityQuery query = from(Resource.T).where(lt(property(Resource.created), limitDate)).orderBy(property(Resource.created)).limit(batchSize);
			EntityQueryResult result = session.query().entities(query).result();
			List<Resource> resources = (List<Resource>)(List<?>)result.getEntities();
			
			return Pair.of(resources, result.getHasMore());
		}
	}
}
