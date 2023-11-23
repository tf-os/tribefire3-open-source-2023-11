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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.service.api.InstanceId;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import tribefire.extension.metrics.connector.api.MetricsConnector;
import tribefire.extension.metrics.model.service.MetricsRequest;
import tribefire.extension.metrics.model.service.MetricsResult;
import tribefire.extension.metrics.model.service.local.LogMetricsLocal;
import tribefire.extension.metrics.model.service.local.LogMetricsLocalResult;
import tribefire.extension.metrics.model.service.local.ResetMetricsLocal;
import tribefire.extension.metrics.model.service.local.ResetMetricsLocalResult;
import tribefire.extension.metrics.service.util.LongHolder;

//TODO: how to handle multi nodes? Some metrics should be per node (e.g. CPU load), some should be per node _and_ total (e.g. distribution summary per service request) -> probably with tags; yes, with tags
//TODO: how to custom tags at runtime?
//TODO: check naming: local?
public class MetricsProcessor extends AbstractDispatchingServiceProcessor<MetricsRequest, MetricsResult> implements LifecycleAware {

	private static final Logger logger = Logger.getLogger(MetricsProcessor.class);

	private tribefire.extension.metrics.model.deployment.service.MetricsProcessor deployable;

	private InstanceId instanceId;

	private Set<MetricsConnector> aspectMetricsConnectors;
	private Set<MetricsConnector> binderMetricsConnectors;

	private CompositeMeterRegistry compositeRegistry;

	// -----------------------------------------------------------------------
	// DISPATCHING
	// -----------------------------------------------------------------------

	@Override
	protected void configureDispatching(DispatchConfiguration<MetricsRequest, MetricsResult> dispatching) {
		// -----------------------------
		// OPERATIONS
		// -----------------------------
		// local
		dispatching.register(ResetMetricsLocal.T, this::resetMetricsLocal);
		dispatching.register(LogMetricsLocal.T, this::logMetricsLocal);

		// multicast

		// -----------------------------
		// TODO: another?
		// -----------------------------
		// local

		// multicast

		// TODO: remove this
		// -----------------------------
		// BINDER
		// -----------------------------
		// default binders
		// dispatching.register(ClassLoaderMetricsBinder.T, this::classLoaderMetricsBinder);
		// dispatching.register(FileDescriptorMetricsBinder.T, this::fileDescriptorMetricsBinder);
	}

	// -----------------------------------------------------------------------
	// LifecycleAware
	// -----------------------------------------------------------------------

	@Override
	public void postConstruct() {
		// nothing so far

		// logger.info(() -> "Deploying Metrics Binders during redeployment....");
		//
		// MetricsBinder request = MetricsBinder.T.create();
		// request.setMetricsBinderRequest(ClassLoaderMetricsBinder.T.create());
		// request.setServiceId(deployable.getExternalId());
		// MetricsBinderResult result = request.eval(requestEvaluator).get();
		//
		// logger.info(() -> "Finished deploying Metrics Binders during redeployment!");
	}

	@Override
	public void preDestroy() {
		List<String> metricsConnectorNames = metricsConnectors().stream().map(mc -> mc.name()).collect(Collectors.toList());

		compositeRegistry.getRegistries().forEach(registry -> {
			registry.forEachMeter(meter -> {
				meter.close();
			});

			registry.clear();
			registry.close();
		});
		compositeRegistry.clear();
		compositeRegistry.close();

		logger.info(() -> "Removed all metrics from processor: '" + defaultName() + "' (" + metricsConnectorNames + ")");
	}

	// -----------------------------------------------------------------------
	// SERVICE METHODS - LOCAL
	// -----------------------------------------------------------------------

	public ResetMetricsLocalResult resetMetricsLocal(@SuppressWarnings("unused") ServiceRequestContext requestContext,
			@SuppressWarnings("unused") ResetMetricsLocal request) {

		// TODO: reset seems to be not good because followed recreation of the metrics is based on deployables
		// deployment - probably remove this
		metricsConnectors().forEach(mc -> {
			mc.registry().clear();
			logger.info(() -> "Finished resetting metric of connector '" + mc.name() + "'");
		});

		logger.info(() -> "Finished resetting all metrics of all connectors locally");

		ResetMetricsLocalResult result = ResetMetricsLocalResult.T.create();
		return result;
	}

