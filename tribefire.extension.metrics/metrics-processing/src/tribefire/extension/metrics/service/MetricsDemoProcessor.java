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
package tribefire.extension.metrics.service;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.logging.LogLevel;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.utils.logging.LogLevels;

import tribefire.extension.metrics.model.service.test.MetricsDemoService;
import tribefire.extension.metrics.model.service.test.MetricsDemoServiceResult;

public class MetricsDemoProcessor implements ServiceProcessor<MetricsDemoService, MetricsDemoServiceResult>, LifecycleAware {

	private static final Logger logger = Logger.getLogger(MetricsDemoProcessor.class);

	private LogLevel logLevel;

	// -----------------------------------------------------------------------
	// LifecycleAware
	// -----------------------------------------------------------------------

	@Override
	public void postConstruct() {
		// TODO Auto-generated method stub

	}

	@Override
	public void preDestroy() {
		// TODO Auto-generated method stub

	}

	// -----------------------------------------------------------------------
	// Service
	// -----------------------------------------------------------------------

	@Override
	public MetricsDemoServiceResult process(ServiceRequestContext requestContext, MetricsDemoService request) {
		logger.log(LogLevels.convert(logLevel),
				() -> "Executing '" + this.getClass().getSimpleName() + "' - '" + request.type().getTypeName() + "'...");

		try {
			long minDuration = request.getMinDuration();
			long maxDuration = request.getMaxDuration();

			long duration = minDuration + (long) (Math.random() * (maxDuration - minDuration));

			Thread.sleep(duration);
		} catch (InterruptedException e) {
			throw Exceptions.unchecked(e);
		}

		LogLevel logLevel = request.getLogLevel();
		String message = request.getMessage();

		if (message != null) {
			logger.log(LogLevels.convert(logLevel), message);
		}

		if (request.getThrowException()) {
			throw new IllegalStateException("THIS IS BY INTENTION! Reached demo service for request: '" + request + "' to throw an exception!");
		}

		MetricsDemoServiceResult result = MetricsDemoServiceResult.T.create();
		logger.log(LogLevels.convert(logLevel),
				() -> "Finished executing '" + this.getClass().getSimpleName() + "' - '" + request.type().getTypeName() + "'");

		return result;
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
