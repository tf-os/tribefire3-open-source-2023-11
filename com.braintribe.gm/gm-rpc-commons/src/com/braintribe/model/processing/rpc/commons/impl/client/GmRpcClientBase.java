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
package com.braintribe.model.processing.rpc.commons.impl.client;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.Codec;
import com.braintribe.codec.marshaller.api.EntityVisitorOption;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.common.attribute.TypeSafeAttribute;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.ReasonException;
import com.braintribe.gm.model.reason.UnsatisfiedMaybeTunneling;
import com.braintribe.gm.model.security.reason.AuthenticationFailure;
import com.braintribe.logging.Logger;
import com.braintribe.logging.ThreadRenamer;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.eval.AbstractEvalContext;
import com.braintribe.model.generic.eval.EvalException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.rpc.commons.api.GmRpcException;
import com.braintribe.model.processing.rpc.commons.api.RpcConstants;
import com.braintribe.model.processing.rpc.commons.api.authorization.RpcClientAuthorizationContext;
import com.braintribe.model.processing.rpc.commons.api.config.GmRpcClientConfig;
import com.braintribe.model.processing.rpc.commons.impl.authorization.EmptyAuthorizationContext;
import com.braintribe.model.processing.service.api.ResponseConsumerAspect;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.api.ServiceRequestSummaryLogger;
import com.braintribe.model.processing.service.common.FailureCodec;
import com.braintribe.model.resource.source.TransientSource;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.thread.api.ThreadContextScoping;
import com.braintribe.utils.lcd.StopWatch;

public abstract class GmRpcClientBase {

	protected static final Supplier<Map<String, Object>> defaultMetaDataProvider = Collections::emptyMap;
	protected static final RpcClientAuthorizationContext<Throwable> defaultAuthorizationContext = new EmptyAuthorizationContext();

	private Supplier<Map<String, Object>> metaDataProvider = defaultMetaDataProvider;
	private Codec<Throwable, Failure> failureCodec = FailureCodec.INSTANCE;
	private RpcClientAuthorizationContext<Throwable> authorizationContext = defaultAuthorizationContext;
	private Supplier<ServiceRequestContext> requestContextSupplier;

	protected volatile boolean stopProcessing = false;

	protected String serviceId;
	protected Class<?> serviceInterface;
	protected InstanceId clientInstanceId;
	protected GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	protected Consumer<Set<String>> requiredTypesReceiver;

	protected static ThreadRenamer threadRenamer;

	protected ThreadContextScoping threadContextScoping;
	protected ExecutorService executorService = ForkJoinPool.commonPool();

	protected GmRpcClientBase() {
	}

	protected Supplier<Map<String, Object>> getMetaDataProvider() {
		return metaDataProvider;
	}

	protected void setMetaDataProvider(Supplier<Map<String, Object>> metaDataProvider) {
		this.metaDataProvider = metaDataProvider == null ? defaultMetaDataProvider : metaDataProvider;
	}

	public Codec<Throwable, Failure> getFailureCodec() {
		return failureCodec;
	}

	public void setFailureCodec(Codec<Throwable, Failure> failureCodec) {
		Objects.requireNonNull(failureCodec, "failureCodec cannot be set to null");
		this.failureCodec = failureCodec;
	}

	protected RpcClientAuthorizationContext<Throwable> getAuthorizationContext() {
		return authorizationContext;
	}

	protected void setAuthorizationContext(RpcClientAuthorizationContext<Throwable> authorizationContext) {
		this.authorizationContext = authorizationContext == null ? defaultAuthorizationContext : authorizationContext;
	}

	protected Supplier<ServiceRequestContext> getRequestContextSupplier() {
		return requestContextSupplier;
	}

	protected void setRequestContextSupplier(Supplier<ServiceRequestContext> requestContextSupplier) {
		this.requestContextSupplier = requestContextSupplier;
	}

	protected abstract ServiceResult sendRequest(GmRpcClientRequestContext requestContext);

	protected abstract Logger logger();

	protected <T> Maybe<T> invoke(GmRpcClientRequestContext clientContext) throws Throwable {

		StopWatch stopWatch = new StopWatch();
		stopWatch.intermediate(Thread.currentThread().getName());

		ServiceResult response = null;
		Logger log = logger();

		try {

			int reAuthorizationRetries = getAuthorizationContext().getMaxRetries();

			if (!clientContext.isAsynchronous()) {
				enrichWithMetaData(clientContext.getServiceRequest());
				stopWatch.intermediate("Enrich with meta-data");
			}

			while (!stopProcessing) {
				response = sendRequest(clientContext);
				stopWatch.intermediate("Send Request");

				if (response == null) {
					throw new GmRpcException("Unexpected null rpc response");
				}

				switch (response.resultType()) {
					case success:
						return Maybe.complete((T) response.asResponse().getResult());

					case unsatisfied: {
						Maybe<T> maybe = response.asUnsatisfied().toMaby();
						if (maybe.isUnsatisfiedBy(AuthenticationFailure.T)) {
							reAuthorizationRetries = evaluateReAuthorization(new UnsatisfiedMaybeTunneling(maybe), reAuthorizationRetries);
							enrichWithMetaData(clientContext.getServiceRequest(), true);
							continue;
						}
						return maybe;
					}

					case failure: {
						Throwable exception = decodeFailure(response.asFailure());
						stopWatch.intermediate("Decode Failure");
						throw exception;
					}

					default:
						throw new IllegalStateException("Unexpected result type: " + response.resultType());
				}
			}

		} finally {
			clientContext.summaryLogger().log(this, clientContext.getServiceRequest());
			clientContext.summaryLogger().logOneLine(RpcConstants.RPC_LOGSTEP_FINAL, clientContext.getServiceRequest());

			log.trace(() -> "RpcEvalContext.invoke: " + stopWatch);
		}

		throw new GmRpcException("RPC client is closed: " + toString());

	}

