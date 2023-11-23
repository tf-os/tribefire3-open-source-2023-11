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
package tribefire.extension.aws.wire.space;

import com.braintribe.model.aws.deployment.S3Connector;
import com.braintribe.model.aws.deployment.processor.AwsServiceProcessor;
import com.braintribe.model.aws.deployment.processor.HealthCheckProcessor;
import com.braintribe.model.aws.deployment.processor.S3BinaryProcessor;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * This module's javadoc is yet to be written.
 */
@Managed
public class AwsModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private AwsDeployablesSpace deployables;

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		//@formatter:off
		bindings.bind(AwsServiceProcessor.T) //
			.component(tfPlatform.binders().serviceProcessor()) //
			.expertSupplier(deployables::awsServiceProcessor);
		
		bindings.bind(S3Connector.T) //
			.component(com.braintribe.model.processing.aws.connect.S3Connector.class) //
			.expertFactory(deployables::connector);
		
		bindings.bind(S3BinaryProcessor.T) //
			.component(tfPlatform.binders().binaryRetrievalProcessor()) //
			.expertFactory(deployables::binaryProcessor) //
			.component(tfPlatform.binders().binaryPersistenceProcessor()) //
			.expertFactory(deployables::binaryProcessor);
		
		bindings.bind(HealthCheckProcessor.T) //
			.component(tfPlatform.binders().checkProcessor()) //
			.expertSupplier(this.deployables::healthCheckProcessor);
		//@formatter:on
	}
}
