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
package tribefire.extension.metrics.wire.space;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.newrelic.NewRelicConfig;
import io.micrometer.newrelic.NewRelicMeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import tribefire.extension.metrics.connector.api.MetricsConnector;
import tribefire.extension.metrics.connector.inmemory.InMemoryMetricsConnector;
import tribefire.extension.metrics.connector.newrelic.NewRelicMetricsConnector;
import tribefire.extension.metrics.connector.prometheus.PrometheusMetricsConnector;
import tribefire.extension.metrics.connector.prometheus.PrometheusMetricsScrapingEndpoint;
import tribefire.extension.metrics.health.HealthCheckProcessor;
import tribefire.extension.metrics.model.deployment.service.MetricsBinderConfig;
import tribefire.extension.metrics.model.deployment.service.metricsbinder.ClassLoaderMetrics;
import tribefire.extension.metrics.model.deployment.service.metricsbinder.FileDescriptorMetrics;
import tribefire.extension.metrics.model.service.MetricsBinder;
import tribefire.extension.metrics.service.MetricsDemoProcessor;
import tribefire.extension.metrics.service.MetricsProcessor;
import tribefire.extension.metrics.service.aspect.MetricsCounterAspect;
import tribefire.extension.metrics.service.aspect.MetricsInProgressAspect;
import tribefire.extension.metrics.service.aspect.MetricsSummaryAspect;
import tribefire.extension.metrics.service.aspect.MetricsTimerAspect;
import tribefire.module.wire.contract.PlatformReflectionContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;
import tribefire.module.wire.contract.WebPlatformResourcesContract;

/**
 *
 */
@Managed
public class DeployablesSpace implements WireSpace {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private WebPlatformResourcesContract resources;

	@Import
	private PlatformReflectionContract platformReflection;

	// -----------------------------------------------------------------------
	// CONNECTOR
	// -----------------------------------------------------------------------

	@Managed
	public PrometheusMetricsConnector prometheusMetricsConnector(
			ExpertContext<tribefire.extension.metrics.model.deployment.connector.PrometheusMetricsConnector> context) {

		tribefire.extension.metrics.model.deployment.connector.PrometheusMetricsConnector deployable = context.getDeployable();

		PrometheusMeterRegistry registry = prometheusMeterRegistry();
		enrichMeterRegistry(registry, deployable);

		PrometheusMetricsConnector bean = new PrometheusMetricsConnector();
		bean.setDeployable(deployable);
		bean.setRegistry(registry);

		return bean;
	}

	@Managed
	public PrometheusMetricsScrapingEndpoint prometheusMetricsScrapingEndpoint(
			ExpertContext<tribefire.extension.metrics.model.deployment.connector.PrometheusMetricsConnector> context) {

		tribefire.extension.metrics.model.deployment.connector.PrometheusMetricsConnector deployable = context.getDeployable();

		PrometheusMetricsConnector prometheusMetricsConnector = prometheusMetricsConnector(context);
		PrometheusMeterRegistry registry = (PrometheusMeterRegistry) prometheusMetricsConnector.registry();

		PrometheusMetricsScrapingEndpoint bean = new PrometheusMetricsScrapingEndpoint();
		bean.setDeployable(deployable);
		bean.setPrometheusMeterRegistry(registry);

		return bean;
	}

	@Managed
	public InMemoryMetricsConnector inMemoryMetricsConnector(
			ExpertContext<tribefire.extension.metrics.model.deployment.connector.InMemoryMetricsConnector> context) {

		tribefire.extension.metrics.model.deployment.connector.InMemoryMetricsConnector deployable = context.getDeployable();

		SimpleMeterRegistry registry = simpleMeterRegistry();
		enrichMeterRegistry(registry, deployable);

		InMemoryMetricsConnector bean = new InMemoryMetricsConnector();
		bean.setDeployable(deployable);
		bean.setRegistry(registry);

		return bean;
	}

	@Managed
	public NewRelicMetricsConnector newRelicMetricsConnector(
			ExpertContext<tribefire.extension.metrics.model.deployment.connector.NewRelicMetricsConnector> context) {

		tribefire.extension.metrics.model.deployment.connector.NewRelicMetricsConnector deployable = context.getDeployable();

		// TODO: cannot connect right now NewRelic - account is EU account. Could not find the correct settings yet.
		NewRelicMeterRegistry registry = newRelicMeterRegistry(deployable.getAccountId(), deployable.getUri(), deployable.getApiKey());
		enrichMeterRegistry(registry, deployable);

		NewRelicMetricsConnector bean = new NewRelicMetricsConnector();
		bean.setDeployable(deployable);
		bean.setRegistry(registry);

		return bean;
	}

