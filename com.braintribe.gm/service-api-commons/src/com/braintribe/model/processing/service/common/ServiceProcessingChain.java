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
package com.braintribe.model.processing.service.common;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.UnsatisfiedMaybeTunneling;
import com.braintribe.model.processing.service.api.InterceptingServiceProcessorBuilder;
import com.braintribe.model.processing.service.api.ProceedContext;
import com.braintribe.model.processing.service.api.ProceedContextBuilder;
import com.braintribe.model.processing.service.api.ServiceAroundProcessor;
import com.braintribe.model.processing.service.api.ServiceInterceptionChainBuilder;
import com.braintribe.model.processing.service.api.ServiceInterceptorProcessor;
import com.braintribe.model.processing.service.api.ServicePostProcessor;
import com.braintribe.model.processing.service.api.ServicePreProcessor;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.OverridingPostProcessResponse;
import com.braintribe.utils.collection.impl.AttributeContexts;

/**
 * TODO: proceeding must check the type invariance for the ServiceRequest in order to keep the selected ServiceProcessor
 * a valid match
 * 
 * @author Dirk Scheffler
 *
 */
public class ServiceProcessingChain implements ServiceProcessor<ServiceRequest, Object>, ProceedContext {
	private static final ServiceProcessor<ServiceRequest, Object> DEFAULT_PROCESSOR = (c, r) -> {
		throw new UnsupportedOperationException("No service processor mapped for request: " + r);
	};

	private List<ServicePreProcessor<ServiceRequest>> preProcessors;
	private List<ServicePostProcessor<Object>> postProcessors;
	private List<ServiceAroundProcessor<ServiceRequest, ?>> aroundProcessors;
	private ServiceRequestContext serviceRequestContext;

	private ServiceProcessor<ServiceRequest, ?> processor = DEFAULT_PROCESSOR;

	private static <T extends ServiceInterceptorProcessor, E extends T> List<E> appendInterceptor(List<E> list, T element) {
		if (list == null)
			list = new ArrayList<>();

		list.add((E) element);
		return list;
	}

	public static InterceptingServiceProcessorBuilder create() {
		return create(DEFAULT_PROCESSOR);
	}

	public static InterceptingServiceProcessorBuilder create(ServiceProcessor<?, ?> processor) {
		return new InterceptingServiceProcessorBuilderImpl(processor);
	}

	private static abstract class AbstractServiceInterceptionBuilder implements ServiceInterceptionChainBuilder {
		final protected ServiceProcessingChain chain;

		private AbstractServiceInterceptionBuilder() {
			chain = new ServiceProcessingChain();
		}

		@Override
		public void preProcessWith(ServicePreProcessor<?> preProcessor) {
			chain.preProcessors = appendInterceptor(chain.preProcessors, preProcessor);
		}

		@Override
		public void postProcessWith(ServicePostProcessor<?> postProcessor) {
			chain.postProcessors = appendInterceptor(chain.postProcessors, postProcessor);
		}

		@Override
		public void aroundProcessWith(ServiceAroundProcessor<?, ?> aroundProcessor) {
			chain.aroundProcessors = appendInterceptor(chain.aroundProcessors, aroundProcessor);
		}
	}

	private static class ProceedContextBuilderImpl extends AbstractServiceInterceptionBuilder implements ProceedContextBuilder {

		public ProceedContextBuilderImpl(ServiceRequestContext requestContext, ServiceProcessor<?, ?> processor) {
			chain.processor = (ServiceProcessor<ServiceRequest, ?>) processor;
			chain.serviceRequestContext = requestContext;
		}

		@Override
		public ProceedContext build() {
			return chain;
		}
	}

	private static class InterceptingServiceProcessorBuilderImpl extends AbstractServiceInterceptionBuilder
			implements InterceptingServiceProcessorBuilder {

		public InterceptingServiceProcessorBuilderImpl(ServiceProcessor<?, ?> processor) {
			chain.processor = (ServiceProcessor<ServiceRequest, ?>) processor;
		}

		public InterceptingServiceProcessorBuilderImpl() {
		}

		@Override
		public ServiceProcessor<ServiceRequest, Object> build() {
			return chain;
		}
	}

	@Override
	public Object process(ServiceRequestContext requestContext, ServiceRequest request) {
		request = preProcess(requestContext, request);
		Object response = aroundProcess(requestContext, request);
		response = postProcess(requestContext, response);
		return response;
	}

	@Override
	public <T> T proceed(ServiceRequest serviceRequest) {
		return (T) process(serviceRequestContext, serviceRequest);
	}

	@Override
	public <T> T proceed(ServiceRequestContext context, ServiceRequest request) {
		if (context != serviceRequestContext)
			AttributeContexts.push(context);

		try {
			return (T) process(context, request);
		} finally {
			if (context != serviceRequestContext)
				AttributeContexts.pop();
		}
	}

