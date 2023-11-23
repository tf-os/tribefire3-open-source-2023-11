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
package tribefire.extension.appconfiguration.app_configuration.wire.space;

import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.appconfiguration.model.deployment.AppConfigurationProcessor;
import tribefire.extension.appconfiguration.model.deployment.ExportLocalizationsToSpreadsheetProcessor;
import tribefire.extension.appconfiguration.model.deployment.ImportLocalizationsFromSpreadsheetProcessor;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class AppConfigurationModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private AppConfigurationDeployablesSpace deployables;

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		bindings.bind(AppConfigurationProcessor.T) //
				.component(tfPlatform.binders().serviceProcessor()) //
				.expertFactory(deployables::appConfigurationProcessor);

		bindings.bind(ImportLocalizationsFromSpreadsheetProcessor.T) //
				.component(tfPlatform.binders().serviceProcessor()) //
				.expertFactory(deployables::importLocalizationsFromSpreadsheetProcessor);

		bindings.bind(ExportLocalizationsToSpreadsheetProcessor.T) //
				.component(tfPlatform.binders().serviceProcessor()) //
				.expertFactory(deployables::exportLocalizationsToSpreadsheetProcessor);
	}

}
