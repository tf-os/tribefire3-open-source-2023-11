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
package com.braintribe.gwt.gmview.client.js;

import java.util.function.Function;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;

/**
 * Supplier which provides an {@link ExternalWidgetGmContentView} for a given module JS (which is the module's URL).
 * @author michel.docouto
 *
 */
public class TestExternalComponentWidgetSupplier implements Function<String, ExternalWidgetGmContentView> {
	
	private JsScriptLoader jsScriptLoader;

	/**
	 * Configures the loader used for loading the TribefireJs script into the DOM.
	 */
	@Configurable
	public void setJsScriptLoader(JsScriptLoader jsScriptLoader) {
		this.jsScriptLoader = jsScriptLoader; 
	}		
	
	@Override
	public ExternalWidgetGmContentView apply(String jsModule) {
		//RVE - add tf.js to DOM document header
		if (this.jsScriptLoader != null && !jsScriptLoader.containsScript())
			this.jsScriptLoader.loadScript(); //TODO: I think this should be an async call... and it should return only when the js is loaded
		
		ExternalWidgetGmContentView view = new ExternalWidgetGmContentView(null);
		
		Scheduler.get().scheduleFixedDelay(() -> {
			if (!isTribefireJsLoaded())
				return true;
			
			Future<JavaScriptObject> moduleFuture = new Future<>();
			supplyModule(jsModule, moduleFuture, TestExternalComponentWidgetSupplier.this);
			moduleFuture //
					.andThen(componentModule -> view.setExternalModule(null, componentModule)) //
					.onError(e -> {
						ErrorDialog.show("Error while loading the external js module.", e);
						view.setExternalModule(null, null);
					});
			
			return false;
		}, 100);
		
		return view;
	}
	
	private void returnImportException(String jsModule, Future<?> future, Object javascriptException) {
		JavaScriptException jsException = new JavaScriptException(javascriptException, "Error while importing JS module: '" + jsModule + "'");
		future.onFailure(jsException);
	}
	
	private native void supplyModule(String jsModule, Future<JavaScriptObject> future, TestExternalComponentWidgetSupplier supplier) /*-{
		var importFunction = new Function("module", "return import(module);");
		var promise = importFunction(jsModule);
		
		promise.then(
			function(loadedModule) {
				future.@com.braintribe.gwt.async.client.Future::onSuccess(Ljava/lang/Object;)(loadedModule);
			},
			function(err) {
				supplier.@com.braintribe.gwt.gmview.client.js.TestExternalComponentWidgetSupplier::
						returnImportException(Ljava/lang/String;Lcom/braintribe/gwt/async/client/Future;Ljava/lang/Object;)(jsModule, future, err);
			}
		);
	}-*/;
	
	private static native boolean isTribefireJsLoaded() /*-{
		if (typeof $wnd.$T === 'undefined')
			return false;
			
		return true;
	}-*/;

}
