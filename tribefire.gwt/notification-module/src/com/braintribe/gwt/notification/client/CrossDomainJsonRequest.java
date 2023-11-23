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
package com.braintribe.gwt.notification.client;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.logging.client.Logger;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.ScriptElement;
import com.google.gwt.user.client.DOM;

/**
 * The CrossDomainJsonRequest is being used to load a JSON encoded data from an HTTP connection.
 * The JSON data will be loaded with help of a dynamic script tag that is able to overcome
 * the SOP (same origin policy). Thus it is suitable for cases where a keep alive connection
 * is needed (e.g. {@link NotificationPoll}) because a browser can have only two simultanous
 * connections for one host name. For not to block those rare connections it is required to
 * access a hostname that violates SOP (same origin policy) and that can only be done with
 * a dynamic script tag.
 * 
 * Because receiving JSON data via a dynamic script tag an injector function is needed and the 
 * server part of the HTTP connection must be able to support this kind of JSON injection.
 *  
 * @author Dirk
 *
 */
public class CrossDomainJsonRequest {
	private static int counter = 1;
	private static Logger logger = new Logger(CrossDomainJsonRequest.class);
	private String url;
	private Future<JavaScriptObject> future; 
	private ScriptElement scriptTag;
	private int number;
	
	/**
	 * @param baseUrl the url the request will load its data from (an injector parameter will be added)
	 */
	public CrossDomainJsonRequest(String baseUrl) {
		url = baseUrl + "&injector=" + getInjector();
		number = counter++;
	}
	
	/**
	 * @return The function name of the associated injector function used to receive the actual JSON data
	 */
	public String getInjectorFunctionName() {
		return "injector" + System.identityHashCode(this);
	}
	
	/**
	 * @return The name of the global variable used to store injector function (in cleanup those function will be removed)
	 */
	public String getInjectorsSlot() {
		return "bt_injectors";
	}

	/**
	 * @return the action injector function path that will be used for the injector parameter
	 */
	public String getInjector() {
		return getInjectorsSlot() + "." + getInjectorFunctionName();
	}
	
	/**
	 * Starts the request
	 * @return a {@link Future} for the awaited result.
	 */
	public Future<JavaScriptObject> execute() throws NotificationException {
		if (future != null) 
			throw new NotificationException("this request has already been started");

		future = new Future<>();
		future.andThen(result -> cleanup()).onError(e -> cleanup());
		
		createInjector();
		createScriptTag();
		
		return future;
	}
	
