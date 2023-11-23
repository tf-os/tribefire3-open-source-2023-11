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
package com.braintribe.model.processing.service.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;
import com.braintribe.model.processing.service.api.CompositeException;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceProcessorAddressingException;
import com.braintribe.model.processing.service.api.ServiceProcessorException;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.CompositeRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.CompositeResponse;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.processing.async.api.Promise;
import com.braintribe.processing.async.impl.HubPromise;

/**
 * <p>
 * A {@link ServiceProcessor} for processing batches of {@link ServiceRequest}(s), as given by
 * {@link CompositeRequest}.
 * 
 */
public class CompositeServiceProcessor implements ServiceProcessor<CompositeRequest, CompositeResponse> {

	// constants
	private static final Logger log = Logger.getLogger(CompositeServiceProcessor.class);
	private static final String compositeExceptionMessasge = "Processing of composite request failed. From %d requests: %d successful; %d failed; %d skipped.";
	private static final String compositeExceptionMessasgeLine = System.lineSeparator() + "\tRequest #%2d: %s: ";

	protected LogLevel swallowedExceptionsLogLevel = LogLevel.ERROR;

	@Configurable
	public void setSwallowedExceptionsLogLevel(LogLevel swallowedExceptionsLogLevel) {
		Objects.requireNonNull(swallowedExceptionsLogLevel, "swallowedExceptionsLogLevel cannot be set to null");
		this.swallowedExceptionsLogLevel = swallowedExceptionsLogLevel;
	}

	@Override
	public CompositeResponse process(ServiceRequestContext requestContext, CompositeRequest compositeRequest) {

		Objects.requireNonNull(requestContext, "requestContext");
		Objects.requireNonNull(compositeRequest, "compositeRequest");

		validate(compositeRequest);

		List<ServiceRequest> requests = compositeRequest.getRequests();

		CompositeResponse compositeResult = CompositeResponse.T.create();

		boolean parallelize = compositeRequest.getParallelize();
		if (parallelize) {
			processParallelized(requestContext, compositeRequest, requests, compositeResult);
		} else {
			processSerialized(requestContext, compositeRequest, requests, compositeResult);
		}

		return compositeResult;
	}

