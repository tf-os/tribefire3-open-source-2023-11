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
package tribefire.extension.tracing.connector.logging;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;
import com.braintribe.model.logging.LogLevel;
import com.braintribe.utils.logging.LogLevels;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import tribefire.extension.tracing.connector.api.AbstractTracingConnector;

/**
 *
 */
public class LoggingTracingConnector extends AbstractTracingConnector {

	private final static Logger logger = Logger.getLogger(LoggingTracingConnector.class);

	private tribefire.extension.tracing.model.deployment.connector.LoggingTracingConnector deployable;

	private OpenTelemetrySdk openTelemetry;

	private Tracer tracer;
	
	private SdkTracerProvider tracerProvider;
	
	private LogLevel logLevel;
	
	private boolean logAttributes;

	// -----------------------------------------------------------------------
	// LifecycleAware
	// -----------------------------------------------------------------------

	// -----------------------------------------------------------------------
	// METHODS
	// -----------------------------------------------------------------------

	@Override
	public void initialize() {
		
		Resource resource = Resource.getDefault()
				.merge(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, serviceName)));
		
		tracerProvider = SdkTracerProvider
				.builder()
				.setResource(resource)
				.addSpanProcessor(BatchSpanProcessor.builder(new ConfigurableLoggingExporter(LogLevels.convert(logLevel), logAttributes)).build())
				.build();

		openTelemetry = OpenTelemetrySdk
				.builder()
				.setTracerProvider(tracerProvider)
				.build();

		tracer = openTelemetry
				.getTracer(serviceName);

		// it's always a good idea to shut down the SDK cleanly at JVM exit.
		Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::close));

		logger.info(() -> "Setup logging tracer");
	}

	@Override
	public OpenTelemetry openTelemetry() {
		return openTelemetry;
	}

	@Override
	public Tracer tracer() {
		return tracer;
	}
	
	@Override
	public SdkTracerProvider tracerProvider() {
		return tracerProvider;
	}

	@Override
	public CheckResultEntry actualHealth() {
		CheckResultEntry entry = CheckResultEntry.T.create();

		entry.setCheckStatus(CheckStatus.ok);
		entry.setName(deployable.getName() + " (TYPE: '" + deployable.entityType().getShortName() + "')");
		entry.setMessage("(" + deployable.getGlobalId() + ")");

		if (tracer() == null) {
			entry.setCheckStatus(CheckStatus.fail);
			entry.setName("Tracer is not set");
		}

		return entry;
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------
	
	@Configurable
	@Required
	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}
	
	@Configurable
	public void setLogAttributes(boolean logAttributes) {
		this.logAttributes = logAttributes;
	}

	@Required
	@Configurable
	public void setDeployable(tribefire.extension.tracing.model.deployment.connector.LoggingTracingConnector deployable) {
		this.deployable = deployable;
	}

}
