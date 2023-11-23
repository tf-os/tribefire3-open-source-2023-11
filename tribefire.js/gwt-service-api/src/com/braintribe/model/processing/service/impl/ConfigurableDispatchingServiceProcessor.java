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
package com.braintribe.model.processing.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMap;
import com.braintribe.model.processing.service.api.ProcessorRegistry;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.DispatchableRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.provider.Holder;

@SuppressWarnings("unusable-by-js")
public class ConfigurableDispatchingServiceProcessor implements ServiceProcessor<ServiceRequest, Object>, ProcessorRegistry {
	
	@SuppressWarnings("unusable-by-js")
	private static final ServiceProcessor<ServiceRequest, Object> DEFAULT_PROCESSOR = new ServiceProcessor<ServiceRequest, Object>() {
		@Override
		@SuppressWarnings("unusable-by-js")
		public Object process(ServiceRequestContext c, ServiceRequest r) { 
			throw new UnsupportedOperationException("No service processor mapped for request type: " + r.entityType()); 
		}
	};
	
	@SuppressWarnings("unusable-by-js")
	private static final ServiceProcessor<DispatchableRequest, Object> DEFAULT_DISPATCHABLE_PROCESSOR = new ServiceProcessorImplementation(); 
	
	@SuppressWarnings("unusable-by-js")
	private static final class ServiceProcessorImplementation implements ServiceProcessor<DispatchableRequest, Object> {
		@Override
		@SuppressWarnings("unusable-by-js")
		public Object process(ServiceRequestContext c, DispatchableRequest r) { 
			throw new UnsupportedOperationException("No service processor mapped for serviceId [" + r.getServiceId() + "] and request type: " + r.entityType()); 
		}
	}

	private static class DispatchEntry {
		EntityType<? extends DispatchableRequest> type;
		ServiceProcessor<?, ?> processor;
		
		public DispatchEntry(EntityType<? extends DispatchableRequest> type, ServiceProcessor<?, ?> processor) {
			super();
			this.type = type;
			this.processor = processor;
		}
	}
	
	private PolymorphicDenotationMap<ServiceRequest, ServiceProcessor<?, ?>> processorByType = new PolymorphicDenotationMap<ServiceRequest, ServiceProcessor<?,?>>(false);
	private Map<String, DispatchEntry> processorByServiceId = new HashMap<>();
	
	@Override
	public <I extends ServiceRequest> void bind(EntityType<I> type, ServiceProcessor<? super I, ?> processor) {
		processorByType.put(type, processor);
	}
	
	@Override
	public <I extends DispatchableRequest> void bind(EntityType<I> type, String serviceId,
			ServiceProcessor<? super I, ?> processor) {
		processorByServiceId.put(serviceId, new DispatchEntry(type, processor));
	}
	
	@Override
	public <I extends DispatchableRequest> void unbind(EntityType<I> type, String serviceId,
			ServiceProcessor<? super I, ?> processor) {
		Holder<RuntimeException> exception = new Holder<>();		
		processorByServiceId.compute(serviceId, (id, e) -> {
			if (e == null) {
				exception.accept(newNoSuchElement(type, serviceId));
				return null;
			}
			if (e.type == type && e.processor == processor)
				return null;
			else {
				exception.accept(newNoSuchElement(type, serviceId));
				return e;
			}
			
		});
		
		RuntimeException e = exception.get();
		
		if (e != null)
			throw e;
	}

	private <I extends DispatchableRequest> NoSuchElementException newNoSuchElement(EntityType<I> type, String serviceId) {
		return new NoSuchElementException("No matching binding found for type [" + type.getTypeSignature() + "] and serviceId [" + serviceId +"]");
	}
	
	@Override
	public Object process(ServiceRequestContext requestContext, ServiceRequest request) {
		ServiceProcessor<ServiceRequest, ?> resolveProcessor = (ServiceProcessor<ServiceRequest, ?>) resolveProcessor(request);
		return resolveProcessor.process(requestContext, request);
	}

	private ServiceProcessor<?, ?> resolveProcessor(ServiceRequest request) {
		if (request.dispatchable()) {
			DispatchableRequest dispatchableRequest = (DispatchableRequest)request;
			String serviceId = dispatchableRequest.getServiceId();
			
			if (serviceId != null) {
				return Optional.ofNullable(processorByServiceId.get(serviceId)) //
					.filter(e -> e.type.isInstance(request))
					.<ServiceProcessor<?,?>>map(e -> e.processor) //
					.orElse(DEFAULT_DISPATCHABLE_PROCESSOR); //
			}
			
		}

		return Optional.<ServiceProcessor<?,?>>ofNullable(processorByType.find(request.entityType()))
				.orElse(DEFAULT_PROCESSOR);
	}
}
