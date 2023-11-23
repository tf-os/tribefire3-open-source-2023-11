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
package tribefire.platform.impl.service;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.AsynchronousRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.AsynchronousResponse;
import com.braintribe.thread.api.ThreadContextScoping;

/**
 * A {@link ServiceProcessor} which processes the {@link ServiceRequest}(s) wrapped by the incoming
 * {@link AsynchronousRequest}(s) asynchronously.
 * 
 */
public class AsynchronousServiceProcessor implements ServiceProcessor<AsynchronousRequest, AsynchronousResponse> {

	private static final Logger log = Logger.getLogger(AsynchronousServiceProcessor.class);

	// configurable
	private ExecutorService executorService;
	private LogLevel swallowedExceptionsLogLevel = LogLevel.ERROR;
	protected ThreadContextScoping threadScoping;

	@Required
	@Configurable
	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	@Configurable
	public void setSwallowedExceptionsLogLevel(LogLevel swallowedExceptionsLogLevel) {
		this.swallowedExceptionsLogLevel = swallowedExceptionsLogLevel;
	}

	@Required
	@Configurable
	public void setThreadScoping(ThreadContextScoping threadScoping) {
		this.threadScoping = threadScoping;
	}

	@Override
	public AsynchronousResponse process(ServiceRequestContext requestContext, AsynchronousRequest request) {

		Objects.requireNonNull(requestContext, "requestContext");
		Objects.requireNonNull(request, "request");

		ServiceRequest payload = request.getServiceRequest();

		if (payload == null) {
			throw new IllegalArgumentException("The incoming " + AsynchronousRequest.class.getSimpleName() + " has no service request set.");
		}

		String correlationId = UUID.randomUUID().toString();

		AsynchronousResponse response = AsynchronousResponse.T.create();
		response.setCorrelationId(correlationId);

		Callable<Void> callable = () -> {

			log.trace(() -> "Asynchronous processing " + correlationId + " of " + payload.getClass().getName() + " has started");

			try {
				Object result = payload.eval(requestContext).get();

				log.trace(() -> "Asynchronous processing " + correlationId + " of " + payload.getClass().getName() + " completed. Result: " + result);

			} catch (Throwable t) {
				log.log(swallowedExceptionsLogLevel,
						"Asynchronous processing " + correlationId + " of " + payload.getClass().getName() + " failed with " + t, t);
			}

			return null;

		};

		callable = threadScoping.bindContext(callable);

		log.trace(() -> "Asynchronous processing " + correlationId + " of " + payload.getClass().getName() + " will be submitted");

		executorService.submit(callable);

		log.trace(() -> "Asynchronous processing " + correlationId + " of " + payload.getClass().getName() + " was submitted");

		return response;

	}

}
