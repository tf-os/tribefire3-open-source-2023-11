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
package com.braintribe.model.processing.ddra.endpoints.rest.v2.ioc;

import com.braintribe.exception.AuthorizationException;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.QueryEntities;
import com.braintribe.model.accessapi.QueryProperty;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.EvalContextAspect;
import com.braintribe.model.generic.eval.EvalException;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.service.api.DispatchableRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.processing.async.api.AsyncCallback;

import java.util.HashMap;
import java.util.Map;

public class TestRestEvaluator implements Evaluator<ServiceRequest> {

	private static class Context<T> implements EvalContext<T> {

		private final IncrementalAccess access;
		private final ServiceRequest evaluable;

		public Context(IncrementalAccess access, ServiceRequest evaluable) {
			this.access = access;
			this.evaluable = evaluable;
		}

		@Override
		public T get() throws EvalException {

			if (evaluable instanceof QueryEntities) {
				QueryEntities query = (QueryEntities) evaluable;
				return (T) access.queryEntities(query.getQuery());
			}
			if (evaluable instanceof ManipulationRequest) {
				ManipulationRequest manipulation = (ManipulationRequest) evaluable;
				return (T) access.applyManipulation(manipulation);
			}
			if (evaluable instanceof QueryProperty) {
				QueryProperty query = (QueryProperty) evaluable;
				return (T) access.queryProperty(query.getQuery());
			}

			throw new IllegalArgumentException("Unsupported service request type: " + evaluable.getClass().getName());
		}

		@Override
		public void get(AsyncCallback<? super T> callback) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <U, A extends EvalContextAspect<? super U>> EvalContext<T> with(Class<A> aspect, U value) {
			throw new UnsupportedOperationException();
		}

	}

	private final Map<String, IncrementalAccess> accesses = new HashMap<>();

	public void reset(IncrementalAccess access) {
		accesses.put(access.getAccessId(), access);
	}

	@Override
	public <T> EvalContext<T> eval(ServiceRequest evaluable) {
		if (!(evaluable instanceof AuthorizedRequest)) {
			throw new IllegalArgumentException("Expected service request to be AuthorizedRequest");
		}
		if (!(evaluable instanceof DispatchableRequest)) {
			throw new IllegalArgumentException("Expected service request to be DispatchableRequest");
		}

		String sessionId = ((AuthorizedRequest) evaluable).getSessionId();
		String accessId = ((DispatchableRequest) evaluable).getServiceId();

		if (sessionId == null) {
			throw new AuthorizationException("No provided sessionId.");
		}
		if (accessId == null) {
			throw new RuntimeException("No provided serviceId.");
		}

		IncrementalAccess access = accesses.get(accessId);
		if (access == null) {
			throw new RuntimeException("No access found with accessId=" + accessId);
		}

		return new Context<T>(access, evaluable);
	}

}