	/**
	 * This method is called after a successful or unsuccessful request to cleanup
	 * the used dynamic script tag and also the injector function. 
	 */
	private void cleanup() {
		try {
			deleteScriptTag();
			deleteInjector();
			//logger.debug("#"+ number + ": cleaned up request resources");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void createInjector() {
		JavaScriptObject window = getWindow();
		JavaScriptObject injectorFunctions = (JavaScriptObject)getProperty(window, getInjectorsSlot());
		if (injectorFunctions == null) {
			injectorFunctions = JavaScriptObject.createObject();
			setProperty(window, getInjectorsSlot(), injectorFunctions);
		}
		
		setProperty(injectorFunctions, getInjectorFunctionName(), createFunction(new Function() {
			@Override
			public void run(JsArray<JavaScriptObject> arguments) {
				//logger.debug("#"+ number + ": receiving JSON data");
				future.onSuccess(arguments.get(0));
			}
		}));
	}
	
	/**
	 * This function creates the injection function that will transfer the JSON data to
	 * the future that was return from {@link #execute()}
	 * @see #future
	 */
//	private native void legacy_createInjector() /*-{
//		var future = this.@com.braintribe.gwt.notification.client.CrossDomainJsonRequest::future;
//		var injectorName = this.@com.braintribe.gwt.notification.client.CrossDomainJsonRequest::getInjectorFunctionName()();
//		var injectorsSlot = this.@com.braintribe.gwt.notification.client.CrossDomainJsonRequest::getInjectorsSlot()();
//		
//		// ensure existence of global slot for injector functions
//		if (!$wnd[injectorsSlot]) $wnd[injectorsSlot] = {}; 
//		
//		// create injector function		
//		$wnd[injectorsSlot][injectorName] = function(jsonObj) {
//			future.@com.google.gwt.user.client.rpc.AsyncCallback::onSuccess(Ljava/lang/Object;)(jsonObj);
//		}
//	}-*/;
	
//	/**
//	 * This method deletes the associated injector function for cleanup
//	 */
//	private native void legacy_deleteInjector() /*-{
//		var injectorsSlot = this.@com.braintribe.gwt.notification.client.CrossDomainJsonRequest::getInjectorsSlot()();
//		var injectorName = this.@com.braintribe.gwt.notification.client.CrossDomainJsonRequest::getInjectorFunctionName()();
//		delete $wnd[injectorsSlot][injectorName];
//	}-*/;
	
	private void deleteInjector() {
		JavaScriptObject window = getWindow();
		JavaScriptObject injectorFunctions = (JavaScriptObject)getProperty(window, getInjectorsSlot());
		if (injectorFunctions != null) {
			deleteProperty(injectorFunctions, getInjectorFunctionName());
		}
	}
	
	/*private static Throwable createErrorException() {
		return new NotificationException("error while polling notification");
	}*/

	/**
	 * @return if the request was alread successful. This function is used in the case that IE
	 * is the browser engine because the onreadstatechange notification of IE only informs about
	 * the end of an request but not if it was successful. So the check is being done by checking
	 * if the injector was called (results in a future success state). Otherwise an error is detected
	 * in IE case. In FF the error handling is done via the functional onerror callback.
	 */
	private boolean wasSuccessful() {
		return future.getState() == Future.State.success;
	}
	
	private void createScriptTag() {
		//logger.debug("#"+ number + ": injector = " + getInjector());
		//logger.debug("#"+ number + ": poll url = " + url);
		scriptTag = DOM.createElement("script").cast();
		scriptTag.setType("text/javascript");
		
		setProperty(scriptTag, "onerror", createFunction((Runnable) () -> future.onFailure(new NotificationException("error occured while polling notification"))));
		
		setProperty(scriptTag, "onreadystatechange", createFunction((Runnable) () -> {
			String readyState = scriptTag.getPropertyString("readyState");
			//logger.debug("#"+ number + ": IE script tag readyState changed to: " +  readyState);
			if (!"loaded".equals(readyState))
				return;
			
			/*if (wasSuccessful()) {
				logger.debug("#"+ number + ": reached readyState 'loaded' and JSON data was successfully received");
			}
			else {*/
			if (!wasSuccessful()) { 
				logger.debug("#"+ number + ": reached readyState 'loaded' and JSON data is missing");
				future.onFailure(new NotificationException("reached readyState 'loaded' without receiving JSON data"));
			}
		}));
		scriptTag.setSrc(url);
		
		//logger.debug("#"+ number + ": script tag created");
		
		HeadElement headElement = Document.get().getElementsByTagName("head").getItem(0).cast();
		if(headElement != null)
			headElement.appendChild(scriptTag);
		
		//logger.debug("#"+ number + ": script tag integrated in DOM");
	}
	
	/**
	 * creates a dynamic script tag with the url as source and starts the loading by 
	 * adding it to the DOM.
	 */
//	private native void legacyCreateScriptTag() /*-{
//	    var url = this.@com.braintribe.gwt.notification.client.CrossDomainJsonRequest::url;
//	    var future = this.@com.braintribe.gwt.notification.client.CrossDomainJsonRequest::future;
//		var scriptTag = $wnd.document.createElement("script");
//		var request = this;
//	    
//	    var errorFunction = function() {
//	    	var throwable = @com.braintribe.gwt.notification.client.CrossDomainJsonRequest::createErrorException()();
//	    	future.@com.google.gwt.user.client.rpc.AsyncCallback::onFailure(Ljava/lang/Throwable;)(throwable);
//	    };
//	    
//	    var ieErrorFunction = function() {
//	    	if (scriptTag.readyState == 'loaded') {
//	    		if (!request.@com.braintribe.gwt.notification.client.CrossDomainJsonRequest::wasSuccessful()()) {
//	    			errorFunction();
//	    		}
//	    	}
//	    };
//	    
//		scriptTag.setAttribute("type", "text/javascript");
//		scriptTag.setAttribute("onerror", errorFunction);
//		scriptTag.setAttribute("onreadystatechange", ieErrorFunction);
//		scriptTag.setAttribute("src", url);
//		
//		// remember scriptTag for later cleanup
//		this.@com.braintribe.gwt.notification.client.CrossDomainJsonRequest::scriptTag = scriptTag;
//		
//		$wnd.document.getElementsByTagName("head")[0].appendChild(scriptTag);
//	}-*/;
	
	/**
	 * deletes the dynamic script tag from the DOM to cleanup
	 */
	public void deleteScriptTag() {
		if (scriptTag != null) {
			scriptTag.getParentElement().removeChild(scriptTag);
		}
	}
	
	private static native JavaScriptObject createFunction(Runnable runnable)/*-{
		return function() {
			runnable.@java.lang.Runnable::run()();
		};
	}-*/;
	
	private static native JavaScriptObject createFunction(Function functionImpl)/*-{
		return function() {
			functionImpl.@com.braintribe.gwt.notification.client.Function::run(Lcom/google/gwt/core/client/JsArray;)(arguments);
		};
	}-*/;
	
	private static native void setProperty(JavaScriptObject jsObject, String key, Object value) /*-{
		jsObject[key] = value;
	}-*/;
	
	private static native Object getProperty(JavaScriptObject jsObject, String key) /*-{
		return jsObject[key];
	}-*/;
	
	private static native void deleteProperty(JavaScriptObject jsObject, String key) /*-{
		delete jsObject[key];
	}-*/;
	
	private static native JavaScriptObject getWindow() /*-{
		return $wnd;
	}-*/;
}
