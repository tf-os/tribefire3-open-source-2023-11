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
package tribefire.extension.tracing.service.demo;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.logging.LogLevel;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.utils.logging.LogLevels;

import tribefire.extension.tracing.model.service.demo.DemoTracing;
import tribefire.extension.tracing.model.service.demo.DemoTracingRequest;
import tribefire.extension.tracing.model.service.demo.DemoTracingResponse;
import tribefire.extension.tracing.model.service.demo.DemoTracingResult;

/**
 * Processor to demo tracing capabilities
 * 
 *
 */
public class DemoTracingProcessor extends AbstractDispatchingServiceProcessor<DemoTracingRequest, DemoTracingResponse> {

	private static final Logger logger = Logger.getLogger(DemoTracingProcessor.class);

	private LogLevel logLevel;

	// -----------------------------------------------------------------------
	// DISPATCHING
	// -----------------------------------------------------------------------

	@Override
	protected void configureDispatching(DispatchConfiguration<DemoTracingRequest, DemoTracingResponse> dispatching) {
		dispatching.register(DemoTracing.T, this::demoTracing);
	}

	// -----------------------------------------------------------------------
	// METHODS
	// -----------------------------------------------------------------------

	public DemoTracingResult demoTracing(ServiceRequestContext requestContext, DemoTracing request) {
		logger.log(LogLevels.convert(logLevel),
				() -> "Executing '" + this.getClass().getSimpleName() + "' - '" + request.type().getTypeName() + "'...");

		waiting(request.getBeforeDuration());
		handleRequest(request);

		boolean waitToFinish = request.getWaitToFinish();
		boolean executeParallel = request.getExecuteParallel();
		List<DemoTracing> nestedTracings = request.getNestedTracings();
		CountDownLatch countDownLatch = new CountDownLatch(nestedTracings.size());
		for (DemoTracing nestedTracing : nestedTracings) {
			if (executeParallel) {
				nestedTracing.eval(requestContext).get(new AsyncCallback<DemoTracingResponse>() {

					@Override
					public void onSuccess(DemoTracingResponse future) {
						countDownLatch.countDown();
						logger.info("Successfully finished demo execution with response: '" + future + "'");
					}

					@Override
					public void onFailure(Throwable t) {
						countDownLatch.countDown();
						logger.info("Error while executing demo: '" + t + "'");
					}
				});
			} else {
				nestedTracing.eval(requestContext).get();
			}
		}

		waiting(request.getAfterDuration());

		if (waitToFinish) {
			try {
				countDownLatch.await(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		DemoTracingResult result = DemoTracingResult.T.create();
		logger.log(LogLevels.convert(logLevel),
				() -> "Finished executing '" + this.getClass().getSimpleName() + "' - '" + request.type().getTypeName() + "'");
		return result;
	}

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

	private void handleRequest(DemoTracingRequest request) {

		LogLevel logLevel = request.getLogLevel();
		String message = request.getMessage();

		if (message != null) {
			logger.log(LogLevels.convert(logLevel), message);
		}

		if (request.getThrowException()) {
			throw new IllegalStateException("THIS IS BY INTENTION! Reached demo service for request: '" + request + "' to throw an exception!");
		}
	}

	private void waiting(long duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			throw Exceptions.unchecked(e);
		}
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Configurable
	@Required
	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

}
