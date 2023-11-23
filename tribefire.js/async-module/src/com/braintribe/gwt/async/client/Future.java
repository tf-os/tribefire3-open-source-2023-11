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
package com.braintribe.gwt.async.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.braintribe.common.lcd.function.CheckedBiConsumer;
import com.braintribe.common.lcd.function.CheckedConsumer;
import com.braintribe.common.lcd.function.CheckedFunction;
import com.braintribe.common.lcd.function.CheckedRunnable;
import com.braintribe.common.lcd.function.CheckedSupplier;
import com.braintribe.processing.async.api.JsPromise;
import com.braintribe.utils.promise.JsPromiseCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * This class is a special {@link AsyncCallback} that acts as a kind of multicaster whereby further callbacks can listen for the result.
 * 
 * There are two cases when a {@link AsyncCallback} is registered via {@link #get(AsyncCallback)}.
 * 
 * 1) The result which can be either a valid object (also null) or a {@link Throwable} is already present. In this case the callback will be
 * immediately called.
 * 
 * 2) The result which can be either a valid object (also null) or a {@link Throwable} is yet outstanding. In this case the callback will be called
 * when the result or the Throwable is received by this {@link Future}
 * 
 * @author Dirk
 *
 * @param <T>
 *            type parameter that defines the type of the awaited object
 */
@JsType(namespace = "$tf.session")
@SuppressWarnings("unusable-by-js")
public class Future<T> implements DualAsyncCallback<T>, Loader<T> {
	
	@JsType(namespace = "$tf.session")
	public static enum State {
		outstanding,
		success,
		failure
	}

	private final List<AsyncCallback<? super T>> callbacks = new ArrayList<>();
	private T result;
	private Throwable caught;
	private State state = State.outstanding;

	public Future() {
	}

	/** Constructor that is suitable for creating futures with already existing results (support for synchronous situations) */
	@JsIgnore
	public Future(T result) {
		this();
		this.result = result;
		state = State.success;
	}

	public JsPromise<T> toJsPromise() {
		JsPromiseCallback<T> promiseCallback = new JsPromiseCallback<>();

		this //
				.andThen(promiseCallback::onSuccess) //
				.onError(promiseCallback::onFailure);

		return promiseCallback.asPromise();
	}

	public State getState() {
		return state;
	}

	public T getResult() throws IllegalStateException {
		if (state == State.failure)
			throw new IllegalStateException(
					"Cannot reture a future result, as the computation failed. Setting the original error as the cause for this exception.", caught);
		
		if (state != State.success)
			throw new IllegalStateException("cannot reture a future result synchronously that is outstanding.");

		return result;
	}

	@JsMethod (name="getByConsumer")
	public void get(Consumer<T> success, Consumer<Throwable> failure) {
		get(AsyncCallbacks.of(success, failure));
	}

	/**
	 * Registers a callback. An immediate delivery of the result is possible in the case that the result is already received otherwise the result is
	 * delivered when received.
	 * 
	 * @see #remove(AsyncCallback)
	 */
	public void get(AsyncCallback<? super T> asyncCallback) {
		if (callbacks.contains(asyncCallback))
			return;

		switch (state) {
			case outstanding:
				callbacks.add(asyncCallback);
				break;
			case failure:
				asyncCallback.onFailure(caught);
				break;
			case success:
				asyncCallback.onSuccess(result);
				break;
		}
	}

	/** @see com.braintribe.gwt.async.client.Loader#load(com.google.gwt.user.client.rpc.AsyncCallback) */
	@Override
	public void load(AsyncCallback<T> asyncCallback) {
		get(asyncCallback);
	}

	/** Unregisters a callback that was registered before with {@link #get(AsyncCallback)} */
	public boolean remove(AsyncCallback<? super T> asyncCallback) {
		return callbacks.remove(asyncCallback);
	}

	/** Propagates the failure to any waiting callback */
	@Override
	public void onFailure(Throwable caught) {
		state = State.failure;
		this.caught = caught;

		for (AsyncCallback<? super T> callback : callbacks)
			callback.onFailure(caught);

		callbacks.clear();
	}

	/** Propagates the result to any waiting callback */
	@Override
	public void onSuccess(T result) {
		state = State.success;
		this.result = result;

		for (AsyncCallback<? super T> callback : callbacks) {
			callback.onSuccess(result);
		}
		callbacks.clear();
	}

	public <X> void passAdapted(AsyncCallback<X> callback, Function<? super T, ? extends X> adapter) {
		get(t -> {
			X x;
			try {
				x = adapter.apply(t);
			} catch (Exception e) {
				callback.onFailure(e);
				return;
			}
			callback.onSuccess(x);

		}, callback::onFailure);
	}

	public static <T extends Object> AsyncCallback<T> asyncGwt(Consumer<Throwable> failure, Consumer<T> success) {
		return AsyncCallbacks.of(success, failure);
	}

	public static <T extends Object> com.braintribe.processing.async.api.AsyncCallback<T> async(Consumer<Throwable> failure, Consumer<T> success) {
		return com.braintribe.processing.async.api.AsyncCallback.of(success, failure);
	}

	// ###############################################
	// ## . . . . Fluent Asynchronous Chain . . . . ##
	// ###############################################

	public static <T> Future<T> of(T value) {
		return new Future<>(value);
	}

	public static <T> Future<T> fromSupplier(CheckedSupplier<? extends T, Throwable> supplier) {
		return FutureChaining.fromSupplier(supplier);
	}

	public static <T> Future<T> fromAsyncCallbackConsumer(CheckedConsumer<? super DualAsyncCallback<T>, ? extends Throwable> consumer) {
		return FutureChaining.fromAsyncCallback(consumer);
	}

	public static <T> Future<T> fromError(Throwable e) {
		Future<T> result = new Future<>();
		result.onFailure(e);
		return result;
	}

	// onFailure just delegates
	public Future<T> andThen(CheckedConsumer<? super T, Throwable> consumer) {
		return FutureChaining.andThen(this, consumer);
	}

	public <X> Future<X> andThenNotify(CheckedBiConsumer<? super T, ? super DualAsyncCallback<X>, ? extends Throwable> consumer) {
		return FutureChaining.andThenNotify(this, consumer);
	}

	public <X> Future<X> andThenMap(CheckedFunction<? super T, ? extends X, Throwable> mappingFunction) {
		return FutureChaining.andThenMap(this, mappingFunction);
	}

	public <X> Future<X> andThenMapAsync(CheckedFunction<? super T, ? extends Future<? extends X>, Throwable> mappingFunction) {
		return FutureChaining.andThenMapAsync(this, mappingFunction);
	}

	// onSuccess does nothing
	public Future<T> onError(CheckedConsumer<? super Throwable, Throwable> consumer) {
		return FutureChaining.onError(this, consumer);
	}

	public Future<T> contextualizeError(CheckedFunction<? super Throwable, ? extends Throwable, Throwable> contextualizer) {
		return FutureChaining.contextualizeError(this, contextualizer);
	}

	public Future<T> catchError(CheckedFunction<? super Throwable, ? extends T, Throwable> valueProvider) {
		return FutureChaining.catchError(this, valueProvider);
	}

	// onSucess/onFailure call this function (the other param being null), and failure is of course also delegated
	public Future<T> andThenOrOnError(CheckedBiConsumer<? super T, ? super Throwable, Throwable> consumer) {
		return FutureChaining.andThenOrOnError(this, consumer);
	}

	public Future<T> andFinally(CheckedRunnable<Throwable> runnable) {
		return FutureChaining.andFinally(this, runnable);
	}

}
