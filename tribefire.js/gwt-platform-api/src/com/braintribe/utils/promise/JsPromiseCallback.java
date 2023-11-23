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
package com.braintribe.utils.promise;

import java.util.function.Consumer;

import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.processing.async.api.JsPromise;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * helper class that adapts Javascript Promise with AsyncCallback from GWT and from Braintribe
 * 
 * @author Dirk Scheffler
 */
public class JsPromiseCallback<T> implements AsyncCallback<T>, com.google.gwt.user.client.rpc.AsyncCallback<T> {

	private com.braintribe.processing.async.api.JsPromise<T> promise;
	private JavaScriptObject resolve;
	private JavaScriptObject reject;

	public JsPromiseCallback() {
		initPromise();
	}

	private native void initPromise() /*-{
		var that = this;
		this.@JsPromiseCallback::promise = new Promise(function(resolve, reject) {
			that.@JsPromiseCallback::resolve = resolve;
			that.@JsPromiseCallback::reject = reject;
		});
	}-*/;

	@Override
	public native void onSuccess(T future) /*-{
		var f = this.@JsPromiseCallback::resolve;
		f(future);
	}-*/;

	@Override
	@SuppressWarnings("unusable-by-js")
	public native void onFailure(Throwable t) /*-{
		var f = this.@JsPromiseCallback::reject;
		f(t);
	}-*/;

	public JsPromise<T> asPromise() {
		return promise;
	}

	/**
	 * Convenience method to create a {@link JsPromise} for a method which accepts some kind of AsyncCallback.
	 * 
	 * Example: {@code return JsPromiseCallback.promisify(serviceRequest.eval(evaluator)::get)}
	 */
	public static <T> JsPromise<T> promisify(Consumer<? super JsPromiseCallback<T>> asyncCode) {
		JsPromiseCallback<T> callback = new JsPromiseCallback<>();
		asyncCode.accept(callback);
		return callback.asPromise();
	}

}
