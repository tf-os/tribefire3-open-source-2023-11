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
package com.braintribe.gwt.gmrpc.web.client;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import com.braintribe.common.attribute.TypeSafeAttribute;
import com.braintribe.common.attribute.TypeSafeAttributeEntry;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.ReasonException;
import com.braintribe.gwt.gmrpc.api.client.exception.GmRpcException;
import com.braintribe.gwt.gmrpc.api.client.user.EmbeddedRequiredTypesExpert;
import com.braintribe.gwt.gmrpc.api.client.user.EmbeddedRequiredTypesExpertAspect;
import com.braintribe.gwt.gmrpc.base.client.FailureDecoder;
import com.braintribe.gwt.gmrpc.base.client.RpcExceptionDetection;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.EvalContextAspect;
import com.braintribe.model.generic.eval.EvalException;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.processing.async.api.AsyncCallback;

@SuppressWarnings("unusable-by-js")
public class GwtGmWebRpcEvaluator extends AbstractGmWebRpcRequestSender implements Evaluator<ServiceRequest> {
	private Function<Failure, Throwable> failureDecoder = new FailureDecoder();
	
	@Override
	public <T> EvalContext<T> eval(ServiceRequest request) {
		return new GmWebRpcEvalContext<T>(request);
	}
	
	@SuppressWarnings("unusable-by-js")	
	private class GmWebRpcEvalContext<T> implements EvalContext<T>, RpcContext {

		private ServiceRequest request;
		private EmbeddedRequiredTypesExpert embeddedRequiredTypesExpert;
		private Map<Class<? extends TypeSafeAttribute<?>>, Object> attributes = new HashMap<>();
		

		public GmWebRpcEvalContext(ServiceRequest request) {
			this.request = request;
		}

		@Override
		public <A extends TypeSafeAttribute<? super V>, V> void setAttribute(Class<A> attribute, V value) {
			if (attribute == EmbeddedRequiredTypesExpertAspect.class)
				this.embeddedRequiredTypesExpert = (EmbeddedRequiredTypesExpert)value;

			attributes.put(attribute, value);
		}
		
		@Override
		public <A extends TypeSafeAttribute<V>, V> Optional<V> findAttribute(Class<A> attribute) {
			return Optional.ofNullable((V) attributes.get(attribute));
		}
		
		@Override
		public <A extends TypeSafeAttribute<V>, V> V getAttribute(Class<A> attribute) {
			return findAttribute(attribute).orElseThrow(() -> new NoSuchElementException("Attribute not found: " + attribute));
		}
		
		@SuppressWarnings("unused")
		public <V, A extends EvalContextAspect<? super V>> void put(Class<A> aspect, V value) {
			setAttribute(aspect, value);
		}
		
		@Override
		public <V, A extends EvalContextAspect<? super V>> V get(Class<A> aspect) {
			return (V)attributes.get(aspect);
		}
		
		@Override
		public Stream<TypeSafeAttributeEntry> streamAttributes() {
			return attributes.entrySet().stream().map(e -> TypeSafeAttributeEntry.of((Class<TypeSafeAttribute<Object>>)e.getKey(), e.getValue()));
		}

		@Override
		public T get() throws EvalException {
			return getReasoned().get();
		}

		@Override
		public Maybe<T> getReasoned() {
			ServiceResult serviceResult;
			try {
				serviceResult = sendRequest(request, embeddedRequiredTypesExpert, true);
			} catch (GmRpcException e) {
				throw new EvalException(e);
			}
			
			return maybeFromResult(serviceResult);
		}

		private Maybe<T> maybeFromResult(ServiceResult serviceResult) throws Error {
			switch (serviceResult.resultType()) {
			case success:
				return Maybe.complete((T)serviceResult.asResponse().getResult());
			case failure:
				throw asRuntimeException(serviceResult);
			case unsatisfied:
				return serviceResult.asUnsatisfied().toMaby();
			default:
				throw new IllegalStateException("Unsupported result type: " + serviceResult.resultType());
			}
		}
		

		private RuntimeException asRuntimeException(ServiceResult serviceResult) throws Error {
			Throwable throwable = RpcExceptionDetection.detect(serviceResult, failureDecoder);
			if (throwable != null) {
				// TODO: somehow analyze for notification requests
				
				if (throwable instanceof RuntimeException) {
					return (RuntimeException) throwable;
				} else if (throwable instanceof Error) {
					throw (Error) throwable;
				} else {
					return new EvalException("error while evaluating request", throwable);
				}
			}
			
			return new IllegalStateException("Unexpected error state. Missing exception from Failure");
		}
		
		@Override
		public void get(final AsyncCallback<? super T> callback) {
			getReasoned(new AsyncCallback<Maybe<T>>() {
				@Override
				public void onSuccess(Maybe<T> future) {
					if (future.isSatisfied()) {
						callback.onSuccess(future.get());
					}
					else {
						callback.onFailure(new ReasonException(future.whyUnsatisfied()));
					}
				}
				
				@Override
				public void onFailure(Throwable t) {
					callback.onFailure(t);
				}
			});
		}
		
		@Override
		public void getReasoned(AsyncCallback<? super Maybe<T>> callback) {
			sendRequest(this, request, embeddedRequiredTypesExpert, new com.google.gwt.user.client.rpc.AsyncCallback<ServiceResult>() {
				@Override
				public void onSuccess(ServiceResult result) {
					try {
						callback.onSuccess(maybeFromResult(result));
					}
					catch (Throwable t) {
						callback.onFailure(t);
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					// TODO: somehow analyze for notification requests
					callback.onFailure(caught);
				}
			}, true);
		}
		
	}
	

}