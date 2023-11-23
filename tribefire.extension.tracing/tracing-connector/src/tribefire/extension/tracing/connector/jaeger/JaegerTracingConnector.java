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
package tribefire.extension.tracing.connector.jaeger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import tribefire.extension.tracing.connector.api.AbstractTracingConnector;
import tribefire.extension.tracing.model.deployment.connector.AgentSenderConfiguration;
import tribefire.extension.tracing.model.deployment.connector.ConstantSampler;
import tribefire.extension.tracing.model.deployment.connector.HttpSenderConfiguration;
import tribefire.extension.tracing.model.deployment.connector.ProbabilisticSampler;
import tribefire.extension.tracing.model.deployment.connector.RateLimitingSampler;

/**
 *
 */
public class JaegerTracingConnector extends AbstractTracingConnector {

	private final static Logger logger = Logger.getLogger(JaegerTracingConnector.class);

	private tribefire.extension.tracing.model.deployment.connector.JaegerTracingConnector deployable;
	
	private Tracer tracer;
	
	private SdkTracerProvider tracerProvider;
	
	private OpenTelemetry openTelemetry;

	// -----------------------------------------------------------------------
	// LifecycleAware
	// -----------------------------------------------------------------------

	// -----------------------------------------------------------------------
	// METHODS
	// -----------------------------------------------------------------------

