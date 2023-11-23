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
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.result.MulticastResponse;

/**
 * A {@link GenericProcessingRequest} that wraps a request which is to be multicasted and processed by multiple nodes in a clustered runtime
 * environment.
 * 
 * @see UnicastRequest
 */
public interface MulticastRequest extends AuthorizedRequest, NonInterceptableRequest, GenericProcessingRequest, HasServiceRequest {

	EntityType<MulticastRequest> T = EntityTypes.T(MulticastRequest.class);

	/**
	 * This property allows to create a wildcarded filter to which node/application the request should be addressed.
	 */
	InstanceId getAddressee();
	void setAddressee(InstanceId addressee);

	/**
	 * This property holds the information who was sending the request. It will be automatically filled in by the evaluation framework
	 */
	InstanceId getSender();
	void setSender(InstanceId sender);

	/**
	 * The amount of milliseconds to wait for the expected amount of answers
	 */
	Long getTimeout();
	void setTimeout(Long timeout);

	/**
	 * This flag controls if the multicast is to be evaluated asynchronously which means that there is no waiting for the results.
	 * 
	 * @deprecated wrap your {@link MulticastRequest} in an {@link AsynchronousRequest} or use
	 *             {@link EvalContext#get(com.braintribe.processing.async.api.AsyncCallback)} instead
	 */
	@Deprecated
	boolean getAsynchronous();
	/** @deprecated see {@link #getAsynchronous()} */
	@Deprecated
	void setAsynchronous(boolean value);

	@Override
	EvalContext<? extends MulticastResponse> eval(Evaluator<ServiceRequest> evaluator);

	@Override
	default boolean system() {
		return true;
	}

}