	// ---------------
	// REGISTRY
	// ---------------
	// @Managed
	private PrometheusMeterRegistry prometheusMeterRegistry() {
		// TODO: add custom prometheus settings
		PrometheusMeterRegistry bean = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
		return bean;
	}
	private SimpleMeterRegistry simpleMeterRegistry() {
		SimpleMeterRegistry bean = new SimpleMeterRegistry();
		return bean;
	}

	// TODO: managed?
	// @Managed
	private NewRelicMeterRegistry newRelicMeterRegistry(String accountId, String uri, String apiKey) {
		// TODO: add custom prometheus settings
		NewRelicConfig newRelicConfig = new NewRelicConfig() {
			@Override
			public String accountId() {
				return accountId;
			}

			@Override
			public String uri() {
				return uri;
			}

			@Override
			public Duration step() {
				// TODO: make configurable
				return Duration.ofSeconds(10);
			}

			@Override
			public String eventType() {
				// TODO Auto-generated method stub
				return NewRelicConfig.super.eventType();
			}

			@Override
			public boolean meterNameEventTypeEnabled() {
				return true;
			}

			@Override
			public String apiKey() {
				return apiKey;
			}

			@Override
			public String get(String k) {
				return null; // accept the rest of the defaults
			}
		};

		NewRelicMeterRegistry bean = new NewRelicMeterRegistry(newRelicConfig, Clock.SYSTEM);
		return bean;
	}

	// ---------

	private void enrichMeterRegistry(MeterRegistry registry, tribefire.extension.metrics.model.deployment.connector.MetricsConnector deployable) {
		Map<String, String> commonTags = deployable.getCommonTags();

		if ((commonTags.size() % 2) != 0) {
			throw new IllegalArgumentException(
					"'commonTags' must have equal number of entries. Key/Value pairs. But it is: '" + commonTags.size() + "'");
		}

		registry.config().commonTags(mapToArray(commonTags));
	}

	// -----------------------------------------------------------------------
	// PROCESSOR
	// -----------------------------------------------------------------------

	@Managed
	public MetricsProcessor metricsProcessor(ExpertContext<tribefire.extension.metrics.model.deployment.service.MetricsProcessor> context) {

		tribefire.extension.metrics.model.deployment.service.MetricsProcessor deployable = context.getDeployable();

		MetricsProcessor bean = new MetricsProcessor();

		bean.setDeployable(deployable);
		bean.setInstanceId(platformReflection.instanceId());

		CompositeMeterRegistry compositeRegistry = new CompositeMeterRegistry();

		// initialize compound registry based on MetricsConnectors from aspects
		Set<MetricsConnector> aspectMetricsConnectors = new HashSet<>();
		deployable.getMetricsAspects().forEach(metricsAspects -> {
			metricsAspects.getMetricsConnectors().forEach(metricsConnectorDeployable -> {
				MetricsConnector metricsConnector = context.resolve(metricsConnectorDeployable,
						tribefire.extension.metrics.model.deployment.connector.MetricsConnector.T);
				aspectMetricsConnectors.add(metricsConnector);
				compositeRegistry.add(metricsConnector.registry());
			});
		});
		bean.setAspectMetricsConnectors(aspectMetricsConnectors);

		// add binders if configured
		Set<MetricsConnector> binderMetricsConnectors = new HashSet<>();
		MetricsBinderConfig metricsBinderConfig = deployable.getMetricsBinderConfig();
		if (metricsBinderConfig != null) {
			Set<MetricsBinder> metricsBinders = metricsBinderConfig.getMetricsBinders();

			Set<tribefire.extension.metrics.model.deployment.connector.MetricsConnector> binderMetricsConnectorDeployables = metricsBinderConfig
					.getMetricsConnectors();
			binderMetricsConnectorDeployables.forEach(binderMetricsConnectorDeployable -> {

				MetricsConnector metricsConnector = context.resolve(binderMetricsConnectorDeployable,
						tribefire.extension.metrics.model.deployment.connector.MetricsConnector.T);
				MeterRegistry binderRegistry = metricsConnector.registry();
				enrichMeterRegistry(binderRegistry, binderMetricsConnectorDeployable);
				compositeRegistry.add(binderRegistry);

				binderMetricsConnectors.add(metricsConnector);

				// TODO: cleanup when service based method is working
				metricsBinders.forEach(metricsBinder -> {
					String typeSignature = metricsBinder.entityType().getTypeSignature();
					if (typeSignature.equals(ClassLoaderMetrics.T.getTypeSignature())) {
						new io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics().bindTo(binderRegistry);
					} else if (typeSignature.equals(FileDescriptorMetrics.T.getTypeSignature())) {
						new io.micrometer.core.instrument.binder.system.FileDescriptorMetrics().bindTo(binderRegistry);
					} else {
						throw new IllegalArgumentException("Metrics binder: '" + metricsBinder.entityType().getTypeSignature() + "' not supported");
					}
				});
			});
		}
		bean.setBinderMetricsConnectors(binderMetricsConnectors);

		bean.setCompositeRegistry(compositeRegistry);
		return bean;

		// TODO: make sure that there is no exception when connector is not deployed
	}