	@Override
	public <T> Maybe<T> proceedReasoned(ServiceRequest request) {
		try {
			return proceed(request);
		} catch (UnsatisfiedMaybeTunneling m) {
			return m.getMaybe();
		}
	}

	@Override
	public <T> Maybe<T> proceedReasoned(ServiceRequestContext context, ServiceRequest request) {
		try {
			return proceed(context, request);
		} catch (UnsatisfiedMaybeTunneling m) {
			return m.getMaybe();
		}
	}

	@Override
	public ProceedContextBuilder newInterceptionChain(ServiceProcessor<?, ?> processor) {
		return new ProceedContextBuilderImpl(serviceRequestContext, processor);
	}

	@Override
	public ProceedContextBuilder extend() {
		return new ProceedContextBuilderImpl(serviceRequestContext, this);
	}

	private ServiceRequest preProcess(ServiceRequestContext requestContext, ServiceRequest request) {
		if (preProcessors != null && !preProcessors.isEmpty()) {
			for (ServicePreProcessor<ServiceRequest> preProcessor : preProcessors) {
				request = preProcessor.process(requestContext, request);
			}
		}
		return request;
	}

	private Object aroundProcess(ServiceRequestContext requestContext, ServiceRequest request) {
		if (aroundProcessors != null && !aroundProcessors.isEmpty()) {
			Object response = new ImmutableProceedContext(requestContext, 0).proceed(request);
			return response;
		} else {
			return processor.process(requestContext, request);
		}
	}

	@SuppressWarnings("deprecation")
	private Object postProcess(ServiceRequestContext requestContext, Object response) {
		if (postProcessors != null && !postProcessors.isEmpty()) {
			for (ServicePostProcessor<Object> postProcessor : postProcessors) {
				Object postProcessorResponse = postProcessor.process(requestContext, response);

				if (response != postProcessorResponse) {
					if (postProcessorResponse instanceof OverridingPostProcessResponse) {
						response = ((OverridingPostProcessResponse) postProcessorResponse).getResponse();
					}
					response = postProcessorResponse;
				}
			}
		}

		return response;
	}

	protected ProceedContext newProceedContext(ServiceRequestContext requestContext) {
		return new ImmutableProceedContext(requestContext, 0);
	}

	private class ImmutableProceedContext implements ProceedContext, ServiceProcessor<ServiceRequest, Object> {
		private int index;
		private ServiceRequestContext requestContext;

		public ImmutableProceedContext(ServiceRequestContext requestContext, int index) {
			super();
			this.requestContext = requestContext;
			this.index = index;
		}

		@Override
		public Object process(ServiceRequestContext requestContext, ServiceRequest request) {
			if (index < aroundProcessors.size()) {
				return aroundProcessors.get(index).process(requestContext, request, new ImmutableProceedContext(requestContext, index + 1));
			} else {
				return processor.process(requestContext, request);
			}
		}

		@Override
		public <T> T proceed(ServiceRequest serviceRequest) {
			if (index < aroundProcessors.size()) {
				return (T) aroundProcessors.get(index).process(requestContext, serviceRequest,
						new ImmutableProceedContext(requestContext, index + 1));
			} else {
				return (T) processor.process(requestContext, serviceRequest);
			}
		}

		@Override
		public <T> T proceed(ServiceRequestContext context, ServiceRequest serviceRequest) {
			if (context != requestContext)
				AttributeContexts.push(context);

			try {
				if (index < aroundProcessors.size()) {
					return (T) aroundProcessors.get(index).process(context, serviceRequest, new ImmutableProceedContext(context, index + 1));
				} else {
					return (T) processor.process(context, serviceRequest);
				}
			} finally {
				if (context != requestContext)
					AttributeContexts.pop();
			}
		}

		@Override
		public <T> Maybe<T> proceedReasoned(ServiceRequest request) {
			try {
				return Maybe.complete(proceed(request));
			} catch (UnsatisfiedMaybeTunneling m) {
				return m.getMaybe();
			}
		}

		@Override
		public <T> Maybe<T> proceedReasoned(ServiceRequestContext context, ServiceRequest request) {
			try {
				return Maybe.complete(proceed(context, request));
			} catch (UnsatisfiedMaybeTunneling m) {
				return m.getMaybe();
			}
		}

		@Override
		public ProceedContextBuilder extend() {
			return new ProceedContextBuilderImpl(requestContext, this);
		}

		@Override
		public ProceedContextBuilder newInterceptionChain(ServiceProcessor<?, ?> processor) {
			return new ProceedContextBuilderImpl(requestContext, processor);
		}

	}
}