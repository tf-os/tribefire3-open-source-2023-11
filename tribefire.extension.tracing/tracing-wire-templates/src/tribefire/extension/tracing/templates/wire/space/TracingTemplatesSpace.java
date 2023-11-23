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
package tribefire.extension.tracing.templates.wire.space;

import com.braintribe.logging.Logger;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.CommonTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.tracing.model.deployment.connector.AgentSenderConfiguration;
import tribefire.extension.tracing.model.deployment.connector.ConstantSampler;
import tribefire.extension.tracing.model.deployment.connector.HttpSenderConfiguration;
import tribefire.extension.tracing.model.deployment.connector.JaegerInMemoryTracingConnector;
import tribefire.extension.tracing.model.deployment.connector.JaegerTracingConnector;
import tribefire.extension.tracing.model.deployment.connector.LoggingTracingConnector;
import tribefire.extension.tracing.model.deployment.connector.ProbabilisticSampler;
import tribefire.extension.tracing.model.deployment.connector.RateLimitingSampler;
import tribefire.extension.tracing.model.deployment.connector.ReporterConfiguration;
import tribefire.extension.tracing.model.deployment.connector.SamplerConfiguration;
import tribefire.extension.tracing.model.deployment.connector.TracingConnector;
import tribefire.extension.tracing.model.deployment.service.TracingAspect;
import tribefire.extension.tracing.model.deployment.service.TracingProcessor;
import tribefire.extension.tracing.model.deployment.service.demo.DemoTracingProcessor;
import tribefire.extension.tracing.templates.api.TracingTemplateContext;
import tribefire.extension.tracing.templates.api.connector.TracingTemplateConnectorContext;
import tribefire.extension.tracing.templates.api.connector.TracingTemplateInMemoryJaegerConnectorContext;
import tribefire.extension.tracing.templates.api.connector.TracingTemplateJaegerConnectorContext;
import tribefire.extension.tracing.templates.api.connector.TracingTemplateLoggingConnectorContext;
import tribefire.extension.tracing.templates.api.connector.reporterconfiguration.TracingTemplateReporterConfigurationContext;
import tribefire.extension.tracing.templates.api.connector.samplerconfiguration.TracingTemplateConstantSamplerContext;
import tribefire.extension.tracing.templates.api.connector.samplerconfiguration.TracingTemplateProbabilisticSamplerContext;
import tribefire.extension.tracing.templates.api.connector.samplerconfiguration.TracingTemplateRateLimitingSamplerContext;
import tribefire.extension.tracing.templates.api.connector.samplerconfiguration.TracingTemplateSamplerConfigurationContext;
import tribefire.extension.tracing.templates.api.connector.senderconfiguration.TracingTemplateAgentSenderConfigurationContext;
import tribefire.extension.tracing.templates.api.connector.senderconfiguration.TracingTemplateHttpSenderConfigurationContext;
import tribefire.extension.tracing.templates.api.connector.senderconfiguration.TracingTemplateSenderConfigurationContext;
import tribefire.extension.tracing.templates.util.TracingTemplateUtil;
import tribefire.extension.tracing.templates.wire.contract.BasicInstancesContract;
import tribefire.extension.tracing.templates.wire.contract.TracingTemplatesContract;

/**
 *
 */
@Managed
public class TracingTemplatesSpace implements WireSpace, TracingTemplatesContract {

	private static final Logger logger = Logger.getLogger(TracingTemplatesSpace.class);

	@Import
	private BasicInstancesContract basicInstances;

	@Import
	private TracingMetaDataSpace tracingMetaData;

	@Override
	public void setupTracing(TracingTemplateContext context) {
		if (context == null) {
			throw new IllegalArgumentException("The TracingTemplateContext must not be null.");
		}
		logger.debug(() -> "Configuring TRACING based on:\n" + StringTools.asciiBoxMessage(context.toString(), -1));

		// processing
		tracingServiceProcessor(context);
		if (context.getAddDemo()) {
			demoTracingProcessor(context);
		}

		// aspect
		tracingAspect(context);

		// metadata
		tracingMetaData.metaData(context);
	}

