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
/**
 * 
 */
package com.braintribe.processing.async.api;

import java.util.function.Consumer;
import java.util.function.Function;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

@JsType(namespace = "$tf.async")
@SuppressWarnings("unusable-by-js")
public interface AsyncCallback<T> {

	void onSuccess(T future);
	void onFailure(Throwable t);

	static <T> AsyncCallback<T> of(Consumer<? super T> consumer, Consumer<Throwable> errorHandler) {
		return new AsyncCallback<T>() {
			@Override
			public void onSuccess(T value) {
				consumer.accept(value);
			}

			@Override
			public void onFailure(Throwable t) {
				errorHandler.accept(t);
			}
		};
	}

	@JsMethod(name = "fromErrorHandler")
	static <T> AsyncCallback<T> of(Consumer<Throwable> errorHandler) {
		return of(r -> { /* NO OP */ }, errorHandler);
	}
	
	static <T> AsyncCallback<T> ofConsumer(Consumer<? super T> consumer) {
		return of (consumer, e -> { /* NO OP */});
	}

	/**
	 * Creates a new callback which accepts values of type X, by transforming them using given function to a value of type T and passing them to this
	 * callback.
	 */
	default <X> AsyncCallback<X> adapt(Function<? super X, ? extends T> adapter) {
		return of(x -> {
			T adapted;
			try {
				adapted = adapter.apply(x);
			} catch (Exception e) {
				onFailure(e);
				return;
			}
			onSuccess(adapted);
		}, this::onFailure);
	}

}