	@Managed
	public MetricsDemoProcessor metricsDemoProcessor(
			ExpertContext<tribefire.extension.metrics.model.deployment.service.MetricsDemoProcessor> context) {

		tribefire.extension.metrics.model.deployment.service.MetricsDemoProcessor deployable = context.getDeployable();

		MetricsDemoProcessor bean = new MetricsDemoProcessor();
		bean.setLogLevel(deployable.getLogLevel());
		return bean;
	}

	// -----------------------------------------------------------------------
	// ASPECT
	// -----------------------------------------------------------------------

	@Managed
	public MetricsCounterAspect metricsCounterAspect(
			ExpertContext<tribefire.extension.metrics.model.deployment.service.aspect.MetricsCounterAspect> context) {

		tribefire.extension.metrics.model.deployment.service.aspect.MetricsCounterAspect deployable = context.getDeployable();

		Set<tribefire.extension.metrics.model.deployment.connector.MetricsConnector> metricsConnectors = deployable.getMetricsConnectors();

		Set<MetricsConnector> metricsConnectorsImpl = new HashSet<>();
		metricsConnectors.forEach(metricsConnector -> {
			MetricsConnector metricsConnectorImpl = context.resolve(metricsConnector,
					tribefire.extension.metrics.model.deployment.connector.MetricsConnector.T);
			metricsConnectorsImpl.add(metricsConnectorImpl);
		});

		MetricsCounterAspect bean = new MetricsCounterAspect();
		bean.setMetricsConnectors(metricsConnectorsImpl);
		bean.setName(deployable.getName());
		bean.setDescription(deployable.getDescription());
		bean.setBaseUnit(deployable.getBaseUnit());
		bean.setTagsSuccess(mapToArray(deployable.getTagsSuccess()));
		bean.setTagsError(mapToArray(deployable.getTagsError()));

		bean.setAddDomainIdTag(deployable.getAddDomainIdTag());
		bean.setAddPartitionTag(deployable.getAddPartitionTag());
		bean.setAddRequiresAuthenticationTag(deployable.getAddRequiresAuthenticationTag());
		bean.setAddTypeSignatureTag(deployable.getAddTypeSignatureTag());

		return bean;
	}

	@Managed
	public MetricsTimerAspect metricsTimerAspect(
			ExpertContext<tribefire.extension.metrics.model.deployment.service.aspect.MetricsTimerAspect> context) {

		tribefire.extension.metrics.model.deployment.service.aspect.MetricsTimerAspect deployable = context.getDeployable();

		Set<tribefire.extension.metrics.model.deployment.connector.MetricsConnector> metricsConnectors = deployable.getMetricsConnectors();

		Set<MetricsConnector> metricsConnectorsImpl = new HashSet<>();
		metricsConnectors.forEach(metricsConnector -> {
			MetricsConnector metricsConnectorImpl = context.resolve(metricsConnector,
					tribefire.extension.metrics.model.deployment.connector.MetricsConnector.T);
			metricsConnectorsImpl.add(metricsConnectorImpl);
		});

		MetricsTimerAspect bean = new MetricsTimerAspect();
		bean.setMetricsConnectors(metricsConnectorsImpl);
		bean.setName(deployable.getName());
		bean.setDescription(deployable.getDescription());
		bean.setTagsSuccess(mapToArray(deployable.getTagsSuccess()));
		bean.setTagsError(mapToArray(deployable.getTagsError()));

		bean.setAddDomainIdTag(deployable.getAddDomainIdTag());
		bean.setAddPartitionTag(deployable.getAddPartitionTag());
		bean.setAddRequiresAuthenticationTag(deployable.getAddRequiresAuthenticationTag());
		bean.setAddTypeSignatureTag(deployable.getAddTypeSignatureTag());

		return bean;
	}

