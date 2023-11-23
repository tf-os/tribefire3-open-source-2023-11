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

import java.util.ArrayList;
import java.util.List;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.EvalContextAspect;
import com.braintribe.model.generic.eval.EvalException;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.service.api.CompositeRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.CompositeResponse;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.processing.async.api.AsyncCallback;

public class CompositeRequestEvaluator implements Evaluator<ServiceRequest> {
	private List<AsyncCallback<?>> callbacks = new ArrayList<>();
	private CompositeRequest request;
	
	/**
	 * @deprecated {@link CompositeRequest} always requires authentication.
	 */
	@SuppressWarnings("unused")
	@Deprecated
	public CompositeRequestEvaluator(boolean authorized) {
		this();
	}
	
	public CompositeRequestEvaluator() {
		request = CompositeRequest.T.create();
		request.setParallelize(false);
	}
	
	@Override
	public <T> EvalContext<T> eval(ServiceRequest evaluable) {
		request.getRequests().add(evaluable);
		final int position = callbacks.size();
		callbacks.add(null);

		return new EvalContext<T>() {

			@Override
			public T get() throws EvalException {
				throw new UnsupportedOperationException("not supported in composite building");
			}

			@Override
			public void get(AsyncCallback<? super T> callback) {
				callbacks.set(position, callback);
			}

			@Override
			public <U, A extends EvalContextAspect<? super U>> EvalContext<T> with(Class<A> aspect, U value) {
				return this;
			}
			
		};
	}
	
	protected void processResults(CompositeResponse compositeResponse) {
		int index = 0;
		for (AsyncCallback<?> individualCallback: callbacks) {
			if (individualCallback != null) {
				@SuppressWarnings("unchecked")
				AsyncCallback<Object> castedCallback = (AsyncCallback<Object>) individualCallback;
				ServiceResult serviceResult = compositeResponse.getResults().get(index);
				ResponseEnvelope responseEnvelope = (serviceResult != null) ? serviceResult.asResponse() : null;
				if (responseEnvelope != null) {
					castedCallback.onSuccess(responseEnvelope.getResult());
				}
			}
			
			index++;
		}
	}
	
	public EvalContext<? extends CompositeResponse> eval(Evaluator<ServiceRequest> evaluator) {
		return new CompositeEvalContext<>(request.eval(evaluator));
	}
	
	private class CompositeEvalContext<T extends CompositeResponse> implements EvalContext<T> {
		final EvalContext<T> delegate;
		
		public CompositeEvalContext(EvalContext<T> delegate) {
			super();
			this.delegate = delegate;
		}

		@Override
		public T get() throws EvalException {
			T compositeResponse = delegate.get();
			processResults(compositeResponse);
			return compositeResponse;
		}

		@Override
		public void get(final AsyncCallback<? super T> callback) {
			delegate.get(new AsyncCallback<T>() {
				@Override
				public void onSuccess(T compositeResponse) {
					processResults(compositeResponse);
					callback.onSuccess(compositeResponse);
				}

				@Override
				public void onFailure(Throwable t) {
					callback.onFailure(t);
				}
			});
		}
		
		@Override
		public Maybe<T> getReasoned() {
			Maybe<T> maybe = delegate.getReasoned();
			
			if (maybe.isSatisfied()) {
				CompositeResponse compositeResponse = maybe.get();
				processResults(compositeResponse);
			}
			
			return maybe;
		}
		
		@Override
		public void getReasoned(AsyncCallback<? super Maybe<T>> callback) {
			delegate.getReasoned(AsyncCallback.of(
				m -> {
					if (m.isSatisfied()) {
						CompositeResponse compositeResponse = m.get();
						processResults(compositeResponse);
					}
					callback.onSuccess(m);
				},
				t-> {
					callback.onFailure(t);
				})
			);
		}

		@Override
		public <X, A extends EvalContextAspect<? super X>> EvalContext<T> with(Class<A> aspect, X value) {
			delegate.with(aspect, value);
			return null;
		}
	}
}
