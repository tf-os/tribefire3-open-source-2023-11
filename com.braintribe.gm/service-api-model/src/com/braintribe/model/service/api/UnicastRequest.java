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
package com.braintribe.model.service.api;

import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * <p>
 * A {@link GenericProcessingRequest} wraps a request which is to be processed by a specific node in a clustered runtime environment which is the opposite
 * of the normal route which involves a load balancer
 * {@link #setAddressee(InstanceId)}.
 */
public interface UnicastRequest extends AuthorizedRequest, NonInterceptableRequest, GenericProcessingRequest, HasServiceRequest {

	EntityType<UnicastRequest> T = EntityTypes.T(UnicastRequest.class);

	/**
	 * The description of the node that should execute the wrapped request
	 */
	InstanceId getAddressee();
	void setAddressee(InstanceId addressee);

	/**
	 * The time to wait for the request to be exectued synchronously as unicasts are often implemented by using messasge queues.
	 */
	Long getTimeout();
	void setTimeout(Long timeout);

	/**
	 * Determines if the wrapped request will be executed in an asynchronous mode which means that there is no waiting for a response
	 * @deprecated use {@link AsynchronousRequest} or {@link EvalContext#get(com.braintribe.processing.async.api.AsyncCallback)} instead  
	 */
	@Deprecated
	boolean getAsynchronous();
	
	/**
	 * @deprecated use {@link AsynchronousRequest} or {@link EvalContext#get(com.braintribe.processing.async.api.AsyncCallback)} instead  
	 */
	@Deprecated
	void setAsynchronous(boolean value);

	@Override
	default boolean system() {
		return true;
	}

}
