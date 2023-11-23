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
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.core.expert.api.MutableDenotationMap;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMap;
import com.braintribe.model.processing.service.api.InterceptingServiceProcessorBuilder;
import com.braintribe.model.processing.service.api.ServiceAroundProcessor;
import com.braintribe.model.processing.service.api.ServiceInterceptorProcessor;
import com.braintribe.model.processing.service.api.ServicePostProcessor;
import com.braintribe.model.processing.service.api.ServicePreProcessor;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.ServiceRequest;

public class ConfigurableDispatchingServiceProcessor implements ServiceProcessor<ServiceRequest, Object> {

	private static final ServiceProcessor<ServiceRequest, Object> DEFAULT_PROCESSOR = (c, r) -> { 
		throw new UnsupportedOperationException("No service processor mapped for request: " + r); 
	}; 
	
	private final MutableDenotationMap<ServiceRequest, ServiceProcessor<? extends ServiceRequest, ?>> processorMap;
	
	private final List<InterceptorEntry> interceptors = new ArrayList<>();

	private static class InterceptorEntry {
		String identification;
		Predicate<ServiceRequest> filter;
		ServiceInterceptorProcessor interceptor;
		
		public InterceptorEntry(String identifier, Predicate<ServiceRequest> filter, ServiceInterceptorProcessor interceptor) {
			super();
			this.identification = identifier;
			this.filter = filter;
			this.interceptor = interceptor;
		}
	}
	
	public ConfigurableDispatchingServiceProcessor() {
		this(new PolymorphicDenotationMap<>());
	}

	public ConfigurableDispatchingServiceProcessor(
			MutableDenotationMap<ServiceRequest, ServiceProcessor<? extends ServiceRequest, ?>> processorMap) {
		this.processorMap = processorMap;
	}
	
	public <R extends ServiceRequest> void register(EntityType<R> requestType, ServiceProcessor<? super R, ?> serviceProcessor) {
		processorMap.put(requestType, serviceProcessor);  
	}
	
	public InterceptorRegistration registerInterceptor(String identification) {
		return new InterceptorRegistration() {
			
			private String insertIdentification;
			private boolean before;
			
			@Override
			public void register(ServiceInterceptorProcessor interceptor) {
				registerWithPredicate(r -> true, interceptor);
			}
			
			@Override
			public <R extends ServiceRequest> void registerForType(EntityType<R> requestType, ServiceInterceptorProcessor interceptor) {
				registerWithPredicate(r -> requestType.isInstance(r), interceptor);
			}
			
			@Override
			public void registerWithPredicate(Predicate<ServiceRequest> predicate, ServiceInterceptorProcessor interceptor) {
				InterceptorEntry interceptorEntry = new InterceptorEntry(identification, predicate, interceptor);
				
				if (insertIdentification != null) {
					requireInterceptorIterator(insertIdentification, before).add(interceptorEntry);
				}
				else {
					interceptors.add(interceptorEntry);
				}
			}
			
			@Override
			public InterceptorRegistration before(String identification) {
				this.insertIdentification = identification;
				this.before = true;
				return this;
			}
			
			@Override
			public InterceptorRegistration after(String identification) {
				this.insertIdentification = identification;
				this.before = false;
				return this;
			}
		};
	}
	
	private ListIterator<InterceptorEntry> find(String identification, boolean before) {
		ListIterator<InterceptorEntry> it = interceptors.listIterator();
		while (it.hasNext()) {
			InterceptorEntry entry = it.next();
			if (entry.identification.equals(identification)) {
				if (before)
					it.previous();
				break;
			}
		}
		
		return it;
	}
	
	private ListIterator<InterceptorEntry> requireInterceptorIterator(String identification, boolean before) {
		ListIterator<InterceptorEntry> iterator = find(identification, before);
		
		if (!iterator.hasNext())
			throw new NoSuchElementException("No processor found with identification: '" + identification + "'");
		
		return iterator;
	}
	
	public void removeInterceptor(String identification) {
		requireInterceptorIterator(identification, true).remove();
	}
	
	private ServiceProcessor<?, ?> getProcessor(ServiceRequest request, ServiceProcessor<?, ?> defaultProcessor) {
		ServiceProcessor<ServiceRequest, Object> processor = processorMap.find(request);
		
		return processor != null? processor: defaultProcessor;
	}
	
	private ServiceProcessor<ServiceRequest, Object> getProcessor(ServiceRequest request) {
		ServiceProcessor<ServiceRequest, Object> processor = processorMap.find(request);
		
		if (processor == null)
			throw new UnsupportedOperationException("No processor registered for " + request.entityType().getTypeSignature());
		
		return processor;
	}
	
	@Override
	public Object process(ServiceRequestContext requestContext, ServiceRequest request) {
		final ServiceProcessor<ServiceRequest, Object> processor;
		
		if (interceptors == null || interceptors.isEmpty()) {
			processor = getProcessor(request);
		}
		else {
			processor = getInterceptingProcessor(request);
		}
			
		return processor.process(requestContext, request);
	}


	private ServiceProcessor<ServiceRequest, Object> getInterceptingProcessor(ServiceRequest request) {
		ServiceProcessor<?, ?> processor = getProcessor(request, DEFAULT_PROCESSOR);
		
		InterceptingServiceProcessorBuilder builder = ServiceProcessingChain.create(processor); //
		
		boolean hasAroundProcessors = false;
		
		for (InterceptorEntry entry: interceptors) {
			if (entry.filter.test(request)) {
				ServiceInterceptorProcessor interceptor = entry.interceptor;
				switch (interceptor.getKind()) {
					case pre: 
						builder.preProcessWith((ServicePreProcessor<?>) interceptor);
						break;
					case around:
						hasAroundProcessors = true;
						builder.aroundProcessWith((ServiceAroundProcessor<?, ?>) interceptor);
						break;
					case post: 
						builder.postProcessWith((ServicePostProcessor<?>) interceptor);
						break;
					default:
						throw new UnsupportedOperationException("Unsupported InterceptorKind: " + interceptor.getKind());
				}
			}
		}
		
		if (!hasAroundProcessors && processor == DEFAULT_PROCESSOR)
			throw new UnsupportedOperationException("No service processor mapped for request: " + request);
		
		return builder.build();
	}
}