	// -----------------------------------------------------------------------
	// PROCESSOR
	// -----------------------------------------------------------------------

	@Override
	@Managed
	public TracingProcessor tracingServiceProcessor(TracingTemplateContext context) {
		TracingProcessor bean = context.create(TracingProcessor.T, InstanceConfiguration.currentInstance());
		bean.setModule(context.getTracingModule());
		bean.setAutoDeploy(true);

		bean.setName(TracingTemplateUtil.resolveContextBasedDeployableName("TRACING Service Processor", context));

		bean.setTracingConnector(tracingConnector(context));

		return bean;
	}

	@Override
	@Managed
	public DemoTracingProcessor demoTracingProcessor(TracingTemplateContext context) {
		DemoTracingProcessor bean = context.create(DemoTracingProcessor.T, InstanceConfiguration.currentInstance());
		bean.setModule(context.getTracingModule());
		bean.setAutoDeploy(true);

		bean.setName(TracingTemplateUtil.resolveContextBasedDeployableName("TRACING Demo Service Processor", context));

		return bean;
	}

	// -----------------------------------------------------------------------
	// CONNECTOR
	// -----------------------------------------------------------------------

	@Override
	public TracingConnector tracingConnector(TracingTemplateContext context) {
		TracingTemplateConnectorContext connectorContext = context.getConnectorContext();

		TracingConnector bean = null;
		if (connectorContext instanceof TracingTemplateJaegerConnectorContext) {
			bean = jaegerTracingConnector(context);
		} else if (connectorContext instanceof TracingTemplateInMemoryJaegerConnectorContext) {
			bean = jaegerInMemoryTracingConnector(context);
		} else if (connectorContext instanceof TracingTemplateLoggingConnectorContext) {
			bean = loggingTracingConnector(context);
		} else {
			throw new IllegalArgumentException("Tracing connectorContext: '" + connectorContext + "' not supported or is not set");
		}

		bean.setEntityTypeInclusions(connectorContext.getEntityTypeInclusions());
		bean.setEntityTypeHierarchyInclusions(connectorContext.getEntityTypeHierarchyInclusions());
		bean.setEntityTypeInclusions(connectorContext.getEntityTypeExclusions());
		bean.setEntityTypeHierarchyExclusions(connectorContext.getEntityTypeHierarchyExclusions());
		bean.setUserInclusions(connectorContext.getUserInclusions());
		bean.setUserExclusions(connectorContext.getUserExclusions());
		bean.setDefaultAttributes(connectorContext.getDefaultAttributes());
		bean.setCustomAttributes(connectorContext.getCustomAttributes());
		Boolean defaultTracingEnabled = connectorContext.getDefaultTracingEnabled();
		if (defaultTracingEnabled != null) {
			bean.setDefaultTracingEnabled(defaultTracingEnabled);
		}
		String componentName = connectorContext.getComponentName();
		if (!CommonTools.isEmpty(componentName)) {
			bean.setComponentName(componentName);
		}
		bean.setTenant(connectorContext.getTenant());
		String serviceName = connectorContext.getServiceName();
		if (!CommonTools.isEmpty(serviceName)) {
			bean.setServiceName(serviceName);
		}
		bean.setAddAttributesFromNotificationsMessage(connectorContext.getAddAttributesFromNotificationsMessage());
		bean.setAddAttributesFromNotificationsDetailsMessage(connectorContext.getAddAttributesFromNotificationsDetailsMessage());

		return bean;
	}

	@Managed
	private JaegerTracingConnector jaegerTracingConnector(TracingTemplateContext context) {
		JaegerTracingConnector bean = context.create(JaegerTracingConnector.T, InstanceConfiguration.currentInstance());
		bean.setModule(context.getTracingModule());
		bean.setAutoDeploy(true);

		bean.setName(TracingTemplateUtil.resolveContextBasedDeployableName("TRACING Connector (Jaeger)", context));

		TracingTemplateJaegerConnectorContext connectorContext = (TracingTemplateJaegerConnectorContext) context.getConnectorContext();

		TracingTemplateSamplerConfigurationContext samplerConfigurationContext = connectorContext.getSamplerConfigurationContext();
		if (samplerConfigurationContext instanceof TracingTemplateConstantSamplerContext) {
			bean.setSamplerConfiguration(jaegerConstantSampler(context));
		} else if (samplerConfigurationContext instanceof TracingTemplateProbabilisticSamplerContext) {
			bean.setSamplerConfiguration(jaegerProbabilisticSampler(context));
		} else if (samplerConfigurationContext instanceof TracingTemplateRateLimitingSamplerContext) {
			bean.setSamplerConfiguration(jaegerRateLimitingSampler(context));
		} else {
			throw new IllegalArgumentException("SamplerConfiguration: '" + samplerConfigurationContext + "' not supported or is not set");
		}
		bean.setReporterConfiguration(jaegerReporterConfiguration(context));

		return bean;
	}

