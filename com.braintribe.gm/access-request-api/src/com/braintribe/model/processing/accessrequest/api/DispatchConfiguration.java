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
package com.braintribe.model.processing.accessrequest.api;

import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.model.accessapi.AccessRequest;
import com.braintribe.model.generic.reflection.EntityType;

public interface DispatchConfiguration {
	<T extends AccessRequest> void register(EntityType<T> denotationType, AccessRequestProcessor<T, ?> processor);
	
	default <T extends AccessRequest> void registerReasoned(EntityType<T> denotationType, ReasonedAccessRequestProcessor<T, ?> processor) {
		register(denotationType, processor);
	}
	
	/**
	 * @deprecated use {@link #registerStateful(EntityType, Supplier)} 
	 */
	@Deprecated
	default <T extends AccessRequest> void register(EntityType<T> denotationType, Supplier<AbstractStatefulAccessRequestProcessor<T, ?>> processorSupplier) {
		registerStateful(denotationType, processorSupplier);
	}
	
	default <T extends AccessRequest> void registerStateful(EntityType<T> denotationType, Supplier<AbstractStatefulAccessRequestProcessor<T, ?>> processorSupplier) {
		register(denotationType, c -> {
			AbstractStatefulAccessRequestProcessor<T, ?> processor = processorSupplier.get();
			processor.initContext(c);
			return processor.process();
		});
	}
	
	default <T extends AccessRequest> void registerStatefulWithContext(EntityType<T> denotationType, Function<AccessRequestContext<T>, AbstractStatefulAccessRequestProcessor<T, ?>> processorFunction) {
		register(denotationType, c -> {
			AbstractStatefulAccessRequestProcessor<T, ?> processor = processorFunction.apply(c);
			processor.initContext(c);
			return processor.process();
		});
	}
	
}
