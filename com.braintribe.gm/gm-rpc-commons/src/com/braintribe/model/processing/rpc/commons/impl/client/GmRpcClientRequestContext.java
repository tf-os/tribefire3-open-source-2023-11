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
package com.braintribe.model.processing.rpc.commons.impl.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.braintribe.cfg.Configurable;
import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.rpc.commons.impl.logging.RpcServiceRequestSummaryLogger;
import com.braintribe.model.processing.service.api.ServiceRequestSummaryLogger;
import com.braintribe.model.processing.traverse.EntityCollector;
import com.braintribe.model.resource.CallStreamCapture;
import com.braintribe.model.resource.source.TransientSource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.collection.impl.AttributeContexts;

public class GmRpcClientRequestContext {

	private ServiceRequest serviceRequest;
	private String logName;
	private Logger clientLogger;
	private boolean clientLoggerDebug;
	private boolean async = false;

	private final List<TransientSource> transientSources = new ArrayList<>();
	private final Map<String, CallStreamCapture> callStreamCaptures = new HashMap<>();
	private boolean streamEntitiesInitialized;

	private final Object assembly;

	private ServiceRequestSummaryLogger summaryLogger;

	private Consumer<?> responseConsumer;
	
	private boolean reasoned;

	private AttributeContext attributeContext;
	
	public GmRpcClientRequestContext(ServiceRequest serviceRequest, String logName, Logger clientLogger, boolean async) {
		initialize(serviceRequest, logName, clientLogger, async);
		assembly = serviceRequest;
	}

	public GmRpcClientRequestContext(ServiceRequest serviceRequest, List<Object> entryPoints, String logName, Logger clientLogger) {
		initialize(serviceRequest, logName, clientLogger, false);
		assembly = entryPoints;
	}

	@Configurable
	public void setReasoned(boolean reasoned) {
		this.reasoned = reasoned;
	}
	
	public boolean isReasoned() {
		return reasoned;
	}
	
	@Configurable
	public void setAttributeContext(AttributeContext attributeContext) {
		this.attributeContext = attributeContext;
	}

	public AttributeContext getAttributeContext() {
		return attributeContext == null ? AttributeContexts.peek() : attributeContext;
	}
	
	public Consumer<GenericEntity> getEntityVisitor() {
		if (streamEntitiesInitialized) {
			return e -> { /* do nothing */ };
		}
		else {
			streamEntitiesInitialized = true;
			return entity -> {
				if (entity instanceof TransientSource) {
					transientSources.add((TransientSource) entity);
				}
				else if (entity instanceof CallStreamCapture) {
					callStreamCaptures.put(entity.getGlobalId(), (CallStreamCapture) entity);
				}
			};
		}
	}

	private void ensureStreamEntitiesInitialized() {
		if (streamEntitiesInitialized)
			return;
		
		scanResources(assembly);
	}
	
	public List<TransientSource> getTransientSources() {
		ensureStreamEntitiesInitialized();
		return transientSources;
	}
	

	public Map<String, CallStreamCapture> getCallStreamCaptures() {
		ensureStreamEntitiesInitialized();
		return callStreamCaptures;
	}


	public ServiceRequest getServiceRequest() {
		return serviceRequest;
	}

	public String getLogName() {
		return logName;
	}

	public Logger getClientLogger() {
		return clientLogger;
	}

	public boolean isClientLoggerDebug() {
		return clientLoggerDebug;
	}
	
	public boolean hasInputResources() {
		return !getTransientSources().isEmpty();
	}
	
	/**
	 * <p>
	 * Whether this request is to be invoked asynchronously, in a different Thread than the caller.
	 */
	public boolean isAsynchronous() {
		return async;
	}

	protected void initialize(ServiceRequest _serviceRequest, String _logName, Logger _clientLogger, boolean _async) {
		this.serviceRequest = _serviceRequest;
		this.logName = _logName;
		this.clientLogger = _clientLogger;
		this.clientLoggerDebug = clientLogger.isDebugEnabled();
		this.async = _async;
		this.summaryLogger = RpcServiceRequestSummaryLogger.getInstance(clientLogger, AttributeContexts.peek(), null);
	}
	
	public ServiceRequestSummaryLogger summaryLogger() {
		return summaryLogger;
	}

	private void scanResources(Object assembly) {
		Consumer<GenericEntity> entityVisitor = getEntityVisitor();
		
		new EntityCollector() {
			@Override
			protected boolean add(GenericEntity entity, EntityType<?> type) {
				boolean added = super.add(entity, type);
				
				if (added) {
					entityVisitor.accept(entity);
				}
				
				return added;
			}
		}.visit(assembly);
	}

	public void setResponseConsumer(Consumer<?> responseConsumer) {
		this.responseConsumer = responseConsumer;
	}
	
	public Consumer<Object> getResponseConsumer() {
		return (Consumer<Object>)responseConsumer;
	}

	public void notifyResponse(Object result) {
		Consumer<Object> responseConsumer = getResponseConsumer();
		
		if (responseConsumer != null)
			responseConsumer.accept(result);
	}
}