	@Managed
	public JaegerInMemoryTracingConnector jaegerInMemoryTracingConnector(TracingTemplateContext context) {
		JaegerInMemoryTracingConnector bean = context.create(JaegerInMemoryTracingConnector.T, InstanceConfiguration.currentInstance());
		bean.setModule(context.getTracingModule());
		bean.setAutoDeploy(true);

		bean.setName(TracingTemplateUtil.resolveContextBasedDeployableName("TRACING Connector (Jaeger InMemory)", context));

		return bean;
	}

	@Managed
	public LoggingTracingConnector loggingTracingConnector(TracingTemplateContext context) {
		LoggingTracingConnector bean = context.create(LoggingTracingConnector.T, InstanceConfiguration.currentInstance());
		bean.setModule(context.getTracingModule());
		bean.setAutoDeploy(true);

		bean.setName(TracingTemplateUtil.resolveContextBasedDeployableName("TRACING Connector (Logging)", context));

		TracingTemplateLoggingConnectorContext connectorContext = (TracingTemplateLoggingConnectorContext) context.getConnectorContext();

		bean.setLogLevel(connectorContext.getLogLevel());
		bean.setLogAttributes(connectorContext.getLogAttributes());

		return bean;
	}

	@Managed
	public ReporterConfiguration jaegerReporterConfiguration(TracingTemplateContext context) {
		ReporterConfiguration bean = context.create(ReporterConfiguration.T, InstanceConfiguration.currentInstance());

		TracingTemplateJaegerConnectorContext connectorContext = (TracingTemplateJaegerConnectorContext) context.getConnectorContext();
		TracingTemplateReporterConfigurationContext reporterConfigurationContext = connectorContext.getReporterConfigurationContext();

		int flushInterval = reporterConfigurationContext.getFlushInterval();
		int maxQueueSize = reporterConfigurationContext.getMaxQueueSize();

		bean.setFlushInterval(flushInterval);
		bean.setMaxQueueSize(maxQueueSize);

		TracingTemplateSenderConfigurationContext senderConfigurationContext = reporterConfigurationContext.getSenderConfigurationContext();

		if (senderConfigurationContext instanceof TracingTemplateAgentSenderConfigurationContext) {
			bean.setSenderConfiguration(jaegerAgentSenderConfiguration(context));
		} else if (senderConfigurationContext instanceof TracingTemplateHttpSenderConfigurationContext) {
			bean.setSenderConfiguration(jaegerHttpSenderConfiguration(context));
		} else {
			throw new IllegalArgumentException("SenderConfiguration: '" + senderConfigurationContext + "' not supported or is not set");
		}

		return bean;
	}

	@Managed
	public SamplerConfiguration jaegerSamplerConfiguration(TracingTemplateContext context) {
		TracingTemplateJaegerConnectorContext connectorContext = (TracingTemplateJaegerConnectorContext) context.getConnectorContext();

		TracingTemplateSamplerConfigurationContext samplerConfigurationContext = connectorContext.getSamplerConfigurationContext();
		if (samplerConfigurationContext instanceof TracingTemplateConstantSamplerContext) {
			return jaegerConstantSampler(context);
		} else if (samplerConfigurationContext instanceof TracingTemplateProbabilisticSamplerContext) {
			ProbabilisticSampler bean = context.create(ProbabilisticSampler.T, InstanceConfiguration.currentInstance());
			bean.setProbability(((TracingTemplateProbabilisticSamplerContext) samplerConfigurationContext).getProbability());
			return bean;
		} else if (samplerConfigurationContext instanceof TracingTemplateRateLimitingSamplerContext) {
			RateLimitingSampler bean = context.create(RateLimitingSampler.T, InstanceConfiguration.currentInstance());
			bean.setRate(((TracingTemplateRateLimitingSamplerContext) samplerConfigurationContext).getRate());
			return bean;
		} else {
			throw new IllegalArgumentException("SamplerConfiguration: '" + samplerConfigurationContext + "' not supported or is not set");
		}
	}

