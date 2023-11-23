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
package tribefire.extension.tracing.wire.space;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.tracing.connector.api.AbstractTracingConnector;
import tribefire.extension.tracing.connector.api.TracingConnector;
import tribefire.extension.tracing.connector.jaeger.JaegerInMemoryTracingConnector;
import tribefire.extension.tracing.connector.jaeger.JaegerTracingConnector;
import tribefire.extension.tracing.connector.logging.LoggingTracingConnector;
import tribefire.extension.tracing.service.HealthCheckProcessor;
import tribefire.extension.tracing.service.TracingAspect;
import tribefire.extension.tracing.service.TracingProcessor;
import tribefire.extension.tracing.service.demo.DemoTracingProcessor;
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
	public JaegerTracingConnector jaegerTracingConnector(
			ExpertContext<tribefire.extension.tracing.model.deployment.connector.JaegerTracingConnector> context) {

		tribefire.extension.tracing.model.deployment.connector.JaegerTracingConnector deployable = context.getDeployable();

		JaegerTracingConnector bean = new JaegerTracingConnector();
		bean.setDeployable(deployable);

		enrichTracingConnector(deployable, bean);
		return bean;
	}

	@Managed
	public JaegerInMemoryTracingConnector jaegerInMemoryTracingConnector(
			ExpertContext<tribefire.extension.tracing.model.deployment.connector.JaegerInMemoryTracingConnector> context) {

		tribefire.extension.tracing.model.deployment.connector.JaegerInMemoryTracingConnector deployable = context.getDeployable();

		JaegerInMemoryTracingConnector bean = new JaegerInMemoryTracingConnector();
		bean.setDeployable(deployable);

		enrichTracingConnector(deployable, bean);
		return bean;
	}

	@Managed
	public LoggingTracingConnector loggingTracingConnector(
			ExpertContext<tribefire.extension.tracing.model.deployment.connector.LoggingTracingConnector> context) {

		tribefire.extension.tracing.model.deployment.connector.LoggingTracingConnector deployable = context.getDeployable();

		LoggingTracingConnector bean = new LoggingTracingConnector();
		bean.setLogLevel(deployable.getLogLevel());
		bean.setLogAttributes(deployable.getLogAttributes());
		bean.setDeployable(deployable);

		enrichTracingConnector(deployable, bean);
		return bean;
	}

	private void enrichTracingConnector(tribefire.extension.tracing.model.deployment.connector.TracingConnector deployable,
			AbstractTracingConnector bean) {
		// TODO:cleanup
		// bean.setEvaluator(tfPlatform.requestProcessing().systemEvaluator());
		bean.setTracingEnabled(deployable.getDefaultTracingEnabled());
		Map<String, String> _customAttributes = deployable.getCustomAttributes();
		if (_customAttributes == null) {
			bean.setCustomAttributesRegistry(new HashMap<>());
		} else {
			bean.setCustomAttributesRegistry(new HashMap<>(_customAttributes));
		}
		Set<String> _entityTypeInclusions = deployable.getEntityTypeInclusions();
		if (_entityTypeInclusions == null) {
			bean.setEntityTypeInclusionsRegistry(new HashSet<>());
		} else {
			bean.setEntityTypeInclusionsRegistry(new HashSet<>(_entityTypeInclusions));
		}
		Set<String> _entityTypeHierarchyInclusions = deployable.getEntityTypeHierarchyInclusions();
		if (_entityTypeHierarchyInclusions == null) {
			bean.setEntityTypeHierarchyInclusionsRegistry(new HashSet<>());
		} else {
			bean.setEntityTypeHierarchyInclusionsRegistry(new HashSet<>(_entityTypeHierarchyInclusions));
		}
		Set<String> _entityTypeExclusions = deployable.getEntityTypeExclusions();
		if (_entityTypeExclusions == null) {
			bean.setEntityTypeExclusionsRegistry(new HashSet<>());
		} else {
			bean.setEntityTypeExclusionsRegistry(new HashSet<>(_entityTypeExclusions));
		}
		Set<String> _entityTypeHierarchyExclusions = deployable.getEntityTypeHierarchyExclusions();
		if (_entityTypeHierarchyExclusions == null) {
			bean.setEntityTypeHierarchyExclusionsRegistry(new HashSet<>());
		} else {
			bean.setEntityTypeHierarchyExclusionsRegistry(new HashSet<>(_entityTypeHierarchyExclusions));
		}
		Set<String> _userInclusions = deployable.getUserInclusions();
		if (_userInclusions == null) {
			bean.setUserInclusionsRegistry(new HashSet<>());
		} else {
			bean.setUserInclusionsRegistry(new HashSet<>(_userInclusions));
		}
		Set<String> _userExclusions = deployable.getUserExclusions();
		if (_userExclusions == null) {
			bean.setUserExclusionsRegistry(new HashSet<>());
		} else {
			bean.setUserExclusionsRegistry(new HashSet<>(_userExclusions));
		}
		bean.setDefaultAttributes(deployable.getDefaultAttributes());
		bean.setComponentName(deployable.getComponentName());
		bean.setTenant(deployable.getTenant());
		bean.setServiceName(deployable.getServiceName());
		bean.setAddAttributesFromNotificationsMessage(deployable.getAddAttributesFromNotificationsMessage());
		bean.setAddAttributesFromNotificationsDetailsMessage(deployable.getAddAttributesFromNotificationsDetailsMessage());

		bean.setInstanceId(platformReflection.instanceId());

	}

	// -----------------------------------------------------------------------
	// PROCESSOR
	// -----------------------------------------------------------------------

	@Managed
	public TracingProcessor tracingProcessor(ExpertContext<tribefire.extension.tracing.model.deployment.service.TracingProcessor> context) {

		tribefire.extension.tracing.model.deployment.service.TracingProcessor deployable = context.getDeployable();
		TracingProcessor bean = new TracingProcessor();
		bean.setDeployable(deployable);

		tribefire.extension.tracing.model.deployment.connector.TracingConnector tracingConnectorDeployable = deployable.getTracingConnector();

		TracingConnector tracingConnector = context.resolve(tracingConnectorDeployable,
				tribefire.extension.tracing.model.deployment.connector.TracingConnector.T);

		bean.setTracingConnector(tracingConnector);
		bean.setTracingConnectorName(deployable.getTracingConnector().entityType().getTypeName());
		bean.setInstanceId(platformReflection.instanceId());
		return bean;
	}

	// ----------
	// DEMO
	// ----------

	@Managed
	public DemoTracingProcessor demoTracingProcessor(
			ExpertContext<tribefire.extension.tracing.model.deployment.service.demo.DemoTracingProcessor> context) {

		tribefire.extension.tracing.model.deployment.service.demo.DemoTracingProcessor deployable = context.getDeployable();

		DemoTracingProcessor bean = new DemoTracingProcessor();
		bean.setLogLevel(deployable.getLogLevel());
		return bean;
	}

	// -----------------------------------------------------------------------
	// ASPECT
	// -----------------------------------------------------------------------

	@Managed
	public TracingAspect tracingAspect(ExpertContext<tribefire.extension.tracing.model.deployment.service.TracingAspect> context) {

		tribefire.extension.tracing.model.deployment.service.TracingAspect deployable = context.getDeployable();

		TracingAspect bean = new TracingAspect();
		bean.setTracingConnector(
				context.resolve(deployable.getTracingConnector(), tribefire.extension.tracing.model.deployment.connector.TracingConnector.T));

		return bean;
	}

	// -----------------------------------------------------------------------
	// PROCESSOR
	// -----------------------------------------------------------------------

	@Managed
	public HealthCheckProcessor healthCheckProcessor(
			@SuppressWarnings("unused") ExpertContext<tribefire.extension.tracing.model.deployment.service.HealthCheckProcessor> context) {

		HealthCheckProcessor bean = new HealthCheckProcessor();
		bean.setCortexSessionSupplier(tfPlatform.systemUserRelated().cortexSessionSupplier());
		bean.setDeployRegistry(tfPlatform.deployment().deployRegistry());
		return bean;
	}

}