	public LogMetricsLocalResult logMetricsLocal(@SuppressWarnings("unused") ServiceRequestContext requestContext,
			@SuppressWarnings("unused") LogMetricsLocal request) {

		StringBuilder sb = new StringBuilder();
		int numberOfMetricsConnectors = metricsConnectors().size();

		sb.append("\n");
		sb.append("Number of Metrics Connectors: '");
		sb.append(numberOfMetricsConnectors);
		sb.append("'");
		sb.append("\n");

		// ---------------

		Set<MeterRegistry> registries = compositeRegistry.getRegistries();
		List<Meter> meters = compositeRegistry.getMeters();

		// ---------------

		metricsConnectors().forEach(mc -> {
			MeterRegistry registry = mc.registry();
			int numberOfMeters = registry.getMeters().size();

			Map<String, Long> numberOfMetersByType = registry.getMeters().stream()
					.collect(Collectors.groupingBy(m -> m.getClass().getSimpleName(), Collectors.counting()));

			sb.append("--------------------------");
			sb.append("\n");
			sb.append("Metrics Connector: ");
			sb.append(mc.name());
			sb.append("(Total: ");
			sb.append(numberOfMeters);
			sb.append(" meters/");
			sb.append(numberOfMetersByType);
			sb.append(")");
			sb.append("\n");
			registry.forEachMeter(meter -> {
				sb.append(" ");
				sb.append(meter.getClass().getSimpleName());
				sb.append(": ");
				sb.append(meter.getId().toString());

				LongHolder numberOfMeasures = new LongHolder();

				StringBuilder s = new StringBuilder();

				meter.measure().forEach(measurement -> {
					numberOfMeasures.increase();
					s.append("\n");
					s.append("    ");
					s.append(measurement.toString());
				});

				long value = numberOfMeasures.getValue();
				sb.append(" (");
				sb.append(value);
				sb.append(" measures)");
				sb.append(s);
				sb.append("\n");
			});

		});
		String loggingOutput = sb.toString();
		logger.info(() -> loggingOutput);

		LogMetricsLocalResult result = LogMetricsLocalResult.T.create();
		result.setLoggingOutput(loggingOutput);
		return result;
	}

	// -----------------------------------------------------------------------
	// BINDERS
	// -----------------------------------------------------------------------

	//@formatter:off
//	public MetricsBinderBaseResult classLoaderMetricsBinder(@SuppressWarnings("unused") ServiceRequestContext requestContext,
//			@SuppressWarnings("unused") ClassLoaderMetricsBinder request) {
//
//		ClassLoaderMetrics binder = new io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics();
//
//		MetricsBinderBaseResult result = MetricsBinderBaseResult.T.create();
//		result.setMeterBinder(binder);
//		return result;
//	}
//
//	public MetricsBinderBaseResult fileDescriptorMetricsBinder(@SuppressWarnings("unused") ServiceRequestContext requestContext,
//			@SuppressWarnings("unused") FileDescriptorMetricsBinder request) {
//
//		FileDescriptorMetrics binder = new io.micrometer.core.instrument.binder.system.FileDescriptorMetrics();
//
//		MetricsBinderBaseResult result = MetricsBinderBaseResult.T.create();
//		result.setMeterBinder(binder);
//		return result;
//	}
	//@formatter:on

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

	private String defaultName() {
		return deployable.getName() + "[" + deployable.getExternalId() + "]";
	}

	private Set<MetricsConnector> metricsConnectors() {
		Set<MetricsConnector> metricsConnectors = new HashSet<>();
		metricsConnectors.addAll(binderMetricsConnectors);
		metricsConnectors.addAll(aspectMetricsConnectors);
		return metricsConnectors;
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Required
	@Configurable
	public void setDeployable(tribefire.extension.metrics.model.deployment.service.MetricsProcessor deployable) {
		this.deployable = deployable;
	}

	@Required
	@Configurable
	public void setInstanceId(InstanceId instanceId) {
		this.instanceId = instanceId;
	}

	@Required
	@Configurable
	public void setAspectMetricsConnectors(Set<MetricsConnector> aspectMetricsConnectors) {
		this.aspectMetricsConnectors = aspectMetricsConnectors;
	}

	@Required
	@Configurable
	public void setBinderMetricsConnectors(Set<MetricsConnector> binderMetricsConnectors) {
		this.binderMetricsConnectors = binderMetricsConnectors;
	}

	@Required
	@Configurable
	public void setCompositeRegistry(CompositeMeterRegistry compositeRegistry) {
		this.compositeRegistry = compositeRegistry;
	}

}