	@Managed
	public ConstantSampler jaegerConstantSampler(TracingTemplateContext context) {
		return context.create(ConstantSampler.T, InstanceConfiguration.currentInstance());
	}
	@Managed
	public ProbabilisticSampler jaegerProbabilisticSampler(TracingTemplateContext context) {
		TracingTemplateJaegerConnectorContext connectorContext = (TracingTemplateJaegerConnectorContext) context.getConnectorContext();
		TracingTemplateSamplerConfigurationContext samplerConfigurationContext = connectorContext.getSamplerConfigurationContext();

		ProbabilisticSampler bean = context.create(ProbabilisticSampler.T, InstanceConfiguration.currentInstance());
		bean.setProbability(((TracingTemplateProbabilisticSamplerContext) samplerConfigurationContext).getProbability());
		return bean;
	}
	@Managed
	public RateLimitingSampler jaegerRateLimitingSampler(TracingTemplateContext context) {
		TracingTemplateJaegerConnectorContext connectorContext = (TracingTemplateJaegerConnectorContext) context.getConnectorContext();
		TracingTemplateSamplerConfigurationContext samplerConfigurationContext = connectorContext.getSamplerConfigurationContext();

		RateLimitingSampler bean = context.create(RateLimitingSampler.T, InstanceConfiguration.currentInstance());
		bean.setRate(((TracingTemplateRateLimitingSamplerContext) samplerConfigurationContext).getRate());
		return bean;
	}
	@Managed
	public AgentSenderConfiguration jaegerAgentSenderConfiguration(TracingTemplateContext context) {
		TracingTemplateJaegerConnectorContext connectorContext = (TracingTemplateJaegerConnectorContext) context.getConnectorContext();
		TracingTemplateSenderConfigurationContext senderConfigurationContext = connectorContext.getReporterConfigurationContext()
				.getSenderConfigurationContext();

		TracingTemplateAgentSenderConfigurationContext agentSenderConfigurationContext = (TracingTemplateAgentSenderConfigurationContext) senderConfigurationContext;
		AgentSenderConfiguration bean = context.create(AgentSenderConfiguration.T, InstanceConfiguration.currentInstance());
		bean.setHost(agentSenderConfigurationContext.getHost());
		bean.setPort(agentSenderConfigurationContext.getPort());
		return bean;
	}
	@Managed
	public HttpSenderConfiguration jaegerHttpSenderConfiguration(TracingTemplateContext context) {
		TracingTemplateJaegerConnectorContext connectorContext = (TracingTemplateJaegerConnectorContext) context.getConnectorContext();
		TracingTemplateSenderConfigurationContext senderConfigurationContext = connectorContext.getReporterConfigurationContext()
				.getSenderConfigurationContext();

		HttpSenderConfiguration bean = context.create(HttpSenderConfiguration.T, InstanceConfiguration.currentInstance());
		bean.setEndpoint(((TracingTemplateHttpSenderConfigurationContext) senderConfigurationContext).getEndpoint());
		return bean;
	}

	// -----------------------------------------------------------------------
	// ASPECT
	// -----------------------------------------------------------------------

	@Override
	@Managed
	public TracingAspect tracingAspect(TracingTemplateContext context) {
		TracingAspect bean = context.create(TracingAspect.T, InstanceConfiguration.currentInstance());
		bean.setModule(context.getTracingModule());
		bean.setAutoDeploy(true);

		bean.setName(TracingTemplateUtil.resolveContextBasedDeployableName("TRACING Aspect", context));

		bean.setTracingConnector(tracingConnector(context));

		return bean;
	}

}
