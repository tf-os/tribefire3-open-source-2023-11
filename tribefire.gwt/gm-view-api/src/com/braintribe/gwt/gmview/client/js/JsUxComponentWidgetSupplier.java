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
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.client.GmActionSupport;
import com.braintribe.gwt.gmview.client.GmContentViewActionManager;
import com.braintribe.gwt.gmview.client.WebSocketSupport;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.service.api.ProcessorRegistry;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.service.api.ServiceRequest;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.sencha.gxt.core.shared.FastMap;

import tribefire.extension.js.model.deployment.JsUxComponent;

/**
 * Supplier which provides an {@link ExternalWidgetGmContentView} for a given {@link JsUxComponentContext}.
 * @author michel.docouto
 *
 */
public class JsUxComponentWidgetSupplier implements Function<JsUxComponentContext, ExternalWidgetGmContentView> {
	protected static final Logger logger = new Logger(JsUxComponentWidgetSupplier.class);
	private static int counter = 0;
	
	private GmContentViewActionManager actionManager;
	private FastMap<Integer> cssCountMap = new FastMap<>();
	private FastMap<String> cssLinkIdMap = new FastMap<>();
	private String servicesUrl;
	private Supplier<ModelEnvironmentDrivenGmSession> rawSessionFactory;
	private Supplier<? extends ProcessorRegistry> processorRegistrySupplier;
	private Supplier<? extends Evaluator<ServiceRequest>> localEvaluatorSupplier;
	private Supplier<String> clientIdSupplier;
	private Supplier<String> sessionIdSupplier;
	private Supplier<? extends WebSocketSupport> webSocketSupportSupplier;
	private static Function<String, String> tribefireRuntimeSupplier;
	
	/**
	 * Configures the required function which provide values for tribefire runtime variables.
	 */
	@Required
	public static void setTribefireRuntimeSupplier(Function<String, String> tribefireRuntimeSupplier) {
		JsUxComponentWidgetSupplier.tribefireRuntimeSupplier = tribefireRuntimeSupplier;
	}

	/**
	 * Configures the required {@link GmContentViewActionManager}. This is used in case the external {@link JsUxComponent} is actually an instanceof of {@link GmActionSupport}.
	 */
	@Required
	public void setActionManager(GmContentViewActionManager actionManager) {
		this.actionManager = actionManager;		
	}
	
	/**
	 * Configures the required tribefire services URL
	 */
	@Required
	public void setServicesUrl(String servicesUrl) {
		this.servicesUrl = servicesUrl;
	}
	
	@Required
	public void setRawSessionFactory(Supplier<ModelEnvironmentDrivenGmSession> rawSessionFactory) {
		this.rawSessionFactory = rawSessionFactory;
	}
	
	/**
	 * Configures the required supplier for the {@link ProcessorRegistry}.
	 */
	@Required
	public void setProcessorRegistrySupplier(Supplier<? extends ProcessorRegistry> processorRegistrySupplier) {
		this.processorRegistrySupplier = processorRegistrySupplier;
	}
	
	/**
	 * Configures the required supplier for the local Evaluator.
	 */
	@Required
	public void setLocalEvaluatorSupplier(Supplier<? extends Evaluator<ServiceRequest>> localEvaluatorSupplier) {
		this.localEvaluatorSupplier = localEvaluatorSupplier;
	}
	
	/**
	 * Configures the required supplier for the clientId.
	 */
	@Required
	public void setClientIdSupplier(Supplier<String> clientIdSupplier) {
		this.clientIdSupplier = clientIdSupplier;
	}
	
	/**
	 * Configures the required supplier for the sessionId.
	 */
	@Required
	public void setSessionIdSupplier(Supplier<String> sessionIdSupplier) {
		this.sessionIdSupplier = sessionIdSupplier;
	}
	
	/**
	 * Configures the required supplier for the {@link WebSocketSupport}.
	 */
	@Configurable
	public void setWebSocketSupportSupplier(Supplier<? extends WebSocketSupport> webSocketSupportSupplier) {
		this.webSocketSupportSupplier = webSocketSupportSupplier;
	}
	
