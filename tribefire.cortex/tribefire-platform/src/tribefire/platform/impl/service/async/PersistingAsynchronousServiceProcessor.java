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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.DestructionAware;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.HasStringCodec;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.common.lcd.Pair;
import com.braintribe.execution.ExtendedThreadPoolExecutor;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.deployment.api.DeployedComponentResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.service.api.AsynchronousRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.AsynchronousResponse;
import com.braintribe.model.service.async.AsynchronousRequestPersistence;
import com.braintribe.model.service.async.AsynchronousRequestPersistenceStrategy;
import com.braintribe.model.service.async.AsynchronousRequestThreadPool;
import com.braintribe.model.service.async.ResourceKind;
import com.braintribe.model.service.async.meta.AsynchronousRequestProcessing;
import com.braintribe.thread.api.ThreadContextScoping;
import com.braintribe.utils.RandomTools;
import com.braintribe.utils.lcd.StopWatch;

/**
 * A {@link ServiceProcessor} which processes the {@link ServiceRequest}(s) wrapped by the incoming
 * {@link AsynchronousRequest}(s) asynchronously.
 * 
 */
public class PersistingAsynchronousServiceProcessor implements ServiceProcessor<AsynchronousRequest, AsynchronousResponse>, DestructionAware {

	private static final Logger log = Logger.getLogger(PersistingAsynchronousServiceProcessor.class);

	protected PersistenceGmSessionFactory sessionFactory;

	protected Function<ServiceRequest, CmdResolver> metaDataResolverProvider;

	protected Map<Pair<String, String>, ExecutorService> executorServiceMap = new ConcurrentHashMap<>();
	protected DeployedComponentResolver deployedComponentResolver;
	protected Evaluator<ServiceRequest> requestEvaluator;

	protected ServiceRequestPersistence servicePersistence;
	protected HasStringCodec stringCodec;

	protected CallbackExpert callbackExpert;
	protected ThreadContextScoping threadScoping;

	@Override
	public AsynchronousResponse process(ServiceRequestContext requestContext, AsynchronousRequest request) {

		Objects.requireNonNull(requestContext, "requestContext");
		Objects.requireNonNull(request, "request");

		ServiceRequest payload = request.getServiceRequest();

		if (payload == null) {
			throw new IllegalArgumentException("The incoming " + AsynchronousRequest.class.getSimpleName() + " has no service request set.");
		}

		String correlationId = request.getCorrelationId();
		if (correlationId == null) {
			correlationId = RandomTools.newStandardUuid();
			request.setCorrelationId(correlationId);
		}

		AsynchronousResponse response = AsynchronousResponse.T.create();
		response.setCorrelationId(correlationId);

		submitAsyncRequest(requestContext, request, correlationId, false);

		return response;

	}

	public void submitAsyncRequest(ServiceRequestContext requestContext, AsynchronousRequest request, String correlationId, boolean revivedRequest) {

		ServiceRequest payload = request.getServiceRequest();

		AsynchronousRequestProcessing processingMetaData = getAsynchronousRequestProcessingMetaData(payload);

		StopWatch stopWatch = new StopWatch();

		ExecutorService service = getExecutorService(processingMetaData);

		Runnable runnable = null;
		if (!revivedRequest) {
			AsynchronousRequestPersistenceStrategy strategy = getPersistenceStrategy(processingMetaData);
			if (strategy == AsynchronousRequestPersistenceStrategy.always) {
				AsynchronousRequestPersistence requestPersistence = processingMetaData.getAsynchronousRequestPersistence();
				Supplier<PersistenceGmSession> sessionSupplier = getPersistenceSessionSupplier(requestPersistence);
				PersistenceGmSession session = sessionSupplier.get();
				String discriminator = requestPersistence.getDiscriminator();
				ResourceKind persistResponseResources = getPersistResponseResourcesStrategy(requestPersistence);
				servicePersistence.persistServiceRequest(session, correlationId, request, discriminator, stopWatch);
				session.commit();
				PersistedServiceRequestRunnable persistedRunnable = new PersistedServiceRequestRunnable(requestContext, correlationId, request,
						requestEvaluator, sessionSupplier, discriminator, stringCodec, callbackExpert, persistResponseResources, sessionFactory);
				persistedRunnable.setPersisted(true);
				runnable = persistedRunnable;
			} else if (strategy == AsynchronousRequestPersistenceStrategy.onEnqueue) {
				AsynchronousRequestPersistence requestPersistence = processingMetaData.getAsynchronousRequestPersistence();
				String discriminator = requestPersistence.getDiscriminator();
				ResourceKind persistResponseResources = getPersistResponseResourcesStrategy(requestPersistence);
				Supplier<PersistenceGmSession> sessionSupplier = getPersistenceSessionSupplier(requestPersistence);
				runnable = new PersistedServiceRequestRunnable(requestContext, correlationId, request, requestEvaluator, sessionSupplier,
						discriminator, stringCodec, callbackExpert, persistResponseResources, sessionFactory);
			} else {
				runnable = new ServiceRequestRunnable(correlationId, request, requestEvaluator, callbackExpert);
			}
		} else {
			AsynchronousRequestPersistence requestPersistence = processingMetaData.getAsynchronousRequestPersistence();
			String discriminator = requestPersistence.getDiscriminator();
			Supplier<PersistenceGmSession> sessionSupplier = getPersistenceSessionSupplier(requestPersistence);
			ResourceKind persistResponseResources = getPersistResponseResourcesStrategy(requestPersistence);

			PersistedServiceRequestRunnable persistedRunnable = new PersistedServiceRequestRunnable(requestContext, correlationId, request,
					requestEvaluator, sessionSupplier, discriminator, stringCodec, callbackExpert, persistResponseResources, sessionFactory);
			persistedRunnable.setPersisted(true);
			runnable = persistedRunnable;
		}

		runnable = threadScoping.bindContext(runnable);

		log.trace(() -> "Asynchronous processing " + correlationId + " of " + payload.getClass().getName() + " will be submitted");

		service.submit(runnable);

		log.trace("Asynchronous processing " + correlationId + " of " + payload.getClass().getName() + ": " + stopWatch);
	}

