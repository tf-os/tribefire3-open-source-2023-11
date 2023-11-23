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
package tribefire.extension.tracing.connector.api;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.notification.Level;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.time.TimeSpan;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import tribefire.extension.tracing.model.deployment.service.DefaultAttribute;

/**
 *
 */
public interface TracingConnector {

	public CheckResultEntry health();

	// -----------------------------------------------------------------------
	// MISC
	// -----------------------------------------------------------------------

	public void initialize();

	boolean tracingEnabled();

	Date disableTracingAt();

	public OpenTelemetry openTelemetry();

	public Tracer tracer();
	
	public SdkTracerProvider tracerProvider();

	boolean tracingActive(ServiceRequest request, ServiceRequestContext requestContext);

	// -----------------------------------------------------------------------
	// CONFIGURE TRACING
	// -----------------------------------------------------------------------

	void enableTracing(TimeSpan enableDuration);

	void disableTracing();

	void changeComponentName(String _componentName);

	void changeAddAttributesFromNotificationsMessage(Level _addAttributesFromNotificationsMessage);

	void changeAddAttributesFromNotificationsDetailsMessage(Level addAttributesFromNotificationsDetailsMessage);

	void populateDefaultAttributes(Set<DefaultAttribute> _attributes);

	void populateCustomAttributes(Map<String, String> _customAttributesRegistry);

	void populateEntityTypeInclusions(Set<String> _entityTypeInclusions);

	void populateEntityTypeHierarchyInclusions(Set<String> _entityTypeHierarchyInclusions);

	void populateEntityTypeExclusions(Set<String> _entityTypeExclusions);

	void populateEntityTypeHierarchyExclusions(Set<String> _entityTypeHierarchyExclusions);

	void populateUserInclusions(Set<String> _userInclusions);

	void populateUserExclusions(Set<String> _userExclusions);

	// -----------------------------------------------------------------------
	// ATTACHING ATTRIBUTES
	// -----------------------------------------------------------------------

	void addAttributesBeforeExecution(SpanBuilder spanBuilder, ServiceRequestContext requestContext, String type);

	void addAttributesBeforeExecution(SpanBuilder spanBuilder, ServiceRequest request, ServiceRequestContext requestContext, String type);

	void addAttributesErrorExecution(Span span, Throwable t);

	void addAttributesAfterExecution(Span span, Long duration, Long tracingOverhead);

	void addAttributesAfterExecution(Span span, Object result, Long duration, Long tracingOverhead);

	// -----------------------------------------------------------------------
	// CONFIGURE TRACING
	// -----------------------------------------------------------------------

	default InMemorySpanExporter spanExporter() {
		throw new IllegalStateException("Only to be used by JaegerInMemoryConnector");
	}

}