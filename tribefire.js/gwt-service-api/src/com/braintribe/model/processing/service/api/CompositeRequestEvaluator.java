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
import java.util.Optional;
import java.util.stream.Stream;

import com.braintribe.common.attribute.TypeSafeAttribute;
import com.braintribe.common.attribute.TypeSafeAttributeEntry;
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

@SuppressWarnings("unusable-by-js")
public class CompositeRequestEvaluator implements Evaluator<ServiceRequest> {
	private List<AsyncCallback<?>> callbacks = new ArrayList<>();
	private CompositeRequest request;

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
			@SuppressWarnings("unusable-by-js")
			public void get(AsyncCallback<? super T> callback) {
				callbacks.set(position, callback);
			}

			@Override
			@SuppressWarnings("unusable-by-js")
			public <U, A extends EvalContextAspect<? super U>> EvalContext<T> with(Class<A> aspect, U value) {
				return this;
			}

			@Override
			@SuppressWarnings("unusable-by-js")
			public <A extends TypeSafeAttribute<V>, V> Optional<V> findAttribute(Class<A> attribute) {
				return EvalContext.super.findAttribute(attribute);
			}

			@Override
			@SuppressWarnings("unusable-by-js")
			public <A extends TypeSafeAttribute<V>, V> V getAttribute(Class<A> attribute) {
				return EvalContext.super.getAttribute(attribute);
			}

			@Override
			@SuppressWarnings("unusable-by-js")
			public Stream<TypeSafeAttributeEntry> streamAttributes() {
				return EvalContext.super.streamAttributes();
			}

			@Override
			@SuppressWarnings("unusable-by-js")
			public <A extends TypeSafeAttribute<? super V>, V> void setAttribute(Class<A> attribute, V value) {
				EvalContext.super.setAttribute(attribute, value);
			}
		};
	}

	protected void processResults(CompositeResponse compositeResponse) {
		int index = 0;
		for (AsyncCallback<?> individualCallback : callbacks) {
			if (individualCallback != null) {
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
		final EvalContext<? extends CompositeResponse> actualEvalContext = request.eval(evaluator);
		return new EvalContext<CompositeResponse>() {

			@Override
			@SuppressWarnings("unusable-by-js")
			public CompositeResponse get() throws EvalException {
				CompositeResponse compositeResponse = actualEvalContext.get();
				processResults(compositeResponse);
				return compositeResponse;
			}

			@Override
			@SuppressWarnings("unusable-by-js")
			public void get(final AsyncCallback<? super CompositeResponse> callback) {
				actualEvalContext.get(new AsyncCallback<CompositeResponse>() {

					@Override
					public void onSuccess(CompositeResponse compositeResponse) {
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
			@SuppressWarnings("unusable-by-js")
			public <T, A extends EvalContextAspect<? super T>> EvalContext<CompositeResponse> with(Class<A> aspect, T value) {
				actualEvalContext.with(aspect, value);
				return null;
			}

			@Override
			@SuppressWarnings("unusable-by-js")
			public <A extends TypeSafeAttribute<V>, V> Optional<V> findAttribute(Class<A> attribute) {
				return EvalContext.super.findAttribute(attribute);
			}

			@Override
			@SuppressWarnings("unusable-by-js")
			public <A extends TypeSafeAttribute<V>, V> V getAttribute(Class<A> attribute) {
				return EvalContext.super.getAttribute(attribute);
			}

			@Override
			@SuppressWarnings("unusable-by-js")
			public Stream<TypeSafeAttributeEntry> streamAttributes() {
				return EvalContext.super.streamAttributes();
			}

			@Override
			@SuppressWarnings("unusable-by-js")
			public <A extends TypeSafeAttribute<? super V>, V> void setAttribute(Class<A> attribute, V value) {
				EvalContext.super.setAttribute(attribute, value);
			}
		};
	}
}
