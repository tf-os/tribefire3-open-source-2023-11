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
package tribefire.platform.impl.service.async;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.service.api.AsynchronousRequest;
import com.braintribe.model.service.api.ServiceRequest;

public class ServiceRequestRunnable implements Runnable {

	private static final Logger logger = Logger.getLogger(ServiceRequestRunnable.class);
	
	private AsynchronousRequest asyncRequest;
	private Evaluator<ServiceRequest> requestEvaluator;
	protected String correlationId;
	protected CallbackExpert callbackExpert;

	public ServiceRequestRunnable(String correlationId, AsynchronousRequest asyncRequest, Evaluator<ServiceRequest> requestEvaluator, CallbackExpert callbackExpert) {
		this.correlationId = correlationId;
		this.asyncRequest = asyncRequest;
		this.requestEvaluator = requestEvaluator;
		this.callbackExpert = callbackExpert;
	}
	
	@Override
	public void run() {
		ServiceRequest serviceRequest = asyncRequest.getServiceRequest();
		logger.trace(() -> "Asynchronous processing " + correlationId + " of " + serviceRequest.getClass().getName() + " has started");

		try {
			preFlight();
			Object result = serviceRequest.eval(requestEvaluator).get();
			onSuccess(result);
			doCallback(result, null);
		} catch (Throwable t) {
			onFailure(t);
			doCallback(null, t);
		}
	}
	
	protected void preFlight() {
		ServiceRequest serviceRequest = asyncRequest.getServiceRequest();
		logger.trace(() -> "Asynchronous processing " + correlationId + " of " + serviceRequest.getClass().getName() + " starting.");
	}
	protected void onSuccess(Object result) {
		ServiceRequest serviceRequest = asyncRequest.getServiceRequest();
		logger.trace(() -> "Asynchronous processing " + correlationId + " of " + serviceRequest.getClass().getName() + " completed. Result: " + result);
	}
	protected void doCallback(Object result, Throwable t) {
		callbackExpert.doCallback(asyncRequest, result, t);
	}

	protected void onFailure(Throwable t) {
		ServiceRequest serviceRequest = asyncRequest.getServiceRequest();
		logger.error("Asynchronous processing " + correlationId + " of " + serviceRequest.getClass().getName() + " failed with " + t, t);
	}
	
	public AsynchronousRequest getAsyncRequest() {
		return asyncRequest;
	}
	public String getCorrelationId() {
		return correlationId;
	}
	
}
