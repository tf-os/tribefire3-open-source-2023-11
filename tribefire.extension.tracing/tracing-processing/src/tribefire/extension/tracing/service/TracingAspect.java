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
package tribefire.extension.tracing.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.service.api.ProceedContext;
import com.braintribe.model.processing.service.api.ServiceAroundProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.api.ServiceRequestContextBuilder;
import com.braintribe.model.service.api.ServiceRequest;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;
import tribefire.extension.tracing.connector.api.TracingConnector;

public class TracingAspect implements ServiceAroundProcessor<ServiceRequest, Object>, TracingServiceConstants {

	private final static Logger logger = Logger.getLogger(TracingAspect.class);

	private TracingConnector tracingConnector;

	// -----------------------------------------------------------------------
	// AROUND ASPECT
	// -----------------------------------------------------------------------

	@Override
	public Object process(ServiceRequestContext requestContext, ServiceRequest request, ProceedContext proceedContext) {

		boolean tracingActive = tracingConnector.tracingActive(request, requestContext);

		if (tracingActive) {
			// nothing before this point
			long start = System.currentTimeMillis();

			logger.trace(() -> "Tracing enabled for request: '" + request + "'");

			Tracer tracer = tracingConnector.tracer();

			String entityType = request.entityType().getTypeName();
			String operationName = entityType;

			ServiceRequestContextBuilder serviceRequestContextBuilder = requestContext.derive();
			Map<String, String> tracingInformationMap = new HashMap<>();
			Optional<Map<String, String>> tracingInformation = requestContext.getAspect(TracingInformation.class);
			if (tracingInformation.isPresent()) {
				tracingInformationMap = tracingInformation.get();
			}

			Map<String, String> map = new HashMap<>(tracingInformationMap);
			
			OpenTelemetry openTelemetry = tracingConnector.openTelemetry();

			// TODO Not sure if Context.current() works with multiple repositories 
			// Ticket raised (D1-3800) check it later on
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
				tracingConnector.addAttributesBeforeExecution(spanBuilder, request, requestContext, ATTRIBUTE_VALUE_TYPE_PARENT);

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
				tracingConnector.addAttributesBeforeExecution(spanBuilder, request, requestContext, ATTRIBUTE_VALUE_TYPE_CHILD);
				span = spanBuilder.startSpan();
			}

			serviceRequestContextBuilder.set(TracingInformation.class, map);

			long startService = Long.MIN_VALUE;
			Object result = null;
			try (Scope scope = span.makeCurrent()) {
				startService = System.currentTimeMillis();
				// nothing after this point except the real service execution
				result = proceedContext.proceed(serviceRequestContextBuilder.build(), request);

				// nothing time consuming here!

				return result;
			} catch (Throwable t) {
				tracingConnector.addAttributesErrorExecution(span, t);
				throw t;
			} finally {
				long end = System.currentTimeMillis();

				long serviceDuration = end - startService;
				long tracingOverhead = startService - start;

				// -------------------

				logger.debug(() -> {
					return "Finished executing of: '" + TracingAspect.class.getName() + "' with request: '" + request.entityType().getTypeName()
							+ "' in '" + tracingOverhead + "'ms";
				});
				tracingConnector.addAttributesAfterExecution(span, result, serviceDuration, tracingOverhead);
				if (span != null) {
					span.end();
				}
			}
		} else {
			logger.trace(() -> "Tracing disabled for request: '" + request + "'");
			Object result = proceedContext.proceed(request);
			return result;
		}
	}

	// -----------------------------------------------------------------------
	// HELPER
	// -----------------------------------------------------------------------

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Required
	@Configurable
	public void setTracingConnector(TracingConnector tracingConnector) {
		this.tracingConnector = tracingConnector;
	}

}