	protected void setConfig(GmRpcClientConfig config) {
		setMetaDataProvider(config.getMetaDataProvider());
		setAuthorizationContext(config.getAuthorizationContext());
		setRequiredTypesReceiver(config.getRequiredTypesReceiver());
		setExecutorService(config.getExecutorService());
		setClientInstanceId(config.getClientInstanceId());
		setRequestContextSupplier(getRequestContextSupplier());
	}

	protected void enrichWithMetaData(ServiceRequest request) {
		enrichWithMetaData(request, false);
	}

	protected void enrichWithMetaData(ServiceRequest request, boolean refreshSessionId) {
		Map<String, Object> metaData = metaDataProvider.get();
		if (metaData != null && !metaData.isEmpty()) {
			Map<String, Object> existingMetadata = request.getMetaData();
			if (existingMetadata == null) {
				existingMetadata = new HashMap<>();
				request.setMetaData(existingMetadata);
			}
			for (Entry<String, Object> e : metaData.entrySet()) {
				String key = e.getKey();
				Object value = e.getValue();
				existingMetadata.put(key, value);
				if (AuthorizedRequest.sessionId.equals(key) && refreshSessionId && request.requiresAuthentication()) {
					((AuthorizedRequest) request).setSessionId(value != null ? value.toString() : null);
				}
			}
		}
	}

	protected int evaluateReAuthorization(Throwable authorizationException, int reAuthorizationRetries) throws Throwable {

		Logger log = logger();

		if (reAuthorizationRetries == 0) {
			if (log.isDebugEnabled()) {
				if (authorizationContext.getMaxRetries() == 0) {
					log.debug("Unauthorized request [ " + authorizationException.getMessage()
							+ " ]. Skipping retry due to the configured context's max retries [ 0 ]");
				} else {
					log.debug("Unauthorized request [ " + authorizationException.getMessage() + " ]. Reached max retries [ "
							+ authorizationContext.getMaxRetries() + " ]");
				}
			}
			throw authorizationException;
		}

		reAuthorizationRetries--;

		if (log.isDebugEnabled()) {
			log.debug("Unauthorized request [ " + authorizationException.getMessage() + " ]. Retrying. Remaining retries: [ " + reAuthorizationRetries
					+ " ]");
		}

		authorizationContext.onAuthorizationFailure(authorizationException);

		if (stopProcessing) {
			if (log.isDebugEnabled()) {
				log.debug("Skipping retry of unauthorized request, [ " + toString() + " ] was closed.");
			}
			throw authorizationException;
		}

		return reAuthorizationRetries;

	}

	protected Throwable decodeFailure(Failure failure) {
		return failureCodec.decode(failure);
	}

	protected static class MethodInfo {
		public List<String> parameterTypeSignatures;
		public List<GenericModelType> parameterTypes;

		public MethodInfo(List<String> parameterTypeSignatures, List<GenericModelType> parameterTypes) {
			super();
			this.parameterTypeSignatures = parameterTypeSignatures;
			this.parameterTypes = parameterTypes;
		}
	}

	protected ServiceResult unmarshallRpcResponse(InputStream in, Marshaller marshaller, List<TransientSource> resources) throws MarshallException {

		ServiceResult response = (ServiceResult) marshaller.unmarshall(in, GmDeserializationOptions.defaultOptions.derive()
				.setRequiredTypesReceiver(requiredTypesReceiver).set(EntityVisitorOption.class, entity -> {
					if (entity instanceof TransientSource) {
						resources.add((TransientSource) entity);
					}
				}).build());

		return response;
	}

	protected class RpcEvalContext<T> extends AbstractEvalContext<T> {

		private final ServiceRequest request;
		private Consumer<T> responseConsumer;

		public RpcEvalContext(ServiceRequest request) {
			super();
			this.request = request;
		}

		@Override
		public T get() throws EvalException {
			GmRpcClientRequestContext requestContext = new GmRpcClientRequestContext(request, request.type().getTypeSignature(), logger(), false);
			requestContext.setReasoned(true);

			T result = get(requestContext, false).get();

			return result;
		}