	@Override
	public ExternalWidgetGmContentView apply(JsUxComponentContext jsUxComponentContext) {
		ExternalWidgetGmContentView view = new ExternalWidgetGmContentView(this);
		view.setActionManager(this.actionManager);
		view.setServicesUrl(servicesUrl);
		view.setRawSessionFactory(rawSessionFactory);
		view.setProcessorRegistry(processorRegistrySupplier.get());
		view.setLocalEvaluator(localEvaluatorSupplier.get());
		view.setClientId(clientIdSupplier.get());
		view.setSessionId(sessionIdSupplier.get());
		if (webSocketSupportSupplier != null)
			view.setWebSocketSupport(webSocketSupportSupplier.get());
		view.setWindow(jsUxComponentContext.getWindow());
		
		Scheduler.get().scheduleDeferred(() -> {
			Future<JavaScriptObject> moduleFuture = new Future<>();
			JsUxComponent jsUxComponent = jsUxComponentContext.getJsUxComponent();
			String path = getNormalizedPath(jsUxComponent.getModule().getPath());
			if (!path.startsWith("http"))
				path = servicesUrl + path;
			
			supplyModule(path, moduleFuture, JsUxComponentWidgetSupplier.this);
			moduleFuture //
					.andThen(componentModule -> view.setExternalModule(jsUxComponent, componentModule)) //
					.onError(e -> {
						ErrorDialog.show("Error while loading the external js module.", e);
						view.setExternalModule(null, null);
					});
		});
		
		return view;
	}
	
	protected void handleAttachCssStyles(ComponentCreateContext context) {
		if (context == null || context.getCssStyles() == null)
			return;
		
		for (String cssLink : context.getCssStyles()) {
			int count = cssCountMap.getOrDefault(cssLink, 0);
			if (count == 0) {
				String cssLinkId = "gmeExternalComponentCss" + counter++;
				cssLinkIdMap.put(cssLink, cssLinkId);
				CssLoader.loadCss(cssLinkId, cssLink);
			}
			
			cssCountMap.put(cssLink, ++count);
		}
	}
	
	protected void handleDetachCssStyles(ComponentCreateContext context) {
		if (context == null || context.getCssStyles() == null)
			return;
		
		for (String cssLink : context.getCssStyles()) {
			int count = cssCountMap.getOrDefault(cssLink, 0);
			if (count == 0)
				continue;
			
			if (count > 1)
				cssCountMap.put(cssLink, --count);
			else if (count == 1) {
				cssCountMap.remove(cssLink);
				CssLoader.unloadCss(cssLinkIdMap.remove(cssLink));
			}
		}
	}
	
	private void returnImportException(boolean importNotSupported, String jsModule, Future<?> future, Object javascriptException) {
		JavaScriptException jsException;
		if (importNotSupported)
			jsException = new JavaScriptException(javascriptException, "ES6 import not supported in your browser. Can't import JS module: '" + jsModule + "'");
		else
			jsException = new JavaScriptException(javascriptException, "Error while importing JS module: '" + jsModule + "'");
			
		future.onFailure(jsException);
	}
	
	/**
	 * Handles the given modulePath, returning the normalized path.
	 * If the given path contains variables in brackets, then they are first retrieved from the TribefireRuntime.
	 */
	public static String getNormalizedPath(String modulePath) {
		if (tribefireRuntimeSupplier == null)
			return modulePath;
		
		while (modulePath.contains("${")) {
			int index = modulePath.indexOf("${");
			int finalIndex = modulePath.indexOf("}");
			String varName = modulePath.substring(index + 2, finalIndex);
			String value = tribefireRuntimeSupplier.apply(varName);
			if (value == null) {
				value = "";
				logger.info("The variable + '" + varName + "' has no value");
			}
			
			modulePath = modulePath.replace("${" + varName + "}", value);
		}
		
		//If somehow the variable has an absolute path, then we ignore what comes before that, in the path (if something).
		int index = modulePath.indexOf("http");
		if (index > 0)
			modulePath = modulePath.substring(index);
		
		return modulePath;
	}
	
	private native void supplyModule(String jsModule, Future<JavaScriptObject> future, JsUxComponentWidgetSupplier supplier) /*-{
		try {
			var promise = $wnd.importModule(jsModule);
			
			promise.then(
				function(loadedModule) {
					future.@com.braintribe.gwt.async.client.Future::onSuccess(Ljava/lang/Object;)(loadedModule);
				},
				function(err) {
					supplier.@com.braintribe.gwt.gmview.client.js.JsUxComponentWidgetSupplier::
							returnImportException(ZLjava/lang/String;Lcom/braintribe/gwt/async/client/Future;Ljava/lang/Object;)(false, jsModule, future, err);
				}
			);
		} catch (err) {
			supplier.@com.braintribe.gwt.gmview.client.js.JsUxComponentWidgetSupplier::
							returnImportException(ZLjava/lang/String;Lcom/braintribe/gwt/async/client/Future;Ljava/lang/Object;)(true, jsModule, future, err);
		}
	}-*/;

}