	protected static class FixedValueFuture implements Future<Object> {
		private Object value;
		public FixedValueFuture(Object value) {
			this.value = value;
		}
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return true;
		}
		@Override
		public boolean isCancelled() {
			return false;
		}
		@Override
		public boolean isDone() {
			return true;
		}
		@Override
		public Object get() throws InterruptedException, ExecutionException {
			return value;
		}
		@Override
		public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			return value;
		}
	}

	protected void processParallelized(ServiceRequestContext requestContext, CompositeRequest compositeRequest, List<ServiceRequest> requests,
			CompositeResponse compositeResult) {
		
		int totalRequests = requests.size();
		boolean continueOnFailure = compositeRequest.getContinueOnFailure();

		ServiceResult[] results = new ServiceResult[totalRequests];
		Throwable[] failures = new Throwable[totalRequests];
		List<Promise<Object>> promises = new ArrayList<>(totalRequests);

		int processedResults = 0;
		int processedExceptions = 0;

		for (int i = 0; i < totalRequests; i++) {

			HubPromise<Object> promise = new HubPromise<>();
			
			ServiceRequest request = requests.get(i);
			
			request.eval(requestContext).get(promise);

			promises.add(promise);
		}

		for (int i = 0; i < totalRequests; i++) {
			Promise<Object> promise = promises.get(i);
			try {
				Object result = promise.get();
				
				if (result == null || !(result instanceof ServiceResult)) {
					results[i] = ServiceResults.envelope(result);
				} else {
					results[i] = (ServiceResult) result;
				}
				
				processedResults++;
			} catch (Exception ee) {
				if (!continueOnFailure) {
					failures[i] = ee;
					processedExceptions++;
				} else {
					results[i] = FailureConverter.INSTANCE.apply(ee);
					log.log(swallowedExceptionsLogLevel, ee.getMessage(), ee);
					processedResults++;
				}
			}
		}
		
		if (processedResults > 0) {
			compositeResult.setResults(Arrays.asList(results));
		}
		
		if (processedExceptions > 0) {
			Throwable throwable = getSingleThrowable(requests, processedExceptions, failures, compositeResult, processedResults);
			if (throwable instanceof RuntimeException) {
				throw (RuntimeException)throwable;
			} else if (throwable instanceof Error) {
				throw (Error)throwable;
			} else {
				throw new ServiceProcessorException(throwable);
			}
		}

	}
	
	protected void processSerialized(ServiceRequestContext requestContext, CompositeRequest compositeRequest, List<ServiceRequest> requests,
			CompositeResponse compositeResult) {

		int totalRequests = requests.size();
		boolean continueOnFailure = compositeRequest.getContinueOnFailure();

		for (int i = 0; i < totalRequests; i++) {

			ServiceRequest request = requests.get(i);

			try {

				Object result = null;

				if (request != null) {
					result = request.eval(requestContext).get();
				}

				ServiceResult serviceResult = null;

				if (result == null || !(result instanceof ServiceResult)) {
					serviceResult = ServiceResults.envelope(result);
				} else {
					serviceResult = (ServiceResult) result;
				}

				compositeResult.getResults().add(serviceResult);

			} catch (Throwable t) {

				if (!continueOnFailure) {
					throw t;
				} 

				log.log(swallowedExceptionsLogLevel, t.getMessage(), t);
				Failure failure = FailureConverter.INSTANCE.apply(t);
				compositeResult.getResults().add(failure);

			}

		}

	}

	/**
	 * <p>
	 * Checks if the given {@link CompositeRequest} is in a valid state to be further processed and addressed to the
	 * delegates.
	 * 
	 * <p>
	 * A {@link ServiceProcessorAddressingException} is thrown if {@link CompositeRequest#getRequests()}:
	 * 
	 * <ul>
	 * <li>Is empty.
	 * <li>Contains {@code null} entries.
	 * <li>Contains other {@code CompositeRequests} with circular references.
	 * </ul>
	 * 
	 * <p>
	 * Nested {@code CompositeRequests} are validated in the same manner.
	 * 
	 * @param compositeRequest
	 *            The {@link CompositeRequest} to be validated.
	 * @throws ServiceProcessorAddressingException
	 *             If the given {@link CompositeRequest} is invalid.
	 */
	protected void validate(CompositeRequest compositeRequest) throws ServiceProcessorAddressingException {
		validate(compositeRequest, new HashSet<CompositeRequest>());
	}

	private void validate(CompositeRequest compositeRequest, Set<CompositeRequest> outerReferences) {
		
		List<ServiceRequest> requests = compositeRequest.getRequests();
		
		if (requests == null || requests.isEmpty()) {
			throw new IllegalArgumentException("CompositeRequest.getRequests() is null or empty");
		}

		if (!outerReferences.add(compositeRequest)) {
			throw new IllegalArgumentException(
					"There are circular references in the nested composite requests. The following request instance is already present in another level: "
							+ compositeRequest);
		}

		for (ServiceRequest serviceRequest : compositeRequest.getRequests()) {
			if (serviceRequest == null) {
				throw new IllegalArgumentException("CompositeRequest.getRequests() contains a null entry");
			}
			if (serviceRequest instanceof CompositeRequest) {
				validate((CompositeRequest) serviceRequest, outerReferences);
			}
		}

		outerReferences.remove(compositeRequest);

	}

	private static Throwable getSingleThrowable(List<ServiceRequest> requests, Integer totalFailures, Throwable[] failures,
			CompositeResponse compositeResult, Integer totalResults) {

		if (totalFailures == 1) {
			return getFirstNonNull(failures);
		}

		Integer totalRequests = requests.size();
		Integer totalSkipped = totalRequests - totalResults - totalFailures;

		StringBuilder message = new StringBuilder();
		message.append(String.format(compositeExceptionMessasge, totalRequests, totalResults, totalFailures, totalSkipped));

		List<Throwable> suppressed = new ArrayList<>(totalFailures);

		for (int i = 0; i < requests.size(); i++) {

			String typeSignature = null;

			ServiceRequest request = requests.get(i);
			if (request != null) {
				typeSignature = request.entityType().getTypeSignature();
			} else {
				typeSignature = "null";
			}

			message.append(String.format(compositeExceptionMessasgeLine, i, typeSignature));

			ServiceResult result = totalResults == 0 ? null : compositeResult.getResults().get(i);
			Throwable failure = result != null ? null : failures[i];

			if (result != null) {
				message.append("successfully processed: ").append(result);
			} else if (failure != null) {
				message.append("failed: ").append(failure.getClass().getSimpleName()).append(": ").append(failure.getMessage());
				suppressed.add(failure);
			} else {
				message.append("skipped");
			}
		}

		CompositeException exception = new CompositeException(message.toString());
		suppressed.forEach(exception::addSuppressed);
		return exception;

	}

	private static <T> T getFirstNonNull(T[] array) {
		for (T t : array) {
			if (t != null) {
				return t;
			}
		}
		return null;
	}

}
