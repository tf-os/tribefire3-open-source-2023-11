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

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.accessapi.AccessRequest;
import com.braintribe.model.generic.reflection.EntityType;

/**
 * @author peter.gazdik
 */
public class ConfigurableDispatchingAccessRequestProcessor<P extends AccessRequest, R> extends AbstractDispatchingAccessRequestProcessor<P, R> {

	/**
	 * @param dispatcherId
	 *            identification of this dispatcher for (better) error messages.
	 */
	public ConfigurableDispatchingAccessRequestProcessor(String dispatcherId) {
		this.dispatcherId = dispatcherId;
	}

	@Override
	protected void configureDispatching(DispatchConfiguration dispatching) {
		// NO OP
	}

	@Override
	public Maybe<? extends R> processReasoned(AccessRequestContext<P> context) {
		return super.processReasoned(context);
	}
	
	public <T extends AccessRequest> void registerReasoned(EntityType<T> denotationType, ReasonedAccessRequestProcessor<T, ?> processor) {
		dispatchMap.registerReasoned(denotationType, processor);
	}

	public <T extends AccessRequest> void registerStateful(EntityType<T> denotationType,
			Supplier<AbstractStatefulAccessRequestProcessor<T, ?>> processorSupplier) {
		dispatchMap.registerStateful(denotationType, processorSupplier);
	}

	public <T extends AccessRequest> void registerStatefulWithContext(EntityType<T> denotationType,
			Function<AccessRequestContext<T>, AbstractStatefulAccessRequestProcessor<T, ?>> processorFunction) {
		dispatchMap.registerStatefulWithContext(denotationType, processorFunction);
	}

	public <T extends AccessRequest> void register(EntityType<T> denotationType, AccessRequestProcessor<T, ?> processor) {
		dispatchMap.register(denotationType, processor);
	}

}
