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
package com.braintribe.gwt.gme.constellation.client.js;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.client.ModelEnvironmentSetListener;
import com.braintribe.gwt.gmview.client.js.ComponentCreateContext;
import com.braintribe.gwt.gmview.client.js.JsUxComponentWidgetSupplier;
import com.braintribe.gwt.gmview.client.js.TribefireUxModuleContract;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.meta.data.ui.UxModulesInitializer;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Window.Location;

/**
 * This class will check for the presence of UxModulesInitializer metadata, and load (import) the configured modules accordingly.
 * @author michel.docouto
 *
 */
public class JsUxModuleInitializer implements ModelEnvironmentSetListener {
	private static final Logger logger = new Logger(JsUxModuleInitializer.class);
	
	private String servicesUrl;
	private PersistenceGmSession gmSession;
	
	/**
	 * Configures the required tribefire services URL
	 */
	@Required
	public void setServicesUrl(String servicesUrl) {
		this.servicesUrl = servicesUrl;
	}
	
	/**
	 * Configures the session used for checking the metadata.
	 */
	@Required
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	@Override
	public void onModelEnvironmentSet() {
		UxModulesInitializer uxModulesInitializer = gmSession.getModelAccessory().getMetaData().lenient(true).meta(UxModulesInitializer.T).exclusive();
		if (uxModulesInitializer == null)
			return;
		
		uxModulesInitializer.getModules().forEach(module -> {
			module = JsUxComponentWidgetSupplier.getNormalizedPath(module);
			if (!module.startsWith("http"))
				module = servicesUrl + module;
			
			Future<JavaScriptObject> moduleFuture = new Future<>();
			supplyModule(module, moduleFuture, JsUxModuleInitializer.this);
			String finalModule = module;
			moduleFuture //
					.andThen(componentModule -> handleModuleLoaded(finalModule, componentModule)) //
					.onError(e -> {
						logger.error("Error while loading the external js module.", e);
					});
		});
	}
	
	private void handleModuleLoaded(String module, JavaScriptObject componentModule) {
		logger.info("Loaded external js module for: " + module);
		
		TribefireUxModuleContract contract = null;
		//Added the try catch blocks because the method is implemented by JS code. By catching them we have a better exception message
		try {
			contract = getContract(componentModule);
			if (contract != null) {
				//Don't do anything with the return, since this is only the initialization and no view should be returned anyway
				contract.createComponent(getComponentCreateContext(module), null);
			}
		} catch (Exception ex) {
			logger.info("A contract is not available for the given module: " + module, ex);
		}
	}
	
	private ComponentCreateContext getComponentCreateContext(String modulePath) {
		ComponentCreateContext context = new ComponentCreateContext();
		context.setModulePath(modulePath);
		//context.setCssStyles(new ArrayList<>());
		//context.setPersistenceSessionFactory(new JsPersistenceSessionFactoryImpl(remoteEvaluator, rawSessionFactory));
		context.setAccessId(gmSession.getAccessId());
		context.setRootUrl(Location.getProtocol() + "//" + Location.getHost() + "/");
		context.setServicesUrl(servicesUrl);
		
		return context;
	}
	
	private native TribefireUxModuleContract getContract(JavaScriptObject module) /*-{
		return module.contract;
	}-*/;
	
	private void returnImportException(boolean importNotSupported, String jsModule, Future<?> future, Object javascriptException) {
		JavaScriptException jsException;
		if (importNotSupported)
			jsException = new JavaScriptException(javascriptException, "ES6 import not supported in your browser. Can't import JS module: '" + jsModule + "'");
		else
			jsException = new JavaScriptException(javascriptException, "Error while importing JS module: '" + jsModule + "'");
			
		future.onFailure(jsException);
	}
	
	private native void supplyModule(String jsModule, Future<JavaScriptObject> future, JsUxModuleInitializer supplier) /*-{
		try {
			var promise = $wnd.importModule(jsModule);
			
			promise.then(
				function(loadedModule) {
					future.@com.braintribe.gwt.async.client.Future::onSuccess(Ljava/lang/Object;)(loadedModule);
				},
				function(err) {
					supplier.@com.braintribe.gwt.gme.constellation.client.js.JsUxModuleInitializer::
							returnImportException(ZLjava/lang/String;Lcom/braintribe/gwt/async/client/Future;Ljava/lang/Object;)(false, jsModule, future, err);
				}
			);
		} catch (err) {
			supplier.@com.braintribe.gwt.gme.constellation.client.js.JsUxModuleInitializer::
							returnImportException(ZLjava/lang/String;Lcom/braintribe/gwt/async/client/Future;Ljava/lang/Object;)(true, jsModule, future, err);
		}
	}-*/;

}