	@Override
	public void initialize() {
		closeTracer();
		tribefire.extension.tracing.model.deployment.connector.SamplerConfiguration samplerConfiguration = deployable.getSamplerConfiguration();

		Sampler sampler;
		if (samplerConfiguration instanceof ConstantSampler) {
			sampler = Sampler.alwaysOn();
		} else if (samplerConfiguration instanceof ProbabilisticSampler) {
			ProbabilisticSampler probabilisticSampler = (ProbabilisticSampler) samplerConfiguration;
			double probability = probabilisticSampler.getProbability();
			
			sampler = Sampler.traceIdRatioBased(probability);
		} else if (samplerConfiguration instanceof RateLimitingSampler) {
			RateLimitingSampler rateLimitingSampler = (RateLimitingSampler) samplerConfiguration;
			double rate = rateLimitingSampler.getRate();

			sampler = Sampler.alwaysOn();
			// TODO implement this sampler, for now it's always on
		} else {
			throw new IllegalArgumentException("'" + tribefire.extension.tracing.model.deployment.connector.SamplerConfiguration.T.getShortName()
					+ "': '" + samplerConfiguration.entityType().getTypeName() + "' not supported");
		}

		tribefire.extension.tracing.model.deployment.connector.ReporterConfiguration reporterConfiguration = deployable.getReporterConfiguration();
		tribefire.extension.tracing.model.deployment.connector.SenderConfiguration senderConfiguration = reporterConfiguration
				.getSenderConfiguration();

		String endpoint;
		if (senderConfiguration instanceof HttpSenderConfiguration) {
			HttpSenderConfiguration httpSenderConfiguration = (HttpSenderConfiguration) senderConfiguration;
			endpoint = httpSenderConfiguration.getEndpoint();

			try {
				// This is here only to check if the endpoint is correct
				@SuppressWarnings("unused")
				URL url = new URL(endpoint);
			} catch (MalformedURLException e) {
				throw Exceptions.unchecked(e, "Could not parse endpoint: '" + endpoint + "' as http sender configuration");
			}
		} else if (senderConfiguration instanceof AgentSenderConfiguration) {
			AgentSenderConfiguration agentSenderConfiguration = (AgentSenderConfiguration) senderConfiguration;
			String host = agentSenderConfiguration.getHost();
			int port = agentSenderConfiguration.getPort();
			
			// TODO does the host include "http"/"https"?
			endpoint = host + port;
		} else {
			throw new IllegalArgumentException("'" + tribefire.extension.tracing.model.deployment.connector.SenderConfiguration.T.getShortName()
					+ "': '" + senderConfiguration.entityType().getTypeName() + "' not supported");
		}

		OtlpGrpcSpanExporter jaegerOtlpExporter = OtlpGrpcSpanExporter
				.builder()
				.setEndpoint(endpoint)
				.build();
		
		SpanProcessor spanProcessor = BatchSpanProcessor.builder(jaegerOtlpExporter)
				.setScheduleDelay(Duration.ofMillis(reporterConfiguration.getFlushInterval()))
				.setMaxQueueSize(reporterConfiguration.getMaxQueueSize())
		.build();
		
		Resource resource = Resource.getDefault()
				.merge(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, serviceName)));

		tracerProvider = SdkTracerProvider
				.builder()
				.addSpanProcessor(spanProcessor)
				.setSampler(sampler)
				.setResource(resource)
				.build();
		
		// This is needed otherwise the injext/extract functions default to NoopTextMapPropagator
		ContextPropagators contextPropagator = ContextPropagators
				.create(W3CTraceContextPropagator.getInstance());

		openTelemetry = OpenTelemetrySdk.builder()
				.setTracerProvider(tracerProvider)
				.setPropagators(contextPropagator)
				.build();
	    
	    tracer = openTelemetry
	    		.getTracer(serviceName);

		// it's always a good idea to shut down the SDK cleanly at JVM exit.
		Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::close));

		logger.info(() -> "Successfully setup jaeger tracer");

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

		if (tracer() == null) {
			entry.setCheckStatus(CheckStatus.fail);
			entry.setName("Tracer is not set");
		}
		entry.setName(deployable.getName() + " (TYPE: '" + deployable.entityType().getShortName() + "')");
		entry.setMessage("(" + deployable.getGlobalId() + ")");

		tribefire.extension.tracing.model.deployment.connector.SenderConfiguration senderConfiguration = deployable.getReporterConfiguration()
				.getSenderConfiguration();
		if (senderConfiguration instanceof HttpSenderConfiguration) {
			HttpSenderConfiguration httpSenderConfiguration = (HttpSenderConfiguration) senderConfiguration;
			String endpoint = httpSenderConfiguration.getEndpoint();

			boolean success = pingCollector(endpoint);
			if (!success) {
				entry.setCheckStatus(CheckStatus.fail);
				entry.setDetails("Could not reach jaeger collector endpoint: '" + endpoint + "'");

			} else {
				entry.setDetails("Successfully reached jaeger collector endpoint: '" + endpoint + "'");
			}
		} else if (senderConfiguration instanceof AgentSenderConfiguration) {
			AgentSenderConfiguration agentSenderConfiguration = (AgentSenderConfiguration) senderConfiguration;
			String host = agentSenderConfiguration.getHost();
			int port = agentSenderConfiguration.getPort();

			boolean success = pingAgent(host, port);
			if (!success) {
				entry.setCheckStatus(CheckStatus.fail);
				entry.setDetails("Could not reach jaeger agent host: '" + host + "' port: '" + port + "'");
			}
		} else {
			throw new IllegalArgumentException("'" + tribefire.extension.tracing.model.deployment.connector.SenderConfiguration.T.getShortName()
					+ "': '" + senderConfiguration.entityType().getTypeName() + "' not supported");
		}

		return entry;
	}

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

	private boolean pingCollector(String endpoint) {
		try {
			HttpPost post = new HttpPost(endpoint);

			post.addHeader("Content-Type", "application/x-thrift");

			try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(post)) {
				response.getEntity();
				logger.debug(() -> "Successfully jaeger pinged collector endpoint: '" + endpoint + "'");
			}
			return true;
		} catch (Exception e) {
			logger.debug(() -> "Could not ping collector endpoint: '" + endpoint + "' - " + e.getMessage() + "' - details on trace");
			logger.trace(() -> "Could not ping collector endpoint: '" + endpoint + "' - " + e.getMessage(), e);
			return false;
		}
	}

	private boolean pingAgent(String host, int port) {
		try {
			String msg = "ping";
			byte[] buf = msg.getBytes();
			try (DatagramSocket socket = new DatagramSocket()) {
				DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(host), port);
				socket.send(packet);
				logger.debug(() -> "Successfully pinged jaeger agent host: '" + host + "' port: '" + port + "'");
				return true;
			}
		} catch (Exception e) {
			logger.debug(() -> "Could not ping collector host: '" + host + "' port: '" + port + "' - " + e.getMessage() + "' - details on trace");
			logger.trace(() -> "Could not ping collector host: '" + host + "' port: '" + port + "' - " + e.getMessage(), e);
			return false;
		}
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Required
	@Configurable
	public void setDeployable(tribefire.extension.tracing.model.deployment.connector.JaegerTracingConnector deployable) {
		this.deployable = deployable;
	}

}
