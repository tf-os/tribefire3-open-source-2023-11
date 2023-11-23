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
package tribefire.extension.metrics.service.aspect;

import java.time.Duration;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.service.api.ProceedContext;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.lcd.CommonTools;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Builder;
import tribefire.extension.metrics.connector.api.MetricsConnector;

//TODO: add long task timer
public class MetricsTimerAspect extends MetricsAspect {

	private final static Logger logger = Logger.getLogger(MetricsTimerAspect.class);

	@Override
	public Object process(ServiceRequestContext requestContext, ServiceRequest request, ProceedContext proceedContext) {
		boolean metricsActive = true;

		if (metricsActive) {
			logger.trace(() -> "Metrics enabled for request: '" + request + "'");

			Object result = null;
			long start = System.currentTimeMillis();
			try {

				result = proceedContext.proceed(request);

				metricsConnectors.forEach(metricsConnector -> {
					Timer timer = fetchTimer(metricsConnector, request, tagsSuccess);

					timer.record(Duration.ofMillis(System.currentTimeMillis() - start));
				});

				return result;
			} catch (Throwable t) {
				metricsConnectors.forEach(metricsConnector -> {
					Timer timer = fetchTimer(metricsConnector, request, tagsError);

					timer.record(Duration.ofMillis(System.currentTimeMillis() - start));
				});
				throw t;
			}
		} else {
			logger.trace(() -> "Metrics disabled for request: '" + request + "'");
			Object result = proceedContext.proceed(request);
			return result;
		}
	}

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

	private Timer fetchTimer(MetricsConnector metricsConnector, ServiceRequest request, String[] tags) {
		//@formatter:off
		Builder builder = Timer
				.builder(name)
				.tags(enrichTags(request, tags));
		//@formatter:on

		if (!CommonTools.isEmpty(description)) {
			builder.description(description);
		}

		// TODO: many other functionalities like percentiles, sla,...

		Timer timer = builder.register(metricsConnector.registry());

		return timer;
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

}