	@Managed
	public MetricsSummaryAspect metricsSummaryAspect(
			ExpertContext<tribefire.extension.metrics.model.deployment.service.aspect.MetricsSummaryAspect> context) {

		tribefire.extension.metrics.model.deployment.service.aspect.MetricsSummaryAspect deployable = context.getDeployable();

		Set<tribefire.extension.metrics.model.deployment.connector.MetricsConnector> metricsConnectors = deployable.getMetricsConnectors();

		Set<MetricsConnector> metricsConnectorsImpl = new HashSet<>();
		metricsConnectors.forEach(metricsConnector -> {
			MetricsConnector metricsConnectorImpl = context.resolve(metricsConnector,
					tribefire.extension.metrics.model.deployment.connector.MetricsConnector.T);
			metricsConnectorsImpl.add(metricsConnectorImpl);
		});

		MetricsSummaryAspect bean = new MetricsSummaryAspect();
		bean.setMetricsConnectors(metricsConnectorsImpl);
		bean.setName(deployable.getName());
		bean.setDescription(deployable.getDescription());
		bean.setTagsSuccess(mapToArray(deployable.getTagsSuccess()));
		bean.setTagsError(mapToArray(deployable.getTagsError()));
		bean.setDeployable(deployable);
		bean.setMarshaller(tfPlatform.marshalling().jsonMarshaller());

		bean.setAddDomainIdTag(deployable.getAddDomainIdTag());
		bean.setAddPartitionTag(deployable.getAddPartitionTag());
		bean.setAddRequiresAuthenticationTag(deployable.getAddRequiresAuthenticationTag());
		bean.setAddTypeSignatureTag(deployable.getAddTypeSignatureTag());

		return bean;
	}

	@Managed
	public MetricsInProgressAspect metricsInProgressAspect(
			ExpertContext<tribefire.extension.metrics.model.deployment.service.aspect.MetricsInProgressAspect> context) {

		tribefire.extension.metrics.model.deployment.service.aspect.MetricsInProgressAspect deployable = context.getDeployable();

		Set<tribefire.extension.metrics.model.deployment.connector.MetricsConnector> metricsConnectors = deployable.getMetricsConnectors();

		Set<MetricsConnector> metricsConnectorsImpl = new HashSet<>();
		metricsConnectors.forEach(metricsConnector -> {
			MetricsConnector metricsConnectorImpl = context.resolve(metricsConnector,
					tribefire.extension.metrics.model.deployment.connector.MetricsConnector.T);
			metricsConnectorsImpl.add(metricsConnectorImpl);
		});

		MetricsInProgressAspect bean = new MetricsInProgressAspect();
		bean.setMetricsConnectors(metricsConnectorsImpl);
		bean.setName(deployable.getName());
		bean.setDescription(deployable.getDescription());
		bean.setTagsSuccess(mapToArray(deployable.getTagsSuccess()));
		bean.setTagsError(mapToArray(deployable.getTagsError()));

		bean.setAddDomainIdTag(deployable.getAddDomainIdTag());
		bean.setAddPartitionTag(deployable.getAddPartitionTag());
		bean.setAddRequiresAuthenticationTag(deployable.getAddRequiresAuthenticationTag());
		bean.setAddTypeSignatureTag(deployable.getAddTypeSignatureTag());

		return bean;

	}

	// -----------------------------------------------------------------------
	// HEALTH
	// -----------------------------------------------------------------------

	@Managed
	public HealthCheckProcessor healthCheckProcessor(
			@SuppressWarnings("unused") ExpertContext<tribefire.extension.metrics.model.deployment.health.HealthCheckProcessor> context) {

		HealthCheckProcessor bean = new HealthCheckProcessor();
		bean.setCortexSessionSupplier(tfPlatform.systemUserRelated().cortexSessionSupplier());
		bean.setDeployRegistry(tfPlatform.deployment().deployRegistry());
		return bean;
	}

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

	private String[] mapToArray(Map<String, String> map) {
		List<String> mapAsList = map.entrySet().stream().flatMap(e -> Stream.of(e.getKey(), e.getValue())).collect(Collectors.toList());
		return mapAsList.toArray(new String[mapAsList.size()]);
	}
}
