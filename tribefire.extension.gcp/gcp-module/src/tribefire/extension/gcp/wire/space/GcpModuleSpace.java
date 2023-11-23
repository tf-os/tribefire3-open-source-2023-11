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
package tribefire.extension.gcp.wire.space;

import com.braintribe.model.gcp.deployment.GcpConnector;
import com.braintribe.model.gcp.deployment.GcpServiceProcessor;
import com.braintribe.model.gcp.deployment.GcpStorageBinaryProcessor;
import com.braintribe.model.gcp.deployment.HealthCheckProcessor;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.model.processing.gcp.connect.GcpStorageConnector;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class GcpModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private GcpDeployablesSpace deployables;

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		bindings.bind(GcpServiceProcessor.T) //
				.component(tfPlatform.binders().accessRequestProcessor()) //
				.expertSupplier(deployables::gcpServiceProcessor);

		bindings.bind(GcpConnector.T) //
				.component(GcpStorageConnector.class) //
				.expertFactory(deployables::connector);

		bindings.bind(GcpStorageBinaryProcessor.T) //
				.component(tfPlatform.binders().binaryPersistenceProcessor()) //
				.expertFactory(deployables::binaryProcessor) //
				.component(tfPlatform.binders().binaryRetrievalProcessor()) //
				.expertFactory(deployables::binaryProcessor);

		bindings.bind(HealthCheckProcessor.T) //
				.component(tfPlatform.binders().checkProcessor()) //
				.expertSupplier(deployables::healthCheckProcessor);
	}
}
