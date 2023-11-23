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
package com.braintribe.model.access;

import com.braintribe.model.accessapi.CustomPersistenceRequest;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.core.expert.api.MutableDenotationMap;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMap;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;

/**
 * We will get rid of this once we get rid of {@link AbstractAccess} and only use {@link AccessBase}.
 * 
 * @author peter.gazdik
 */
public abstract class AbstractCustomPersistenceRequestProcessingAccess implements IncrementalAccess {

	private final MutableDenotationMap<CustomPersistenceRequest, ServiceProcessor<?, ?>> customProcessors = new PolymorphicDenotationMap<>(true);

	public <T extends CustomPersistenceRequest> void registerCustomPersistenceRequestProcessor(EntityType<T> entityType,
			ServiceProcessor<? super T, ?> processor) {

		customProcessors.put(entityType, processor);
	}

	@Override
	public Object processCustomRequest(ServiceRequestContext context, CustomPersistenceRequest request) {
		ServiceProcessor<CustomPersistenceRequest, ?> processor = customProcessors.find(request);
		if (processor == null)
			throw new UnsupportedOperationException("Method 'processCustomRequest' is not supported for implementation type: "
					+ getClass().getSimpleName() + ". AccessId: " + getAccessId());

		return processor.process(context, request);
	}

}
