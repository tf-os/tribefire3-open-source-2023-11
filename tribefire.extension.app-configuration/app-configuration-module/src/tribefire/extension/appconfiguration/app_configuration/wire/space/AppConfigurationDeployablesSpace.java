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

import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.appconfiguration.processing.services.AppConfigurationProcessor;
import tribefire.extension.appconfiguration.processing.services.ExportLocalizationsToSpreadsheetProcessor;
import tribefire.extension.appconfiguration.processing.services.ImportLocalizationsFromSpreadsheetProcessor;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class AppConfigurationDeployablesSpace implements WireSpace {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Managed
	public AppConfigurationProcessor appConfigurationProcessor(
			ExpertContext<tribefire.extension.appconfiguration.model.deployment.AppConfigurationProcessor> context) {
		tribefire.extension.appconfiguration.model.deployment.AppConfigurationProcessor deployable = context.getDeployable();

		AppConfigurationProcessor bean = new AppConfigurationProcessor();
		bean.setSessionSupplier(() -> tfPlatform.systemUserRelated().sessionFactory().newSession(deployable.getAccessId()));
		return bean;
	}

	@Managed
	public ImportLocalizationsFromSpreadsheetProcessor importLocalizationsFromSpreadsheetProcessor(
			ExpertContext<tribefire.extension.appconfiguration.model.deployment.ImportLocalizationsFromSpreadsheetProcessor> context) {
		tribefire.extension.appconfiguration.model.deployment.ImportLocalizationsFromSpreadsheetProcessor deployable = context.getDeployable();

		ImportLocalizationsFromSpreadsheetProcessor bean = new ImportLocalizationsFromSpreadsheetProcessor();
		bean.setSessionSupplier(() -> tfPlatform.systemUserRelated().sessionFactory().newSession(deployable.getAccessId()));
		return bean;
	}

	@Managed
	public ExportLocalizationsToSpreadsheetProcessor exportLocalizationsToSpreadsheetProcessor(
			ExpertContext<tribefire.extension.appconfiguration.model.deployment.ExportLocalizationsToSpreadsheetProcessor> context) {
		tribefire.extension.appconfiguration.model.deployment.ExportLocalizationsToSpreadsheetProcessor deployable = context.getDeployable();

		ExportLocalizationsToSpreadsheetProcessor bean = new ExportLocalizationsToSpreadsheetProcessor();
		bean.setSessionSupplier(() -> tfPlatform.systemUserRelated().sessionFactory().newSession(deployable.getAccessId()));
		return bean;
	}
	
}
