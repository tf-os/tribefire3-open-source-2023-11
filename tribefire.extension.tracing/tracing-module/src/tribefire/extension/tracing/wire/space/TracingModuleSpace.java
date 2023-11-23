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

import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.tracing.model.deployment.connector.JaegerInMemoryTracingConnector;
import tribefire.extension.tracing.model.deployment.connector.JaegerTracingConnector;
import tribefire.extension.tracing.model.deployment.connector.LoggingTracingConnector;
import tribefire.extension.tracing.model.deployment.connector.TracingConnector;
import tribefire.extension.tracing.model.deployment.service.HealthCheckProcessor;
import tribefire.extension.tracing.model.deployment.service.TracingAspect;
import tribefire.extension.tracing.model.deployment.service.TracingProcessor;
import tribefire.extension.tracing.model.deployment.service.demo.DemoTracingProcessor;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class TracingModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private DeployablesSpace deployables;

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		//@formatter:off
		//----------------------------
		// CONNECTOR
		//----------------------------
		bindings.bind(LoggingTracingConnector.T)
			.component(TracingConnector.T, tribefire.extension.tracing.connector.api.TracingConnector.class)
			.expertFactory(deployables::loggingTracingConnector);
	
		bindings.bind(JaegerTracingConnector.T)
			.component(TracingConnector.T, tribefire.extension.tracing.connector.api.TracingConnector.class)
			.expertFactory(deployables::jaegerTracingConnector);
		
		bindings.bind(JaegerInMemoryTracingConnector.T)
			.component(TracingConnector.T, tribefire.extension.tracing.connector.api.TracingConnector.class)
			.expertFactory(deployables::jaegerInMemoryTracingConnector);
		
		//----------------------------
		// PROCESSOR
		//----------------------------
		bindings.bind(TracingProcessor.T)
			.component(tfPlatform.binders().serviceProcessor())
			.expertFactory(deployables::tracingProcessor);
		
		//----------------------------
		// ASPECT
		//----------------------------
		bindings.bind(TracingAspect.T)
			.component(tfPlatform.binders().serviceAroundProcessor())
			.expertFactory(deployables::tracingAspect);
		
		//----------------------------
		// DEMO
		//----------------------------
		bindings.bind(DemoTracingProcessor.T)
			.component(tfPlatform.binders().serviceProcessor())
			.expertFactory(deployables::demoTracingProcessor);
		
		//----------------------------
		// DEMO
		//----------------------------
		bindings.bind(HealthCheckProcessor.T)
			.component(tfPlatform.binders().checkProcessor())
			.expertFactory(deployables::healthCheckProcessor);	
		//@formatter:on
	}

}
