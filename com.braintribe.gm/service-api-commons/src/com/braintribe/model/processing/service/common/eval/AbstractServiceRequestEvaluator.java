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
package com.braintribe.model.processing.service.common.eval;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.common.attribute.TypeSafeAttribute;
import com.braintribe.common.attribute.TypeSafeAttributeEntry;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.ReasonException;
import com.braintribe.gm.model.reason.UnsatisfiedMaybeTunneling;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.AbstractEvalContext;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.EvalException;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.service.api.EvaluatorAspect;
import com.braintribe.model.processing.service.api.ParentAttributeContextAspect;
import com.braintribe.model.processing.service.api.ResponseConsumerAspect;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.api.ServiceRequestContextBuilder;
import com.braintribe.model.processing.service.api.ServiceRequestSummaryLogger;
import com.braintribe.model.processing.service.api.aspect.EagerResponseConsumerAspect;
import com.braintribe.model.processing.service.api.aspect.RequestEvaluationIdAspect;
import com.braintribe.model.processing.service.commons.ServiceRequestContexts;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.utils.collection.impl.AttributeContexts;

/**
 * <p>
 * Standard local abstract {@link Evaluator} of {@link ServiceRequest}(s).
 * 
 * @author Dirk Scheffler
 */
public abstract class AbstractServiceRequestEvaluator implements Evaluator<ServiceRequest> {

	private static final Logger log = Logger.getLogger(AbstractServiceRequestEvaluator.class);

	protected ServiceProcessor<ServiceRequest, Object> serviceProcessor;
	protected ExecutorService executorService;
	protected Evaluator<ServiceRequest> contextEvaluator = this;
	protected Function<Reason, RuntimeException> reasonExceptionFactory = ReasonException::new;
	
	@Configurable
	public void setReasonExceptionFactory(Function<Reason, RuntimeException> reasonToExceptionTransformator) {
		this.reasonExceptionFactory = reasonToExceptionTransformator;
	}
	
	public void setContextEvaluator(Evaluator<ServiceRequest> contextEvaluator) {
		this.contextEvaluator = contextEvaluator;
	}

