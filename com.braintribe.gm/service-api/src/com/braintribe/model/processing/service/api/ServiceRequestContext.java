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
package com.braintribe.model.processing.service.api;

import java.util.Optional;
import java.util.function.Consumer;

import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.service.api.aspect.DomainIdAspect;
import com.braintribe.model.processing.service.api.aspect.EagerResponseConsumerAspect;
import com.braintribe.model.processing.service.api.aspect.IsAuthorizedAspect;
import com.braintribe.model.processing.service.api.aspect.IsTrustedAspect;
import com.braintribe.model.processing.service.api.aspect.RequestedEndpointAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorAddressAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorIdAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorSessionIdAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorUserNameAspect;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * <p>
 * Provides contextual information about a request.
 * 
 */
public interface ServiceRequestContext extends Evaluator<ServiceRequest>, AttributeContext {
	
	@Override
	ServiceRequestContextBuilder derive();
	
	default Evaluator<ServiceRequest> getEvaluator() { return this; }
	
	<T, A extends ServiceRequestContextAspect<? super T>> T findAspect(Class<A> aspect);
	
	default <T, A extends ServiceRequestContextAspect<? super T>> Optional<T> getAspect(Class<A> aspect) {
		return Optional.ofNullable(findAspect(aspect));
	}
	
	/**
	 * Peeks the given {@link ServiceRequestContextAspect} from the {@code ServiceRequestContext}. If the given aspect
	 * is not found in the context stack, the given default value <code>def</code> is being returned.
	 * 
	 * @param <T>
	 *            The value type.
	 * @param <A>
	 *            The {@link ServiceRequestContextAspect} type.
	 * @param aspect
	 *            The {@link ServiceRequestContextAspect} which is peeked from the context
	 * @param def
	 *            The default value to be returned
	 * @return The peeked value matching the given {@link ServiceRequestContextAspect} or .
	 */
	default <T, A extends ServiceRequestContextAspect<? super T>> T getAspect(Class<A> aspect, T def) {
		T aspectValue = findAspect(aspect);
		return aspectValue == null ? def : aspectValue;
	}

	default String getRequestorAddress() { return findAspect(RequestorAddressAspect.class); }

	default String getRequestorId() { return findAspect(RequestorIdAspect.class); }
	
	
	/**
	 * Returns the id of the service domain which may be used to resolve the model that was used to map the request
	 */
	default String getDomainId() { return findAspect(DomainIdAspect.class); }

	/**
	 * Returns the user session id associated with the request.
	 * <p>
	 * This information will be present when the request has undergone basic authorization (see
	 * {@link #isAuthorized()}).
	 * 
	 * @return The user session id associated with the request.
	 */
	default String getRequestorSessionId()  { return findAspect(RequestorSessionIdAspect.class); }

	/**
	 * Returns the user name associated with the request.
	 * <p>
	 * This information will be present when the request has undergone basic authorization (see
	 * {@link #isAuthorized()}).
	 */
	default String getRequestorUserName() { return findAspect(RequestorUserNameAspect.class); }

	/** @return A representation of the originally requested endpoint. */
	default String getRequestedEndpoint() { return findAspect(RequestedEndpointAspect.class); }

	/** @return Whether the request has undergone basic authorization (user session id validation). */
	default boolean isAuthorized() { return getAspect(IsAuthorizedAspect.class, false); }

	/** @return Whether the request has originated through a trusted communication layer. */
	default boolean isTrusted() { return getAspect(IsTrustedAspect.class, false); }

	/** Notifies given {@code response} to a {@link Consumer} resolved as {@link EagerResponseConsumerAspect}. */
	default void notifyResponse(Object response) {
		Consumer<Object> consumer = findAspect(EagerResponseConsumerAspect.class);
		
		if (consumer != null)
			consumer.accept(response);
	}

	
	/** @return A {@link ServiceRequestSummaryLogger} for the current request. */
	ServiceRequestSummaryLogger summaryLogger();
}
