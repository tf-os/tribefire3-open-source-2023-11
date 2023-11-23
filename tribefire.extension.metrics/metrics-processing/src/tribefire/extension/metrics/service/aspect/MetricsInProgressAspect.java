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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.service.api.ProceedContext;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.lcd.CommonTools;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Gauge.Builder;
import tribefire.extension.metrics.connector.api.MetricsConnector;

public class MetricsInProgressAspect extends MetricsAspect implements InitializationAware {

	private final static Logger logger = Logger.getLogger(MetricsInProgressAspect.class);

	private String baseUnit;

	private Map<String, AtomicInteger> map;

	// -----------------------------------------------------------------------
	// InitializationAware
	// -----------------------------------------------------------------------

	@Override
	public void postConstruct() {
		map = new HashMap<>();
	}

	// -----------------------------------------------------------------------
	// METHODS
	// -----------------------------------------------------------------------

	@Override
	public Object process(ServiceRequestContext requestContext, ServiceRequest request, ProceedContext proceedContext) {
		boolean metricsActive = true;

		if (metricsActive) {
			logger.trace(() -> "Metrics enabled for request: '" + request + "'");

			String typeSignature = request.entityType().getTypeSignature();

			// fetch actual gauge value per type signature
			AtomicInteger value = map.get(typeSignature);
			if (value == null) {
				synchronized (this) {
					value = map.get(typeSignature);
					if (value == null) {
						value = new AtomicInteger();
						map.put(typeSignature, value);
						for (MetricsConnector metricsConnector : metricsConnectors) {
							fetchGauge(metricsConnector, request, tagsSuccess, value);
						}
					}
				}
			}
			value.incrementAndGet();
			try {
				Object result = proceedContext.proceed(request);
				value.decrementAndGet();
				return result;
			} catch (Throwable t) {
				value.decrementAndGet();
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

	private void fetchGauge(MetricsConnector metricsConnector, ServiceRequest request, String[] tags, AtomicInteger value) {
		//@formatter:off
		Builder<Supplier<Number>> builder = Gauge
				.builder(name, () -> value)
			    .tags(enrichTags(request, tags));
		//@formatter:on

		if (!CommonTools.isEmpty(description)) {
			builder.description(description);
		}
		if (!CommonTools.isEmpty(baseUnit)) {
			builder.baseUnit(baseUnit);
		}
		builder.register(metricsConnector.registry());
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Configurable
	public void setBaseUnit(String baseUnit) {
		this.baseUnit = baseUnit;
	}

}
