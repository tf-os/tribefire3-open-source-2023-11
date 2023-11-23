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

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.UnsupportedOperation;
import com.braintribe.model.accessapi.AccessRequest;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMap;

public abstract class AbstractDispatchingAccessRequestProcessor<P extends AccessRequest, R> implements ReasonedAccessRequestProcessor<P, R> {

	protected final DispatchMap dispatchMap = new DispatchMap();

	/** Optional identification of this dispatcher for better error messages. */
	protected String dispatcherId;

	protected AbstractDispatchingAccessRequestProcessor() {
		configureDispatching(dispatchMap);
	}

	protected abstract void configureDispatching(DispatchConfiguration dispatching);

	@Override
	public Maybe<? extends R> processReasoned(AccessRequestContext<P> context) {
		P request = context.getOriginalRequest();

		AccessRequestProcessor<P, R> processor = dispatchMap.get(request);
		if (processor == null)
			return Reasons.build(UnsupportedOperation.T) //
					.text("No dispatching configured for access request " + request.entityType().getTypeSignature() + "." + optionalDispatchId()) //
					.toMaybe();

		return Maybe.complete(processor.process(context));
	}

	private String optionalDispatchId() {
		return dispatcherId == null ? "" : " Dispatcher: " + dispatcherId;
	}

	protected static class DispatchMap extends PolymorphicDenotationMap<AccessRequest, AccessRequestProcessor<?, ?>>
			implements DispatchConfiguration {
		@Override
		public <T extends AccessRequest> void register(EntityType<T> denotationType, AccessRequestProcessor<T, ?> processor) {
			put(denotationType, processor);
		}
	}

}