		@Override
		public void get(AsyncCallback<? super T> callback) {
			getReasoned(AsyncCallback.of((Maybe<T> m) -> {
				if (m.isSatisfied())
					callback.onSuccess(m.get());
				else
					callback.onFailure(new ReasonException(m.whyUnsatisfied()));
			}, callback::onFailure));
		}

		@Override
		public Maybe<T> getReasoned() {
			GmRpcClientRequestContext requestContext = new GmRpcClientRequestContext(request, request.type().getTypeSignature(), logger(), false);
			requestContext.setReasoned(true);

			Maybe<T> result = get(requestContext, false);

			return result;
		}

		@Override
		public void getReasoned(AsyncCallback<? super Maybe<T>> callback) {
			String context = request.entityType().getShortName();
			ThreadRenamer threadRenamer = threadRenamer();

			GmRpcClientRequestContext requestContext = new GmRpcClientRequestContext(request, request.type().getTypeSignature(), logger(), true);
			requestContext.setReasoned(true);

			// In asynchronous requests, the metadata must be enriched in the callers Thread
			enrichWithMetaData(request);

			Runnable runnable = () -> {
				threadRenamer.push(() -> context);
				try {
					Maybe<T> result = get(requestContext, true);
					callback.onSuccess(result);
				} catch (Throwable e) {
					callback.onFailure(e);
				} finally {
					threadRenamer.pop();
				}
			};

			if (threadContextScoping != null) {
				runnable = threadContextScoping.bindContext(runnable);
			}

			executorService.submit(runnable);
		}

		@Override
		public <A extends TypeSafeAttribute<? super V>, V> void setAttribute(Class<A> attribute, V value) {
			if (attribute == ResponseConsumerAspect.class) {
				if (value == null) {
					responseConsumer = null;
				} else {
					responseConsumer = (Consumer<T>) value;
				}

			}
			super.setAttribute(attribute, value);
		}

		protected Maybe<T> get(GmRpcClientRequestContext requestContext, boolean async) throws EvalException {
			StopWatch stopWatch = new StopWatch();
			stopWatch.intermediate(Thread.currentThread().getName());

			Logger log = logger();
			try {

				EagerResultHolder<T> resultHolder = null;
				if (responseConsumer != null) {
					resultHolder = new EagerResultHolder<>();
					requestContext.setResponseConsumer(responseConsumer.andThen(resultHolder));
				}

				ServiceRequestSummaryLogger summaryLogger = null;
				String summaryStep = null;
				ServiceRequestContext context = !async && requestContextSupplier != null ? requestContextSupplier.get() : null;

				if (context != null && (summaryLogger = context.summaryLogger()).isEnabled()) {
					summaryStep = requestContext.getServiceRequest().entityType().getShortName() + " remote evaluation";
				}

				Maybe<T> result;

				if (summaryStep != null) {
					summaryLogger.startTimer(summaryStep);
				}
				try {
					result = invoke(requestContext);
				} finally {
					if (summaryStep != null) {
						summaryLogger.stopTimer(summaryStep);
					}
				}

				if (resultHolder != null) {

					if (resultHolder.consumed()) {

						// User-given consumer was already notified. We return what was consumed (can be null)
						T eagerResult = resultHolder.get();

						if (log.isTraceEnabled()) {
							log.trace("Returning eagerly notified result " + eagerResult + " instead of returned " + result);
						}

						result = Maybe.complete(eagerResult);

					} else {

						// User-given consumer must be notified about the response if it wasn't already
						if (result.isSatisfied())
							responseConsumer.accept(result.get());
					}

				}

				// A value is always returned: This will be the eager result if a result was eagerly notified to the
				// ServiceRequestContext.

				return result;

			} catch (RuntimeException | Error e) {
				throw e;
			} catch (Throwable e) {
				throw new EvalException("Failed to evaluate " + request.entityType().getTypeSignature() + " instance "
						+ (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
			} finally {
				log.trace(() -> "RpcEvalContext.get: " + stopWatch);
			}
		}

	}

	private static class EagerResultHolder<T> implements Consumer<T>, Supplier<T> {

		T result;
		boolean consumed;

		@Override
		public void accept(T t) {
			result = t;
			consumed = true;
		}

		@Override
		public T get() {
			return result;
		}

		public boolean consumed() {
			return consumed;
		}

	}

	@Configurable
	public void setRequiredTypesReceiver(Consumer<Set<String>> requiredTypesReceiver) {
		this.requiredTypesReceiver = requiredTypesReceiver;
	}

	@Configurable
	public void setThreadContextScoping(ThreadContextScoping threadContextScoping) {
		this.threadContextScoping = threadContextScoping;
	}

	@Configurable
	public void setExecutorService(ExecutorService executorService) {
		if (executorService != null) {
			this.executorService = executorService;
		}
	}

	@Configurable
	public void setClientInstanceId(InstanceId clientInstanceId) {
		this.clientInstanceId = clientInstanceId;
	}

	protected ThreadRenamer threadRenamer() {
		if (threadRenamer == null) {
			boolean enabled = Boolean.valueOf(TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_THREAD_RENAMING, "true"));
			threadRenamer = new ThreadRenamer(enabled);
		}
		return threadRenamer;
	}

}
