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
package tribefire.extension.swagger.model_import.wire.space;

import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.swagger.deployment.ConvertSwaggerModelProcessor;
import com.braintribe.swagger.deployment.ExportSwaggerModelProcessor;
import com.braintribe.swagger.deployment.ImportSwaggerModelProcessor;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class SwaggerModelImportModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;
	
	@Import
	private SwaggerModelImportDeployablesSpace deployables;

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		// @formatter:off
		bindings.bind(ImportSwaggerModelProcessor.T)
			.component(tfPlatform.binders().accessRequestProcessor())
			.expertSupplier(deployables::importSwaggerModelProcessor);
		
		bindings.bind(ExportSwaggerModelProcessor.T)
			.component(tfPlatform.binders().accessRequestProcessor())
			.expertSupplier(deployables::exportSwaggerModelProcessor);
		
		bindings.bind(ConvertSwaggerModelProcessor.T)
			.component(tfPlatform.binders().accessRequestProcessor())
			.expertSupplier(deployables::convertSwaggerModelProcessor);
		// @formatter:on
	}

}
