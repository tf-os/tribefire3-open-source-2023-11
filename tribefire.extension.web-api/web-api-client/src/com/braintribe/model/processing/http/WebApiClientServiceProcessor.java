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
package com.braintribe.model.processing.http;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.exception.HttpException;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.api.aspect.HttpStatusCodeNotification;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.processing.http.client.HttpClient;
import com.braintribe.processing.http.client.HttpRequestContext;
import com.braintribe.processing.http.client.HttpResponse;
import com.braintribe.utils.lcd.StopWatch;

public class WebApiClientServiceProcessor implements ServiceProcessor<ServiceRequest, Object>{
	
	private static final Logger logger = Logger.getLogger(WebApiClientServiceProcessor.class);
	
	
	private HttpContextResolver httpContextResolver;
	
	// ***************************************************************************************************
	// Setter
	// ***************************************************************************************************

	@Required
	@Configurable
	public void setHttpContextResolver(HttpContextResolver httpContextResolver) {
		this.httpContextResolver = httpContextResolver;
	}
	
	// ***************************************************************************************************
	// ServiceProcessor
	// ***************************************************************************************************

	@Override
	public Object process(ServiceRequestContext context, ServiceRequest request) {
		StopWatch watch = stopWatch();
		try {
			
			HttpRequestContext httpContext = this.httpContextResolver.resolve(context, request);
			logger.trace(()->"Context creation for HTTP execution of ServiceRequest: "+request+" took: "+watch.getElapsedTime()+"ms.");
			
			HttpClient httpClient = httpContext.httpClient();
			HttpResponse response = httpClient.sendRequest(httpContext);
			logger.trace(()->"Sending the http request for: "+request+" took: "+watch.getElapsedTime()+"ms.");
			
			return response.combinedResponse();
			
		}catch(HttpException e) {
			context.getAspect(HttpStatusCodeNotification.class) //
				.ifPresent(a -> a.accept(e.getStatusCode()));
			return e.getPayload();
		} finally {
			logger.debug(()->"Finished HTTP execution for ServiceRequest: "+request+" after: "+watch.getElapsedTime()+"ms.");
		}
	}

	// ***************************************************************************************************
	// Helper
	// ***************************************************************************************************
	
	private StopWatch stopWatch() {
		StopWatch watch = new StopWatch();
		watch.setAutomaticResetEnabled(true);
		return watch;
	}

	
}