	private ResourceKind getPersistResponseResourcesStrategy(AsynchronousRequestPersistence requestPersistence) {
		ResourceKind strategy = requestPersistence.getPersistResponseResourcesStrategy();
		if (strategy == null) {
			return ResourceKind.none;
		}
		return strategy;
	}

	protected AsynchronousRequestProcessing getAsynchronousRequestProcessingMetaData(ServiceRequest payload) {
		CmdResolver metaDataResolver = metaDataResolverProvider.apply(payload);
		AsynchronousRequestProcessing processingMetaData = metaDataResolver.getMetaData().lenient(true).entity(payload)
				.meta(AsynchronousRequestProcessing.T).exclusive();
		return processingMetaData;
	}

	protected Supplier<PersistenceGmSession> getPersistenceSessionSupplier(AsynchronousRequestPersistence requestPersistence) {
		IncrementalAccess persistence = requestPersistence.getPersistence();
		if (persistence == null) {
			throw new IllegalStateException("The AsynchronousRequestPersistence " + requestPersistence + " does not reference an IncrementalAccess.");
		}
		return () -> sessionFactory.newSession(persistence.getExternalId());
	}

	protected AsynchronousRequestPersistenceStrategy getPersistenceStrategy(AsynchronousRequestProcessing processingMetaData) {
		if (processingMetaData == null) {
			return AsynchronousRequestPersistenceStrategy.never;
		}
		AsynchronousRequestPersistence requestPersistence = processingMetaData.getAsynchronousRequestPersistence();
		if (requestPersistence == null) {
			return AsynchronousRequestPersistenceStrategy.never;
		}
		return requestPersistence.getPersistenceStrategy();
	}

	protected ExecutorService getExecutorService(AsynchronousRequestProcessing processingMetaData) {

		Pair<String, String> key = null;
		if (processingMetaData != null) {
			AsynchronousRequestThreadPool serviceReference = processingMetaData.getExecutionPool();
			String serviceId = serviceReference != null ? serviceReference.getServiceId() : "default";

			AsynchronousRequestPersistence persistence = processingMetaData.getAsynchronousRequestPersistence();
			String persistenceId = persistence != null ? persistence.getGlobalId() : "default";

			key = Pair.of(serviceId, persistenceId);
		} else {
			key = Pair.of("default", "default");
		}

		return executorServiceMap.computeIfAbsent(key, sid -> {

			BlockingQueue<Runnable> queue = null;
			int corePoolSize = 5;
			int maxPoolSize = 5;
			long keepAliveTime = Numbers.MILLISECONDS_PER_MINUTE * 3;

			if (processingMetaData != null) {
				AsynchronousRequestPersistence requestPersistence = processingMetaData.getAsynchronousRequestPersistence();

				String discriminator = requestPersistence.getDiscriminator();
				AsynchronousRequestPersistenceStrategy strategy = getPersistenceStrategy(processingMetaData);
				switch (strategy) {
					case never:
					case always: {
						queue = new LinkedBlockingQueue<>();
						break;
					}
					case onEnqueue: {
						Supplier<PersistenceGmSession> sessionSupplier = getPersistenceSessionSupplier(requestPersistence);
						queue = new PersistingServiceRequestBlockingQueue(sessionSupplier, discriminator, servicePersistence);
						break;
					}
					default:
						throw new IllegalStateException("Unsupported persistence strategy: " + strategy);
				}

				AsynchronousRequestThreadPool serviceReference = processingMetaData.getExecutionPool();
				if (serviceReference != null) {
					corePoolSize = serviceReference.getCorePoolSize();
					maxPoolSize = serviceReference.getMaxPoolSize();
					keepAliveTime = serviceReference.getKeepAliveTime();
				}

			} else {
				queue = new LinkedBlockingQueue<>();
			}

			ExtendedThreadPoolExecutor bean = new ExtendedThreadPoolExecutor(corePoolSize, maxPoolSize, // maxPoolSize
					keepAliveTime, // keepAliveTime
					TimeUnit.MILLISECONDS, queue);
			return bean;
		});
	}

	@Override
	public void preDestroy() {
		executorServiceMap.values().forEach(p -> p.shutdown());
	}

	@Required
	@Configurable
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	@Required
	@Configurable
	public void setMetaDataResolverProvider(Function<ServiceRequest, CmdResolver> metaDataResolverProvider) {
		this.metaDataResolverProvider = metaDataResolverProvider;
	}
	@Required
	@Configurable
	public void setDeployedComponentResolver(DeployedComponentResolver deployedComponentResolver) {
		this.deployedComponentResolver = deployedComponentResolver;
	}
	@Required
	@Configurable
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}
	@Required
	@Configurable
	public void setServicePersistence(ServiceRequestPersistence servicePersistence) {
		this.servicePersistence = servicePersistence;
	}
	@Required
	@Configurable
	public void setStringCodec(HasStringCodec stringCodec) {
		this.stringCodec = stringCodec;
	}
	@Required
	@Configurable
	public void setCallbackExpert(CallbackExpert callbackExpert) {
		this.callbackExpert = callbackExpert;
	}
	@Required
	@Configurable
	public void setThreadScoping(ThreadContextScoping threadScoping) {
		this.threadScoping = threadScoping;
	}

}
