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
package tribefire.extension.tracing.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.api.ServiceRequestContextBuilder;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;
import tribefire.extension.tracing.connector.api.TracingConnector;
import tribefire.extension.tracing.service.TracingInformation;
import tribefire.extension.tracing.service.TracingServiceConstants;

public class ManualTracing<T> implements TracingServiceConstants {

	private TracingConnector tracingConnector;

	private String operationName = "manual";

	// -----------------------------------------------------------------------
	// METHODS
	// -----------------------------------------------------------------------

	public void withTracing(Runnable runnable, ServiceRequestContext requestContext) {
		Objects.requireNonNull(tracingConnector, "TracingConnector needs to be set");

		long start = System.currentTimeMillis();

		Tracer tracer = tracingConnector.tracer();

		ServiceRequestContextBuilder serviceRequestContextBuilder = requestContext.derive();
		Map<String, String> _map = new HashMap<>();
		Optional<Map<String, String>> tracingInformation = requestContext.getAspect(TracingInformation.class);
		if (tracingInformation.isPresent()) {
			_map = tracingInformation.get();
		}

		Map<String, String> map = new HashMap<>(_map);
		
		OpenTelemetry openTelemetry = GlobalOpenTelemetry.get();
		
		Context context = openTelemetry.getPropagators()
		.getTextMapPropagator()
		.extract(Context.current(), map, new TextMapGetter<>() {

			@Override
			public Iterable<String> keys(Map<String, String> carrier) {
				return carrier.keySet();
			}

			@Override
			public String get(Map<String, String> carrier, String key) {
				return carrier.get(key);
			}
		});
		
		SpanBuilder spanBuilder = tracer.spanBuilder(operationName);
		Span span = null;

		// If it's a root context, then the traceId contains zeros only, ie not valid -> it's a parent
		boolean isValid = Span.fromContext(context).getSpanContext().isValid();

		if (!isValid) {
			// let's start the parent span
			
			spanBuilder.setNoParent();
			tracingConnector.addAttributesBeforeExecution(spanBuilder, requestContext, ATTRIBUTE_VALUE_TYPE_PARENT_MANUAL);

			span = spanBuilder.startSpan();
			
			openTelemetry.getPropagators()
			.getTextMapPropagator()
			.inject(Context.current(), map, new TextMapSetter<>() {

				@Override
				public void set(Map<String, String> carrier, String key, String value) {
					carrier.put(key, value);
				}
				
			});

		} else {
			tracingConnector.addAttributesBeforeExecution(spanBuilder, requestContext, ATTRIBUTE_VALUE_TYPE_CHILD_MANUAL);
			span = spanBuilder.startSpan();
		}

		serviceRequestContextBuilder.set(TracingInformation.class, map);

		requestContext = serviceRequestContextBuilder.build();

		long startService = Long.MIN_VALUE;
		try {
			runnable.run();
		} catch (Throwable t) {
			tracingConnector.addAttributesErrorExecution(span, t);
			// nothing time consuming here!
			throw t;
		} finally {
			long end = System.currentTimeMillis();

			long serviceDuration = end - startService;
			long tracingOverhead = startService - start;

			tracingConnector.addAttributesAfterExecution(span, serviceDuration, tracingOverhead);
			if (span != null) {
				span.end();
			}
		}
	}

	public T withTracing(@SuppressWarnings("unused") Supplier<T> s, @SuppressWarnings("unused") ServiceRequestContext requestContext) {
		Objects.requireNonNull(tracingConnector, "TracingConnector needs to be set");
		// TODO: not implemented yet
		return null;
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Configurable
	@Required
	public void setTracingConnector(TracingConnector tracingConnector) {
		this.tracingConnector = tracingConnector;
	}
}