	public void setServiceProcessor(ServiceProcessor<ServiceRequest, Object> serviceProcessor) {
		this.serviceProcessor = serviceProcessor;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public <T> EvalContext<T> eval(ServiceRequest serviceRequest) {
		Objects.requireNonNull(serviceRequest, "serviceRequest must not be null");
		return new ServiceProcessorEvalContext<T>(serviceRequest);
	}

	private static Collection<Class<? extends TypeSafeAttribute<?>>> contextAttributeVetos = Arrays.asList(EagerResponseConsumerAspect.class, ParentAttributeContextAspect.class);
	
	private class ServiceProcessorEvalContext<T> extends AbstractEvalContext<T> {

		private final ServiceRequest serviceRequest;
		private final EagerResultHolder responseConsumer = new EagerResultHolder();
		private AttributeContext parentContext;
		private Evaluator<ServiceRequest> evaluator;

		public ServiceProcessorEvalContext(ServiceRequest serviceRequest) {
			this.serviceRequest = serviceRequest;
		}

		@Override
		public T get() throws EvalException {
			try {
				T result = processSync();
				return result;
			}
			catch (UnsatisfiedMaybeTunneling m) {
				throw reasonExceptionFactory.apply(m.getMaybe().whyUnsatisfied());
			}
		}
		
		@Override
		public void get(AsyncCallback<? super T> targetCallback) {
			AsyncCallback<? super T> nullSafeCallback = ensureCallback(targetCallback);
			
			processAsync(AsyncCallback.of(
				nullSafeCallback::onSuccess,
				t -> {
					if (t instanceof UnsatisfiedMaybeTunneling) {
						nullSafeCallback.onFailure(reasonExceptionFactory.apply(((UnsatisfiedMaybeTunneling)t).whyUnsatisfied()));
					}
					else
						nullSafeCallback.onFailure(t);
				})
			);
		}
		
		@Override
		public Maybe<T> getReasoned() {
			try {
				return Maybe.complete(processSync());
			}
			catch (UnsatisfiedMaybeTunneling m) {
				return m.getMaybe();
			}
		}
		
		@Override
		public void getReasoned(AsyncCallback<? super Maybe<T>> targetCallback) {
			AsyncCallback<? super Maybe<T>> nullSafeCallback = ensureCallback(targetCallback);
			
			processAsync(AsyncCallback.of(
				(T v) -> nullSafeCallback.onSuccess(Maybe.complete(v)),
				t -> {
					if (t instanceof UnsatisfiedMaybeTunneling) {
						nullSafeCallback.onSuccess(((UnsatisfiedMaybeTunneling)t).getMaybe());
					}
					else
						nullSafeCallback.onFailure(t);
				})
			);
		}

		@Override
		public <A extends TypeSafeAttribute<? super V>, V> void setAttribute(Class<A> attribute, V value) {
			if (attribute == ResponseConsumerAspect.class) {
				responseConsumer.listener = (Consumer<Object>) value;
				value = (V)responseConsumer;
			}
			else if (attribute == ParentAttributeContextAspect.class) {
				parentContext = (AttributeContext)value;
			}
			else if (attribute == EvaluatorAspect.class) {
				evaluator = (Evaluator<ServiceRequest>)value;
			}

			super.setAttribute(attribute, value);
		}
		
		protected T processAsync(final AsyncCallback<? super T> targetCallback) {

			ServiceRequestContext invocationContext = prepareContext();
			
			processAsync(targetCallback, invocationContext);
			return null;
		}
		
		protected T processSync() {
			
			ServiceRequestContext invocationContext = prepareContext();
			
			AttributeContexts.push(invocationContext);
			
			try {
				return processNormalizedWithSummary(invocationContext);
			} finally {
				AttributeContexts.pop();
			}
		}
		
		private AttributeContext getParentContext() {
			return parentContext != null? parentContext: AttributeContexts.peek();
		}
		
		private boolean filterAttribute(TypeSafeAttributeEntry entry) {
			Class<? extends TypeSafeAttribute<?>> attribute = entry.attribute();
			
			return !contextAttributeVetos.contains(attribute);
		}

		private ServiceRequestContext prepareContext() {
			Evaluator<ServiceRequest> effectiveEvaluator = evaluator != null? evaluator: contextEvaluator;
			final ServiceRequestContextBuilder invocationContextBuilder = ServiceRequestContexts.serviceRequestContext(getParentContext(), effectiveEvaluator);
			
			//@formatter:off
			streamAttributes()
				.filter(this::filterAttribute)
				.forEach(e -> invocationContextBuilder.setAttribute(e.attribute(), e.value()));
			//@formatter:on
			
			invocationContextBuilder.set(EagerResponseConsumerAspect.class, responseConsumer);
			invocationContextBuilder.set(RequestEvaluationIdAspect.class, UUID.randomUUID().toString());
			
			return invocationContextBuilder.build();
		}

		private void processAsync(final AsyncCallback<? super T> targetCallback, ServiceRequestContext invocationContext) {
			ServiceRequestSummaryLogger summaryLogger = invocationContext.summaryLogger();
			if (summaryLogger.isEnabled()) {
				String summaryStep = serviceRequest.entityType().getShortName() + " async evaluation" ;
				summaryLogger.startTimer(summaryStep);

				try {
					submitAsyncProcessing(invocationContext, targetCallback);
				}
				finally {
					summaryLogger.stopTimer(summaryStep);
				}
			}
			else {
				submitAsyncProcessing(invocationContext, targetCallback);
			}
		}

		
		private void submitAsyncProcessing(ServiceRequestContext context, final AsyncCallback<? super T> targetCallback) {
			executorService.submit(() -> {
				AttributeContexts.push(context);
				
				try {
					T result = processNormalized(context);
					targetCallback.onSuccess(result);
				} catch (Exception e) {
					targetCallback.onFailure(e);
				} finally {
					AttributeContexts.pop();
				}
			});
			log.trace(() -> "Submitted async " + serviceRequest.entityType().getShortName() + " evaluation via " + executorService);
		}
		
		private T processNormalized(ServiceRequestContext context) {
			responseConsumer.notifyActualResult(serviceProcessor.process(context, serviceRequest));
			return (T)responseConsumer.get();
		}
		
		private T processNormalizedWithSummary(ServiceRequestContext context) {
			ServiceRequestSummaryLogger summaryLogger = context.summaryLogger();
			if (!summaryLogger.isEnabled())
				return processNormalized(context);
			
			String summaryStep = serviceRequest.entityType().getShortName() + " evaluation";

			summaryLogger.startTimer(summaryStep);
			try {
				return processNormalized(context);
			} finally {
				summaryLogger.stopTimer(summaryStep);
			}
		}
		
		private <V> AsyncCallback<V> ensureCallback(AsyncCallback<V> callback) {
			if (callback != null)
				return callback;
			
			return AsyncCallback.of(
				v -> log.debug("Received result [" + v + "] for async service request without callback " + serviceRequest),
				t -> log.error("Error while exuting async service request without callback [" + serviceRequest + "]", t)
			);
		}
	}

	public static class EagerResultHolder implements Consumer<Object>, Supplier<Object> {

		Object result;
		boolean consumed;
		Consumer<Object> listener;
		
		public EagerResultHolder() {
			super();
		}

		public void notifyActualResult(Object retVal) {
			if (consumed)
				return;
			
			synchronized(this) {
				if (consumed)
					return;
				
				consumed = true;
				result = retVal;
			}
		}
		
		@Override
		public void accept(Object retVal) {
			if (consumed)
				return;
			
			synchronized(this) {
				if (consumed)
					return;
				
				consumed = true;
				result = retVal;
				if (listener != null) 
					listener.accept(retVal);
			}
		}

		@Override
		public Object get() {
			return result;
		}

		public boolean consumed() {
			return consumed;
		}

	}

}
