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
package tribefire.platform.impl.service.async;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import com.braintribe.codec.marshaller.api.HasStringCodec;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.execution.persistence.JobState;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.traverse.EntityCollector;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.AsynchronousRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.async.ResourceKind;
import com.braintribe.model.service.persistence.ServiceRequestJob;
import com.braintribe.model.service.persistence.service.PersistedDataWithResources;

public class PersistedServiceRequestRunnable extends ServiceRequestRunnable {

	private static final Logger logger = Logger.getLogger(PersistedServiceRequestRunnable.class);

	private Supplier<PersistenceGmSession> sessionSupplier;
	private boolean persisted = false;
	private String discriminator;
	private HasStringCodec stringCodec;
	private long processingStart = 0;
	private PersistenceGmSession session = null;
	private ServiceRequestContext requestContext;

	private ResourceKind persistResponseResources;

	private PersistenceGmSessionFactory sessionFactory;

	public PersistedServiceRequestRunnable(ServiceRequestContext requestContext, String correlationId, AsynchronousRequest asyncRequest,
			Evaluator<ServiceRequest> requestEvaluator, Supplier<PersistenceGmSession> sessionSupplier, String discriminator,
			HasStringCodec stringCodec, CallbackExpert callbackExpert, ResourceKind persistResponseResources,
			PersistenceGmSessionFactory sessionFactory) {
		super(correlationId, asyncRequest, requestEvaluator, callbackExpert);
		this.requestContext = requestContext;
		this.sessionSupplier = sessionSupplier;
		this.discriminator = discriminator;
		this.stringCodec = stringCodec;
		this.persistResponseResources = persistResponseResources;
		this.sessionFactory = sessionFactory;
	}

	@Override
	protected void preFlight() {
		session = sessionSupplier.get();
		ServiceRequestJob job = getServiceRequestJob(session);
		if (job != null) {
			job.setState(JobState.running);
			Integer tries = job.getTries();
			if (tries == null) {
				tries = 0;
			}
			job.setTries(tries + 1);

			if (requestContext != null) {
				job.setClientSessionId(requestContext.getRequestorSessionId());
				job.setClientUsername(requestContext.getRequestorUserName());
				job.setClientAddress(requestContext.getRequestorAddress());
			}
			session.commit();
		}
		processingStart = System.currentTimeMillis();

		super.preFlight();
	}

	@Override
	protected void onSuccess(Object result) {
		ServiceRequestJob job = getServiceRequestJob(session);
		if (job != null) {
			Set<Resource> transientResources = new HashSet<>();
			Date now = new Date();
			job.setState(JobState.done);
			job.setLastStatusUpdate(now);
			job.setEndTimestamp(now);
			job.setDuration(System.currentTimeMillis() - processingStart);

			logger.debug(() -> "An asynchronous service request was successful. Persisting the result now. persistResponseResources is set to "
					+ persistResponseResources);

			Map<Resource, String> resourcePersistenceIds = new HashMap<>();
			PersistedDataWithResources envelope = PersistedDataWithResources.T.create();
			envelope.setData(result);
			envelope.setResourcePersistenceIds(resourcePersistenceIds);

			AtomicInteger resources = new AtomicInteger(0);
			AtomicInteger persistedResources = new AtomicInteger(0);
			EntityCollector entityCollector = new EntityCollector() {
				@Override
				protected boolean add(GenericEntity entity, EntityType<?> type) {
					if (entity instanceof Resource) {
						resources.incrementAndGet();
						Resource r = (Resource) entity;
						logger.trace(() -> "Examining resource " + r + " if it should be persisted.");

						if (persistResource(r, transientResources)) {
							logger.trace(() -> "Resource " + r + " will be persisted.");
							persistedResources.incrementAndGet();

							InputStreamProvider streamProvider = getInputStreamProvider(r);
							if (streamProvider != null) {
								Resource persistedResource = session.resources().create().md5(r.getMd5()).name(r.getName()).mimeType(r.getMimeType())
										.store(streamProvider);
								resourcePersistenceIds.put(r, persistedResource.getId());
								job.getResultResources().add(persistedResource);
							}
						}
					}

					return super.add(entity, type);
				}
			};
			entityCollector.visit(result);

			String serializedResult = stringCodec.getStringCodec().encode(envelope);
			job.setSerializedResult(serializedResult);

			session.commit();

			logger.debug(() -> "Done persisting the result. Resources: " + resources.get() + ", persisted Resources: " + persistedResources.get());

		} else {
			logger.debug(() -> "Could not find the ServiceRequestJob with the correlation ID: " + correlationId);
		}

		super.onSuccess(result);
	}

	protected boolean persistResource(Resource r, Set<Resource> transientResources) {
		if (persistResponseResources == null || persistResponseResources == ResourceKind.none) {
			return false;
		}
		if (r.isTransient()) {
			if (!transientResources.add(r)) {
				return false;
			}

			return persistResponseResources == ResourceKind.transientOnly || persistResponseResources == ResourceKind.all;
		} else {
			return persistResponseResources == ResourceKind.all;
		}
	}

	protected InputStreamProvider getInputStreamProvider(Resource r) {
		if (r == null) {
			return null;
		}
		if (r.isTransient()) {
			return r::openStream;
		}
		String partition = r.getPartition();
		Object id = r.getId();
		if (partition == null || id == null) {
			return null;
		}
		PersistenceGmSession session = sessionFactory.newSession(partition);
		return () -> session.resources().openStream(r);
	}

	@Override
	protected void onFailure(Throwable t) {
		ServiceRequestJob job = getServiceRequestJob(session);
		if (job != null) {
			Date now = new Date();
			job.setState(JobState.panic);
			job.setLastStatusUpdate(now);
			job.setEndTimestamp(now);
			job.setStackTrace(Exceptions.stringify(t));
			job.setErrorMessage(t.getMessage());
			session.commit();
		}

		super.onFailure(t);
	}

	@Override
	protected void doCallback(Object result, Throwable t) {
		super.doCallback(result, t);

		ServiceRequestJob job = getServiceRequestJob(session);
		if (job != null) {
			job.setNotificationTimestamp(new Date());
			session.commit();
		}
	}

	private ServiceRequestJob getServiceRequestJob(PersistenceGmSession session) {
		EntityQuery query = EntityQueryBuilder.from(ServiceRequestJob.T).where().property(ServiceRequestJob.id).eq(correlationId).done();
		ServiceRequestJob job = session.query().entities(query).first();
		return job;
	}

	public boolean getPersisted() {
		return persisted;
	}
	public void setPersisted(boolean persisted) {
		this.persisted = persisted;
	}
	public String getDiscriminator() {
		return discriminator;
	}

}
