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

import com.braintribe.gwt.gmview.client.WebSocketSupport;
import com.braintribe.gwt.gmview.client.js.interop.InteropConstants;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.service.api.ProcessorRegistry;
import com.braintribe.model.service.api.ServiceRequest;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType (namespace = InteropConstants.MODULE_NAMESPACE)
@SuppressWarnings("unusable-by-js")
public class ServiceBindingContext implements WebSocketSupport {
	
	private ProcessorRegistry processorRegistry;
	private Evaluator<ServiceRequest> localEvaluator;
	private Evaluator<ServiceRequest> remoteEvaluator;
	private String clientId;
	private String sessionId;
	private WebSocketSupport webSocketImpl;
	
	@JsConstructor
	public ServiceBindingContext() {
	}
	
	/**
	 * Configures the required implementation for the {@link WebSocketSupport}.
	 */
	@JsIgnore
	@Required
	public void setWebSocketImpl(WebSocketSupport webSocketImpl) {
		this.webSocketImpl = webSocketImpl;
	}
	
	@JsProperty
	public void setProcessorRegistry(ProcessorRegistry processorRegistry) {
		this.processorRegistry = processorRegistry;
	}
	
	@JsProperty
	public ProcessorRegistry getProcessorRegistry() {
		return processorRegistry;
	}
	
	@JsProperty
	public void setLocalEvaluator(Evaluator<ServiceRequest> localEvaluator) {
		this.localEvaluator = localEvaluator;
	}
	
	@JsProperty
	public Evaluator<ServiceRequest> getLocalEvaluator() {
		return localEvaluator;
	}
	
	@JsProperty
	public void setRemoteEvaluator(Evaluator<ServiceRequest> remoteEvaluator) {
		this.remoteEvaluator = remoteEvaluator;
	}
	
	@JsProperty
	public Evaluator<ServiceRequest> getRemoteEvaluator() {
		return remoteEvaluator;
	}
	
	@JsProperty
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	@JsProperty
	public String getSessionId() {
		return sessionId;
	}
	
	@JsProperty
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	@JsProperty
	public String getClientId() {
		return clientId;
	}
	
	@Override
	@JsMethod
	public void openNotificationChannel(String clientId) {
		webSocketImpl.openNotificationChannel(clientId);
	}
	
	@Override
	@JsMethod
	public void closeNotificationChannel(String clientId) {
		webSocketImpl.closeNotificationChannel(clientId);
	}

}
