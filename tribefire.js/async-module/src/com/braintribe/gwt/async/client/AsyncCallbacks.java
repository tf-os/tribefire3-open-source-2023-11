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

import java.util.function.Consumer;
import java.util.function.Function;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author peter.gazdik
 */
public interface AsyncCallbacks {

	/**
	 * Creates a new callback which accepts values of type X, by transforming them using given function to a value of type T and passing them to this
	 * callback.
	 */
	static <T, X> AsyncCallback<X> adapt(AsyncCallback<T> callback, Function<? super X, ? extends T> adapter) {
		return of(x -> {
			T adapted;
			try {
				adapted = adapter.apply(x);
			} catch (Exception e) {
				callback.onFailure(e);
				return;
			}
			callback.onSuccess(adapted);
		}, callback::onFailure);
	}

	static <T extends Object> AsyncCallback<T> of(Consumer<T> success, Consumer<Throwable> failure) {
		return new AsyncCallback<T>() {
			@Override
			public void onFailure(Throwable caught) {
				failure.accept(caught);
			}
			@Override
			public void onSuccess(T result) {
				success.accept(result);
			}
		};
	}

}
